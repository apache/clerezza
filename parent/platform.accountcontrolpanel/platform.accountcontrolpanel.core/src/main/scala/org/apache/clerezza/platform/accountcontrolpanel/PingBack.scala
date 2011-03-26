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

import org.apache.clerezza.platform.accountcontrolpanel.ontologies.PINGBACK
import org.apache.clerezza.rdf.core.access.{NoSuchEntityException, TcManager}
import org.apache.clerezza.rdf.ontologies.{SIOC, PLATFORM, RDF}
import org.osgi.service.component.ComponentContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.apache.clerezza.rdf.core.access.security.TcPermission
import org.apache.clerezza.platform.Constants
import java.security.{PrivilegedAction, AccessController}
import javax.ws.rs.core.{Response, Context, UriInfo}
import org.apache.clerezza.rdf.core.{MGraph, UriRef}
import javax.ws.rs.{PathParam, FormParam, POST, QueryParam, GET, Path}
import java.net.URI
import org.apache.clerezza.rdf.utils.{UnionMGraph, GraphNode}
import org.apache.clerezza.rdf.core.impl.SimpleMGraph


object PingBack {
	private val logger: Logger = LoggerFactory.getLogger(classOf[PingBack])

	val classPathTemplate = classOf[PingBack].getAnnotation(classOf[Path]).value
	val regex = """\{([^}]+)\}""".r

	// taken from http://dcsobral.blogspot.com/2010/01/string-interpolation-in-scala-with.html
	def interpolate(text: String, values: String*) = {
		val it = values.iterator
		regex.replaceAllIn(text, _ => it.next)
	}
}

/**
 * The PingBack JSR311 class. Should enable the following
 * - adding pings to a list of pings to be awaited
 * - showing the rdf of the pings
 *
 * @author Henry Story
 */

@Path("/user/{id}/ping")
class PingBack {

	import PingBack._

	protected def activate(componentContext: ComponentContext): Unit = {
	}


	/**
	 * The ping form, where you can POST new pings
	 */
	def pingGraphNode(id: String, uriInfo: UriInfo): GraphNode = {
		val pingRef = new UriRef(pingCollUri(id, uriInfo))
		val resultNode: GraphNode = new GraphNode(pingRef, pingColl(pingRef))
		resultNode
	}

	@GET
	@Path("add")
	def pingForm(@Context uriInfo: UriInfo,
					 @QueryParam("uri") uri: UriRef,
					 @PathParam("id") id: String): GraphNode = {
		val resultNode: GraphNode = pingGraphNode(id, uriInfo)
		resultNode.addProperty(RDF.`type`, PLATFORM.HeadedPage)
		resultNode.addProperty(RDF.`type`, PINGBACK.Container)
		return resultNode

	}



	/**
	 * get Ping Collection
	 */
	def pingColl(pingCollRef: UriRef): MGraph = {
		AccessController.doPrivileged(new PrivilegedAction[MGraph] {
			def run: MGraph = try {
				tcManager.getMGraph(pingCollRef)
			} catch {
				//todo: getTriples should state that it throws this exception. It makes it easier to know that one should catch it
				case e: NoSuchEntityException => {
					tcManager.getTcAccessController.
						setRequiredReadPermissionStrings(pingCollRef,
						java.util.Collections.singleton(new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READ).toString))
					tcManager.createMGraph(pingCollRef)
				}
			}
		})
	}


	/**
	 * This is not written with all the tools available to someone with access to
	 * a full UriInfo implementation or Jersey's ExtendedUriInfo as triaxrs implementation
	 * UriInfoImpl has many methods that are not yet implemented.
	 * Apparently if they were one could use getMatchedURIs or one of those methods to build this
	 * more cleanly.
	 * Perhaps one should look at http://incubator.apache.org/wink/index.html implementations...
	 *
	 * Currently this assumes that the path of the class is a root class, ie, that it starts with /
	 */
	def pingCollUri(id: String, uriInfo: UriInfo): String = {
		val path = interpolate(classPathTemplate, id)
		val uriStr = uriInfo.getBaseUri.resolve(path); //a bit expensive for something so simple
		System.out.println("res=" + uriStr)
		uriStr.toString
	}


	/**
	 * Add a new Ping Item
	 */
	@POST
	def addPing(@Context uriInfo: UriInfo,
					@FormParam("source") source: UriRef,
					@FormParam("target") target: UriRef,
					@FormParam("comment") comment: String,
					@PathParam("id") id: String): Response = {

		// check that the resource pointed to, does in fact contain a reference to the resource
		// in question


		//create a new Resource for this ping (we'll use time stamps to get going)
		val pingCollStr: String = pingCollUri(id, uriInfo)
		val pingItem = new UriRef(pingCollStr + "/ts" + System.currentTimeMillis)

		//build the graph and add to the store if ok
		val itemNde: GraphNode = new GraphNode(pingItem, pingColl(new UriRef(pingCollStr)))
		itemNde.addProperty(RDF.`type`, PINGBACK.Item)
		itemNde.addProperty(PINGBACK.source, source)
		itemNde.addProperty(PINGBACK.target, target)
		itemNde.addPropertyValue(SIOC.content, comment)
		itemNde.addInverseProperty(SIOC.container_of,new UriRef(pingCollStr))

		val resultNode = new GraphNode(pingItem,new UnionMGraph(new SimpleMGraph(),itemNde.getGraph))
		resultNode.addProperty(RDF.`type`, PLATFORM.HeadedPage)
		//response
		Response.ok(resultNode).header("Content-Location",new URI(pingItem.getUnicodeString).getPath).build()
	}

	@GET
	def viewCollection(@Context uriInfo: UriInfo,
							 @PathParam("id") id: String): GraphNode = {
		val resultNode: GraphNode = pingGraphNode(id,uriInfo )
		resultNode.addProperty(RDF.`type`, PLATFORM.HeadedPage)
		resultNode.addProperty(RDF.`type`, PINGBACK.Container)
		return resultNode
	}

	@POST
	@Path("delete")
	def deleteItems(@Context uriInfo: UriInfo,
						 @PathParam("id") id: String,
						 @FormParam("item") items: java.util.List[UriRef]): GraphNode= {
		import collection.JavaConversions._

		val pingColl: GraphNode = pingGraphNode(id,uriInfo )
		//todo: verify if access is allowed
		for(item <- items) {
			 new GraphNode(item,pingColl.getGraph).deleteNodeContext
		}
		//todo: return a read only collection
		return pingColl
	}

	/**
	 *
	 */
	@GET
	@Path("{item}")
	def viewPing(@Context uriInfo: UriInfo,
					 @PathParam("id") id: String,
					 @PathParam("item") item: String): GraphNode = {

		//ITS the wrong ping collection!!!

		val resultNode: GraphNode = new GraphNode(
			new UriRef(uriInfo.getAbsolutePath.toString),
			pingColl(new UriRef(pingCollUri(id, uriInfo)))
		)
		resultNode.addProperty(RDF.`type`, PLATFORM.HeadedPage)
		resultNode.addProperty(RDF.`type`, PINGBACK.Item)
		return resultNode

	}

	protected var tcManager: TcManager = null;

	protected def bindTcManager(tcManager: TcManager) = {
		this.tcManager = tcManager
	}

	protected def unbindTcManager(tcManager: TcManager) = {
		this.tcManager = null
	}

}
