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

package org.apache.clerezza.platform.typerendering.manager;


import java.security.Permission;
import org.apache.clerezza.permissiondescriptions.PermissionInfo;

/**
 * Permission to use the Renderlet Manager. Note that the user
 * additionally needs permission write into the content graph.
 *
 * @author tio
 */
@PermissionInfo(value="Renderlet Manager Access Permission", description="Grants access " +
	"to the Renderlet Manager page")
public class RenderletManagerAccessPermission extends Permission {

	public RenderletManagerAccessPermission() {
		super("RenderletManagerAccess");
	}

	public RenderletManagerAccessPermission(String name, String actions) {
		super("RenderletManagerAccess");
	}

	@Override
	public boolean implies(Permission permission) {
		return (permission instanceof RenderletManagerAccessPermission);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return true;
	}

	@Override
	public String getActions() {
		return "";
	}

	@Override
	public int hashCode() {
		int hash = 13;
		hash = 101 * hash + "RenderletManagerAccess".hashCode();
		return hash;
	}
}
