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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;


/**
 * This class represents a node in the context of a graph. It provides
 * utility methods to explore and modify its neighbourhood. The method
 * modifying the graph will throw an {@link UnsupportedOperationException}
 * it the undelying TripleCollection in inmutable (i.e. is a {@link Graph}.
 *
 * @since 0.2
 * @author reto, mir
 */
public class GraphNode {

	private final Resource resource;
	private final TripleCollection graph;

	public GraphNode(Resource resource, TripleCollection graph) {
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
		final HashSet<BNode> dontExpand = new HashSet<BNode>();
		if (resource instanceof BNode) {
			dontExpand.add((BNode) resource);
		}
		return getContextOf(resource, dontExpand).getGraph();

	}

	/**
	 * Returns the context of a <code>NonLiteral</code>
	 *
	 * @param node
	 * @param dontExpand a list of bnodes at which to stop expansion, if node
	 * is a BNode it should be contained (potentially faster)
	 * @return the context of a node
	 */
	private MGraph getContextOf(Resource node, Set<BNode> dontExpand) {
		MGraph result = new SimpleMGraph();
		if (node instanceof NonLiteral) {
			Iterator<Triple> forwardProperties = graph.filter((NonLiteral) node, null, null);
			while (forwardProperties.hasNext()) {
				Triple triple = forwardProperties.next();
				result.add(triple);
				Resource object = triple.getObject();
				if (object instanceof BNode) {
					BNode bNodeObject = (BNode) object;
					if (!dontExpand.contains(bNodeObject)) {
						dontExpand.add(bNodeObject);
						result.addAll(getContextOf(bNodeObject, dontExpand));
					}
				}
			}
		}
		Iterator<Triple> backwardProperties = graph.filter(null, null, node);
		while (backwardProperties.hasNext()) {
			Triple triple = backwardProperties.next();
			result.add(triple);
			NonLiteral subject = triple.getSubject();
			if (subject instanceof BNode) {
				BNode bNodeSubject = (BNode) subject;
				if (!dontExpand.contains(bNodeSubject)) {
					dontExpand.add(bNodeSubject);
					result.addAll(getContextOf(bNodeSubject, dontExpand));
				}
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
					return triples.next().getObject();
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
	 * Checks wether this node has the given property with the given value
	 *
	 * @param property
	 * @param object
	 * @return true if the node represented by this object is the subject of a
	 *         statement with the given prediate and object, false otherwise
	 */
	public boolean hasProperty(UriRef property, Resource object) {
		Iterator<Resource> objects = getObjects(property);
		while (objects.hasNext()) {
			if (objects.next().equals(object)) {
				return true;
			}
		}
		return false;
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
		return countTriples(graph.filter(null, property, resource));
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
	 * creates and returns an <code>RdfList</code> for the node and
	 * TripleCollection represented by this object.
	 *
	 * @return a List to easy access the rdf:List represented by this node
	 */
	public List<Resource> asList() {
		if (resource instanceof NonLiteral) {
			return new RdfList((NonLiteral)resource, graph);
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
			graph.remove(new TripleImpl((NonLiteral)resource, predicate, object));
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
			Iterator<Triple> subjectTriples = graph.filter((NonLiteral)resource, null,
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

		if (checkPredicates && replacement instanceof UriRef &&
				resource instanceof UriRef) {
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
	 *
	 * @param obj
	 * @return true if obj is an instance of the same class represening the same
	 * node in the same graph, subclasses may have different identity criteria.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj.getClass().equals(getClass()))) {
			return false;
		}
		GraphNode other = (GraphNode)obj;
		return getNode().equals(other.getNode()) &&
				getGraph().equals(other.getGraph());
	}

	@Override
	public int hashCode() {
		return 13 * getNode().hashCode() + getGraph().hashCode();
	}
}
