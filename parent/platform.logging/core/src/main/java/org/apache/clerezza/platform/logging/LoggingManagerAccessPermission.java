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

package org.apache.clerezza.platform.logging;

import java.security.Permission;
import org.apache.clerezza.permissiondescriptions.PermissionInfo;

/**
 * Grants access to the logging manager. Additionally the user needs permission to
 * update the configuration via ConfigurationAdmin.
 *
 *
 * @author tio
 */
@PermissionInfo(value="Logging Manager Access Permission", description="Grants access " +
	"to the logging manager.")
public class LoggingManagerAccessPermission extends Permission {

	public LoggingManagerAccessPermission() {
		super("LoggingMangerAccess");
	}

	public LoggingManagerAccessPermission(String name, String actions) {
		super("LoggingMangerAccess");
	}

	@Override
	public boolean implies(Permission permission) {
		return (permission instanceof LoggingManagerAccessPermission);
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
		hash = 101 * hash + "LoggingMangerAccess".hashCode();
		return hash;
	}
}
