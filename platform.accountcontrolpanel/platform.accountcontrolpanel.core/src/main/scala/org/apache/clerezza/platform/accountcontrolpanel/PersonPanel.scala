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

import java.util.ArrayList
import java.util.Arrays
import java.util.Iterator
import org.apache.clerezza.platform.security.UserUtil
import org.apache.clerezza.ssl.keygen.CertSerialisation
import org.apache.clerezza.ssl.keygen.Certificate
import org.apache.clerezza.foafssl.ontologies.CERT
import org.apache.clerezza.foafssl.ontologies.RSA
import org.apache.clerezza.jaxrs.utils.RedirectUtil
import org.apache.clerezza.jaxrs.utils.TrailingSlash
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL
import org.apache.clerezza.platform.config.PlatformConfig
import org.apache.clerezza.platform.typerendering.RenderletManager
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet
import org.apache.clerezza.platform.usermanager.UserManager
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.core.impl.TripleImpl
import org.apache.clerezza.rdf.ontologies.DC
import org.apache.clerezza.rdf.ontologies.FOAF
import org.apache.clerezza.rdf.ontologies.PLATFORM
import org.apache.clerezza.rdf.ontologies.RDF
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.apache.clerezza.web.fileserver.FileServer
import org.apache.felix.scr.annotations.Component
import org.apache.felix.scr.annotations.Property
import org.apache.felix.scr.annotations.Reference
import org.apache.felix.scr.annotations.Service
import org.osgi.service.component.ComponentContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.ws.rs._
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import java.math.BigInteger
import java.net.URL
import java.security.AccessController
import java.security.PrivilegedAction
import java.security.interfaces.RSAPublicKey
import java.util.List
import org.apache.clerezza.platform.typerendering.scala.PageRenderlet
import org.apache.clerezza.rdf.ontologies.RDFS
import org.apache.clerezza.ssl.keygen.KeygenService

object PersonPanel {
	private val logger: Logger = LoggerFactory.getLogger(classOf[ProfilePanel])
}

/**
 * Presents a panel where the user can create a webid and edit her profile.
 *
 * @author bblfish
 */
@Path("/user/{id}/people")
class PersonPanel  {

	import PersonPanel.logger

	protected def activate(componentContext: ComponentContext): Unit = {
//		this.componentContext = componentContext.
	}

	@GET
	def viewPerson(@Context uriInfo: UriInfo,
						@QueryParam("uri") uri: UriRef): GraphNode = {
		if (uri != null) {//show some error page
			System.out.println("uri =="+uri.getUnicodeString)
		}

		//val foaf = descriptionProvider.fetchSemantics(uri, Cache.Fetch)
		//so here the initial fetch could be used to decide if information is available at all,
		//ie, if the URL is accessible, if there are error conditions - try later for example...


		//Here we make a BNode the subject of the properties as a workaround to CLEREZZA-447
		val resultNode: GraphNode = new GraphNode(new UriRef(uriInfo.getRequestUri().toString),new SimpleMGraph())
		resultNode.addProperty(RDF.`type`, PLATFORM.HeadedPage)
		resultNode.addProperty(RDF.`type`, CONTROLPANEL.ProfileViewerPage)
		resultNode.addProperty(FOAF.primaryTopic,uri)
//		val result = new GraphNode(new UriRef(uri.getUnicodeString),resultNode.fetchSemantics)
	   return resultNode
	}



	protected def bindRenderletManager(renderletmanager: RenderletManager): Unit = {
		renderletManager = renderletmanager
	}

	protected def unbindRenderletManager(renderletmanager: RenderletManager): Unit = {
		if (renderletManager == renderletmanager) {
			renderletManager = null
		}
	}


	//called by the ssp, web component
	private var renderletManager: RenderletManager = null

}