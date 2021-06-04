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
package org.apache.clerezza.implementation.graphmatching;

import org.apache.clerezza.BlankNode;
import org.apache.clerezza.BlankNodeOrIRI;
import org.apache.clerezza.Graph;
import org.apache.clerezza.IRI;
import org.apache.clerezza.implementation.TripleImpl;
import org.apache.clerezza.implementation.in_memory.SimpleGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Map;

/**
 * @author reto
 */
@RunWith(JUnitPlatform.class)
public class GraphMatcherTest {

    final static IRI u1 = new IRI("http://example.org/u1");

    @Test
    public void testEmpty() {
        Graph tc1 = new SimpleGraph();
        Graph tc2 = new SimpleGraph();
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNotNull(mapping);
        Assertions.assertEquals(0, mapping.size());
    }

    @Test
    public void test2() {
        Graph tc1 = new SimpleGraph();
        tc1.add(new TripleImpl(u1, u1, u1));
        Graph tc2 = new SimpleGraph();
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNull(mapping);
    }

    @Test
    public void test3() {
        Graph tc1 = new SimpleGraph();
        tc1.add(new TripleImpl(u1, u1, u1));
        Graph tc2 = new SimpleGraph();
        tc2.add(new TripleImpl(u1, u1, u1));
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNotNull(mapping);
        Assertions.assertEquals(0, mapping.size());
    }

    @Test
    public void test4() {
        Graph tc1 = new SimpleGraph();
        tc1.add(new TripleImpl(u1, u1, new BlankNode()));
        Graph tc2 = new SimpleGraph();
        tc2.add(new TripleImpl(u1, u1, new BlankNode()));
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNotNull(mapping);
        Assertions.assertEquals(1, mapping.size());
    }

    @Test
    public void test5() {
        Graph tc1 = new SimpleGraph();
        tc1.add(new TripleImpl(new BlankNode(), u1, new BlankNode()));
        Graph tc2 = new SimpleGraph();
        tc2.add(new TripleImpl(new BlankNode(), u1, new BlankNode()));
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNotNull(mapping);
        Assertions.assertEquals(2, mapping.size());
    }

    @Test
    public void test6() {
        Graph tc1 = new SimpleGraph();
        final BlankNode b11 = new BlankNode();
        tc1.add(new TripleImpl(new BlankNode(), u1, b11));
        tc1.add(new TripleImpl(new BlankNode(), u1, b11));
        Graph tc2 = new SimpleGraph();
        tc2.add(new TripleImpl(new BlankNode(), u1, new BlankNode()));
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNull(mapping);
    }

    private Graph generateCircle(int size) {
        return generateCircle(size, new BlankNode());
    }

    private Graph generateCircle(int size, final BlankNodeOrIRI firstNode) {
        if (size < 1) {
            throw new IllegalArgumentException();
        }
        Graph result = new SimpleGraph();
        BlankNodeOrIRI lastNode = firstNode;
        for (int i = 0; i < (size - 1); i++) {
            final BlankNode newNode = new BlankNode();
            result.add(new TripleImpl(lastNode, u1, newNode));
            lastNode = newNode;
        }
        result.add(new TripleImpl(lastNode, u1, firstNode));
        return result;
    }

    @Test
    public void test7() {
        Graph tc1 = generateCircle(2);
        Graph tc2 = generateCircle(2);
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNotNull(mapping);
        Assertions.assertEquals(2, mapping.size());
    }

    @Test
    public void test8() {
        Graph tc1 = generateCircle(5);
        Graph tc2 = generateCircle(5);
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNotNull(mapping);
        Assertions.assertEquals(5, mapping.size());
    }

    @Test
    public void test9() {
        BlankNodeOrIRI crossing = new IRI("http://example.org/");
        Graph tc1 = generateCircle(2, crossing);
        tc1.addAll(generateCircle(3, crossing));
        Graph tc2 = generateCircle(2, crossing);
        tc2.addAll(generateCircle(3, crossing));
        Assertions.assertEquals(5, tc1.size());
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNotNull(mapping);
        //a circle of 2 with 1 bnode and one of 2 bnodes
        Assertions.assertEquals(3, mapping.size());
    }

    @Test
    public void test10() {
        BlankNodeOrIRI crossing1 = new BlankNode();
        Graph tc1 = generateCircle(2, crossing1);
        tc1.addAll(generateCircle(3, crossing1));
        BlankNodeOrIRI crossing2 = new BlankNode();
        Graph tc2 = generateCircle(2, crossing2);
        tc2.addAll(generateCircle(3, crossing2));
        Assertions.assertEquals(5, tc1.size());
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNotNull(mapping);
        //a circle of 2 and one of 3 with one common node
        Assertions.assertEquals(4, mapping.size());
    }

    @Test
    public void test11() {
        BlankNodeOrIRI crossing1 = new BlankNode();
        Graph tc1 = generateCircle(2, crossing1);
        tc1.addAll(generateCircle(4, crossing1));
        BlankNodeOrIRI crossing2 = new BlankNode();
        Graph tc2 = generateCircle(3, crossing2);
        tc2.addAll(generateCircle(3, crossing2));
        Assertions.assertEquals(6, tc1.size());
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNull(mapping);
    }

    @Test
    public void test12() {
        BlankNodeOrIRI start1 = new BlankNode();
        Graph tc1 = Utils4Testing.generateLine(4, start1);
        tc1.addAll(Utils4Testing.generateLine(5, start1));
        BlankNodeOrIRI start2 = new BlankNode();
        Graph tc2 = Utils4Testing.generateLine(5, start2);
        tc2.addAll(Utils4Testing.generateLine(4, start2));
        Assertions.assertEquals(9, tc1.size());
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNotNull(mapping);
        Assertions.assertEquals(10, mapping.size());
    }

    @Test
    public void test13() {
        BlankNodeOrIRI start1 = new BlankNode();
        Graph tc1 = Utils4Testing.generateLine(4, start1);
        tc1.addAll(Utils4Testing.generateLine(5, start1));
        BlankNodeOrIRI start2 = new BlankNode();
        Graph tc2 = Utils4Testing.generateLine(3, start2);
        tc2.addAll(Utils4Testing.generateLine(3, start2));
        Assertions.assertEquals(9, tc1.size());
        final Map<BlankNode, BlankNode> mapping = GraphMatcher.getValidMapping(tc1, tc2);
        Assertions.assertNull(mapping);
    }
}
