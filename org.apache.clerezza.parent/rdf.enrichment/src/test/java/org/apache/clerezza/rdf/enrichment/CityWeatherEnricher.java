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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.utils.IteratorMerger;

/**
 * add a a weather property pointing to the weather expressed a a bnode with
 * current temperature and humidity (fake values)
 *
 * @author reto
 */
public class CityWeatherEnricher extends Enricher {

	//ontology part
	static UriRef classCity = new UriRef("http://example.org/ontology#City");
	static UriRef weatherProperty = new UriRef("http://example.org/ontology#weather");
	static UriRef temperatureProperty = new UriRef("http://example.org/ontology#temperature");
	static UriRef humidityProperty = new UriRef("http://example.org/ontology#humidity");
	final private static UriRef xsdDouble =
			new UriRef("http://www.w3.org/2001/XMLSchema#double");
	private final ResourceFilter classCityFilter = getFilterForSubjectsWith(RDF.type, classCity);
	private final Map<NonLiteral, WeakReference<BNode>> cityWeatherMap = new HashMap<NonLiteral, WeakReference<BNode>>();

	/*
	 * we might be able to tell more about things that are a City and about
	 * the Weather-Bnode we created
	 */
	@Override
	public ResourceFilter getSubjectFilter() {
		return new OrConnector(
				classCityFilter, getLocalBNodeFilter());
	}

	@Override
	public ResourceFilter getPredicateFilter() {
		return getExtensionalFilter(weatherProperty, temperatureProperty, humidityProperty);
	}

	@Override
	public ResourceFilter getObjectFilter() {
		return new OrConnector(getLocalBNodeFilter(), getDataTypeFilter(xsdDouble));
	}

	@Override
	public Iterator<Triple> filter(final NonLiteral subject,
			final UriRef predicate, final Resource object,
			final TripleCollection base) {
		if (subject == null) {

			Iterator<Triple> iteratorCitySubjects =  new IteratorMerger(
					new Iterator<Iterator<Triple>>() {

						Iterator<Resource> subjectIterator =
								classCityFilter.getAcceptable(base).iterator();

						@Override
						public boolean hasNext() {
							return subjectIterator.hasNext();
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException("Not supported.");
						}

						@Override
						public Iterator<Triple> next() {
							return CityWeatherEnricher.this.filterSubjectNonNull(
									(NonLiteral)subjectIterator.next(),
									predicate, object, base);
						}
					});
			//TODO add with bnode subjects
			return iteratorCitySubjects;
		} else {
			return filterSubjectNonNull(subject, predicate, object, base);
		}
		
	}

	private Iterator<Triple> filterSubjectNonNull(final NonLiteral subject,
			final UriRef predicate, final Resource object,
			final TripleCollection base) {
		Set<Triple> resultSet = new HashSet<Triple>();
		if (classCityFilter.accept(subject, base)) {
			if (predicate.equals(weatherProperty)) {
				BNode weatherNode = getWeatherNode(subject);
				resultSet.add(new TripleImpl(subject, predicate, weatherNode));
			}
		} else {
			NonLiteral city = null;
			Collection<NonLiteral> obsoleteCities = new ArrayList<NonLiteral>();
			for (Map.Entry<NonLiteral, WeakReference<BNode>> entry : cityWeatherMap.entrySet()) {
				BNode node = entry.getValue().get();
				if (node == null) {
					obsoleteCities.add(entry.getKey());
				}
				if (node.equals(subject)) {
					city = entry.getKey();
					break;
				}
			}
			for (NonLiteral nonLiteral : obsoleteCities) {
				cityWeatherMap.remove(nonLiteral);
			}
			if (predicate.equals(temperatureProperty) || (predicate == null)) {
				resultSet.add(new TripleImpl(subject,
						temperatureProperty, LiteralFactory.getInstance().createTypedLiteral((double)city.toString().length())));
			}
			if (predicate.equals(humidityProperty) || (predicate == null)) {
				resultSet.add(new TripleImpl(subject,
						humidityProperty, LiteralFactory.getInstance().createTypedLiteral(0.51)));
			}
		}
		return resultSet.iterator();
	}

	/**
	 * for every city the is one weather node
	 * 
	 * @param subject
	 * @return
	 */
	private synchronized BNode getWeatherNode(NonLiteral city) {
		WeakReference<BNode> nodeRef =  cityWeatherMap.get(city);
		if (nodeRef != null) {
			BNode node = nodeRef.get();
			if (node != null) {
				return node;
			}
		}
		BNode newNode =  createLocalBNode();
		cityWeatherMap.put(city, new WeakReference<BNode>(newNode));
		return newNode;
	}

	@Override
	public int providedTriplesCount(TripleCollection base) {
		int count = 0;
		for (Iterator<Triple> bas = base.filter(null, RDF.type, classCity); bas.hasNext();) {
			bas.next();
			count++;
		}
		return count * 4;
	}
}


