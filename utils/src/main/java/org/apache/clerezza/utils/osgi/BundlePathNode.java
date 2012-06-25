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
package org.apache.clerezza.utils.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import org.osgi.framework.Bundle;
import org.wymiwyg.commons.util.dirbrowser.PathNameFilter;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 *
 * @author reto
 */
public class BundlePathNode implements PathNode {

	Bundle bundle;
	String path;

	public BundlePathNode(Bundle bundle, String path) {
		this.bundle = bundle;
		if (path.charAt(0) == '/') {
			this.path = path.substring(1);
		} else {
			this.path = path;
		}
	}

	@Override
	public PathNode getSubPath(String requestPath) {
		StringWriter mergedPath = new StringWriter(255);
		mergedPath.append(path);
		if ((!path.isEmpty()) && (path.charAt(path.length() - 1) != '/')) {
			mergedPath.append('/');
		}
		if (requestPath.charAt(0) == '/') {
			mergedPath.append(requestPath.substring(1));
		} else {
			mergedPath.append(requestPath);
		}
		return new BundlePathNode(bundle, mergedPath.toString());
	}

	@Override
	public boolean isDirectory() {
		//empty directories are not recognized
		String normalizedPath;
		if (path.charAt(path.length() - 1) != '/') {
			normalizedPath = path + "/";
		} else {
			normalizedPath = path;
		}
		return bundle.getEntryPaths(normalizedPath) != null;
	}

	@Override
	public String[] list(PathNameFilter filter) {
		List<String> resultList = new ArrayList<String>();
		String[] unfilterd = list();
		for (String entry : unfilterd) {
			if (filter.accept(this, entry)) {
				resultList.add(entry);
			}
		}
		return resultList.toArray(new String[resultList.size()]);
	}

	@Override
	public String[] list() {
		List<String> resultList = new ArrayList<String>();
		Enumeration<String> absPathEnum = bundle.getEntryPaths(path);
		if (absPathEnum != null) {
			final int pathLength = path.length();
			while (absPathEnum.hasMoreElements()) {
				String absPath = absPathEnum.nextElement();
				resultList.add(absPath.substring(pathLength));
			}
		}
		return resultList.toArray(new String[resultList.size()]);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return bundle.getEntry(path).openStream();
	}

	@Override
	public long getLength() {
		return -1;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public Date getLastModified() {
		return null;
	}

	@Override
	public boolean exists() {
		return (bundle.getEntry(path) != null);
	}

	@Override
	public String toString() {
		return "BundlePathNode for Path " + path + " in bundle " + bundle;
	}
}
