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
package org.apache.clerezza.platform.scripting.scriptmanager;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import javax.script.ScriptException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.platform.content.DiscobitsHandler;
import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.scripting.NoEngineException;
import org.apache.clerezza.platform.scripting.ScriptExecution;
import org.apache.clerezza.platform.scripting.ScriptLanguageDescription;
import org.apache.clerezza.platform.scripting.scriptmanager.ontology.SCRIPTMANAGER;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.seedsnipe.SeedsnipeRenderlet;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.security.TcPermission;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.SCRIPT;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 *
 * Script Manager (/admin/scripting)
 *
 * Allows to install, delete, edit and execute script using a web front end.
 *
 * Also enables creating and deleting of execution URIs for scripts.
 *
 * @author daniel, marc
 */
@Component
@Services({
	@Service(value=Object.class),
	@Service(value=GlobalMenuItemsProvider.class)
})
@Property(name="javax.ws.rs", boolValue=true)
@Path("/admin/scripting")
public class ScriptManager implements GlobalMenuItemsProvider{

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Reference
	private RenderletManager renderletManager;

	@Reference
	private ContentGraphProvider cgProvider;

	@Reference
	private DiscobitsHandler contentHandler;

	@Reference
	private ScriptExecution scriptExecution;

	private FileServer fileServer;
	
	/**
	 * Called when bundle is activated.
	 *
	 * Registers templates.
	 *
	 * @param componentContext  the context.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected void activate(final ComponentContext componentContext)
			throws IOException, URISyntaxException {
		logger.info("Script Manager activated.");

		Bundle bundle = componentContext.getBundleContext().getBundle();
		URL resourceDir = getClass().getResource("staticweb");
		PathNode pathNode = new BundlePathNode(bundle, resourceDir.getPath());
		logger.info("Initializing file server for {} ({})", resourceDir,
				resourceDir.getFile());

		fileServer = new FileServer(pathNode);
		URL renderlet = getClass().getResource("scriptmanager-script-overview.xhtml");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(renderlet.toURI().toString()),
				SCRIPTMANAGER.ScriptManagerOverviewPage,
				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderlet = getClass().getResource("scriptmanager-script-list.xhtml");
		renderletManager.registerRenderlet(SeedsnipeRenderlet.class.getName(),
				new UriRef(renderlet.toURI().toString()),
				SCRIPTMANAGER.ScriptList,
				"naked" , MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderlet = getClass().getResource("scriptmanager-script-install.xhtml");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(renderlet.toURI().toString()),
				SCRIPTMANAGER.ScriptManagerInstallPage,
				"naked" , MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderlet = getClass().getResource("scriptmanager-execution-uri-overview.xhtml");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(renderlet.toURI().toString()),
				SCRIPTMANAGER.ExecutionUriOverviewPage,
				"naked" , MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderlet = getClass().getResource("scriptmanager-execution-uri-list.xhtml");
		renderletManager.registerRenderlet(SeedsnipeRenderlet.class.getName(),
				new UriRef(renderlet.toURI().toString()),
				SCRIPTMANAGER.ExecutionUriList,
				"naked" , MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderlet = getClass().getResource("scriptmanager-script-information.xhtml");
		renderletManager.registerRenderlet(SeedsnipeRenderlet.class.getName(),
				new UriRef(renderlet.toURI().toString()),
				SCRIPTMANAGER.SelectedScript,
				"naked" , MediaType.APPLICATION_XHTML_XML_TYPE, true);

	}

	/**
	 * Redirects to the overview page
	 *
	 * @return  Redirect to overview page.
	 *
	 */
	@GET
	public Response redirectToOverviewPage(@Context UriInfo uriInfo) {
		if (uriInfo.getAbsolutePath().toString().endsWith("/")) {
			return RedirectUtil.createSeeOtherResponse(
					"script-overview", uriInfo);
		}
		return RedirectUtil.createSeeOtherResponse(
				"scripting/script-overview", uriInfo);
	}

