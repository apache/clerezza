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

package org.apache.clerezza.foafssl.testservlets

import org.apache.clerezza.platform.security.UserUtil
import org.apache.clerezza.platform.usermanager.UserManager
import javax.ws.rs.{Produces, GET, Path}
import org.osgi.service.component.ComponentContext
import org.apache.clerezza.foafssl.auth.X509Claim

/**
 * implementation of (very early) version of test server for WebID so that the following tests
 * can be checked.
 *
 * http://lists.w3.org/Archives/Public/public-xg-webid/2011Jan/0107.html
 */

@Path("/test/WebId")
class TestMe {

  var userManager: UserManager = null;

  protected def bindUserManager(um: UserManager) = {
    userManager = um
  }

  protected def unbindUserManager(um: UserManager) = {
    userManager = null
  }

  protected def activate(componentContext: ComponentContext) = {
    //		configure(componentContext.getBundleContext(), "profile-staticweb");
  }

  @GET
  @Produces(Array("text/plain"))
  def getTestMe(): String = {
    val subject = UserUtil.getCurrentSubject();
    val creds = subject.getPublicCredentials
    if (creds.size == 0) return "No public keys found"
    val cred = creds.iterator.next
    def outString(x509: X509Claim): String = {
      val res = for (id <- x509.webidclaims) yield {
        id.verified + " webid " + id.webId + " hasname " + id.userName
      }
      res.toString
    }

    return cred match {
      case x509: X509Claim => outString(x509)
      case other: AnyRef => "no X509 certificate found: found " + other.getClass()
    }
  }

  @GET
  @Path("x509")
  @Produces(Array("text/plain"))
  def getTestX509(): String = {
    val subject = UserUtil.getCurrentSubject();
    val creds = subject.getPublicCredentials
    if (creds.size == 0) return "No public keys found"
    return creds.iterator.next match {
      case x509: X509Claim => "X509 Certificate found. " + x509.cert.toString
      case other: Any => "no X509 certificate found: found " + other.getClass()
    }
  }


}
