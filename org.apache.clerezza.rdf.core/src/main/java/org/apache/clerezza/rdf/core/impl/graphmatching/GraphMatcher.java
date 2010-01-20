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
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
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
	 * the graph are equals and null otherwise.
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
	public static Map<BNode, BNode> getValidMapping(TripleCollection og1, TripleCollection og2) {
		MGraph g1 = new SimpleMGraph(og1);
		MGraph g2 = new SimpleMGraph(og2);
		if (!Utils.removeGrounded(g1,g2)) {
			return null;
		}
		final HashMatching hashMatching;
		try {
			hashMatching = new HashMatching(g1, g2);
		} catch (GraphNotIsomorphicException ex) {
			return null;
		}
		Map<BNode, BNode> matchings = hashMatching.getMatchings();
		if (g1.size() > 0) {
			//start trial an error matching
			//TODO (CLEREZZA-81) at least in the situation where one matching
			//group is big (approx > 5) we should switch back to hash-based matching
			//after a first guessed matching, rather than try all permutations
			Map<BNode, BNode> remainingMappings = trialAndErrorMatching(g1, g2, hashMatching.getMatchingGroups());
			if (remainingMappings == null) {
				return null;
			} else {
				matchings.putAll(remainingMappings);
			}
		}
		return matchings;
	}

	private static Map<BNode, BNode> trialAndErrorMatching(MGraph g1, MGraph g2,
			Map<Set<BNode>, Set<BNode>> matchingGroups) {
		if (log.isDebugEnabled()) {
			Set<BNode> bn1  = Utils.getBNodes(g1);
			log.debug("doing trial and error matching for {} bnodes, " +
					"in graphs of size: {}.", bn1.size(), g1.size());
		}
		Iterator<Map<BNode, BNode>> mappingIter
				= GroupMappingIterator.create(matchingGroups);
		while (mappingIter.hasNext()) {
			Map<BNode, BNode> map = mappingIter.next();
			if (checkMapping(g1, g2, map)) {
				return map;
			}
		}
		return null;
	}

	private static boolean checkMapping(MGraph g1, MGraph g2, Map<BNode, BNode> map) {
		for (Triple triple : g1) {
			if (!g2.contains(map(triple, map))) {
				return false;
			}
		}
		return true;
	}

	private static Triple map(Triple triple, Map<BNode, BNode> map) {
		final NonLiteral oSubject = triple.getSubject();

		NonLiteral subject = oSubject instanceof BNode ?
			map.get((BNode)oSubject) : oSubject;

		Resource oObject = triple.getObject();
		Resource object = oObject instanceof BNode ?
			map.get((BNode)oObject) : oObject;
		return new TripleImpl(subject, triple.getPredicate(), object);
	}


}