	/**
	 * The overview page.
	 *
	 * @param  the script URI
	 * @return  Graphnode containing ScriptManagerOverviewPage.
	 */
	@GET
	@Path("script-overview")
	public GraphNode overview(
			@QueryParam(value = "script") UriRef script) {

		AccessController.checkPermission(new ScriptManagerAppPermission());
		MGraph contentGraph = cgProvider.getContentGraph();
		BNode resultResource = new BNode();
		MGraph resultGraph = new SimpleMGraph();
		
		GraphNode scriptNode = null;
		if(script != null){
			scriptNode = getScript(script);
			resultGraph.add(new TripleImpl(resultResource, 
					SCRIPTMANAGER.script,
					scriptNode.getNode()));
		}
		resultGraph.add(new TripleImpl(resultResource,
				RDF.type,
				PLATFORM.HeadedPage));
		resultGraph.add(new TripleImpl(resultResource, 
				RDF.type,
				SCRIPTMANAGER.ScriptManagerOverviewPage));
		GraphNode scriptList = getScriptList(resultResource);

		UnionMGraph unionGraph = null;
		if(scriptNode != null){
			unionGraph = new UnionMGraph(resultGraph, scriptList.getGraph(),
					scriptNode.getGraph(), contentGraph);
		} else {
			unionGraph = new UnionMGraph(resultGraph, scriptList.getGraph(),
				contentGraph);
		}
		
		return new GraphNode(resultResource, unionGraph);
	}

	/**
	 * Returns a selected script node which has a property that points to the
	 * specified script resource and conains the decoded script code and
	 * the available script languages.
	 *
	 * @param scriptUri  the URI of the script resource.
	 * @return  {@code GraphNode} containing the selected script.
	 *
	 * @see SCRIPTMANAGER#SelectedScript
	 */
	@GET
	@Path("get-script")
	@Produces("text/plain")
	public GraphNode getScript(
			@QueryParam(value = "script") UriRef scriptUri){

		AccessController.checkPermission(new ScriptManagerAppPermission());
		BNode resource = new BNode();
		MGraph resultGraph = new SimpleMGraph();


		resultGraph.add(new TripleImpl(resource, 
				SCRIPTMANAGER.script,
				scriptUri));
		resultGraph.add(new TripleImpl(resource, 
				RDF.type,
				SCRIPTMANAGER.SelectedScript));
		resultGraph.add(new TripleImpl(scriptUri,
				SCRIPTMANAGER.code, 
				new PlainLiteralImpl(
					new String(contentHandler.getData(scriptUri)))));

		GraphNode scriptLanguageList = getScriptLanguageList(resource);

		return new GraphNode(resource, new UnionMGraph(resultGraph,
				cgProvider.getContentGraph(), scriptLanguageList.getGraph()));
	}

	/**
	 * Returns the menu with available scripts as GraphNode.
	 *
	 * @param resource  The resource to which to attach the list.
	 * @return {@link GraphNode} containing all available script resources.
	 * 
	 */
	@GET
	@Path("script-list")
	@Produces("text/plain")
	public GraphNode getScriptList(
			@QueryParam(value = "resource") NonLiteral resource) {

		AccessController.checkPermission(new ScriptManagerAppPermission());
		if(resource == null) {
			resource = new BNode();
		}

		BNode resultResource = new BNode();
		MGraph contentGraph = cgProvider.getContentGraph();
		
		MGraph additionGraph = new SimpleMGraph();

		UnionMGraph resultGraph = new UnionMGraph(additionGraph, contentGraph);

		RdfList list = RdfList.createEmptyList(resultResource, additionGraph);
		resultGraph.add(new TripleImpl(resource,
				SCRIPTMANAGER.scriptList, resultResource));
		resultGraph.add(new TripleImpl(resultResource, RDF.type,
				SCRIPTMANAGER.ScriptList));
		
		Iterator<Triple> it =
				contentGraph.filter(null, RDF.type, SCRIPT.Script);
		while (it.hasNext()) {
			list.add(it.next().getSubject());
		}
		return new GraphNode(resultResource,
				new UnionMGraph(resultGraph, contentGraph));
	}

	/**
	 * The page that contains the install form.
	 *
	 * @return  a Graphnode containing a ScriptManagerInstallPage.
	 */
	@GET
	@Path("script-install")
	public GraphNode install() {

		AccessController.checkPermission(new ScriptManagerAppPermission());
		MGraph contentGraph = cgProvider.getContentGraph();
		BNode resultResource = new BNode();
		MGraph resultGraph = new SimpleMGraph();
		resultGraph.add(new TripleImpl(resultResource, 
				RDF.type,
				SCRIPTMANAGER.ScriptManagerInstallPage));
		resultGraph.add(new TripleImpl(resultResource,
				RDF.type, PLATFORM.HeadedPage));
		
		GraphNode languageList = getScriptLanguageList(resultResource);
		
		GraphNode scriptList = getScriptList(resultResource);
		
		UnionMGraph unionGraph = new UnionMGraph(resultGraph,
				scriptList.getGraph(), languageList.getGraph(), contentGraph);
		
		return new GraphNode(resultResource, unionGraph);
	}

