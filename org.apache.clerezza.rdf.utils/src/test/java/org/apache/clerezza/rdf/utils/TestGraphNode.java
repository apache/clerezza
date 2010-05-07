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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.test.RandomMGraph;

/**
 *
 * @author reto, mir
 */
public class TestGraphNode {

	@Test
	public void nodeContext() {
		MGraph g = new SimpleMGraph();
		BNode bNode1 = new BNode() {};
		BNode bNode2 = new BNode() {};
		UriRef property1 = new UriRef("http://example.org/property1");
		UriRef property2 = new UriRef("http://example.org/property2");
		g.add(new TripleImpl(bNode1, property1, new PlainLiteralImpl("literal")));
		g.add(new TripleImpl(bNode1, property2, property1));
		g.add(new TripleImpl(bNode2, property2, bNode1));
		g.add(new TripleImpl(property1, property1, bNode2));
		g.add(new TripleImpl(property1, property1, new PlainLiteralImpl("bla bla")));
		GraphNode n = new GraphNode(bNode1, g);
		Assert.assertEquals(4, n.getNodeContext().size());
		n.deleteNodeContext();
		Assert.assertEquals(1, g.size());
		Assert.assertFalse(n.getObjects(property2).hasNext());
	}

	@Test
	public void addNode() {
		MGraph g = new SimpleMGraph();
		BNode bNode1 = new BNode() {};
		BNode bNode2 = new BNode() {};
		UriRef property1 = new UriRef("http://example.org/property1");
		GraphNode n = new GraphNode(bNode1, g);
		n.addProperty(property1, bNode2);
		Assert.assertEquals(1, g.size());
	}

	@Test
	public void testGetSubjectAndObjectNodes() {
		RandomMGraph graph = new RandomMGraph(500, 20, new SimpleMGraph());
		for (int j = 0; j < 200; j++) {
			Triple randomTriple = graph.getRandomTriple();
			GraphNode node = new GraphNode(randomTriple.getSubject(), graph);
			Iterator<UriRef> properties = node.getProperties();
			while (properties.hasNext()) {
				UriRef property = properties.next();
				Set<Resource> objects = createSet(node.getObjects(property));
				Iterator<GraphNode> objectNodes = node.getObjectNodes(property);
				while (objectNodes.hasNext()) {
					GraphNode graphNode = objectNodes.next();
					Assert.assertTrue(objects.contains(graphNode.getNode()));
				}
			}
		}

		for (int j = 0; j < 200; j++) {
			Triple randomTriple = graph.getRandomTriple();
			GraphNode node = new GraphNode(randomTriple.getObject(), graph);
			Iterator<UriRef> properties = node.getProperties();
			while (properties.hasNext()) {
				UriRef property = properties.next();
				Set<Resource> subjects = createSet(node.getSubjects(property));
				Iterator<GraphNode> subjectNodes = node.getSubjectNodes(property);
				while (subjectNodes.hasNext()) {
					GraphNode graphNode = subjectNodes.next();
					Assert.assertTrue(subjects.contains(graphNode.getNode()));
				}
			}
		}
	}

	@Test
	public void getAvailableProperties(){
		MGraph g = new SimpleMGraph();
		BNode bNode1 = new BNode() {};
		BNode bNode2 = new BNode() {};
		UriRef property1 = new UriRef("http://example.org/property1");
		UriRef property2 = new UriRef("http://example.org/property2");
		UriRef property3 = new UriRef("http://example.org/property3");
		UriRef property4 = new UriRef("http://example.org/property4");
		ArrayList<UriRef> props = new ArrayList<UriRef>();
		props.add(property1);
		props.add(property2);
		props.add(property3);
		props.add(property4);
		GraphNode n = new GraphNode(bNode1, g);
		n.addProperty(property1, bNode2);
		n.addProperty(property2, bNode2);
		n.addProperty(property3, bNode2);
		n.addProperty(property4, bNode2);
		Iterator<UriRef> properties = n.getProperties();
		int i = 0;
		while(properties.hasNext()){
			i++;
			UriRef prop = properties.next();
			Assert.assertTrue(props.contains(prop));
			props.remove(prop);
		}
		Assert.assertEquals(i, 4);
		Assert.assertEquals(props.size(), 0);

	}

	@Test
	public void deleteAll() {
		MGraph g = new SimpleMGraph();
		BNode bNode1 = new BNode() {};
		BNode bNode2 = new BNode() {};
		UriRef property1 = new UriRef("http://example.org/property1");
		UriRef property2 = new UriRef("http://example.org/property2");
		//the two properties two be deleted
		g.add(new TripleImpl(bNode1, property1, new PlainLiteralImpl("literal")));
		g.add(new TripleImpl(bNode1, property1, new PlainLiteralImpl("bla bla")));
		//this 3 properties should stay
		g.add(new TripleImpl(bNode1, property2, property1));
		g.add(new TripleImpl(property1, property1, new PlainLiteralImpl("bla bla")));
		g.add(new TripleImpl(bNode2, property1, new PlainLiteralImpl("bla bla")));
		GraphNode n = new GraphNode(bNode1, g);
		n.deleteProperties(property1);
		Assert.assertEquals(3, g.size());
	}

