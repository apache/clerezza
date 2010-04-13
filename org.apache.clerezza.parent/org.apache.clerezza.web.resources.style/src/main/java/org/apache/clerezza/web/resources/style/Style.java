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
package org.apache.clerezza.web.resources.style;

import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import javax.ws.rs.core.MediaType;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * Bundlized version oficons. Bundles which uses these icons
 * should use this service (bundle depends on org.apache.clerezza.web.resources.style.Style)
 *
 * @author tio
 */
@Component
@Services({
	@Service(Object.class),
	@Service(Style.class)}
)
@Property(name="javax.ws.rs", boolValue=true)
@Path("/style")
public class Style {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private FileServer fileServer;

	@Reference
	private RenderletManager renderletManager;

	/**
	 * configuration.
	 *
	 * @param context
	 */
	protected void activate(ComponentContext context) {
		Bundle bundle = context.getBundleContext().getBundle();
		URL resourceDir = getClass().getResource("staticweb");
		PathNode pathNode = new BundlePathNode(bundle, resourceDir.getPath());
		logger.debug("Initializing file server for {} ({})", resourceDir,
				resourceDir.getFile());
		fileServer = new FileServer(pathNode);
		URL templateURL = getClass().getResource("globalmenu-naked.ssp");

		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(templateURL.toString()), RDFS.Resource,
				"menu", MediaType.APPLICATION_XHTML_XML_TYPE, true);

		templateURL = getClass().getResource("headed-page-template.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(templateURL.toString()), PLATFORM.HeadedPage, "(?!.*naked).*",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);

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
		return node;
	}
}
