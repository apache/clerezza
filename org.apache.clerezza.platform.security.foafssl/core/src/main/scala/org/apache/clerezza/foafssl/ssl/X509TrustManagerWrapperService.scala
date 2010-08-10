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

package org.apache.clerezza.foafssl.ssl

import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.PublicKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.X509TrustManager;
import org.apache.clerezza.foafssl.Utilities
import org.apache.clerezza.platform.users.WebDescriptionProvider
import org.apache.clerezza.foafssl.ontologies.CERT
import org.apache.clerezza.foafssl.ontologies.RSA
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.rdf.core.Literal
import org.apache.clerezza.rdf.core.LiteralFactory
import org.apache.clerezza.rdf.core.MGraph
import org.apache.clerezza.rdf.core.NoConvertorException
import org.apache.clerezza.rdf.core.Resource
import org.apache.clerezza.rdf.core.TripleCollection
import org.apache.clerezza.rdf.core.TypedLiteral
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.core.access.LockableMGraph
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.core.impl.TripleImpl
import org.apache.clerezza.rdf.core.serializedform.{Serializer, SupportedFormat}
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils._
import org.apache.clerezza.rdf.ontologies.FOAF
import org.apache.clerezza.rdf.ontologies.PLATFORM
import org.apache.clerezza.rdf.ontologies.RDF
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.jsslutils.sslcontext.X509TrustManagerWrapper
import org.jsslutils.sslcontext.trustmanagers.TrustAllClientsWrappingTrustManager
import org.slf4j.LoggerFactory;

class X509TrustManagerWrapperService() extends X509TrustManagerWrapper {

	private val logger = LoggerFactory.getLogger(classOf[X509TrustManagerWrapperService])
	private var descriptionProvider: WebDescriptionProvider = null;

	protected def bindWebDescriptionProvider(descriptionProvider: WebDescriptionProvider) = {
		this.descriptionProvider = descriptionProvider
	}
	
	protected def unbindWebDescriptionProvider(descriptionProvider: WebDescriptionProvider) = {
		this.descriptionProvider = null
	}
	
	private var systemGraph: MGraph = null
	
	protected def bindSystemGraph(g: LockableMGraph) {
		systemGraph = g
	}
	
	protected def unbindSystemGraph(g: LockableMGraph) {
		systemGraph = null
	}
	
	override def wrapTrustManager(trustManager: X509TrustManager): X509TrustManager =  {
		new TrustAllClientsWrappingTrustManager(
			trustManager) {
			override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {
				val webIdUriRefs = Utilities.getClaimedWebIds(chain)
				if (webIdUriRefs.length == 0) {
					trustManager.checkClientTrusted(chain, authType)
				} else {
					val publicKey = chain(0).getPublicKey
					for (uriRef <- webIdUriRefs) {
						verify(uriRef, publicKey)
					}
				}
				return
			}
		}
	}
	
	private val systemGraphUri = Constants.SYSTEM_GRAPH_URI;
	
	private def verify(uriRef: UriRef, publicKey: PublicKey): Unit = {
		var webDescription = descriptionProvider.getWebDescription(uriRef, false)
		if (
			!verify(uriRef, publicKey, webDescription.getGraph)
			) {
				webDescription = descriptionProvider.getWebDescription(uriRef, true)
				if (
					!verify(uriRef, publicKey, webDescription.getGraph)
				) throw new CertificateException
			}
			systemGraph.addAll(createSystemUserDescription(webDescription))		
	}
	
	def createSystemUserDescription(webDescription: GraphNode) = {
		val result = new SimpleMGraph()
		val webId = webDescription.getNode.asInstanceOf[UriRef]
		result.add(new TripleImpl(webId, PLATFORM.userName, 
															new PlainLiteralImpl(Utilities.cretateUsernameForWebId(webId))))
		result.add(new TripleImpl(webId, RDF.`type` , 
															FOAF.Agent))
		result
	}
	
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
	 * gets the parts of key from rdf
	 * @return (mod, exp)
	 */
	private def getPublicKeysInGraph(webId: UriRef, tc: TripleCollection): Array[(BigInt, BigInt)]= {
		import scala.collection.JavaConversions._
		val publicKeys = for (t <- tc.filter(null, CERT.identity, webId)) yield {
			t.getSubject
		}
		(for (p <- publicKeys) yield {
			val node = new GraphNode(p, tc)
			val modulusRes = node/RSA.modulus
			val modulus = intValueOfResource(modulusRes) match {
				case Some(x) => x
				case _ => BigInt(0)
			}
			val exponentRes = node/RSA.public_exponent
			val exponent = intValueOfResource(exponentRes) match {
				case Some(x) => x
				case _ => BigInt(0)
			}		
			(modulus, exponent)
		}).toArray
	}
 
	
	
