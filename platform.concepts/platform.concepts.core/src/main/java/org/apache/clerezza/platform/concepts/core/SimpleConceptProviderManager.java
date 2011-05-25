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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.concepts.ontologies.CONCEPTS;
import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.security.TcPermission;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.felix.scr.annotations.Services;
import org.osgi.service.component.ComponentContext;

/**
 * This service manages concept providers. Concept providers are prioritized.
 *
 * The URI path of this service is /concepts/provider-manager.
 *
 * @author hasan, tio
 */
@Component
@Services({
	@Service(Object.class),
	@Service(ConceptProviderManager.class),
	@Service(GlobalMenuItemsProvider.class)
})
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/concepts/provider-manager")
public class SimpleConceptProviderManager implements ConceptProviderManager,
		GlobalMenuItemsProvider {

	@Reference
	private TcManager tcManager;

	@Reference
	protected ContentGraphProvider cgProvider;

	@Reference
	private RenderletManager renderletManager;

	private List<ConceptProvider> conceptProviderList =
			new ArrayList<ConceptProvider>();

	/**
	 * The activate method is called when SCR activates the component
	 * configuration. 
	 * 
	 * @param context
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected void activate(ComponentContext context)
			throws IOException,
			URISyntaxException {

		URL template = getClass().getResource("manage-concept-providers-page.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(template.toURI().toString()),
				CONCEPTS.ManageConceptProvidersPage, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);

		instantiateConceptProviders();
	}

	protected void deactivate(ComponentContext context) {
		conceptProviderList.clear();
	}

	private void instantiateConceptProviders() {
		conceptProviderList.clear();
		MGraph contentGraph = cgProvider.getContentGraph();

		NonLiteral cplNode = getConceptProviderListNode(contentGraph);
		RdfList cpl = new RdfList(cplNode, contentGraph);
		for (int i = 0; i < cpl.size(); i++) {
			NonLiteral conceptProvider = (NonLiteral) cpl.get(i);
			Iterator<Triple> conceptProviderTypes = contentGraph.filter(
					conceptProvider, RDF.type, null);
			if (conceptProviderTypes.hasNext()) {
				UriRef conceptProviderType = (UriRef) conceptProviderTypes.next().getObject();
				if (conceptProviderType.equals(CONCEPTS.LocalConceptProvider)) {
					instantiateLocalConceptProvider(contentGraph, conceptProvider);
				} else {
					instantiateRemoteConceptProvider(contentGraph, conceptProvider);
				}
			}
		}
	}

	private NonLiteral getConceptProviderListNode(MGraph contentGraph) {
		Iterator<Triple> triples = contentGraph.filter(null, RDF.type,
				CONCEPTS.ConceptProviderList);
		if (triples.hasNext()) {
			return triples.next().getSubject();
		}
		NonLiteral cplNode = new BNode();
		new RdfList(cplNode, contentGraph);
		contentGraph.add(new TripleImpl(cplNode, RDF.type,
				CONCEPTS.ConceptProviderList));
		return cplNode;
	}

	private void instantiateLocalConceptProvider(MGraph contentGraph,
			NonLiteral conceptProvider) {
		Iterator<Triple> selectedSchemes = contentGraph.filter(
				conceptProvider, CONCEPTS.selectedScheme, null);
		if (selectedSchemes.hasNext()) {
			UriRef selectedScheme = (UriRef) selectedSchemes.next().getObject();
			conceptProviderList.add(new LocalConceptProvider(tcManager,
					cgProvider, selectedScheme));
		}
	}

	private void instantiateRemoteConceptProvider(MGraph contentGraph,
			NonLiteral conceptProvider) {
		Iterator<Triple> endPoints = contentGraph.filter(
				conceptProvider, CONCEPTS.sparqlEndPoint, null);
		if (endPoints.hasNext()) {
			UriRef sparqlEndPoint = (UriRef) endPoints.next().getObject();
			Iterator<Triple> defaultGraphs = contentGraph.filter(
					conceptProvider, CONCEPTS.defaultGraph, null);
			UriRef defaultGraph = null;
			if (defaultGraphs.hasNext()) {
				defaultGraph = (UriRef) defaultGraphs.next().getObject();
			}
			Iterator<Triple> queryTemplates = contentGraph.filter(
					conceptProvider, CONCEPTS.queryTemplate, null);
			if (queryTemplates.hasNext()) {
				TypedLiteral queryTemplate =
						(TypedLiteral) queryTemplates.next().getObject();
				conceptProviderList.add(
						new RemoteConceptProvider(sparqlEndPoint,
						defaultGraph, queryTemplate.getLexicalForm()));
			}
		}
	}

	/**
	 * Returns a GraphNode containing a list of {@link ConceptProvider}s stored
	 * in the content graph to be managed. The order in the list represents
	 * the priority of the providers.
	 * This resource is accessible through a GET request on the URI sub-path
	 * "edit-concept-provider-list".
	 *
	 */
	@GET
	@Path("edit-concept-provider-list")
	public GraphNode getProviderList(@Context UriInfo uriInfo) {
		AccessController.checkPermission(
				new ConceptProviderManagerAppPermission());
		TrailingSlash.enforceNotPresent(uriInfo);
		MGraph contentGraph = cgProvider.getContentGraph();
		MGraph resultGraph = new SimpleMGraph();

		NonLiteral cplNode = getConceptProviderListNode(contentGraph);
		GraphNode resultNode = new GraphNode(cplNode, resultGraph);

		resultNode.addProperty(RDF.type, CONCEPTS.ManageConceptProvidersPage);
		resultNode.addProperty(RDF.type, PLATFORM.HeadedPage);
		return new GraphNode(resultNode.getNode(),
				new UnionMGraph(resultGraph, contentGraph));
	}

	/**
	 * Allows the list of {@link ConceptProvider}s stored in the content graph
	 * to be updated with the list POSTed via the URI sub-path
	 * "update-concept-provider-list".
	 * The order in the list represents the priority of the providers.
	 *
	 * @param types
	 *		specify the type of each ConceptProvider: either a
	 *		LocalConceptProvider or a RemoteConceptProvider.
	 * @param sparqlEndPoints
	 *		the SPARQL EndPoint to connect to in case of a RemoteConceptProvider.
	 * @param defaultGraphs
	 *		the Graph to be queried in case of a RemoteConceptProvider.
	 * @param queryTemplates
	 *		the template for the query to be used in case of a RemoteConceptProvider.
	 * @param conceptSchemes
	 *		the concept scheme within which concepts are to be searched,
	 *		in case of a LocalConceptProvider.
	 * @return
	 *		- a 200 (OK) response if everything is fine.
	 *		- a 400 (BAD REQUEST) response if types parameter is undefined.
	 */
	@POST
	@Path("update-concept-provider-list")
	public Response updateConceptProviders(
			@FormParam("types") List<String> types,
			@FormParam("sparqlEndPoints") List<String> sparqlEndPoints,
			@FormParam("defaultGraphs") List<String> defaultGraphs,
			@FormParam("queryTemplates") List<String> queryTemplates,
			@FormParam("conceptSchemes") List<String> conceptSchemes) {

		if (types == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity("Form parameter \"types\" should be defined").build();
		}
		MGraph contentGraph = cgProvider.getContentGraph();
		NonLiteral cplNode = getConceptProviderListNode(contentGraph);
		GraphNode cplGraphNode = new GraphNode(cplNode, contentGraph);
		cplGraphNode.deleteNodeContext();

		cplNode = getConceptProviderListNode(contentGraph);
		RdfList cpl = new RdfList(cplNode, contentGraph);

		int length = types.size();
		for (int i=0; i<length; i++) {
			UriRef conceptProviderType = new UriRef(types.get(i));
			BNode conceptProvider = new BNode();
			contentGraph.add(new TripleImpl(conceptProvider, RDF.type,
					conceptProviderType));
			if (conceptProviderType.equals(CONCEPTS.LocalConceptProvider)) {
				contentGraph.add(new TripleImpl(conceptProvider,
						CONCEPTS.selectedScheme,
						new UriRef(conceptSchemes.get(i))));
			} else {
				contentGraph.add(new TripleImpl(conceptProvider,
						CONCEPTS.sparqlEndPoint,
						new UriRef(sparqlEndPoints.get(i))));
				String defaultGraph = defaultGraphs.get(i);
				if (!defaultGraph.trim().isEmpty()) {
					contentGraph.add(new TripleImpl(conceptProvider,
							CONCEPTS.defaultGraph,
							new UriRef(defaultGraph)));
				}
				contentGraph.add(new TripleImpl(conceptProvider,
						CONCEPTS.queryTemplate,
						LiteralFactory.getInstance().createTypedLiteral(
						queryTemplates.get(i))));
			}
			cpl.add(i, conceptProvider);
		}
		instantiateConceptProviders();
		return Response.status(Status.OK).build();
	}

	@Override
	public List<ConceptProvider> getConceptProviders() {
		return conceptProviderList;
	}

	@Override
	public Set<GlobalMenuItem> getMenuItems() {
		Set<GlobalMenuItem> items = new HashSet<GlobalMenuItem>();
		try {
			AccessController.checkPermission(
					new TcPermission("urn:x-localinstance:/content.graph",
					TcPermission.READWRITE));
		} catch (AccessControlException e) {
			return items;
		}
		try {
			AccessController.checkPermission(
					new ConceptProviderManagerAppPermission());
		} catch (AccessControlException e) {
			return items;
		}
		items.add(new GlobalMenuItem("/concepts/provider-manager/edit-concept-provider-list",
				"CPM", "Concept Providers", 5, "Administration"));
		return items;
	}
}
