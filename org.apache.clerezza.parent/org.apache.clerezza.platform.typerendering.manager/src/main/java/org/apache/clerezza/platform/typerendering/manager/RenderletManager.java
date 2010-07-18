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
package org.apache.clerezza.platform.typerendering.manager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.content.DiscobitsHandler;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.clerezza.platform.typerendering.ontologies.TYPERENDERING;
import org.apache.clerezza.platform.typerendering.ontology.RENDERLETMANAGER;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.security.TcPermission;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.felix.scr.annotations.Services;

/**
 * 
 * GUI Renderlet Manager
 * 
 * @author tio
 */
@Component
@Services ({
	@Service(Object.class),
	@Service(GlobalMenuItemsProvider.class)
})
@Property(name="javax.ws.rs", boolValue=true)
@Path("/admin/renderlets")
public class RenderletManager implements GlobalMenuItemsProvider{
	
	@Reference(target = PlatformConfig.CONFIG_GRAPH_FILTER)
	private MGraph configGraph;
	@Reference
	private DiscobitsHandler contentHandler;
	@Reference
	private org.apache.clerezza.platform.typerendering.RenderletManager renderletManager;
	private final String RdfTypePrioList = "http://tpf.localhost/rdfTypePriorityList";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The activate method is called when SCR activates the component
	 * configuration. This method creates a directory and the renderletManagerTemplateUrl if the
	 * renderletManagerTemplateUrl does not already exists.
	 * 
	 * @param componentContext
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected void activate(ComponentContext componentContext)
			throws IOException, URISyntaxException {
		logger.info("Renderlet Manager activated.");
		URL renderletManagerTemplateUrl = getClass().getResource("renderlet-manager.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(renderletManagerTemplateUrl.toURI().toString()), RENDERLETMANAGER.RenderletManagerPage,
				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);
		URL renderletTemplateUrl = getClass().getResource("renderlet.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(renderletTemplateUrl.toURI().toString()), TYPERENDERING.RenderletDefinition,
				null, MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}

	@GET
	public Response renderletMgrHome(@Context UriInfo uriInfo) {
		if (uriInfo.getAbsolutePath().toString().endsWith("/")) {
			return RedirectUtil.createSeeOtherResponse("overview", uriInfo);
		}
		return RedirectUtil.createSeeOtherResponse("renderlets/overview",
				uriInfo);
	}

	/**
	 * Overview of installed renderlets
	 * 
	 * @return {@link GraphNode}
	 * 
	 */
	@GET
	@Path("overview")
	public GraphNode overview(@Context UriInfo uriInfo) {
		TrailingSlash.enforceNotPresent(uriInfo);
		AccessController.checkPermission(new RenderletManagerAccessPermission());

		MGraph additionGraph = new SimpleMGraph();
		UnionMGraph resultGraph = new UnionMGraph(additionGraph, configGraph);
		GraphNode resultNode = new GraphNode(new UriRef(
				RdfTypePrioList), resultGraph);
		resultNode.addProperty(RDF.type, RENDERLETMANAGER.RenderletManagerPage);
		resultNode.addProperty(RDF.type, PLATFORM.HeadedPage);
		return resultNode;

	}

	/**
	 * Installs a renderletManagerTemplateUrl from. The type defines the RDF-type of the Renderlet.
	 * 
	 * @param form
	 * @return {@link Response}
	 * @throws URISyntaxException
	 * 
	 */
	@POST
	@Consumes("multipart/form")
	@Path("submit-renderlet")
	public Response submitRenderlet(MultiPartBody form, @Context UriInfo uriInfo) {
		TrailingSlash.enforceNotPresent(uriInfo);
		AccessController.checkPermission(new RenderletManagerAccessPermission());
		String type = form.getTextParameterValues("renderedType")[0];
		String mode = form.getTextParameterValues("renderingMode")[0];
		MediaType mediaType = MediaType.valueOf(form.getTextParameterValues("mediaType")[0]);
		String renderletName = form.getTextParameterValues("renderlet")[0];
		String renderingSpecificationUriString = form.getTextParameterValues("renderingSpecificationUri")[0];
		UriRef renderingSpecificationUri = getUriRef(renderingSpecificationUriString);
		FormFile formFile = form.getFormFileParameterValues("renderingSpecification")[0];
		byte[] renderSpecBytes = formFile.getContent();
		final boolean fileUploaded = renderSpecBytes != null && (renderSpecBytes.length != 0);
		UriRef renderedType;
		if (renderingSpecificationUri == null) {
			if (!fileUploaded) {
				String message = "no renderlet uploaded";
				logger.warn(message);
				throw new WebApplicationException(Response.status(
						Status.BAD_REQUEST).entity(message).build());
			}
			renderingSpecificationUri = getUnusedUriRef(uriInfo, formFile.getFileName());
		} else {
			if (fileUploaded) {
				//check that the URI point to the platfomrm
				//for now just exclude non-http/https uris
				if (!renderingSpecificationUri.getUnicodeString().startsWith("http")) {
					String message = "when uploading a file the URI must be empty" +
							"or a location served by this platform instance.";
					logger.warn(message);
					throw new WebApplicationException(Response.status(
							Status.BAD_REQUEST).entity(message).build());
				}
			}
		}
		renderedType = new UriRef(type);

		contentHandler.put(renderingSpecificationUri, formFile.getMediaType(), renderSpecBytes);

		if (mode.equals("")) {
			mode = null;
		}
		renderletManager.registerRenderlet(
				renderletName, renderingSpecificationUri, renderedType, mode, mediaType, false);
		return RedirectUtil.createSeeOtherResponse("overview", uriInfo);
	}

	private Resource getDefinitionResource(String renderlet,
			UriRef renderSpec, UriRef renderedType, String renderingMode, String mediaType) {
		if (renderlet != null) {
			final Literal renderletLit = LiteralFactory.getInstance().createTypedLiteral(renderlet);
			Iterator<Triple> triples = configGraph.filter(null, TYPERENDERING.renderlet,
					renderletLit);
			while (triples.hasNext()) {
				final NonLiteral resource = triples.next().getSubject();
				if (matchProperty(resource, renderSpec, renderedType, 
						renderingMode, mediaType)) {
					return resource;
				}
			}
		}
		throw new RuntimeException("No rendering defintion " + renderlet + "/" + renderSpec + "/" + renderedType + "/" + renderingMode);
	}

	private boolean matchProperty(NonLiteral renderletDef, UriRef renderSpec,
			UriRef rdfType, String renderingMode, String mediaType) {
		String mode = getRenderingMode(renderletDef);
		if (renderingMode.equals("")) {
			renderingMode = null;
		}
		if (!configGraph.contains(new TripleImpl(renderletDef,
				TYPERENDERING.mediaType, LiteralFactory.getInstance()
							.createTypedLiteral(mediaType)))) {
			return false;
		}
		if (equals(renderSpec, getRenderSpecOfRenderletDefinition(renderletDef)) 
				&& rdfType.equals(getRenderletRdfType(renderletDef)) && equals(renderingMode, mode)) {
			return true;
		}
		return false;
	}

	private boolean equals(Object o1, Object o2) {
		return o1 == o2 ||
				(o1 != null) && o1.equals(o2);
	}

	/**
	 * Returns the renderering specification of the specified renderletManagerTemplateUrl definition. Returns null
	 * if the renderletManagerTemplateUrl definition has no renderletManagerTemplateUrl.
	 * 
	 * @param renderletDef
	 * @return
	 */
	private UriRef getRenderSpecOfRenderletDefinition(Resource renderletDef) {
		Iterator<Triple> renderletIter = configGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.renderingSpecification, null);
		if (renderletIter.hasNext()) {
			return (UriRef) renderletIter.next().getObject();
		}
		return null;
	}

