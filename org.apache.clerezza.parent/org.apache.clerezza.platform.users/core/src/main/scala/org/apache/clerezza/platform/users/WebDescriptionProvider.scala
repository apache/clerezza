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

package org.apache.clerezza.platform.users

import java.net.HttpURLConnection
import java.net.URL
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.platform.config.PlatformConfig
import org.apache.clerezza.platform.config.SystemConfig
import org.apache.clerezza.rdf.core.TripleCollection
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.core.access.NoSuchEntityException
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.serializedform.Parser
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.osgi.service.component.ComponentContext


/**
 * delivers the description of a resource. This description is based on local 
 * data as well as (cached) web-data
 */
class WebDescriptionProvider {
	
	private var webIdGraphsService: WebIdGraphsService = null
	protected def bindWebIdGraphsService(s: WebIdGraphsService) = {
		this.webIdGraphsService = s
	}
	protected def unbindWebIdGraphsService(s: WebIdGraphsService) = {
		this.webIdGraphsService = null
	}
	
	
	private var tcManager: TcManager = null;

	protected def bindTcManager(tcManager: TcManager) = {
		this.tcManager = tcManager
	}
	
	protected def unbindTcManager(tcManager: TcManager) = {
		this.tcManager = null
	}
	
	private val authoritativeLocalGraphs = Array(
		Constants.SYSTEM_GRAPH_URI,
		Constants.CONFIG_GRAPH_URI)
	
	private var authoritativeLocalGraphUnion: TripleCollection = null
	
	protected def activate(context: ComponentContext) = {	
		val baseTripleCollections = for (uri <- authoritativeLocalGraphs) yield {
			tcManager.getTriples(uri)
		}
		authoritativeLocalGraphUnion = new UnionMGraph(baseTripleCollections: _*)
	}
	
	protected def deactivate(context: ComponentContext) = {	
		authoritativeLocalGraphUnion = null
	}
	
	
	
	/**
	 * @return a GraphNode describing uri
	 */
	def getWebDescription(uri: UriRef, update: Boolean): GraphNode = {
		
		val webIdGraphs = webIdGraphsService.getWebIdGraphs(uri)
		if (update) {
					webIdGraphs.updateLocalCache()
		}
		val cacheGraphOption: Option[TripleCollection] = try {
			Some(tcManager.getTriples(webIdGraphs.localCacheUri))
		} catch {
			case e: NoSuchEntityException =>  None
		}
		val tripleCollection = cacheGraphOption match {
			case Some(g) => new UnionMGraph(authoritativeLocalGraphUnion, g)
			case None => authoritativeLocalGraphUnion
		}
		new GraphNode(uri, tripleCollection)
		
	}
	
	
}
