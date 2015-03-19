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

import org.apache.commons.rdf.impl.*;
import org.apache.commons.rdf.impl.utils.*;
import org.apache.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.commons.rdf.impl.utils.TripleImpl;

import java.util.*;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.commons.rdf.*;

/**
 * This class represents a node in the context of a graph. It provides
 * utility methods to explore and modify its neighbourhood. The method
 * modifying the graph will throw an {@link UnsupportedOperationException}
 * it the underlying Graph in immutable (i.e. is a {@link ImmutableGraph}.
 *
 * @since 0.2
 * @author reto, mir
 */
public class GraphNode {

    private final RdfTerm resource;
    private final Graph graph;

    /**
     * Create a GraphNode representing resource within graph.
     *
     * @param resource the resource this GraphNode represents
     * @param graph the Graph that describes the resource
     */
    public GraphNode(RdfTerm resource, Graph graph) {
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
    public Graph getGraph() {
        return graph;
    }

    /**
     * Gets the unwrapped node
     *
     * @return the node represented by this GraphNode
     */
    public RdfTerm getNode() {
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
     * The triples in the ImmutableGraph returned by this method contain the same bnode
     * instances as in the original graph.
     *
     * @return the context of the node represented by the instance
     */
    public ImmutableGraph getNodeContext() {
        Lock l = readLock();
        l.lock();
        try {
            final HashSet<RdfTerm> dontExpand = new HashSet<RdfTerm>();
            dontExpand.add(resource);
            if (resource instanceof Iri) {
                return getContextOf((Iri) resource, dontExpand).getImmutableGraph();
            }
            return getContextOf(resource, dontExpand).getImmutableGraph();
        } finally {
            l.unlock();
        }

    }

    private Graph getContextOf(Iri node, final Set<RdfTerm> dontExpand) {
        final String uriPrefix = node.getUnicodeString()+'#';
        return getContextOf(node, dontExpand, new Acceptor() {

            @Override
            public boolean expand(RdfTerm resource) {
                if (resource instanceof BlankNode) {
                    return true;
                }
                if (resource instanceof Iri) {
                    return ((Iri)resource).getUnicodeString().startsWith(uriPrefix);
                }
                return false;
            }
        });
    }

    /**
     * Returns the context of a <code>BlankNodeOrIri</code>
     *
     * @param node
     * @param dontExpand a list of bnodes at which to stop expansion, if node
     * is a BlankNode it should be contained (potentially faster)
     * @return the context of a node
     */
    private Graph getContextOf(RdfTerm node, final Set<RdfTerm> dontExpand) {
        return getContextOf(node, dontExpand, new Acceptor() {

            @Override
            public boolean expand(RdfTerm resource) {
                if (resource instanceof BlankNode) {
                    return true;
                }
                return false;
            }
        });
    }

    private interface Acceptor {
        boolean expand(RdfTerm resource);
    }
    private Graph getContextOf(RdfTerm node, final Set<RdfTerm> dontExpand, Acceptor acceptor) {
        Graph result = new SimpleGraph();
        if (node instanceof BlankNodeOrIri) {
            Iterator<Triple> forwardProperties = graph.filter((BlankNodeOrIri) node, null, null);
            while (forwardProperties.hasNext()) {
                Triple triple = forwardProperties.next();
                result.add(triple);
                RdfTerm object = triple.getObject();
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
            BlankNodeOrIri subject = triple.getSubject();
            if (acceptor.expand(subject) && !dontExpand.contains(subject)) {
                dontExpand.add(subject);
                result.addAll(getContextOf(subject, dontExpand, acceptor));
            }
        }
        return result;
    }

    private <T> Iterator<T> getTypeSelectedObjects(Iri property, final Class<T> type) {
        final Iterator<RdfTerm> objects = getObjects(property);
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
                    RdfTerm nextObject = objects.next();
                    if (type.isAssignableFrom(nextObject.getClass())) {
                        return (T) nextObject;
                    }
                }
                return null;
            }
        };
    }

