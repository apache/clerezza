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
package org.apache.clerezza.rdf.core.impl.graphmatching;

import java.util.Map;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class GraphMatcherTest {

	final static UriRef u1 = new UriRef("http://example.org/u1");

	@Test
	public void testEmpty() {
		TripleCollection tc1 = new SimpleMGraph();
		TripleCollection tc2 = new SimpleMGraph();
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNotNull(mapping);
		Assert.assertEquals(0, mapping.size());
	}

	@Test
	public void test2() {
		TripleCollection tc1 = new SimpleMGraph();
		tc1.add(new TripleImpl(u1, u1, u1));
		TripleCollection tc2 = new SimpleMGraph();
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNull(mapping);
	}

	@Test
	public void test3() {
		TripleCollection tc1 = new SimpleMGraph();
		tc1.add(new TripleImpl(u1, u1, u1));
		TripleCollection tc2 = new SimpleMGraph();
		tc2.add(new TripleImpl(u1, u1, u1));
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNotNull(mapping);
		Assert.assertEquals(0, mapping.size());
	}

	@Test
	public void test4() {
		TripleCollection tc1 = new SimpleMGraph();
		tc1.add(new TripleImpl(u1, u1, new BNode()));
		TripleCollection tc2 = new SimpleMGraph();
		tc2.add(new TripleImpl(u1, u1, new BNode()));
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNotNull(mapping);
		Assert.assertEquals(1, mapping.size());
	}

	@Test
	public void test5() {
		TripleCollection tc1 = new SimpleMGraph();
		tc1.add(new TripleImpl(new BNode(), u1, new BNode()));
		TripleCollection tc2 = new SimpleMGraph();
		tc2.add(new TripleImpl(new BNode(), u1, new BNode()));
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNotNull(mapping);
		Assert.assertEquals(2, mapping.size());
	}

	@Test
	public void test6() {
		TripleCollection tc1 = new SimpleMGraph();
		final BNode b11 = new BNode();
		tc1.add(new TripleImpl(new BNode(), u1,b11));
		tc1.add(new TripleImpl(new BNode(), u1,b11));
		TripleCollection tc2 = new SimpleMGraph();
		tc2.add(new TripleImpl(new BNode(), u1, new BNode()));
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNull(mapping);
	}

	private MGraph generateCircle(int size) {
		return generateCircle(size, new BNode());
	}

	private MGraph generateCircle(int size, final NonLiteral firstNode) {
		if (size < 1) {
			throw new IllegalArgumentException();
		}
		MGraph result = new SimpleMGraph();
		NonLiteral lastNode = firstNode;
		for (int i = 0; i < (size-1); i++) {
			final BNode newNode = new BNode();
			result.add(new TripleImpl(lastNode, u1, newNode));
			lastNode = newNode;
		}
		result.add(new TripleImpl(lastNode, u1, firstNode));
		return result;
	}

	@Test
	public void test7() {
		TripleCollection tc1 = generateCircle(2);
		TripleCollection tc2 = generateCircle(2);
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNotNull(mapping);
		Assert.assertEquals(2, mapping.size());
	}

	@Test
	public void test8() {
		TripleCollection tc1 = generateCircle(5);
		TripleCollection tc2 = generateCircle(5);
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNotNull(mapping);
		Assert.assertEquals(5, mapping.size());
	}

	@Test
	public void test9() {
		NonLiteral crossing = new UriRef("http://example.org/");
		TripleCollection tc1 = generateCircle(2,crossing);
		tc1.addAll(generateCircle(3,crossing));
		TripleCollection tc2 = generateCircle(2,crossing);
		tc2.addAll(generateCircle(3,crossing));
		Assert.assertEquals(5, tc1.size());
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNotNull(mapping);
		//a circle of 2 with 1 bnode and one of 2 bnodes
		Assert.assertEquals(3, mapping.size());
	}

	@Test
	public void test10() {
		NonLiteral crossing1 = new BNode();
		TripleCollection tc1 = generateCircle(2,crossing1);
		tc1.addAll(generateCircle(3,crossing1));
		NonLiteral crossing2 = new BNode();
		TripleCollection tc2 = generateCircle(2,crossing2);
		tc2.addAll(generateCircle(3,crossing2));
		Assert.assertEquals(5, tc1.size());
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNotNull(mapping);
		//a circle of 2 and one of 3 with one common node
		Assert.assertEquals(4, mapping.size());
	}

	@Test
	public void test11() {
		NonLiteral crossing1 = new BNode();
		TripleCollection tc1 = generateCircle(2,crossing1);
		tc1.addAll(generateCircle(4,crossing1));
		NonLiteral crossing2 = new BNode();
		TripleCollection tc2 = generateCircle(3,crossing2);
		tc2.addAll(generateCircle(3,crossing2));
		Assert.assertEquals(6, tc1.size());
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNull(mapping);
	}

	@Test
	public void test12() {
		NonLiteral start1 = new BNode();
		TripleCollection tc1 = Utils4Testing.generateLine(4,start1);
		tc1.addAll(Utils4Testing.generateLine(5,start1));
		NonLiteral start2 = new BNode();
		TripleCollection tc2 = Utils4Testing.generateLine(5,start2);
		tc2.addAll(Utils4Testing.generateLine(4,start2));
		Assert.assertEquals(9, tc1.size());
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNotNull(mapping);
		Assert.assertEquals(10, mapping.size());
	}

	@Test
	public void test13() {
		NonLiteral start1 = new BNode();
		TripleCollection tc1 = Utils4Testing.generateLine(4,start1);
		tc1.addAll(Utils4Testing.generateLine(5,start1));
		NonLiteral start2 = new BNode();
		TripleCollection tc2 = Utils4Testing.generateLine(3,start2);
		tc2.addAll(Utils4Testing.generateLine(3,start2));
		Assert.assertEquals(9, tc1.size());
		final Map<BNode, BNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
		Assert.assertNull(mapping);
	}
}
