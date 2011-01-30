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
package org.apache.clerezza.web.resources.yui;

import java.io.IOException;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * Bundelized version of Yahoo UI . Bundles which use yui
 * should use this service (bundle depends on org.apache.clerezza.web.resources.yui.Yui)
 * 
 * 
 * @scr.component
 * @scr.service interface="java.lang.Object"
 * @scr.service interface="org.apache.clerezza.web.resources.yui.Yui"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true"
 * 
 * @author mkn
 */
@Path("/yui")
public class Yui {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private FileServer fileServer;

	/**
	 * The activate method is called when SCR activates the component
	 * configuration. 
	 * 
	 * @param componentContext
	 */
	protected void activate(ComponentContext context) {
		Bundle bundle = context.getBundleContext().getBundle();
		URL resourceDir = getClass().getResource("staticweb");
		PathNode pathNode = new BundlePathNode(bundle, resourceDir.getPath());
		logger.debug("Initializing file server for {} ({})", resourceDir,
				resourceDir.getFile());
		fileServer = new FileServer(pathNode);
	}

	/**
	 * Returns a PathNode of a static file from the staticweb folder.
	 * 
	 * @return {@link PathNode}
	 */
	@GET
	@Path("{path:.+}")
	public PathNode getStaticFile(@PathParam("path") String path) {
		logger.warn("Accessing deprecated path for yui2, use 2/{} instead.", path);
		return getYui2File(path);
	}

	@GET
	@Path("2/{path:.+}")
	public PathNode getYui2File(@PathParam("path") String path) {
		final PathNode node = fileServer.getNode("2/"+path);
		logger.debug("Serving static {}", node);
		return node;
	}
	
	@GET
	@Path("3/{path:.+}")
	public PathNode getYui3File(@PathParam("path") String path) {
		final PathNode node = fileServer.getNode("3/"+path);
		logger.debug("Serving static {}", node);
		return node;
	}

}
