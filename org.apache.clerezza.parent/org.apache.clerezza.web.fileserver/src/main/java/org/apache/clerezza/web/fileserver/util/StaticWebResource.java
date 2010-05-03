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

package org.apache.clerezza.web.fileserver.util;

import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.wymiwyg.commons.util.dirbrowser.FilePathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * A JAX-RS that exposes all files in the "staticweb" folder over 
 * <code>FileServer</code>.
 *
 * @author mir
 */
public class StaticWebResource {

	private FileServer fileServer;

	/**
	 * Sets up the 'staticweb' folder in the bundle associated to the specified
	 * scr component context.
	 * 
	 * @param context The scr component context of the bundle containing the
	 *		'staticweb' path.
	 */
	protected void setupStaticWeb(ComponentContext context) {
		setupStaticWeb(context, "staticweb", false);
	}
	
	/**
	 * Sets up a path in a bundle or file system to be exposed over a 
	 * <code>org.apache.clerezza.web.fileserver.FileServer</code>. You can 
	 * specify over the 'local' parameter if the specified path is within a 
	 * bundle (false) or file system (true).	 * 
	 * 
	 * @param context The scr component context of the bundle containing the path. 
	 *		Only needed if 'local' is false.
	 * @param path the path where the file are to be exposed
	 * @param local specifies if the path is within a bundle or the file system.
	 */
	protected void setupStaticWeb(ComponentContext context, String path, boolean local) {
		PathNode pathNode;
		if (local) {
			pathNode = new FilePathNode(path);
		} else {
			Bundle bundle = context.getBundleContext().getBundle();
			URL resourceDir = getClass().getResource(path);
			pathNode = new BundlePathNode(bundle, resourceDir.getPath());
		}
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
		final PathNode node = fileServer.getNode(path);
		return node;
	}
}
