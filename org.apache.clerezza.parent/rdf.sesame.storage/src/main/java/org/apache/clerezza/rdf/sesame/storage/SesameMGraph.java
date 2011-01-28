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
package org.apache.clerezza.rdf.sesame.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.nativerdf.NativeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.event.AddEvent;
import org.apache.clerezza.rdf.core.event.GraphEvent;
import org.apache.clerezza.rdf.core.event.RemoveEvent;
import org.apache.clerezza.rdf.core.impl.AbstractMGraph;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.wymiwyg.commons.util.collections.BidiMap;
import org.wymiwyg.commons.util.collections.BidiMapImpl;

/**
 * Sesame graph implementation.
 * 
 * This class is threadsafe. The <code>SesameMGraph</code> is synchronized
 * against itself. Note that the iteration over triples using an
 * <code>Iterator</code> returned by {@link #iterator()} or
 * {@link #filter(NonLiteral, UriRef, Resource)} must be manually synched.
 * 
 * @author szalay
 */
public class SesameMGraph extends AbstractMGraph implements LockableMGraph {

	private final Logger logger;
	private final Repository repository;

	/**
	 *  Bidirectional map for managing the conversion from
	 *  sesame blank nodes to triamodel blank nodes and vice versa.
	 */
	private final BidiMap<BNode, org.openrdf.model.BNode> tria2SesameBNodes;
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	/**
	 * Default constructor to create a new instance of
	 * <code>SesameMGraph</code>.
	 */
	public SesameMGraph() {
		logger = LoggerFactory.getLogger(SesameMGraph.class);
		tria2SesameBNodes = new BidiMapImpl<BNode, org.openrdf.model.BNode>();
		repository = new SailRepository(new NativeStore());
	}

	/**
	 *	initialize the graph from an existing directory
	 *
	 * @param directory data directory
	 * @throws RepositoryException an error occured initializing the directory
	 */
	public void initialize(File directory) throws RepositoryException {
		repository.setDataDir(directory);
		repository.initialize();
	}

	/**
	 *	public shutdown method
	 *
	 * @param cCtx component context or null
	 * @throws RepositoryException technical problem while shutdown
	 */
	public void shutdown()
			throws RepositoryException {
		repository.shutDown();
	}


	private Iterator<Triple> repositoryResultToTriple(RepositoryResult<Statement> statements)
			throws RepositoryException {
		List<Statement> statementList = statements.asList();
		List<Triple> triples = new ArrayList<Triple>();

		for (Statement s : statementList) {
			NonLiteral subject = getNonLiteralFromSesameResource(s.getSubject());
			UriRef predicate = getUriRefFromSesameUri(s.getPredicate());
			Resource resource = getResourceFromSesameValue(s.getObject());
			Triple triple = new TripleImpl(subject, predicate, resource);
			triples.add(triple);
		}

		statements.close();
		//the following ensures that the remove-method acts on the MGraph
		final Iterator<Triple> convertedIter =  triples.iterator();
		return new Iterator<Triple>() {

			Triple lastReturned = null;

			@Override
			public boolean hasNext() {
				readLock.lock();
				try {
					return convertedIter.hasNext();
				} finally {
					readLock.unlock();
				}
			}

			@Override
			public Triple next() {
				readLock.lock();
				try {
					lastReturned = convertedIter.next();
					return lastReturned;
				} finally {
					readLock.unlock();
				}
			}

			@Override
			public void remove() {
				writeLock.lock();
				try {
					if (lastReturned == null) {
						throw new IllegalStateException();
					}
					SesameMGraph.this.performRemove(lastReturned);
					lastReturned = null;
				} finally {
					writeLock.unlock();
				}
			}

		};
	}

	/**
	 *  convert openrdf resource to clerezza 
	 */
	private NonLiteral getNonLiteralFromSesameResource(org.openrdf.model.Resource r) {

		if (r instanceof org.openrdf.model.BNode) {
			org.openrdf.model.BNode sesameBNode = (org.openrdf.model.BNode) r;
			return getBNodeFromSesameBNode(sesameBNode);
		} else {
			//r is a uri
			URI uri = (URI) r;
			return getUriRefFromSesameUri(uri);
		}
	}

