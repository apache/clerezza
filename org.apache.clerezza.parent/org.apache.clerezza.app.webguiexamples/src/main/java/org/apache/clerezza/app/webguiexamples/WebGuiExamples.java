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
package org.apache.clerezza.app.webguiexamples;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.osgi.service.component.ComponentContext;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 *
 * @author mhe, tio
 */
@Component
@Service(Object.class)
@Properties({@Property(name="javax.ws.rs", boolValue=true)})

@Path("/web-gui-examples")
public class WebGuiExamples {

	private FileServer fileServer;


	/**
	 * The activate method is called when SCR activates the component
	 * configuration.
	 *
	 * @param context
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected void activate(ComponentContext context) throws IOException,
			URISyntaxException {

		Bundle bundle = context.getBundleContext().getBundle();
		URL resourceDir = getClass().getResource("staticweb");
		PathNode pathNode = new BundlePathNode(bundle, resourceDir.getPath());
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
