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

package org.apache.clerezza.foafssl.auth

import java.security.interfaces.RSAPublicKey
import java.security.cert.CertificateException
import java.io.ByteArrayOutputStream
import org.apache.clerezza.rdf.core.serializedform.{SupportedFormat, Serializer}
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.foafssl.ontologies.{RSA, CERT}
import org.apache.clerezza.rdf.web.proxy.Cache
import java.util.LinkedList
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import java.security.PublicKey
import org.apache.clerezza.platform.security.auth.PrincipalImpl


/**
 * An X509 Claim maintains information about the proofs associated with claims
 * found in an X509 Certificate. It is the type of object that can be passed
 * into the public credentials part of a Subject node
 *
 * todo: think of what this would look like for a chain of certificates
 *
 * @author hjs
 * @created 30/03/2011
 */
class WebIDClaim(val webId: UriRef, val key: PublicKey) {

  import X509Claim._

  val errors = new LinkedList[java.lang.Throwable]()

  lazy val principal = new PrincipalImpl(userName)
  var verified = Verification.Unverified

 /*private lazy val selectQuery = {
	 val query = """PREFIX cert: <http://www.w3.org/ns/auth/cert#>
	 PREFIX rsa: <http://www.w3.org/ns/auth/rsa#>
	 SELECT ?m ?e ?mod ?exp
	 WHERE {
	 [] cert:identity ?webid ;
	 rsa:modulus ?m ;
	 rsa:public_exponent ?e .
	 OPTIONAL { ?m cert:hex ?mod . }
	 OPTIONAL { ?e cert:decimal ?exp . }
	 }"""
	 queryParser.parse(query).asInstanceOf[SelectQuery]
	 }*/

  //todo: not at all a satisfactory username method. Find something better.
  lazy val userName = for (c <- webId.getUnicodeString) yield
      c match {
        case ':' => '_';
        case '#' => '_';
        case '/' => '_';
        case _ => c
      }

  /**
   * verify this claim
   * @param authSrvc: the authentication service contains information about where to get graphs
   */
  //todo: make this asynchronous
  def verify(authSrvc: FoafSslAuthentication)  {
    try {
      var webIdInfo = authSrvc.webIdSrvc.getWebIDInfo(webId, Cache.CacheOnly)
      if (
        !verify(webIdInfo.publicUserGraph)
      ) {
        webIdInfo = authSrvc.webIdSrvc.getWebIDInfo(webId, Cache.ForceUpdate)
        if (
          !verify(webIdInfo.publicUserGraph)
        ) {
          verified = Verification.Failed
          return
        }
      }
    } catch {
      case e => {
        errors.add(e)
        verified = Verification.Failed
        return
      }
    }
    verified = Verification.Verified
  }

  def verify(tc: TripleCollection): Boolean = {
    key match {
      case k: RSAPublicKey => verify(k, tc);
      case _ => throw new CertificateException("Unsupported key format")
    }
  }

  private def verify(publicKey: RSAPublicKey, tc: TripleCollection): Boolean = {
    val publicKeysInGraph = getPublicKeysInGraph(tc)
    val publicKeyTuple = (new BigInt(publicKey.getModulus), new BigInt(publicKey.getPublicExponent))
    val result = publicKeysInGraph.contains(publicKeyTuple)
    if (logger.isDebugEnabled) {
      if (!result) {
        val baos = new ByteArrayOutputStream
        Serializer.getInstance.serialize(baos, tc, SupportedFormat.TURTLE);
        logger.debug("no matching key in: \n{}", new String(baos.toByteArray));
        logger.debug("the public key is not among the " +
          publicKeysInGraph.size + " keys in the profile graph of size " +
          tc.size)
        logger.debug("PublicKey: " + publicKeyTuple)
        publicKeysInGraph.foreach(k => logger.debug("PublikKey in graph: " + k))
      }
    }
    result
  }

  private def getPublicKeysInGraph(tc: TripleCollection): Array[(BigInt, BigInt)] = {
    import scala.collection.JavaConversions._
    val publicKeys = for (t <- tc.filter(null, CERT.identity, webId)) yield {
      t.getSubject
    }
    (for (p <- publicKeys) yield {
      val node = new GraphNode(p, tc)
      val modulusRes = node / RSA.modulus
      val modulus = intValueOfResource(modulusRes) match {
        case Some(x) => x
        case _ => BigInt(0)
      }
      val exponentRes = node / RSA.public_exponent
      val exponent = intValueOfResource(exponentRes) match {
        case Some(x) => x
        case _ => BigInt(0)
      }
      (modulus, exponent)
    }).toArray
  }



  def canEqual(other: Any) = other.isInstanceOf[WebIDClaim]

  override
  def equals(other: Any): Boolean =
    other match {
      case that: WebIDClaim => (that eq this) || (that.canEqual(this) && webId == that.webId && key == that.key)
      case _ => false
    }

  override
  lazy val hashCode: Int = 41 * (
      41 * (
        41 + (if (webId != null) webId.hashCode else 0)
        ) + (if (key != null) key.hashCode else 0)
      )
}

object Verification extends Enumeration {

	val Unverified = Value

	val Verified = Value

	val Failed = Value

}




