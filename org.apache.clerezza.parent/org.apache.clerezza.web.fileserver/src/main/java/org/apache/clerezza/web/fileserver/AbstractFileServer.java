/*
 *  Copyright 2010 reto.
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

package org.apache.clerezza.web.fileserver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * An abstract superclass of jax-rs resources serving files. Subclasses
 * overwrite the <code>getPathNode</code>-method to return the root of their 
 * hierachy.
 *
 * @author reto
 */
public abstract class AbstractFileServer {

	/**
	 * This method return the root of the served hierarchy. For example
	 * if the instance is to server all Files in /var/www, this method would
	 * return an instance of org.wymiwyg.commons.util.dirbrowser.FilePathNode
	 * initialized with "/var/www".
	 *
	 * @return the root of the served hierarchy
	 */
	protected abstract PathNode getRootNode();

	@Path("{path:.*}")
	@GET
	public PathNode getNode(@PathParam("path") String path) {
		String[] pathSections = path.split("/");
		PathNode current = getRootNode();
		for (String pathSection : pathSections) {
			current = current.getSubPath(pathSection);
			if (!current.exists()) {
				throw new WebApplicationException(404);
			}
		}
		return current;

	}

}
