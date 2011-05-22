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

package org.apache.clerezza.foafssl.test

import org.apache.clerezza.platform.security.UserUtil
import org.osgi.service.component.ComponentContext
import org.apache.clerezza.rdf.utils.GraphNode
import javax.ws.rs._
import org.apache.clerezza.rdf.ontologies._
import org.slf4j.{LoggerFactory, Logger}
import org.apache.clerezza.rdf.core.impl.util.Base64
import java.security.interfaces.RSAPublicKey
import org.apache.clerezza.rdf.core._
import access.NoSuchEntityException
import impl.{PlainLiteralImpl, TypedLiteralImpl, SimpleMGraph}
import org.apache.clerezza.foafssl.ontologies._
import org.apache.clerezza.platform.security.auth.WebIdPrincipal
import org.apache.clerezza.foafssl.auth.{WebIDClaim, Verification, X509Claim}
import java.util.Date
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.rdf.scala.utils.{CollectedIter, EasyGraphNode, EasyGraph, RichGraphNode}
import serializedform.Serializer
import java.io.ByteArrayOutputStream
import org.apache.clerezza.rdf.scala.utils.EasyGraph._
import java.math.BigInteger
import collection.mutable.{Queue, LinkedList}
import javax.security.auth.Subject
import collection.JavaConversions._
import org.apache.clerezza.platform.users.WebIdGraphsService


/**
 * implementation of (very early) version of test server for WebID so that the following tests
 * can be checked.
 *
 * http://lists.w3.org/Archives/Public/public-xg-webid/2011Jan/0107.html
 */

object WebIDTester {
  val testCls = new UriRef("https://localhost/test/WebID/ont/tests")   //todo: change url
  private val logger: Logger = LoggerFactory.getLogger(classOf[WebIDTester])

}

@Path("/test/WebId")
class WebIDTester {
  import WebIDTester._


  protected def activate(componentContext: ComponentContext) = {
    //		configure(componentContext.getBundleContext(), "profile-staticweb");
  }



	/**
	 * don't bother converting to rdf here for the moment.
	 */
  @GET
  @Produces(Array("application/xhtml","text/html"))
  def getTestMe(): GraphNode = {
    val resultNode: GraphNode = new GraphNode(new BNode(),new SimpleMGraph())
    resultNode.addProperty(RDF.`type`, testCls)
	 resultNode.addProperty(RDF.`type`,PLATFORM.HeadedPage)
    return resultNode
  }

	@GET
	@Produces(Array("text/n3","text/rdf+n3","text/turtle"))
	@Path("n3")
	def getTestMe_N3(): TripleCollection = getTestMeRDF()

	@GET
	def getTestMeRDF(): TripleCollection = {
		val certTester = new CertTester(UserUtil.getCurrentSubject(), webIdGraphsService)
		certTester.runTests()
		return certTester.toRdf()
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

	private var webIdGraphsService: WebIdGraphsService = null
	protected def bindWebIdGraphsService(webIdGraphsService: WebIdGraphsService): Unit = {
		this.webIdGraphsService = webIdGraphsService
	}

	protected def unbindWebIdGraphsService(webIdGraphsService: WebIdGraphsService): Unit = {
		this.webIdGraphsService = null
	}

}

/** All the cert tests are placed here */
class CertTester(subj: Subject, webIdGraphsService: WebIdGraphsService) extends Assertor {

	import EARL.{passed, failed, cantTell, untested, inapplicable}


	val creds: scala.collection.mutable.Set[X509Claim] = subj.getPublicCredentials(classOf[X509Claim]);
	val now = new Date()


