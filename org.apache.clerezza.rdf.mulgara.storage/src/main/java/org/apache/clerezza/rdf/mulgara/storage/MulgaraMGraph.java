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
package org.apache.clerezza.rdf.mulgara.storage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.Graph;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.URIReference;
import org.jrdf.util.ClosableIterator;
import org.mulgara.jrdf.JRDFGraph;
import org.mulgara.query.QueryException;
import org.mulgara.server.JRDFSession;
import org.mulgara.server.local.LocalSessionFactory;
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
import org.apache.clerezza.rdf.core.event.RemoveEvent;
import org.apache.clerezza.rdf.core.impl.AbstractMGraph;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.wymiwyg.commons.util.Util;
import org.wymiwyg.commons.util.collections.BidiMap;
import org.wymiwyg.commons.util.collections.BidiMapImpl;

public class MulgaraMGraph extends AbstractMGraph implements LockableMGraph{
	public static final String FAKE_BNODE_PREFIX = "http://mulgara-storage.clerezza.org/bnodes";

	private final Logger logger;

	/**
	 *  Bidirectional map for managing the conversion from
	 *  mulgara blank nodes to triamodel blank nodes and vice versa.
	 */
	private final BidiMap<BNode, URIReference> tria2MulgaraBNodes = new BidiMapImpl<BNode, URIReference>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
		
	private URI graphUri;
	private LocalSessionFactory lsf;

	/**
	 * Default constructor to create a new instance of
	 * <code>MulgaraMGraph</code>.
	 */
	public MulgaraMGraph(URI graphUri, LocalSessionFactory lsf) {
		logger = LoggerFactory.getLogger(MulgaraMGraph.class);
		this.graphUri = graphUri;
		this.lsf = lsf;
	}
	
