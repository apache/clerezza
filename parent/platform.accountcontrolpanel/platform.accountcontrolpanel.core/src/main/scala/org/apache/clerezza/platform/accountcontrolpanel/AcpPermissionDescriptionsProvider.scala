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

import java.util.HashSet
import java.util.Set
import org.apache.clerezza.permissiondescriptions.PermissionDescriptionsProvider
import org.apache.clerezza.permissiondescriptions.PermissionDescripton
import org.apache.felix.scr.annotations.Component
import org.apache.felix.scr.annotations.Service

/**
 * <code>PermissionDescriptionsProvider</code> implementation that provides
 * <code>PermissionDescripton</code>s of Account Control Panel permissions.
 * The following permissions are described:
 * <ul>
 * 	<li>org.apache.clerezza.platform.accountcontrolpanel.AccountControlPanelAppPermission</li>
 * 	<li>org.apache.clerezza.platform.accountcontrolpanel.ChangePasswordPermission</li>
 * 	<li>org.apache.clerezza.platform.accountcontrolpanel.UserBundlePermission</li>
 * </ul>
 *
 * @author mir
 */
object AcpPermissionDescriptionsProvider {
	private  val ACP_PERMISSION_DESCRIPTIONS: Set[PermissionDescripton] = new HashSet[PermissionDescripton]
	ACP_PERMISSION_DESCRIPTIONS.add(
		new PermissionDescripton("Change Own Password Permission",
			"Grants permission to the user to change its own password",
			null, classOf[ChangePasswordPermission],
			"(org.apache.clerezza.platform.accountcontrolpanel.ChangePasswordPermission \"{username}\" \"\")"
		)
	)

	ACP_PERMISSION_DESCRIPTIONS.add(
		new PermissionDescripton("Access Own Account Control Panel Permission",
			"Grants permission to the user to access its own Account Control Panel",
			null, classOf[AccountControlPanelAppPermission],
			"(org.apache.clerezza.platform.accountcontrolpanel.AccountControlPanelAppPermission \"{username}\" \"\")"
		)
	)

	ACP_PERMISSION_DESCRIPTIONS.add(
		new PermissionDescripton("Bundle Upload Permission",
			"Grants permission to the user to upload a bundle",
			null, classOf[AccountControlPanelAppPermission],
			"(org.apache.clerezza.platform.accountcontrolpanel.UserBundlePermission \"{username}\" \"\")"
		)
	)

}

class AcpPermissionDescriptionsProvider extends PermissionDescriptionsProvider {

	import AcpPermissionDescriptionsProvider._

	def getPermissionDescriptors: Set[PermissionDescripton] = {
		return ACP_PERMISSION_DESCRIPTIONS
	}
}