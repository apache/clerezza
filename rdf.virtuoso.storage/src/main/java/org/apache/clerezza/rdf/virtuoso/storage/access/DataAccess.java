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
package org.apache.clerezza.rdf.virtuoso.storage.access;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.PooledConnection;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;
import org.apache.clerezza.rdf.core.sparql.query.Variable;
import org.apache.clerezza.rdf.virtuoso.storage.VirtuosoBNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wymiwyg.commons.util.collections.BidiMap;
import org.wymiwyg.commons.util.collections.BidiMapImpl;

import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc4.VirtuosoException;
import virtuoso.jdbc4.VirtuosoExtendedString;
import virtuoso.jdbc4.VirtuosoRdfBox;

/**
 * 
 * @author enridaga
 *
 */
public class DataAccess {
	private Logger logger = LoggerFactory.getLogger(DataAccess.class);
	
	final static String DRIVER = "virtuoso.jdbc4.Driver";

	// XXX This is only used to create a new bnode identifier in virtuoso
	final static String INSERT_NEW_BNODE = "SPARQL INSERT INTO iri(??) { [] `iri(??)` "
			+ "`iri(??)`}";

	// XXX This is only used to delete a new bnode identifier in virtuoso
	final static String DELETE_NEW_BNODE = "SPARQL DELETE FROM iri(??) { ?s ?p ?o } WHERE { ?s ?p ?o . filter( ?p = iri(??) && "
			+ " ?o = iri(??) ) }";

	final static String INSERT_QUAD = "SPARQL INSERT INTO iri(??) {`iri(??)` `iri(??)` "
			+ "`bif:__rdf_long_from_batch_params(??,??,??)`}";
	final static String DELETE_QUAD = "SPARQL DELETE FROM iri(??) { ?s ?p ?o } WHERE { ?s ?p ?o . filter( ?s = iri(??) && ?p = iri(??) && "
			+ " ?o = bif:__rdf_long_from_batch_params(??,??,??) ) } ";
	final static String LIST_GRAPHS = "SPARQL SELECT DISTINCT ?G WHERE {GRAPH ?G {[] [] []} }";
	final static String CLEAR_GRAPH = "SPARQL CLEAR GRAPH iri(??)";
	final static String COUNT_TRIPLES_OF_GRAPH = "SPARQL SELECT COUNT(*) WHERE { bind( iri(??) as ?graph ) . graph ?graph { [] [] [] } }";
	final static String SELECT__ = "SPARQL SELECT ?subject ?predicate ?object WHERE { bind( iri(??) as ?graph ) . GRAPH ?graph { ?subject ?predicate ?object ";
	final static String SELECT_TRIPLES_NULL_NULL_NULL = SELECT__ + " } }";
	final static String SELECT_TRIPLES_S_NULL_NULL = SELECT__ + " . FILTER( ?subject = iri(??) ) } }";
	final static String SELECT_TRIPLES_S_P_NULL = SELECT__ + " . FILTER( ?subject = iri(??) && ?predicate = iri(??) ) } }";
	final static String SELECT_TRIPLES_S_P_O = SELECT__ + " . FILTER( ?subject = iri(??) && ?predicate = iri(??) && ?object = bif:__rdf_long_from_batch_params(??,??,??) ) } }";
	final static String SELECT_TRIPLES_NULL_P_NULL = SELECT__ + " . FILTER( ?predicate = iri(??) ) } }";
	final static String SELECT_TRIPLES_NULL_P_O = SELECT__ + " . FILTER( ?predicate = iri(??) && ?object = bif:__rdf_long_from_batch_params(??,??,??) ) } }";
	final static String SELECT_TRIPLES_NULL_NULL_O = SELECT__ + " . FILTER( ?object = bif:__rdf_long_from_batch_params(??,??,??) ) } }";
	final static String SELECT_TRIPLES_S_NULL_O = SELECT__ + " . FILTER( ?subject = iri(??) && ?object = bif:__rdf_long_from_batch_params(??,??,??) ) } }";