	/**
	 * Returns the renderletManagerTemplateUrl rdf-type of the specified renderletManagerTemplateUrl definition.
	 * Returns null if the renderletManagerTemplateUrl definition has no renderletManagerTemplateUrl rdf-type.

	 * @param renderletDef
	 * @return
	 */
	private UriRef getRenderletRdfType(Resource renderletDef) {
		Iterator<Triple> renderedTypeIter = configGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.renderedType, null);

		if (renderedTypeIter.hasNext()) {
			return (UriRef) renderedTypeIter.next().getObject();
		}
		return null;
	}

	private String getRenderingMode(Resource renderletDef) {
		Iterator<Triple> renderingModeIter = configGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.renderingMode, null);

		if (renderingModeIter.hasNext()) {
			TypedLiteral literal = (TypedLiteral) renderingModeIter.next().getObject();
			return literal.getLexicalForm();
		}
		return null;
	}

	/**
	 * Moves an element of the list one to the right
	 * 
	 * @param uriRef
	 * @return Response
	 * @throws URISyntaxException
	 * 
	 */
	@POST
	@Path("down")
	public Response down(@FormParam("renderedType") UriRef renderedType,
			@Context UriInfo uriInfo) throws URISyntaxException {
		TrailingSlash.enforceNotPresent(uriInfo);
		AccessController.checkPermission(new RenderletManagerAccessPermission());
		List<Resource> rdfTypesPrioList = new RdfList(new UriRef(
				RdfTypePrioList), configGraph);
		final int index = rdfTypesPrioList.indexOf(renderedType);
		if (index < rdfTypesPrioList.size() - 1) {
			rdfTypesPrioList.remove(renderedType);
			rdfTypesPrioList.add(index + 1, renderedType);
		}
		return RedirectUtil.createSeeOtherResponse("overview", uriInfo);
	}

	/**
	 * Moves an element of a list one to the left.
	 *
	 * @param uriRef
	 * @return Response
	 * @throws URISyntaxException
	 *
	 */
	@POST
	@Path("up")
	public Response up(@FormParam("renderedType") UriRef renderedType,
			@Context UriInfo uriInfo) throws URISyntaxException {
		TrailingSlash.enforceNotPresent(uriInfo);
		AccessController.checkPermission(new RenderletManagerAccessPermission());
		final MGraph mGraph = configGraph;
		final List<Resource> rdfTypesPrioList = new RdfList(new UriRef(
				RdfTypePrioList), mGraph);
		final int index = rdfTypesPrioList.indexOf(renderedType);
		if (index > 0) {
			rdfTypesPrioList.remove(renderedType);
			rdfTypesPrioList.add(index - 1, renderedType);
		}
		return RedirectUtil.createSeeOtherResponse("overview", uriInfo);
	}

	/**
	 * Uninstalls the renderletManagerTemplateUrl with the specified index.
	 * 
	 * @param uriRef
	 * @return Response
	 * @throws URISyntaxException
	 * 
	 */
	@POST
	@Path("uninstall-renderlet")
	public Response uninstallRenderlet(@FormParam("renderlet") String renderlet,
			@FormParam("renderingSpecification") UriRef renderSpec,
			@FormParam("renderedType") UriRef renderedType,
			@FormParam("renderingMode") String renderingMode,
			@FormParam("mediaType") String mediaType,
			@Context UriInfo uriInfo) throws URISyntaxException {	
		TrailingSlash.enforceNotPresent(uriInfo);
		AccessController.checkPermission(new RenderletManagerAccessPermission());
		logger.info("remove item");
		List<Resource> renderletDefinitions = new RdfList(new UriRef(
				RdfTypePrioList), configGraph);
		final Resource resource = getDefinitionResource(renderlet,
				renderSpec, renderedType, renderingMode, mediaType);

		GraphNode node = new GraphNode((NonLiteral) resource, configGraph);
		node.deleteProperties(TYPERENDERING.renderlet);
		node.deleteProperties(TYPERENDERING.renderingSpecification);
		node.deleteProperties(TYPERENDERING.renderingMode);
		node.deleteProperties(TYPERENDERING.renderedType);
		node.deleteProperty(RDF.type, TYPERENDERING.RenderletDefinition);
		renderletDefinitions.remove(resource);
		return RedirectUtil.createSeeOtherResponse("overview", uriInfo);
	}

	/**
	 * Form for installing a renderletManagerTemplateUrl by specifying its location.
	 *
	 * @return a page containing the requested form
	 *
	 */
	@GET
	@Produces("text/html")
	@Path("install-renderlet-form")
	public GraphNode installRenderletForm(@Context UriInfo uriInfo) {
		TrailingSlash.enforceNotPresent(uriInfo);
		AccessController.checkPermission(new RenderletManagerAccessPermission());
		MGraph resultGraph = new SimpleMGraph();
		GraphNode resultNode = new GraphNode(new UriRef(
				RdfTypePrioList), resultGraph);
		resultNode.addProperty(RDF.type, TYPERENDERING.RenderletDefinition);
		return resultNode;
	}

	/**
	 * Form for editing a renderletManagerTemplateUrl by specifying its location.
	 * 
	 * @return a page containing the requested form
	 * 
	 */
	@GET
	@Path("edit-renderlet-form")
	public GraphNode editRenderletForm(@QueryParam("renderlet") String renderlet,
			@QueryParam("renderingSpecification") UriRef renderSpec,
			@QueryParam("renderedType") UriRef renderedType,
			@QueryParam("renderingMode") String renderingMode,
			@QueryParam("mediaType") String mediaType,
			@Context UriInfo uriInfo) {
		TrailingSlash.enforceNotPresent(uriInfo);
		AccessController.checkPermission(new RenderletManagerAccessPermission());
		final Resource resource = getDefinitionResource(renderlet,
				renderSpec, renderedType, renderingMode, mediaType);
		GraphNode node = new GraphNode((NonLiteral) resource, configGraph);
		return node;
	}

	private UriRef getUriRef(String renderingSpecificationUri) {
		if ((renderingSpecificationUri == null) ||
				renderingSpecificationUri.equals("") ||
				renderingSpecificationUri.equals("<CREATE NEW URI FOR UPLOADED FILE>")) {
			return null;
		} else {
			return new UriRef(renderingSpecificationUri);
		}
	}

	private UriRef getUnusedUriRef(UriInfo uriInfo, String fileName) {
		URI absolutePath = uriInfo.getAbsolutePath();
		final String baseUriString = absolutePath.getScheme() + "://" + absolutePath.getAuthority() + "/render-spec/" + fileName;
		UriRef uriRef = new UriRef(baseUriString);
		int counter = 0;
		while (contentHandler.getData(uriRef) != null) {
			counter++;
			uriRef = new UriRef(baseUriString + "." + counter);
		}
		return uriRef;

	}

	@Override
	public Set<GlobalMenuItem> getMenuItems() {
		Set<GlobalMenuItem> items = new HashSet<GlobalMenuItem>();
		try {
			AccessController.checkPermission(new RenderletManagerAccessPermission());
			AccessController.checkPermission(
					new TcPermission("http://tpf.localhost/config.graph", TcPermission.READWRITE));
		} catch (AccessControlException e) {
			return items;
		}
		items.add(new GlobalMenuItem("/admin/renderlets/", "RMR", "Renderlets", 3,
				"Administration"));
		return items;
	}
}