	/**
	 *  create or get bnode for sesame bnode
	 */
	private org.apache.clerezza.rdf.core.BNode getBNodeFromSesameBNode(org.openrdf.model.BNode sesameBNode) {

		org.apache.clerezza.rdf.core.BNode result = tria2SesameBNodes.getKey(sesameBNode);

		if (result == null) {
			result = new BNode() {
			};
			tria2SesameBNodes.put(result, sesameBNode);
		}

		return result;
	}

	/**
	 *  convert openrdf uri 
	 */
	private UriRef getUriRefFromSesameUri(org.openrdf.model.URI uri) {
		UriRef ref = new UriRef(uri.stringValue());
		return ref;
	}

	/**
	 * convert openrdf value to clerezza resource
	 */
	private Resource getResourceFromSesameValue(org.openrdf.model.Value value) {
		if (value instanceof org.openrdf.model.Literal) {
			return getLiteralFromSesameLiteral((org.openrdf.model.Literal) value);
		} else {
			return getNonLiteralFromSesameResource((org.openrdf.model.Resource) value);
		}
	}

	private Resource getLiteralFromSesameLiteral(org.openrdf.model.Literal literal) {
		URI dt = literal.getDatatype();
		if (dt == null) {
			String languageString = literal.getLanguage();
			Language language = languageString == null ? null : new Language(
					languageString);
			return new PlainLiteralImpl(literal.stringValue(), language);
		} else {
			return new TypedLiteralImpl(literal.stringValue(), getUriRefFromSesameUri(dt));
		}
	}

