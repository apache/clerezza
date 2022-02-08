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

import org.apache.clerezza.BlankNode;
import org.apache.clerezza.Graph;
import org.apache.clerezza.IRI;
import org.apache.clerezza.RDFTerm;
import org.apache.clerezza.implementation.in_memory.SimpleGraph;
import org.apache.clerezza.implementation.literal.PlainLiteralImpl;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author rbn
 */
@RunWith(JUnitPlatform.class)
public class RdfListTest {

    @Test
    public void listCreationAndAccess() {
        Graph tc = new SimpleGraph();
        List<RDFTerm> list = new RdfList(new IRI("http://example.org/mytest"), tc);
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
        List<RDFTerm> list2 = new RdfList(new IRI("http://example.org/mytest"), tc);
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
        Graph tc = new SimpleGraph();
        List<RDFTerm> list = new RdfList(new IRI("http://example.org/mytest"), tc);
        assertEquals(0, list.size());
        list.add(0, new PlainLiteralImpl("world"));
        list = new RdfList(new IRI("http://example.org/mytest"), tc);
        list.add(0, new PlainLiteralImpl("beautifuly"));
        list = new RdfList(new IRI("http://example.org/mytest"), tc);
        list.add(0, new PlainLiteralImpl("hello"));
        assertEquals(new PlainLiteralImpl("hello"), list.get(0));
        assertEquals(new PlainLiteralImpl("beautifuly"), list.get(1));
        assertEquals(new PlainLiteralImpl("world"), list.get(2));
    }

    @Test
    public void listCreationAndAccess3() {
        Graph tc = new SimpleGraph();
        List<RDFTerm> list = new RdfList(new IRI("http://example.org/mytest"), tc);
        assertEquals(0, list.size());
        BlankNode node0 = new BlankNode() {
        };
        BlankNode node1 = new BlankNode() {
        };
        BlankNode node2 = new BlankNode() {
        };
        list.add(0, node2);
        list.add(0, node1);
        list.add(0, node0);
        assertEquals(node0, list.get(0));
        assertEquals(node1, list.get(1));
        assertEquals(node2, list.get(2));
    }

    @Test
    public void secondButLastElementAccessTest() {
        Graph tc = new SimpleGraph();
        List<RDFTerm> list = new RdfList(new IRI("http://example.org/mytest2"), tc);
        list.add(new PlainLiteralImpl("hello"));
        list.add(new PlainLiteralImpl("world"));
        list.remove(1);
        assertEquals(1, list.size());
    }

    @Test
    public void cleanGraphAfterRemoval() {
        Graph tc = new SimpleGraph();
        List<RDFTerm> list = new RdfList(new IRI("http://example.org/mytest"), tc);
        list.add(new PlainLiteralImpl("hello"));
        list.add(new PlainLiteralImpl("world"));
        list.remove(1);
        assertEquals(2, tc.size());

    }

    @Test
    public void findContainingListNodesAndfindContainingListsTest() {
        Graph tc = new SimpleGraph();
        GraphNode listA = new GraphNode(new IRI("http:///listA"), tc);
        GraphNode listB = new GraphNode(new IRI("http:///listB"), tc);
        BlankNode element1 = new BlankNode();
        BlankNode element2 = new BlankNode();
        BlankNode element3 = new BlankNode();
        BlankNode element4 = new BlankNode();
        BlankNode element5 = new BlankNode();

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
        assertEquals(1, containingListNodes.size());
        assertTrue(containingListNodes.contains(listA));

        Set<RdfList> containingLists = RdfList.findContainingLists(
                new GraphNode(element3, tc));
        assertEquals(1, containingLists.size());
        assertTrue(containingLists.contains(rdfListA));

        containingListNodes = RdfList.findContainingListNodes(
                new GraphNode(element4, tc));
        assertEquals(2, containingListNodes.size());
        assertTrue(containingListNodes.contains(listA));
        assertTrue(containingListNodes.contains(listB));

        containingLists = RdfList.findContainingLists(
                new GraphNode(element4, tc));
        assertEquals(2, containingLists.size());
        assertTrue(containingLists.contains(rdfListA));
        assertTrue(containingLists.contains(rdfListB));
    }
}
