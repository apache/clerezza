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

package org.apache.clerezza.foafssl.auth

import org.apache.clerezza.platform.security.auth._
import org.apache.clerezza.rdf.core._
import access.LockableMGraph
import impl.{TripleImpl, PlainLiteralImpl, SimpleMGraph}
import org.wymiwyg.wrhapi.Request
import org.wymiwyg.wrhapi.Response
import javax.security.auth.Subject
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.rdf.ontologies.{FOAF, RDF, PLATFORM}
import org.apache.clerezza.platform.users.WebIdGraphsService
import org.slf4j.LoggerFactory
import java.util.Collections
import org.apache.clerezza.platform.security.UserUtil
import org.apache.clerezza.rdf.scala.utils.EasyGraph


object FoafSslAuthentication {
  final private val logger = LoggerFactory.getLogger(classOf[FoafSslAuthentication])

  final val ANONYMOUS: String = "anonymous"

  def createSystemUserDescription(claim: WebIDClaim): MGraph = {
    val result = new EasyGraph()
	 import org.apache.clerezza.rdf.scala.utils.EasyGraph._
    result.addType(claim.webId, FOAF.Agent)
	  //add(claim.webId, PLATFORM.userName,new String(claim.webId.hashCode())).
    result
  }


}


/**
 * Here we no longer care about verifying the web-id claims as this should
 * already have been done by X509TrustManagerWrapperService
 */
class FoafSslAuthentication extends WeightedAuthenticationMethod {

  import FoafSslAuthentication._
  import collection.JavaConversions._


  override
  def authenticate(request: Request, subject: Subject): Boolean = {
    val certificates = request.getCertificates()
    if ((certificates == null) || (certificates.length == 0)) {
      return false
    }
    val x509c = new X509Claim(certificates(0))
    x509c.verify(this)

    val verified = for (claim <- x509c.webidclaims;
         if (claim.verified == Verification.Verified) ) yield {
      addAgentToSystem(claim)
      claim.principal
    }

	  subject.getPublicCredentials.add(x509c)
	  if (verified.size > 0) {
		  subject.getPrincipals().remove(UserUtil.ANONYMOUS)
		  subject.getPrincipals().addAll(verified)
		  return true
	  } else {
		  return false
  }
  }

  def addAgentToSystem(id: WebIDClaim) {
    systemGraph.addAll(createSystemUserDescription(id))
  }

  //todo: perhaps this makes more sense now that the verification has moved up higher
  def writeLoginResponse(request: Request, response: Response,
                         cause: Throwable) = {
    false;
  }

  def getWeight() = 400

  protected[auth] var webIdSrvc: WebIdGraphsService = null;

  protected def bindWebIdService(webcache: WebIdGraphsService) = {
    this.webIdSrvc = webcache
  }

  protected def unbindWebIdService(webcache: WebIdGraphsService) = {
    this.webIdSrvc = null
  }

  private var systemGraph: MGraph = null

  protected def bindSystemGraph(g: LockableMGraph) {
    systemGraph = g
  }

  protected def unbindSystemGraph(g: LockableMGraph) {
    systemGraph = null
  }


  private val systemGraphUri = Constants.SYSTEM_GRAPH_URI;


}
