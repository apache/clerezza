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
package org.apache.clerezza.rdf.utils;

import org.apache.clerezza.rdf.core.*;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;

import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * This class represents a node in the context of a graph. It provides
 * utility methods to explore and modify its neighbourhood. The method
 * modifying the graph will throw an {@link UnsupportedOperationException}
 * it the underlying TripleCollection in immutable (i.e. is a {@link Graph}.
 *
 * @since 0.2
 * @author reto, mir
 */
public class GraphNode {

	private final Resource resource;
	private final TripleCollection graph;

	/**
	 * Create a GraphNode representing resource within graph.
	 *
	 * @param resource the resource this GraphNode represents
	 * @param graph the TripleCollection that describes the resource
	 */
	public GraphNode(Resource resource, TripleCollection graph) {
		if (resource == null) {
			throw new IllegalArgumentException("resource may not be null");
		}
		if (graph == null) {
			throw new IllegalArgumentException("graph may not be null");
		}
		this.resource = resource;
		this.graph = graph;
	}

	/**
	 * Gets the graph the node represented by this instance is in
	 *
	 * @return the graph of this GraphNode
	 */
	public TripleCollection getGraph() {
		return graph;
	}

	/**
	 * Gets the unwrapped node
	 *
	 * @return the node represented by this GraphNode
	 */
	public Resource getNode() {
		return resource;
	}

	/**
	 * Deletes the context of a node
	 * @see getNodeContext()
	 */
	public void deleteNodeContext() {
		for (Triple triple : getNodeContext()) {
			graph.remove(triple);
		}
	}

	/**
	 * The context of a node are the triples containing a node
	 * as subject or object and recursively the context of the b-nodes in any
	 * of these statements.
	 *
	 * The triples in the Graph returned by this method contain the same bnode
	 * instances as in the original graph.
	 *
	 * @return the context of the node represented by the instance
	 */
	public Graph getNodeContext() {
		Lock l = readLock();
		l.lock();
		try {
			final HashSet<Resource> dontExpand = new HashSet<Resource>();
			dontExpand.add(resource);
			if (resource instanceof UriRef) {
				return getContextOf((UriRef) resource, dontExpand).getGraph();
			}
			return getContextOf(resource, dontExpand).getGraph();
		} finally {
			l.unlock();
		}

	}

	private MGraph getContextOf(UriRef node, final Set<Resource> dontExpand) {
		final String uriPrefix = node.getUnicodeString()+'#';
		return getContextOf(node, dontExpand, new Acceptor() {

			@Override
			public boolean expand(Resource resource) {
				if (resource instanceof BNode) {
					return true;
				}
				if (resource instanceof UriRef) {
					return ((UriRef)resource).getUnicodeString().startsWith(uriPrefix);
				}
				return false;
			}
		});
	}

	/**
	 * Returns the context of a <code>NonLiteral</code>
	 *
	 * @param node
	 * @param dontExpand a list of bnodes at which to stop expansion, if node
	 * is a BNode it should be contained (potentially faster)
	 * @return the context of a node
	 */
	private MGraph getContextOf(Resource node, final Set<Resource> dontExpand) {
		return getContextOf(node, dontExpand, new Acceptor() {

			@Override
			public boolean expand(Resource resource) {
				if (resource instanceof BNode) {
					return true;
				}
				return false;
			}
		});
	}

	private interface Acceptor {
		boolean expand(Resource resource);
	}
	private MGraph getContextOf(Resource node, final Set<Resource> dontExpand, Acceptor acceptor) {
		MGraph result = new SimpleMGraph();
		if (node instanceof NonLiteral) {
			Iterator<Triple> forwardProperties = graph.filter((NonLiteral) node, null, null);
			while (forwardProperties.hasNext()) {
				Triple triple = forwardProperties.next();
				result.add(triple);
				Resource object = triple.getObject();
				if (acceptor.expand(object) && !dontExpand.contains(object)) {
					dontExpand.add(object);
					result.addAll(getContextOf(object, dontExpand, acceptor));
				}
			}
		}
		Iterator<Triple> backwardProperties = graph.filter(null, null, node);
		while (backwardProperties.hasNext()) {
			Triple triple = backwardProperties.next();
			result.add(triple);
			NonLiteral subject = triple.getSubject();
			if (acceptor.expand(subject) && !dontExpand.contains(subject)) {
				dontExpand.add(subject);
				result.addAll(getContextOf(subject, dontExpand, acceptor));
			}
		}
		return result;
	}

	private <T> Iterator<T> getTypeSelectedObjects(UriRef property, final Class<T> type) {
		final Iterator<Resource> objects = getObjects(property);
		return new Iterator<T>() {

			T next = prepareNext();

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public T next() {
				T result = next;
				next = prepareNext();
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			private T prepareNext() {
				while (objects.hasNext()) {
					Resource nextObject = objects.next();
					if (type.isAssignableFrom(nextObject.getClass())) {
						return (T) nextObject;
					}
				}
				return null;
			}
		};
	}

	public Iterator<Literal> getLiterals(UriRef property) {
		final Iterator<Resource> objects = getObjects(property);
		return new Iterator<Literal>() {

			Literal next = prepareNext();

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public Literal next() {
				Literal result = next;
				next = prepareNext();
				return result;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			private Literal prepareNext() {
				while (objects.hasNext()) {
					Resource nextObject = objects.next();
					if (nextObject instanceof Literal) {
						return (Literal) nextObject;
					}
				}
				return null;
			}
		};
	}

	/**
	 * Count the number of triples in the underlying triple-collection
	 * with this node as subject and a specified property as predicate.
	 *
	 * @param property the property to be examined
	 * @return the number of triples in the underlying triple-collection
	 *		which meet the specified condition
	 */
	public int countObjects(UriRef property) {
		return countTriples(graph.filter((NonLiteral) resource, property, null));
	}

	private int countTriples(final Iterator<Triple> triples) {
		int count = 0;
		while (triples.hasNext()) {
			triples.next();
			count++;
		}
		return count;
	}

	/**
	 * Get the objects of statements with this node as subject and a specified
	 * property as predicate.
	 *
	 * @param property the property
	 * @return
	 */
	public Iterator<Resource> getObjects(UriRef property) {
		if (resource instanceof NonLiteral) {
			final Iterator<Triple> triples = graph.filter((NonLiteral) resource, property, null);
			return new Iterator<Resource>() {

				@Override
				public boolean hasNext() {
					return triples.hasNext();
				}

				@Override
				public Resource next() {
					final Triple triple = triples.next();
					if (triple != null) {
						return triple.getObject();
					} else {
						return null;
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Not supported yet.");
				}
			};
		} else {
			return new Iterator<Resource>() {

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public Resource next() {
					return null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Not supported yet.");
				}
			};
		}
	}

	/**
	 * Checks wether this node has the given property with the given value.
	 * If the given value is null, then it is checked if this node has the
	 * specified property regardless of its value.
	 *
	 * @param property
	 * @param object
	 * @return true if the node represented by this object is the subject of a
	 *         statement with the given prediate and object, false otherwise
	 */
	public boolean hasProperty(UriRef property, Resource object) {
		Lock l = readLock();
		l.lock();
		try {
			Iterator<Resource> objects = getObjects(property);
			if (object == null) {
				return objects.hasNext();
			}
			while (objects.hasNext()) {
				if (objects.next().equals(object)) {
					return true;
				}
			}
			return false;
		} finally {
			l.unlock();
		}
	}

	/**
	 * Count the number of triples in the underlying triple-collection
	 * with this node as object and a specified property as predicate.
	 *
	 * @param property the property to be examined
	 * @return the number of triples in the underlying triple-collection
	 *		which meet the specified condition
	 */
	public int countSubjects(UriRef property) {
		Lock l = readLock();
		l.lock();
		try {
			return countTriples(graph.filter(null, property, resource));
		} finally {
			l.unlock();
		}
	}

	/**
	 * Get the subjects of statements with this node as object and a specified
	 * property as predicate.
	 *
	 * @param property the property
	 * @return
	 */
	public Iterator<NonLiteral> getSubjects(UriRef property) {
		final Iterator<Triple> triples = graph.filter(null, property, resource);
		return new Iterator<NonLiteral>() {

			@Override
			public boolean hasNext() {
				return triples.hasNext();
			}

			@Override
			public NonLiteral next() {
				return triples.next().getSubject();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}

	public Iterator<UriRef> getUriRefObjects(UriRef property) {
		return getTypeSelectedObjects(property, UriRef.class);

	}

	/**
	 * Get all available properties as an {@link Iterator}<{@link UriRef}>.
	 * You can use <code>getObjects(UriRef property)</code> to get the values of
	 * each property
	 *
	 * @return an iterator over properties of this node
	 */
	public Iterator<UriRef> getProperties() {
		if (resource instanceof NonLiteral) {
			final Iterator<Triple> triples = graph.filter((NonLiteral) resource, null, null);
			return getUniquePredicates(triples);
		} else {
			return new Iterator<UriRef>() {

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public UriRef next() {
					return null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException("Not supported yet.");
				}
			};
		}
	}

	/**
	 * Get all inverse properties as an {@link Iterator}<{@link UriRef}>.
	 * You can use <code>getSubject(UriRef property)</code> to get the values of
	 * each inverse property
	 *
	 * @return an iterator over properties pointing to this node
	 */
	public Iterator<UriRef> getInverseProperties() {
		final Iterator<Triple> triples = graph.filter(null, null, resource);
		return getUniquePredicates(triples);
	}

	/**
	 *
	 * @param triples
	 * @returnan {@link Iterator}<{@link UriRef}> containing the predicates from
	 * an {@link Iterator}<{@link Triple}>
	 */
	private Iterator<UriRef> getUniquePredicates(final Iterator<Triple> triples) {
		final Set<UriRef> resultSet = new HashSet<UriRef>();
		while (triples.hasNext()) {
			resultSet.add(triples.next().getPredicate());
		}
		return resultSet.iterator();
	}

	/**
	 * Adds a property to the node with the specified predicate and object
	 *
	 * @param predicate
	 * @param object
	 */
	public void addProperty(UriRef predicate, Resource object) {
		if (resource instanceof NonLiteral) {
			graph.add(new TripleImpl((NonLiteral) resource, predicate, object));
		} else {
			throw new RuntimeException("Literals cannot be the subject of a statement");
		}
	}


	/**
	 * Coverts the value into a typed literals and sets it as object of the
	 * specified property
	 *
	 * @param property the predicate of the triple to be created
	 * @param value the value of the typed literal object
	 */
	public void addPropertyValue(UriRef property, Object value) {
		addProperty(property,
				LiteralFactory.getInstance().createTypedLiteral(value));
	}

	/**
	 * Adds a property to the node with the inverse of the specified predicate and object
	 * In other words <code>subject</code> will be related via the property <code>relation</code> to this node.
	 *
	 * @param predicate
	 * @param subject
	 */
	public void addInverseProperty(UriRef predicate, Resource subject) {
		if (subject instanceof NonLiteral) {
			graph.add(new TripleImpl((NonLiteral) subject, predicate, resource));
		} else {
			throw new RuntimeException("Literals cannot be the subject of a statement");
		}
	}


	/**
	 * creates and returns an <code>RdfList</code> for the node and
	 * TripleCollection represented by this object.
	 *
	 * @return a List to easy access the rdf:List represented by this node
	 */
	public List<Resource> asList() {
		if (resource instanceof NonLiteral) {
			return new RdfList((NonLiteral) resource, graph);
		} else {
			throw new RuntimeException("Literals cannot be the subject of a List");
		}
	}

	/**
	 * Deletes all statement with the current node as subject and the specified
	 * predicate
	 *
	 * @param predicate
	 */
	public void deleteProperties(UriRef predicate) {
		if (resource instanceof NonLiteral) {
			Iterator<Triple> tripleIter = graph.filter((NonLiteral) resource, predicate, null);
			Collection<Triple> toDelete = new ArrayList<Triple>();
			while (tripleIter.hasNext()) {
				Triple triple = tripleIter.next();
				toDelete.add(triple);
			}
			for (Triple triple : toDelete) {
				graph.remove(triple);
			}
		}
	}

	/**
	 * Delete property to the node with the specified predicate and object
	 *
	 * @param predicate
	 * @param object
	 */
	public void deleteProperty(UriRef predicate, Resource object) {
		if (resource instanceof NonLiteral) {
			graph.remove(new TripleImpl((NonLiteral) resource, predicate, object));
		}
	}

	@Override
	public String toString() {
		return resource.toString();
	}

	/**
	 * Replaces the graph node resouce with the specified <code>NonLiteral</code>.
	 * The resource is only replaced where it is either subject or object.
	 * @param replacement
	 * @return a GraphNode representing the replecement node
	 */
	public GraphNode replaceWith(NonLiteral replacement) {
		return replaceWith(replacement, false);
	}

	/**
	 * Replaces the graph node resouce with the specified <code>NonLiteral</code>.
	 * Over the boolean <code>checkPredicate</code> it can be specified if the
	 * resource should also be replaced where it is used as predicate.
	 * @param replacement
	 * @param checkPredicates
	 * @return a GraphNode representing the replecement node
	 */
	public GraphNode replaceWith(NonLiteral replacement, boolean checkPredicates) {
		MGraph newTriples = new SimpleMGraph();
		if (!(resource instanceof Literal)) {
			Iterator<Triple> subjectTriples = graph.filter((NonLiteral) resource, null,
					null);
			while (subjectTriples.hasNext()) {
				Triple triple = subjectTriples.next();
				Triple newTriple = new TripleImpl(replacement, triple.getPredicate(),
						triple.getObject());
				subjectTriples.remove();
				newTriples.add(newTriple);
			}
			graph.addAll(newTriples);
			newTriples.clear();
		}

		Iterator<Triple> objectTriples = graph.filter(null, null, resource);
		while (objectTriples.hasNext()) {
			Triple triple = objectTriples.next();
			Triple newTriple = new TripleImpl(triple.getSubject(),
					triple.getPredicate(), replacement);
			objectTriples.remove();
			newTriples.add(newTriple);
		}
		graph.addAll(newTriples);
		newTriples.clear();

		if (checkPredicates && replacement instanceof UriRef
				&& resource instanceof UriRef) {
			Iterator<Triple> predicateTriples = graph.filter(null,
					(UriRef) resource, null);
			while (predicateTriples.hasNext()) {
				Triple triple = predicateTriples.next();
				Triple newTriple = new TripleImpl(triple.getSubject(),
						(UriRef) replacement, triple.getObject());
				predicateTriples.remove();
				newTriples.add(newTriple);
			}
			graph.addAll(newTriples);
		}
		return new GraphNode(replacement, graph);
	}

	/**
	 * Returns a iterator containing all objects of the triples where this
	 * graph node is the subject and has the specified property. The objects
	 * are returned as <code>GraphNode</code>s.
	 *
	 * @param property
	 * @return
	 */
	public Iterator<GraphNode> getObjectNodes(UriRef property) {
		final Iterator<Resource> objects = this.getObjects(property);
		return new Iterator<GraphNode>() {

			@Override
			public boolean hasNext() {
				return objects.hasNext();
			}

			@Override
			public GraphNode next() {
				Resource object = objects.next();
				return new GraphNode(object, graph);

			}

			@Override
			public void remove() {
				objects.remove();
			}
		};
	}

	/**
	 * Returns a iterator containing all subjects of the triples where this
	 * graph node is the object and has the specified property. The subjects
	 * are returned as <code>GraphNode</code>s.
	 *
	 * @param property
	 * @return
	 */
	public Iterator<GraphNode> getSubjectNodes(UriRef property) {
		final Iterator<NonLiteral> subjects = this.getSubjects(property);
		return new Iterator<GraphNode>() {

			@Override
			public boolean hasNext() {
				return subjects.hasNext();
			}

			@Override
			public GraphNode next() {
				Resource object = subjects.next();
				return new GraphNode(object, graph);

			}

			@Override
			public void remove() {
				subjects.remove();
			}
		};
	}

	/**
	 *
	 * @param obj
	 * @return true if obj is an instance of the same class represening the same
	 * node in the same graph, subclasses may have different identity criteria.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj.getClass().equals(getClass()))) {
			return false;
		}
		GraphNode other = (GraphNode) obj;
		return getNode().equals(other.getNode())
				&& getGraph().equals(other.getGraph());
	}

	@Override
	public int hashCode() {
		return 13 * getNode().hashCode() + getGraph().hashCode();
	}

	/**
	 * @return a ReadLock if the underlying Graph is a LockableMGraph it returns its lock, otherwise null
	 */
	public Lock readLock() {
		if (getGraph() instanceof LockableMGraph) {
			return ((LockableMGraph) getGraph()).getLock().readLock();
		}
		return new FakeLock();
	}

	/**
	 *
	 * @return
	 */
	public Lock writeLock() {
		if (getGraph() instanceof LockableMGraph) {
			return ((LockableMGraph) getGraph()).getLock().writeLock();
		}
		return new FakeLock();
	}

	private static class FakeLock implements Lock {

		public void lock() {
		}

		public void lockInterruptibly() throws java.lang.InterruptedException {
		}

		public boolean tryLock() {
			return false;
		}

		public boolean tryLock(long l, java.util.concurrent.TimeUnit timeUnit) throws java.lang.InterruptedException {
			return false;
		}

		public void unlock() {
		}

		public java.util.concurrent.locks.Condition newCondition() {
			return null;
		}
	}
}