	def runTests() {

		val thisDoc = (g.bnode ∈ FOAF.Document //there really has to be a way to get THIS doc url, to add relative urls to the graph
			⟝ DCTERMS.created ⟶ now
			)
		//
		// Description of certificates and their public profileKeys
		//
		val x509claimRefs = for (claim <- creds) yield {
			val cert = g.bnode
			(
				cert ∈ CERT.Certificate
					⟝ CERT.base64der ⟶ Base64.encode(claim.cert.getEncoded())
				)

			//
			// Assertion public key
			//
			val pubkey = claim.cert.getPublicKey
			val testCertKey = create(TEST.certificatePubkeyRecognised, cert.ref)

			pubkey match {
				case rsa: RSAPublicKey => {
					val pk = (g.bnode ∈ RSA.RSAPublicKey
						⟝ RSA.modulus ⟶ new TypedLiteralImpl(rsa.getModulus.toString(16), CERT.hex)
						⟝ RSA.public_exponent ⟶ new TypedLiteralImpl(rsa.getPublicExponent.toString(10), CERT.int_)
						)
					cert ⟝ CERT.principal_key ⟶ pk
					val res = testCertKey.result;
					res.description = "Certificate contains RSA key which is recognised"
					res.outcome = EARL.passed
					res.pointer(pk.ref)
				}
				case _ => {
					testCertKey.result.description = "Certificate contains key that is not understood by WebID layer " +
						"Pubkey algorith is " + pubkey.getAlgorithm
					testCertKey.result.outcome = EARL.failed
				}
			}

			//
			// Assertion time stamp of certificate
			//
			val dateOkAss = create(TEST.certificateDateOk, cert.ref)
			val notBefore = claim.cert.getNotBefore
			val notAfter = claim.cert.getNotAfter

			if (now.before(notBefore)) {
				dateOkAss.result("Certificate time is too early. Watch out this is often due to time " +
					"synchronisation issues accross servers", failed)
			} else if (now.after(notAfter)) {
				dateOkAss.result("Certificate validity time has expired. ", failed, thisDoc.ref)
			} else {
				dateOkAss.result("Certificate time is valid", passed, thisDoc.ref)
			}

			cert.ref -> claim
		}

		//
		// certificate was provided
		//
		val eC = x509claimRefs.size > 0
		val ass = (
			g.bnode ∈ EARL.Assertion
				⟝ EARL.test ⟶ TEST.certificateProvided
				⟝ EARL.result ⟶ (g.bnode ∈ EARL.TestResult
				                     ⟝ DC.description ⟶ {if (eC) "Certificate available" else "No Certificate Found"}
				                     ⟝ EARL.outcome ⟶ {if (eC) EARL.passed else EARL.failed})
			)
		if (eC) ass ⟝ EARL.subject ⟶* x509claimRefs.map(p => p._1)
		else return g.graph


		//
		// WebID authentication succeeded
		//
		val principals = for (p <- subj.getPrincipals
		                      if p.isInstanceOf[WebIdPrincipal]) yield p.asInstanceOf[WebIdPrincipal]
		(g.bnode ∈ EARL.Assertion
			⟝ EARL.test ⟶ TEST.webidAuthentication
			⟝ EARL.result ⟶ (g.bnode ∈ EARL.TestResult
						⟝ DC.description ⟶ {"found " + principals.size + " valid principals"}
						⟝ EARL.outcome ⟶ {if (principals.size > 0) EARL.passed else EARL.failed}
						⟝ EARL.pointer ⟶* principals.map(p => p.getWebId)
						)
			⟝ EARL.subject ⟶* x509claimRefs.map(p => p._1)
			)
		import collection.JavaConversions._

		for ((certRef, claim) <- x509claimRefs) {
			for (widc <- claim.webidclaims) {
				import Verification._
				val webidAss = create(TEST.webidClaim,
					Seq(widc.webId, certRef)) //todo, we need to add a description of the profileKeys as found in the remote file
				val result = webidAss.result
				result.pointer(widc.webId)
				result.exceptions = widc.errors
				widc.verified match {
					case Verified => {
						result("claim for WebId " + widc.webId + " was verified", passed)
						claimTests(widc)
					}
					case Failed => {
						result("claim for WebID " + widc.webId + " failed", failed)
						claimTests(widc)
					}
					case Unverified => {
						result("claim for WebId " + widc.webId + " was not verified",untested)
					}
					case Unsupported => {
						result("this webid is unsupported ",cantTell)
					}
				}
			}
		}
	}

	// more detailed tester for claims that passed or failed
	// even tester that succeed could be just succeeding by chance (if public profileKeys are badly written out for eg)
	def claimTests(claim: WebIDClaim) {
		val sem: Option[GraphNode] = try {
			Some(new GraphNode(claim.webId, webIdGraphsService.getWebIdInfo(claim.webId).publicProfile)) //webProxy.fetchSemantics(claim.webId, Cache.CacheOnly)
		} catch {
			case e: NoSuchEntityException => None
		}
		val profileXst = create(TEST.profileGet, claim.webId)

		sem match {
			case Some(profile) => {
				if (profile.getGraph.size() > 0) {
					profileXst.result("Profile was fetched. The information about this is not yet very detailed" +
						" in Clerezza. Later will be able to give more details.", passed)
					testKeys(profile /- CERT.identity)

				} else {
					profileXst.result("Profile seems to have been fetched but it contains very little" +
						" information. There may be other issues too", cantTell)
				}

			}
			case None => {
				profileXst.result("No profile was found or is in store", failed)
			}
		}

	}

