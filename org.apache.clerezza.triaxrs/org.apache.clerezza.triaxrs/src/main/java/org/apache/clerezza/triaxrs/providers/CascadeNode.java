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
package org.apache.clerezza.triaxrs.providers;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hasan
 */
class CascadeNode {
	
	private Map<String, CascadeNode> children = new HashMap<String, CascadeNode>();
	private ProvidersImpl providers = new ProvidersImpl();

	/**
	 * returns a direct child
	 * 
	 * @param childName may not contain a slash ('/')
	 * @return
	 */
	CascadeNode getChild(String childName) {
		return children.get(childName);
	}

	/**
	 * determines whether the node has any child
	 * 
	 * @return true, if the node has any child, otherwise returns false
	 */
	boolean hasChild() {
		return !children.isEmpty();
	}

	/**
	 * creates a new child if it does not exist
	 * @param childName
	 * @return the newly created child or an existing one
	 */
	CascadeNode createChild(String childName) {
		CascadeNode result = children.get(childName);
		if (result == null) {
			result = new CascadeNode();
			children.put(childName, result);
		}
		return result;
	}

	/**
	 * deletes a child of a specified name
	 * 
	 * @param childName
	 */
	void deleteChild(String childName) {
		children.remove(childName);
	}
	
	ProvidersImpl getProviders() {
		return providers;
	}
}
