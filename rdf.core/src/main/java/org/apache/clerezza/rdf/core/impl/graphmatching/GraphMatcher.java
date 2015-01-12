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



import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.rdf.BlankNode;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.BlankNodeOrIri;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.RdfTerm;
import org.apache.commons.rdf.Triple;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author reto
 * 
 */
public class GraphMatcher {


    private final static Logger log = LoggerFactory.getLogger(GraphMatcher.class);

    /**
     * get a mapping from g1 to g2 or null if the graphs are not isomorphic. The
     * returned map maps each <code>BNode</code>s from g1 to one
     * of g2. If the graphs are ground graphs the method return an empty map if
     * the ImmutableGraph are equals and null otherwise.
     * <p/>
     * NOTE: This method does not returned mapping from blank nodes to grounded
     * nodes, a bnode in g1 is not a vraiable that may match any node, but must
     * match a bnode in g2.
     * <p/>
     *
     * On the algorithm:<br/>
     * - In a first step it checked if every grounded triple in g1 matches one
     * in g2<br/>
     * - [optional] blank node blind matching</br>
     * - in a map mbng1 bnode of g1 is mapped to a set of of its
     * properties and inverse properties, this is the predicate and the object
     * or subject respectively, analoguosly in mbgn2 every bnode of g2<br/>
     * - based on the incoming and outgoing properties a hash is calculated for
     * each bnode, in the first step when calculating the hash  aconstant value
     * is taken for the bnodes that might be subject or object in the (inverse properties)
     * - hash-classes:
     * 
     * @param g1
     * @param g2
     * @return a Set of NodePairs
     */
    public static Map<BlankNode, BlankNode> getValidMapping(Graph og1, Graph og2) {
        Graph g1 = new SimpleMGraph(og1);
        Graph g2 = new SimpleMGraph(og2);
        if (!Utils.removeGrounded(g1,g2)) {
            return null;
        }
        final HashMatching hashMatching;
        try {
            hashMatching = new HashMatching(g1, g2);
        } catch (GraphNotIsomorphicException ex) {
            return null;
        }
        Map<BlankNode, BlankNode> matchings = hashMatching.getMatchings();
        if (g1.size() > 0) {
            //start trial an error matching
            //TODO (CLEREZZA-81) at least in the situation where one matching
            //group is big (approx > 5) we should switch back to hash-based matching
            //after a first guessed matching, rather than try all permutations
            Map<BlankNode, BlankNode> remainingMappings = trialAndErrorMatching(g1, g2, hashMatching.getMatchingGroups());
            if (remainingMappings == null) {
                return null;
            } else {
                matchings.putAll(remainingMappings);
            }
        }
        return matchings;
    }

    private static Map<BlankNode, BlankNode> trialAndErrorMatching(Graph g1, Graph g2,
            Map<Set<BlankNode>, Set<BlankNode>> matchingGroups) {
        if (log.isDebugEnabled()) {
            Set<BlankNode> bn1  = Utils.getBNodes(g1);
            log.debug("doing trial and error matching for {} bnodes, " +
                    "in graphs of size: {}.", bn1.size(), g1.size());
        }
        Iterator<Map<BlankNode, BlankNode>> mappingIter
                = GroupMappingIterator.create(matchingGroups);
        while (mappingIter.hasNext()) {
            Map<BlankNode, BlankNode> map = mappingIter.next();
            if (checkMapping(g1, g2, map)) {
                return map;
            }
        }
        return null;
    }

    private static boolean checkMapping(Graph g1, Graph g2, Map<BlankNode, BlankNode> map) {
        for (Triple triple : g1) {
            if (!g2.contains(map(triple, map))) {
                return false;
            }
        }
        return true;
    }

    private static Triple map(Triple triple, Map<BlankNode, BlankNode> map) {
        final BlankNodeOrIri oSubject = triple.getSubject();

        BlankNodeOrIri subject = oSubject instanceof BlankNode ?
            map.get((BlankNode)oSubject) : oSubject;

        RdfTerm oObject = triple.getObject();
        RdfTerm object = oObject instanceof BlankNode ?
            map.get((BlankNode)oObject) : oObject;
        return new TripleImpl(subject, triple.getPredicate(), object);
    }


}
