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
import org.apache.clerezza.implementation.TripleImpl;
import org.apache.clerezza.implementation.in_memory.SimpleGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author reto
 */
@RunWith(JUnitPlatform.class)
public class GraphUtilsTest {

    final IRI u1 = new IRI("http://ex.org/1");
    final IRI u2 = new IRI("http://ex.org/2");
    final IRI u3 = new IRI("http://ex.org/3");

    @Test
    public void removeSubGraph() throws GraphUtils.NoSuchSubGraphException {
        Graph baseGraph = createBaseGraph();

        Graph subGraph = new SimpleGraph();
        {
            BlankNode bNode1 = new BlankNode();
            BlankNode bNode2 = new BlankNode();
            subGraph.add(new TripleImpl(u1, u2, bNode2));
            subGraph.add(new TripleImpl(bNode2, u2, bNode2));
            subGraph.add(new TripleImpl(bNode2, u2, bNode1));
        }
        GraphUtils.removeSubGraph(baseGraph, subGraph);
        Assertions.assertEquals(1, baseGraph.size());
    }

    private Graph createBaseGraph() {
        Graph baseGraph = new SimpleGraph();
        {
            BlankNode bNode1 = new BlankNode();
            BlankNode bNode2 = new BlankNode();
            baseGraph.add(new TripleImpl(u1, u2, bNode2));
            baseGraph.add(new TripleImpl(bNode2, u2, bNode2));
            baseGraph.add(new TripleImpl(bNode2, u2, bNode1));
            baseGraph.add(new TripleImpl(u3, u2, u1));
        }
        return baseGraph;
    }

    /**
     * It is required that the subgraph comprises the whole context of the BNodes it includes
     *
     * @throws org.apache.clerezza.utils.GraphUtils.NoSuchSubGraphException
     */
    @Test
    public void removeIncompleteSubGraph() throws GraphUtils.NoSuchSubGraphException {
        Graph baseGraph = createBaseGraph();

        Graph subGraph = new SimpleGraph();
        {
            BlankNode bNode1 = new BlankNode();
            BlankNode bNode2 = new BlankNode();
            subGraph.add(new TripleImpl(u1, u2, bNode2));
            subGraph.add(new TripleImpl(bNode2, u2, bNode2));
        }
        Assertions.assertThrows(
                GraphUtils.NoSuchSubGraphException.class,
                () -> GraphUtils.removeSubGraph(baseGraph, subGraph)
        );
    }

    @Test
    public void removeInvalidSubGraph() throws GraphUtils.NoSuchSubGraphException {
        Graph baseGraph = createBaseGraph();

        Graph subGraph = new SimpleGraph();
        {
            BlankNode bNode1 = new BlankNode();
            BlankNode bNode2 = new BlankNode();
            subGraph.add(new TripleImpl(u1, u2, bNode2));
            subGraph.add(new TripleImpl(bNode2, u2, bNode2));
            baseGraph.add(new TripleImpl(bNode2, u2, bNode1));
            baseGraph.add(new TripleImpl(bNode2, u2, new BlankNode()));
        }
        Assertions.assertThrows(
                GraphUtils.NoSuchSubGraphException.class,
                () -> GraphUtils.removeSubGraph(baseGraph, subGraph)
        );
    }
}
