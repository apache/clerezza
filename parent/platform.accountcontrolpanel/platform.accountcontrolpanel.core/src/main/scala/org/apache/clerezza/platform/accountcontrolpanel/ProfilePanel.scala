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
package org.apache.clerezza.platform.accountcontrolpanel

import java.util.List
import java.util.Arrays
import java.util.Iterator
import ontologies.{PINGBACK, CONTROLPANEL}
import org.apache.clerezza.platform.security.UserUtil
import org.apache.clerezza.ssl.keygen.CertSerialisation
import org.apache.clerezza.ssl.keygen.Certificate
import org.apache.clerezza.foafssl.ontologies.CERT
import org.apache.clerezza.foafssl.ontologies.RSA
import org.apache.clerezza.jaxrs.utils.RedirectUtil
import org.apache.clerezza.jaxrs.utils.TrailingSlash
import org.apache.clerezza.platform.config.PlatformConfig
import org.apache.clerezza.platform.usermanager.UserManager
import org.apache.clerezza.rdf.core._
import access.TcManager
import impl.{SimpleMGraph, TripleImpl}
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.osgi.service.component.ComponentContext
import javax.ws.rs._
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import java.math.BigInteger
import java.security.AccessController
import java.security.PrivilegedAction
import java.security.interfaces.RSAPublicKey
import org.apache.clerezza.ssl.keygen.KeygenService
import java.net.URI
import org.apache.clerezza.rdf.scala.utils.{RichGraphNode, EasyGraphNode, EasyGraph}
import org.apache.clerezza.rdf.ontologies._
import org.slf4j.scala.Logging
import javax.security.auth.Subject
import org.apache.clerezza.platform.users.{WebIdInfo, WebIdGraphsService}


object ProfilePanel {
	val webIdTemplate = classOf[ProfilePanel].getAnnotation(classOf[Path]).value+"#me"


	/*
	 * return the WebID for the given user name  given the UriInfo (which contains information for
	 * server default name)
	 **/
	def webID(uname: String, uriInfo: UriInfo): UriRef = {
		val path = PingBack.interpolate(webIdTemplate, uname)
		val uriStr = uriInfo.getBaseUri.resolve(path); //a bit expensive for something so simple
		new UriRef(uriStr.toString)
	}
}

/**
 * Presents a panel where the user can create a webid and edit her profile.
 *
 * @author reto
 */

@Path("/user/{id}/profile")
class ProfilePanel extends Logging {

	import collection.JavaConversions._
	import EasyGraph._


	@GET
	def getPersonalProfilePage(@Context uriInfo: UriInfo,
	                           @PathParam(value = "id") userName: String): GraphNode = {
		TrailingSlash.enforceNotPresent(uriInfo)
		val resultNode= getPersonalProfile(userName, uriInfo)

		return resultNode
	}

