/*
 *  Copyright 2010 mir.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.web.fileserver.util;

import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.apache.felix.scr.annotations.Reference;
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

	@Reference
	private FileServer fileServer;
	
	protected String getFilePath() {
		return null;
	}

	protected void activate(ComponentContext context) throws Exception{
	Bundle bundle = context.getBundleContext().getBundle();
		URL resourceDir = getClass().getResource("staticweb");
		PathNode pathNode;
		if (getFilePath() != null) {
			pathNode = new FilePathNode(getFilePath());
		} else {
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
