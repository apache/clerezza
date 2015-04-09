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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.BlankNodeOrIri;
import org.apache.clerezza.commons.rdf.RdfTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;

/**
 * Utility methods to manipulate <code>Graph</code>s
 *
 * @author reto
 */
public class GraphUtils {

    /**
     * Removes a subGraph from an Graph. The subGraph must match a subgraph of
     * Graph so that for every node in <code>subGraph</code>
     * each triple it appears in is also present in <code>mGraph</code>. Two
     * bnodes are considered equals if their contexts (as returned by
     * <code>GraphNode.getNodeContext</code> are equals.
     *
     * @param mGraph
     * @param subGraph
     * @throws org.apache.clerezza.rdf.utils.GraphUtils.NoSuchSubGraphException
     */
    public static void removeSubGraph(Graph mGraph, Graph subGraph)
            throws NoSuchSubGraphException {
        //point to triples of mGraph that are to be removed (if something is removed)
        final Set<Triple> removingTriples = new HashSet<Triple>();
        //we first check only the grounded triples and put the non-grounded in here:
        final Graph unGroundedTriples = new SimpleGraph();
        for (Triple triple : subGraph) {
            if (isGrounded(triple)) {
                if (!mGraph.contains(triple)) {
                    throw new NoSuchSubGraphException();
                }
                removingTriples.add(triple);
            } else {
                unGroundedTriples.add(triple);
            }
        }

        //we first remove the context of bnodes we find in object position
        OBJ_BNODE_LOOP: while (true) {
            final Triple triple = getTripleWithBlankNodeObject(unGroundedTriples);
            if (triple == null) {
                break;
            }
            final GraphNode objectGN = new GraphNode(triple.getObject(), unGroundedTriples);
            BlankNodeOrIri subject = triple.getSubject();
            ImmutableGraph context = objectGN.getNodeContext();
            Iterator<Triple> potentialIter = mGraph.filter(subject, triple.getPredicate(), null);
            while (potentialIter.hasNext()) {
                try {
                    final Triple potentialTriple = potentialIter.next();
                    BlankNode potentialMatch = (BlankNode)potentialTriple.getObject();
                    final ImmutableGraph potentialContext = new GraphNode(potentialMatch, mGraph).getNodeContext();
                    if (potentialContext.equals(context)) {
                        removingTriples.addAll(potentialContext);
                        unGroundedTriples.removeAll(context);
                        continue OBJ_BNODE_LOOP;
                    }
                } catch (ClassCastException e) {
                    continue;
                }
            }
            throw new NoSuchSubGraphException();
        }
        SUBJ_BNODE_LOOP: while (true) {
            final Triple triple = getTripleWithBlankNodeSubject(unGroundedTriples);
            if (triple == null) {
                break;
            }
            final GraphNode subjectGN = new GraphNode(triple.getSubject(), unGroundedTriples);
            RdfTerm object = triple.getObject();
            if (object instanceof BlankNode) {
                object = null;
            }
            ImmutableGraph context = subjectGN.getNodeContext();
            Iterator<Triple> potentialIter = mGraph.filter(null, triple.getPredicate(), object);
            while (potentialIter.hasNext()) {
                try {
                    final Triple potentialTriple = potentialIter.next();
                    BlankNode potentialMatch = (BlankNode)potentialTriple.getSubject();
                    final ImmutableGraph potentialContext = new GraphNode(potentialMatch, mGraph).getNodeContext();
                    if (potentialContext.equals(context)) {
                        removingTriples.addAll(potentialContext);
                        unGroundedTriples.removeAll(context);
                        continue SUBJ_BNODE_LOOP;
                    }
                } catch (ClassCastException e) {
                    continue;
                }
            }
            throw new NoSuchSubGraphException();
        }
        mGraph.removeAll(removingTriples);
    }

    private static boolean isGrounded(Triple triple) {
        if (triple.getSubject() instanceof BlankNode) {
            return false;
        }
        if (triple.getObject() instanceof BlankNode) {
            return false;
        }
        return true;
    }

    /** retrun triples with a bnode only at object position
     *
     * @param triples
     * @return
     */
    private static Triple getTripleWithBlankNodeObject(Graph triples) {
        for (Triple triple : triples) {
            if (triple.getSubject() instanceof BlankNode) {
                continue;
            }
            if (triple.getObject() instanceof BlankNode) {
                return triple;
            }
        }
        return null;
    }
    private static Triple getTripleWithBlankNodeSubject(Graph triples) {
        for (Triple triple : triples) {
            if (triple.getSubject() instanceof BlankNode) {
                return triple;
            }
        }
        return null;
    }

    public static class NoSuchSubGraphException extends Exception {
    }

}
