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

import org.apache.clerezza.platform.Constants
import org.apache.clerezza.rdf.core.access.NoSuchEntityException
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.osgi.service.component.ComponentContext
import org.apache.clerezza.rdf.core.{MGraph, TripleCollection, UriRef}
import org.apache.clerezza.rdf.core.impl.SimpleMGraph

//todo: this class can be generalised to a generalised semweb caching service
//there's not really a reason to have it specialised for WebIDs.
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

	/** OSGI method, called on activation */
	protected def activate(context: ComponentContext) = {	
		val baseTripleCollections = for (uri <- authoritativeLocalGraphs) yield {
			tcManager.getTriples(uri)
		}
		authoritativeLocalGraphUnion = new UnionMGraph(baseTripleCollections: _*)
	}
	
	protected def deactivate(context: ComponentContext) = {	
		authoritativeLocalGraphUnion = null
	}

	//todo: this should probably return Some[GraphNode] as it is possible that there is no URI
	//todo: or it should return an explanation of what went wrong, for user processing
	//todo: why do we really need the merge with the system graph? How doangerous is this?
	/**
	 * This graph merges remote information and local system information
	 *
	 * @param uri the URI to fetch
	 * @param update true if the local cache is to be updated, false otherwise
	 * @return the cached Node as GraphNode with the authoritativeLocalGraphUnion
	 */
	def getWebDescription(uri: UriRef, update: Cache.Value): GraphNode = {

		val grph = fetchSemantics(uri, update)
		val tc = new UnionMGraph(authoritativeLocalGraphUnion, grph, authoritativeLocalGraphUnion)
		new GraphNode(uri, tc)
		
	}

	/**
	 * similar to cwm log:semantics relation. Fetches the graph associated with a URI
	 * this just returns a simple Graph for the representation at the given URI.121
	 *
	 * todo: should this return an MGraph, or a TripleCollection, or something else?
	 *
	 * @param uri the URI to fetch
	 * @param update true if the local cache is to be updated, false otherwise
	 * @return the cached Node as an MGraph
	 *
	*/
	def fetchSemantics(uri: UriRef, update: Cache.Value): MGraph = {
			val webIdGraphs = webIdGraphsService.getWebIdGraphs(uri)
		   if (webIdGraphs.isLocal) return webIdGraphs.localGraph

		//the logic here is not quite right, as we don't look at time of previous fetch.
		update match {
			case Cache.Fetch => if (webIdGraphs.localCache.size() == 0) webIdGraphs.updateLocalCache()
			case Cache.ForceUpdate => webIdGraphs.updateLocalCache()
			case Cache.CacheOnly => {}
		}
		 return try {
			 new SimpleMGraph(tcManager.getTriples(webIdGraphs.localCacheUri))
		} catch {
			case e: NoSuchEntityException =>  new SimpleMGraph()
		}

	}

	
}

object Cache extends Enumeration {
	/** fetch if not in cache, if version in cache is out of date, or return cache */
	val Fetch = Value
	/** fetch from source whatever is in cache */
	val ForceUpdate = Value
	/** only get cached version. If none exists return empty graph */
	val CacheOnly = Value
}