	/**
	 * @param exponentNode the node in the remote profile descrbing the modulus - can be a literal or a resource
	 * @param litRef a resource to the literal as described in the test graph
	 * @return true, if the modulus is recognised as parsing
	 */
	def testRSAModulus(modulusNode: RichGraphNode, litRef: Resource):Boolean = {
		val asrtKeyModulusLit = create(TEST.pubkeyRSAModulusLiteral, litRef)
		val asrtKeyMod = create(TEST.pubkeyRSAModulus, litRef)
		val asrtKeyModulusOldFunc = create(TEST.pubkeyRSAModulusOldFunctional,litRef)
		var result = false

		modulusNode! match {
			case ref: NonLiteral => {
				asrtKeyModulusLit.result("the modulus of this key is not described directly as" +
					" a literal. It is currently the preferred practice.", failed)
				val hex = modulusNode / CERT.hex
				if (hex.size == 0) {
					asrtKeyModulusOldFunc.result("no hexadecimal value for the modulus found", failed)
				} else if (hex.size > 1) {
					asrtKeyModulusOldFunc.result((hex.size - 1) + " too many hexadecimal values for " +
						"the modulus found. 1 is enough. If the numbers don't end up matching this is very likely" +
						" to cause random behavior ", failed)
				} else {
					asrtKeyModulusOldFunc.result("one hex value for modulus", EARL.passed)
					val kmres = asrtKeyMod.result
					hex(0) ! match {
						case refh: NonLiteral => {
							asrtKeyMod.result("The modulus is using old notation and it's hex is not " +
								"a literal. Going further would require reasoning engines which it is currently unlikely" +
								"many sites have access to.", failed)
						}
						case lith: Literal => {
							lith match {
								case plainLit: PlainLiteral => {
									if (plainLit.getLanguage != null)
										kmres("keymodulus exists and is parseable", passed)
									else
										kmres("keymodulus exists and is parseable, but has a language tag", passed)
									result = true
								}
								case typedLit: TypedLiteral => {
									if (typedLit.getDataType == null ||
										XSD.string == typedLit.getDataType) {
										kmres("keymodulus exists and is parseable", passed)
										result = true
									} else {
										kmres("keymodulus exists but does not have a string type", failed)
									}
								}
								case lit => {
									// cert:hex cannot be mistyped, since anything that is odd in the string is
									//removed
									kmres("keymodulus exists and is parseable", passed)
									result = true
								}
							}
						}
					}
				}

			}
			case numLit: Literal => {
				val reskeyModLit = asrtKeyModulusLit.result
				numLit match {
					case tl: TypedLiteral => tl.getDataType match {
						case CERT.int_ => {
							try {
								BigInt(tl.getLexicalForm)
								reskeyModLit("Modulus is of type cert:int. It parsed ok.", passed, tl)
								result = true
							} catch {
								case e: NumberFormatException => {
									reskeyModLit("Modulus cert:int failed to parse as one", failed, tl)
								}
							}
						}
						case CERT.decimal => {
							//todo: need to add cert:decimal parsing flexibility to ontology
								reskeyModLit("Modulus is of type cert:decimal. It always parses ok", passed, tl)
								result = true
						}
						case CERT.hex => {
							result = true
							reskeyModLit("Modulus is of type cert:hex. It will always parse to a positive number.", passed, tl)
						}
						case XSD.int_ => {
							try {
								BigInt(tl.getLexicalForm)
								reskeyModLit("Modulus is of type xsd:int. It parsed but it is certainly too small for " +
									"a modulus", failed)
							} catch {
								case e: NumberFormatException => {
									reskeyModLit("Modulus cert:decimal is failed to parse", failed, tl)
								}
							}
						}
						case XSD.base64Binary => reskeyModLit("Base 64 binaries are not numbers. If you wish to have " +
							"a base64 integer notation let the WebId Group know. We can define one easily.", failed, tl)
						case XSD.hexBinary => reskeyModLit("Base hex binary literals are not a numbers. If you wish to have a hex " +
							" integer notation use the " + CERT.hex +
							" relation. It is easier for people to write out.", failed, tl)
						case XSD.nonNegativeInteger => {
							try {
								val bi = BigInt(tl.getLexicalForm)
								if (bi >= 0) {
									reskeyModLit("Modulus is declared to be of type non-negative integer and it is", passed, tl)
									result = true
								} else {
									reskeyModLit("Modulus is declared to be of type non-negative integer but it is negative", failed, tl)
								}
							} catch {
								case e: NumberFormatException => {
									reskeyModLit("Modulus xsd:int is very likely too short a number for a modulus. It also " +
										"failed to parse as one", failed, tl)
								}
							}

						}
						case XSD.integer => {
							try {
								BigInt(tl.getLexicalForm)
								reskeyModLit("Modulus is of type xsd:integer. It parsed.", passed, tl)
								result = true
							} catch {
								case e: NumberFormatException => {
									reskeyModLit("Modulus xsd:integer is failed to parse", failed, tl)
								}
							}

						}
						case XSD.positiveInteger => {
							try {
								val bi = BigInt(tl.getLexicalForm)
								if (bi > 0) {
									reskeyModLit("Modulus is declared to be of type positive integer and it is", passed, tl)
									result = true
								} else if (bi == 0) {
									reskeyModLit("Modulus is 0 which is certainly too small", failed, tl)
								} else {
									reskeyModLit("Modulus is declared to be of type positive integer but it is not", failed, tl)
								}
							} catch {
								case e: NumberFormatException => {
									reskeyModLit("Modulus xsd:positiveInteger failed to parse", failed, tl)
								}
							}

						}
						case littype => reskeyModLit("We don't know how to interpret numbers of type " + littype +
							"It would be better to use either cert:hex or cert:int", cantTell, tl)
					}
					case lit: Literal => reskeyModLit("The literal needs to be of a number type, not a string", failed, lit)
				}
			}


			//its ok, and do other modulus verification
		}
		return result
	}


