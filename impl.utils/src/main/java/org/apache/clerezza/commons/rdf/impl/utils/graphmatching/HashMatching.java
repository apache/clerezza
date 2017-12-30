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

package org.apache.clerezza.commons.rdf.impl.utils.graphmatching;


import org.apache.clerezza.commons.rdf.impl.utils.graphmatching.collections.IntHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.commons.rdf.impl.utils.graphmatching.collections.IntIterator;

/**
 *
 * @author reto
 */
public class HashMatching {

    private Map<BlankNode, BlankNode> matchings = new HashMap<BlankNode, BlankNode>();
    private Map<Set<BlankNode>, Set<BlankNode>> matchingGroups;

    /**
     * tc1 and tc2 will be modified: the triples containing no unmatched bnode
     * will be removed
     *
     * @param tc1
     * @param tc2
     * @throws GraphNotIsomorphicException
     */
    HashMatching(Graph tc1, Graph tc2) throws GraphNotIsomorphicException {
        int foundMatchings = 0;
        int foundMatchingGroups = 0;
        Map<BlankNode, Integer> bNodeHashMap = new HashMap<BlankNode, Integer>();
        while (true) {
            bNodeHashMap = matchByHashes(tc1, tc2, bNodeHashMap);
            if (bNodeHashMap == null) {
                throw new GraphNotIsomorphicException();
            }
            if (matchings.size() == foundMatchings) {
                if (!(matchingGroups.size() > foundMatchingGroups)) {
                    break;
                }
            }
            foundMatchings = matchings.size();
            foundMatchingGroups = matchingGroups.size();
        }
    }

    /**
     *
     * @return a map containing set of which each bnodes mappes one of the other set
     */
    public Map<Set<BlankNode>, Set<BlankNode>> getMatchingGroups() {
        return matchingGroups;
    }

