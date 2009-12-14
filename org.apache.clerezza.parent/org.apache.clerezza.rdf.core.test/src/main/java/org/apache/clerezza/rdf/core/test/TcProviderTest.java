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
package org.apache.clerezza.rdf.core.test;

import java.util.Iterator;

import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;

import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import static org.junit.Assert.*;

/**
 * 
 * @author mir,rbn
 */
public abstract class TcProviderTest {

	protected final UriRef uriRefA = generateUri("a");
	protected final UriRef uriRefA1 = generateUri("a1");
	protected final UriRef uriRefB = generateUri("b");
	protected final UriRef uriRefB1 = generateUri("b1");
	protected final UriRef uriRefC = generateUri("c");

	protected final UriRef graphUriRef = generateUri("myGraph");
	protected final UriRef otherGraphUriRef = new UriRef(graphUriRef.getUnicodeString());

	@Test
	public void testCreateGraph() {
		TcProvider simpleTcmProvider = getInstance();
		MGraph mGraph = new SimpleMGraph();
		mGraph.add(new TripleImpl(uriRefA, uriRefA, uriRefA));

		Graph createdGraph = simpleTcmProvider.createGraph(uriRefA, mGraph);

		Iterator<Triple> iteratorInput = mGraph.iterator();
		Iterator<Triple> iteratorCreated = createdGraph.iterator();
		assertEquals(iteratorInput.next(), iteratorCreated.next());
		assertFalse(iteratorCreated.hasNext());

		try {
			simpleTcmProvider.createGraph(uriRefA, mGraph);
			assertTrue(false);
		} catch (EntityAlreadyExistsException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testCreateMGraph() {
		TcProvider simpleTcmProvider = getInstance();
		MGraph mGraph = simpleTcmProvider.createMGraph(uriRefA);
		assertTrue(mGraph.isEmpty());

		try {
			simpleTcmProvider.createMGraph(uriRefA);
			assertTrue(false);
		} catch (EntityAlreadyExistsException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testGetGraph() {
		TcProvider simpleTcmProvider = getInstance();
		// add Graphs
		MGraph mGraph = new SimpleMGraph();
		mGraph.add(new TripleImpl(uriRefA, uriRefA, uriRefA));
		simpleTcmProvider.createGraph(uriRefA, mGraph);
		mGraph = new SimpleMGraph();
		mGraph.add(new TripleImpl(uriRefA1, uriRefA1, uriRefA1));
		simpleTcmProvider.createGraph(uriRefA1, mGraph);
		mGraph = new SimpleMGraph();
		mGraph.add(new TripleImpl(uriRefB, uriRefB, uriRefB));
		simpleTcmProvider.createGraph(uriRefB, mGraph);
		mGraph = new SimpleMGraph();
		mGraph.add(new TripleImpl(uriRefB1, uriRefB1, uriRefB1));
		simpleTcmProvider.createGraph(uriRefB1, mGraph);

		Graph bGraph = simpleTcmProvider.getGraph(uriRefB);
		Iterator<Triple> iterator = bGraph.iterator();
		assertEquals(new TripleImpl(uriRefB, uriRefB, uriRefB), iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testGetMGraph() {
		TcProvider simpleTcmProvider = getInstance();
		// add MGraphs
		MGraph mGraph = simpleTcmProvider.createMGraph(uriRefA);
		mGraph.add(new TripleImpl(uriRefA, uriRefA, uriRefA));
		mGraph = simpleTcmProvider.createMGraph(uriRefA1);
		mGraph.add(new TripleImpl(uriRefA1, uriRefA1, uriRefA1));
		mGraph = simpleTcmProvider.createMGraph(uriRefB);
		mGraph.add(new TripleImpl(uriRefB, uriRefB, uriRefA));
		mGraph.add(new TripleImpl(uriRefB, uriRefB, uriRefB));
		mGraph.remove(new TripleImpl(uriRefB, uriRefB, uriRefA));
		assertEquals(1, mGraph.size());
		mGraph = simpleTcmProvider.createMGraph(uriRefB1);
		mGraph.add(new TripleImpl(uriRefB1, uriRefB1, uriRefB1));

		MGraph bGraph = simpleTcmProvider.getMGraph(uriRefB);
		Iterator<Triple> iterator = bGraph.iterator();
		assertEquals(new TripleImpl(uriRefB, uriRefB, uriRefB), iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testGetTriples() {
		TcProvider simpleTcmProvider = getInstance();
		// add Graphs
		MGraph mGraph = new SimpleMGraph();
		mGraph.add(new TripleImpl(uriRefA, uriRefA, uriRefA));
		simpleTcmProvider.createGraph(uriRefA, mGraph);
		mGraph = new SimpleMGraph();
		mGraph.add(new TripleImpl(uriRefB, uriRefB, uriRefB));
		simpleTcmProvider.createGraph(uriRefB, mGraph);
		// add MGraphs
		mGraph = simpleTcmProvider.createMGraph(uriRefA1);
		mGraph.add(new TripleImpl(uriRefA1, uriRefA1, uriRefA1));
		mGraph = simpleTcmProvider.createMGraph(uriRefB1);
		mGraph.add(new TripleImpl(uriRefB1, uriRefB1, uriRefB1));

		// get a Graph
		TripleCollection tripleCollection = simpleTcmProvider.getTriples(uriRefA);
		// get a MGraph
		TripleCollection tripleCollection2 = simpleTcmProvider.getTriples(uriRefB1);

		Iterator<Triple> iterator = tripleCollection.iterator();
		assertEquals(new TripleImpl(uriRefA, uriRefA, uriRefA), iterator.next());
		assertFalse(iterator.hasNext());

		iterator = tripleCollection2.iterator();
		assertEquals(new TripleImpl(uriRefB1, uriRefB1, uriRefB1), iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testDeleteEntity() {
		TcProvider simpleTcmProvider = getInstance();
		MGraph mGraph = new SimpleMGraph();
		mGraph.add(new TripleImpl(uriRefA, uriRefA, uriRefA));
		Graph graph = mGraph.getGraph();
		simpleTcmProvider.createGraph(uriRefA, graph);
		simpleTcmProvider.createGraph(uriRefC, graph);

		simpleTcmProvider.deleteTripleCollection(uriRefA);
		try {
			simpleTcmProvider.getGraph(uriRefA);
			assertTrue(false);
		} catch (NoSuchEntityException e) {
			assertTrue(true);
		}

		// Check that graph is still available under uriRefC
		Graph cGraph = simpleTcmProvider.getGraph(uriRefC);
		assertNotNull(cGraph);
	}

	/**
	 * Subclasses implement this method to provide implementation instances of
	 * <code>TcProvider</code>. The first call within a test method has to
	 * return a empty TcProvider. Subsequent calls within the test method
	 * should instantiate a new provider, but load the previously added data from
	 * its "persistent" store.
	 *
	 * @return a TcProvider of the implementation to be tested.
	 */
	protected abstract TcProvider getInstance();

//	@Test
//	public void testGetNames() {
//		MGraph mGraph = new SimpleMGraph();
//		mGraph.add(new TripleImpl(uriRefB, uriRefB, uriRefB));
//		simpleTcmProvider.createGraph(uriRefB, mGraph.getGraph());
//		
//		mGraph = new SimpleMGraph();
//		mGraph.add(new TripleImpl(uriRefA, uriRefA, uriRefA));
//		Graph graph = mGraph.getGraph();
//		simpleTcmProvider.createGraph(uriRefA, graph);
//		simpleTcmProvider.createGraph(uriRefC, graph);
//
//		Set<UriRef> names = simpleTcmProvider.getNames(graph);
//
//		assertTrue(names.contains(uriRefA));
//		assertTrue(names.contains(uriRefC));
//		assertEquals(2, names.size());
//
//		assertFalse(names.contains(uriRefB));
//	}

	@Test
	public void testCreateMGraphExtended() throws Exception {

		TcProvider provider = getInstance();
		MGraph graph = provider.createMGraph(graphUriRef);
		assertNotNull(graph);
		//get a new provider and check that graph is there
		provider = getInstance();
		graph = provider.getMGraph(graphUriRef);
		assertNotNull(graph);
		//check that there is no such graph, but only the mgraph
		boolean expThrown = false;
		try {
			Graph g = provider.getGraph(graphUriRef);
		} catch(NoSuchEntityException e) {
			expThrown = true;
		}

		assertTrue(expThrown);
	}

	@Test
	public void testCreateGraphExtended() throws Exception {

		TcProvider provider = getInstance();
		Graph graph = provider.createGraph(graphUriRef, null);

		assertNotNull(graph);

		//get a new provider and check that graph is there
		provider = getInstance();
		graph = provider.getGraph(graphUriRef);
		assertNotNull(graph);

		//check that there is no such mgraph, but only the graph
		boolean expThrown = false;

		try {
			MGraph g = provider.getMGraph(graphUriRef);
		} catch(NoSuchEntityException e) {
			expThrown = true;
		}

		assertTrue(expThrown);
	}

	@Test
	public void testCreateGraphNoDuplicateNames() throws Exception {

		TcProvider provider = getInstance();
		Graph graph = provider.createGraph(graphUriRef, null);
		assertNotNull(graph);
		boolean expThrown = false;
		try {
			Graph other = provider.createGraph(otherGraphUriRef, null);
		} catch(EntityAlreadyExistsException eaee) {
			expThrown = true;
		}
		assertTrue(expThrown);
	}

	@Test
	public void testCreateMGraphNoDuplicateNames() throws Exception {

		TcProvider provider = getInstance();
		MGraph graph = provider.createMGraph(graphUriRef);
		assertNotNull(graph);
		boolean expThrown = false;
		try {
			MGraph other = provider.createMGraph(otherGraphUriRef);
		} catch(EntityAlreadyExistsException eaee) {
			expThrown = true;
		}
		assertTrue(expThrown);
	}

	@Test
	public void testCreateGraphWithInitialCollection() throws Exception {

		Triple t1 = createTestTriple();

		TcProvider provider = getInstance();

		Graph graph = provider.createGraph(graphUriRef, createTestTripleCollection(t1));

		assertEquals(1, graph.size());
		assertTrue(graph.contains(t1));
	}

	@Test
	public void testGraphIsNotMutable() throws Exception {

		Triple t1 = createTestTriple();
		Set<Triple> t = new TreeSet<Triple>();
		t.add(t1);

		TcProvider provider = getInstance();

		Graph graph = provider.createGraph(graphUriRef, createTestTripleCollection(t1));

		boolean expThrown = false;

		try {
			graph.add(t1);
		} catch(UnsupportedOperationException uoe) {
			expThrown = true;
		}

		assertTrue(expThrown);
		expThrown = false;

		try {
			graph.remove(t1);
		} catch(UnsupportedOperationException uoe) {
			expThrown = true;
		}

		assertTrue(expThrown);
		expThrown = false;

		try {
			graph.addAll(t);
		} catch(UnsupportedOperationException uoe) {
			expThrown = true;
		}

		assertTrue(expThrown);

		expThrown = false;

		try {
			graph.clear();
		} catch(UnsupportedOperationException uoe) {
			expThrown = true;
		}

		assertTrue(expThrown);

		expThrown = false;

		try {
			graph.removeAll(t);
		} catch(UnsupportedOperationException uoe) {
			expThrown = true;
		}

		assertTrue(expThrown);
	}

//	This tests can not pass, because equals in AbstractGraph is not implemented
//	yet.
//	@Test
//	public void testGraphHasName() throws Exception {
//
//		TcProvider provider = getInstance();
//
//		TripleCollection triples = createTestTripleCollection(createTestTriple());
//		Graph graph = provider.createGraph(graphUriRef, triples);
//
//		provider = getInstance();
//		Set<UriRef> names = provider.getNames(graph);
//		assertTrue(names.contains(graphUriRef));
//	}
//
//	@Test
//	public void testCreateSameGraphWithDifferentNames() throws Exception {
//
//		TripleCollection triples = createTestTripleCollection(createTestTriple());
//
//		TcProvider provider = getInstance();
//		UriRef name1 = new UriRef("http://myGraph1");
//		Graph graph = provider.createGraph(name1, triples);
//
//		UriRef name2 = new UriRef("http://myGraph2");
//		Graph secondGraph = provider.createGraph(name2, triples);
//
//		Set<UriRef> names = provider.getNames(graph);
//		assertNotNull(names);
//		assertEquals(2, names.size());
//	}

	@Test
	public void testGraphDeletion() throws Exception {

		TripleCollection triples = createTestTripleCollection(createTestTriple());

		TcProvider provider = getInstance();
		UriRef name1 = new UriRef("http://myGraph1");
		Graph graph = provider.createGraph(name1, triples);

		UriRef name2 = new UriRef("http://myGraph2");
		Graph secondGraph = provider.createGraph(name2, triples);

		//if we delete graph with name1, the second graph should still be there
		provider.deleteTripleCollection(name1);

		provider = getInstance();
		Graph firstGraph = provider.getGraph(name2);
		assertNotNull(firstGraph);

		//check second name is not there
		boolean expThrown = false;

		try {
			Graph g = provider.getGraph(name1);
		} catch(NoSuchEntityException nses) {
			expThrown = true;
		}

		assertTrue(expThrown);
	}


	@Test
	public void testGetTriplesGraph() throws Exception {
		TcProvider provider = getInstance();
		Graph graph = provider.createGraph(graphUriRef,
				createTestTripleCollection(createTestTriple()));
		TripleCollection tc = provider.getTriples(graphUriRef);
		assertNotNull(tc);
	}

	@Test
	public void testGetTriplesMGraph() throws Exception {
		TcProvider provider = getInstance();

		MGraph graph = provider.createMGraph(graphUriRef);

		TripleCollection tc = provider.getTriples(graphUriRef);
		assertNotNull(tc);
	}

	private Triple createTestTriple() {
		NonLiteral subject = new BNode() {};
		UriRef predicate = new UriRef("http://test.com/");
		NonLiteral object = new UriRef("http://test.com/myObject");
		return new TripleImpl(subject, predicate, object);
	}

	private TripleCollection createTestTripleCollection(Triple t) {
		Set<Triple> ts = new TreeSet<Triple>();
		ts.add(t);
		return new SimpleMGraph(ts);
	}

	protected UriRef generateUri(String name) {
		return new UriRef("http://example.org/" + name);
	}
	
}
