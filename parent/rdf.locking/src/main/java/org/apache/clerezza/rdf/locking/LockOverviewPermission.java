/*
 *  Copyright 2010 reto.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.rdf.locking;

import java.security.Permission;
import org.apache.clerezza.permissiondescriptions.PermissionInfo;

/**
 *
 * @author reto
 */
@PermissionInfo(value="Lock Overview Permission", description="Grants access " +
	"to the Lock overview Pafe")
class LockOverviewPermission extends Permission{

	public LockOverviewPermission() {
		super("LockOverviewPermission");
	}
	/**
	 *
	 * @param target ignored
	 * @param action ignored
	 */
	public LockOverviewPermission(String target, String actions) {
		super("LockOverviewPermission");
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
		return 17587;
	}

	@Override
	public String getActions() {
		return "";
	}
}