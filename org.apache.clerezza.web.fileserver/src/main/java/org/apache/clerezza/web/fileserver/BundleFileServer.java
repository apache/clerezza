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
package org.apache.clerezza.web.fileserver;

import org.apache.clerezza.utils.osgi.BundlePathNode;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.ComponentContext;
import org.wymiwyg.commons.util.dirbrowser.FilePathNode;
import org.wymiwyg.commons.util.dirbrowser.MultiPathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * Serves file located under "META-INF/static-web" in bundles at the path "/static".
 * As well as from the file path specified by the framework property
 * <code>org.apache.clerezza.web.fileserver.static.extra</code>
 *
 * @author rbn, mir
 */
@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("static")
public class BundleFileServer implements BundleListener {
	private volatile FileServer fileServer;
	private Map<Bundle, PathNode> bundleNodeMap =
			Collections.synchronizedMap(new HashMap<Bundle, PathNode>());
	//an option path for addditional static files
	private String extraPath;

	protected void activate(ComponentContext context) throws IOException,
			URISyntaxException {

		for (Bundle bundle : context.getBundleContext().getBundles()) {
			registerStaticFiles(bundle);
		}
		context.getBundleContext().addBundleListener(this);
		extraPath = context.getBundleContext().getProperty("org.apache.clerezza.web.fileserver.static.extra");
		updateFileServer();
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		Bundle bundle = event.getBundle();
		switch (event.getType()) {
			case BundleEvent.STARTED:
				registerStaticFiles(bundle);
				break;
			case BundleEvent.STOPPED:
				unregisterStaticFiles(bundle);
				break;
		}
		updateFileServer();
	}

	private void registerStaticFiles(Bundle bundle) {
		PathNode pathNode = new BundlePathNode(bundle, "META-INF/static-web");
		bundleNodeMap.put(bundle, pathNode);
	}

	private void unregisterStaticFiles(Bundle bundle) {
		bundleNodeMap.remove(bundle);
	}

	@GET
	@Path("{path:.+}")
	public PathNode getStaticFile(@PathParam("path") String path) {
		final PathNode node = fileServer.getNode(path);
		return node;
	}

	private void updateFileServer() {
		final Collection<PathNode> nodes = new HashSet<PathNode>(bundleNodeMap.values());
		if (extraPath != null) {
			nodes.add(new FilePathNode(extraPath));
		}
		MultiPathNode multiPathNode = new MultiPathNode(nodes.
				toArray(new PathNode[nodes.size()]));
		fileServer = new FileServer(multiPathNode);
	}
}
