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

import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.ontologies.FOAF
import org.apache.clerezza.rdf.ontologies.PLATFORM
import org.apache.clerezza.rdf.ontologies.RDF
import org.apache.clerezza.rdf.utils.GraphNode
import org.osgi.service.component.ComponentContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.ws.rs._
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo


object PersonPanel {
	private val logger: Logger = LoggerFactory.getLogger(classOf[PersonPanel])
}

/**
 * Presents a panel where the user can create a webid and edit her profile.
 *
 * @author bblfish
 */
@Path("/user/{id}/people")
class PersonPanel {
	protected def activate(componentContext: ComponentContext): Unit = {
//		this.componentContext = componentContext
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




}