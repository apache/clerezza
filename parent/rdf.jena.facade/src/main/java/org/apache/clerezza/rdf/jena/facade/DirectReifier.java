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
package org.apache.clerezza.rdf.jena.facade;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Reifier;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.rdf.model.impl.NodeIteratorImpl;
import com.hp.hpl.jena.shared.AlreadyReifiedException;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.NullIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.Iterator;

/**
 * an implementation of reifier that does nothing but access the underlying
 * graph to return the reifications;
 *
 * @author reto
 */
public class DirectReifier implements Reifier {
	private Graph graph;

	public DirectReifier(Graph graph) {
		this.graph = graph;
	}

	@Override
	public ExtendedIterator find(TripleMatch arg0) {
		return new NullIterator();
	}

	@Override
	public ExtendedIterator findExposed(TripleMatch arg0) {
		return new NullIterator();
	}

	@Override
	public ExtendedIterator findEither(TripleMatch arg0, boolean arg1) {
		return new NullIterator();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public ReificationStyle getStyle() {
		return ReificationStyle.Standard;
	}

	@Override
	public Graph getParentGraph() {
		return graph;
	}

	@Override
	public Node reifyAs(Node node, Triple t) {
		if (hasTriple(node)) {
			throw new AlreadyReifiedException(node);
		}
		graph.add(new Triple(node,RDF.subject.asNode(), t.getSubject()));
		graph.add(new Triple(node,RDF.predicate.asNode(), t.getPredicate()));
		graph.add(new Triple(node,RDF.object.asNode(), t.getObject()));
		return node;
	}

	@Override
	public boolean hasTriple(Node node) {
		if (!graph.contains(node,RDF.subject.asNode(), null)) {
			return false;
		}
		if (!graph.contains(node,RDF.predicate.asNode(), null)) {
			return false;
		}
		if (!graph.contains(node,RDF.object.asNode(), null)) {
			return false;
		}
		return false;
	}

	@Override
	public boolean hasTriple(Triple triple) {
		if (!graph.contains(null, RDF.subject.asNode(), triple.getSubject())) {
			return false;
		}
		if (!graph.contains(null, RDF.predicate.asNode(), triple.getPredicate())) {
			return false;
		}
		if (!graph.contains(null, RDF.object.asNode(), triple.getObject())) {
			return false;
		}
		return false;
	}

	@Override
	public ExtendedIterator allNodes() {
		ExtendedIterator tripleIter = graph.find(null, RDF.subject.asNode(), null);
		final ExtendedIterator filteredTripleIter = tripleIter.filterKeep(new Filter() {

			@Override
			public boolean accept(Object o) {
				Triple t = (Triple) o;
				return hasTriple(t.getSubject());
			}
		});
		return new NodeIteratorImpl(new Iterator() {

			@Override
			public boolean hasNext() {
				return filteredTripleIter.hasNext();
			}

			@Override
			public Object next() {
				return ((Triple)filteredTripleIter.next()).getSubject();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		}, this
		);
	}

	@Override
	public ExtendedIterator allNodes(final Triple triple) {
		ExtendedIterator tripleIter = graph.find(null, RDF.subject.asNode(), triple.getSubject());
		final ExtendedIterator filteredTripleIter = tripleIter.filterKeep(new Filter() {

			@Override
			public boolean accept(Object o) {
				Triple t = (Triple) o;
				return graph.contains(t.getSubject(), RDF.object.asNode(), triple.getObject()) 
						&& graph.contains(t.getSubject(), RDF.predicate.asNode(), triple.getPredicate());
			}
		});
		return new NodeIteratorImpl(new Iterator() {

			@Override
			public boolean hasNext() {
				return filteredTripleIter.hasNext();
			}

			@Override
			public Object next() {
				return ((Triple)filteredTripleIter.next()).getSubject();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		}, this
		);
	}

	@Override
	public void remove(Node arg0, Triple arg1) {
	}

	@Override
	public void remove(Triple arg0) {
	}

	@Override
	public boolean handledAdd(Triple arg0) {
		return false;
	}

	@Override
	public boolean handledRemove(Triple arg0) {
		return false;
	}

	@Override
	public void close() {
	}

	@Override
	public Triple getTriple(Node node) {
		ExtendedIterator iter =  graph.find(node, RDF.subject.asNode(), null);
		if (!iter.hasNext()) {
			return null;
		}
		Node subject = ((Triple)iter.next()).getObject();
		iter =  graph.find(node, RDF.predicate.asNode(), null);
		if (!iter.hasNext()) {
			return null;
		}
		Node predicate = ((Triple)iter.next()).getObject();
		iter =  graph.find(node, RDF.object.asNode(), null);
		if (!iter.hasNext()) {
			return null;
		}
		Node object = ((Triple)iter.next()).getObject();
		return new Triple(subject, predicate, object);
	}

}
