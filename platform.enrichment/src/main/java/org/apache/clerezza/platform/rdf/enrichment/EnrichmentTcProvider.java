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
package org.apache.clerezza.platform.rdf.enrichment;

import java.security.Permission;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.access.security.TcPermission;
import org.apache.clerezza.rdf.enrichment.Enricher;
import org.apache.clerezza.rdf.enrichment.EnrichmentTriples;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/**
 * Provides a read-only MGraph with the name urn:x-localinstance:/enrichment.graph
 * containing the enrichments provided by all available services of type 
 * Enricher on the content graph.
 *
 * For performance reasons EnrichmentTcProvider gets the content-graph from
 * TcManager and doesn't do access control check on every request, to prevent
 * deductions about the content of the content graph by unauthorized users
 * the required permissions on the enrichment graph are set to those on the
 * content graph.
 *
 *
 * @author reto
 */
@Component
@Service(WeightedTcProvider.class)
@References(
	{
		@Reference(referenceInterface=Enricher.class, name="enricher",
				cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
				policy=ReferencePolicy.DYNAMIC)
	}
)
public class EnrichmentTcProvider implements WeightedTcProvider {

	public static final UriRef ENRICHMENT_GRAPH_URI = new UriRef("urn:x-localinstance:/enrichment.graph");
	
	private LockableMGraph contentGraph;
	private final Collection<Enricher> enrichers = Collections.synchronizedCollection(new HashSet<Enricher>());

	@Reference
	private ContentGraphProvider cgProvider;

	@Reference
	private TcManager tcManager;

	@Override
	public Graph getGraph(UriRef name) throws NoSuchEntityException {
		throw new NoSuchEntityException(name);
	}

	@Override
	public MGraph getMGraph(UriRef name) throws NoSuchEntityException {
		if (ENRICHMENT_GRAPH_URI.equals(name)) {
			return getEnrichmentGraph();
		}
		throw new NoSuchEntityException(name);
	}

	@Override
	public TripleCollection getTriples(UriRef name) throws NoSuchEntityException {
		if (ENRICHMENT_GRAPH_URI.equals(name)) {
			return getEnrichmentGraph();
		}
		throw new NoSuchEntityException(name);
	}

	@Override
	public Set<UriRef> listGraphs() {
		return new HashSet<UriRef>(0);
	}

	@Override
	public Set<UriRef> listMGraphs() {
		return Collections.singleton(ENRICHMENT_GRAPH_URI);
	}

	@Override
	public Set<UriRef> listTripleCollections() {
		return Collections.singleton(ENRICHMENT_GRAPH_URI);
	}

	@Override
	public MGraph createMGraph(UriRef name) throws UnsupportedOperationException, EntityAlreadyExistsException {
		throw new UnsupportedOperationException("creating entities not supported");
	}

	@Override
	public Graph createGraph(UriRef name, TripleCollection triples) throws UnsupportedOperationException, EntityAlreadyExistsException {
		throw new UnsupportedOperationException("creating entities not supported");
	}

	@Override
	public void deleteTripleCollection(UriRef name) throws UnsupportedOperationException, NoSuchEntityException, EntityUndeletableException {
		throw new UnsupportedOperationException("deleting entities not supported");
	}

	@Override
	public Set<UriRef> getNames(Graph graph) {
		return new HashSet<UriRef>(0);
	}

	@Override
	public int getWeight() {
		return 0;
	}

	private MGraph getEnrichmentGraph() {
		return new EnrichmentTriples(contentGraph, enrichers);
	}

	/**
	 * avtivates the component and adds the enrichment-graph to the virtual
	 * content graph.
	 * 
	 * gets the base content-graph from tcManager and sets the permission
	 * required to access the enrichment-graph accordingly
	 */
	protected void activate(ComponentContext context) {
		contentGraph = tcManager.getMGraph(Constants.CONTENT_GRAPH_URI);
		Collection<Permission> requiredReadPermissions =
				tcManager.getTcAccessController().getRequiredReadPermissions(Constants.CONTENT_GRAPH_URI);
		if (requiredReadPermissions.isEmpty()) {
			tcManager.getTcAccessController().setRequiredReadPermissionStrings(
					ENRICHMENT_GRAPH_URI, Collections.singleton(
						new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READ).toString()
					));
		} else {
			tcManager.getTcAccessController().setRequiredReadPermissions(
					ENRICHMENT_GRAPH_URI, requiredReadPermissions);
		}
		cgProvider.addTemporaryAdditionGraph(ENRICHMENT_GRAPH_URI);
	}

	/**
	 * deactivates the compononent removing the enrichment-graph from the
	 * virtual content graph
	 */
	protected void deactivate(ComponentContext context) {
		cgProvider.removeTemporaryAdditionGraph(ENRICHMENT_GRAPH_URI);
		contentGraph = null;
	}



	protected void bindEnricher(Enricher enricher) {
		enrichers.add(enricher);
	}

	protected void unbindEnricher(Enricher enricher) {
		enrichers.remove(enricher);
	}

}
