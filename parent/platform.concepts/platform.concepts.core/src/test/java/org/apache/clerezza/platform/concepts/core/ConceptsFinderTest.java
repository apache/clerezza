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
package org.apache.clerezza.platform.concepts.core;

import java.util.Arrays;
import java.util.List;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.concepts.ontologies.QUERYRESULT;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.LockableMGraphWrapper;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.SKOS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the functionality of a ConceptsFinder.
 *
 * @author hasan
 */
public class ConceptsFinderTest {

	private final UriRef concept1a = new UriRef("http://example.org/concept1a");
	private final UriRef concept1b = new UriRef("http://example.org/concept1b");
	private final UriRef concept1c = new UriRef("http://example.org/concept1c");
	private final UriRef concept1d = new UriRef("http://example.org/concept1d");

	private final UriRef concept2a = new UriRef("http://example.org/concept2a");
	private final UriRef concept2b = new UriRef("http://example.org/concept2b");
	private final UriRef concept2c = new UriRef("http://example.org/concept2c");
	private final UriRef concept2d = new UriRef("http://example.org/concept2d");

	private final List<UriRef> concepts1 = Arrays.asList(
			concept1a, concept1b, concept1c, concept1d);
	private final List<UriRef> concepts2 = Arrays.asList(
			concept2a, concept2b, concept2c, concept2d);

	private class TestConceptProvider implements ConceptProvider {
		MGraph conceptGraph = new SimpleMGraph();

		public TestConceptProvider(List<UriRef> myConcepts,
				List<UriRef> yourConcepts, List<Integer> sameAs) {
			Assert.assertTrue(myConcepts.size()==4);
			Assert.assertTrue(yourConcepts.size()==4);
			for (UriRef concept : myConcepts) {
				conceptGraph.add(new TripleImpl(concept, RDF.type, SKOS.Concept));
			}
			for (Integer index : sameAs) {
				conceptGraph.add(new TripleImpl(myConcepts.get(index), OWL.sameAs,
					yourConcepts.get(index)));
			}
		}

		@Override
		public Graph retrieveConcepts(String searchTerm) {
			return conceptGraph.getGraph();
		}
	}

	private class TestedConceptProviderManager extends SimpleConceptProviderManager {
		public void fillConceptProviderList() {
			ConceptProvider CP1 = new TestConceptProvider(concepts1, concepts2,
					Arrays.asList(1,3));
			ConceptProvider CP2 = new TestConceptProvider(concepts2, concepts1,
					Arrays.asList(2,3));
			getConceptProviders().add(CP1);
			getConceptProviders().add(CP2);
		}
	}

	private class TestedConceptsFinder extends ConceptsFinder {
	}

	private static LockableMGraph mGraph = new LockableMGraphWrapper(new SimpleMGraph());
	private TestedConceptProviderManager testedConceptProviderManager;
	private TestedConceptsFinder testedConceptsFinder;

	@Before
	public void setUp() {
		final PlatformConfig platformConfig = new PlatformConfig() {

			@Override
			public UriRef getDefaultBaseUri() {
				return new UriRef("http://testing.localhost/");
			}
			
		};
		final ContentGraphProvider cgProvider = new ContentGraphProvider() {

			@Override
			public LockableMGraph getContentGraph() {
				return new LockableMGraphWrapper(new SimpleMGraph());
			}

		};

		testedConceptProviderManager = new TestedConceptProviderManager();
		testedConceptProviderManager.cgProvider = new ContentGraphProvider() {
			@Override
			public LockableMGraph getContentGraph() {
				return mGraph;
			}
		};
		testedConceptsFinder = new TestedConceptsFinder() {
			{
				bindPlatformConfig(platformConfig);
				bindCgProvider(cgProvider);
			}
		};
		testedConceptsFinder.conceptProviderManager =
				testedConceptProviderManager;

	}

	@Test
	public void testFindConcepts() {
		testedConceptProviderManager.fillConceptProviderList();
		GraphNode proposals = testedConceptsFinder.findConcepts("any");
		Assert.assertEquals(5, proposals.countObjects(QUERYRESULT.concept));
		Assert.assertTrue(proposals.hasProperty(QUERYRESULT.concept, concept1a));
		Assert.assertTrue(proposals.hasProperty(QUERYRESULT.concept, concept1b));
		Assert.assertTrue(proposals.hasProperty(QUERYRESULT.concept, concept1c));
		Assert.assertTrue(proposals.hasProperty(QUERYRESULT.concept, concept1d));
		Assert.assertTrue(proposals.hasProperty(QUERYRESULT.concept, concept2a));
		Assert.assertFalse(proposals.hasProperty(QUERYRESULT.concept, concept2b));
		Assert.assertFalse(proposals.hasProperty(QUERYRESULT.concept, concept2c));
		Assert.assertFalse(proposals.hasProperty(QUERYRESULT.concept, concept2d));
	}
}
