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
import org.osgi.service.component.ComponentContext
import org.apache.clerezza.rdf.core.access.security.TcPermission
import org.apache.clerezza.platform.Constants
import java.security.{PrivilegedAction, AccessController}
import javax.ws.rs.core.{Response, Context, UriInfo}
import org.apache.clerezza.rdf.scala.utils.{EasyGraphNode, EasyGraph}
import java.net._
import org.slf4j.scala.Logger
import javax.ws.rs._
import java.io.{IOException, OutputStreamWriter}
import collection.JavaConversions._
import org.apache.clerezza.rdf.ontologies.{SIOC, PLATFORM, RDF}
import org.apache.clerezza.rdf.core.{UriRef, MGraph}
import org.apache.clerezza.rdf.utils.{UnionMGraph, GraphNode}
import org.apache.clerezza.rdf.core.impl.SimpleMGraph

object PingBack {
	private val log: Logger = Logger(classOf[PingBack])
	val ProxyForm = new UriRef(PINGBACK.THIS_ONTOLOGY+"ProxyForm")

	val pingPathTemplate = classOf[PingBack].getAnnotation(classOf[Path]).value
	val regex = """\{([^}]+)\}""".r

	// taken from http://dcsobral.blogspot.com/2010/01/string-interpolation-in-scala-with.html
	def interpolate(text: String, values: String*) = {
		val it = values.iterator
		regex.replaceAllIn(text, _ => it.next)
	}

