/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */
package org.apache.clerezza.utils;

import org.apache.clerezza.*;
import org.apache.clerezza.implementation.TripleImpl;
import org.apache.clerezza.implementation.in_memory.SimpleGraph;
import org.apache.clerezza.implementation.literal.PlainLiteralImpl;
import org.apache.clerezza.test.utils.RandomGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author reto, mir
 */
@RunWith(JUnitPlatform.class)
public class TestGraphNode {

    @Test
    public void nodeContext() {
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {
        };
        BlankNode bNode2 = new BlankNode() {
        };
        IRI property1 = new IRI("http://example.org/property1");
        IRI property2 = new IRI("http://example.org/property2");
        g.add(new TripleImpl(bNode1, property1, new PlainLiteralImpl("literal")));
        g.add(new TripleImpl(bNode1, property2, property1));
        g.add(new TripleImpl(bNode2, property2, bNode1));
        g.add(new TripleImpl(property1, property1, bNode2));
        g.add(new TripleImpl(property1, property1, new PlainLiteralImpl("bla bla")));
        GraphNode n = new GraphNode(bNode1, g);
        Assertions.assertEquals(4, n.getNodeContext().size());
        n.deleteNodeContext();
        Assertions.assertEquals(1, g.size());
        Assertions.assertFalse(n.getObjects(property2).hasNext());
    }

    @Test
    public void addNode() {
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {
        };
        BlankNode bNode2 = new BlankNode() {
        };
        IRI property1 = new IRI("http://example.org/property1");
        GraphNode n = new GraphNode(bNode1, g);
        n.addProperty(property1, bNode2);
        Assertions.assertEquals(1, g.size());
    }

    @Test
    public void testGetSubjectAndObjectNodes() {
        RandomGraph graph = new RandomGraph(500, 20, new SimpleGraph());
        for (int j = 0; j < 200; j++) {
            Triple randomTriple = graph.getRandomTriple();
            GraphNode node = new GraphNode(randomTriple.getSubject(), graph);
            Iterator<IRI> properties = node.getProperties();
            while (properties.hasNext()) {
                IRI property = properties.next();
                Set<RDFTerm> objects = createSet(node.getObjects(property));
                Iterator<GraphNode> objectNodes = node.getObjectNodes(property);
                while (objectNodes.hasNext()) {
                    GraphNode graphNode = objectNodes.next();
                    Assertions.assertTrue(objects.contains(graphNode.getNode()));
                }
            }
        }

        for (int j = 0; j < 200; j++) {
            Triple randomTriple = graph.getRandomTriple();
            GraphNode node = new GraphNode(randomTriple.getObject(), graph);
            Iterator<IRI> properties = node.getProperties();
            while (properties.hasNext()) {
                IRI property = properties.next();
                Set<RDFTerm> subjects = createSet(node.getSubjects(property));
                Iterator<GraphNode> subjectNodes = node.getSubjectNodes(property);
                while (subjectNodes.hasNext()) {
                    GraphNode graphNode = subjectNodes.next();
                    Assertions.assertTrue(subjects.contains(graphNode.getNode()));
                }
            }
        }
    }

    @Test
    public void getAvailableProperties() {
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {
        };
        BlankNode bNode2 = new BlankNode() {
        };
        IRI property1 = new IRI("http://example.org/property1");
        IRI property2 = new IRI("http://example.org/property2");
        IRI property3 = new IRI("http://example.org/property3");
        IRI property4 = new IRI("http://example.org/property4");
        ArrayList<IRI> props = new ArrayList<IRI>();
        props.add(property1);
        props.add(property2);
        props.add(property3);
        props.add(property4);
        GraphNode n = new GraphNode(bNode1, g);
        n.addProperty(property1, bNode2);
        n.addProperty(property2, bNode2);
        n.addProperty(property3, bNode2);
        n.addProperty(property4, bNode2);
        Iterator<IRI> properties = n.getProperties();
        int i = 0;
        while (properties.hasNext()) {
            i++;
            IRI prop = properties.next();
            Assertions.assertTrue(props.contains(prop));
            props.remove(prop);
        }
        Assertions.assertEquals(i, 4);
        Assertions.assertEquals(props.size(), 0);

    }

    @Test
    public void deleteAll() {
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {
        };
        BlankNode bNode2 = new BlankNode() {
        };
        IRI property1 = new IRI("http://example.org/property1");
        IRI property2 = new IRI("http://example.org/property2");
        //the two properties two be deleted
        g.add(new TripleImpl(bNode1, property1, new PlainLiteralImpl("literal")));
        g.add(new TripleImpl(bNode1, property1, new PlainLiteralImpl("bla bla")));
        //this 3 properties should stay
        g.add(new TripleImpl(bNode1, property2, property1));
        g.add(new TripleImpl(property1, property1, new PlainLiteralImpl("bla bla")));
        g.add(new TripleImpl(bNode2, property1, new PlainLiteralImpl("bla bla")));
        GraphNode n = new GraphNode(bNode1, g);
        n.deleteProperties(property1);
        Assertions.assertEquals(3, g.size());
    }

