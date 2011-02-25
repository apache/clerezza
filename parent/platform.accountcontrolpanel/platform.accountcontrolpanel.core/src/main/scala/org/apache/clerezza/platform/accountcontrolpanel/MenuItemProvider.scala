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
package org.apache.clerezza.platform.accountcontrolpanel

import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.AccessControlContext
import java.security.AccessControlException
import java.security.AccessController
import java.security.Principal
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction
import java.util.HashSet
import java.util.Iterator
import java.util.Set
import javax.security.auth.Subject
import org.apache.felix.scr.annotations.Component
import org.apache.felix.scr.annotations.Service
import org.apache.clerezza.platform.dashboard.GlobalMenuItem
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider
import org.apache.clerezza.platform.security.UserUtil

/**
 *
 * Provides menu-item to profile or settings-panel or none depending on user
 * permissions.
 *
 * @author reto
 */
class MenuItemProvider extends GlobalMenuItemsProvider {
	def getMenuItems: Set[GlobalMenuItem] = {
		var items: Set[GlobalMenuItem] = new HashSet[GlobalMenuItem]
		var userName: String = UserUtil.getCurrentUserName
		if (userName != null) {
			try {
				AccessController.checkPermission(new AccountControlPanelAppPermission(userName, ""))
			}
			catch {
				case e: AccessControlException => {
					return items
				}
			}
			try {
				var path: String = "/user/" + URLEncoder.encode(userName, "utf-8") + "/control-panel"
				items.add(new GlobalMenuItem(path, "ACP", "Account Control Panel", 5, "Administration"))
			}
			catch {
				case e: UnsupportedEncodingException => {
					throw new RuntimeException(e)
				}
			}
		}
		return items
	}
}