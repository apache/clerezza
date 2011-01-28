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
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.ontologies.RDFS;

/**
 * Expands a GraphNode expanding SeeAlso-References of the node.
 *
 * @author reto
 */
public class SeeAlsoExpander {
	/**
	 * using TcManger instead of TcProvider as this ensures LockableMGraphs
	 */
	private final TcManager tcManager;
	public SeeAlsoExpander(TcManager tcManager) {
		this.tcManager = tcManager;

	}

	/**
	 * expands a node dereferencing its rdfs:seeAlso references using the
	 * tcManager associated to this instance. If the added TripleCollections
	 * also associate rdfs:seeAlso properties to node this are expanded till
	 * the maximum recursion depth specified.
	 *
	 * @param node the node to be expanded
	 * @param recursion the maximum recursion depth
	 * @return a new GraphNode over the union of the original and all expansion graphs
	 */
	public GraphNode expand(GraphNode node, int recursion) {
		Set<UriRef> alreadyVisited = new HashSet();
		Set<TripleCollection> resultTripleCollections = new HashSet<TripleCollection>();
		resultTripleCollections.add(node.getGraph());
		for (UriRef uriRef : expand(node, alreadyVisited, recursion)) {
			try {
				resultTripleCollections.add(tcManager.getTriples(uriRef));
			} catch (NoSuchEntityException e) {
				//ignore
			}
		}
		return new GraphNode(node.getNode(),
				new UnionMGraph(resultTripleCollections.toArray(
				new TripleCollection[resultTripleCollections.size()])));

	}

	private Set<UriRef> getSeeAlsoObjectUris(GraphNode node) {
		Set<UriRef> result = new HashSet<UriRef>();
		Lock l = node.readLock();
		l.lock();
		try {
			Iterator<Resource> objects = node.getObjects(RDFS.seeAlso);
			while (objects.hasNext()) {
				Resource next = objects.next();
				if (next instanceof UriRef) {
					result.add((UriRef)next);
				}
			}
		} finally {
			l.unlock();
		}
		return result;
	}

	private Set<UriRef> expand(GraphNode node, Set<UriRef> alreadyVisited, int recursion) {
		Set<UriRef> rdfSeeAlsoTargets = getSeeAlsoObjectUris(node);
		Set<UriRef> result = new HashSet<UriRef>();
		result.addAll(rdfSeeAlsoTargets);
		recursion++;
		if (recursion > 0) {
			rdfSeeAlsoTargets.removeAll(alreadyVisited);
			alreadyVisited.addAll(rdfSeeAlsoTargets);
			for (UriRef target : rdfSeeAlsoTargets) {
				try {
					result.addAll(expand(new GraphNode(node.getNode(),
						tcManager.getTriples(target)), alreadyVisited, recursion));
				} catch (NoSuchEntityException e) {
					//ignore
				}
			}
		}
		return result;
	}

}
