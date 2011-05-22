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
package org.apache.clerezza.platform.dashboard;

import org.apache.clerezza.platform.security.UserUtil;
import org.apache.clerezza.platform.typerendering.UserContextProvider;
import org.apache.clerezza.platform.usermanager.UserManager;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.PERMISSION;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;

import javax.security.auth.Subject;

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
				final Subject subject = UserUtil.getSubject(context);
				if (subject == null) {
					return null;
				}
				return userManager.getUserGraphNode(subject);
			}
		});
		if (agent != null) {
			node.addProperty(PLATFORM.user, agent.getNode());
			MGraph userContext = new SimpleMGraph(agent.getNodeContext());
			removeTripleWithProperty(userContext, PERMISSION.password);
			removeTripleWithProperty(userContext, PERMISSION.passwordSha1);
			node.getGraph().addAll(userContext);			
		}
		return node;
	}

	private void removeTripleWithProperty(MGraph userContext, UriRef property) {
		Iterator<Triple> propertyTriples = userContext.filter(null, property, null);
		while (propertyTriples.hasNext()) {
			propertyTriples.next();
			propertyTriples.remove();
		}
	}
}