    public Iterator<Literal> getLiterals(Iri property) {
        final Iterator<RdfTerm> objects = getObjects(property);
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
                    RdfTerm nextObject = objects.next();
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
     *        which meet the specified condition
     */
    public int countObjects(Iri property) {
        return countTriples(graph.filter((BlankNodeOrIri) resource, property, null));
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
    public Iterator<RdfTerm> getObjects(Iri property) {
        if (resource instanceof BlankNodeOrIri) {
            final Iterator<Triple> triples = graph.filter((BlankNodeOrIri) resource, property, null);
            return new Iterator<RdfTerm>() {

                @Override
                public boolean hasNext() {
                    return triples.hasNext();
                }

                @Override
                public RdfTerm next() {
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
            return new Iterator<RdfTerm>() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public RdfTerm next() {
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
    public boolean hasProperty(Iri property, RdfTerm object) {
        Lock l = readLock();
        l.lock();
        try {
            Iterator<RdfTerm> objects = getObjects(property);
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
     *        which meet the specified condition
     */
    public int countSubjects(Iri property) {
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
    public Iterator<BlankNodeOrIri> getSubjects(Iri property) {
        final Iterator<Triple> triples = graph.filter(null, property, resource);
        return new Iterator<BlankNodeOrIri>() {

            @Override
            public boolean hasNext() {
                return triples.hasNext();
            }

            @Override
            public BlankNodeOrIri next() {
                return triples.next().getSubject();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public Iterator<Iri> getIriObjects(Iri property) {
        return getTypeSelectedObjects(property, Iri.class);

    }

    /**
     * Get all available properties as an {@link Iterator}<{@link Iri}>.
     * You can use <code>getObjects(Iri property)</code> to get the values of
     * each property
     *
     * @return an iterator over properties of this node
     */
    public Iterator<Iri> getProperties() {
        if (resource instanceof BlankNodeOrIri) {
            final Iterator<Triple> triples = graph.filter((BlankNodeOrIri) resource, null, null);
            return getUniquePredicates(triples);
        } else {
            return new Iterator<Iri>() {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Iri next() {
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
     * Get all inverse properties as an {@link Iterator}<{@link Iri}>.
     * You can use <code>getSubject(Iri property)</code> to get the values of
     * each inverse property
     *
     * @return an iterator over properties pointing to this node
     */
    public Iterator<Iri> getInverseProperties() {
        final Iterator<Triple> triples = graph.filter(null, null, resource);
        return getUniquePredicates(triples);
    }

    /**
     *
     * @param triples
     * @returnan {@link Iterator}<{@link Iri}> containing the predicates from
     * an {@link Iterator}<{@link Triple}>
     */
    private Iterator<Iri> getUniquePredicates(final Iterator<Triple> triples) {
        final Set<Iri> resultSet = new HashSet<Iri>();
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
    public void addProperty(Iri predicate, RdfTerm object) {
        if (resource instanceof BlankNodeOrIri) {
            graph.add(new TripleImpl((BlankNodeOrIri) resource, predicate, object));
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
    public void addPropertyValue(Iri property, Object value) {
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
    public void addInverseProperty(Iri predicate, RdfTerm subject) {
        if (subject instanceof BlankNodeOrIri) {
            graph.add(new TripleImpl((BlankNodeOrIri) subject, predicate, resource));
        } else {
            throw new RuntimeException("Literals cannot be the subject of a statement");
        }
    }


    /**
     * creates and returns an <code>RdfList</code> for the node and
     * Graph represented by this object.
     *
     * @return a List to easy access the rdf:List represented by this node
     */
    public List<RdfTerm> asList() {
        if (resource instanceof BlankNodeOrIri) {
            return new RdfList((BlankNodeOrIri) resource, graph);
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
    public void deleteProperties(Iri predicate) {
        if (resource instanceof BlankNodeOrIri) {
            Iterator<Triple> tripleIter = graph.filter((BlankNodeOrIri) resource, predicate, null);
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
    public void deleteProperty(Iri predicate, RdfTerm object) {
        if (resource instanceof BlankNodeOrIri) {
            graph.remove(new TripleImpl((BlankNodeOrIri) resource, predicate, object));
        }
    }

    @Override
    public String toString() {
        return resource.toString();
    }

    /**
     * Replaces the graph node resouce with the specified <code>BlankNodeOrIri</code>.
     * The resource is only replaced where it is either subject or object.
     * @param replacement
     * @return a GraphNode representing the replecement node
     */
    public GraphNode replaceWith(BlankNodeOrIri replacement) {
        return replaceWith(replacement, false);
    }

    /**
     * Replaces the graph node resouce with the specified <code>BlankNodeOrIri</code>.
     * Over the boolean <code>checkPredicate</code> it can be specified if the
     * resource should also be replaced where it is used as predicate.
     * @param replacement
     * @param checkPredicates
     * @return a GraphNode representing the replecement node
     */
    public GraphNode replaceWith(BlankNodeOrIri replacement, boolean checkPredicates) {
        Graph newTriples = new SimpleGraph();
        if (!(resource instanceof Literal)) {
            Iterator<Triple> subjectTriples = graph.filter((BlankNodeOrIri) resource, null,
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

        if (checkPredicates && replacement instanceof Iri
                && resource instanceof Iri) {
            Iterator<Triple> predicateTriples = graph.filter(null,
                    (Iri) resource, null);
            while (predicateTriples.hasNext()) {
                Triple triple = predicateTriples.next();
                Triple newTriple = new TripleImpl(triple.getSubject(),
                        (Iri) replacement, triple.getObject());
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
    public Iterator<GraphNode> getObjectNodes(Iri property) {
        final Iterator<RdfTerm> objects = this.getObjects(property);
        return new Iterator<GraphNode>() {

            @Override
            public boolean hasNext() {
                return objects.hasNext();
            }

            @Override
            public GraphNode next() {
                RdfTerm object = objects.next();
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
    public Iterator<GraphNode> getSubjectNodes(Iri property) {
        final Iterator<BlankNodeOrIri> subjects = this.getSubjects(property);
        return new Iterator<GraphNode>() {

            @Override
            public boolean hasNext() {
                return subjects.hasNext();
            }

            @Override
            public GraphNode next() {
                RdfTerm object = subjects.next();
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
     * @return a ReadLock if the underlying ImmutableGraph is a LockableGraph it returns its lock, otherwise null
     */
    public Lock readLock() {

            return getGraph().getLock().readLock();

    }

    /**
     *
     * @return
     */
    public Lock writeLock() {

            return (getGraph()).getLock().writeLock();

    }
}
