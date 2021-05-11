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
package org.apache.clerezza.dataset;

import org.apache.clerezza.Graph;
import org.apache.clerezza.IRI;
import org.apache.clerezza.ImmutableGraph;
import org.apache.clerezza.Triple;
import org.apache.clerezza.dataset.providers.WeightedA;
import org.apache.clerezza.dataset.providers.WeightedA1;
import org.apache.clerezza.dataset.providers.WeightedAHeavy;
import org.apache.clerezza.dataset.providers.WeightedBlight;
import org.apache.clerezza.implementation.TripleImpl;
import org.apache.clerezza.implementation.in_memory.SimpleGraph;
import org.apache.clerezza.sparql.NoQueryEngineException;
import org.apache.clerezza.sparql.QueryEngine;
import org.apache.clerezza.sparql.query.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 
 * @author reto
 */
@RunWith(JUnitPlatform.class)
public class TcManagerTest {

	public static IRI uriRefAHeavy = new IRI("http://example.org/aHeavy");
	public static IRI uriRefB = new IRI("http://example.org/b");;
	public static final IRI uriRefA = new IRI("http://example.org/a");
	public static final IRI uriRefA1 = new IRI("http://example.org/a1");
	private TcManager graphAccess;
	private QueryEngine queryEngine;
	private final WeightedA weightedA = new WeightedA();
	private final WeightedA1 weightedA1 = new WeightedA1();
	private WeightedTcProvider weightedBlight = new WeightedBlight();

	@BeforeEach
	public void setUp() {
		graphAccess = TcManager.getInstance();
		graphAccess.bindWeightedTcProvider(weightedA);
		graphAccess.bindWeightedTcProvider(weightedA1);
		graphAccess.bindWeightedTcProvider(weightedBlight);

		queryEngine = Mockito.mock(QueryEngine.class);
	}

	@AfterEach
	public void tearDown() {
		graphAccess = TcManager.getInstance();
		graphAccess.unbindWeightedTcProvider(weightedA);
		graphAccess.unbindWeightedTcProvider(weightedA1);
		graphAccess.unbindWeightedTcProvider(weightedBlight);

		queryEngine = null;
	}