	//todo: there is a bit of repetition in the graphs, and it is not clear why these relations should not go straight into the DB. What should, what should not?
	private def getPersonalProfile(userName: String, info: UriInfo): EasyGraphNode = {
		val profileDocUri = getSuggestedPPDUri(userName)

		val profile = AccessController.doPrivileged(new PrivilegedAction[EasyGraphNode] {
			def run: EasyGraphNode = {
				val userInSysGraph = userManager.getUserInSystemGraph(userName)
				val user = userInSysGraph.getNode
				val profile = userInSysGraph.getNode match {
					case blank: BNode => {
						//user does not have a webId yet
						val g = new EasyGraph()
						(
							g.bnode ⟝ CONTROLPANEL.isLocalProfile ⟶ true
								⟝ CONTROLPANEL.suggestedPPDUri ⟶ profileDocUri
								⟝ FOAF.primaryTopic ⟶ (g.bnode ⟝ PLATFORM.userName ⟶ userName)
							)
					}
					case webid: UriRef => {
						var webIDInfo = webIdGraphsService.getWebIdInfo(webid)
						var res = new EasyGraphNode(profileDocUri, new UnionMGraph(new SimpleMGraph, webIDInfo.localPublicUserData))
						(res ⟝ CONTROLPANEL.isLocalProfile ⟶ webIDInfo.isLocal
							⟝ FOAF.primaryTopic ⟶ webid)
						if (webIDInfo.isLocal) {
							//the reason we need to call pingCollUri is we need a full URI because lack of support for relative URIs
							res ⟝ PINGBACK.to ⟶ new UriRef(PingBack.pingCollUri(userName, info))
						}
						res
					}
				}
				val friendInfo = for (kn: Triple <- profile.getGraph.filter(user.asInstanceOf[NonLiteral], FOAF.knows, null)
				                      if kn.getObject.isInstanceOf[UriRef];
				                      friend = kn.getObject.asInstanceOf[UriRef]
				                      if (friend != profileDocUri)
				) yield {
					try {
						val friendGraph = tcManager.getGraph(FoafBrowser.removeHash(friend))
						new RichGraphNode(friend, friendGraph).getNodeContext
					} catch {
						case e => {
							logger.warn("cought exception trying to fetch graph - these graphs should already be in store " + friend, e)
							new EasyGraph().add(friend, SKOS.note, "problem with fetching this node: " + e)
						}
					}
				}
				for (g <- friendInfo) profile.graph.addAll(g)
				profile
			}
		})


		(profile ∈   PLATFORM.HeadedPage
		         ∈  CONTROLPANEL.ProfilePage)
	}

	/**
	 * @param userName
	 * @return the suggested Personal Profile Document URI
	 */
	def getSuggestedPPDUri(userName: String): UriRef = {
		return new UriRef(platformConfig.getDefaultBaseUri.getUnicodeString + "user/" + userName + "/profile")
	}

	@POST
	@Path("set-existing-webid")
	def setExistingWebId(@Context uriInfo: UriInfo,
	                     @FormParam("webid") webId: UriRef,
	                     @PathParam(value = "id") userName: String): Response = {
		return AccessController.doPrivileged(new PrivilegedAction[Response] {
			def run: Response = {
				var userInSystemGraph: GraphNode = userManager.getUserInSystemGraph(userName)
				userInSystemGraph.replaceWith(webId)
				return RedirectUtil.createSeeOtherResponse("../profile", uriInfo)
			}
		})
	}

	@POST
	@Path("create-new-web-id")
	def createNewWebId(@Context uriInfo: UriInfo,
	                   @PathParam(value = "id") userName: String): Response = {
		val ppd: UriRef = getSuggestedPPDUri(userName)
		val webId: UriRef = new UriRef(ppd.getUnicodeString + "#me")
		return AccessController.doPrivileged(new PrivilegedAction[Response] {
			def run: Response = {
				tcManager.createMGraph(ppd)
				val webIDInfo = webIdGraphsService.getWebIdInfo(webId)
				webIDInfo.localPublicUserData.addAll(
					Arrays.asList(
						new TripleImpl(ppd, RDF.`type`, FOAF.PersonalProfileDocument),
						new TripleImpl(ppd, FOAF.primaryTopic, webId))
				)
				var userInSystemGraph: GraphNode = userManager.getUserInSystemGraph(userName)
				userInSystemGraph.replaceWith(webId)
				return RedirectUtil.createSeeOtherResponse("../profile", uriInfo)
			}
		})
	}

	@POST
	@Path("addContact")
	def addContact(@Context uriInfo: UriInfo,
	               @FormParam("webId") newContacts: java.util.List[UriRef]): Response = {
		import collection.JavaConversions._
		if (newContacts.size > 0) {
			val subject: Subject = UserUtil.getCurrentSubject
			var me: GraphNode = AccessController.doPrivileged(new PrivilegedAction[GraphNode] {
				def run: GraphNode = {
					return userManager.getUserGraphNode(subject)
				}
			})
			for (contactWebID <- newContacts) {
				val webIdGraphs = webIdGraphsService.getWebIdInfo(me.getNode.asInstanceOf[UriRef])
				var meGrph: GraphNode = new GraphNode(me.getNode, webIdGraphs.localPublicUserData)
				meGrph.addProperty(FOAF.knows, contactWebID)
			} //todo: one should catch errors here (bad uris sent for ex
		}
		return RedirectUtil.createSeeOtherResponse("../profile", uriInfo)
	}

