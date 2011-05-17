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
import org.apache.clerezza.rdf.core.TripleCollection
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.core.access.NoSuchEntityException
import org.apache.clerezza.rdf.core.access.SecuredMGraph
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.access.security.TcPermission
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.ontologies.PLATFORM
import org.apache.clerezza.rdf.storage.web.WebProxy
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.apache.clerezza.rdf.core.serializedform.{SupportedFormat, Parser}

/**
 * For agents with a Web-Id various graphs are available, these graphs are
 * grouped by <code>WebIDInfo</code> which this service provides.
 */
class WebIdGraphsService {



	private var proxy: WebProxy  = null

	protected def bindProxy(proxy: WebProxy) = {
		this.proxy = proxy
	}

	protected def unbindProxy(proxy: WebProxy) = {
		this.proxy = null
	}

	/**
	 * We use the parser just to get the supported formats so we get the right redirect location
	 */
	private var parser: Parser = null;

	protected def bindParser(parser: Parser) = {
		this.parser = parser
	}

	protected def unbindParser(parser: Parser) = {
		this.parser = null
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

	/**
	 *
	 * @param uri for which info should be fetched
	 * @param update, a Cache.Value for how much to force the info for the resource. By default get what is in the cache
	 *        if this is still valid
	 * @return a resource info, more or less updated
	 */
	def getWebIdInfo(uri: UriRef): WebIdInfo = {
		return new WebIdInfo {

			private val uriString = uri.getUnicodeString


			/**
			 * We don't know if there are multiple rediects from the person to the
			 * Document with the triples which one is the Document
			 */
			private lazy val representationGraphUriString = {
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
			//FIXME multiple remote users could have same
			/*private lazy val localGraphUri = {
				new UriRef(localGraphUriString)
			}*/
			/**
			 * the graph for putting local information in addition to the remote graph
			 */
			private lazy val localGraphUri = {
				if (isLocal) uri
				else {
					new UriRef("urn:x-localinstance:/user/"+hashTruncatedUriString)
				}
			}

			private lazy val hashTruncatedUriString = {
				val hashPos = uriString.indexOf('#')
				if (hashPos != -1) {
					uriString.substring(0, hashPos)
				} else {
					uriString
				}
			}


			private lazy val representationGraphUri = {
				new UriRef(representationGraphUriString)
			}

			//TODO maybe its better to just follow one redirect and assume this
			//to be the profile rather than get the uri of the actual representation
			private lazy val finalRedirectLocation = {
				finalRedirectLocationFor(uriString)
			}
			private def finalRedirectLocationFor(us: String): String = {
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

			private lazy val acceptHeader = {
				import scala.collection.JavaConversions._
				(for (f <- parser.getSupportedFormats) yield {
						val qualityOfFormat = {
							f match {
								//the default, well established format
								case SupportedFormat.RDF_XML => "1.0";
									//n3 is a bit less well defined and/or many parsers supports only subsets
								case SupportedFormat.N3 => "0.6";
									//we prefer most dedicated formats to (X)HTML, not because those are "better",
									//but just because it is quite likely that the pure RDF format will be
									//ligher (contain less presentation markup), and it is also possible that HTML does not
									//contain any RDFa, but just points to another format.
								case SupportedFormat.XHTML => "0.5";
									//we prefer XHTML over html, because parsing (shoule) be easier
								case SupportedFormat.HTML => "0.4";
									//all other formats known currently are structured formats
								case _ => "0.8"
							}
						}
						f+"; q="+qualityOfFormat+","
					}).mkString +" *; q=.1"  //with grddl should add */*
			}



			private def systemTriples = {
				AccessController.doPrivileged(new PrivilegedAction[MGraph]() {
					def run() = {
						val systemGraph = tcManager.getMGraph(Constants.SYSTEM_GRAPH_URI)
						val triples = systemGraph.filter(uri, PLATFORM.userName, null)
						val result = new SimpleMGraph
						while (triples.hasNext) {
							result.add(triples.next())
						}
						result
					}
				})
			}

			private lazy val localGraph = try {
				val g = tcManager.getMGraph(localGraphUri)
				g
			} catch {
				case e: NoSuchEntityException => {
						import scala.collection.JavaConversions._
						tcManager.getTcAccessController.
						setRequiredReadPermissionStrings(localGraphUri,
														 List(new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READ).toString))
						tcManager.createMGraph(localGraphUri)
					}
			}
			//implementing exposed methods (from WebIdInfo trait)
			def publicProfile: TripleCollection = {
				tcManager.getMGraph(representationGraphUri)
			}

			def localPublicUserData: MGraph = {
				if (isLocal) {
					new UnionMGraph(tcManager.getMGraph(representationGraphUri), systemTriples)
				} else {
					new UnionMGraph(localGraph, systemTriples, publicProfile)
				}
			}

			lazy val isLocal: Boolean = {
				import scala.collection.JavaConversions._
				platformConfig.getBaseUris.exists(baseUri => uriString.startsWith(baseUri.getUnicodeString))
			}

			val webId = uri

			def forceCacheUpdate() = {
				proxy.getGraph(representationGraphUri)
			}
		}

	}
}