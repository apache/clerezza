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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Language;
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
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wymiwyg.commons.util.collections.BidiMap;
import org.wymiwyg.commons.util.collections.BidiMapImpl;

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
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	/**
	 * Bidirectional map for managing the conversion from virtuoso blank nodes
	 * (strings) to clerezza blank nodes and vice versa.
	 */
	private final BidiMap<String, BNode> bnodesMap;
	private int maxVirtBnodeIndex = 0;

	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(VirtuosoMGraph.class);

	/**
	 * Connection
	 */
	private VirtuosoConnection connection = null;
	/**
	 * The name of the graph
	 */
	private String name = null;
	private int size = 0;

	private VirtuosoGraph readOnly = null;

	/**
	 * Creates a {@link VirtuosoMGraph} Virtuoso MGraph binds directly to the
	 * store.
	 * 
	 * @param connection
	 */
	public VirtuosoMGraph(String name, VirtuosoConnection connection) {
		logger.debug("VirtuosoMGraph(String {}, VirtuosoConnection {})", name,
				connection);
		this.name = name;
		this.connection = connection;
		this.bnodesMap = new BidiMapImpl<String, BNode>();
	}

	/**
	 * Gets the connection
	 * 
	 * @return
	 */
	protected VirtuosoConnection getConnection() {
		logger.debug("getConnection()");
		return this.connection;
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
			this.readOnly = new VirtuosoGraph(name, connection);
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
		sb.append("SPARQL SELECT ?SUBJECT ?PREDICATE ?OBJECT WHERE { GRAPH <")
				.append(this.getName()).append("> { ")
				.append(" ?SUBJECT ?PREDICATE ?OBJECT ");
		if (virtSubject != null) {
			sb.append(". FILTER( ").append("?SUBJECT = ").append(virtSubject)
					.append(") ");
		}
		if (virtPredicate != null) {
			sb.append(". FILTER( ").append("?PREDICATE = ")
					.append(virtPredicate).append(") ");
		}
		if (virtObject != null) {
			sb.append(". FILTER( ").append("?OBJECT = ").append(virtObject)
					.append(") ");
		}
		sb.append(" } } ");

		String sql = sb.toString();
		logger.debug("Executing SQL: {}", sql);
		Statement st;
		try {
			readLock.lock();
			st = connection.createStatement();
			boolean has = st.execute(sql);
			final VirtuosoResultSet rs = (VirtuosoResultSet) st.getResultSet();
			readLock.unlock();
			return new Iterator<Triple>() {
				Triple current = null;
				private boolean didNext = false;
				private boolean hasNext = false;

				@Override
				public boolean hasNext() {
					readLock.lock();
					try {
						if (!didNext) {
							hasNext = rs.next();
							didNext = true;
						}
						return hasNext;
					} catch (SQLException e) {
						logger.error("Error while iterating results", e);
						return false;
					} finally {
						readLock.unlock();
					}
				}

				@Override
				public Triple next() {
					try {
						readLock.lock();
						if (!didNext) {
							rs.next();
						}
						didNext = false;
						current = new TripleBuilder(rs.getObject(1),
								rs.getObject(2), rs.getObject(3)).build();
					} catch (VirtuosoException e) {
						logger.error("Error while iterating results", e);
					} finally {
						readLock.unlock();
					}
					return current;
				}

				@Override
				public void remove() {
					writeLock.lock();
					try {
						if (current == null) {
							throw new IllegalStateException();
						}
						VirtuosoMGraph.this.performRemove(current);
						current = null;
					} finally {
						writeLock.unlock();
					}
				}

			};
		} catch (VirtuosoException e) {
			logger.error("ERROR while executing statement", e);
			throw new RuntimeException(e);
		} catch (SQLException e) {
			logger.error("ERROR while executing statement", e);
			throw new RuntimeException(e);
		}
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
		try {
			logger.debug("Executing SQL: {}", SQL);
			Statement st = getConnection().createStatement();
			boolean success = st.execute(SQL);
			if (success) {
				this.size = 0;
			} else {
				throw new RuntimeException(
						"Problems on clear() method. Cannot clear the graph!");
			}
		} catch (VirtuosoException e) {
			logger.error("ERROR while executing statement", e);
			throw new RuntimeException(e);
		} catch (SQLException e) {
			logger.error("ERROR while executing statement", e);
			throw new RuntimeException(e);
		} finally {
			this.writeLock.unlock();
		}
	}

	private void loadSize() {
		logger.debug("loadSize()");
		String SQL = "SPARQL SELECT COUNT(*) FROM <" + this.getName()
				+ "> WHERE { ?s ?p ?o } ";
		int size = 0;
		this.readLock.lock();
		try {
			logger.debug("Executing SQL: {}", SQL);
			Statement st = getConnection().createStatement();
			VirtuosoResultSet rs = (VirtuosoResultSet) st.executeQuery(SQL);
			rs.next();
			size = rs.getInt(1);
		} catch (VirtuosoException e) {
			logger.error("ERROR while executing statement", e);
			throw new RuntimeException(e);
		} catch (SQLException e) {
			logger.error("ERROR while executing statement", e);
			throw new RuntimeException(e);
		} finally {
			this.readLock.unlock();
		}
		this.size = size;
	}

	protected boolean performAdd(Triple triple) {
		return add(triple, connection);
	}

	protected boolean performRemove(Triple triple) {
		return remove(triple, connection);
	}

	/**
	 * Adds a triple in the store
	 * 
	 * @param triple
	 * @param connection
	 * @return
	 */
	private boolean add(Triple triple, VirtuosoConnection connection) {
		logger.debug("add(Triple {}, VirtuosoConnection {})", triple,
				connection);
		String sql = getAddSQLStatement(triple);
		logger.debug("Executing SQL: {}", sql);
		writeLock.lock();
		try {
			Statement st = connection.createStatement();
			st.execute(sql);
		} catch (VirtuosoException e) {
			logger.error("ERROR while executing statement", e);
			return false;
		} catch (SQLException e) {
			logger.error("ERROR while executing statement", e);
			return false;
		} finally {
			writeLock.unlock();
		}
		return true;
	}

	/**
	 * Removes a triple from the store.
	 * 
	 * @param triple
	 * @param connection
	 * @return
	 */
	private boolean remove(Triple triple, VirtuosoConnection connection) {
		logger.debug("remove(Triple triple, VirtuosoConnection connection)",
				triple, connection);
		String sql = getRemoveSQLStatement(triple);
		logger.debug("Executing SQL: {}", sql);
		writeLock.lock();
		try {
			Statement st = connection.createStatement();
			st.execute(sql);
		} catch (SQLException e) {
			logger.error("ERROR while executing statement", e);
			return false;
		} finally {
			writeLock.unlock();
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
	 * Get the clerezza BNode corresponding to the given String from a Virtuoso
	 * resultset.
	 * 
	 * @param virtbnode
	 * @return
	 */
	private BNode toBNode(String virtbnode) {
		logger.debug("toBNode(String {})", virtbnode);
		BNode bnode = bnodesMap.get(virtbnode);
		if (bnode == null) {
			bnode = new BNode();
			bnodesMap.put(virtbnode, bnode);
		}
		// Subject is BNode
		return bnode;
	}

	/**
	 * Generate a new local bnode to be used in virtuoso queries
	 * 
	 * @return
	 */
	private String nextVirtBnode() {
		logger.debug("nextVirtBnode()");
		maxVirtBnodeIndex++;
		return new StringBuilder().append("_:b").append(maxVirtBnodeIndex)
				.toString();
	}

	/**
	 * 
	 * @param bnode
	 * @return
	 */
	private String toVirtBnode(BNode bnode) {
		logger.debug("toVirtBnode(BNode {})", bnode);
		String virtBnode = bnodesMap.getKey(bnode);
		if (virtBnode == null) {
			// We create a local bnode mapped to the BNode given
			virtBnode = nextVirtBnode();
			bnodesMap.put(virtBnode, bnode);
		}
		return virtBnode;
	}

	private String getAddSQLStatement(Triple triple) {
		logger.debug("getAddSQLStatement(Triple {})", triple);
		StringBuilder sb = new StringBuilder();
		String subject = toVirtSubject(triple.getSubject());
		String predicate = toVirtPredicate(triple.getPredicate());
		String object = toVirtObject(triple.getObject());
		String sql = sb.append("SPARQL INSERT INTO <").append(this.getName())
				.append("> { ").append(subject).append(" ").append(predicate)
				.append(" ").append(object).append(" }").toString();
		return sql;
	}

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
	 * Returns a string to be used in SQL statements as Object of a triple.
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
			return toVirtBnode((BNode) object);
		} else if (object instanceof PlainLiteral) {
			return toVirtPlainLiteral((PlainLiteral) object);
		} else if (object instanceof TypedLiteral) {
			return toVirtTypedLiteral((TypedLiteral) object);
		}
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
		String literal = object.getLexicalForm();
		return new StringBuilder().append('"').append(literal).append('"')
				.append("^^").append(toVirtIri(dt)).toString();
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
		StringBuilder sb = new StringBuilder().append('"').append(literal)
				.append('"');
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
			return toVirtBnode((BNode) subject);
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

		public Triple build() {
			logger.debug("TripleBuilder.build()");
			return new Triple() {
				@Override
				public NonLiteral getSubject() {
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

				@Override
				public UriRef getPredicate() {
					logger.debug("TripleBuilder.getPredicate() : {}", p);
					if (p instanceof VirtuosoExtendedString) {
						VirtuosoExtendedString vs = (VirtuosoExtendedString) p;
						if (vs.iriType == VirtuosoExtendedString.IRI
								&& (vs.strType & 0x01) == 0x01) {
							// Subject is IRI
							return new UriRef(vs.str);
						} else {
							// !Cannot happen
							throw new IllegalStateException(
									"Predicate must be an IRI ");
						}
					} else {
						throw new IllegalStateException(
								"Predicate must be an IRI");
					}
				}

				@Override
				public Resource getObject() {
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
						String value = (String) rb.rb_box;
						String lang = rb.getLang();
						String type = rb.getType();
						if (type == null) {
							Language language = lang == null ? null
									: new Language(lang);
							return new PlainLiteralImpl(value, language);
						} else {
							return new TypedLiteralImpl(value, new UriRef(type));
						}
					} else if (o == null) {
						// Raise an exception
						throw new IllegalStateException(
								"Object cannot be NULL!");
					} else {
						// FIXME (not clear this...)
						return new PlainLiteralImpl(o.toString());
					}
				}
			};
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
