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

package org.apache.clerezza.platform.config.gui;

import java.security.Permission;
import org.apache.clerezza.permissiondescriptions.PermissionInfo;

/**
 * Permission to use the Config GUI. Note that the user
 * additionally needs permission write into the system graph.
 *
 * @author tio
 */
@PermissionInfo(value="Config GUI Access Permission", description="Grants access " +
	"to the Config GUI")
public class ConfigGuiAccessPermission extends Permission{

	public ConfigGuiAccessPermission() {
		super("Config GUI Access permission");
	}
	/**
	 *
	 * @param target ignored
	 * @param action ignored
	 */
	public ConfigGuiAccessPermission(String target, String actions) {
		super("Config GUI Access permission");
	}

	@Override
	public boolean implies(Permission permission) {
		return equals(permission);
	}

	@Override
	public boolean equals(Object obj) {
		return getClass().equals(obj.getClass());
	}

	@Override
	public int hashCode() {
		return 2177987;
	}

	@Override
	public String getActions() {
		return "";
	}
}