	@Test
	public void deleteSingleProperty() {
		MGraph g = new SimpleMGraph();
		BNode bNode1 = new BNode() {};
		BNode bNode2 = new BNode() {};
		UriRef property1 = new UriRef("http://example.org/property1");
		UriRef property2 = new UriRef("http://example.org/property2");
		//the properties two be deleted
		g.add(new TripleImpl(bNode1, property1, new PlainLiteralImpl("literal")));
		//this 4 properties should stay
		g.add(new TripleImpl(bNode1, property1, new PlainLiteralImpl("bla bla")));
		g.add(new TripleImpl(bNode1, property2, property1));
		g.add(new TripleImpl(property1, property1, new PlainLiteralImpl("bla bla")));
		g.add(new TripleImpl(bNode2, property1, new PlainLiteralImpl("bla bla")));
		GraphNode n = new GraphNode(bNode1, g);
		n.deleteProperty(property1, new PlainLiteralImpl("literal"));
		Assert.assertEquals(4, g.size());
	}

	@Test
	public void replaceWith() {
		MGraph initialGraph = new SimpleMGraph();
		BNode bNode1 = new BNode();
		BNode bNode2 = new BNode();
		BNode newBnode = new BNode();
		UriRef property1 = new UriRef("http://example.org/property1");
		UriRef property2 = new UriRef("http://example.org/property2");
		UriRef newUriRef = new UriRef("http://example.org/newName");
		Literal literal1 = new PlainLiteralImpl("literal");
		Literal literal2 = new PlainLiteralImpl("bla bla");

		Triple triple1 = new TripleImpl(bNode1, property1, literal1);
		Triple triple2 = new TripleImpl(bNode1, property2, property1);
		Triple triple3 = new TripleImpl(bNode2, property2, bNode1);
		Triple triple4 = new TripleImpl(property1, property1, bNode2);
		Triple triple5 = new TripleImpl(property1, property1, literal2);
		initialGraph.add(triple1);
		initialGraph.add(triple2);
		initialGraph.add(triple3);
		initialGraph.add(triple4);
		initialGraph.add(triple5);
		GraphNode node = new GraphNode(property1,
				new SimpleMGraph(initialGraph.iterator()));

		node.replaceWith(newUriRef, true);
		Assert.assertEquals(5, node.getGraph().size());
		Triple expectedTriple1 = new TripleImpl(bNode1, newUriRef, literal1);
		Triple expectedTriple2 = new TripleImpl(bNode1, property2, newUriRef);
		Triple expectedTriple3 = new TripleImpl(newUriRef, newUriRef, bNode2);
		Triple expectedTriple4 = new TripleImpl(newUriRef, newUriRef, literal2);

		Assert.assertTrue(node.getGraph().contains(expectedTriple1));
		Assert.assertTrue(node.getGraph().contains(expectedTriple2));
		Assert.assertTrue(node.getGraph().contains(expectedTriple3));
		Assert.assertTrue(node.getGraph().contains(expectedTriple4));

		Assert.assertFalse(node.getGraph().contains(triple1));
		Assert.assertFalse(node.getGraph().contains(triple2));
		Assert.assertFalse(node.getGraph().contains(triple4));
		Assert.assertFalse(node.getGraph().contains(triple5));

		node = new GraphNode(property1, new SimpleMGraph(initialGraph.iterator()));
		node.replaceWith(newBnode);
		Triple expectedTriple5 = new TripleImpl(bNode1, property2, newBnode);
		Triple expectedTriple6 = new TripleImpl(newBnode, property1, bNode2);
		Triple expectedTriple7 = new TripleImpl(newBnode, property1, literal2);

		Assert.assertTrue(node.getGraph().contains(triple1));
		Assert.assertTrue(node.getGraph().contains(expectedTriple5));
		Assert.assertTrue(node.getGraph().contains(expectedTriple6));
		Assert.assertTrue(node.getGraph().contains(expectedTriple7));

		node = new GraphNode(literal1, new SimpleMGraph(initialGraph.iterator()));
		node.replaceWith(newBnode);
		Triple expectedTriple8 = new TripleImpl(bNode1, property1, newBnode);
		Assert.assertTrue(node.getGraph().contains(expectedTriple8));

		node = new GraphNode(property1, new SimpleMGraph(initialGraph.iterator()));
		node.replaceWith(newUriRef);
		Triple expectedTriple9 = new TripleImpl(bNode1, property2, newUriRef);
		Triple expectedTriple10 = new TripleImpl(newUriRef, property1, bNode2);
		Triple expectedTriple11 = new TripleImpl(newUriRef, property1, literal2);
		Assert.assertTrue(node.getGraph().contains(triple1));
		Assert.assertTrue(node.getGraph().contains(expectedTriple9));
		Assert.assertTrue(node.getGraph().contains(expectedTriple10));
		Assert.assertTrue(node.getGraph().contains(expectedTriple11));
	}

	@Test
	public void equality() {
		MGraph g = new SimpleMGraph();
		BNode bNode1 = new BNode() {};
		BNode bNode2 = new BNode() {};
		UriRef property1 = new UriRef("http://example.org/property1");
		GraphNode n = new GraphNode(bNode1, g);
		n.addProperty(property1, bNode2);
		Assert.assertTrue(n.equals(new GraphNode(bNode1, g)));
		Assert.assertFalse(n.equals(new GraphNode(bNode2, g)));
		GraphNode n2 = null;
		Assert.assertFalse(n.equals(n2));
	}

	private Set<Resource> createSet(Iterator<? extends Resource> resources) {
		Set<Resource> set = new HashSet<Resource>();
		while (resources.hasNext()) {
			Resource resource = resources.next();
			set.add(resource);
		}
		return set;
	}

}
