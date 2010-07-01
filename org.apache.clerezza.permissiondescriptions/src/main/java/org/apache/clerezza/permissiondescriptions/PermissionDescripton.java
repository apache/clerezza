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

package org.apache.clerezza.permissiondescriptions;

import java.net.URL;
import java.security.Permission;

/**
 * The implementation of <code>PermissionDescription</code> used by the
 * <code>PersmissionDescriptionsProvider</code>
 *
 * @author mir
 */
public class PermissionDescripton {

	private String name, description, javaPermissionString;
	private URL iconUri;

	public PermissionDescripton(String name, String description, URL iconUri,
			Class<? extends Permission> permissionClassName, String javaPermissionString) {
		this.name = name;
		this.description = description;
		this.javaPermissionString = javaPermissionString;
		this.iconUri = iconUri;
	}

	/**
	 * Returns a description about the permission.
	 *
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns an icon URI that describes pictorgraphically what the permission
	 * in its current configuration does. E.g. the icon of a file read permission
	 * may depict an eye, while the icon for a file write permission might be a pencil.
	 *
	 * @return
	 */
	public URL getIcon () {
		return iconUri;
	}

	/**
	 * The name of the permission in a human comprehensible form.
	 *
	 * @return
	 */
	public String getSimpleName() {
		return name;
	}

	/**
	 * Returns a string describing this Permission. It has to be formated according
	 * the java.security.Permission toString()-convention. The convention is to specify
	 * the class name, the permission name, and the actions in the following format:
	 * '("ClassName" "name" "actions")'.
	 * @return
	 */
	public String getJavaPermissionString() {
		return javaPermissionString ;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PermissionDescripton other = (PermissionDescripton) obj;
		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
			return false;
		}
		if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
			return false;
		}
		if ((this.javaPermissionString == null) ? (other.javaPermissionString != null) : !this.javaPermissionString.equals(other.javaPermissionString)) {
			return false;
		}
		if (this.iconUri != other.iconUri && (this.iconUri == null || !this.iconUri.equals(other.iconUri))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 41 * hash + (this.name != null ? this.name.hashCode() : 0);
		hash = 41 * hash + (this.description != null ? this.description.hashCode() : 0);
		hash = 41 * hash + (this.javaPermissionString != null ? this.javaPermissionString.hashCode() : 0);
		hash = 41 * hash + (this.iconUri != null ? this.iconUri.hashCode() : 0);
		return hash;
	}
}