	private GraphNode getScriptLanguageList(NonLiteral resource){
		MGraph resultGraph = new SimpleMGraph();
		Iterator<ScriptLanguageDescription> it =
				scriptExecution.getInstalledScriptLanguages();
		while(it.hasNext()){
			BNode languageDescription = new BNode();
			ScriptLanguageDescription sld = it.next();
			resultGraph.add(new TripleImpl(resource,
					SCRIPTMANAGER.scriptLanguageDescription,
					languageDescription));

			resultGraph.add(new TripleImpl(languageDescription, 
					SCRIPT.scriptLanguage, 
					LiteralFactory.getInstance().
					createTypedLiteral(sld.getLanguage())));
			resultGraph.add(new TripleImpl(languageDescription,
					SCRIPT.scriptLanguageVersion,
					LiteralFactory.getInstance().
					createTypedLiteral(sld.getVersion())));
		}
		return new GraphNode(resource, resultGraph);
	}

	/**
	 * Installs a script.
	 *
	 * @param form  the install form containing the script data.
	 * @return
	 *			BAD_REQUEST on invalid data, redirect to overview page otherwise.
	 *
	 */
	@POST
	@Consumes("multipart/form")
	@Path("install-script")
	public Response installScript(MultiPartBody form,
			@Context UriInfo uriInfo) {

		AccessController.checkPermission(new ScriptManagerAppPermission());
		TrailingSlash.enforceNotPresent(uriInfo);
		
		URI absolutePath = uriInfo.getAbsolutePath();
		final String baseUriString = absolutePath.getScheme() + "://" +
				absolutePath.getAuthority() + "/scripts/" +
				UUID.randomUUID().toString();
		UriRef scriptUri = new UriRef(baseUriString);
		int counter = 0;
		while (contentHandler.getData(scriptUri) != null) {
			counter++;
			scriptUri = new UriRef(baseUriString + "." + counter);
		}

		String scriptExecutionUri =
				form.getTextParameterValues("scriptExecutionUri")[0];
		String scriptLanguageAndVersion =
				form.getTextParameterValues("scriptLanguage")[0];
		String mediaType = form.getTextParameterValues("mediaType")[0];
		String producedType =
				form.getTextParameterValues("producedType")[0];

		ScriptLanguageDescription sld =
				extractLanguageAndVersion(scriptLanguageAndVersion);


		String fileChoice = form.getTextParameterValues("fileChoice")[0];
		String scriptName = "unnamed";

		byte[] scriptFileBytes = new byte[0];

		if(fileChoice.equals("file")) {
			FormFile formFile =
					form.getFormFileParameterValues("scriptFile")[0];
			scriptFileBytes = formFile.getContent();

			if (scriptFileBytes == null || (scriptFileBytes.length == 0)) {
				String message = "no script uploaded";
				logger.warn(message);
				throw new WebApplicationException(Response.status(
						Status.BAD_REQUEST).entity(message).build());
			}

			scriptName = formFile.getFileName();

			if(mediaType.trim().equals("")) {
				mediaType = formFile.getMediaType().toString();
			}
		} else if(fileChoice.equals("text")) {
			if(form.getTextParameterValues("scriptCode").length > 0) {
				scriptFileBytes = form.getTextParameterValues("scriptCode")[0].
						getBytes();
			}
			if(form.getTextParameterValues("scriptName").length > 0) {
				scriptName = form.getTextParameterValues("scriptName")[0];
				if(scriptName.trim().equals("")) {
					scriptName = "unnamed";
				}
			}
			if(mediaType.trim().equals("")) {
				mediaType = "text/plain";
			}
		}

		if(!scriptExecutionUri.trim().equals("")) {
			if(!saveExecutionUri(scriptExecutionUri, scriptUri)) {
				logger.warn("The execution URI {} is already used.",
						scriptExecutionUri);
				return Response.status(Status.BAD_REQUEST).build();
			}
		}
		saveScript(scriptUri, scriptFileBytes, scriptName,
				sld.getLanguage(), sld.getVersion(), mediaType, producedType);
		
		return RedirectUtil.createSeeOtherResponse("script-overview", uriInfo);
	}

