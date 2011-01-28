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


import org.apache.clerezza.rdf.core.impl.graphmatching.collections.IntHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.graphmatching.collections.IntIterator;

/**
 *
 * @author reto
 */
public class HashMatching {

	private Map<BNode, BNode> matchings = new HashMap<BNode, BNode>();
	private Map<Set<BNode>, Set<BNode>> matchingGroups;

	/**
	 * tc1 and tc2 will be modified: the triples containing no unmatched bnode
	 * will be removed
	 *
	 * @param tc1
	 * @param tc2
	 * @throws GraphNotIsomorphicException
	 */
	HashMatching(MGraph tc1, MGraph tc2) throws GraphNotIsomorphicException {
		int foundMatchings = 0;
		int foundMatchingGroups = 0;
		Map<BNode, Integer> bNodeHashMap = new HashMap<BNode, Integer>();
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
	public Map<Set<BNode>, Set<BNode>> getMatchingGroups() {
		return matchingGroups;
	}

	public Map<BNode, BNode> getMatchings() {
		return matchings;
	}

	
	private static IntHashMap<Set<BNode>> getHashNodes(Map<BNode,
			Set<Property>> bNodePropMap, Map<BNode, Integer> bNodeHashMap) {
		IntHashMap<Set<BNode>> result = new IntHashMap<Set<BNode>>();
		for (Map.Entry<BNode, Set<Property>> entry : bNodePropMap.entrySet()) {
			int hash = computeHash(entry.getValue(), bNodeHashMap);
			Set<BNode> bNodeSet = result.get(hash);
			if (bNodeSet == null) {
				bNodeSet = new HashSet<BNode>();
				result.put(hash,bNodeSet);
			}
			bNodeSet.add(entry.getKey());
		}
		return result;
	}
	/*
	 * returns a Map from bnodes to hash that can be used for future
	 * refinements, this could be separate for each graph.
	 *
	 * triples no longer containing an unmatched bnodes ae removed.
	 *
	 * Note that the matched node are not guaranteed to be equals, but only to
	 * be the correct if the graphs are isomorphic.
	 */
	private Map<BNode, Integer> matchByHashes(MGraph g1, MGraph g2,
			Map<BNode, Integer> bNodeHashMap) {
		Map<BNode, Set<Property>> bNodePropMap1  = getBNodePropMap(g1);
		Map<BNode, Set<Property>> bNodePropMap2  = getBNodePropMap(g2);
		IntHashMap<Set<BNode>> hashNodeMap1 = getHashNodes(bNodePropMap1, bNodeHashMap);
		IntHashMap<Set<BNode>> hashNodeMap2 = getHashNodes(bNodePropMap2, bNodeHashMap);
		if (!hashNodeMap1.keySet().equals(hashNodeMap2.keySet())) {
			return null;
		}

		matchingGroups = new HashMap<Set<BNode>, Set<BNode>>();
		IntIterator hashIter = hashNodeMap1.keySet().intIterator();
		while (hashIter.hasNext()) {
			int hash = hashIter.next();
			Set<BNode> nodes1 = hashNodeMap1.get(hash);
			Set<BNode> nodes2 = hashNodeMap2.get(hash);
			if (nodes1.size() != nodes2.size()) {
				return null;
			}
			if (nodes1.size() != 1) {
				matchingGroups.put(nodes1, nodes2);
				continue;
			}
			final BNode bNode1 = nodes1.iterator().next();
			final BNode bNode2 = nodes2.iterator().next();
			matchings.put(bNode1,bNode2);
			//in the graphs replace node occurences with grounded node,
			NonLiteral mappedNode = new MappedNode(bNode1, bNode2);
			replaceNode(g1,bNode1, mappedNode);
			replaceNode(g2, bNode2, mappedNode);
			//remove grounded triples
			if (!Utils.removeGrounded(g1,g2)) {
				return null;
			}
		}
		Map<BNode, Integer> result = new HashMap<BNode, Integer>();
		addInverted(result, hashNodeMap1);
		addInverted(result, hashNodeMap2);
		return result;
	}
	private static int computeHash(Set<Property> propertySet, Map<BNode, Integer> bNodeHashMap) {
		int result = 0;
		for (Property property : propertySet) {
			result += property.hashCode(bNodeHashMap);
		}
		return result;
	}
	private static Map<BNode, Set<Property>> getBNodePropMap(MGraph g) {
		Set<BNode> bNodes = Utils.getBNodes(g);
		Map<BNode, Set<Property>> result = new HashMap<BNode, Set<Property>>();
		for (BNode bNode : bNodes) {
			result.put(bNode, getProperties(bNode, g));
		}
		return result;
	}
	private static Set<Property> getProperties(BNode bNode, MGraph g) {
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
	private static int nodeHash(Resource resource, Map<BNode, Integer> bNodeHashMap) {
		if (resource instanceof BNode) {
			Integer mapValue = bNodeHashMap.get((BNode)resource);
			if (mapValue == null) {
				return 0;
			} else {
				return mapValue;
			}
		} else {
			return resource.hashCode();
		}
	}
	private static void replaceNode(MGraph mGraph, BNode bNode, NonLiteral replacementNode) {
		Set<Triple> triplesToRemove = new HashSet<Triple>();
		for (Triple triple : mGraph) {
			Triple replacementTriple = getReplacement(triple, bNode, replacementNode);
			if (replacementTriple != null) {
				triplesToRemove.add(triple);
				mGraph.add(replacementTriple);
			}
		}
		mGraph.removeAll(triplesToRemove);
	}
	private static Triple getReplacement(Triple triple, BNode bNode, NonLiteral replacementNode) {
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
	private static void addInverted(Map<BNode, Integer> result, IntHashMap<Set<BNode>> hashNodeMap) {
		for (int hash : hashNodeMap.keySet()) {
			Set<BNode> bNodes = hashNodeMap.get(hash);
			for (BNode bNode : bNodes) {
				result.put(bNode, hash);
			}
		}
	}
	
	private static class BackwardProperty implements Property {
		private NonLiteral subject;
		private UriRef predicate;
	
		public BackwardProperty(NonLiteral subject, UriRef predicate) {
			this.subject = subject;
			this.predicate = predicate;
		}
	
		@Override
		public int hashCode(Map<BNode, Integer> bNodeHashMap) {
			return  0xFF ^ predicate.hashCode() ^ nodeHash(subject, bNodeHashMap);
		}
	
	}
	private static class ForwardProperty implements Property {
		private UriRef predicate;
		private Resource object;
	
		public ForwardProperty(UriRef predicate, Resource object) {
			this.predicate = predicate;
			this.object = object;
		}
	
		@Override
		public int hashCode(Map<BNode, Integer> bNodeHashMap) {
			return predicate.hashCode() ^ nodeHash(object, bNodeHashMap);
		}
	}
	private static class MappedNode implements NonLiteral {
		private BNode bNode1, bNode2;
	
		public MappedNode(BNode bNode1, BNode bNode2) {
			this.bNode1 = bNode1;
			this.bNode2 = bNode2;
		}
		
	}
	private static interface Property {
		public int hashCode(Map<BNode, Integer> bNodeHashMap);
	}
}
