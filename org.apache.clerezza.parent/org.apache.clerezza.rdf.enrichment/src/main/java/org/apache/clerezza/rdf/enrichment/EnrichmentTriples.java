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

package org.apache.clerezza.rdf.enrichment;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.AbstractMGraph;
import org.apache.clerezza.utils.IteratorMerger;

/**
 * Given a base graph and a set of Enrichers this TripleCollection contains
 * the triples provided by the enrichers.
 *
 * @author reto
 */
public class EnrichmentTriples extends AbstractMGraph {

	private TripleCollection base;
	private final Collection<Enricher> enrichers;
	
	public EnrichmentTriples(TripleCollection base, Collection<Enricher> enrichers) {
		this.base = base;
		this.enrichers = enrichers;
	}

	@Override
	protected Iterator<Triple> performFilter(NonLiteral subject,
			UriRef predicate, Resource object) {
		Collection<Iterator<Triple>> iteratorCollection =
				new ArrayList<Iterator<Triple>>(enrichers.size());
		synchronized(enrichers) {
			for (Enricher enricher : enrichers) {
				if (((subject == null) || enricher.getSubjectFilter().accept(subject, base)) &&
					((predicate == null) || enricher.getPredicateFilter().accept(predicate, base)) &&
					((object == null) || enricher.getObjectFilter().accept(object, base))) {
					iteratorCollection.add(enricher.filter(subject, predicate, object, base));

				}
			}
		}
		return new IteratorMerger<Triple>(iteratorCollection.iterator());
	}

	@Override
	public int size() {
		int totalSize = 0;
		synchronized(enrichers) {
			for (Enricher enricher : enrichers) {
				totalSize += enricher.providedTriplesCount(base);
			}
		}
		return totalSize;
	}

}
