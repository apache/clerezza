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

import java.util.Collections;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.*;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class EnrichmentTriplesTest {

	@Test
	public void siblingAddition() {
		MGraph base = new SimpleMGraph();
		Enricher siblingEnricher = new SiblingEnricher();
		TripleCollection enrichmentTriples = new EnrichmentTriples(base,
				Collections.singleton(siblingEnricher));
		NonLiteral grandMother = new UriRef("http://example.org/grand");
		NonLiteral mother = new UriRef("http://example.org/mother");
		NonLiteral me = new UriRef("http://example.org/me");
		NonLiteral sister = new UriRef("http://example.org/sister");
		NonLiteral uncle = new UriRef("http://example.org/uncle");
		NonLiteral cousin = new UriRef("http://example.org/cousin");
		base.add(new TripleImpl(me, SiblingEnricher.parentProperty, mother));
		base.add(new TripleImpl(sister, SiblingEnricher.parentProperty, mother));
		base.add(new TripleImpl(mother, SiblingEnricher.parentProperty, grandMother));
		base.add(new TripleImpl(uncle, SiblingEnricher.parentProperty, grandMother));
		base.add(new TripleImpl(cousin, SiblingEnricher.parentProperty, uncle));
		Assert.assertTrue(enrichmentTriples.filter(sister, SiblingEnricher.siblingProperty, me).hasNext());
		Assert.assertTrue(enrichmentTriples.filter(uncle, SiblingEnricher.siblingProperty, mother).hasNext());
		Assert.assertFalse(enrichmentTriples.filter(uncle, SiblingEnricher.siblingProperty, sister).hasNext());
	}

	@Test
	public void cityWeather() {
		MGraph base = new SimpleMGraph();
		Enricher cityEnricher = new CityWeatherEnricher();
		TripleCollection enrichmentTriples = new EnrichmentTriples(base,
				Collections.singleton(cityEnricher));
		NonLiteral london = new UriRef("http://example.org/london");
		NonLiteral me = new UriRef("http://example.org/me");
		base.add(new TripleImpl(me, RDF.type, FOAF.Agent));
		base.add(new TripleImpl(london, RDF.type, CityWeatherEnricher.classCity));
		Assert.assertTrue(enrichmentTriples.filter(london, CityWeatherEnricher.weatherProperty, null).hasNext());
		GraphNode node = new GraphNode(london, enrichmentTriples);
		Assert.assertEquals("0.51",
				node.getObjectNodes(CityWeatherEnricher.weatherProperty).next()
				.getLiterals(CityWeatherEnricher.humidityProperty).next().getLexicalForm());
		Assert.assertEquals(Double.toString(london.toString().length()),
				node.getObjectNodes(CityWeatherEnricher.weatherProperty).next()
				.getLiterals(CityWeatherEnricher.temperatureProperty).next().getLexicalForm());
	}



}
