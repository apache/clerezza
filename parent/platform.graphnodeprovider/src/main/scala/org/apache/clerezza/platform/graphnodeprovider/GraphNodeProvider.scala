package org.apache.clerezza.platform.graphnodeprovider

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

import java.net.{HttpURLConnection, URL}
import org.slf4j.scala._
import org.apache.clerezza.rdf.core.access._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.platform.config.PlatformConfig
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.platform.users.WebIdGraphsService
import org.apache.clerezza.rdf.utils.{UnionMGraph, UriMutatingTripleCollection, GraphNode}
import java.util.concurrent.locks.Lock
import org.apache.clerezza.platform.usermanager.UserManager
import java.security.{PrivilegedAction, AccessController}

/**
 * A service that returns a GraphNode for a specified named resource, the returned GraphNode has
 * as BaseGraph the ContentGraph provided by the ContentGraphProvider and the for remote uris the
 * Graphs they dereference to and for local URIs with a path-section starting with /user/{username}/
 * the local-public-graph of that user.
 */
class GraphNodeProvider extends Logging {

	/**
	 * Get a GraphNode for the specified resource, see class comments for details.
	 */
	def get(uriRef: UriRef): GraphNode = {
		val uriString = uriRef.getUnicodeString
		val isLocal: Boolean = {
			import scala.collection.JavaConversions._
			//we assume all non http* uris to be local
			!uriString.toLowerCase.startsWith("http") || platformConfig.getBaseUris.exists(baseUri => uriString.startsWith(baseUri.getUnicodeString))
		}
		get(uriRef, isLocal)
	}

	/**
	 * Get a GraphNode for the specified resource, The resource is assumed to be local, i.e. the method behaves like
	 * get(UriRef) for a Uri with an authority section contained in the Set retuned by
	 * <code>org.apache.clerezza.platform.config.PlatformConfig#getBaseUris()</code>
	 */
	def getLocal(uriRef: UriRef): GraphNode = {
		get(uriRef, true)
	}

	private def get(uriRef: UriRef, isLocal: Boolean): GraphNode = {
		val uriString = uriRef.getUnicodeString
		def existsInGraph(nodeUri: UriRef, tc: LockableMGraph): Boolean =
			{
				var readLock: Lock = tc.getLock.readLock
				readLock.lock
				try {
					return tc.filter(nodeUri, null, null).hasNext || tc.filter(null, null, nodeUri).hasNext
				}
				finally {
					readLock.unlock
				}
			}

		val uriPath = {
			val uri = new java.net.URI(uriString)
			uri.getPath
		}

		lazy val uriPrefix = {
			val uri = new java.net.URI(uriString)
			uri.getScheme+"://"+uri.getAuthority
		}

		val anyHostUri = new UriRef(Constants.ALL_HOSTS_URI_PREFIX + uriPath)

		var mGraphs: List[TripleCollection] = Nil

		def addToUnion(mGraph: LockableMGraph) {
			//adding uncondinionately if (existsInGraph(uriRef, mGraph)) {
			mGraphs ::= mGraph
			//}
			if (isLocal) {
				if (existsInGraph(anyHostUri, mGraph)) {
					mGraphs ::= new UriMutatingTripleCollection(mGraph, Constants.URN_LOCAL_INSTANCE, uriPrefix)
				}
			}
		}

		val cgGraph = cgProvider.getContentGraph

		addToUnion(cgGraph)

		if (isLocal && uriPath.startsWith("/user/")) {
			val nextSlash = uriPath.indexOf('/',6)		
			if (nextSlash != -1) {
				val userName = uriPath.substring(6, nextSlash)
				val webIdOption = AccessController.doPrivileged(new PrivilegedAction[Option[UriRef]]() {
						def run(): Option[UriRef] = {
							val userNode: GraphNode = userManager.getUserInSystemGraph(userName)
							if (userNode != null) {
								userNode.getNode match {
									case u: UriRef => Some(u)
									case _ => None
								}
							} else {
								None
							}
						}
					}
				)
				webIdOption match {
					case Some(u) => {
						val webIdInfo = webIdGraphsService.getWebIdInfo(u)
						addToUnion(webIdInfo.localPublicUserData)
					}
					case None => ;
				}
			}
		}

		if (!isLocal) {
			/**
			 * As the resource might identify something other than a document we use this to find the redirect location
			 */
			lazy val redirectLocationString = {
				val acceptHeader = "application/rdf+xml,*/*;q.1"
				val url = new URL(uriString)
				val connection = url.openConnection()
				connection match {
					case hc : HttpURLConnection => {
							hc.setRequestMethod("HEAD");
							hc.setInstanceFollowRedirects(false)
							hc.addRequestProperty("Accept",  acceptHeader)
							hc.getResponseCode match {
								case HttpURLConnection.HTTP_SEE_OTHER  => {
										val location = hc.getHeaderField("Location")
										if (location == null) {
											throw new RuntimeException("No Location Headers in 303 response")
										}
										location
									}
								case _ => uriString
							}
						}
					case _ => uriString
				}
			}

			//TODO add method to WebProxy to get the graph location location
			val graphUriString = {
				val hashPos = uriString.indexOf('#')
				if (hashPos != -1) {
					uriString.substring(0, hashPos)
				} else {
					redirectLocationString
				}
			}
			
			addToUnion(tcManager.getMGraph(new UriRef(graphUriString)))
		}

		val unionMGraph = new UnionMGraph(mGraphs:_*);
		new GraphNode(uriRef, unionMGraph)
	}

	private var tcManager: TcManager = null;

	protected def bindTcManager(tcManager: TcManager) = {
		this.tcManager = tcManager
	}

	protected def unbindTcManager(tcManager: TcManager) = {
		this.tcManager = null
	}

	private var platformConfig: PlatformConfig = null;

	protected def bindPlatformConfig(c: PlatformConfig) = {
		this.platformConfig = c
	}

	protected def unbindPlatformConfig(c: PlatformConfig) = {
		this.platformConfig = null
	}

	private var cgProvider: ContentGraphProvider = null
	protected def bindCgProvider(p: ContentGraphProvider) {
		this.cgProvider = p
	}
	protected def unbindCgProvider(p: ContentGraphProvider) {
		this.cgProvider = null
	}

	private var webIdGraphsService: WebIdGraphsService = null
	protected def bindWebIdGraphsService(webIdGraphsService: WebIdGraphsService): Unit = {
		this.webIdGraphsService = webIdGraphsService
	}

	protected def unbindWebIdGraphsService(webIdGraphsService: WebIdGraphsService): Unit = {
		this.webIdGraphsService = null
	}

	private var userManager: UserManager = null

	protected def bindUserManager(userManager: UserManager): Unit = {
		this.userManager = userManager
	}

	protected def unbindUserManager(userManager: UserManager): Unit = {
		this.userManager = null
	}

}
