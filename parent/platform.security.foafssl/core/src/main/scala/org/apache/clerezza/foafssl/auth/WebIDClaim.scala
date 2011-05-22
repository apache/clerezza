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
import java.util.LinkedList
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import java.security.PublicKey
import scala.None
import org.apache.clerezza.platform.security.auth.WebIdPrincipal

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

	lazy val principal = new WebIdPrincipal(webId)
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



	/**
	 * verify this claim
	 * @param authSrvc: the authentication service contains information about where to get graphs
	 */
	//todo: make this asynchronous
	def verify(authSrvc: FoafSslAuthentication) {
		if (!webId.getUnicodeString.startsWith("http:") && !webId.getUnicodeString.startsWith("https:")) {
			//todo: ftp, and ftps should also be doable, though content negoations is then lacking
			verified = Verification.Unsupported
			return
		}
		verified = try {
			var webIdInfo = authSrvc.webIdSrvc.getWebIdInfo(webId)
			verify(webIdInfo.localPublicUserData) match {
				case None => Verification.Verified
				case Some(err) => {
					webIdInfo.forceCacheUpdate()
					webIdInfo = authSrvc.webIdSrvc.getWebIdInfo(webId)
					verify(webIdInfo.localPublicUserData) match {
						case None => Verification.Verified
						case Some(err) => {
							errors.add(err)
							Verification.Failed
						}
					}
				}
			}
		} catch {
			case e => {
				errors.add(e)
				Verification.Failed
			}
		}
	}

	def verify(tc: TripleCollection): Option[WebIDVerificationError] = {
		key match {
			case k: RSAPublicKey => verify(k, tc);
			case x => Some(new WebIDVerificationError("Unsupported key format "+x.getClass) )
		}
	}

	private def verify(publicKey: RSAPublicKey, tc: TripleCollection): Option[WebIDVerificationError] = {
		val publicKeysInGraph = getPublicKeysInGraph(tc)
		if (publicKeysInGraph.size==0) return Some(new WebIDVerificationError("No public keys found in WebID Profile for "+webId.getUnicodeString))
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
		if (result) return None
		else return Some(new WebIDVerificationError("No matching keys found in WebID Profile"))
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

class WebIDVerificationError(msg: String) extends Error(msg) {

}




object Verification extends Enumeration {

	/**
	 * the claim has not yet been verified
	 */
	val Unverified = Value

	/**
	 * The claim was verified and succeeded
	 */
	val Verified = Value


	/**
	 * The claim was verified and failed
	 */
	val Failed = Value

	/**
	 * The claim cannot be verified by this agent
	 */
	val Unsupported = Value

}

