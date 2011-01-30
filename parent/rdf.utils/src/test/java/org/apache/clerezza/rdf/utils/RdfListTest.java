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

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import static org.junit.Assert.*;

/**
 *
 * @author rbn
 */
public class RdfListTest {

	@Test
	public void listCreationAndAccess() {
		TripleCollection tc = new SimpleMGraph();
		List<Resource> list = new RdfList(new UriRef("http://example.org/mytest"), tc);
		assertEquals(0, list.size());
		list.add(new PlainLiteralImpl("hello"));
		list.add(new PlainLiteralImpl("world"));
		assertEquals(new PlainLiteralImpl("hello"), list.get(0));
		assertEquals(new PlainLiteralImpl("world"), list.get(1));
		assertEquals(2, list.size());
		list.add(new PlainLiteralImpl("welcome"));
		assertEquals(3, list.size());
		assertEquals(new PlainLiteralImpl("welcome"), list.get(2));
		list.add(1, new PlainLiteralImpl("interesting"));
		assertEquals(4, list.size());
		assertEquals(new PlainLiteralImpl("interesting"), list.get(1));
		assertEquals(new PlainLiteralImpl("world"), list.get(2));
		assertEquals(new PlainLiteralImpl("welcome"), list.get(3));
		list.add(0, new PlainLiteralImpl("start"));
		assertEquals(5, list.size());
		assertEquals(new PlainLiteralImpl("hello"), list.get(1));
		assertEquals(new PlainLiteralImpl("interesting"), list.get(2));
		List<Resource> list2 = new RdfList(new UriRef("http://example.org/mytest"), tc);
		assertEquals(5, list2.size());
		assertEquals(new PlainLiteralImpl("hello"), list2.get(1));
		assertEquals(new PlainLiteralImpl("interesting"), list2.get(2));
		list2.remove(2);
		assertEquals(4, list2.size());
		assertEquals(new PlainLiteralImpl("hello"), list2.get(1));
		assertEquals(new PlainLiteralImpl("world"), list2.get(2));
		while (list2.size() > 0) {
			list2.remove(0);
		}
		assertEquals(1, tc.size()); //list = rdf:nil statement
		list2.add(0, new PlainLiteralImpl("restart"));
		list2.add(1, new PlainLiteralImpl("over"));
		assertEquals(2, list2.size());
		list2.add(new PlainLiteralImpl("2"));
		list2.add(new PlainLiteralImpl("3"));
		assertEquals(4, list2.size());
		list2.add(new PlainLiteralImpl("4"));
		list2.add(new PlainLiteralImpl("5"));
		assertEquals(new PlainLiteralImpl("3"), list2.get(3));
	}

    @Test
	public void listCreationAndAccess2() {
		TripleCollection tc = new SimpleMGraph();
		List<Resource> list = new RdfList(new UriRef("http://example.org/mytest"), tc);
		assertEquals(0, list.size());
		list.add(0,new PlainLiteralImpl("world"));
        list = new RdfList(new UriRef("http://example.org/mytest"), tc);
        list.add(0,new PlainLiteralImpl("beautifuly"));
        list = new RdfList(new UriRef("http://example.org/mytest"), tc);
        list.add(0,new PlainLiteralImpl("hello"));
        assertEquals(new PlainLiteralImpl("hello"), list.get(0));
        assertEquals(new PlainLiteralImpl("beautifuly"), list.get(1));
        assertEquals(new PlainLiteralImpl("world"), list.get(2));
    }

    @Test
	public void listCreationAndAccess3() {
		TripleCollection tc = new SimpleMGraph();
		List<Resource> list = new RdfList(new UriRef("http://example.org/mytest"), tc);
		assertEquals(0, list.size());
        BNode node0 = new BNode() {};
        BNode node1 = new BNode() {};
        BNode node2 = new BNode() {};
		list.add(0,node2);
        list.add(0,node1);
        list.add(0,node0);
        assertEquals(node0, list.get(0));
        assertEquals(node1, list.get(1));
        assertEquals(node2, list.get(2));
    }

	@Test
	public void secondButLastElementAccessTest() {
		TripleCollection tc = new SimpleMGraph();
		List<Resource> list = new RdfList(new UriRef("http://example.org/mytest2"), tc);
		list.add(new PlainLiteralImpl("hello"));
		list.add(new PlainLiteralImpl("world"));
		list.remove(1);
		assertEquals(1, list.size());
	}

	@Test
	public void cleanGraphAfterRemoval() {
		TripleCollection tc = new SimpleMGraph();
		List<Resource> list = new RdfList(new UriRef("http://example.org/mytest"), tc);
		list.add(new PlainLiteralImpl("hello"));
		list.add(new PlainLiteralImpl("world"));
		list.remove(1);
		Assert.assertEquals(2, tc.size());

	}

	@Test
	public void findContainingListNodesAndfindContainingListsTest() {
		TripleCollection tc = new SimpleMGraph();
		GraphNode listA = new GraphNode(new UriRef("http:///listA"), tc);
		GraphNode listB = new GraphNode(new UriRef("http:///listB"), tc);
		BNode element1 = new BNode();
		BNode element2 = new BNode();
		BNode element3 = new BNode();
		BNode element4 = new BNode();
		BNode element5 = new BNode();

		RdfList rdfListA = new RdfList(listA);
		rdfListA.add(element1);
		rdfListA.add(element2);
		rdfListA.add(element3);
		rdfListA.add(element4);

		RdfList rdfListB = new RdfList(listB);
		rdfListB.add(element2);
		rdfListB.add(element4);
		rdfListB.add(element5);

		Set<GraphNode> containingListNodes = RdfList.findContainingListNodes(
				new GraphNode(element3, tc));
		Assert.assertEquals(1, containingListNodes.size());
		Assert.assertTrue(containingListNodes.contains(listA));

		Set<RdfList> containingLists = RdfList.findContainingLists(
				new GraphNode(element3, tc));
		Assert.assertEquals(1, containingLists.size());
		Assert.assertTrue(containingLists.contains(rdfListA));

		containingListNodes = RdfList.findContainingListNodes(
				new GraphNode(element4, tc));
		Assert.assertEquals(2, containingListNodes.size());
		Assert.assertTrue(containingListNodes.contains(listA));
		Assert.assertTrue(containingListNodes.contains(listB));

		containingLists = RdfList.findContainingLists(
				new GraphNode(element4, tc));
		Assert.assertEquals(2, containingLists.size());
		Assert.assertTrue(containingLists.contains(rdfListA));
		Assert.assertTrue(containingLists.contains(rdfListB));
	}
}