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
import javax.ws.rs.core.Response;
import org.apache.clerezza.platform.concepts.ontologies.CONCEPTS;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.LockableMGraphWrapper;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the functionality of a SimpleConceptProviderManager.
 *
 * @author hasan
 */
public class ConceptProviderManagerTest {

	private class TestConceptProvider implements ConceptProvider {
		@Override
		public Graph retrieveConcepts(String searchTerm) {
			return null;
		}
	}

	private class TestedConceptProviderManager extends SimpleConceptProviderManager {
		public void fillConceptProviderList() {
			ConceptProvider CP1 = new TestConceptProvider();
			ConceptProvider CP2 = new TestConceptProvider();
			getConceptProviders().add(CP1);
			getConceptProviders().add(CP2);
		}
	}

	private static LockableMGraph mGraph = new LockableMGraphWrapper(new SimpleMGraph());

	private TestedConceptProviderManager testedConceptProviderManager;

	@Before
	public void setUp() {
		testedConceptProviderManager = new TestedConceptProviderManager();
		testedConceptProviderManager.cgProvider = new ContentGraphProvider() {

			@Override
			public LockableMGraph getContentGraph() {
				return mGraph;
			}
		};
	}

	@Test
	public void testUpdateConceptProviders() {
		testedConceptProviderManager.fillConceptProviderList();
		List<ConceptProvider> cpl = testedConceptProviderManager
				.getConceptProviders();
		Assert.assertTrue(cpl.get(0) instanceof TestConceptProvider);
		Assert.assertTrue(cpl.get(1) instanceof TestConceptProvider);
		Assert.assertTrue(cpl.size()==2);
		List<String> types = Arrays.asList(
				CONCEPTS.LocalConceptProvider.getUnicodeString(),
				CONCEPTS.RemoteConceptProvider.getUnicodeString());
		List<String> sparqlEndPoint = Arrays.asList(
				"", "http://example.org/sparql");
		List<String> defaultGraphs = Arrays.asList(
				"", "http://example.org/graph");
		List<String> queryTemplates = Arrays.asList(
				"", "CONSTRUCT {?a ?b ?c .} WHERE {?a ?b ?c .}");
		List<String> conceptSchemes = Arrays.asList(
				"http://localhost:8080/default", "");
		Response response = testedConceptProviderManager.updateConceptProviders(
				types, sparqlEndPoint, defaultGraphs, queryTemplates,
				conceptSchemes);
		cpl = testedConceptProviderManager.getConceptProviders();
		Assert.assertTrue(cpl.get(0) instanceof LocalConceptProvider);
		Assert.assertTrue(cpl.get(1) instanceof RemoteConceptProvider);
		Assert.assertTrue(cpl.size()==2);
	}
}
