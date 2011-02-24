/*
 * Copyright 2011 hjs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.clerezza.foafssl.testservlet

import java.security.AccessController
import java.security.PrivilegedAction
import org.apache.clerezza.platform.security.UserUtil
import org.apache.clerezza.platform.usermanager.UserManager
import org.apache.felix.scr.annotations.Reference;
import javax.ws.rs._

@Path("/test/webIdEndPoint")
class TestMe {

  @Reference
	var userManager: UserManager;

  @GET
  def getPersonalProfilePage() = {
    val context = AccessController.getContext();
    val agent = AccessController.doPrivileged(new PrivilegedAction[GraphNode]() {
    			@Override
			def run(): GraphNode = {
				val userName = UserUtil.getUserName(context);
				if (userName == null) {
					return null;
				}
				return userManager.getUserGraphNode(userName);
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
  }

}
