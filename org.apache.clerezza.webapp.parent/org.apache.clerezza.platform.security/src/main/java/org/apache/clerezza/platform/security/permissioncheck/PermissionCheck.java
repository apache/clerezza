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
package org.apache.clerezza.platform.security.permissioncheck;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.Permission;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.osgi.service.permissionadmin.PermissionInfo;

/**
 * This class provides a web service to check whether the current user owns a 
 * certain permission.
 *
 * The check-permission method is invoked with a GET-requests against
 * /security/check?permission=&lt;permission> where &lt;permission> is the
 * permissionString parameter of the checkPermission method.
 *
 *
 *
 * @scr.component
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true"
 * 
 * @author hasan
 */
@Path("/security")
public class PermissionCheck {

	/**
	 * Checks that the current user has the specified permission.
	 * 
	 * @param permissionString
	 *		java.security.Permission encoded as (type name actions),
	 *		e.g., (java.io.FilePermission "*" "read")
	 * @param uriInfo
	 */
	@GET
	@Path("check")
	public void checkPermission(
			@QueryParam(value = "permission") final String permissionString) {

		if (permissionString == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST).build());
		}
		PermissionInfo pi = new PermissionInfo(permissionString);
		final Permission permission;
		try {
			Constructor<?> constructor = Class.forName(pi.getType())
					.getConstructor(String.class, String.class);
			permission = (Permission) constructor
					.newInstance(pi.getName(), pi.getActions());
		} catch (Exception ex) {
			throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
		}
		AccessController.checkPermission(permission);
	}
}