	/**
	 * Updates a script.
	 *
	 * @param form  the update form containing the script data.
	 * @return   Redirect to overview page.
	 *
	 */
	@POST
	@Consumes("multipart/form")
	@Path("update-script")
	public Response updateScript(MultiPartBody form, @Context UriInfo uriInfo) {
		AccessController.checkPermission(new ScriptManagerAppPermission());
		UriRef scriptUri =
				new UriRef(form.getTextParameterValues("scriptUri")[0]);

		String scriptName = form.getTextParameterValues("fileName")[0];
		String scriptLanguageAndVersion =
				form.getTextParameterValues("scriptLanguage")[0];
		String mediaType = form.getTextParameterValues("mediaType")[0];
		String producedType =
				form.getTextParameterValues("producedType")[0];
		String scriptCode = form.getTextParameterValues("scriptCode")[0];

		ScriptLanguageDescription sld =
				extractLanguageAndVersion(scriptLanguageAndVersion);

		FormFile formFile =
				form.getFormFileParameterValues("scriptFile")[0];
		
		byte[] scriptFileBytes = formFile.getContent();



		if (scriptFileBytes == null || (scriptFileBytes.length == 0)) {
			scriptFileBytes = scriptCode.getBytes();
			if(mediaType.trim().equals("")) {
				mediaType = "text/plain";
			}
		} else {
			if(mediaType.trim().equals("")) {
				mediaType = formFile.getMediaType().toString();
			}
			scriptName = formFile.getFileName();
		}
		saveScript(scriptUri, scriptFileBytes, scriptName, sld.getLanguage(),
				sld.getVersion(), mediaType, producedType);

		return RedirectUtil.createSeeOtherResponse(
				"script-overview?script="+scriptUri.getUnicodeString(),
				uriInfo);
	}
	
	/**
	 * Deletes a script.
	 *
	 * @param  script the script URI.
	 * @return CREATED Responce if script has been deleted.
	 *
	 */
	@POST
	@Path("delete")
	public Response deleteScript(@FormParam("script") String script) {
		AccessController.checkPermission(new ScriptManagerAppPermission());
		UriRef scriptUri = new UriRef(script);
		

		//remove execution URIs
		Set<NonLiteral> scriptGeneratedResources =
				getScriptGeneratedResources(scriptUri);
		for(NonLiteral scriptGenratedResource : scriptGeneratedResources) {
			deleteExecutionUri(scriptGenratedResource, scriptUri);
		}

		//remove Script
		deleteScript(scriptUri);

		logger.info("script {} deleted", scriptUri);
		
		return Response.status(Status.CREATED).build();
	}


	private void saveScript(UriRef scriptUri, byte[] scriptFileBytes,
			String scriptName, String scriptLanguage,
			String scriptLanguageVersion, String mediaTypeString,
			String producedTypeString) {
		try {
			MediaType mediaType = MediaType.valueOf(mediaTypeString);

			MGraph contentGraph = cgProvider.getContentGraph();



			contentHandler.put(scriptUri, mediaType, scriptFileBytes);

			GraphNode scriptNode = new GraphNode(scriptUri, contentGraph);
			scriptNode.deleteProperties(DCTERMS.title);
			scriptNode.deleteProperties(SCRIPT.scriptLanguage);
			scriptNode.deleteProperties(SCRIPT.scriptLanguageVersion);
			scriptNode.deleteProperties(SCRIPT.producedType);
			scriptNode.addProperty(RDF.type, SCRIPT.Script);
			scriptNode.addProperty(DCTERMS.title,
					LiteralFactory.getInstance().
					createTypedLiteral(scriptName));
			scriptNode.addProperty(SCRIPT.scriptLanguage,
					LiteralFactory.getInstance().
					createTypedLiteral(scriptLanguage));
			scriptNode.addProperty(SCRIPT.scriptLanguageVersion,
					LiteralFactory.getInstance().
					createTypedLiteral(scriptLanguageVersion));
			if(!producedTypeString.equals("")) {
				scriptNode.addProperty(SCRIPT.producedType,
						LiteralFactory.getInstance().
							createTypedLiteral(
								MediaType.valueOf(producedTypeString).
									toString()));
			}

		} catch (IllegalArgumentException ex) {
			//either one of the media types is malformed
			//or the executionUri is not unique.
			throw new WebApplicationException(Response.status(
					Status.BAD_REQUEST).entity(ex.getMessage()).build());
		}
	}

