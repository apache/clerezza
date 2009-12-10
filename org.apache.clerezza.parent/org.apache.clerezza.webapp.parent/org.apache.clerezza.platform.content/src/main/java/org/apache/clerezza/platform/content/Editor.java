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
package org.apache.clerezza.platform.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;

import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * This Jax-rs root resource provides an ajax edito ro edit content structured
 * using the Discobits ontology
 *
 * @author rbn
 * @scr.component
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true"
 *
 */
@Path("tools/editor")
public class Editor {
	
	/**
	 * @scr.reference
	 */
	private ContentGraphProvider cgProvider;

	/**
	 * @scr.reference
	 */
	private TcManager tcManager;

	
	private static final Logger logger = LoggerFactory.getLogger(Editor.class);
	private FileServer fileServer;
	private Providers providers;
	private final MediaType rdfXmlType = MediaType.valueOf("application/rdf+xml");
	
	
	/**
	 * On activation the {@link FileServer} is prepared
	 *
	 * @param context
	 */
	protected void activate(ComponentContext context) {
		Bundle bundle = context.getBundleContext().getBundle();
		URL directoryRes = DiscobitsTypeHandler.class.getResource("staticweb");
		PathNode pathNode = new BundlePathNode(bundle, directoryRes.getPath());
		logger.info("initializing fileserver for {} ({})", directoryRes, directoryRes.getFile());
		fileServer = new FileServer(pathNode);
	}

	
	/**
	 * The providers are injected by the Jax-rs implementation and used to
	 * locate readers for the RDF content of the body
	 *
	 * @param providers
	 */
	@Context
	public void setProviders(Providers providers) {
		this.providers = providers;
	}


	@GET
	@Path("get")
	public GraphNode getDiscobit(@QueryParam("resource") UriRef uri,
			@QueryParam("graph") UriRef graphUri) {
		final MGraph mGraph = graphUri == null ? cgProvider.getContentGraph() :
			tcManager.getMGraph(graphUri);
		return new GraphNode(uri, mGraph);
	}
	/*
	 * note that without specifying consumes the subsequent method using
	 * @FormParam is never selected
	 */
	@POST
	@Path("post")
	@Consumes({"application/rdf+xml", "text/rdf+n3","application/n-triples","application/turtle","application/n3","text/n3","text/turtle"})
	public void postDiscobit(@QueryParam("resource") UriRef uri, 
			@QueryParam("graph") UriRef graphUri, Graph graph) {
		final MGraph mGraph = graphUri == null ? cgProvider.getContentGraph() :
			tcManager.getMGraph(graphUri);
		new SimpleDiscobitsHandler(mGraph).remove(uri);
		mGraph.addAll(graph);
	}
	
	@POST
	@Path("post")
	public void postDiscobit(@QueryParam("resource") UriRef uri,
			@QueryParam("graph") UriRef graphUri,
			@FormParam("assert") String assertedString) {
		MessageBodyReader<Graph> graphReader = providers.getMessageBodyReader(Graph.class, Graph.class, null,rdfXmlType);
		final Graph assertedGraph;
		try {
			assertedGraph = graphReader.readFrom(Graph.class, Graph.class, new Annotation[0], rdfXmlType, null, new ByteArrayInputStream(assertedString.getBytes()));			
		} catch (IOException ex) {
			logger.error("reading graph {}", ex);
			throw new WebApplicationException(ex, 500);
		}
		postDiscobit(uri, graphUri, assertedGraph);
	}

	@GET
	public PathNode getStaticFile(@Context UriInfo uriInfo) {
		TrailingSlash.enforcePresent(uriInfo);
		final PathNode node = fileServer.getNode("disco.xhtml");
		logger.debug("serving static {}", node);
		return node;
	}
	
	@GET
	@Path("{path:.+}")
	public PathNode getStaticFile(@PathParam("path") String path) {
		final PathNode node = fileServer.getNode(path);
		logger.debug("serving static {}", node);
		return node;
	}
}