	@Test
	public void getGraphFromA() {
		ImmutableGraph graphA = graphAccess.getImmutableGraph(uriRefA);
		Iterator<Triple> iterator = graphA.iterator();
		assertEquals(new TripleImpl(uriRefA, uriRefA, uriRefA), iterator.next());
		assertFalse(iterator.hasNext());
		Graph triplesA = graphAccess.getGraph(uriRefA);
		iterator = triplesA.iterator();
		assertEquals(new TripleImpl(uriRefA, uriRefA, uriRefA), iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void getGraphFromB() {
		ImmutableGraph graphA = graphAccess.getImmutableGraph(uriRefB);
		Iterator<Triple> iterator = graphA.iterator();
		assertEquals(new TripleImpl(uriRefB, uriRefB, uriRefB), iterator.next());
		assertFalse(iterator.hasNext());
		Graph triplesA = graphAccess.getGraph(uriRefB);
		iterator = triplesA.iterator();
		assertEquals(new TripleImpl(uriRefB, uriRefB, uriRefB), iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void getGraphFromAAfterUnbinding() {
		graphAccess.unbindWeightedTcProvider(weightedA);
		ImmutableGraph graphA = graphAccess.getImmutableGraph(uriRefA);
		Iterator<Triple> iterator = graphA.iterator();
		assertEquals(new TripleImpl(uriRefA1, uriRefA1, uriRefA1),
				iterator.next());
		assertFalse(iterator.hasNext());
		Graph triplesA = graphAccess.getGraph(uriRefA);
		iterator = triplesA.iterator();
		assertEquals(new TripleImpl(uriRefA1, uriRefA1, uriRefA1),
				iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void getGraphFromAWithHeavy() {
		final WeightedAHeavy weightedAHeavy = new WeightedAHeavy();
		graphAccess.bindWeightedTcProvider(weightedAHeavy);
		ImmutableGraph graphA = graphAccess.getImmutableGraph(uriRefA);
		Iterator<Triple> iterator = graphA.iterator();
		assertEquals(new TripleImpl(uriRefAHeavy, uriRefAHeavy, uriRefAHeavy),
				iterator.next());
		assertFalse(iterator.hasNext());
		Graph triplesA = graphAccess.getGraph(uriRefA);
		iterator = triplesA.iterator();
		assertEquals(new TripleImpl(uriRefAHeavy, uriRefAHeavy, uriRefAHeavy),
				iterator.next());
		assertFalse(iterator.hasNext());
		graphAccess.unbindWeightedTcProvider(weightedAHeavy);
	}

	@Test
	public void executeSparqlQueryNoEngineWithString() throws Exception {
		// Prepare
		injectQueryEngine(null);

		// Execute
		assertThrows(NoQueryEngineException.class, () -> graphAccess.executeSparqlQuery("", new SimpleGraph()));
	}

	@Test
	public void executeSparqlQueryNoEngineWithQuery() throws Exception {
		// Prepare
		injectQueryEngine(null);

		// Execute
		assertThrows(
				NoQueryEngineException.class,
				() -> graphAccess.executeSparqlQuery((Query) null, new SimpleGraph())
		);
	}

	@Test
	public void executeSparqlQueryNoEngineWithSelectQuery() throws Exception {
		// Prepare
		injectQueryEngine(null);

		// Execute
		assertThrows(
				NoQueryEngineException.class,
				() -> graphAccess.executeSparqlQuery((SelectQuery) null, new SimpleGraph())
		);
	}

	@Test
	public void executeSparqlQueryNoEngineWithAskQuery() throws Exception {
		// Prepare
		injectQueryEngine(null);

		// Execute
		assertThrows(
				NoQueryEngineException.class,
				() -> graphAccess.executeSparqlQuery((AskQuery) null, new SimpleGraph())
		);
	}

	@Test
	public void executeSparqlQueryNoEngineWithDescribeQuery() throws Exception {
		// Prepare
		injectQueryEngine(null);

		// Execute
		assertThrows(
				NoQueryEngineException.class,
				() -> graphAccess.executeSparqlQuery((DescribeQuery) null, new SimpleGraph())
		);
	}

	@Test
	public void executeSparqlQueryNoEngineWithConstructQuery() throws Exception {
		// Prepare
		injectQueryEngine(null);

		// Execute
		assertThrows(
				NoQueryEngineException.class,
				() -> graphAccess.executeSparqlQuery((ConstructQuery) null, new SimpleGraph())
		);
	}

	@Test
	public void executeSparqlQueryWithEngineWithString() throws Exception {
		// Prepare
		injectQueryEngine(queryEngine);
		Graph Graph = new SimpleGraph();

		// Execute
		graphAccess.executeSparqlQuery("", Graph);

		// Verify
		Mockito.verify(queryEngine).execute(graphAccess, Graph, "");
//		Mockito.verify(queryEngine, Mockito.never()).execute(
//				(TcManager) Mockito.anyObject(),
//				(Graph) Mockito.anyObject(),
//				Mockito.anyString());
	}

	@Test
	public void executeSparqlQueryWithEngineWithSelectQuery() throws Exception {
		// Prepare
		injectQueryEngine(queryEngine);
		Graph Graph = new SimpleGraph();
		SelectQuery query = Mockito.mock(SelectQuery.class);

		// Execute
		graphAccess.executeSparqlQuery(query, Graph);

		// Verify
		Mockito.verify(queryEngine).execute(graphAccess, Graph,
				query.toString());
//		Mockito.verify(queryEngine, Mockito.never()).execute(
//				(TcManager) Mockito.anyObject(),
//				(Graph) Mockito.anyObject(), Mockito.anyString());
	}

	@Test
	public void executeSparqlQueryWithEngineWithAskQuery() throws Exception {
		// Prepare
		injectQueryEngine(queryEngine);
		Graph Graph = new SimpleGraph();
		AskQuery query = Mockito.mock(AskQuery.class);

		Mockito.when(
				queryEngine.execute((TcManager) Mockito.anyObject(),
						(Graph) Mockito.anyObject(),
						Mockito.anyString())).thenReturn(Boolean.TRUE);

		// Execute
		graphAccess.executeSparqlQuery(query, Graph);

		// Verify
		Mockito.verify(queryEngine).execute(graphAccess, Graph,
				query.toString());
//		Mockito.verify(queryEngine, Mockito.never()).execute(
//				(TcManager) Mockito.anyObject(),
//				(Graph) Mockito.anyObject(), Mockito.anyString());
	}

	@Test
	public void executeSparqlQueryWithEngineWithDescribeQuery()
			throws Exception {
		// Prepare
		injectQueryEngine(queryEngine);
		Graph Graph = new SimpleGraph();
		DescribeQuery query = Mockito.mock(DescribeQuery.class);

		// Execute
		graphAccess.executeSparqlQuery(query, Graph);

		// Verify
		Mockito.verify(queryEngine).execute(graphAccess, Graph,
				query.toString());
//		Mockito.verify(queryEngine, Mockito.never()).execute(
//				(TcManager) Mockito.anyObject(),
//				(Graph) Mockito.anyObject(), Mockito.anyString());
	}

	@Test
	public void executeSparqlQueryWithEngineWithConstructQuery()
			throws Exception {
		// Prepare
		injectQueryEngine(queryEngine);
		Graph Graph = new SimpleGraph();
		ConstructQuery query = Mockito.mock(ConstructQuery.class);

		// Execute
		graphAccess.executeSparqlQuery(query, Graph);

		// Verify
		Mockito.verify(queryEngine).execute(graphAccess, Graph,
				query.toString());
//		Mockito.verify(queryEngine, Mockito.never()).execute(
//				(TcManager) Mockito.anyObject(),
//				(Graph) Mockito.anyObject(), Mockito.anyString());
	}

	// ------------------------------------------------------------------------
	// Implementing QueryableTcProvider
	// ------------------------------------------------------------------------

	private void injectQueryEngine(QueryEngine engine)
			throws NoSuchFieldException, IllegalAccessException {
		Field queryEngineField = TcManager.class
				.getDeclaredField("queryEngine");
		queryEngineField.setAccessible(true);
		queryEngineField.set(graphAccess, engine);
	}
}