	private void deleteScript(UriRef scriptUri) {
		MGraph contentGraph = cgProvider.getContentGraph();
		
		contentHandler.remove(scriptUri);
		GraphNode scriptNode = new GraphNode(scriptUri, contentGraph);
		scriptNode.deleteProperty(RDF.type, SCRIPT.Script);
		scriptNode.deleteProperties(DCTERMS.title);
		scriptNode.deleteProperties(SCRIPT.scriptLanguage);
		scriptNode.deleteProperties(SCRIPT.scriptLanguageVersion);
		scriptNode.deleteProperties(SCRIPT.producedType);
	}

	/**
	 * @return	false if the specified execution URI is already used,
	 *			true otherwise.
	 */
	private boolean saveExecutionUri(String scriptExecutionUri,
			UriRef scriptUri) {

		MGraph contentGraph = cgProvider.getContentGraph();

		if (!scriptExecutionUri.equals("")) {
			UriRef generatedResourceUri = new UriRef(scriptExecutionUri);
			if (!contentGraph.filter(generatedResourceUri, RDF.type,
					SCRIPT.ScriptGeneratedResource).hasNext()) {
				
				GraphNode generatedResourceNode =
						new GraphNode(generatedResourceUri, contentGraph);
				generatedResourceNode.addProperty(RDF.type,
						SCRIPT.ScriptGeneratedResource);
				generatedResourceNode.addProperty(SCRIPT.scriptSource,
						scriptUri);
			} else {
				return false;
			}
		}

		return true;
	}

	private void deleteExecutionUri(NonLiteral scriptGeneratedResource,
			UriRef scriptUri) {
		MGraph contentGraph = cgProvider.getContentGraph();

		GraphNode generatedResourceNode =
				new GraphNode(scriptGeneratedResource, contentGraph);
		generatedResourceNode.deleteProperty(RDF.type,
				SCRIPT.ScriptGeneratedResource);
		generatedResourceNode.deleteProperty(SCRIPT.scriptSource,
				scriptUri);
	}

	private ScriptLanguageDescription extractLanguageAndVersion(String str) {
		int begin = str.indexOf(" (");
		int end = str.lastIndexOf(')');

		String scriptLanguage = str.substring(0, begin);
		String scriptLanguageVersion =
				str.substring(begin + 2, end);

		return new ScriptLanguageDescription(scriptLanguage,
				scriptLanguageVersion);
	}

	/**
	 * Execution Uri overview page.
	 *
	 * @return A Graphnode containing a ExecutionUriOverviewPage.
	 */
	@GET
	@Path("execution-uri-overview")
	public GraphNode getExecutionUriOverview() {
		AccessController.checkPermission(new ScriptManagerAppPermission());
		MGraph contentGraph = cgProvider.getContentGraph();
		BNode resultResource = new BNode();
		MGraph resultGraph = new SimpleMGraph();
		resultGraph.add(new TripleImpl(resultResource,
				RDF.type,
				PLATFORM.HeadedPage));
		resultGraph.add(new TripleImpl(resultResource, 
				RDF.type,
				SCRIPTMANAGER.ExecutionUriOverviewPage));
		GraphNode scriptList = getScriptList(resultResource);
		
		UnionMGraph unionGraph = new UnionMGraph(resultGraph,
				scriptList.getGraph(), contentGraph);
		return new GraphNode(resultResource, unionGraph);
	}

	/**
	 * Returns the executionURIs as RdfList in a GraphNode.
	 *
	 * @param script  The script URI for which to show its execution URIs.
	 * @return  A Graphnode containing a ExecutionUriList.
	 */
	@GET
	@Path("get-execution-uri")
	@Produces("text/plain")
	public GraphNode getExecutionUris(
			@QueryParam(value = "script") UriRef script){
		AccessController.checkPermission(new ScriptManagerAppPermission());
		BNode resultResource = new BNode();
		MGraph resultGraph = new SimpleMGraph();
		Iterator<NonLiteral> executionUris =
				getScriptGeneratedResources(script).iterator();
		while(executionUris.hasNext()){
			resultGraph.add(new TripleImpl(resultResource,
					SCRIPTMANAGER.executionUri,
					executionUris.next()));
		}
		resultGraph.add(new TripleImpl(resultResource,
				SCRIPTMANAGER.script,
				script));
		resultGraph.add(new TripleImpl(resultResource,
				RDF.type,
				SCRIPTMANAGER.ExecutionUriList));
		
		return new GraphNode(resultResource, new UnionMGraph(resultGraph,
				cgProvider.getContentGraph()));
	}
	
