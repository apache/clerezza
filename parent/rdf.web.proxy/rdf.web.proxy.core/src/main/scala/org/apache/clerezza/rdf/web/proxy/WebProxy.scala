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

package org.apache.clerezza.rdf.web.proxy

import org.apache.clerezza.platform.Constants
import org.apache.clerezza.rdf.utils.GraphNode
import org.osgi.service.component.ComponentContext
import org.apache.clerezza.rdf.core.{MGraph, TripleCollection, UriRef}
import org.apache.clerezza.platform.config.PlatformConfig
import java.net.{HttpURLConnection, URL}
import org.apache.clerezza.rdf.core.access.{NoSuchEntityException, TcManager}
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat
import org.apache.clerezza.rdf.core.serializedform.Parser
import org.apache.clerezza.platform.typerendering.WebRenderingService


/**
 * The Web Proxy Service enables applications to request remote graphs. It keeps cached
 * version of them in store for faster delivery.
 *
 */
@WebRenderingService
class WebProxy {


	protected var tcManager: TcManager = null;

	protected def bindTcManager(tcManager: TcManager) = {
		this.tcManager = tcManager
	}

	protected def unbindTcManager(tcManager: TcManager) = {
		this.tcManager = null
	}

	//todo: replace this with an appropriate graph
	protected val authoritativeLocalGraphs = Constants.CONFIG_GRAPH_URI

	private var cacheMetaGraph: TripleCollection = null

	/**OSGI method, called on activation */
	protected def activate(context: ComponentContext) = {

	}

	protected def deactivate(context: ComponentContext) = {
		cacheMetaGraph = null
	}

	private var platformConfig: PlatformConfig = null;

	protected def bindPlatformConfig(c: PlatformConfig) = {
		this.platformConfig = c
	}

	protected def unbindPlatformConfig(c: PlatformConfig) = {
		this.platformConfig = null
	}

	private var parser: Parser = null

	protected def bindParser(p: Parser) = {
		parser = p
	}

	protected def unbindParser(p: Parser) = {
		parser = null
	}


	/**
	 * similar to cwm log:semantics relation. Fetches the graph associated with a URI
	 * this just returns a simple Graph for the representation at the given URI.121
	 *
	 * @param uri the URI to fetch
	 * @param update true if the local cache is to be updated, false otherwise
	 * @return the cached Node as an MGraph. (The wrapper here is Some/None, but could be more tuned for this
	 *
	 */
	def fetchSemantics(uri: UriRef, update: Cache.Value= Cache.Fetch): Option[GraphNode] = {
		val resource = getResourceInfo(uri, update)
		return try {
			Some(new GraphNode(uri,tcManager.getTriples(resource.localCacheUri)))
		} catch {
			case e: NoSuchEntityException => None
		}

	}

	/**
	 *
	 * @param uri for which info should be fetched
	 * @param update, a Cache.Value for how much to force the info for the resource. By default get what is in the cache
	 *        if this is still valid
	 * @return a resource info, more or less updated
	 */
	def getResourceInfo(uri: UriRef, update: Cache.Value = Cache.Fetch): ResourceInfo = {
		val resource = new ResourceInfo(uri)
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
  	 * A Resource Info gives us access to a number of things about a resource:
	 * its semantics in the form of a cached graphs, representation URI(s), local cache uris
	 *
	 * currently the local cache of the remote graph is named by the resource name + ".cache"
	 * the local graph that can contain extra information is the name of the resource
	 *
	 *
	 * todo? access to representations
	 * todo? should the graphs returned contain metadata about their update time?
 */
	class ResourceInfo(url: UriRef) {
		val uriString = url.getUnicodeString

		lazy val isLocal: Boolean = {
			import scala.collection.JavaConversions._
			//todo: the base uris are checked on every invocation. This seems somewhat heavy.
			platformConfig.getBaseUris.exists(baseUri => uriString.startsWith(baseUri.getUnicodeString))
		}

		/**
		 * remote graphs are cache locally with this name
		 */
		//todo: work on a cache database. It could be that a URL redirects to another resource...
		lazy val localCacheUri = {
			new UriRef(representationUri + ".cache")
		}

		// the graph containing the local cache of the resource
		//todo: watch out: if someone can just make us add graphs to tcmanager even when there is no data...
		lazy val localCache: MGraph = try {
			val g = tcManager.getMGraph(localCacheUri)
			g
		} catch {
			case e: NoSuchEntityException => tcManager.createMGraph(localCacheUri)
		}


		lazy val representationUri = {
			val hashPos = uriString.indexOf('#')
			if (hashPos != -1) {
				uriString.substring(0, hashPos)
			} else {
				finalRedirectLocation
			}
		}


		lazy val representationGraphUri = {
			new UriRef(representationUri)
		}

		private lazy val finalRedirectLocation = {
			finalRedirectLocationFor(url.getUnicodeString)
		}

		def finalRedirectLocationFor(us: String): String = {
			val url = new URL(us)
			val connection = url.openConnection()
			connection match {
				case hc: HttpURLConnection => {
					hc.setRequestMethod("HEAD");
					hc.setInstanceFollowRedirects(false)
					hc.addRequestProperty("Accept", acceptHeader)
					hc.getResponseCode match {
						case HttpURLConnection.HTTP_SEE_OTHER => {
							val location = hc.getHeaderField("Location")
							if (location == null) {
								throw new RuntimeException("No Location Headers in 303 response")
							}
							finalRedirectLocationFor(location)
						}
						case _ => us
					}
				}
				case _ => us
			}
		}

		//todo: follow redirects and keep track of them
		//todo: keep track of headers especially date and etag. test for etag similarity
		//todo: it may be important to have blank node identifiers for graphs as the same webid, when called by different
		//      agents could have very different content
		//todo: for https connection allow user to specify his webid and send his key: ie allow web server to be an agent
		//todo: add GRDDL functionality, so that other return types can be processed too
		//todo: enable ftp and other formats (though content negotiation won't work there)
		def updateLocalCache() = {
			val url = new URL(representationUri)
			val connection = url.openConnection()
			connection match {
				case hc: HttpURLConnection => hc.addRequestProperty("Accept", acceptHeader);
			}
			connection.connect()
			val in = connection.getInputStream()
			val mediaType = connection.getContentType()
			val remoteTriples = parser.parse(in, mediaType, representationGraphUri)
			localCache.clear()
			localCache.addAll(remoteTriples)
		}


		private lazy val acceptHeader = {
			import scala.collection.JavaConversions._
			(for (f <- parser.getSupportedFormats) yield {
				val qualityOfFormat = {
					f match {
						//the default, well established format
						case SupportedFormat.RDF_XML => "1.0";
						//we prefer most dedicated formats to (X)HTML, not because those are "better",
						//but just because it is quite likely that the pure RDF format will be
						//lighter (contain less presentation markup), and it is also possible that HTML does not
						//contain any RDFa, but just points to another format.
						case SupportedFormat.XHTML => "0.5";
						//we prefer XHTML over html, because parsing (should) be easier
						case SupportedFormat.HTML => "0.4";
						//all other formats known currently are structured formats
						case _ => "0.8"
					}
				}
				f + "; q=" + qualityOfFormat + ","
			}).mkString + " *; q=.1" //is that for GRDDL?
		}


	}

}

object Cache extends Enumeration {
	/**fetch if not in cache, if version in cache is out of date, or return cache */
	val Fetch = Value
	/**fetch from source whatever is in cache */
	val ForceUpdate = Value
	/**only get cached version. If none exists return empty graph */
	val CacheOnly = Value
}
