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

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URL
import java.security.AccessControlException
import java.security.AccessController
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.PrivilegedAction
import java.util.Dictionary
import java.util.HashMap
import java.util.HashSet
import java.util.Hashtable
import java.util.Iterator
import java.util.Map
import java.util.Set
import javax.ws.rs.Consumes
import javax.ws.rs.FormParam
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.ResponseBuilder
import javax.ws.rs.core.UriInfo
import org.apache.felix.scr.annotations.Component
import org.apache.felix.scr.annotations.Property
import org.apache.felix.scr.annotations.Reference
import org.apache.felix.scr.annotations.ReferenceCardinality
import org.apache.felix.scr.annotations.ReferencePolicy
import org.apache.felix.scr.annotations.Service
import org.osgi.framework.Bundle
import org.osgi.framework.BundleException
import org.osgi.service.cm.Configuration
import org.osgi.service.cm.ConfigurationAdmin
import org.osgi.service.component.ComponentContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.clerezza.jaxrs.utils.TrailingSlash
import org.apache.clerezza.jaxrs.utils.RedirectUtil
import org.apache.clerezza.jaxrs.utils.form.FormFile
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL
import org.apache.clerezza.platform.config.SystemConfig
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.platform.typerendering.RenderletManager
import org.apache.clerezza.platform.typerendering.scala.PageRenderlet
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet
import org.apache.clerezza.rdf.core.BNode
import org.apache.clerezza.rdf.core.Graph
import org.apache.clerezza.rdf.core.Literal
import org.apache.clerezza.rdf.core.LiteralFactory
import org.apache.clerezza.rdf.core.MGraph
import org.apache.clerezza.rdf.core.NonLiteral
import org.apache.clerezza.rdf.core.Triple
import org.apache.clerezza.rdf.core.TypedLiteral
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl
import org.apache.clerezza.rdf.core.impl.SimpleLiteralFactory
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.core.impl.TripleImpl
import org.apache.clerezza.rdf.ontologies.OSGI
import org.apache.clerezza.rdf.ontologies.PERMISSION
import org.apache.clerezza.rdf.ontologies.PLATFORM
import org.apache.clerezza.rdf.ontologies.RDF
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.apache.clerezza.triaxrs.prefixmanager.TriaxrsPrefixManager

object SettingPanel {
	val logger: Logger = LoggerFactory.getLogger(classOf[SettingsPanel])
}

/**
 *
 * Account control panel
 *
 * @author mir, hasan
 */
@Path("/user/{id}/control-panel")
class SettingsPanel {

	import SettingPanel.logger