	/**
	 * Adds a specified execution URI to a specified script.
	 *
	 * @param scriptUri  
	 *			the URI of the script to which the execution URI should be added.
	 * @param generatedResourceUri  
	 *			the execution URI to add.
	 * @return  Redirect to execution uri overview page.
	 */
	@POST
	@Path("add-execution-uri")
	public Response addExecutionUri(
			@FormParam( "scriptUri" ) UriRef scriptUri,
			@FormParam( "executionUri" ) String generatedResourceUri,
			@Context UriInfo uriInfo){
		AccessController.checkPermission(new ScriptManagerAppPermission());
		if(!saveExecutionUri(generatedResourceUri, scriptUri)) {
			logger.warn("Execution URI {} already used.", 
					generatedResourceUri);
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		return RedirectUtil.createSeeOtherResponse(
				"execution-uri-overview", uriInfo);
	}
	
	/**
	 * Deletes a specified executionUri for a given script.
	 *
	 * @param scriptUri
	 *			The URI of the script to which the execution URI belongs.
	 * @param generatedResourceUri
	 *			The execution URI to delete.
	 * @return  A CREATED response if the execution URI could be deleted.
	 */
	@POST
	@Path("delete-executionUri")
	public Response deleteExecutionUri(
			@FormParam( "scriptUri" ) UriRef scriptUri,
			@FormParam( "executionUri" ) UriRef generatedResourceUri){

		AccessController.checkPermission(new ScriptManagerAppPermission());
		deleteExecutionUri((NonLiteral) generatedResourceUri, scriptUri);

		return Response.status(Status.CREATED).build();
	}

	/**
	 * Returns all the execution URIs pointing to a specified script.
	 *
	 * @param scriptResource  the script (URI)
	 * @return  All the ScriptGeneratedResources pointing
	 *			to <code>scriptResource</code>.
	 */
	private Set<NonLiteral> getScriptGeneratedResources(
			NonLiteral scriptResource) {
		MGraph contentGraph = cgProvider.getContentGraph();

		Iterator<Triple> it = contentGraph.filter(null,
				SCRIPT.scriptSource,
				scriptResource);

		Set<NonLiteral> resources = new HashSet<NonLiteral>();

		while(it.hasNext()) {
			resources.add(it.next().getSubject());
		}
		return resources;
	}

	/**
	 * Executes a script.
	 *
	 * @param script
	 *			The script URI of the script that should be executed.
	 * @return
	 *			The script return value. Can be GraphNode or a response with
	 *			the media type of the script output.
	 * @throws NoEngineException
	 *			If no engine can be found to execute the script.
	 * @throws ScriptException
	 *			If an error occurs while executing the script.
	 *
	 * @see org.apache.clerezza.platform.scripting.ScriptExecution#execute(
	 *			org.apache.clerezza.rdf.core.NonLiteral, javax.script.Bindings)
	 */
	@GET
	@Path("execute")
	public Object executeScript(@QueryParam("script") String script) {
		AccessController.checkPermission(new ScriptManagerAppPermission());
		try {
			return scriptExecution.execute(new UriRef(script));
		} catch (NoEngineException ex) {
			throw new WebApplicationException(ex);
		} catch (ScriptException ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			pw.println("Exception executing script: ");
			if (ex.getLineNumber() != -1 || ex.getColumnNumber() != -1) {
				pw.print("at line number" + ex.getLineNumber() + " ");
				pw.print("at column number" + ex.getColumnNumber() + ": ");
			}
			pw.println(ex.getMessage());
			ex.printStackTrace(pw);
			pw.flush();
			return sw.toString();
		} 
	}

	/**
	 * Returns a PathNode of a static file from the staticweb folder.
	 * 
	 * @return {@link PathNode}
	 */
	@GET
	@Path("{path:.+}")
	public PathNode getStaticFile(@PathParam("path") String path) {
		final PathNode node = fileServer.getNode(path);
		logger.debug("Serving static {}", node);
		return node;
	}

	@Override
	public Set<GlobalMenuItem> getMenuItems() {
		Set<GlobalMenuItem> items = new HashSet<GlobalMenuItem>();
		try {
			AccessController.checkPermission(
					new TcPermission("urn:x-localinstance:/content.graph",
					TcPermission.READWRITE));
			AccessController.checkPermission(new ScriptManagerAppPermission());
		} catch (AccessControlException e) {
			return items;
		}
		items.add(new GlobalMenuItem("/admin/scripting/", "SCM", "Scripting", 1,
				"Development"));
		return items;
	}
}