	private final static String[] filterQueries = new String[] {
			SELECT_TRIPLES_NULL_NULL_NULL, SELECT_TRIPLES_S_NULL_NULL,
			SELECT_TRIPLES_S_P_O, SELECT_TRIPLES_NULL_NULL_O,
			SELECT_TRIPLES_NULL_P_NULL, SELECT_TRIPLES_S_P_NULL,
			SELECT_TRIPLES_NULL_P_O, SELECT_TRIPLES_S_NULL_O };

	// rdf:XMLLiteral needs to be shadowed in the storage because of a BUG in virtuoso
	private final static UriRef XMLLiteral = new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");
	private final static UriRef XMLLiteralShadowed = new UriRef("urn:x-clerezza:rdf#XMLLiteral");
	
	/**
	 * Bidirectional map for managing the conversion from virtuoso blank nodes
	 * (strings) to clerezza blank nodes and vice versa.
	 */
	private final BidiMap<VirtuosoBNode, BNode> bnodesMap;
	
	private VirtuosoConnectionPoolDataSource pds;

	private int preftechSize;

	// We protect the constructor from outside the package...
	DataAccess(VirtuosoConnectionPoolDataSource pds) {
		this.pds = pds;
		this.preftechSize = 200;
		// Init bnodes map
		this.bnodesMap = new BidiMapImpl<VirtuosoBNode, BNode>();
	}

	private Connection getConnection(){
		return AccessController
				.doPrivileged(new PrivilegedAction<Connection>() {
					public Connection run() {
						try {
							PooledConnection pconn = pds.getPooledConnection();
							return pconn.getConnection();
						} catch (Throwable e) {
							throw new RuntimeException(e);
						}
					}
				});
	}

	private PreparedStatement getStatement(Connection connection, String query) throws SQLException {
		PreparedStatement ps = connection.prepareStatement(query);
		ps.setFetchSize(this.preftechSize);
		return ps;
	}

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

	public void close() {

	}

	private void close(Object... resources) {
		for (Object o : resources) {
			try {
				if (o instanceof ResultSet ) {
					((ResultSet) o).close();
				} else if (o instanceof Statement) {
					((Statement) o).close();
				}else if (o instanceof Connection) {
					((Connection) o).close();
				}else{
					throw new SQLException("XXX Unsupported resource: " + o.toString());
				}
			} catch (SQLException e) {
				logger.error("Cannot close resource of type {}", o.getClass());
			}
		}
	}
	
