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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
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
import org.wymiwyg.commons.util.dirbrowser.MultiPathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * Serves file located under "META-INF/static-web" in bundles at the path "/static".
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

	protected void activate(ComponentContext context) throws IOException,
			URISyntaxException {

		for (Bundle bundle : context.getBundleContext().getBundles()) {
			registerStaticFiles(bundle);
		}
		updateFileServer();
		context.getBundleContext().addBundleListener(this);		
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
		MultiPathNode multiPathNode = new MultiPathNode(bundleNodeMap.values().
				toArray(new PathNode[bundleNodeMap.size()]));
		fileServer = new FileServer(multiPathNode);
	}
}
