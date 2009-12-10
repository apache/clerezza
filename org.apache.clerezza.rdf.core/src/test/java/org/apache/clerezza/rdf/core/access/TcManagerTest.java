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
package org.apache.clerezza.rdf.core.access;

import java.util.Iterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.providers.WeightedA;
import org.apache.clerezza.rdf.core.access.providers.WeightedA1;
import org.apache.clerezza.rdf.core.access.providers.WeightedAHeavy;
import org.apache.clerezza.rdf.core.access.providers.WeightedBlight;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import static org.junit.Assert.*;

/**
 *
 * @author reto
 */
public class TcManagerTest {

	public static UriRef uriRefAHeavy = new UriRef("http://example.org/aHeavy");
	public static UriRef uriRefB = new UriRef("http://example.org/b");
	;
	public static final UriRef uriRefA = new UriRef("http://example.org/a");
	public static final UriRef uriRefA1 = new UriRef("http://example.org/a1");
	private TcManager graphAccess;
	private final WeightedA weightedA = new WeightedA();
	private final WeightedA1 weightedA1 = new WeightedA1();
	private WeightedTcProvider weightedBlight = new WeightedBlight();

	@Before
	public void setUp() {
		graphAccess = TcManager.getInstance();
		graphAccess.bindWeightedTcProvider(weightedA);
		graphAccess.bindWeightedTcProvider(weightedA1);
		graphAccess.bindWeightedTcProvider(weightedBlight);
	}

	@After
	public void tearDown() {
		graphAccess = TcManager.getInstance();
		graphAccess.unbindWeightedTcProvider(weightedA);
		graphAccess.unbindWeightedTcProvider(weightedA1);
		graphAccess.unbindWeightedTcProvider(weightedBlight);
	}

	@Test
	public void getGraphFromA() {
		Graph graphA = graphAccess.getGraph(uriRefA);
		Iterator<Triple> iterator = graphA.iterator();
		assertEquals(new TripleImpl(uriRefA, uriRefA, uriRefA), iterator.next());
		assertFalse(iterator.hasNext());
		TripleCollection triplesA = graphAccess.getTriples(uriRefA);
		iterator = triplesA.iterator();
		assertEquals(new TripleImpl(uriRefA, uriRefA, uriRefA), iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void getGraphFromB() {
		Graph graphA = graphAccess.getGraph(uriRefB);
		Iterator<Triple> iterator = graphA.iterator();
		assertEquals(new TripleImpl(uriRefB, uriRefB, uriRefB), iterator.next());
		assertFalse(iterator.hasNext());
		TripleCollection triplesA = graphAccess.getTriples(uriRefB);
		iterator = triplesA.iterator();
		assertEquals(new TripleImpl(uriRefB, uriRefB, uriRefB), iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void getGraphFromAAfterUnbinding() {
		graphAccess.unbindWeightedTcProvider(weightedA);
		Graph graphA = graphAccess.getGraph(uriRefA);
		Iterator<Triple> iterator = graphA.iterator();
		assertEquals(new TripleImpl(uriRefA1, uriRefA1, uriRefA1), iterator.next());
		assertFalse(iterator.hasNext());
		TripleCollection triplesA = graphAccess.getTriples(uriRefA);
		iterator = triplesA.iterator();
		assertEquals(new TripleImpl(uriRefA1, uriRefA1, uriRefA1), iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void getGraphFromAWithHeavy() {
		final WeightedAHeavy weightedAHeavy = new WeightedAHeavy();
		graphAccess.bindWeightedTcProvider(weightedAHeavy);
		Graph graphA = graphAccess.getGraph(uriRefA);
		Iterator<Triple> iterator = graphA.iterator();
		assertEquals(new TripleImpl(uriRefAHeavy, uriRefAHeavy, uriRefAHeavy), iterator.next());
		assertFalse(iterator.hasNext());
		TripleCollection triplesA = graphAccess.getTriples(uriRefA);
		iterator = triplesA.iterator();
		assertEquals(new TripleImpl(uriRefAHeavy, uriRefAHeavy, uriRefAHeavy), iterator.next());
		assertFalse(iterator.hasNext());
		graphAccess.unbindWeightedTcProvider(weightedAHeavy);
	}
}