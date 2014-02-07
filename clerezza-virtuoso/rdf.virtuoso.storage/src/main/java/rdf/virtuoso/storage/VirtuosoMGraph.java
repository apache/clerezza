/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package rdf.virtuoso.storage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.AbstractMGraph;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wymiwyg.commons.util.collections.BidiMap;
import org.wymiwyg.commons.util.collections.BidiMapImpl;

import rdf.virtuoso.storage.access.VirtuosoWeightedProvider;
import virtuoso.jdbc4.VirtuosoConnection;
import virtuoso.jdbc4.VirtuosoException;
import virtuoso.jdbc4.VirtuosoExtendedString;
import virtuoso.jdbc4.VirtuosoRdfBox;
import virtuoso.jdbc4.VirtuosoResultSet;

/**
 * Implementation of MGraph for the Virtuoso quad store.
 * 
 * @author enridaga
 * 
 */
public class VirtuosoMGraph extends AbstractMGraph implements MGraph,
		LockableMGraph {
	
	// XXX This may be a configuration of the weighted provider
	private static final int PLAN_B_LITERAL_SIZE = 50000;

	private static final int CHECKPOINT = 1000;
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	/**
	 * Bidirectional map for managing the conversion from virtuoso blank nodes
	 * (strings) to clerezza blank nodes and vice versa.
	 */
	private final BidiMap<VirtuosoBNode, BNode> bnodesMap;

	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(VirtuosoMGraph.class);

	/**
	 * The name of the graph
	 */
	private String name = null;
	private int size = 0;

	private VirtuosoGraph readOnly = null;
	private VirtuosoWeightedProvider provider = null;

	/**
	 * Creates a {@link VirtuosoMGraph} Virtuoso MGraph binds directly to the
	 * store.
	 * 
	 * @param connection
	 */
	public VirtuosoMGraph(String name, VirtuosoWeightedProvider provider) {
		logger.debug("VirtuosoMGraph(String {}, VirtuosoWeightedProvider {})",
				name, provider);
		this.name = name;
		this.provider = provider;
		this.bnodesMap = new BidiMapImpl<VirtuosoBNode, BNode>();
	}

	/**
	 * Gets the connection
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected VirtuosoConnection getConnection() throws SQLException,
			ClassNotFoundException {
		logger.debug("getConnection()");
		return this.provider.getConnection();
	}

	@Override
	public ReadWriteLock getLock() {
		logger.debug("getLock()");
		return lock;
	}

	@Override
	public Graph getGraph() {
		logger.debug("getGraph()");
		return asVirtuosoGraph();
	}

	public VirtuosoGraph asVirtuosoGraph() {
		logger.debug("asVirtuosoGraph()");
		if (this.readOnly == null) {
			logger.debug("create embedded singleton read-only instance");
			this.readOnly = new VirtuosoGraph(name, provider);
		}
		return readOnly;
	}

	@Override
	protected Iterator<Triple> performFilter(NonLiteral subject,
			UriRef predicate, Resource object) {
		if (logger.isDebugEnabled()) {
			logger.debug("performFilter(NonLiteral s, UriRef p, Resource o)");
			logger.debug(" > s: {}", subject);
			logger.debug(" > p: {}", predicate);
			logger.debug(" > o: {}", object);
		}
		StringBuilder sb = new StringBuilder();
		String virtSubject = toVirtSubject(subject);
		String virtPredicate = toVirtPredicate(predicate);
		String virtObject = toVirtObject(object);
		sb.append("SPARQL SELECT ");
		if (virtSubject != null) {
			sb.append(" ").append(virtSubject).append(" as ?subject");
		} else {
			sb.append(" ?subject ");
		}
		if (virtPredicate != null) {
			sb.append(" ").append(virtPredicate).append(" as ?predicate");
		} else {
			sb.append(" ?predicate ");
		}
		if (virtObject != null) {
			sb.append(" ").append(virtObject).append(" as ?object");
		} else {
			sb.append(" ?object ");
		}
		sb.append(" WHERE { GRAPH <").append(this.getName()).append("> { ");
		if (virtSubject != null) {
			sb.append(" ").append(virtSubject).append(" ");
		} else {
			sb.append(" ?subject ");
		}
		if (virtPredicate != null) {
			sb.append(" ").append(virtPredicate).append(" ");
		} else {
			sb.append(" ?predicate ");
		}
		if (virtObject != null) {
			sb.append(" ").append(virtObject).append(" ");
		} else {
			sb.append(" ?object ");
		}

		sb.append(" } } ");

		String sql = sb.toString();
//		logger.trace("Executing SQL: {}", sql);
		Statement st = null;
		List<Triple> list = null;
		Exception e = null;
		VirtuosoConnection connection = null;
		VirtuosoResultSet rs = null;
		try {
			readLock.lock();
			connection = provider.getConnection();
			st = connection.createStatement();
			st.execute(sql);
			rs = (VirtuosoResultSet) st.getResultSet();
			list = new ArrayList<Triple>();
			while (rs.next()) {
				list.add(new TripleBuilder(rs.getObject(1), rs.getObject(2), rs
						.getObject(3)).build());
			}
		} catch (VirtuosoException ve) {
			logger.error("ERROR while executing statement", ve);
			logger.error(" executing SQL: {}", sql);
			e = ve;
		} catch (SQLException e1) {
			logger.error("ERROR while executing statement", e1);
			logger.error(" executing SQL: {}", sql);
			e = e1;
		} catch (ClassNotFoundException e1) {
			logger.error("ERROR while executing statement", e1);
			e = e1;
		} finally {
			readLock.unlock();
			try {
				if (rs != null)
					rs.close();
			} catch (Exception ex) {
			}
			;
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
			}
			;
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (VirtuosoException e1) {
				logger.error("Cannot close connection", e1);
			}
		}

		if (list == null || e != null) {
			throw new RuntimeException(e);
		}
		return list.iterator();
	}

	/**
	 * We load the size every time it is requested.
	 */
	@Override
	public int size() {
		logger.debug("size()");
		loadSize();
		return this.size;
	}

	@Override
	public void clear() {
		logger.debug("clear()");
		String SQL = "SPARQL CLEAR GRAPH <" + this.getName() + ">";
		this.writeLock.lock();
		VirtuosoConnection connection = null;
		Exception e = null;
		Statement st = null;
		try {
			logger.debug("Executing SQL: {}", SQL);
			connection = getConnection();
			st = connection.createStatement();
			boolean success = st.execute(SQL);
			if (success) {
				this.size = 0;
			} else {
				e = new RuntimeException(
						"Problems on clear() method. Cannot clear the graph!");
			}
		} catch (VirtuosoException ve) {
			logger.error("ERROR while executing statement", e);
			e = ve;
		} catch (SQLException se) {
			logger.error("ERROR while executing statement", e);
			e = se;
		} catch (ClassNotFoundException e1) {
			e = e1;
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
			}
			;
			if (connection != null) {
				try {
					connection.close();
				} catch (VirtuosoException e1) {
					logger.error("Cannot close connection", e1);
				}
			}
			this.writeLock.unlock();
		}
		if (e != null) {
			throw new RuntimeException(e);
		}
	}

	private void loadSize() {
		logger.debug("loadSize()");
		String SQL = "SPARQL SELECT COUNT(*) FROM <" + this.getName()
				+ "> WHERE { ?s ?p ?o } ";
		int size = 0;
		logger.debug("loadSize() pre lock");
		this.readLock.lock();
		logger.debug("loadSize() post lock");
		VirtuosoConnection connection = null;
		Exception e = null;
		Statement st = null;
		VirtuosoResultSet rs = null;
		try {
			logger.debug("Executing SQL: {}", SQL);
			connection = getConnection();
			st = connection.createStatement();
			rs = (VirtuosoResultSet) st.executeQuery(SQL);
			rs.next();
			size = rs.getInt(1);
		} catch (VirtuosoException ve) {
			logger.error("ERROR while executing statement", e);
			e = ve;
		} catch (SQLException se) {
			logger.error("ERROR while executing statement", e);
			e = se;
		} catch (ClassNotFoundException e1) {
			e = e1;
		} finally {
			this.readLock.unlock();
			logger.debug("loadSize() unlock");
			try {
				if (rs != null)
					rs.close();
			} catch (Exception ex) {
			}
			;
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
			}
			;
			if (connection != null) {
				try {
					connection.close();
				} catch (VirtuosoException e1) {
					logger.error("Cannot close connection", e1);
				}
			}
		}
		if (e != null) {
			throw new RuntimeException(e);
		}
		this.size = size;
	}

	private int checkpoint = 0;
	private void checkpoint(boolean force){
		if(checkpoint <= CHECKPOINT && force == false){
			checkpoint++;
			return;
		}else{
			checkpoint = 0;
		}
		VirtuosoConnection connection = null;
		Exception e = null;
		Statement st = null;
		try {
			connection = provider.getConnection();
			st = connection.createStatement();
			st.execute("Checkpoint");
			logger.info("Checkpoint.");
		} catch (VirtuosoException ve) {
			logger.error("ERROR while executing statement", ve);
			e = ve;
		} catch (SQLException se) {
			logger.error("ERROR while executing statement", se);
			e = se;
		} catch (ClassNotFoundException e1) {
			e = e1;
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
			}
			;
			if (connection != null) {
				try {
					connection.close();
				} catch (VirtuosoException e1) {
					logger.error("Cannot close connection", e1);
				}
			}
		}
		if (e != null) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean performAddPlanB(Triple triple){
		StringBuilder b = new StringBuilder();
		b.append(toVirtSubject(triple.getSubject()))
		.append(" ")
		.append(toVirtPredicate(triple.getPredicate()))
		.append(" ")
		.append(toVirtObject(triple.getObject()))
		.append(" . ");
		String sql = "db.dba.ttlp(?, '', '" + this.getName() + "2', 0)";
		logger.debug("Exec Plan B: {}", sql);
		writeLock.lock();
		VirtuosoConnection connection = null;
		Exception e = null;
		PreparedStatement st = null;
		try {
			connection = provider.getConnection();
			st = connection.prepareStatement(sql);
			st.setNString(1, b.toString());
			st.execute();
		} catch (VirtuosoException ve) {
			logger.error("ERROR while executing statement", ve);
			e = ve;
		} catch (SQLException se) {
			logger.error("ERROR while executing statement", se);
			e = se;
		} catch (ClassNotFoundException e1) {
			e = e1;
		} finally {
			writeLock.unlock();
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
			}
			;
			if (connection != null) {
				try {
					connection.close();
				} catch (VirtuosoException e1) {
					logger.error("Cannot close connection", e1);
				}
			}
			checkpoint(true);
		}
		if (e != null) {
			logger.error("S {}", triple.getSubject());
			logger.error("P {}", triple.getPredicate());
			logger.error("O {}", triple.getObject());
			logger.error(" O size: {}", triple.getObject().toString().length());
			logger.error("Sql: {}", sql);
			throw new RuntimeException(e);
		}
		return true;
	}
	protected boolean performAdd(Triple triple) {
		logger.debug("performAdd(Triple {})", triple);
		
		// XXX If the object is a very long literal we use plan B
		if (triple.getObject() instanceof Literal) {
			if (((Literal) triple.getObject()).getLexicalForm().length() > PLAN_B_LITERAL_SIZE) {
				return performAddPlanB(triple);
			}
		}

		// String sql = getAddSQLStatement(triple);
		String sql = INSERT;
		// logger.info("Executing SQL: {}", sql);
		// logger.info("--- {} ", sql);
		writeLock.lock();
		VirtuosoConnection connection = null;
		Exception e = null;
		PreparedStatement st = null;
		try {
			connection = getConnection();
			// st = connection.createStatement();
			st = connection.prepareStatement(sql);
			bindGraph(st, 1, new UriRef(getName()));
			bindSubject(st, 2, triple.getSubject());
			bindPredicate(st, 3, triple.getPredicate());
			bindValue(st, 4, triple.getObject());
			
			st.execute();
		} catch (VirtuosoException ve) {
			logger.error("ERROR while executing statement", ve);
			e = ve;
		} catch (SQLException se) {
			logger.error("ERROR while executing statement", se);
			e = se;
		} catch (ClassNotFoundException e1) {
			e = e1;
		} finally {
			writeLock.unlock();
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
			}
			;
			if (connection != null) {
				try {
					connection.close();
				} catch (VirtuosoException e1) {
					logger.error("Cannot close connection", e1);
				}
			}
			checkpoint(false);
		}
		if (e != null) {
			logger.error("S {}", triple.getSubject());
			logger.error("P {}", triple.getPredicate());
			logger.error("O {}", triple.getObject());
			logger.error(" O size: {}", triple.getObject().toString().length());
			logger.error("Sql: {}", sql);
			throw new RuntimeException(e);
		}
		return true;
	}

	private void bindValue(PreparedStatement st, int i, Resource object) throws SQLException {
		if (object instanceof UriRef) {
			st.setInt(i, 1);
			st.setString(i + 1,((UriRef)object).getUnicodeString());
			st.setNull(i + 2, java.sql.Types.VARCHAR);
		} else if (object instanceof BNode) {
			st.setInt(i, 1);
			st.setString(i + 1, toVirtBnode((BNode) object).getSkolemId());
			st.setNull(i + 2, java.sql.Types.VARCHAR);
		} else if (object instanceof TypedLiteral) {
			TypedLiteral tl = ((TypedLiteral)object);
			st.setInt(i, 4);
			// if datatype is XMLLiteral
			String lf = tl.getLexicalForm();
			if(tl.getDataType().getUnicodeString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral")){
				//lf = prepareString(lf, true).toString();
			}

			// XXX
			if(object.toString().length() == 416){
				logger.warn("416 Chars length");
				lf += " XXXXXXXXXX  ";
			}
			st.setString(i+1, lf);
			st.setString(i+2, tl.getDataType().getUnicodeString());
		} else if (object instanceof PlainLiteral) {
			PlainLiteral pl = (PlainLiteral) object;
			if(pl.getLanguage() != null){
				st.setInt(i, 5);
				st.setString(i + 1, pl.getLexicalForm());
				st.setString(i + 2, pl.getLanguage().toString());
			}else{
				st.setInt(i, 3);
				st.setString(i + 1, pl.getLexicalForm());
				st.setNull(i + 2, java.sql.Types.VARCHAR);
			}
		}else throw new IllegalArgumentException(object.toString());
	}

	private void bindPredicate(PreparedStatement st, int i, UriRef predicate)
			throws SQLException {
		st.setString(i, predicate.getUnicodeString());
	}

	private void bindSubject(PreparedStatement st, int i, NonLiteral subject)
			throws SQLException {
		if (subject instanceof UriRef) {
			st.setString(i, ((UriRef) subject).getUnicodeString());
		} else {
			st.setString(i, toVirtBnode((BNode) subject).getSkolemId());
		}
	}

	private void bindGraph(PreparedStatement st, int i, UriRef uriRef)
			throws SQLException {
		st.setString(i, uriRef.getUnicodeString());
	}

	protected boolean performRemove(Triple triple) {
		logger.debug("performRemove(Triple triple)", triple);
		String sql = getRemoveSQLStatement(triple);
		logger.debug("Executing SQL: {}", sql);
		writeLock.lock();
		VirtuosoConnection connection = null;
		Exception e = null;
		Statement st = null;
		try {
			connection = getConnection();
			st = connection.createStatement();
			st.execute(sql);
		} catch (SQLException se) {
			logger.error("ERROR while executing statement", e);
			e = se;
		} catch (ClassNotFoundException e1) {
			e = e1;
		} finally {
			writeLock.unlock();
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
			}
			;
			if (connection != null) {
				try {
					connection.close();
				} catch (VirtuosoException e1) {
					logger.error("Cannot close connection", e1);
				}
			}
			checkpoint(false);
		}
		if (e != null) {
			throw new RuntimeException(e);
		}
		return true;
	}

	/**
	 * Returns the graph name
	 * 
	 * @return
	 */
	public String getName() {
		logger.debug("getName()");
		return name;
	}

	/**
	 * Build a virtuoso bnode mapped to the intern
	 * 
	 * @param virtbnode
	 * @return
	 */
	private VirtuosoBNode toBNode(String virtbnode) {
		logger.debug("toBNode(String {})", virtbnode);
		VirtuosoBNode bnode;
		bnode = new VirtuosoBNode(virtbnode);
		return bnode;
	}

	/**
	 * Generate a new local bnode to be used in virtuoso queries
	 * 
	 * @return
	 */
	private VirtuosoBNode nextVirtBnode(BNode bn) {
		logger.debug("nextVirtBnode(BNode)");
		// maxVirtBnodeIndex++;

		String temp_graph = "<urn:x-virtuoso:bnode-tmp>";
		String bno = new StringBuilder().append('<').append(bn).append('>')
				.toString();
		String _bnodeObject = "<urn:x-virtuoso:bnode:object>";
		String sql_insert = new StringBuilder()
				.append("sparql INSERT IN GRAPH ").append(temp_graph)
				.append(" { [] ").append(_bnodeObject).append(' ').append(bno)
				.append(" } ").toString();
		logger.trace(" insert: {}", sql_insert);
		String sql_select = new StringBuilder()
				.append("sparql SELECT ?S FROM ").append(temp_graph)
				.append(" WHERE { ?S ").append(_bnodeObject).append(' ')
				.append(bno).append(' ').append(" } LIMIT 1").toString();
		logger.trace(" select new bnode: {}", sql_select);
		String sql_delete = new StringBuilder().append("sparql DELETE FROM ")
				.append(temp_graph).append(" { ?S ").append(_bnodeObject)
				.append(' ').append(bno).append(" } ").toString();
		logger.trace(" delete tmp triple: {}", sql_delete);

		// logger.info("SQL {}", sql);
		writeLock.lock();
		VirtuosoConnection connection = null;
		Exception e = null;
		Statement st = null;
		VirtuosoResultSet rs = null;
		String bnodeId = null;
		try {
			connection = getConnection();
			st = connection.createStatement();
			st.executeUpdate(sql_insert);
			rs = (VirtuosoResultSet) st.executeQuery(sql_select);
			rs.next();
			bnodeId = rs.getString(1);
			st.executeUpdate(sql_delete);
		} catch (VirtuosoException ve) {
			logger.error("ERROR while executing statement", ve);
			e = ve;
		} catch (SQLException se) {
			logger.error("ERROR while executing statement", se);
			e = se;
		} catch (ClassNotFoundException e1) {
			logger.error("ERROR while executing statement", e1);
			e = e1;
		} finally {
			writeLock.unlock();
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
			}
			;
			try {
				if (rs != null)
					rs.close();
			} catch (Exception ex) {
			}
			;
			if (connection != null) {
				try {
					connection.close();
				} catch (VirtuosoException e1) {
					logger.error("Cannot close connection", e1);
				}
			}
			checkpoint(false);
		}
		if (e != null) {
			throw new RuntimeException(e);
		}
		return new VirtuosoBNode(bnodeId);
	}

	/**
	 * 
	 * @param bnode
	 * @return
	 */
	private VirtuosoBNode toVirtBnode(BNode bnode) {
		logger.debug("toVirtBnode(BNode {})", bnode);
		if (bnode instanceof VirtuosoBNode) {
			return ((VirtuosoBNode) bnode);
		} else {
			VirtuosoBNode virtBnode = bnodesMap.getKey(bnode);
			if (virtBnode == null) {
				// We create a local bnode mapped to the BNode given
				virtBnode = nextVirtBnode(bnode);
				bnodesMap.put(virtBnode, bnode);
			}
			return virtBnode;
		}
	}

	private static String INSERT = "SPARQL INSERT INTO iri(??) {`iri(??)` `iri(??)` "
			+ "`bif:__rdf_long_from_batch_params(??,??,??)`}";