	/**
	 * @parm webId: A list of WebIDs to be added as Subject Alternative Names
	 * @param cn Common Name, the name that usually appears in the certificate selection box
	 * @param spkac key request in format generated by  Netscape, Safari, Opera
	 * @param crmf hey request in format generated by M$ Explorer
	 * @param csr key request as generated by Javascript of Netscape
	 * @param hours the certificate should last  (hours and days add up)
	 * @param days the certificate should last
	 * @param comment a comment to be attached to the public key in the database
	 */
	@POST
	@Path("keygen")
	def createCert(@FormParam("webId") webIds: java.util.List[URI],
	               @FormParam("cn") commonName: String,
	               @FormParam("spkac") spkac: String,
	               @FormParam("crmf") crmf: String,
	               @FormParam("csr") csr: String,
	               @FormParam("hours") hours: String,
	               @FormParam("days") days: String,
	               @FormParam("comment") comment: String): Response = {
		import scala.collection.JavaConversions._
		var cert: Certificate = null
		if (spkac != null && spkac.length > 0) {
			cert = keygenSrvc.createFromSpkac(spkac)
			if (cert == null) {
				logger.warn("unable to create certificate from spkac request")
			}
		}
		if (cert == null && crmf != null && crmf.length > 0) {
			cert = keygenSrvc.createFromCRMF(crmf)
			if (cert == null) {
				logger.warn("unable to create certificate from crmf requrest :" + crmf)
			}
		}
		if (cert == null && csr != null && csr.length > 0) {
			cert = keygenSrvc.createFromPEM(csr)
			if (cert == null) {
				logger.warn("unable to create certificate from csr request :" + csr)
			}
		}
		if (cert == null) {
			throw new RuntimeException("The server was unable to create a certificate")
		}
		cert.setSubjectCommonName(commonName)
		cert.addDurationInHours(hours)
		cert.addDurationInDays(days)
		cert.startEarlier("2")
		for(san: URI<-webIds) {
		  cert.addSubjectAlternativeName(san.toString)
		}
		var ser: CertSerialisation = null
		try {
			ser = cert.getSerialisation
		}
		catch {
			case ex: Exception => {
				throw new RuntimeException(ex)
			}
		}
		var pubKey: RSAPublicKey = cert.getSubjectPublicKey.getPublicKey.asInstanceOf[RSAPublicKey]
		var publicExponent: BigInteger = pubKey.getPublicExponent
		var modulus: BigInteger = pubKey.getModulus

		for (webid: URI<-webIds
		     if (webid.getScheme=="https"||webid.getScheme=="http");
	  		  val webidRef = new UriRef(webid.toString);
		     val webIdInfo = webIdGraphsService.getWebIdInfo(webidRef);
		     if (webIdInfo.isLocal)
		) {
			val certNode = new EasyGraph(webIdInfo.localPublicUserData).bnode
			( certNode ∈  RSA.RSAPublicKey
			   ⟝ CERT.identity ⟶  webidRef
			   ⟝ RSA.modulus ⟶  modulus
			   ⟝ RSA.public_exponent ⟶  publicExponent
			   ⟝ DC.date ⟶  cert.getStartDate )
			if (comment != null && comment.length > 0) {
				certNode ⟝  RDFS.comment ⟶  comment
			}
		}
		var resBuild: Response.ResponseBuilder = Response.ok(ser.getContent, MediaType.valueOf(ser.getMimeType))
		return resBuild.build
	}