	/**
	 * @param exponentNode the node in the remote profile describing the expontent - can be a literal or a resource
	 * @param litRef a reference to the literal as described in the test graph
	 * @return true if the exponent parses correctly
	 */
	def testRSAExp(exponentNode: RichGraphNode, litRef: Resource) : Boolean = {
		val asrtKeyExpLit = create(TEST.pubkeyRSAExponentLiteral, litRef)
		val asrtKeyExp = create(TEST.pubkeyRSAExponent, litRef)
		val asrtKeyExpOldFunc = create(TEST.pubkeyRSAExponentOldFunctional,litRef)
		var result = false

		exponentNode! match {
			case ref: NonLiteral => {
				asrtKeyExpLit.result("the exponent of this key is not described directly as" +
					" a literal. It is currently the preferred practice.", failed)
				val decml = exponentNode / CERT.decimal
				if (decml.size == 0) {
					asrtKeyExpOldFunc.result("no decimal value for the exponent found", failed)
				} else if (decml.size > 1) {
					asrtKeyExpOldFunc.result((decml.size - 1) + " too many decimal values for " +
						"the exponent found. 1 is enough. If the numbers don't end up matching this is very likely" +
						" to cause random behavior ", failed)
				} else {
					asrtKeyExpOldFunc.result("one hex value for modulus", EARL.passed)
					val kExpres = asrtKeyExp.result
					decml(0) ! match {
						case refh: NonLiteral => {
							asrtKeyExp.result("The exponent is using old notation and it's cert:decimal relation is not " +
								"to a literal. Going further would require reasoning engines which it is currently unlikely" +
								"many sites have access to.", failed)
						}
						case lith: Literal => {
							lith match {
								case plainLit: PlainLiteral => {
									if (plainLit.getLanguage != null)
										kExpres("key exponent exists and is parseable", passed)
									else
										kExpres("key exponent exists and is parseable, but has a language tag", passed)
									result = true
								}
								case typedLit: TypedLiteral => {
									if (typedLit.getDataType == null ||
										XSD.string == typedLit.getDataType) {
										kExpres("keymodulus exists and is parseable", passed)
										result = true
									} else {
										kExpres("keymodulus exists but does not have a string type", failed)
									}
								}
								case lit => {
									//todo: can cert:int not be mistyped?
									kExpres("keymodulus exists and is parseable", passed)
								}
							}
						}
					}
				}

			}
			case numLit: Literal => {
				val reskeyExpLit = asrtKeyExpLit.result
				numLit match {
					case tl: TypedLiteral => tl.getDataType match {
						case CERT.int_ => {
							try {
								BigInt(tl.getLexicalForm)
								reskeyExpLit("Exponent is of type cert:int. It parsed ok.", passed, tl)
								result = true
							} catch {
								case e: NumberFormatException => {
									reskeyExpLit("Exponent cert:int failed to parse as one", failed, tl)
								}
							}
						}
						case CERT.hex => {
							reskeyExpLit("Exponent is of type cert:hex. It will always parse to a positive number.", passed, tl)
							result = true
						}
						case CERT.decimal => {
							try {
								BigInt(tl.getLexicalForm)
								reskeyExpLit("Exponent is of type xsd:int. It parsed ok.", passed)
								result = true
							} catch {
								case e: NumberFormatException => {
									reskeyExpLit("Exeponent of type cert:decimal failed to parse", failed, tl)
								}
							}
						}
						case XSD.base64Binary => reskeyExpLit("Base 64 binaries are not numbers. If you wish to have " +
							"a base64 integer notation let the WebId Group know. We can define one easily.", failed, tl)
						case XSD.hexBinary => reskeyExpLit("Base hex binary literals are not a numbers. If you wish to have a hex " +
							" integer notation use the " + CERT.hex +
							" relation. It is easier for people to write out.", failed, tl)
						case XSD.nonNegativeInteger => {
							try {
								val bi = BigInt(tl.getLexicalForm)
								if (bi >= 0) {
									reskeyExpLit("Exponent is declared to be of type non-negative integer and it is", passed, tl)
									result = true
								} else {
									reskeyExpLit("Exponent is declared to be of type non-negative integer but it is negative", failed, tl)
								}
							} catch {
								case e: NumberFormatException => {
									reskeyExpLit("Exponent xsd:nonNegativeInteger failed to parse as one", failed, tl)
								}
							}

						}
						case XSD.integer => {
							try {
								BigInt(tl.getLexicalForm)
								reskeyExpLit("Exponent is of type xsd:integer. It parsed.", passed, tl)
								result = true
							} catch {
								case e: NumberFormatException => {
									reskeyExpLit("Exponent xsd:integer is failed to parse", failed, tl)
								}
							}

						}
						case XSD.positiveInteger => {
							try {
								val bi = BigInt(tl.getLexicalForm)
								if (bi > 0) {
									reskeyExpLit("Exponent is declared to be of type positive integer and it is", passed, tl)
									result = true
								} else if (bi == 0) {
									reskeyExpLit("Exponent is 0 which is certainly too small", failed, tl)
								} else {
									reskeyExpLit("Exponent is declared to be of type positive integer but it is not", failed, tl)
								}
							} catch {
								case e: NumberFormatException => {
									reskeyExpLit("Exponent xsd:positiveInteger failed to parse", failed, tl)
								}
							}

						}
						case littype => reskeyExpLit("We don't know how to interpret numbers of type " + littype +
							"It would be better to use either cert:hex or cert:int", cantTell, tl)
					}
					case lit: Literal => reskeyExpLit("The literal needs to be of a number type, not a string", failed, lit)
				}
			}
		}
		return result
	}