	/**
	 * Mainpage
	 *
	 * @param id is the username as given in the URL
	 * @return an array of installed {@link Bundle}s to be managed
	 *
	 */
	@GET
	def settingsPage(@PathParam(value = "id") idP: String,
						  @QueryParam("changedPassword") changedPassword: String,
						  @Context uriInfo: UriInfo): GraphNode = {
		TrailingSlash.enforceNotPresent(uriInfo)
		val id: String = idP
		var graphNode: GraphNode = null
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""))
		try {
			AccessController.checkPermission(new UserBundlePermission(id, ""))
			graphNode = AccessController.doPrivileged(new PrivilegedAction[GraphNode] {
				def run: GraphNode = {
					var bundles: Array[Bundle] = userBundles(getAgent(id))
					return asGraphNode(id, bundles)
				}
			})
		}
		catch {
			case e: AccessControlException => {
				graphNode = new GraphNode(new BNode, new SimpleMGraph)
				graphNode.addProperty(CONTROLPANEL.userBundlePermission, LiteralFactory.getInstance.createTypedLiteral(false))
			}
		}
		try {
			AccessController.checkPermission(new ChangePasswordPermission(id, ""))
			graphNode.addProperty(CONTROLPANEL.changePasswordPermission, LiteralFactory.getInstance.createTypedLiteral(true))
		}
		catch {
			case e: AccessControlException => {
				graphNode.addProperty(CONTROLPANEL.changePasswordPermission, LiteralFactory.getInstance.createTypedLiteral(false))
			}
		}
		if (changedPassword != null && changedPassword.equals("false")) {
			graphNode.addProperty(CONTROLPANEL.changedPassword, new PlainLiteralImpl("false"))
		}
		graphNode.addProperty(RDF.`type`, CONTROLPANEL.SettingsPage)
		graphNode.addProperty(RDF.`type`, PLATFORM.HeadedPage)
		return graphNode
	}

	private def addBundleDescriptionToGraph(responseGraph: MGraph, bundle: Bundle): Unit = {
		var status: TypedLiteral = LiteralFactory.getInstance.createTypedLiteral(bundle.getState)
		var bundleUri: UriRef = new UriRef(bundle.getLocation)
		var triple: Triple = new TripleImpl(bundleUri, OSGI.status, status)
		responseGraph.add(triple)
		var bundleId: TypedLiteral = LiteralFactory.getInstance.createTypedLiteral(bundle.getBundleId)
		triple = new TripleImpl(bundleUri, OSGI.bundle_id, bundleId)
		responseGraph.add(triple)
	}

	private def asGraphNode(userId: String, bundles: Array[Bundle]): GraphNode = {
		val responseGraph: MGraph = new SimpleMGraph
		for (bundle <- bundles) {
			addBundleDescriptionToGraph(responseGraph, bundle)
		}
		return AccessController.doPrivileged(new PrivilegedAction[GraphNode] {
			def run: GraphNode = {
				var userDescriptionGraph: Graph = new GraphNode(getAgent(userId), systemGraph).getNodeContext
				var unionGraph: UnionMGraph = new UnionMGraph(responseGraph, userDescriptionGraph)
				var graphNode: GraphNode = new GraphNode(getAgent(userId), unionGraph)
				graphNode.addProperty(CONTROLPANEL.userBundlePermission, LiteralFactory.getInstance.createTypedLiteral(true))
				return graphNode
			}
		})
	}

	/**
	 * Retrieves all bundles owned by a user represented by agent
	 *
	 * @param agent represents the user who owns bundles to be returned
	 * @return an array of {@link Bundle}s owned by the user
	 *
	 */
	private def userBundles(agent: NonLiteral): Array[Bundle] = {
		logger.debug("Retrieve all bundles from user: {}", agent)
		var installedBundles: Array[Bundle] = componentContext.getBundleContext.getBundles
		val locationMapper: Map[String, Long] = new HashMap[String, Long]
		for (b <- installedBundles) {
			locationMapper.put(b.getLocation, b.getBundleId)
		}
		return AccessController.doPrivileged(new PrivilegedAction[Array[Bundle]] {
			def run: Array[Bundle] = {
				var bundles: Set[Bundle] = new HashSet[Bundle]
				var agentBundles: Iterator[Triple] = systemGraph.filter(null, OSGI.owner, agent)
				while (agentBundles.hasNext) {
					val location: String = (agentBundles.next.getSubject.asInstanceOf[UriRef]).getUnicodeString
					try {
						val id: Long = locationMapper.get(location)
						bundles.add(componentContext.getBundleContext.getBundle(id))
					} catch {
						case _:NumberFormatException => None
					}
				}
				return bundles.toArray(new Array[Bundle](bundles.size))
			}
		})
	}

	private def getAgent(id: String): NonLiteral = {
		logger.debug("Get agent with id {}", id)
		var agents: Iterator[Triple] = systemGraph.filter(null, PLATFORM.userName, new PlainLiteralImpl(id))
		if (agents.hasNext) {
			return agents.next.getSubject
		}
		else {
			logger.debug("System graph does not contain user: {}", id)
			var responseBuilder: Response.ResponseBuilder = Response.ok("<html><body>User does not exist</body></html>")
			throw new WebApplicationException(responseBuilder.build)
		}
	}

	private def getAgentPathPrefix(agent: NonLiteral): PlainLiteralImpl = {
		return AccessController.doPrivileged(new PrivilegedAction[PlainLiteralImpl] {
			def run: PlainLiteralImpl = {
				var pathPrefixes: Iterator[Triple] = systemGraph.filter(agent, OSGI.agent_path_prefix, null)
				if (pathPrefixes.hasNext) {
					return pathPrefixes.next.getObject.asInstanceOf[PlainLiteralImpl]
				}
				return null
			}
		})
	}

	/**
	 * Installs a bundle from the specified location.
	 *
	 * @param id is the username as given in the URL
	 * @param location specifies the URL of the bundle to be installed
	 * @return an array of installed {@link Bundle}s to be managed
	 *
	 */
	@POST
	@Path("install-bundle")
	@Consumes def installBundle(@PathParam(value = "id") id: String, multiForm: MultiPartBody, @Context uriInfo: UriInfo): Response = {
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""))
		AccessController.checkPermission(new UserBundlePermission(id, ""))
		var formFiles: Array[FormFile] = multiForm.getFormFileParameterValues("bundle")
		var filename: String = formFiles(0).getFileName
		var bundleBytes: Array[Byte] = formFiles(0).getContent
		if (bundleBytes.length == 0) {
			var message: String = null
			if (filename.equals("")) {
				message = "No bundle specified"
			}
			else {
				message = "Bundle has length 0"
			}
			var responseBuilder: Response.ResponseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message)
			throw new WebApplicationException(responseBuilder.build)
		}
		val bundleInputStream: InputStream = new ByteArrayInputStream(bundleBytes)
		val location: String = "userbundle:" + id + "/" + filename
		logger.info("Install bundle {} to location {}", id, location)
		AccessController.doPrivileged(new PrivilegedAction[AnyRef] {
			def run: Array[Bundle] = {
				val agent: NonLiteral = getAgent(id)
				val triple: Triple = new TripleImpl(new UriRef(location), OSGI.owner, agent)
				try {
					systemGraph.add(triple)
					var bundle: Bundle = componentContext.getBundleContext.installBundle(location, bundleInputStream)
					var prefix: PlainLiteralImpl = getAgentPathPrefix(agent)
					if (prefix != null) {
						addBundlePrefix(bundle, prefix.getLexicalForm)
					}
					return null
				}
				catch {
					case ex: BundleException => {
						systemGraph.remove(triple)
						logger.debug("Failed to install a bundle from: {}", location)
						logger.error("Exception during install bundle: {}", ex)
						var responseBuilder: Response.ResponseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage)
						throw new WebApplicationException(responseBuilder.build)
					}
				}
			}
		})
		return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo)
	}

	private def addBundlePrefix(bundle: Bundle, prefix: String): Unit = {
		var symbolicName: String = bundle.getSymbolicName
		if (configAdmin != null) {
			try {
				var configuration: Configuration = configAdmin.getConfiguration(classOf[TriaxrsPrefixManager].getName)
				val properties = configuration.getProperties match {
					case null => new Hashtable[String, AnyRef]()
					case dict: Dictionary[String, AnyRef] => dict
				}
				var mappings = TriaxrsPrefixManager.parseMappings(properties.get(TriaxrsPrefixManager.TRIAXRS_MAPPINGS).asInstanceOf[Array[String]])
				logger.debug("Prefix {} added to bundle {}", prefix, symbolicName)
				mappings.put(symbolicName, prefix)
				var newMappings: Array[String] = TriaxrsPrefixManager.unparseMappings(mappings)
				properties.put(TriaxrsPrefixManager.TRIAXRS_MAPPINGS, newMappings)
				configuration.update(properties)
			}
			catch {
				case e: IOException => {
					logger.warn("Unable to update configuration: {}", e.toString)
				}
			}
		}
		else {
			logger.warn("Cannot add prefix mapping. Configuration Admin is missing")
		}
	}

	private[accountcontrolpanel] def removeBundlePrefix(bundle: Bundle): Unit = {
		var symbolicName: String = bundle.getSymbolicName
		if ((this.configAdmin != null) && (symbolicName != null)) {
			try {
				val configuration: Configuration = configAdmin.getConfiguration(classOf[TriaxrsPrefixManager].getName)
				val properties = configuration.getProperties match {
					case null => new Hashtable[String, AnyRef]()
					case dict: Dictionary[String, AnyRef] => dict
				}
				val mappings = TriaxrsPrefixManager.parseMappings(properties.get(TriaxrsPrefixManager.TRIAXRS_MAPPINGS).asInstanceOf[Array[String]])
				mappings.remove(symbolicName)
				val newMappings: Array[String] = TriaxrsPrefixManager.unparseMappings(mappings)
				properties.put(TriaxrsPrefixManager.TRIAXRS_MAPPINGS, newMappings)
				configuration.update(properties)
			}
			catch {
				case e: IOException => {
					logger.warn("Unable to update configuration: {}", e.toString)
				}
			}
		}
		else {
			logger.warn("Cannot add prefix mapping. Configuration Admin is missing")
		}
	}

	/**
	 * Starts the bundle with the specified bundle id.
	 *
	 * @param id is the username as given in the URL
	 * @param bundleIdString specifies the id of the bundle to be started
	 * @return an array of installed {@link Bundle}s to be managed
	 *
	 */
	@POST
	@Path("start-bundle")
	def startBundle(@PathParam(value = "id") idP: String,
						 @FormParam("bundleId") bundleIdStringP: String,
						 @Context uriInfo: UriInfo): Response = {
		val id: String = idP
		val bundleIdString: String = bundleIdStringP
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""))
		AccessController.checkPermission(new UserBundlePermission(id, ""))
		logger.info("Start bundle {} ", id)
		val bundleId: Long = bundleIdString.toLong
		AccessController.doPrivileged(new PrivilegedAction[AnyRef] {
			def run: Array[Bundle] = {
				try {
					val bundle: Bundle = componentContext.getBundleContext.getBundle(bundleId)
					bundle.start
				}
				catch {
					case e: BundleException => {
						logger.debug("Failed to start bundle {}", bundleIdString)
						logger.error("Exception during start bundle: {}", e)
						var responseBuilder: Response.ResponseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage)
						throw new WebApplicationException(responseBuilder.build)
					}
				}
				return null
			}
		})
		return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo)
	}

	/**
	 * Stops the bundle with the specified bundle id.
	 *
	 * @param id is the username as given in the URL
	 * @param bundleIdString specifies the id of the bundle to be stopped
	 * @return an array of installed {@link Bundle}s to be managed
	 *
	 */
	@POST
	@Path("stop-bundle") def stopBundle(@PathParam(value = "id") idP: String,
													@FormParam("bundleId") bundleIdStringP: String,
													@Context uriInfo: UriInfo): Response = {
		val id: String = idP
		val bundleIdString: String = bundleIdStringP
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""))
		AccessController.checkPermission(new UserBundlePermission(id, ""))
		logger.info("Stop bundle {}", id)
		val bundleId: Long = bundleIdString.toLong
		AccessController.doPrivileged(new PrivilegedAction[AnyRef] {
			def run: Array[Bundle] = {
				try {
					var bundle: Bundle = componentContext.getBundleContext.getBundle(bundleId)
					bundle.stop
				}
				catch {
					case e: BundleException => {
						logger.debug("Failed to stop bundle ", bundleIdString)
						logger.error("Exception during stop bundle: {}", e)
						var responseBuilder: Response.ResponseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage)
						throw new WebApplicationException(responseBuilder.build)
					}
				}
				return null
			}
		})
		return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo)
	}

	/**
	 * Uninstalls the bundle with the specified bundle id.
	 *
	 * @param id is the username as given in the URL
	 * @param bundleIdString specifies the id of the bundle to be uninstalled
	 * @return an array of installed {@link Bundle}s to be managed
	 *
	 */
	@POST
	@Path("uninstall-bundle")
	def uninstallBundle(@PathParam(value = "id") idP: String,
							  @FormParam("bundleId") bundleIdStringP: String,
							  @Context uriInfo: UriInfo): Response = {
		val id: String = idP
		val bundleIdString: String = bundleIdStringP
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""))
		AccessController.checkPermission(new UserBundlePermission(id, ""))
		logger.info("Uninstall bundle {}", id)
		val bundleId: Long = bundleIdString.toLong
		AccessController.doPrivileged(new PrivilegedAction[AnyRef] {
			def run: Array[Bundle] = {
				val agent: NonLiteral = getAgent(id)
				try {
					var bundle: Bundle = componentContext.getBundleContext.getBundle(bundleId)
					bundle.uninstall
					val triple: Triple = new TripleImpl(new UriRef(bundle.getLocation), OSGI.owner, agent)
					systemGraph.remove(triple)
					removeBundlePrefix(bundle)
				}
				catch {
					case e: BundleException => {
						logger.debug("Failed to uninstall bundle {}", bundleIdString)
						logger.error("Exception during uninstall bundle: {}", e)
						var responseBuilder: Response.ResponseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage)
						throw new WebApplicationException(responseBuilder.build)
					}
				}
				return null
			}
		})
		return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo)
	}

	/**
	 * changes the password of an user
	 *
	 * @param idP id is the username as given in the URL
	 * @param lang represents the user's new standard language.
	 * @return
	 */
	@POST
	@Path("change-language")
	def changeUserLanguage(@PathParam(value = "id") idP: String,
								  @FormParam("availablelanguages") lang: String,
								  @Context uriInfo: UriInfo): Response = {
		val id: String = idP
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""))
		AccessController.doPrivileged(new PrivilegedAction[AnyRef] {
			def run: AnyRef = {
				var userNode: GraphNode = new GraphNode(getAgent(id), systemGraph)
				userNode.deleteProperties(PLATFORM.preferredLangInISOCode)
				userNode.addProperty(PLATFORM.preferredLangInISOCode, LiteralFactory.getInstance.createTypedLiteral(lang))
				return null
			}
		})
		return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo)
	}

	/**
	 * changes the password of an user
	 *
	 * @param idP id is the username as given in the URL
	 * @param oldPW the current user password
	 * @param newPW the new password
	 * @param confirmNewPW the new password
	 * @return
	 */
	@POST
	@Path("change-password")
	def changePassword(@PathParam(value = "id") idP: String,
							 @FormParam("oldPW") oldPW: String,
							 @FormParam("newPW") newPW: String,
							 @FormParam("confirmNewPW") confirmNewPW: String,
							 @Context uriInfo: UriInfo): Response = {
		val id: String = idP
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""))
		AccessController.checkPermission(new ChangePasswordPermission(id, ""))
		var changedPassword: Boolean = false
		if (newPW.trim.equals(confirmNewPW.trim) && checkPWStrings(oldPW, newPW)) {
			changedPassword = AccessController.doPrivileged(new PrivilegedAction[Boolean] {
				private def getCurrentPassword(agent: NonLiteral): String = {
					var currentPassword: String = null
					var oldPWTriple: Iterator[Triple] = systemGraph.filter(agent, PERMISSION.passwordSha1, null)
					if (oldPWTriple.hasNext) {
						var currentPWLiteral: Literal = oldPWTriple.next.getObject.asInstanceOf[Literal]
						currentPassword = currentPWLiteral.getLexicalForm
					}
					return currentPassword
				}

				def run: Boolean = {
					val agent: NonLiteral = getAgent(id)
					var encodedOlpPW: String = getEncodedPW(oldPW)
					var currentPassword: String = getCurrentPassword(agent)
					if ((currentPassword != null) && !currentPassword.equals(encodedOlpPW)) {
						logger.info("Typed wrong current password!")
						return false
					}
					else {
						removeOldPwAndAddNewPW(agent, currentPassword, newPW)
						return true
					}
				}

				private def removeOldPwAndAddNewPW(agent: NonLiteral, currentPassword: String, newPW: String): Unit = {
					var newPWTriple: Triple = new TripleImpl(agent, PERMISSION.passwordSha1, new PlainLiteralImpl(getEncodedPW(newPW)))
					if (currentPassword != null) {
						var oldPWTriple: Triple = new TripleImpl(agent, PERMISSION.passwordSha1, new PlainLiteralImpl(currentPassword))
						systemGraph.remove(oldPWTriple)
						logger.debug("removed old password from systemgraph")
					}
					systemGraph.add(newPWTriple)
					logger.debug("user " + id + " changed password")
				}

				private def getEncodedPW(password: String): String = {
					if (password == null) {
						return null
					}
					try {
						return bytes2HexString(MessageDigest.getInstance("SHA1").digest(password.getBytes("UTF-8")))
					}
					catch {
						case e: NoSuchAlgorithmException => {
							throw new RuntimeException(e)
						}
						case e: UnsupportedEncodingException => {
							throw new RuntimeException(e)
						}
					}
				}

				private def bytes2HexString(bytes: Array[Byte]): String = {
					val HEXDIGITS: Array[Char] = "0123456789abcdef".toCharArray
					val result = new Array[Char](bytes.length << 1)
					var j: Int = 0
					for (i <- 0 to bytes.length - 1) {
						result(j) = HEXDIGITS(bytes(i) >> 4 & 0xF)
						result(j + 1) = HEXDIGITS(bytes(i) & 0xF)
						j += 2
					}
					return new String(result)
				}
			})
		}
		else {
			logger.info("Changing password failed!")
			changedPassword = false
		}
		if (changedPassword) {
			return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo)
		}
		else {
			return RedirectUtil.createSeeOtherResponse("../control-panel?changedPassword=false", uriInfo)
		}
	}

	/**
	 * checks if the typed strings are valid
	 */
	private def checkPWStrings(oldPW: String, newPW: String): Boolean = {
		if (newPW.length == 0) {
			return false
		}
		return true
	}

	/**
	 * The activate method is called when SCR activates the component configuration.
	 *
	 * @param componentContext
	 */
	protected def activate(componentContext: ComponentContext): Unit = {
		this.componentContext = componentContext
	}

	protected def bindConfigurationAdmin(configAdmin: ConfigurationAdmin): Unit = {
		logger.debug("Binding configuration admin")
		this.configAdmin = configAdmin
	}

	protected def unbindConfigurationAdmin(configAdmin: ConfigurationAdmin): Unit = {
		logger.debug("Unbinding configuration admin")
		this.configAdmin = null
	}

	protected def bindSystemGraph(mgraph: MGraph): Unit = {
		systemGraph = mgraph
	}

	protected def unbindSystemGraph(mgraph: MGraph): Unit = {
		if (systemGraph == mgraph) {
			systemGraph = null
		}
	}

	protected def bindCgProvider(contentgraphprovider: ContentGraphProvider): Unit = {
		cgProvider = contentgraphprovider
	}

	protected def unbindCgProvider(contentgraphprovider: ContentGraphProvider): Unit = {
		if (cgProvider == contentgraphprovider) {
			cgProvider = null
		}
	}

	private var componentContext: ComponentContext = null
	private var systemGraph: MGraph = null
	private var cgProvider: ContentGraphProvider = null
	private var configAdmin: ConfigurationAdmin = null
}