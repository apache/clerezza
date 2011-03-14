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
import java.security.AccessController
import java.security.PrivilegedAction
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.platform.config.PlatformConfig
import org.apache.clerezza.platform.config.SystemConfig
import org.apache.clerezza.rdf.core.MGraph
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.core.access.NoSuchEntityException
import org.apache.clerezza.rdf.core.access.SecuredMGraph
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.access.security.TcPermission
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.core.serializedform.Parser
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat
import org.apache.clerezza.rdf.ontologies.PLATFORM
import org.apache.clerezza.rdf.utils.UnionMGraph


//todo: this class can be generalised to a generalised semweb caching service
/**
 * For agents with a Web-Id various graphs are available, these graphs are
 * grouped by <code>WebIdGraphs</code> which this service provides.
 */
class WebIdGraphsService() {
	
	private var parser: Parser = null
	
	protected def bindParser(p: Parser) = {
		parser = p
	}
	
	protected def unbindParser(p: Parser) = {
		parser = null
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
				f+"; q="+qualityOfFormat+","
			}).mkString +" *; q=.1"  //is that for GRDDL?
	}
	
	def getWebIdGraphs(webId: UriRef): WebIdGraphs = {
		new WebIdGraphs(webId)
	}
	
	class WebIdGraphs(webId: UriRef) {
		val uriString = webId.getUnicodeString
		
		lazy val isLocal: Boolean = {
			import scala.collection.JavaConversions._
			platformConfig.getBaseUris.exists(baseUri => uriString.startsWith(baseUri.getUnicodeString))
		}
		
		/**
		 * remote graphs are cache locally with this name
		 */
		lazy val localCacheUri = {
			new UriRef(representationGraphUriString+".cache")
		}
		
		lazy val localCache = try {
			val g = tcManager.getMGraph(localCacheUri)
			g
		} catch {
			case e: NoSuchEntityException => tcManager.createMGraph(localCacheUri)
		}
	
		lazy val representationGraphUriString = {
			val hashPos = uriString.indexOf('#')
			if (hashPos != -1) {
				uriString.substring(0, hashPos)
			} else {
				finalRedirectLocation
			}
		}
		
		/**
		 * for web-ids with a # same as representationGraphUriString
		 */
		lazy val localGraphUri = {
			new UriRef(localGraphUriString)
		}

		//TODO: One should do some reasoning somewhere for URIs that have redirects, such a http://xmlns.com/foaf/knows
		lazy val localGraphUriString = {
			val hashPos = uriString.indexOf('#')
			if (hashPos != -1) {
				uriString.substring(0, hashPos)
			} else {
				uriString
			}
		}
		
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
		
		lazy val representationGraphUri = {
			new UriRef(representationGraphUriString)
		}
		private lazy val finalRedirectLocation = {
			finalRedirectLocationFor(webId.getUnicodeString)
		}
		def finalRedirectLocationFor(us: String): String = {	
			val url = new URL(us)
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
			val url = new URL(representationGraphUriString)
			val connection = url.openConnection()
			connection match {
				case hc: HttpURLConnection => hc.addRequestProperty("Accept",  acceptHeader);
			}
			connection.connect()
			val in = connection.getInputStream()
			val mediaType = connection.getContentType()
			val remoteTriples = parser.parse(in, mediaType, representationGraphUri)
			localCache.clear()
			localCache.addAll(remoteTriples)
		}
		
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
				val systemGraph = tcManager.getMGraph(SystemConfig.SYSTEM_GRAPH_URI)
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
	}
}