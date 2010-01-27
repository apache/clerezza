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

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.utils.MGraphUtils.NoSuchSubGraphException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class MGraphUtilsTest {

	final UriRef u1 = new UriRef("http://ex.org/1");
	final UriRef u2 = new UriRef("http://ex.org/2");
	final UriRef u3 = new UriRef("http://ex.org/3");

	@Test
	public void removeSubGraph() throws NoSuchSubGraphException {
		MGraph baseGraph = createBaseGraph();

		MGraph subGraph = new SimpleMGraph();
		{
			BNode bNode1 = new BNode();
			BNode bNode2 = new BNode();
			subGraph.add(new TripleImpl(u1, u2, bNode2));
			subGraph.add(new TripleImpl(bNode2, u2, bNode2));
			subGraph.add(new TripleImpl(bNode2, u2, bNode1));
		}
		MGraphUtils.removeSubGraph(baseGraph, subGraph);
		Assert.assertEquals(1, baseGraph.size());
	}

	private MGraph createBaseGraph() {
		MGraph baseGraph = new SimpleMGraph();
		{
			BNode bNode1 = new BNode();
			BNode bNode2 = new BNode();
			baseGraph.add(new TripleImpl(u1, u2, bNode2));
			baseGraph.add(new TripleImpl(bNode2, u2, bNode2));
			baseGraph.add(new TripleImpl(bNode2, u2, bNode1));
			baseGraph.add(new TripleImpl(u3, u2, u1));
		}
		return baseGraph;
	}
	
	/** It is required that the subgraph comprises the whole context of the Bnodes it ioncludes
	 * 
	 * @throws org.apache.clerezza.rdf.utils.MGraphUtils.NoSuchSubGraphException
	 */
	@Test(expected=NoSuchSubGraphException.class)
	public void removeIncompleteSubGraph() throws NoSuchSubGraphException {
		MGraph baseGraph = createBaseGraph();

		MGraph subGraph = new SimpleMGraph();
		{
			BNode bNode1 = new BNode();
			BNode bNode2 = new BNode();
			subGraph.add(new TripleImpl(u1, u2, bNode2));
			subGraph.add(new TripleImpl(bNode2, u2, bNode2));
		}
		MGraphUtils.removeSubGraph(baseGraph, subGraph);
	}

	@Test(expected=NoSuchSubGraphException.class)
	public void removeInvalidSubGraph() throws NoSuchSubGraphException {
		MGraph baseGraph = createBaseGraph();

		MGraph subGraph = new SimpleMGraph();
		{
			BNode bNode1 = new BNode();
			BNode bNode2 = new BNode();
			subGraph.add(new TripleImpl(u1, u2, bNode2));
			subGraph.add(new TripleImpl(bNode2, u2, bNode2));
			baseGraph.add(new TripleImpl(bNode2, u2, bNode1));
			baseGraph.add(new TripleImpl(bNode2, u2, new BNode()));
		}
		MGraphUtils.removeSubGraph(baseGraph, subGraph);
	}
}