    public Map<BlankNode, BlankNode> getMatchings() {
        return matchings;
    }

    
    private static IntHashMap<Set<BlankNode>> getHashNodes(Map<BlankNode,
            Set<Property>> bNodePropMap, Map<BlankNode, Integer> bNodeHashMap) {
        IntHashMap<Set<BlankNode>> result = new IntHashMap<Set<BlankNode>>();
        for (Map.Entry<BlankNode, Set<Property>> entry : bNodePropMap.entrySet()) {
            int hash = computeHash(entry.getValue(), bNodeHashMap);
            Set<BlankNode> bNodeSet = result.get(hash);
            if (bNodeSet == null) {
                bNodeSet = new HashSet<BlankNode>();
                result.put(hash,bNodeSet);
            }
            bNodeSet.add(entry.getKey());
        }
        return result;
    }
    /*
     * returns a Map from bnodes to hash that can be used for future
     * refinements, this could be separate for each ImmutableGraph.
     *
     * triples no longer containing an unmatched bnodes ae removed.
     *
     * Note that the matched node are not guaranteed to be equals, but only to
     * be the correct if the graphs are isomorphic.
     */
    private Map<BlankNode, Integer> matchByHashes(Graph g1, Graph g2,
            Map<BlankNode, Integer> bNodeHashMap) {
        Map<BlankNode, Set<Property>> bNodePropMap1  = getBNodePropMap(g1);
        Map<BlankNode, Set<Property>> bNodePropMap2  = getBNodePropMap(g2);
        IntHashMap<Set<BlankNode>> hashNodeMap1 = getHashNodes(bNodePropMap1, bNodeHashMap);
        IntHashMap<Set<BlankNode>> hashNodeMap2 = getHashNodes(bNodePropMap2, bNodeHashMap);
        if (!hashNodeMap1.keySet().equals(hashNodeMap2.keySet())) {
            return null;
        }

        matchingGroups = new HashMap<Set<BlankNode>, Set<BlankNode>>();
        IntIterator hashIter = hashNodeMap1.keySet().intIterator();
        while (hashIter.hasNext()) {
            int hash = hashIter.next();
            Set<BlankNode> nodes1 = hashNodeMap1.get(hash);
            Set<BlankNode> nodes2 = hashNodeMap2.get(hash);
            if (nodes1.size() != nodes2.size()) {
                return null;
            }
            if (nodes1.size() != 1) {
                matchingGroups.put(nodes1, nodes2);
                continue;
            }
            final BlankNode bNode1 = nodes1.iterator().next();
            final BlankNode bNode2 = nodes2.iterator().next();
            matchings.put(bNode1,bNode2);
            //in the graphs replace node occurences with grounded node,
            BlankNodeOrIRI mappedNode = new MappedNode(bNode1, bNode2);
            replaceNode(g1,bNode1, mappedNode);
            replaceNode(g2, bNode2, mappedNode);
            //remove grounded triples
            if (!Utils.removeGrounded(g1,g2)) {
                return null;
            }
        }
        Map<BlankNode, Integer> result = new HashMap<BlankNode, Integer>();
        addInverted(result, hashNodeMap1);
        addInverted(result, hashNodeMap2);
        return result;
    }
    private static int computeHash(Set<Property> propertySet, Map<BlankNode, Integer> bNodeHashMap) {
        int result = 0;
        for (Property property : propertySet) {
            result += property.hashCode(bNodeHashMap);
        }
        return result;
    }
    private static Map<BlankNode, Set<Property>> getBNodePropMap(Graph g) {
        Set<BlankNode> bNodes = Utils.getBNodes(g);
        Map<BlankNode, Set<Property>> result = new HashMap<BlankNode, Set<Property>>();
        for (BlankNode bNode : bNodes) {
            result.put(bNode, getProperties(bNode, g));
        }
        return result;
    }
    private static Set<Property> getProperties(BlankNode bNode, Graph g) {
        Set<Property> result = new HashSet<Property>();
        Iterator<Triple> ti = g.filter(bNode, null, null);
        while (ti.hasNext()) {
            Triple triple = ti.next();
            result.add(new ForwardProperty(triple.getPredicate(), triple.getObject()));
        }
        ti = g.filter(null, null, bNode);
        while (ti.hasNext()) {
            Triple triple = ti.next();
            result.add(new BackwardProperty(triple.getSubject(), triple.getPredicate()));
        }
        return result;
    }
    private static int nodeHash(RDFTerm resource, Map<BlankNode, Integer> bNodeHashMap) {
        if (resource instanceof BlankNode) {
            Integer mapValue = bNodeHashMap.get((BlankNode)resource);
            if (mapValue == null) {
                return 0;
            } else {
                return mapValue;
            }
        } else {
            return resource.hashCode();
        }
    }
    private static void replaceNode(Graph graph, BlankNode bNode, BlankNodeOrIRI replacementNode) {
        Set<Triple> triplesToRemove = new HashSet<Triple>();
        for (Triple triple : graph) {
            Triple replacementTriple = getReplacement(triple, bNode, replacementNode);
            if (replacementTriple != null) {
                triplesToRemove.add(triple);
                graph.add(replacementTriple);
            }
        }
        graph.removeAll(triplesToRemove);
    }
    private static Triple getReplacement(Triple triple, BlankNode bNode, BlankNodeOrIRI replacementNode) {
        if (triple.getSubject().equals(bNode)) {
            if (triple.getObject().equals(bNode)) {
                return new TripleImpl(replacementNode, triple.getPredicate(), replacementNode);
            } else {
                return new TripleImpl(replacementNode, triple.getPredicate(), triple.getObject());
            }
        } else {
            if (triple.getObject().equals(bNode)) {
                return new TripleImpl(triple.getSubject(), triple.getPredicate(), replacementNode);
            } else {
                return null;
            }
        }
    }
    private static void addInverted(Map<BlankNode, Integer> result, IntHashMap<Set<BlankNode>> hashNodeMap) {
        for (int hash : hashNodeMap.keySet()) {
            Set<BlankNode> bNodes = hashNodeMap.get(hash);
            for (BlankNode bNode : bNodes) {
                result.put(bNode, hash);
            }
        }
    }
    
    private static class BackwardProperty implements Property {
        private BlankNodeOrIRI subject;
        private IRI predicate;
    
        public BackwardProperty(BlankNodeOrIRI subject, IRI predicate) {
            this.subject = subject;
            this.predicate = predicate;
        }
    
        @Override
        public int hashCode(Map<BlankNode, Integer> bNodeHashMap) {
            return  0xFF ^ predicate.hashCode() ^ nodeHash(subject, bNodeHashMap);
        }
    
    }
    private static class ForwardProperty implements Property {
        private IRI predicate;
        private RDFTerm object;
    
        public ForwardProperty(IRI predicate, RDFTerm object) {
            this.predicate = predicate;
            this.object = object;
        }
    
        @Override
        public int hashCode(Map<BlankNode, Integer> bNodeHashMap) {
            return predicate.hashCode() ^ nodeHash(object, bNodeHashMap);
        }
    }
    private static class MappedNode implements BlankNodeOrIRI {
        private BlankNode bNode1, bNode2;
    
        public MappedNode(BlankNode bNode1, BlankNode bNode2) {
            this.bNode1 = bNode1;
            this.bNode2 = bNode2;
        }
        
    }
    private static interface Property {
        public int hashCode(Map<BlankNode, Integer> bNodeHashMap);
    }
}
