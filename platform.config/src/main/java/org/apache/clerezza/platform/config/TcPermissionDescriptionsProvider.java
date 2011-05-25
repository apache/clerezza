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

package org.apache.clerezza.platform.config;

import java.util.HashSet;
import java.util.Set;
import org.apache.clerezza.permissiondescriptions.PermissionDescriptionsProvider;
import org.apache.clerezza.permissiondescriptions.PermissionDescripton;
import org.apache.clerezza.rdf.core.access.security.TcPermission;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * <code>PermissionDescriptionsProvider</code> implementation that provides
 * <code>PermissionDescripton</code>s of the graph access permissions for
 * the graphs used by the platform.
 * The access permissions for the following graphs are described:
 * <ul>
 *	<li>system graph</li>
 *	<li>config graph</li>
 * </ul>
 *
 * @author mir
 */
@Component
@Service(PermissionDescriptionsProvider.class)
public class TcPermissionDescriptionsProvider implements PermissionDescriptionsProvider {

	private static final Set<PermissionDescripton> GRAPH_ACCESS_PERMISSION_DESCRIPTIONS =
			new HashSet<PermissionDescripton>();
	static {
		GRAPH_ACCESS_PERMISSION_DESCRIPTIONS.add(new PermissionDescripton("System Graph Read Permission",
				"Grants permission to the user to read the system graph", null, TcPermission.class,
				"(org.apache.clerezza.rdf.core.access.security.TcPermission \"urn:x-localinstance:/system.graph\" \"read\")"));

		GRAPH_ACCESS_PERMISSION_DESCRIPTIONS.add(new PermissionDescripton("System Graph Read/Write Permission",
				"Grants permission to the user to read and write the system graph", null, TcPermission.class,
				"(org.apache.clerezza.rdf.core.access.security.TcPermission \"urn:x-localinstance:/system.graph\" \"readwrite\")"));

		GRAPH_ACCESS_PERMISSION_DESCRIPTIONS.add(new PermissionDescripton("Configuration Graph Read Permission",
				"Grants permission to the user to read the configuration graph", null, TcPermission.class,
				"(org.apache.clerezza.rdf.core.access.security.TcPermission \"urn:x-localinstance:/config.graph\" \"read\")"));

		GRAPH_ACCESS_PERMISSION_DESCRIPTIONS.add(new PermissionDescripton("Configuration Graph Read/Write Permission",
				"Grants permission to the user to read and write the configuration graph", null, TcPermission.class,
				"(org.apache.clerezza.rdf.core.access.security.TcPermission \"urn:x-localinstance:/config.graph\" \"readwrite\")"));
	}

	@Override
	public Set<PermissionDescripton> getPermissionDescriptors() {
		return GRAPH_ACCESS_PERMISSION_DESCRIPTIONS;
	}
}