	def testKeys(profileKeys: CollectedIter[RichGraphNode]) {

		for (pkey <- profileKeys) yield {
			//
			//create a pointer to this key, so that future tester can refer to it
			//
			val graph: Graph = pkey.getNodeContext
			val sout = Serializer.getInstance()
			val out = new ByteArrayOutputStream(512)
			sout.serialize(out, graph, "text/rdf+n3")
			val n3String = out.toString("UTF-8")
			//todo: turtle mime type literal?
			val keylit = g.bnode ⟝ OWL.sameAs ⟶ (n3String ^^ new UriRef("http://example.com/turtle"))


			//
			// some of the tester we will complete here
			//
			val asrtKeyModulusFunc = create(TEST.pubkeyRSAModulusFunctional, keylit.ref)
			val asrtKeyExpoFunc = create(TEST.pubkeyRSAExponentFunctional, keylit.ref)
			val asrtWffkey = create(TEST.profileWellFormedKey, keylit.ref)


			var claimsTobeRsaKey = pkey.hasProperty(RDF.`type`, RSA.RSAPublicKey)

			val mods = pkey / RSA.modulus
			val exps = pkey / RSA.public_exponent

			claimsTobeRsaKey = claimsTobeRsaKey || mods.size > 0 || exps.size > 0

			if (!claimsTobeRsaKey) {
				asrtWffkey.result("Do not recognise the type of this key", cantTell)
			}

			var rsaExpOk, rsaModOk: Boolean = false

			if (mods.size == 0) {
				if (claimsTobeRsaKey) {
					asrtKeyModulusFunc.result("Missing modulus in RSA key", failed)
				}
				else {
					asrtKeyModulusFunc.result("Can't tell if this is an RSA key", cantTell)
				}
			} else if (mods.size > 1) {
				asrtKeyModulusFunc.result("Found more than one modulus. Careful, unless the numbers are" +
					" exactly the same, there is a danger of erratic behavior", failed)
			} else {
				asrtKeyModulusFunc.result("Found one Modulus", passed)

				rsaModOk = testRSAModulus(mods, keylit.ref)
			}

			if (exps.size == 0) {
				if (claimsTobeRsaKey) {
					asrtKeyExpoFunc.result("Missing exponent in RSA key", failed)
				}
				else {
					asrtKeyExpoFunc.result("Can't tell if this is an RSA key", cantTell)
				}
			} else if (exps.size > 1) {
				asrtKeyExpoFunc.result("Found more than one exponents. Careful, unless the numbers are" +
					" exactly the same, there is a danger of erratic behavior", failed)
				//we could have a problem
			} else {
				asrtKeyExpoFunc.result("Found one Modulus", passed)
				rsaExpOk = testRSAExp(mods, keylit.ref)
			}

			if (rsaExpOk && rsaModOk) {
				asrtWffkey.result("Modulus and Exponent of key good", passed)
			}

		}
	}
}

/**
  * Assertors create and manage assertions.
  *
  * Assertions created with with such an object will be added to the list
  * when runTests information is added - and only then. This is a convenience
  * to make the Assertor keep track of tests
  *
  * sublcass Assertors for specific types of runTests suites
  */
class Assertor {