    @Test
    public void deleteSingleProperty() {
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {
        };
        BlankNode bNode2 = new BlankNode() {
        };
        IRI property1 = new IRI("http://example.org/property1");
        IRI property2 = new IRI("http://example.org/property2");
        //the properties two be deleted
        g.add(new TripleImpl(bNode1, property1, new PlainLiteralImpl("literal")));
        //this 4 properties should stay
        g.add(new TripleImpl(bNode1, property1, new PlainLiteralImpl("bla bla")));
        g.add(new TripleImpl(bNode1, property2, property1));
        g.add(new TripleImpl(property1, property1, new PlainLiteralImpl("bla bla")));
        g.add(new TripleImpl(bNode2, property1, new PlainLiteralImpl("bla bla")));
        GraphNode n = new GraphNode(bNode1, g);
        n.deleteProperty(property1, new PlainLiteralImpl("literal"));
        Assertions.assertEquals(4, g.size());
    }

    @Test
    public void replaceWith() {
        Graph initialGraph = new SimpleGraph();
        BlankNode bNode1 = new BlankNode();
        BlankNode bNode2 = new BlankNode();
        BlankNode newBnode = new BlankNode();
        IRI property1 = new IRI("http://example.org/property1");
        IRI property2 = new IRI("http://example.org/property2");
        IRI newIRI = new IRI("http://example.org/newName");
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

        node.replaceWith(newIRI, true);
        Assertions.assertEquals(5, node.getGraph().size());
        Triple expectedTriple1 = new TripleImpl(bNode1, newIRI, literal1);
        Triple expectedTriple2 = new TripleImpl(bNode1, property2, newIRI);
        Triple expectedTriple3 = new TripleImpl(newIRI, newIRI, bNode2);
        Triple expectedTriple4 = new TripleImpl(newIRI, newIRI, literal2);

        Assertions.assertTrue(node.getGraph().contains(expectedTriple1));
        Assertions.assertTrue(node.getGraph().contains(expectedTriple2));
        Assertions.assertTrue(node.getGraph().contains(expectedTriple3));
        Assertions.assertTrue(node.getGraph().contains(expectedTriple4));

        Assertions.assertFalse(node.getGraph().contains(triple1));
        Assertions.assertFalse(node.getGraph().contains(triple2));
        Assertions.assertFalse(node.getGraph().contains(triple4));
        Assertions.assertFalse(node.getGraph().contains(triple5));

        node = new GraphNode(property1, new SimpleGraph(initialGraph.iterator()));
        node.replaceWith(newBnode);
        Triple expectedTriple5 = new TripleImpl(bNode1, property2, newBnode);
        Triple expectedTriple6 = new TripleImpl(newBnode, property1, bNode2);
        Triple expectedTriple7 = new TripleImpl(newBnode, property1, literal2);

        Assertions.assertTrue(node.getGraph().contains(triple1));
        Assertions.assertTrue(node.getGraph().contains(expectedTriple5));
        Assertions.assertTrue(node.getGraph().contains(expectedTriple6));
        Assertions.assertTrue(node.getGraph().contains(expectedTriple7));

        node = new GraphNode(literal1, new SimpleGraph(initialGraph.iterator()));
        node.replaceWith(newBnode);
        Triple expectedTriple8 = new TripleImpl(bNode1, property1, newBnode);
        Assertions.assertTrue(node.getGraph().contains(expectedTriple8));

        node = new GraphNode(property1, new SimpleGraph(initialGraph.iterator()));
        node.replaceWith(newIRI);
        Triple expectedTriple9 = new TripleImpl(bNode1, property2, newIRI);
        Triple expectedTriple10 = new TripleImpl(newIRI, property1, bNode2);
        Triple expectedTriple11 = new TripleImpl(newIRI, property1, literal2);
        Assertions.assertTrue(node.getGraph().contains(triple1));
        Assertions.assertTrue(node.getGraph().contains(expectedTriple9));
        Assertions.assertTrue(node.getGraph().contains(expectedTriple10));
        Assertions.assertTrue(node.getGraph().contains(expectedTriple11));
    }

    @Test
    public void equality() {
        Graph g = new SimpleGraph();
        BlankNode bNode1 = new BlankNode() {
        };
        BlankNode bNode2 = new BlankNode() {
        };
        IRI property1 = new IRI("http://example.org/property1");
        GraphNode n = new GraphNode(bNode1, g);
        n.addProperty(property1, bNode2);
        Assertions.assertTrue(n.equals(new GraphNode(bNode1, g)));
        Assertions.assertFalse(n.equals(new GraphNode(bNode2, g)));
        GraphNode n2 = null;
        Assertions.assertFalse(n.equals(n2));
    }

    private Set<RDFTerm> createSet(Iterator<? extends RDFTerm> resources) {
        Set<RDFTerm> set = new HashSet<RDFTerm>();
        while (resources.hasNext()) {
            RDFTerm resource = resources.next();
            set.add(resource);
        }
        return set;
    }
}
