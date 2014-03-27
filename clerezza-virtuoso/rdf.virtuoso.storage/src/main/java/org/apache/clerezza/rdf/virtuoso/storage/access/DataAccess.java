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

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.apache.clerezza.rdf.virtuoso.storage.VirtuosoBNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wymiwyg.commons.util.collections.BidiMap;
import org.wymiwyg.commons.util.collections.BidiMapImpl;

import virtuoso.jdbc4.VirtuosoConnection;
import virtuoso.jdbc4.VirtuosoException;
import virtuoso.jdbc4.VirtuosoExtendedString;
import virtuoso.jdbc4.VirtuosoPreparedStatement;
import virtuoso.jdbc4.VirtuosoRdfBox;
import virtuoso.jdbc4.VirtuosoResultSet;

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

	/**
	 * Bidirectional map for managing the conversion from virtuoso blank nodes
	 * (strings) to clerezza blank nodes and vice versa.
	 */
	private final BidiMap<VirtuosoBNode, BNode> bnodesMap;

	private Map<String, VirtuosoPreparedStatement> preparedStatements = null;
	private VirtuosoConnection connection = null;
	private String connectionString;
	private String user;
	private String pwd;

	// We protect the constructor from outside the package...
	DataAccess(String connectionString, String user, String pwd) {
		this.connectionString = connectionString;
		this.user = user;
		this.pwd = pwd;
		connection = createConnection();
		
		// Init collections
		this.preparedStatements = new HashMap<String,VirtuosoPreparedStatement>();
		this.bnodesMap = new BidiMapImpl<VirtuosoBNode, BNode>();

	}

	private VirtuosoConnection createConnection() {
		try {
			Class.forName(VirtuosoWeightedProvider.DRIVER, true, this
					.getClass().getClassLoader());
			VirtuosoConnection c = (VirtuosoConnection) DriverManager
					.getConnection(connectionString, user, pwd);
			c.setAutoCommit(true);
			return c;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	// A simple renewal policy
	private int statementCalls = 0;
	protected PreparedStatement getStatement(String query)
			throws VirtuosoException {
		if(statementCalls >= 10000){
			statementCalls=0; 
			renew();
		}else{
			statementCalls++;
		}
		if (!preparedStatements.containsKey(query)) {
			VirtuosoPreparedStatement ps = (VirtuosoPreparedStatement) connection
					.prepareStatement(query);
			preparedStatements.put(query, ps);
		}
		return preparedStatements.get(query);
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

	public void renew() {
		logger.trace("renewing...");
		close();
		connection = createConnection();
	}

	public void close() {
		logger.trace("closing resources...");
		Collection<VirtuosoPreparedStatement> pss = preparedStatements.values();
		for (VirtuosoPreparedStatement ps : pss) {
			try {
				logger.trace("Closing prepared statement {}", ps);
				ps.close();
			} catch (Exception e) {
				logger.error("Cannot close statement", e);
			}
		}
		logger.trace("closed {} statements.", pss.size());
		preparedStatements.clear();
		try {
			connection.close();
			logger.trace("Connection closed");
		} catch (Exception e) {
			logger.error("Cannot close connection", e);
		}
	}
	
	private void close(String statementId){
		try {
			VirtuosoPreparedStatement ps = preparedStatements.get(statementId);
			if (ps == null) {
				logger.warn(
						"Attempting to close a statement that was not prepared: {}",
						statementId);
			} else {
				logger.trace("Closing prepared statement {}", ps);
				ps.close();
			}
		} catch (Exception e) {
			logger.error("Cannot close statement", e);
		} finally {
			// We won't reuse a statement that thrown a n exception on close...
			preparedStatements.remove(statementId);
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
			st.setString(i + 2, tl.getDataType().getUnicodeString());
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
		VirtuosoResultSet rs = null;

		String bnodeId = null;
		// insert
		try {
			PreparedStatement insert = getStatement(INSERT_NEW_BNODE);
			bindGraph(insert, 1, g);
			bindPredicate(insert, 2, p);
			bindSubject(insert, 3, o);
			insert.executeUpdate();

			// select
			PreparedStatement select = getStatement(SELECT_TRIPLES_NULL_P_O);
			bindGraph(select, 1, g);
			bindPredicate(select, 2, p);
			bindValue(select, 3, o);
			rs = (VirtuosoResultSet) select.executeQuery();
			rs.next();
			bnodeId = rs.getString(1);
			rs.close();

			// delete
			PreparedStatement delete = getStatement(DELETE_NEW_BNODE);
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
			try {
				if (rs != null)
					rs.close();
			} catch (Exception ex) {
				logger.error("Error attempting to close result set", ex);
			}
		}
		if (e != null) {
			close(INSERT_NEW_BNODE);
			close(SELECT_TRIPLES_NULL_P_O);
			close(DELETE_NEW_BNODE);
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
		
		try {
			PreparedStatement st = getStatement(INSERT_QUAD);
			bindGraph(st, 1, graph);
			bindSubject(st, 2, s);
			bindPredicate(st, 3, p);
			bindValue(st, 4, o);
			st.executeUpdate();
		} catch (VirtuosoException e) {
			logger.error("Cannot execute statement", e);
			throw new RuntimeException(e);
		} catch (SQLException e) {
			logger.error("Cannot execute statement", e);
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
		try {
			PreparedStatement st = getStatement(DELETE_QUAD);
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
		}
		
		if (e != null) {
			close(DELETE_QUAD);
			throw new RuntimeException(e);
		}
	}

	public Set<UriRef> listGraphs() {
		Exception e = null;

		Set<UriRef> graphs = new HashSet<UriRef>();
		try {
			PreparedStatement st = getStatement(LIST_GRAPHS);
			ResultSet rs = st.executeQuery();
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
		}
		
		if(e != null){
			close(LIST_GRAPHS);
			throw new RuntimeException(e);
		}
		
		return Collections.unmodifiableSet(graphs);
	}

	public void clearGraph(String graph) {
		Exception e = null;
		try {
			PreparedStatement st = getStatement(CLEAR_GRAPH);
			bindGraph(st, 1, graph);
			st.executeUpdate();
		} catch (VirtuosoException ex) {
			logger.error("Cannot execute statement", ex);
			e = ex;
		} catch (SQLException ex) {
			logger.error("Cannot execute statement", ex);
			e = ex;
		} 
		
		if(e != null){
			close(CLEAR_GRAPH);
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
		

		// Override blank node object to be a skolemized IRI
		if (object != null && object instanceof BNode) {
			object = new UriRef(toVirtBnode((BNode) object).getSkolemId());
		}

		// Override blank node subjects to be a skolemized IRI
		if (subject != null && subject instanceof BNode) {
			subject = new UriRef(toVirtBnode((BNode) subject).getSkolemId());
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("performFilter(UriRef graph, NonLiteral s, UriRef p, Resource o)");
			logger.debug(" > g: {}", graph);
			logger.debug(" > s: {}", subject);
			logger.debug(" > p: {}", predicate);
			logger.debug(" > o: {}", object);
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
		VirtuosoResultSet rs = null;
		try {
			logger.debug("query: {}", filter);
			ps = getStatement(filter);
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

			rs = (VirtuosoResultSet) ps.executeQuery();
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
			try {
				if (rs != null)
					rs.close();
			} catch (Throwable ex) {
				logger.error("Cannot close result set", ex);
			}
		}

		if (list == null || e != null) {
			// We also close the statement
			close(filter);
			throw new RuntimeException(e);
		}
		return list.iterator();
	}

	public int size(String graph){
		Exception e = null;
		PreparedStatement ps = null;
		VirtuosoResultSet rs = null;
		int size = -1;
		try {
			ps = getStatement(COUNT_TRIPLES_OF_GRAPH);
			// In any case the first binding is the graph
			bindGraph(ps, 1, graph);
			ps.execute();

			rs = (VirtuosoResultSet) ps.getResultSet();

			rs.next();

			size = rs.getInt(1);

		} catch (VirtuosoException e1) {
			logger.error("ERROR while executing statement", ps);
			e = e1;
		} catch (SQLException e1) {
			logger.error("ERROR while executing statement", ps);
			e = e1;
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Throwable ex) {
				logger.error("Cannot close result set", ex);
			}
		}
		
		if (size == -1 || e != null) {
			// We also close the statement
			close(COUNT_TRIPLES_OF_GRAPH);
			throw new RuntimeException(e);
		}

		return size;	
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

		public Triple build() {
			logger.debug("TripleBuilder.build()");
			return new TripleImpl(buildSubject(), buildPredicate(),
					buildObject());
		}
	}

	/**
	 * The following private methods are used to support the triple addition
	 * plan B
	 */
	
	public boolean performAddPlanB(String graph, Triple triple) {

		StringBuilder b = new StringBuilder();
		b.append(toVirtSubject(triple.getSubject())).append(" ")
				.append(toVirtPredicate(triple.getPredicate())).append(" ")
				.append(toVirtObject(triple.getObject())).append(" . ");
		String sql = new StringBuilder().append("db.dba.ttlp(?, '', '").append(graph).append("', 0)").toString();
		logger.debug("Exec Plan B: {}", sql);
		Exception e = null;
		PreparedStatement st = null;
		try {
			st = getStatement(sql);
			String s = b.toString();
			logger.trace(" TTL is \n{}\n", s);
			st.setNString(1, b.toString());
			st.execute();
		} catch (VirtuosoException ve) {
			logger.error("ERROR while executing statement", ve);
			e = ve;
		} catch (SQLException se) {
			logger.error("ERROR while executing statement", se);
			e = se;
		}
		if (e != null) {
			close(sql);
			if(logger.isDebugEnabled()){
				logger.error("S {}", triple.getSubject());
				logger.error("P {}", triple.getPredicate());
				logger.error("O {}", triple.getObject());
				logger.error(" O length: {}", triple.getObject().toString()
					.length());
			}
			logger.error("Sql: {}", sql);
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

}