	val g = new EasyGraph(new SimpleMGraph)

	var assertions: List[Assertion] = Nil

	def add(newAssertion: Assertion) = {
		assertions = newAssertion :: assertions
		newAssertion
	}

	def create(testName: UriRef, subjects: Seq[Resource]) = new Assertion(testName, subjects)

	def create(testName: UriRef, subject: Resource) = new Assertion(testName, Seq[Resource](subject))

	def toRdf(): TripleCollection =  {
		for (test <- assertions) {
			test.toRdf()
		}
		g.graph
	}

	class Assertion(testName: UriRef,
	                subjects: Seq[Resource]) {


		//only add this runTests to the list of assertions if there is a result
		//this makes it easier to write code that keeps track of assertions, without ending up having to
		//publish all of them
		lazy val result = {
			add(this)
			new TstResult
		}

		def toRdf(): EasyGraphNode = (
			g.bnode ∈ EARL.Assertion
				⟝ EARL.test ⟶ testName
				⟝ EARL.result ⟶ result.toRdf()
				⟝ EARL.subject ⟶* subjects
			)
	}

	class TstResult {
		var description: String = _
		var outcome: UriRef = _
		var pointers: Seq[Resource] = Nil
		var exceptions: Iterable[java.lang.Throwable] = Nil

		def pointer(point: NonLiteral) {
			pointers = Seq(point)
		}


		// a method to deal with most usual case
		def apply(desc: String, success: UriRef) {
			description = desc
			outcome = success
		}

		def apply(desc: String, success: UriRef, pointer: Resource) {
			description = desc
			outcome = success
			pointers = Seq(pointer)
		}


		def toRdf(): EasyGraphNode =  (
				g.bnode ∈ EARL.TestResult
					⟝ DC.description ⟶ description
					⟝ EARL.outcome ⟶ outcome
					⟝ EARL.pointer ⟶* pointers
				   ⟝ EARL.info ⟶* { for (e <- exceptions) yield new PlainLiteralImpl(e.toString)  }
				)

	}

}