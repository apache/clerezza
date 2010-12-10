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
package org.apache.clerezza.platform.config.gui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.config.SystemConfig;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.UriRef;

import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.utils.GraphNode;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.security.TcPermission;

import org.apache.clerezza.rdf.ontologies.*;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * Provides a GUI to change platform default values.
 *
 * @author osr
 */
@Component
@Services({
	@Service(value = Object.class),
	@Service(value = GlobalMenuItemsProvider.class)
})
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/admin/configuration")
public class PlatformConfigGui implements GlobalMenuItemsProvider {

	private final static char SLASH = '/';
	@Reference
	private PlatformConfig platformConfig;
	@Reference
	private RenderletManager renderletManager;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final UriRef CONFIG_PAGE = new UriRef("http://clerezza.org/2009/08/platform#ConfigPage");
	private FileServer fileServer;

	protected void activate(ComponentContext context) throws IOException,
			URISyntaxException {
		Bundle bundle = context.getBundleContext().getBundle();
		URL resourceDir = getClass().getResource("staticweb");
		PathNode pathNode = new BundlePathNode(bundle, resourceDir.getPath());

		logger.debug("Initializing file server for {} ({})", resourceDir,
				resourceDir.getFile());

		fileServer = new FileServer(pathNode);

		URL template = getClass().getResource(
				"config.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(template.toURI().toString()),
				CONFIG_PAGE, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);

	}

	protected void deactivate(ComponentContext context) {
		fileServer = null;
	}

	/**
	 * Returns a headed page listing RSS Feeds. The optional parameter query is a
	 * search term, which tests if the term is contained in thee title or uri
	 * of the feed.
	 *
	 * @param query an optional parameter which specifies a search term criteria.
	 * The search term will be tested if contained in the title or the uri of the feed.
	 *
	 * @return {@link GraphNode}
	 *
	 */
	@GET
	public GraphNode getConfig() {
		AccessController.checkPermission(new ConfigGuiAccessPermission());
		GraphNode node = new GraphNode(new BNode(), new SimpleMGraph());
		node.addProperty(RDF.type, CONFIG_PAGE);
		node.addProperty(RDF.type, PLATFORM.HeadedPage);
		node.addProperty(PLATFORM.defaultBaseUri, platformConfig.getDefaultBaseUri());
		return node;

	}

	@POST
	public Response setConfig(@FormParam(value = "defaultBaseUri") String defaultBaseUri,
			@Context UriInfo uriInfo) {
		AccessController.checkPermission(new ConfigGuiAccessPermission());
		logger.debug("Setting base-uri to {}", defaultBaseUri);
		if (defaultBaseUri.charAt(defaultBaseUri.length() - 1) != SLASH) {
			defaultBaseUri += SLASH;
		}
		UriRef uri = new UriRef(defaultBaseUri);
		GraphNode node = platformConfig.getPlatformInstance();
		LockableMGraph sysGraph = (LockableMGraph) node.getGraph();
		Lock writeLock = sysGraph.getLock().writeLock();
		writeLock.lock();
		try {
			node.deleteProperties(PLATFORM.defaultBaseUri);
			node.addProperty(PLATFORM.defaultBaseUri, uri);
		} finally {
			writeLock.unlock();
		}
		return Response.status(Response.Status.ACCEPTED).build();
	}

	@Override
	public Set<GlobalMenuItem> getMenuItems() {
		Set<GlobalMenuItem> items = new HashSet<GlobalMenuItem>();
		try {
			AccessController.checkPermission(new ConfigGuiAccessPermission());
			AccessController.checkPermission(
					new TcPermission(SystemConfig.SYSTEM_GRAPH_URI.toString(),
					TcPermission.READWRITE));
		} catch (AccessControlException e) {
			return items;
		}
		items.add(new GlobalMenuItem("/admin/configuration", "Configuration", "Configuration", 2,
				"Administration"));
		return items;
	}

	/**
	 * Returns a PathNode of a static file from the staticweb folder.
	 *
	 * @param path specifies the path parameter
	 * @return {@link PathNode}
	 */
	@GET
	@Path("{path:.+}")
	public PathNode getStaticFile(
			@PathParam("path") String path) {
		return fileServer.getNode(path);


	}
}