//	private String getAddSQLStatement(Triple triple) {
//		logger.debug("getAddSQLStatement(Triple {})", triple);
//		StringBuilder sb = new StringBuilder();
//		String subject = toVirtSubject(triple.getSubject());
//		String predicate = toVirtPredicate(triple.getPredicate());
//		String object = toVirtObject(triple.getObject());
//		String sql = sb.append("SPARQL INSERT INTO <").append(this.getName())
//				.append("> { ").append(subject).append(" ").append(predicate)
//				.append(" ").append(object).append(" }").toString();
//		return sql;
//	}

	private String getRemoveSQLStatement(Triple triple) {
		logger.debug("getRemoveSQLStatement(Triple {})", triple);
		StringBuilder sb = new StringBuilder();
		String subject = toVirtSubject(triple.getSubject());
		String predicate = toVirtPredicate(triple.getPredicate());
		String object = toVirtObject(triple.getObject());
		String sql = sb.append("SPARQL DELETE FROM <").append(this.getName())
				.append("> { ").append(subject).append(" ").append(predicate)
				.append(" ").append(object).append(" } FROM <")
				.append(this.getName()).append("> ").append(" WHERE { ")
				.append(subject).append(" ").append(predicate).append(" ")
				.append(object).append(" }").toString();
		return sql;
	}

	/**
	 * Returns a string to be used inline in SQL statements as Object of a triple.
	 * 
	 * @param object
	 * @return
	 */
	private String toVirtObject(Resource object) {
		logger.debug("toVirtObject(Resource {})", object);
		if (object == null)
			return null;
		if (object instanceof UriRef) {
			return toVirtIri((UriRef) object);
		} else if (object instanceof BNode) {
			return toVirtBnode((BNode) object).asSkolemIri();
		} else if (object instanceof PlainLiteral) {
			return toVirtPlainLiteral((PlainLiteral) object);
		} else if (object instanceof TypedLiteral) {
			return toVirtTypedLiteral((TypedLiteral) object);
		}
		// XXX throw exception here?
		return null;
	}

	/**
	 * Returns a string to be used in SQL statements.
	 * 
	 * @param object
	 * @return
	 */
	private String toVirtTypedLiteral(TypedLiteral object) {
		logger.debug("toVirtTypedLiteral(TypedLiteral {})", object);
		UriRef dt = object.getDataType();
		String literal = object.getLexicalForm();// .replaceAll("\"", "\\\\\"");
		StringBuilder prepared;
		// If XMLLiteral, prepare XML entities
		prepared = prepareString(
				literal,
				dt.getUnicodeString()
						.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral"));
		return new StringBuilder().append('"').append('"').append('"')
				.append(prepared).append('"').append('"').append('"')
				.append("^^").append(toVirtIri(dt)).toString();
	}

	private StringBuilder prepareString(String str, boolean xml) {
		StringBuilder retStr = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			int cp = Character.codePointAt(str, i);
			int charCount = Character.charCount(cp);
			if (charCount > 1) {
				i += charCount - 1; // 2.
				if (i >= str.length()) {
					throw new IllegalArgumentException("truncated unexpectedly");
				}
			}

			if (cp < 128) {
				retStr.appendCodePoint(cp);
			} else {
				if (xml) {
					retStr.append(String.format("&#x%04x;", cp));
				} else {
					retStr.append(String.format("\\u%04x", cp));
				}
			}
		}
		return retStr;
	}

	/**
	 * Returns a string to be used in SQL statements.
	 * 
	 * @param object
	 * @return
	 */
	private String toVirtPlainLiteral(PlainLiteral object) {
		logger.debug("toVirtPlainLiteral(PlainLiteral {})", object);
		Language lang = object.getLanguage();
		String literal = object.getLexicalForm();// .replaceAll("\"", "\\\\\"");
		StringBuilder sb = new StringBuilder().append('"').append('"')
				.append('"').append(prepareString(literal, false)).append('"')
				.append('"').append('"');
		if (lang == null) {
			return sb.toString();
		} else {
			return sb.append("@").append(lang).toString();
		}
	}

	/**
	 * Returns a string to be used in SQL statements as Predicate of a triple.
	 * 
	 * @param predicate
	 * @return
	 */
	private String toVirtPredicate(UriRef predicate) {
		logger.debug("toVirtPredicate(UriRef {}) ", predicate);
		if (predicate == null)
			return null;
		return toVirtIri(predicate);
	}

	private String toVirtIri(UriRef ur) {
		logger.debug("toVirtIri(UriRef {})", ur);
		return "<" + ur.getUnicodeString() + ">";
	}

	/**
	 * Returns a string to be used in SQL statements as Subject of a triple.
	 * 
	 * @param subject
	 * @return
	 */
	private String toVirtSubject(NonLiteral subject) {
		logger.debug("toVirtSubject(NonLiteral {})", subject);
		if (subject == null) {
			return null;
		}
		if (subject instanceof UriRef) {
			return toVirtIri((UriRef) subject);
		} else if (subject instanceof BNode) {
			return toVirtBnode((BNode) subject).asSkolemIri();
		} else {
			// These should be the only 2 implementations
			throw new IllegalArgumentException(
					"subject must be BNode or UriRef");
		}
	}

	/**
	 * Builds a clerezza Triple from Virtuoso result types
	 * 
	 */
	private class TripleBuilder {

		Object s = null;
		Object p = null;
		Object o = null;

		public TripleBuilder(Object s, Object p, Object o) {
			if (logger.isDebugEnabled()) {
				logger.debug("TripleBuilder(Object s, Object p, Object o)");
				logger.debug("> s: {}", s);
				logger.debug("> p: {}", p);
				logger.debug("> o: {}", o);
			}
			this.s = s;
			this.p = p;
			this.o = o;
		}

		private NonLiteral buildSubject() {
			logger.debug("TripleBuilder.getSubject() : {}", s);
			if (s instanceof VirtuosoExtendedString) {
				VirtuosoExtendedString vs = (VirtuosoExtendedString) s;
				if (vs.iriType == VirtuosoExtendedString.IRI
						&& (vs.strType & 0x01) == 0x01) {
					// Subject is IRI
					return new UriRef(vs.str);
				} else if (vs.iriType == VirtuosoExtendedString.BNODE) {
					return VirtuosoMGraph.this.toBNode(vs.str);
				} else {
					// !Cannot happen
					throw new IllegalStateException(
							"Subject must be an IRI or a BNODE");
				}
			} else {
				throw new IllegalStateException(
						"Subject must be an instance of VirtuosoExtendedString");
			}
		}

		private UriRef buildPredicate() {
			logger.debug("TripleBuilder.getPredicate() : {}", p);
			if (p instanceof VirtuosoExtendedString) {
				VirtuosoExtendedString vs = (VirtuosoExtendedString) p;
				if (vs.iriType == VirtuosoExtendedString.IRI
						&& (vs.strType & 0x01) == 0x01) {
					// Subject is IRI
					return new UriRef(vs.str);
				} else {
					// !Cannot happen
					throw new IllegalStateException("Predicate must be an IRI ");
				}
			} else {
				throw new IllegalStateException("Predicate must be an IRI");
			}
		}

		Resource buildObject() {
			logger.debug("TripleBuilder.getObject() : {}", o);
			
			if (o instanceof VirtuosoExtendedString) {
				// In case is IRI
				VirtuosoExtendedString vs = (VirtuosoExtendedString) o;
				if (vs.iriType == VirtuosoExtendedString.IRI
						&& (vs.strType & 0x01) == 0x01) {
					// Is IRI
					return new UriRef(vs.str);
				} else if (vs.iriType == VirtuosoExtendedString.BNODE) {
					//
					return VirtuosoMGraph.this.toBNode(vs.str);
				} else {
					// Is a plain literal
					return new PlainLiteralImpl(vs.str);
				}
			} else if (o instanceof VirtuosoRdfBox) {
				// In case is typed literal
				VirtuosoRdfBox rb = (VirtuosoRdfBox) o;
				
				String value;
				if (rb.rb_box.getClass().isAssignableFrom(String.class)) {
					value = (String) rb.rb_box;
					String lang = rb.getLang();
					String type = rb.getType();
					if (type == null) {
						Language language = lang == null ? null : new Language(
								lang);
						return new PlainLiteralImpl(value, language);
					} else {
						return new TypedLiteralImpl(value, new UriRef(type));
					}
				} else if (rb.rb_box instanceof VirtuosoExtendedString) {
					VirtuosoExtendedString vs = (VirtuosoExtendedString) rb.rb_box;

					if (vs.iriType == VirtuosoExtendedString.IRI
							&& (vs.strType & 0x01) == 0x01) {
						// Is IRI
						return new UriRef(vs.str);
					} else if (vs.iriType == VirtuosoExtendedString.BNODE) {
						//
						return VirtuosoMGraph.this.toBNode(vs.str);
					} else {
						String type = rb.getType();
						if (type == null) {
							String lang = rb.getLang();
							if(lang != null){
								return new PlainLiteralImpl(vs.str, new Language(lang));
							}
							// Is a plain literal
							return new PlainLiteralImpl(vs.str);
						} else {
							return new TypedLiteralImpl(vs.str,
									new UriRef(type));
						}
					}
				}
			} else if (o == null) {
				// Raise an exception
				throw new IllegalStateException("Object cannot be NULL!");
			}

			// FIXME (not clear this...)
			return new PlainLiteralImpl(o.toString());
		}

		public Triple build() {
			logger.debug("TripleBuilder.build()");
			return new TripleImpl(buildSubject(), buildPredicate(),
					buildObject());
		}
	}

	/**
	 * Must be a VirtuosoMGraph with the same name. Subclasses are not assumed
	 * to be equals (VirtuosoGraph is not the same as VirtuosoMGraph)
	 */
	public boolean equals(Object o) {
		logger.debug("equals({})", o.getClass());
		// It must be an instance of VirtuosoMGraph
		if (o.getClass().equals(VirtuosoMGraph.class)) {
			logger.debug("{} is a VirtuosoMGraph)", o);
			if (((VirtuosoMGraph) o).getName().equals(this.getName())) {
				logger.debug("Names are equal! They are equal!");
				return true;
			}
		} else {
			logger.debug("Not a VirtuosoMGraph instance: {}", o.getClass());
		}
		return false;
	}
}