	@POST
	@Path("deletekey")
	def deleteKey(@Context uriInfo: UriInfo,
	              @FormParam("webId") webId: UriRef,
	              @FormParam("keyhash") keys: List[String]): Response = {
		val webIDInfo = webIdGraphsService.getWebIdInfo(webId)
		val agent: GraphNode = new GraphNode(webId, webIDInfo.localPublicUserData)
		var subjects: Iterator[GraphNode] = agent.getSubjectNodes(CERT.identity)
		import scala.util.control.Breaks._
		breakable {
			import scala.collection.JavaConversions._
			//to for loop through iterators
			for (nl <- subjects) {
				var modulusIt: Iterator[Resource] = nl.getObjects(RSA.modulus)
				if (!modulusIt.hasNext) break
				var modLit: Resource = modulusIt.next
				if (modulusIt.hasNext) logger.warn("data error, a modulus too many in cert for " + webId)
				if (!(modLit.isInstanceOf[TypedLiteral])) {
					logger.warn("a public key has a modulus that is not a literal for " + webId)
					break
				}
				var modulus: BigInteger = LiteralFactory.getInstance.createObject(classOf[BigInteger], modLit.asInstanceOf[TypedLiteral])
				for (key <- keys) {
					if (modulus.hashCode == Integer.decode(key)) {
						nl.deleteNodeContext
						break
					}
				}
			}
		}
		return RedirectUtil.createSeeOtherResponse("../profile", uriInfo)
	}

	@POST
	@Path("modify")
	def modifyProfile(@Context uriInfo: UriInfo,
	                  @PathParam("id") userName: String,
	                  @FormParam("webId") webId: UriRef,
	                  @FormParam("name") name: String,
	                  @FormParam("description") description: String): Response = {
		val webIDInfo = AccessController.doPrivileged(new PrivilegedAction[WebIdInfo] {
			def run = {
				webIdGraphsService.getWebIdInfo(webId)
			}
		})
		val agent: GraphNode = new GraphNode(webId, webIDInfo.localPublicUserData)
		agent.deleteProperties(FOAF.name)
		agent.addPropertyValue(FOAF.name, name)
		agent.deleteProperties(DC.description)
		agent.addPropertyValue(DC.description, description)
		logger.debug("local graph (uri: {}) is now of size {}".format(webIDInfo.webId, webIDInfo.localPublicUserData.size))
		RedirectUtil.createSeeOtherResponse("../profile", uriInfo)
	}

	protected def bindUserManager(usermanager: UserManager): Unit = {
		userManager = usermanager
	}

	protected def unbindUserManager(usermanager: UserManager): Unit = {
		if (userManager == usermanager) {
			userManager = null
		}
	}

	protected def bindKeygenSrvc(keygenservice: KeygenService): Unit = {
		keygenSrvc = keygenservice
	}

	protected def unbindKeygenSrvc(keygenservice: KeygenService): Unit = {
		if (keygenSrvc == keygenservice) {
			keygenSrvc = null
		}
	}


	protected def bindWebIdGraphsService(webidgraphsservice: WebIdGraphsService): Unit = {
		webIdGraphsService = webidgraphsservice
	}

	protected def unbindWebIdGraphsService(webidgraphsservice: WebIdGraphsService): Unit = {
		webIdGraphsService = null
	}

	protected def bindPlatformConfig(platformconfig: PlatformConfig): Unit = {
		platformConfig = platformconfig
	}

	protected def unbindPlatformConfig(platformconfig: PlatformConfig): Unit = {
		if (platformConfig == platformconfig) {
			platformConfig = null
		}
	}

	protected def bindTcManager(tcManager: TcManager) = {
		this.tcManager = tcManager
	}

	protected def unbindTcManager(tcManager: TcManager) = {
		this.tcManager = null
	}

	protected def activate(componentContext: ComponentContext): Unit = {
		this.componentContext = componentContext
	}


	private var userManager: UserManager = null

	private var webIdGraphsService: WebIdGraphsService = null

	private var keygenSrvc: KeygenService = null
	private var platformConfig: PlatformConfig = null


	private var componentContext: ComponentContext = null

	private var tcManager: TcManager = null;

}