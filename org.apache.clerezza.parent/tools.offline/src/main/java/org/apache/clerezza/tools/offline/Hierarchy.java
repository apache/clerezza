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
package org.apache.clerezza.tools.offline;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.wymiwyg.commons.util.dirbrowser.PathNameFilter;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

class Hierarchy implements PathNode {

	static class ValueNode implements PathNode {
		private byte[] data;
		private String path;

		public ValueNode(String path, byte[] data) {
			this.data = data;
			this.path = path;
		}

		@Override
		public PathNode getSubPath(String requestPath) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean isDirectory() {
			return false;
		}

		@Override
		public String[] list(PathNameFilter filter) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String[] list() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}

		@Override
		public long getLength() {
			return data.length;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public Date getLastModified() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public boolean exists() {
			return true;
		}

	}

	private Map<String, PathNode> childMap = new HashMap<String, PathNode>();
	private String path;

	Hierarchy() {
		this.path = "";
	}

	Hierarchy(String path) {
		this.path = path;
	}

	/**
	 * creates a direct or indirect child
	 */
	void addChild(String subPath, byte[] data) {
		final int slashPos = subPath.indexOf('/');
		if (slashPos == -1) {
			childMap.put(subPath, new ValueNode(path+"/"+subPath, data));
		} else {
			String subPath1 = subPath.substring(0, slashPos);
			String subPath2 = subPath.substring(slashPos+1);
			PathNode directChild = childMap.get(subPath1);
			if (directChild == null) {
				directChild = new Hierarchy(path+"/"+subPath1);
				childMap.put(subPath1, directChild);
			} else {
				if (!(directChild instanceof Hierarchy)) {
					throw new RuntimeException("Attempt to add subPath "+
							subPath+" but "+directChild+" has a value");
				}
			}
			((Hierarchy)directChild).addChild(subPath2, data);
		}
	}

	@Override
	public PathNode getSubPath(String subPath) {
		return childMap.get(subPath);
	}

	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
	public String[] list(PathNameFilter filter) {
		String[] fullList = list();
		ArrayList resultList = new ArrayList();
		for (int i = 0; i < fullList.length; i++) {
			if (filter.accept(this, fullList[i])) {
				resultList.add(fullList[i]);
			}
		}

		return (String[]) resultList.toArray(new String[resultList.size()]);
	}

	@Override
	public String[] list() {
		final Set<String> keySet = childMap.keySet();
		return keySet.toArray(new String[keySet.size()]);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
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
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean exists() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
