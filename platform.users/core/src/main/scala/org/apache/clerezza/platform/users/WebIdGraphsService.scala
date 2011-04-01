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

import java.security.AccessController
import java.security.PrivilegedAction
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.platform.config.SystemConfig
import org.apache.clerezza.rdf.core.MGraph
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.core.access.NoSuchEntityException
import org.apache.clerezza.rdf.core.access.SecuredMGraph
import org.apache.clerezza.rdf.core.access.security.TcPermission
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.ontologies.PLATFORM
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.apache.clerezza.rdf.web.proxy.{Cache, WebProxy}

//todo: this class can be generalised to a generalised semweb caching service
/**
 * For agents with a Web-Id various graphs are available, these graphs are
 * grouped by <code>WebIdGraphs</code> which this service provides.
 */
class WebIdGraphsService extends WebProxy {



	//todo: Here we duplicate the WebProxy Method and extend the ResourceInfo objects
	//The advantage is that it shows clearly what code is specific to WebIDs.
	//But it also feels a bit dangerous to extend inner classes like this.

	/**
	 *
	 * @param uri for which info should be fetched
	 * @param update, a Cache.Value for how much to force the info for the resource. By default get what is in the cache
	 *        if this is still valid
	 * @return a resource info, more or less updated
	 */
	def getWebIDInfo(uri: UriRef, update: Cache.Value = Cache.Fetch): WebIDInfo = {
		val resource = new WebIDInfo(uri)
		if (resource.isLocal) return resource

		//the logic here is not quite right, as we don't look at time of previous fetch.
		update match {
			case Cache.Fetch => if (resource.localCache.size() == 0) resource.updateLocalCache()
			case Cache.ForceUpdate => resource.updateLocalCache()
			case Cache.CacheOnly => {}
		}
		return resource
	}


	/**
	 * A Proxy for WebID Resource info
	 */
	class WebIDInfo(webId: UriRef) extends ResourceInfo(webId) {
		/**
		 * Returns the graph with triples to which public read access can be granted.
		 *
		 * This will return a union of the following graphs.
		 * - minimum graph constructed for system graph
		 * - cached version of profile document from web, if available
		 * - as read/write graph: the public personal profile graph
		 *
		 * @return a GraphNode describing webId
		 */
		def publicUserGraph: MGraph = {
			def systemTriples = {
				val systemGraph = tcManager.getMGraph(Constants.SYSTEM_GRAPH_URI)
				val triples = systemGraph.filter(webId, PLATFORM.userName, null)
				val result = new SimpleMGraph
				while (triples.hasNext) {
					result.add(triples.next())
				}
				result
			}
			AccessController.doPrivileged(new PrivilegedAction[MGraph]() {
				def run() = {
					val unionGraph = if (isLocal) {
						new UnionMGraph(localGraph, systemTriples)
					} else {
						new UnionMGraph(localGraph, localCache, systemTriples)
					}
					new SecuredMGraph(unionGraph, localGraphUri, tcManager.getTcAccessController)
				}
			})
		}

		/**
		 * the graph for putting local information in addition to the remote graph
		 */
		lazy val localGraphUri = {
			new UriRef(representationUri+".graph")
		}




		//for the WebID Graph this is the place where local information in addition to remote
		//information is stored.
		//for the WebProxy would we want this to be the name for the local cache?
		lazy val localGraph = try {
			tcManager.getMGraph(localGraphUri)
		} catch {
			case e: NoSuchEntityException => {
				import scala.collection.JavaConversions._
				tcManager.getTcAccessController.
					setRequiredReadPermissionStrings(localGraphUri,
					List(new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READ).toString))
				tcManager.createMGraph(localGraphUri)
			}
		}


	}

}