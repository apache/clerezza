package org.apache.clerezza.foafssl.testservlets

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

import java.security.AccessController
import java.security.PrivilegedAction
import org.apache.clerezza.platform.security.UserUtil
import org.apache.clerezza.platform.usermanager.UserManager
import org.apache.clerezza.rdf.utils.GraphNode
import sun.management.resources.agent
import javax.ws.rs.{Produces, GET, Path}
import org.apache.clerezza.web.fileserver.FileServer
import org.osgi.service.component.ComponentContext

/**
 * implementation of (very early) version of test server for WebID so that the following tests
 * can be checked.
 *
 * http://lists.w3.org/Archives/Public/public-xg-webid/2011Jan/0107.html
 */

@Path("/test/webIdEndPoint")
class TestMe extends FileServer {

  var userManager: UserManager =null;

  protected def bindUserManager(um: UserManager)  = {
		userManager = um
	}

	protected def unbindUserManager(um: UserManager)  = {
		userManager = null
	}

 	protected def activate(componentContext: ComponentContext ) = {
//		configure(componentContext.getBundleContext(), "profile-staticweb");
	}

  @GET @Produces(Array("text/plain"))
  def getTestMe() : String = {
    return "Hello World!"
//    val context = AccessController.getContext();
//    val agent = AccessController.doPrivileged(new PrivilegedAction[String]() {
//      @Override
//      def run(): String = {
//        val userName = UserUtil.getUserName(context);
//        if (userName == null) {
//          return "-";
//        }
//        val node: GraphNode = userManager.getUserGraphNode(userName)
//        return node.getNode.toString;
//      }
//    });
 //   return agent
  }



}