	private Graph getMulgaraGraph() {

		try {
			JRDFSession session = (JRDFSession) lsf.newJRDFSession();
			try {
				Graph graph = new JRDFGraph(session, graphUri);
				return graph;
			} catch (GraphException ex) {
				session.close();
				throw new RuntimeException(ex);
			}
		} catch (QueryException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Triple convert2ClerezzaTriple(org.jrdf.graph.Triple mulgaraTriple) {
		NonLiteral subject = getNonLiteralFromMulgaraSubject(mulgaraTriple.getSubject());
		UriRef predicate = getUriRefFromMulgaraUri(mulgaraTriple.getPredicate());
		Resource object = getResourceFromMulgaraValue(mulgaraTriple.getObject());
		return new TripleImpl(subject, predicate, object);
	}

	/**
	 *  convert jrdf SubjectNode to scb NonLiteral
	 */
	private NonLiteral getNonLiteralFromMulgaraSubject(SubjectNode subject) {

		if (subject instanceof BlankNode) {
			throw new RuntimeException("native mulgara BNodes not yet supported");
		}
		URIReference mulgaraUri = (URIReference) subject;
		if (mulgaraUri.getURI().toString().startsWith(
				FAKE_BNODE_PREFIX)) {
			//return getBNodeFromMulgaraBNode((BlankNode)subject);
			return getBNodeFromMulgaraFakeBNode(mulgaraUri);
		} else {			
			return getUriRefFromMulgaraUri(mulgaraUri);
		}
	}

	/**
	 *  create or get BNode for mulgara bnode
	 */
	/*private org.apache.clerezza.rdf.core.BNode getBNodeFromMulgaraBNode(BlankNode mulgaraBNode) {

		org.apache.clerezza.rdf.core.BNode result = tria2MulgaraBNodes.getKey(mulgaraBNode);
		if (result == null) {
			result = new BNode();
			tria2MulgaraBNodes.put(result, mulgaraBNode);
		}
		return result;
	}*/
	private BNode getBNodeFromMulgaraFakeBNode(URIReference mulgaraBNode) {

		BNode result = tria2MulgaraBNodes.getKey(mulgaraBNode);

		if (result == null) {
			result = new BNode();
			tria2MulgaraBNodes.put(result, mulgaraBNode);
		}
		return result;
	}

	/**
	 *  convert openrdf URI to scb UriRef
	 */
	private UriRef getUriRefFromMulgaraUri(org.openrdf.model.URI uri) {
		UriRef ref = new UriRef(uri.toString());
		return ref;
	}

	/**
	 * convert openrdf Value to clerezza resource
	 */
	private Resource getResourceFromMulgaraValue(org.openrdf.model.Value value) {
		if (value instanceof org.openrdf.model.Literal) {
			return getLiteralFromMulgaraLiteral((org.openrdf.model.Literal) value);
		}
		return getNonLiteralFromMulgaraSubject((SubjectNode) value);
	}

	private Resource getLiteralFromMulgaraLiteral(org.openrdf.model.Literal literal) {
		org.openrdf.model.URI dt = literal.getDatatype();
		if (dt == null) {
			String languageString = literal.getLanguage();
			Language language = languageString == null ? null : new Language(
					languageString);
			return new PlainLiteralImpl(literal.getLabel(), language);
		}
		return new TypedLiteralImpl(literal.getLabel(), getUriRefFromMulgaraUri(dt));
	}

	@Override
	public int size() {
		Graph graph = getMulgaraGraph();
		readLock.lock();
		try {
			return (int)graph.getNumberOfTriples();
		} catch (GraphException e) {
			logger.warn("GraphException: {}", e);
			throw new RuntimeException(e);
		} finally {
			readLock.unlock();
			graph.close();
		}
	}

	@Override
	public boolean addAll(Collection<? extends Triple> triples) {
		if (triples == null) {
			return false;
		}
		writeLock.lock();
		try {
			Graph graph = getMulgaraGraph();
			GraphElementFactory elementFactory = graph.getElementFactory();
			try {
				Set<org.jrdf.graph.Triple> jrdfTriples = new HashSet<org.jrdf.graph.Triple>();
				Set<Triple> newTriples = new HashSet<Triple>();
				for (Triple triple : triples) {
					org.jrdf.graph.Triple jrdfTriple = convert2JrdfTriple(triple,
							elementFactory);
					if (!graph.contains(jrdfTriple)){
						jrdfTriples.add(jrdfTriple);
						newTriples.add(triple);
					}
					
				}
				graph.add(jrdfTriples.iterator());
				for (Triple triple: newTriples) {
					dispatchEvent(new AddEvent(this, triple));
				}
			} catch (GraphElementFactoryException e) {
				logger.warn("GraphElementFactoryException: {}", e);
				throw new RuntimeException(e);
			} catch (URISyntaxException e) {
				logger.warn("URISyntaxException: {}", e);
				throw new RuntimeException(e);
			} finally {
				graph.close();
			}
			return true;
		} catch (GraphException e) {
			logger.warn("GraphException: {}", e);
			throw new RuntimeException(e);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public Iterator<Triple> performFilter(
			NonLiteral subject, UriRef predicate, Resource object) {

		try {
			Graph graph = getMulgaraGraph();
			GraphElementFactory elementFactory = graph.getElementFactory();
			org.jrdf.graph.Triple jrdfTriple = convert2JrdfTriple(subject, predicate,
					object, elementFactory);
			readLock.lock();
			try {
				return new MulgaraIteratorWrapper(graph.find(jrdfTriple));
			} finally {
				readLock.unlock();
				graph.close();
			}			
		} catch (GraphElementFactoryException e) {
			logger.debug("GraphElementFactoryException: {}", e);
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			logger.debug("URISyntaxException: {}", e);
			throw new RuntimeException(e);
		} catch (GraphException e) {
			logger.debug("GraphException: {}", e);
			throw new RuntimeException(e);
		}							
	}

	@Override
	public boolean performAdd(Triple triple) {
		Graph graph = getMulgaraGraph();
		writeLock.lock();
		try {
			GraphElementFactory elementFactory = graph.getElementFactory();
			org.jrdf.graph.Triple jrdfTriple = convert2JrdfTriple(triple,
					elementFactory);
			long sizeBefore = graph.getNumberOfTriples();
			graph.add(jrdfTriple);
			long sizeAfter = graph.getNumberOfTriples();
			return sizeBefore < sizeAfter;
		}  catch (GraphElementFactoryException e) {
			logger.warn("GraphElementFactoryException: {}", e);
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			logger.warn("URISyntaxException: {}", e);
			throw new RuntimeException(e);
		} catch (GraphException e) {
			logger.warn("GraphException: {}", e);
			throw new RuntimeException(e);
		} finally {	
			writeLock.unlock();
			graph.close();
		}
	}

	private org.jrdf.graph.Triple convert2JrdfTriple(Triple triple,
			GraphElementFactory elementFactory)
			throws GraphElementFactoryException, URISyntaxException {
		return convert2JrdfTriple(triple.getSubject(), triple.getPredicate(),
				triple.getObject(), elementFactory);
	}
	
	private org.jrdf.graph.Triple convert2JrdfTriple(NonLiteral subject, UriRef predicate,
			Resource object, GraphElementFactory elementFactory)
			throws GraphElementFactoryException, URISyntaxException {
		SubjectNode mulgaraSubject = getMulgaraSubjectNodeForNonLiteral(subject, elementFactory);
		PredicateNode mulgaraPredicate = getMulgaraURIForUriRef(predicate, elementFactory);
		ObjectNode mulgaraObject = getMulgaraObjectNodeForResource(object, elementFactory);
		
		org.jrdf.graph.Triple triple = elementFactory.createTriple(mulgaraSubject, mulgaraPredicate, mulgaraObject);
		return triple;
	}

	private SubjectNode getMulgaraSubjectNodeForNonLiteral(
			NonLiteral nl, GraphElementFactory factory) 
		throws GraphElementFactoryException, URISyntaxException {

		if (nl == null) {
			return null;
		}
		if (nl instanceof BNode) {
			BNode clerezzaBNode = (BNode) nl;
			URIReference blankNode = tria2MulgaraBNodes.get(clerezzaBNode);
			if (blankNode == null) {
				blankNode = factory.createResource(new URI(FAKE_BNODE_PREFIX
						+Util.createRandomString(8)));
				tria2MulgaraBNodes.put(clerezzaBNode, blankNode);
			} 
			return blankNode;

		} else {

			//it is an uri
			String value = ((UriRef) nl).getUnicodeString();
			return factory.createResource(new URI(value));
		}
	}

	private URIReference getMulgaraURIForUriRef(UriRef ref,
			GraphElementFactory factory) throws GraphElementFactoryException, URISyntaxException {

		if (ref == null) {
			return null;
		}
		return factory.createResource(new URI(ref.getUnicodeString()));
	}

	private ObjectNode getMulgaraObjectNodeForResource(Resource resource, 
			GraphElementFactory elementFactory) throws GraphElementFactoryException, URISyntaxException {

		if (resource == null) {
			return null;
		}

		//a clerezza resource is either a non literal or a a literal
		if (NonLiteral.class.isAssignableFrom(resource.getClass())) {
			return (ObjectNode) getMulgaraSubjectNodeForNonLiteral((NonLiteral) resource, elementFactory);
		} else {

			//its a literal
			if (resource instanceof PlainLiteral) {
				PlainLiteral l = (PlainLiteral) resource;
				final Language language = l.getLanguage();
				if (language == null) {
					return elementFactory.createLiteral(l.getLexicalForm());
				}
				return elementFactory.createLiteral(l.getLexicalForm(), language.toString());
			} else {
				TypedLiteral l = (TypedLiteral) resource;
				return elementFactory.createLiteral(l.getLexicalForm(),
						new URI(l.getDataType().getUnicodeString()));
			}
		}
	}

	@Override
	public boolean performRemove(Triple triple) {
		Graph graph = getMulgaraGraph();
		writeLock.lock();
		try {
			GraphElementFactory elementFactory = graph.getElementFactory();
			org.jrdf.graph.Triple jrdfTriple = convert2JrdfTriple(triple,
					elementFactory);
			long sizeBefore = graph.getNumberOfTriples();
			graph.remove(jrdfTriple);
			long sizeAfter = graph.getNumberOfTriples();
			return sizeBefore > sizeAfter;
		}  catch (GraphElementFactoryException e) {
			logger.warn("GraphElementFactoryException: {}", e);
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			logger.warn("URISyntaxException: {}", e);
			throw new RuntimeException(e);
		} catch (GraphException e) {
			return false;
		} finally {
			writeLock.unlock();
			graph.close();
		}	
	}

	@Override
	public boolean removeAll(Collection<?> triples) {
		if (triples == null) {
			return false;
		}
		writeLock.lock();
		try {
			Graph graph = getMulgaraGraph();
			GraphElementFactory elementFactory = graph.getElementFactory();
			try {
				Set<org.jrdf.graph.Triple> jrdfTriples = new HashSet<org.jrdf.graph.Triple>();
				Set<Triple> newTriples = new HashSet<Triple>();
				for (Object object : triples) {
					Triple triple = (Triple) object;
					org.jrdf.graph.Triple jrdfTriple = convert2JrdfTriple(triple,
							elementFactory);
					if (graph.contains(jrdfTriple)){
						jrdfTriples.add(jrdfTriple);
						newTriples.add(triple);
					}
				}
				graph.remove(jrdfTriples.iterator());
				for (Triple triple: newTriples) {
					dispatchEvent(new RemoveEvent(this, triple));
				}
			} catch (GraphElementFactoryException e) {
				logger.warn("GraphElementFactoryException: {}", e);
				throw new RuntimeException(e);
			} catch (URISyntaxException e) {
				logger.warn("URISyntaxException: {}", e);
				throw new RuntimeException(e);
			} finally {
				graph.close();
			}
			return true;
		} catch (GraphException e) {
			logger.warn("GraphException: {}", e);
			throw new RuntimeException(e);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public ReadWriteLock getLock() {
		return lock;
	}

	private class MulgaraIteratorWrapper implements Iterator<Triple> {

		Triple lastReturned = null;
		Iterator mulgaraIterator;

		public MulgaraIteratorWrapper(ClosableIterator mulgaraIterator) {
			//this.mulgaraIterator = mulgaraIterator;

			// provisional, maybe until rdf core supports closable iterators.
			List<org.jrdf.graph.Triple> allTriples =
				new ArrayList<org.jrdf.graph.Triple>();
			while (mulgaraIterator.hasNext()) {
				allTriples.add((org.jrdf.graph.Triple)mulgaraIterator.next());
			}
			this.mulgaraIterator = allTriples.iterator();
		}

		@Override
		public boolean hasNext() {
			readLock.lock();
			try {
				return mulgaraIterator.hasNext();
			} catch (IllegalStateException e) {
				return false;
			} finally {
				readLock.unlock();
			}
		}

		@Override
		public Triple next() {
			readLock.lock();
			try {
				lastReturned = convert2ClerezzaTriple((org.jrdf.graph.Triple)mulgaraIterator.next());
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
				MulgaraMGraph.this.performRemove(lastReturned);
				lastReturned = null;
			} finally {
				writeLock.unlock();
			}
		}
	}	
}