	@Override
	public int size() {
		readLock.lock();
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				return (int) con.size();
			} finally {
				con.close();
			}
		} catch (RepositoryException re) {
			logger.warn("RepositoryException: {}", re);
			throw new RuntimeException(re);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void clear() {
		writeLock.lock();
		try {
			RepositoryConnection connection = repository.getConnection();
			connection.setAutoCommit(false);

			Iterator<Triple> i = iterator();
			while (i.hasNext()) {
				Triple triple = i.next();
				remove(triple, connection);
				dispatchEvent(new RemoveEvent(this, triple));
			}

			connection.commit();
			connection.close();
		} catch (RepositoryException re) {
			logger.warn("RepositoryException: {}", re);
			throw new RuntimeException(re);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean addAll(Collection<? extends Triple> c) {
		writeLock.lock();
		try {
			RepositoryConnection connection = repository.getConnection();
			boolean added = false;
			try {
				connection.setAutoCommit(false);
				for (Triple t : c) {
					boolean addedThis = add(t, connection);
					if (addedThis) {
						dispatchEvent(new AddEvent(this, t));
						added = true;
					}
				}
				connection.commit();
			} finally {
				connection.close();
			}
			return added;
		} catch (RepositoryException re) {
			logger.warn("RepositoryException: {}", re);
			throw new RuntimeException(re);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public Iterator<Triple> performFilter(
			NonLiteral subject, UriRef predicate, Resource object) {
		readLock.lock();
		try {
			org.openrdf.model.Resource sesameSubject = getSesameResourceForNonLiteral(subject);
			org.openrdf.model.URI sesamePredicate = getSesameURIForUriRef(predicate);
			org.openrdf.model.Value sesameObject = getSesameValueForResource(object);

			RepositoryConnection con = repository.getConnection();
			try {
				RepositoryResult<Statement> statements = con.getStatements(
						sesameSubject, sesamePredicate, sesameObject, false);
				return repositoryResultToTriple(statements);
			} finally {
				con.close();
			}
		} catch (RepositoryException re) {
			logger.warn("RepositoryException: {}", re);
			throw new RuntimeException(re);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public boolean performAdd(Triple e) {
		writeLock.lock();
		try {
			RepositoryConnection con = repository.getConnection();
			con.setAutoCommit(false);
			boolean result = add(e, con);
			con.commit();
			con.close();
			return result;
		} catch (RepositoryException re) {
			logger.warn("RepositoryException: {}", re);
			throw new RuntimeException(re);
		} finally {
			writeLock.unlock();
		}
	}

	private boolean add(Triple e, RepositoryConnection con)
			throws RepositoryException {
		if (!contains(e)) {
			org.openrdf.model.Resource subject = getSubjectFromTriple(e);
			org.openrdf.model.URI predicate = getPredicateFromTriple(e);
			org.openrdf.model.Value object = getObjectFromTriple(e);
			con.add(subject, predicate, object);
			return true;
		}
		return false;
	}

	private org.openrdf.model.Resource getSubjectFromTriple(Triple t) {
		return getSesameResourceForNonLiteral(t.getSubject());
	}

	private org.openrdf.model.Resource getSesameResourceForNonLiteral(NonLiteral nl) {

		if (nl == null) {
			return null;
		}

		ValueFactory valueFactory = repository.getValueFactory();

		if (org.apache.clerezza.rdf.core.BNode.class.isAssignableFrom(nl.getClass())) {
			org.apache.clerezza.rdf.core.BNode clerezzaBNode = (org.apache.clerezza.rdf.core.BNode) nl;
			org.openrdf.model.BNode bNode = tria2SesameBNodes.get(clerezzaBNode);

			if (bNode == null) {
				bNode = valueFactory.createBNode();
				tria2SesameBNodes.put(clerezzaBNode, bNode);
			}

			return bNode;

		} else {

			//it is an uri
			String value = ((UriRef) nl).getUnicodeString();
			org.openrdf.model.URI uri = valueFactory.createURI(value);
			return uri;
		}

	}

	private org.openrdf.model.URI getPredicateFromTriple(Triple t) {
		return getSesameURIForUriRef(t.getPredicate());
	}

	private org.openrdf.model.URI getSesameURIForUriRef(UriRef ref) {

		if (ref == null) {
			return null;
		}

		ValueFactory valueFactory = repository.getValueFactory();
		org.openrdf.model.URI u = valueFactory.createURI(ref.getUnicodeString());
		return u;
	}

	private org.openrdf.model.Value getObjectFromTriple(Triple t) {
		return getSesameValueForResource(t.getObject());
	}

	private org.openrdf.model.Value getSesameValueForResource(Resource resource) {

		if (resource == null) {
			return null;
		}

		//a clerezza resource is either a non literal or a a literal
		if (NonLiteral.class.isAssignableFrom(resource.getClass())) {
			return getSesameResourceForNonLiteral((NonLiteral) resource);
		} else {

			ValueFactory valueFactory = repository.getValueFactory();
			//its a literal
			if (resource instanceof PlainLiteral) {
				PlainLiteral l = (PlainLiteral) resource;
				final Language language = l.getLanguage();
				if (language == null) {
					return valueFactory.createLiteral(l.getLexicalForm());
				}
				return valueFactory.createLiteral(l.getLexicalForm(), language.toString());
			} else {
				TypedLiteral l = (TypedLiteral) resource;
				return valueFactory.createLiteral(l.getLexicalForm(),
						new URIImpl(l.getDataType().getUnicodeString()));
			}
		}
	}

	@Override
	public boolean performRemove(Triple o) {
		writeLock.lock();
		try {
			RepositoryConnection con = repository.getConnection();
			con.setAutoCommit(false);
			boolean result = remove(o, con);
			con.commit();
			con.close();
			return result;
		} catch (RepositoryException re) {
			logger.warn("RepositoryException: {}", re);
			throw new RuntimeException(re);
		} finally {
			writeLock.unlock();
		}
	}

	private boolean remove(Object o, RepositoryConnection con) {
		if (contains(o)) {
			try {
				Triple t = (Triple) o;
				org.openrdf.model.Resource subject = getSubjectFromTriple(t);
				org.openrdf.model.URI predicate = getPredicateFromTriple(t);
				org.openrdf.model.Value object = getObjectFromTriple(t);
				con.remove(subject, predicate, object);
			} catch (RepositoryException re) {
				logger.warn("RepositoryException: {}", re);
				throw new RuntimeException(re);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> col) {
		if (col == null) {
			return false;
		}
		boolean result = false;
		writeLock.lock();
		try {
			RepositoryConnection con = repository.getConnection();
			try {
				con.setAutoCommit(false);
				for (Object o : col) {
					boolean trippleRemoved = remove(o, con);
					if (trippleRemoved) {
						result = true;
						dispatchEvent(new RemoveEvent(this, (Triple) o));
					}
				}
				con.commit();
			} finally {
				con.close();
			}
		} catch (RepositoryException re) {
			logger.warn("RepositoryException: {}", re);
			throw new RuntimeException(re);
		} finally {
			writeLock.unlock();
		}
		return result;
	}

	@Override
	public ReadWriteLock getLock() {
		return lock;
	}
}
