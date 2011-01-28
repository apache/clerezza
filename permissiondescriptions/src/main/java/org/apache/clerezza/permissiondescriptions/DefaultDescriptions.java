/*
 *  Copyright 2010 mir.
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

package org.apache.clerezza.permissiondescriptions;

import java.security.AllPermission;
import java.util.HashSet;
import java.util.Set;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * <code>PermissionDescriptionsProvider</code> implementation that provides
 * <code>PermissionDescripton</code>s of permissions of the java api and of 
 * the OSGi framework.
 * The following permissions are described:
 * <ul>
 *	<li>java.security.AllPermission</li>
 *
 * </ul>
 *
 *
 * @author mir
 */
@Component
@Service(PermissionDescriptionsProvider.class)
public class DefaultDescriptions implements PermissionDescriptionsProvider {
	
	private Set<PermissionDescripton> defaultDescriptions = new HashSet<PermissionDescripton>();
	{
		defaultDescriptions.add(new PermissionDescripton("All permissions", 
				"Grants all permissions", null, AllPermission.class,
				"(java.security.AllPermission \"\" \"\")"));
	}

	@Override
	public Set<PermissionDescripton> getPermissionDescriptors() {		
		return defaultDescriptions;		
	}

}
