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
import java.io.File;
import java.net.URL;
import javax.ws.rs.Path;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.wymiwyg.commons.util.dirbrowser.FilePathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * A utility class providing a FileServer that can be configured in different 
 * ways. It provides an alternative to {@link AbstractFileServer} for jax-rs
 * resources serviceng static files to override.
 *
 * @author reto
 */
@Path("/")
public class FileServer extends AbstractFileServer {
	private PathNode rootPathNode;

	/**
	 * Construct a new instance that has to be configured using one of the 
	 * configure-methods.
	 */
	public FileServer() {}

	/**
	 * creates a new instance configured with specified pathnode as root
	 *
	 * @param pathNode the root of the PathNode hierarchy
	 */
	public FileServer(PathNode pathNode) {
		this.rootPathNode = pathNode;
	}


	/**
	 * configures the instance with the specified PathNode
	 *
	 * @param pathNode the root of the PathNode hierarchy
	 */
	public void configure(PathNode pathNode) {
		this.rootPathNode = pathNode;
	}

	/**
	 * Configures the instance to use the 'staticweb' folder next to (sub-)class
	 * in the bundle associated to the specified context.
	 *
	 * @param context The scr component context of the bundle containing the
	 *		'staticweb' directory where the subclass is located.
	 */
	public void configure(BundleContext context) {
		configure(context, "staticweb");
	}

	/**
	 * Sets up a path in a bundle or file system to be exposed over a
	 * <code>org.apache.clerezza.web.fileserver.FileServer</code>. The path is
	 * relative to the locationof the class.
	 *
	 * @param context The bundle context of the bundle containing the path.
	 * @param path the path where the file are to be exposed
	 */
	public void configure(BundleContext context, String path) {
		PathNode pathNode;
		Bundle bundle = context.getBundle();
		URL resourceDir = getClass().getResource(path);
		pathNode = new BundlePathNode(bundle, resourceDir.getPath());
		configure(pathNode);
	}
	/**
	 * configures the instance to use the specified directory as root
	 *
	 * @param rootDir the root of the serverd hierarchy
	 */
	public void configure(File rootDir) {
		configure(new FilePathNode(rootDir));
	}

	/**
	 * resets the configuration
	 */
	public void reset() {
		rootPathNode = null;
	}

	@Override
	protected PathNode getRootNode() {
		return rootPathNode;
	}

}
