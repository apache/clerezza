/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.clerezza.foafssl.test.pages

import org.apache.clerezza.platform.typerendering.scala.{XmlResult, SRenderlet}
import org.apache.clerezza.platform.security.UserUtil
import org.apache.clerezza.foafssl.test.WebIDTester
import org.apache.clerezza.rdf.web.proxy.{Cache, WebProxy}
import org.apache.clerezza.rdf.scala.utils.RichGraphNode
import org.apache.clerezza.rdf.ontologies.RDF
import org.apache.clerezza.foafssl.ontologies.{RSA, CERT}
import org.apache.clerezza.rdf.scala.utils.Preamble._
import java.security.{PrivilegedAction, AccessController}
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.core.Graph
import org.apache.clerezza.rdf.core.serializedform.Serializer
import org.apache.clerezza.foafssl.auth.{Verification, WebIDClaim, X509Claim}
import java.io.{PrintStream, ByteArrayOutputStream}

/**
 * @author hjs
 * @created: 01/04/2011
 */

class CertificateDescription extends SRenderlet {
	def getRdfType() = WebIDTester.testCls

	override def renderedPage(arguments: XmlResult.Arguments) = new XhtmlCertificate(arguments)
}

class XhtmlCertificate(arguments: XmlResult.Arguments) extends XmlResult(arguments )  {
  val subject = UserUtil.getCurrentSubject();


  override def content = <span>
    <p>The TLS connection was established. We do not test the basic TLS connection.</p>
      {
    val subj = UserUtil.getCurrentSubject();
    val creds: scala.collection.mutable.Set[X509Claim] = collection.JavaConversions.asScalaSet(subj.getPublicCredentials(classOf[X509Claim]));
    if (creds.size==0) <p>No credentials available. Cannot tell how we came to accept identity</p>
    else for (cred <- creds) yield describeX509Claim(cred)
    }
  </span>


  def describeX509Claim(claim: X509Claim) = {
     <p>The Certificate sent can be viewed in detail <a href="WebId/x509">here</a>. The public key shown there is:
       <pre>{claim.cert.getPublicKey}</pre>
     </p>

     <p>The certificate sent contains {claim.webidclaims.size} WebId Claims. They are listed here, and it is shown if
       they have been verified.</p>
     <ul>{
        for (idClaim <- claim.webidclaims) yield <li>{describeWebIDClaim(idClaim)}</li>
     }</ul>
  }

  def getWebIDProfile(claim: WebIDClaim): Option[GraphNode] = {
    val som = AccessController.doPrivileged(new PrivilegedAction[Option[GraphNode]] {
      def run = $[WebProxy].fetchSemantics(claim.webId, Cache.CacheOnly)
    })
    som
  }

  def describeWebIDClaim(claim: WebIDClaim) = {
      <span>
        Claimed ID: <a href={claim.webId.getUnicodeString}>{claim.webId.getUnicodeString}</a><br/>
        Claim was {claim.verified}<br/>
        {
        claim.verified match {
        case Verification.Verified => verifiedClaim(claim)
        case Verification.Failed => claimFailure(claim)
        case Verification.Unsupported => <p>WebId's with this protocol are currently unsupported</p>
        case Verification.Unverified => <p>Currently this is not possible, but in asynchronous situations it will be</p>
          }
        }
      </span>
  }

  def verifiedClaim(claim: WebIDClaim) = {
    <p>Supporting information from the WebID Profile
      {val som: Option[GraphNode] = getWebIDProfile(claim)
    som match {
      case Some(profile) => {
        val keys = profile /- CERT.identity
        for (key <- keys) yield displayKey(key)
      }
      case None => "No remote graph?"
    }}
    </p>
  }

  def claimFailure(claim: WebIDClaim) = <p>
     The Claim failed because { import collection.JavaConversions._
       for (e: Throwable <- claim.errors) yield <pre>{
         val str = new ByteArrayOutputStream(1024)
         val out = new PrintStream(str)
         e.printStackTrace(out)
         str.toString("UTF-8")
         }</pre>
    }
    </p>

  def displayKey(key: RichGraphNode) = <pre>
    {val graph: Graph = key.getNodeContext
    val sout = Serializer.getInstance()
    val out = new ByteArrayOutputStream(1024)
    sout.serialize(out, graph, "text/rdf+n3")
    out.toString("UTF-8")}
  </pre>


}