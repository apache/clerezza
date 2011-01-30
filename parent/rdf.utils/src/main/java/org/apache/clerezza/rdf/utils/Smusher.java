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

import java.util.Collection;
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
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 * A utility to equate duplicate nodes in an Mgarph, currently only nodes with 
 * a shared ifp are equated.
 *
 * @author reto
 */
public class Smusher {

	/**
	 * smush mGaph given the ontological facts. Currently it does only
	 * one step ifp smushin, i.e. only ifps are taken in account and only
	 * nodes that have the same node as ifp object in the orignal graph are
	 * equates. (calling the method a second time might lead to additional
	 * smushings.)
	 *
	 * @param mGraph
	 * @param tBox
	 */
	public static void smush(MGraph mGraph, TripleCollection tBox) {
		final Set<UriRef> ifps = getIfps(tBox);
		final Map<PredicateObject, Set<NonLiteral>> ifp2nodesMap = new HashMap<PredicateObject, Set<NonLiteral>>();
		for (Iterator<Triple> it = mGraph.iterator(); it.hasNext();) {
			final Triple triple = it.next();
			final UriRef predicate = triple.getPredicate();
			if (!ifps.contains(predicate)) {
				continue;
			}
			final PredicateObject po = new PredicateObject(predicate, triple.getObject());
			Set<NonLiteral> equivalentNodes = ifp2nodesMap.get(po);
			if (equivalentNodes == null) {
				equivalentNodes = new HashSet<NonLiteral>();
				ifp2nodesMap.put(po, equivalentNodes);
			}
			equivalentNodes.add(triple.getSubject());
		}
		Set<Set<NonLiteral>> unitedEquivalenceSets = uniteSetsWithCommonElement(ifp2nodesMap.values());
		Map<NonLiteral, NonLiteral> current2ReplacementMap = new HashMap<NonLiteral, NonLiteral>();
		final MGraph owlSameAsGraph = new SimpleMGraph();
		for (Set<NonLiteral> equivalenceSet : unitedEquivalenceSets) {
			final NonLiteral replacement = getReplacementFor(equivalenceSet, owlSameAsGraph);
			for (NonLiteral current : equivalenceSet) {
				if (!current.equals(replacement)) {
					current2ReplacementMap.put(current, replacement);
				}
			}
		}
		final Set<Triple> newTriples = new HashSet<Triple>();
		for (Iterator<Triple> it = mGraph.iterator(); it.hasNext();) {
			final Triple triple = it.next();
			Triple replacementTriple = null;
			final NonLiteral subject = triple.getSubject();
			NonLiteral subjectReplacement =
					current2ReplacementMap.get(subject);
			final Resource object = triple.getObject();
			@SuppressWarnings("element-type-mismatch")
			Resource objectReplacement = current2ReplacementMap.get(object);
			if ((subjectReplacement != null) || (objectReplacement != null)) {
				it.remove();
				if (subjectReplacement == null) {
					subjectReplacement = subject;
				}
				if (objectReplacement == null) {
					objectReplacement = object;
				}
				newTriples.add(new TripleImpl(subjectReplacement,
						triple.getPredicate(), objectReplacement));
			}
		}
		for (Triple triple : newTriples) {
			mGraph.add(triple);
		}
		mGraph.addAll(owlSameAsGraph);
	}

	private static Set<UriRef> getIfps(TripleCollection tBox) {
		final Iterator<Triple> ifpDefinitions = tBox.filter(null, RDF.type,
				OWL.InverseFunctionalProperty);
		final Set<UriRef> ifps = new HashSet<UriRef>();
		while (ifpDefinitions.hasNext()) {
			final Triple triple = ifpDefinitions.next();
			ifps.add((UriRef) triple.getSubject());
		}
		return ifps;
	}

	private static NonLiteral getReplacementFor(Set<NonLiteral> equivalenceSet, 
			MGraph owlSameAsGraph) {
		final Set<UriRef> uriRefs = new HashSet<UriRef>();
		for (NonLiteral nonLiteral : equivalenceSet) {
			if (nonLiteral instanceof UriRef) {
				uriRefs.add((UriRef) nonLiteral);
			}
		}
		switch (uriRefs.size()) {
			case 1:
				return uriRefs.iterator().next();
			case 0:
				return new BNode();
		}
		final Iterator<UriRef> uriRefIter = uriRefs.iterator();
		//instead of an arbitrary one we might either decide lexicographically
		//or look at their frequency in mGraph
		final UriRef first = uriRefIter.next();
		while (uriRefIter.hasNext()) {
			UriRef uriRef = uriRefIter.next();
			owlSameAsGraph.add(new TripleImpl(uriRef, OWL.sameAs, first));
		}
		return first;
	}

	private static <T> Set<Set<T>> uniteSetsWithCommonElement(
			Collection<Set<T>> originalSets) {
		Set<Set<T>> result = new HashSet<Set<T>>();
		Iterator<Set<T>> iter = originalSets.iterator();
		while (iter.hasNext()) {
			Set<T> originalSet = iter.next();
			Set<T> matchingSet = getMatchinSet(originalSet, result);
			if (matchingSet != null) {
				matchingSet.addAll(originalSet);
			} else {
				result.add(new HashSet<T>(originalSet));
			}
		}
		if (result.size() < originalSets.size()) {
			return uniteSetsWithCommonElement(result);
		} else {
			return result;
		}
	}

	private static <T> Set<T> getMatchinSet(Set<T> set, Set<Set<T>> setOfSet) {
		for (Set<T> current : setOfSet) {
			if (shareElements(set,current)) {
				return current;
			}
		}
		return null;
	}

	private static <T> boolean shareElements(Set<T> set1, Set<T> set2) {
		for (T elem : set2) {
			if (set1.contains(elem)) {
				return true;
			}
		}
		return false;
	}
	

	static class PredicateObject {

		final UriRef predicate;
		final Resource object;

		public PredicateObject(UriRef predicate, Resource object) {
			this.predicate = predicate;
			this.object = object;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final PredicateObject other = (PredicateObject) obj;
			if (this.predicate != other.predicate && !this.predicate.equals(other.predicate)) {
				return false;
			}
			if (this.object != other.object && !this.object.equals(other.object)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 29 * hash + this.predicate.hashCode();
			hash = 13 * hash + this.object.hashCode();
			return hash;
		}

		@Override
		public String toString() {
			return "("+predicate+", "+object+")";
		}


	};
}
