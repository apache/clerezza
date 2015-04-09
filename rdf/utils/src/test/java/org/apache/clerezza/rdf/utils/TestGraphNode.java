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
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.Literal;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.RdfTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Iri;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.core.test.RandomGraph;

/**
 *
 * @author reto, mir
 */
public class TestGraphNode {

    @Test
    public void nodeContext() {
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {};
        BlankNode bNode2 = new BlankNode() {};
        Iri property1 = new Iri("http://example.org/property1");
        Iri property2 = new Iri("http://example.org/property2");
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
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {};
        BlankNode bNode2 = new BlankNode() {};
        Iri property1 = new Iri("http://example.org/property1");
        GraphNode n = new GraphNode(bNode1, g);
        n.addProperty(property1, bNode2);
        Assert.assertEquals(1, g.size());
    }

    @Test
    public void testGetSubjectAndObjectNodes() {
        RandomGraph graph = new RandomGraph(500, 20, new SimpleGraph());
        for (int j = 0; j < 200; j++) {
            Triple randomTriple = graph.getRandomTriple();
            GraphNode node = new GraphNode(randomTriple.getSubject(), graph);
            Iterator<Iri> properties = node.getProperties();
            while (properties.hasNext()) {
                Iri property = properties.next();
                Set<RdfTerm> objects = createSet(node.getObjects(property));
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
            Iterator<Iri> properties = node.getProperties();
            while (properties.hasNext()) {
                Iri property = properties.next();
                Set<RdfTerm> subjects = createSet(node.getSubjects(property));
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
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {};
        BlankNode bNode2 = new BlankNode() {};
        Iri property1 = new Iri("http://example.org/property1");
        Iri property2 = new Iri("http://example.org/property2");
        Iri property3 = new Iri("http://example.org/property3");
        Iri property4 = new Iri("http://example.org/property4");
        ArrayList<Iri> props = new ArrayList<Iri>();
        props.add(property1);
        props.add(property2);
        props.add(property3);
        props.add(property4);
        GraphNode n = new GraphNode(bNode1, g);
        n.addProperty(property1, bNode2);
        n.addProperty(property2, bNode2);
        n.addProperty(property3, bNode2);
        n.addProperty(property4, bNode2);
        Iterator<Iri> properties = n.getProperties();
        int i = 0;
        while(properties.hasNext()){
            i++;
            Iri prop = properties.next();
            Assert.assertTrue(props.contains(prop));
            props.remove(prop);
        }
        Assert.assertEquals(i, 4);
        Assert.assertEquals(props.size(), 0);

    }

    @Test
    public void deleteAll() {
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {};
        BlankNode bNode2 = new BlankNode() {};
        Iri property1 = new Iri("http://example.org/property1");
        Iri property2 = new Iri("http://example.org/property2");
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
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {};
        BlankNode bNode2 = new BlankNode() {};
        Iri property1 = new Iri("http://example.org/property1");
        Iri property2 = new Iri("http://example.org/property2");
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
        Graph initialGraph = new SimpleGraph();
        BlankNode bNode1 = new BlankNode();
        BlankNode bNode2 = new BlankNode();
        BlankNode newBnode = new BlankNode();
        Iri property1 = new Iri("http://example.org/property1");
        Iri property2 = new Iri("http://example.org/property2");
        Iri newIri = new Iri("http://example.org/newName");
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
                new SimpleGraph(initialGraph.iterator()));

        node.replaceWith(newIri, true);
        Assert.assertEquals(5, node.getGraph().size());
        Triple expectedTriple1 = new TripleImpl(bNode1, newIri, literal1);
        Triple expectedTriple2 = new TripleImpl(bNode1, property2, newIri);
        Triple expectedTriple3 = new TripleImpl(newIri, newIri, bNode2);
        Triple expectedTriple4 = new TripleImpl(newIri, newIri, literal2);

        Assert.assertTrue(node.getGraph().contains(expectedTriple1));
        Assert.assertTrue(node.getGraph().contains(expectedTriple2));
        Assert.assertTrue(node.getGraph().contains(expectedTriple3));
        Assert.assertTrue(node.getGraph().contains(expectedTriple4));

        Assert.assertFalse(node.getGraph().contains(triple1));
        Assert.assertFalse(node.getGraph().contains(triple2));
        Assert.assertFalse(node.getGraph().contains(triple4));
        Assert.assertFalse(node.getGraph().contains(triple5));

        node = new GraphNode(property1, new SimpleGraph(initialGraph.iterator()));
        node.replaceWith(newBnode);
        Triple expectedTriple5 = new TripleImpl(bNode1, property2, newBnode);
        Triple expectedTriple6 = new TripleImpl(newBnode, property1, bNode2);
        Triple expectedTriple7 = new TripleImpl(newBnode, property1, literal2);

        Assert.assertTrue(node.getGraph().contains(triple1));
        Assert.assertTrue(node.getGraph().contains(expectedTriple5));
        Assert.assertTrue(node.getGraph().contains(expectedTriple6));
        Assert.assertTrue(node.getGraph().contains(expectedTriple7));

        node = new GraphNode(literal1, new SimpleGraph(initialGraph.iterator()));
        node.replaceWith(newBnode);
        Triple expectedTriple8 = new TripleImpl(bNode1, property1, newBnode);
        Assert.assertTrue(node.getGraph().contains(expectedTriple8));

        node = new GraphNode(property1, new SimpleGraph(initialGraph.iterator()));
        node.replaceWith(newIri);
        Triple expectedTriple9 = new TripleImpl(bNode1, property2, newIri);
        Triple expectedTriple10 = new TripleImpl(newIri, property1, bNode2);
        Triple expectedTriple11 = new TripleImpl(newIri, property1, literal2);
        Assert.assertTrue(node.getGraph().contains(triple1));
        Assert.assertTrue(node.getGraph().contains(expectedTriple9));
        Assert.assertTrue(node.getGraph().contains(expectedTriple10));
        Assert.assertTrue(node.getGraph().contains(expectedTriple11));
    }

    @Test
    public void equality() {
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {};
        BlankNode bNode2 = new BlankNode() {};
        Iri property1 = new Iri("http://example.org/property1");
        GraphNode n = new GraphNode(bNode1, g);
        n.addProperty(property1, bNode2);
        Assert.assertTrue(n.equals(new GraphNode(bNode1, g)));
        Assert.assertFalse(n.equals(new GraphNode(bNode2, g)));
        GraphNode n2 = null;
        Assert.assertFalse(n.equals(n2));
    }

    private Set<RdfTerm> createSet(Iterator<? extends RdfTerm> resources) {
        Set<RdfTerm> set = new HashSet<RdfTerm>();
        while (resources.hasNext()) {
            RdfTerm resource = resources.next();
            set.add(resource);
        }
        return set;
    }

}
