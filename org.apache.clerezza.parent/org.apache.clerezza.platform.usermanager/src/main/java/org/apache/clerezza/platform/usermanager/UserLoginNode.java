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
package org.apache.clerezza.platform.usermanager;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.clerezza.platform.security.UserUtil;
import org.apache.clerezza.rdf.core.NonLiteral;

import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.platform.typerendering.UserContextProvider;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.ontologies.PLATFORM;

/**
 * The login name is added to the user context node. The name is accessable via
 * ssp template by using the context node
 * (e.g. context/platform("user")/platform("userName")).
 *
 * @author tio
 */
@Component(enabled=true, immediate=true)
@Service(UserContextProvider.class)

public class UserLoginNode implements UserContextProvider {

	@Reference
	protected UserManager userManager;

	@Override
	public GraphNode addUserContext(GraphNode node) {

		final AccessControlContext context = AccessController.getContext();
		GraphNode agent = AccessController.doPrivileged(new PrivilegedAction<GraphNode>() {
			@Override
			public GraphNode run() {
				final String userName = UserUtil.getUserName(context);
				if (userName == null) {
					return null;
				}
				return userManager.getUserGraphNode(userName);
			}
		});
		if (agent != null) {
			if (!(node.getObjects(PLATFORM.user).hasNext())) {
				node.addProperty(PLATFORM.user, agent.getNode());
			} else {
				Resource user = node.getObjects(PLATFORM.user).next();
				agent.replaceWith((NonLiteral) user);
			}
			node.getGraph().addAll(agent.getGraph());
		}
		return node;
	}
}