	private void bindValue(PreparedStatement st, int i, Resource object)
			throws SQLException {
		if (object instanceof UriRef) {
			st.setInt(i, 1);
			st.setString(i + 1, ((UriRef) object).getUnicodeString());
			st.setNull(i + 2, java.sql.Types.VARCHAR);
		} else if (object instanceof BNode) {
			st.setInt(i, 1);
			st.setString(i + 1, ((VirtuosoBNode) object).getSkolemId());
			st.setNull(i + 2, java.sql.Types.VARCHAR);
		} else if (object instanceof TypedLiteral) {
			TypedLiteral tl = ((TypedLiteral) object);
			st.setInt(i, 4);
			st.setString(i + 1, tl.getLexicalForm());
			
			// XXX Shadow rdf:XMLLiteral datatype
			// See CLEREZZA-XXX
			UriRef dt = tl.getDataType();
			if(dt.equals(XMLLiteral)){
				dt = XMLLiteralShadowed;
			}
			st.setString(i + 2, dt.getUnicodeString());
		} else if (object instanceof PlainLiteral) {
			PlainLiteral pl = (PlainLiteral) object;
			if (pl.getLanguage() != null) {
				st.setInt(i, 5);
				st.setString(i + 1, pl.getLexicalForm());
				st.setString(i + 2, pl.getLanguage().toString());
			} else {
				st.setInt(i, 3);
				st.setString(i + 1, pl.getLexicalForm());
				st.setNull(i + 2, java.sql.Types.VARCHAR);
			}
		} else
			throw new IllegalArgumentException(object.toString());
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
			st.setString(i, ((VirtuosoBNode) subject).getSkolemId());
		}
	}

	private void bindGraph(PreparedStatement st, int i, UriRef uriRef)
			throws SQLException {
		st.setString(i, uriRef.getUnicodeString());
	}
	

	private void bindGraph(PreparedStatement st, int i, String uri)
			throws SQLException {
		st.setString(i, uri);
	}

	/**
	 * Generate a new local bnode to be used in virtuoso queries
	 * 
	 * @return
	 */
	private VirtuosoBNode nextVirtBnode(BNode bn) {
		logger.debug("nextVirtBnode(BNode)");
		/**
		 * XXX Here we force virtuoso to generate a valid skolem uri for a blank
		 * node we are going to insert for the first time.
		 * 
		 * All this process should be more efficient, possibly invoking a native
		 * procedure, instead of insert/select/delete a fake triple as it is
		 * now.
		 */
		UriRef g = new UriRef("urn:x-virtuoso:bnode-tmp");
		UriRef p = new UriRef("urn:x-virtuoso:bnode:object");
		UriRef o = new UriRef(new StringBuilder()
				.append("urn:x-virtuoso:bnode:").append(bn).toString());

		Exception e = null;
		ResultSet rs = null;

		String bnodeId = null;

		Connection connection = getConnection();
		PreparedStatement insert = null;
		PreparedStatement select = null;
		PreparedStatement delete = null;
		// insert
		try {
			insert = connection.prepareStatement(INSERT_NEW_BNODE);
			bindGraph(insert, 1, g);
			bindPredicate(insert, 2, p);
			bindSubject(insert, 3, o);
			insert.executeUpdate();

			// select
			select = connection.prepareStatement(SELECT_TRIPLES_NULL_P_O);
			bindGraph(select, 1, g);
			bindPredicate(select, 2, p);
			bindValue(select, 3, o);
			rs = (ResultSet) select.executeQuery();
			rs.next();
			bnodeId = rs.getString(1);
			rs.close();

			// delete
			delete = connection.prepareStatement(DELETE_NEW_BNODE);
			bindGraph(delete, 1, g);
			bindPredicate(delete, 2, p);
			bindSubject(delete, 3, o); // It is a IRI
			delete.executeUpdate();

		} catch (VirtuosoException ve) {
			logger.error("ERROR while executing statement", ve);
			e = ve;
		} catch (SQLException se) {
			logger.error("ERROR while executing statement", se);
			e = se;
		} finally {
			close(rs, insert, select, delete, connection);
		}
		if (e != null) {
			throw new RuntimeException(e);
		}
		return new VirtuosoBNode(bnodeId);

	}
	public void insertQuad(String graph, Triple triple) {
		NonLiteral s = triple.getSubject(); 
		UriRef p = triple.getPredicate() ;
		Resource o = triple.getObject();
		
		// Skolemize bnodes
		if(s instanceof BNode){
			s = toVirtBnode((BNode) s);
		}
		if(o instanceof BNode){
			o = toVirtBnode((BNode) o);
		}
		Exception e = null;
		PreparedStatement st = null;
		Connection connection = null;
		try {
			connection = getConnection();
			st = getStatement(connection, INSERT_QUAD);
			bindGraph(st, 1, graph);
			bindSubject(st, 2, s);
			bindPredicate(st, 3, p);
			bindValue(st, 4, o);
			st.executeUpdate();
		} catch (VirtuosoException e1) {
			logger.error("Cannot execute statement", e1);
			e = e1;
		} catch (SQLException e1) {
			logger.error("Cannot execute statement", e1);
			e = e1;
		} finally{
			close(st, connection);
		}
		if(e!=null){
			throw new RuntimeException(e);
		}
	}

	public void deleteQuad(String graph,  Triple triple) {
		NonLiteral s = triple.getSubject(); 
		UriRef p = triple.getPredicate() ;
		Resource o = triple.getObject();

		// Skolemize bnodes
		if(s instanceof BNode){
			s = toVirtBnode((BNode) s);
		}
		if(o instanceof BNode){
			o = toVirtBnode((BNode) o);
		}
		Exception e = null;
		PreparedStatement st = null;
		Connection connection = null;
		try {
			connection = getConnection();
			st = getStatement(connection, DELETE_QUAD);
			bindGraph(st, 1, graph);
			bindSubject(st, 2, s);
			bindPredicate(st, 3, p);
			bindValue(st, 4, o);
			st.executeUpdate();
		} catch (VirtuosoException ex) {
			logger.error("Cannot execute statement", ex);
			e = ex;
		} catch (SQLException ex) {
			logger.error("Cannot execute statement", ex);
			e = ex;
		} finally {
			close(st, connection);
		}
		
		if (e != null) {
			throw new RuntimeException(e);
		}
	}

	public Set<UriRef> listGraphs() {
		Exception e = null;

		Set<UriRef> graphs = new HashSet<UriRef>();
		PreparedStatement st = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = getConnection();
			st = getStatement(connection, LIST_GRAPHS);
			rs = st.executeQuery();
			while (rs.next()) {
				UriRef graph = new UriRef(rs.getString(1));
				logger.debug(" > Graph {}", graph);
				graphs.add(graph);
			}
		} catch (VirtuosoException ex) {
			logger.error("Cannot execute query", ex);
			e = ex;
		} catch (SQLException ex) {
			logger.error("Cannot execute query", ex);
			e = ex;
		} finally{
			close(rs, st, connection);
		}
		if(e != null){
			throw new RuntimeException(e);
		}
		
		return Collections.unmodifiableSet(graphs);
	}

	public void clearGraph(String graph) {
		Exception e = null;
		PreparedStatement st = null;
		Connection connection = null;
		try {
			connection = getConnection();
			st = getStatement(connection, CLEAR_GRAPH);
			bindGraph(st, 1, graph);
			st.executeUpdate();
		} catch (VirtuosoException ex) {
			logger.error("Cannot execute statement", ex);
			e = ex;
		} catch (SQLException ex) {
			logger.error("Cannot execute statement", ex);
			e = ex;
		} finally{
			close(st, connection);
		}
		if(e != null){
			throw new RuntimeException(e);
		}
	}

	private VirtuosoBNode toBNode(String virtbnode) {
		VirtuosoBNode bnode;
		bnode = new VirtuosoBNode(virtbnode);
		return bnode;
	}

	public Iterator<Triple> filter(String graph, NonLiteral subject,
			UriRef predicate, Resource object) {
		logger.debug("filter(String graph, NonLiteral s, UriRef p, Resource o)");

		// Override blank node object to be a skolemized IRI
		if (object != null && object instanceof BNode) {
			object = new UriRef(toVirtBnode((BNode) object).getSkolemId());
		}

		// Override blank node subjects to be a skolemized IRI
		if (subject != null && subject instanceof BNode) {
			subject = new UriRef(toVirtBnode((BNode) subject).getSkolemId());
		}

		List<Triple> list = null;
		Exception e = null;
		Set<String> filters = new HashSet<String>(Arrays.asList(filterQueries));

		//
		if (subject == null) {
			filters.remove(SELECT_TRIPLES_S_P_O);
			filters.remove(SELECT_TRIPLES_S_NULL_NULL);
			filters.remove(SELECT_TRIPLES_S_P_NULL);
			filters.remove(SELECT_TRIPLES_S_NULL_O);
		} else {
			filters.remove(SELECT_TRIPLES_NULL_NULL_NULL);
			filters.remove(SELECT_TRIPLES_NULL_NULL_O);
			filters.remove(SELECT_TRIPLES_NULL_P_NULL);
			filters.remove(SELECT_TRIPLES_NULL_P_O);
		}
		if (predicate == null) {
			filters.remove(SELECT_TRIPLES_S_P_O);
			filters.remove(SELECT_TRIPLES_NULL_P_NULL);
			filters.remove(SELECT_TRIPLES_NULL_P_O);
			filters.remove(SELECT_TRIPLES_S_P_NULL);
		} else {
			filters.remove(SELECT_TRIPLES_S_NULL_O);
			filters.remove(SELECT_TRIPLES_NULL_NULL_NULL);
			filters.remove(SELECT_TRIPLES_NULL_NULL_O);
			filters.remove(SELECT_TRIPLES_S_NULL_NULL);
		}
		if (object == null) {
			filters.remove(SELECT_TRIPLES_S_P_O);
			filters.remove(SELECT_TRIPLES_S_NULL_O);
			filters.remove(SELECT_TRIPLES_NULL_P_O);
			filters.remove(SELECT_TRIPLES_NULL_NULL_O);
		} else {
			filters.remove(SELECT_TRIPLES_S_P_NULL);
			filters.remove(SELECT_TRIPLES_NULL_NULL_NULL);
			filters.remove(SELECT_TRIPLES_NULL_P_NULL);
			filters.remove(SELECT_TRIPLES_S_NULL_NULL);
		}

		// There must be only 1 boss
		String filter = filters.iterator().next();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			logger.debug("query: {}", filter);
			connection = getConnection();
			ps = getStatement(connection, filter);
			// In any case the first binding is the graph
			bindGraph(ps, 1, graph);

			int index = 2;
			if (subject != null) {
				bindSubject(ps, index, subject);
				index++;
			}
			if (predicate != null) {
				bindPredicate(ps, index, predicate);
				index++;
			}
			if (object != null) {
				bindValue(ps, index, object);
			}

			rs = (ResultSet) ps.executeQuery();
			list = new ArrayList<Triple>();

			while (rs.next()) {
				list.add(new TripleBuilder(rs.getObject(1), rs.getObject(2), rs
						.getObject(3)).build());
			}
		} catch (VirtuosoException e1) {
			logger.error("ERROR while executing statement", ps);
			e = e1;
		} catch (SQLException e1) {
			logger.error("ERROR while executing statement", ps);
			e = e1;
		} finally {
			close(rs, ps, connection);
		}

		if (list == null || e != null) {
			throw new RuntimeException(e);
		}
		return list.iterator();
	}

	public int size(String graph){
		logger.trace("called size({})", graph);
		Exception e = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = null;
		int size = -1;
		try {
			connection = getConnection();
			ps = getStatement(connection, COUNT_TRIPLES_OF_GRAPH);
			logger.trace("statement got: {}", ps);
			// In any case the first binding is the graph
			bindGraph(ps, 1, graph);
			logger.trace("bound value: {}", graph);
			boolean r = ps.execute();
			logger.trace("Executed statement: {}", r);
			if(r){
				rs = (ResultSet) ps.getResultSet();
				logger.trace("Got result set, has next?");
				boolean hn = rs.next();
				logger.trace(" > {}", hn);
				if(hn){
					size = rs.getInt(1);
				}else{
					e = new RuntimeException("Incosistent result. A result row was expected. None obtained.");
				}
			}else{
				e = new RuntimeException("Incosistent result. ResultSet expected but 'false' returned by statement execute() ");
			}
		} catch (VirtuosoException e1) {
			logger.error("ERROR while executing statement", ps);
			e = e1;
		} catch (SQLException e1) {
			logger.error("ERROR while executing statement", ps);
			e = e1;
		} finally {
			close(rs, ps, connection);
		}
		
		if (size == -1 || e != null) {
			throw new RuntimeException(e);
		}

		return size;	
	}
	
	/**
	 * Builds a clerezza Triple from a Virtuoso result types
	 * 
	 */
	private class TripleBuilder {

		Object s = null;
		Object p = null;
		Object o = null;

		public TripleBuilder(Object s, Object p, Object o) {
			if (logger.isTraceEnabled()) {
				logger.trace("TripleBuilder({}, {}, {})", new Object[]{ s, p, o });
			}
			this.s = s;
			this.p = p;
			this.o = o;
		}

		public Triple build() {
			logger.debug("TripleBuilder.build()");
			return new TripleImpl(buildSubject(this.s), buildPredicate(this.p),
					buildObject(this.o));
		}
	}

	/**
	 * The following private methods are used to support the triple addition
	 * plan B
	 * 
	 * XXX This is deprecated. Discussion at CLEREZZA-908
	 */
	@Deprecated
	public boolean performAddPlanB(String graph, Triple triple) {

		StringBuilder b = new StringBuilder();
		b.append(toVirtSubject(triple.getSubject())).append(" ")
				.append(toVirtPredicate(triple.getPredicate())).append(" ")
				.append(toVirtObject(triple.getObject())).append(" . ");
		String sql = new StringBuilder().append("db.dba.ttlp(?, '', '").append(graph).append("', 0)").toString();
		logger.debug("Exec Plan B: {}", sql);
		Exception e = null;
		PreparedStatement st = null;
		Connection connection = null;
		try {
			connection = getConnection();
			st = getStatement(connection, sql);
			st.setNString(1, b.toString());
			st.execute();
		} catch (VirtuosoException ve) {
			logger.error("ERROR while executing statement", ve);
			e = ve;
		} catch (SQLException se) {
			logger.error("ERROR while executing statement", se);
			e = se;
		} finally {
			close(st, connection);
		}
		if (e != null) {
			throw new RuntimeException(e);
		}
		return true;
	}

	/**
	 * Returns a string to be used inline in SQL statements as Object of a
	 * triple.
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
		String literal = object.getLexicalForm();
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
	 * From a Virtuoso object to NonLiteral
	 * 
	 * @param s
	 * @return
	 */
	private NonLiteral buildSubject(Object s) {
		if (s instanceof VirtuosoExtendedString) {
			VirtuosoExtendedString vs = (VirtuosoExtendedString) s;
			if (vs.iriType == VirtuosoExtendedString.IRI
					&& (vs.strType & 0x01) == 0x01) {
				// Subject is IRI
				return new UriRef(vs.str);
			} else if (vs.iriType == VirtuosoExtendedString.BNODE) {
				return DataAccess.this.toBNode(vs.str);
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

	private UriRef buildPredicate(Object p) {
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

	private Resource buildObject(Object o) {
		if (o instanceof VirtuosoExtendedString) {
			// In case is IRI
			VirtuosoExtendedString vs = (VirtuosoExtendedString) o;
			if (vs.iriType == VirtuosoExtendedString.IRI
					&& (vs.strType & 0x01) == 0x01) {
				// Is IRI
				return new UriRef(vs.str);
			} else if (vs.iriType == VirtuosoExtendedString.BNODE) {
				//
				return DataAccess.this.toBNode(vs.str);
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
					// XXX Shadowing XMLLiteral
					UriRef dt;
					if(type.equals(XMLLiteralShadowed.getUnicodeString())){
						dt = XMLLiteral;
					} else {
						dt = new UriRef(type);
					}
					return new TypedLiteralImpl(value, dt);
				}
			} else if (rb.rb_box instanceof VirtuosoExtendedString) {
				VirtuosoExtendedString vs = (VirtuosoExtendedString) rb.rb_box;

				if (vs.iriType == VirtuosoExtendedString.IRI
						&& (vs.strType & 0x01) == 0x01) {
					// Is IRI
					return new UriRef(vs.str);
				} else if (vs.iriType == VirtuosoExtendedString.BNODE) {
					//
					return DataAccess.this.toBNode(vs.str);
				} else {
					String type = rb.getType();
					if (type == null) {
						String lang = rb.getLang();
						if (lang != null) {
							return new PlainLiteralImpl(vs.str,
									new Language(lang));
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
	
	private Resource objectToResource(Object o){
		return buildObject(o);
	}
	
	/**
	 * This is to execute SPARQL queries.
	 * 
	 * @param query
	 * @param defaultGraphUri
	 * @return
	 */
	public Object executeSparqlQuery(String query, UriRef defaultGraphUri) {
		Connection connection = null;
		ResultSet rs = null;
		Statement st = null;
		logger.debug("executeSparqlQuery(String {}, UriRef {})", query, defaultGraphUri);
		Exception e = null;
		
		StringBuilder qb = new StringBuilder();
		qb.append("SPARQL ");
		if(defaultGraphUri != null){
			qb.append("DEFINE input:default-graph-uri <");
			qb.append(defaultGraphUri.getUnicodeString());
			qb.append(">");
			qb.append("\n");
		}
		// XXX Shadowing XMLLiteral
		// This is not very accurate, but we can't go too much further on this, since it is an hack to
		// bypass a virtuoso bug
		// See CLEREZZA-908
		if(query.contains(XMLLiteral.getUnicodeString())){
			query.replace(XMLLiteral.getUnicodeString(), XMLLiteralShadowed.getUnicodeString());
		}
		qb.append(query);
		Object returnThis = null;
		try {
			connection = getConnection();
			String sql = qb.toString();
			logger.debug("Executing SQL: {}", sql);
			st = connection.createStatement();
			st.execute(sql);
			rs = st.getResultSet();
			// ASK :: Boolean
			if (rs.getMetaData().getColumnCount() == 1
					&& rs.getMetaData().getColumnType(1) == 4) {
//				rs.next();
//				returnThis = rs.getBoolean(1);
				if(rs.next()){
					returnThis = rs.getBoolean(1);
				}else{
					returnThis = false;
				}
			} else
			// CONSTRCUT/DESCRIBE :: TripleCollection
			if (rs.getMetaData().getColumnCount() == 3
					&& rs.getMetaData().getColumnType(1) == 12
					&& rs.getMetaData().getColumnType(2) == 12
					&& rs.getMetaData().getColumnType(3) == 1111) {
				final List<Triple> lt = new ArrayList<Triple>();
				while (rs.next()) {
					lt.add(new TripleBuilder(rs.getObject(1), rs.getObject(2),
							rs.getObject(3)).build());
				}
				returnThis = new SimpleGraph(lt.iterator());
			} else {
				// SELECT (anything else?)
				returnThis = new SparqlResultSetWrapper(rs);
			}
		} catch (VirtuosoException ve) {
			logger.error("A virtuoso SQL exception occurred.");
			e = ve;
		} catch (SQLException se) {
			logger.error("An SQL exception occurred.");
			e = se;
		} finally {
			close(rs, st, connection);
		}
		if (e != null) {
			throw new RuntimeException(e);
		}
		
		return returnThis;
	}
	
	/**
	 * To wrap a sparql result set
	 * @author enridaga
	 *
	 */
	private class SparqlResultSetWrapper implements org.apache.clerezza.rdf.core.sparql.ResultSet {

	    private final List<String> resultVars;
	    private Iterator<SolutionMapping> iterator;
	    
	    SparqlResultSetWrapper(final ResultSet jdbcResultSet) throws SQLException {
	    	
	        resultVars = new ArrayList<String>();
	        for(int x = 1; x < jdbcResultSet.getMetaData().getColumnCount() + 1; x++){
	        	resultVars.add(jdbcResultSet.getMetaData().getColumnName(x));
	        }
	        
	        final List<SolutionMapping> solutions = new ArrayList<SolutionMapping>();
	        while (jdbcResultSet.next()) {
	        	RSSolutionMapping sm = new RSSolutionMapping();
	        	for(String column : resultVars){
	        		sm.put(new Variable(column), objectToResource(jdbcResultSet.getObject(column)));
	        	}
	        	solutions.add(sm);
	        }
	        iterator = solutions.iterator();
	    }

	    @Override
	    public boolean hasNext() {
	        return iterator.hasNext();
	    }

	    @Override
	    public SolutionMapping next() {
	        return iterator.next();
	    }

	    @Override
	    public void remove() {
	        throw new UnsupportedOperationException("Not supported yet.");
	    }

	    @Override
	    public List<String> getResultVars() {
	    	return resultVars;
	    }
	}
	
	/**
	 * This is a utility class
	 * 
	 * @author enridaga
	 *
	 */
	private class RSSolutionMapping implements SolutionMapping {

    	private Map<Variable, Resource> map;
    	
		@Override
		public int size() {
			return map.size();
		}

		@Override
		public boolean isEmpty() {
			return map.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return map.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return map.containsValue(value);
		}

		@Override
		public Resource get(Object key) {
			return map.get(key);
		}

		@Override
		public Resource put(Variable key, Resource value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Resource remove(Object key) {
			return map.remove(key);
		}

		@Override
		public void putAll(Map<? extends Variable, ? extends Resource> m) {
			map.putAll(m);
		}

		@Override
		public void clear() {
			map.clear();
		}

		@Override
		public Set<Variable> keySet() {
			return map.keySet();
		}

		@Override
		public Collection<Resource> values() {
			return map.values();
		}

		@Override
		public Set<java.util.Map.Entry<Variable, Resource>> entrySet() {
			return map.entrySet();
		}

		@Override
		public Resource get(String name) {
			return map.get(new Variable(name));
		}
    }

}