	/**
	 * replace the name in the path with the id of the user, in order to get his ping collection
	 * and return the full uri for it
	 *
	 * This also suggests that support for relative URIs in the graphs should be supported
	 *
	 * This is not written with all the tools available to someone with access to
	 * a full UriInfo implementation or Jersey's ExtendedUriInfo as triaxrs implementation
	 * UriInfoImpl has many methods that are not yet implemented.
	 * Apparently if they were one could use getMatchedURIs or one of those methods to build this
	 * more cleanly.
	 * Perhaps one should look at http://incubator.apache.org/wink/index.html implementations...
	 *
	 * Currently this assumes that the path of the class is a root class, ie, that it starts with /
	 *
	 * @param id the value of the parameter to replace in the string taken from the @Path in the class
	 * @param uriInfo the info from the method that called this
	 * @return the full URI  of the ping collection as a string
	 */
	def pingCollUri(id: String, uriInfo: UriInfo): String = {
		val path = interpolate(pingPathTemplate, id)
		val uriStr = uriInfo.getBaseUri.resolve(path); //a bit expensive for something so simple
		uriStr.toString
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
	 * The ping collection graph, where new pings can be posted and saved
	 * @param id: the user id
	 * @param uriInfo jax-rs info
	 */
	def  pingCollection(id: String, uriInfo: UriInfo): EasyGraphNode = {
		val pingRef = new UriRef(pingCollUri(id, uriInfo))
		val pingCollG: EasyGraph = pingColl(pingRef)
		pingCollG(pingRef)
	}

	@GET
	@Path("add")
	def pingForm(@Context uriInfo: UriInfo,
					 @QueryParam("uri") uri: UriRef,
					 @PathParam("id") id: String): GraphNode = {

		( pingCollection(id, uriInfo) ∈ PLATFORM.HeadedPage
				∈ PINGBACK.Container )
	}


	/**
	 * get Ping Collection
	 */
	def pingColl(pingCollRef: UriRef): EasyGraph = {
		val tcgraph =AccessController.doPrivileged(new PrivilegedAction[MGraph] {
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
		new EasyGraph(tcgraph)
	}

	/**
	 * send a ping to another endpoint. Could go directly to that ping outbox and fill in information
	 * there, but the UI is not friendly.
	 */
	@POST
	@Produces(Array("text/plain"))
	@Path("out")
	def pingSomeone(@FormParam("to") pingTo: UriRef,
	                @FormParam("source") source: UriRef,
	                @FormParam("target") target: UriRef,
	                @FormParam("comment") comment: String): String = {
		val wr = new StringBuilder()
		wr append "Sent ping to " append pingTo append "\r\n"
		wr append "with following parameters:"
		wr append "Source=" append source append  "\r\n"
		wr append "target=" append target append "\r\n"
		wr append "comment=" append comment append "\r\n"

		val pingToGraph = tcManager.getGraph(pingTo)
		//initially I just test if something about pingback is there.
		//todo: make the subject the resource itself.
		val filter = pingToGraph.filter(null, RDF.`type`, PINGBACK.Container)
		val res = if (filter.hasNext) try {
			 val to = new URL(pingTo.getUnicodeString)
			 if (to.getProtocol == "http" || to.getProtocol == "https") {
				 val toReq = to.openConnection().asInstanceOf[HttpURLConnection]
				 toReq.setConnectTimeout(2 * 1000)
				 toReq.setReadTimeout(5 * 1000)
				 postData(toReq,
					 Map("source" -> source.getUnicodeString,
						 "target" -> source.getUnicodeString,
						 "comment" -> comment))
				 wr append  "\r\n"
				 wr append "response is"
				 wr append  "\r\n"
				 for ((header, list) <- toReq.getHeaderFields) {
					 for (e <- list) wr append header append ":" append e append "\r\n"
				 }
				 wr append("\r\n\r\n")
				 wr append { for ( line <- new scala.io.BufferedSource(toReq.getInputStream).getLines) yield line }.mkString("\r\n")
				 wr
			 } else {
				 "wrong URL type" + pingTo
			 }

		} catch {
			case e: MalformedURLException =>  "error: was asked to ping an endpoint with a malformed URL "+pingTo
			case io: IOException => "IO exception connecting to "+io.toString
			case t: SocketTimeoutException => "Connection is taking too long to "+pingTo
		} else { //not a pingback endpoint
		   "the endpoint does not specify itself as a pingback endpoint"
		}
		wr append res
		wr.toString()
	}


	private def postData(conn: HttpURLConnection, attrVals: Map[String,String]) {
		conn.setDoOutput(true)
		conn.connect()
		val wr =  new OutputStreamWriter(conn.getOutputStream())
		wr.write(encodePostData(attrVals))
		wr.flush
      wr.close
	}

	private def encodePostData(data: Map[String, String]) = {
		import java.net.URLEncoder.encode
    (for ((name, value) <- data) yield encode(name, "UTF-8") + "=" + encode(value, "UTF-8")).mkString("&")
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
		val pingCollUriStr: String = pingCollUri(id, uriInfo)
		val pingItem = new UriRef(pingCollUriStr + "/ts" + System.currentTimeMillis)

		//build the graph and add to the store if ok
		val pingColGr = pingColl(new UriRef(pingCollUriStr))
		val item = (
			pingColGr(pingItem) ∈ PINGBACK.Item
		         ⟝ PINGBACK.source ⟶  source
		         ⟝ PINGBACK.target ⟶  target
		         ⟝ SIOC.content ⟶  comment
		         ⟵ SIOC.container_of ⟞ pingCollUriStr)

		val resultNode = item.protect() ∈ PLATFORM.HeadedPage

		//response
		Response.ok(resultNode).header("Content-Location",new URI(pingItem.getUnicodeString).getPath).build()
	}



	/**
	  * view a collection. This will return a collection of pings and a form
	  * @param to if this is set then it will filter the pings sent to that endpoint and the form will sent something there
	  * @param uriInfo: set by jsr311
	  * @return a GraphNode -- still to be worked out what should be put in there precisely
	  */
	@GET
	def viewCollection(@Context uriInfo: UriInfo,
	                   @QueryParam("to") to: UriRef,
							 @PathParam("id") id: String): GraphNode = {
		val gn = (pingCollection(id,uriInfo ) ∈ PLATFORM.HeadedPage
										∈  PINGBACK.Container )
		if (to != null)	gn  ⟝ PINGBACK.to ⟶ to
		else gn
	}


	/**
	 * display a simple ping form for pings by this user where the 'source' refers to the 'target'
	 * and the 'target' has a known ping endpoint (to be found in the web cache). A missing source
	 * indicates the source is this user. The form will display a bit of information about the target
	 * (initially an agent of some sort)
	 *
	 * The form will then be posted to a local forwarding endpoint or directly to the remote endpoint
	 * (to be decided) Remote posting means the WebId in the users browser can be used directly for auth
	 * but the UI interface is not in the users control,
	 *
	 * @param source the source of the ping. The thing that is referring to the target that will be pinged
	 * @param target the target of the ping. The thing that the source is referring to (talking about).
	 *        If possible the form should describe the target somewhat. The user knows he is sending the message
	 */
	@GET
	@Path("new")
	def pingSomeone(@QueryParam("source") source: UriRef,
		             @QueryParam("target") target: UriRef,
		             @Context uriInfo: UriInfo ,
		             @PathParam("id") id: String): GraphNode = {
	   //get the source graph
		 val targetGrph = tcManager.getMGraph(target)
		(
			new EasyGraph(new UnionMGraph(new SimpleMGraph(),targetGrph)).bnode ∈ PLATFORM.HeadedPage
			                          ∈ ProxyForm
				⟝ PINGBACK.source ⟶ { if (source == null) ProfilePanel.webID(id,uriInfo) else source }
				⟝ PINGBACK.target ⟶ target
		)
	}

	@POST
	@Path("delete")
	def deleteItems(@Context uriInfo: UriInfo,
						 @PathParam("id") id: String,
						 @FormParam("item") items: java.util.List[UriRef]): GraphNode= {

		val pingColl: GraphNode = pingCollection(id,uriInfo )
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
					 @PathParam("item") item: String): EasyGraphNode = {

		//ITS the wrong ping collection!!!

		val pinG = pingColl(new UriRef(pingCollUri(id, uriInfo)))
		( pinG(new UriRef(uriInfo.getAbsolutePath.toString)) ∈ PLATFORM.HeadedPage
								∈ PINGBACK.Item
			)
	}

	protected var tcManager: TcManager = null;

	protected def bindTcManager(tcManager: TcManager) = {
		this.tcManager = tcManager
	}

	protected def unbindTcManager(tcManager: TcManager) = {
		this.tcManager = null
	}

	}