	/**
	 * @return true if the key could be verified
	 */
	 private def verify(webId: UriRef, publicKey: PublicKey, tc: TripleCollection): Boolean = {
			publicKey match {
				case k: RSAPublicKey => verify(webId, k, tc);
				case _ => throw new CertificateException("Unsupported key format")
			}
		}
	 
	private def verify(webId: UriRef, publicKey: RSAPublicKey, tc: TripleCollection): Boolean = {
		val publicKeysInGraph = getPublicKeysInGraph(webId, tc)
		val publicKeyTuple = (new BigInt(publicKey.getModulus), new BigInt(publicKey.getPublicExponent))
		val result = publicKeysInGraph.contains(publicKeyTuple)
		if (logger.isDebugEnabled) {
			if (!result) {
				val baos = new ByteArrayOutputStream
				Serializer.getInstance.serialize(baos, tc, SupportedFormat.TURTLE);
				logger.debug("no mathing key in: \n{}", new String(baos.toByteArray));
				logger.debug("the public key is not among the "+
					publicKeysInGraph.size+" keys in the profile graph of size "+
					tc.size)
				logger.debug("PublicKey: "+publicKeyTuple)
				publicKeysInGraph.foreach(k => logger.debug("PublikKey in graph: "+ k))
			}
		}
		result
	}

		/**
	  * @return the integer value if r is a typedLiteral of cert:hex or cert:decimal,
		* otherwise the integer value of the  cert:hex or cert:decimal property of r or 
		* None if no such value available
	  */
	private def intValueOfResource(n: GraphNode): Option[BigInt] = {
		n! match {
			case l: TypedLiteral => intValueOfTypedLiteral(l);
			case r: Resource => intValueOfResourceByProperty(n)
		}
	}
	
	private def intValueOfResourceByProperty(n: GraphNode): Option[BigInt] = {
		val hexValues = n/CERT.hex
		if (hexValues.length > 0) {
			return Some(intValueOfHexString(hexValues*))
		}
		val decimalValues = n/CERT.decimal
		if (decimalValues.length > 0) {
			return Some(BigInt(decimalValues*))
		}
		val intValues = n/CERT.int_
		if (intValues.length > 0) {
			return Some(BigInt(intValues*))
		}
		return None
	}
 
	private def intValueOfLiteral(l: Literal): Option[BigInt] = {
		l match {
			case x: TypedLiteral => intValueOfTypedLiteral(x);
			case x => Some(intValueOfHexString(x.getLexicalForm))
		}
	}
	private def intValueOfTypedLiteral(l: TypedLiteral): Option[BigInt] = {
		try {
			(l.getLexicalForm, l.getDataType) match {
				case (lf, CERT.hex) => Some(intValueOfHexString(lf))
				case (lf, CERT.decimal) => Some(BigInt(lf))
				case (lf, CERT.int_) => Some(BigInt(lf))
				case _ => Some(new BigInt(LiteralFactory.getInstance.createObject(classOf[BigInteger], l)))
			}
		} catch {
			case e: NoConvertorException => None
			case e => throw e
		}
	}
	
	private def intValueOfHexString(s: String): BigInt = {
		val strval = cleanHex(s);
    BigInt(strval, 16);
   }



    /**
     * This takes any string and returns in order only those characters that are
     * part of a hex string
     * 
     * @param strval
     *            any string
     * @return a pure hex string
     */

    private def cleanHex( strval: String)  = {
				def legal(c: Char) = {
					((c >= 'a') && (c <= 'f')) ||
					((c >= 'A') && (c <= 'F')) ||
					((c >= '0') && (c <= '9'))
				}
        (for (c <- strval; if legal(c)) yield c)
    }
	 }
	 