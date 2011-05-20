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
import org.apache.clerezza.rdf.core.access.LockableMGraph
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
	 * @param uri the Web-Id
	 * @return a WebIdInfo allowing to access the graphs of the user
	 */
	def getWebIdInfo(uri: UriRef): WebIdInfo = {
		return new WebIdInfo {

			private val uriString = uri.getUnicodeString


			/**
			 * We don't know if there are multiple rediects from the person to the
			 * Document with the triples which one is the Document
			 */
			private lazy val profileDocumentUriString = {
				val hashPos = uriString.indexOf('#')
				if (hashPos != -1) {
					uriString.substring(0, hashPos)
				} else {
					redirectLocationString
				}
			}

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


			private lazy val profileDocumentUri = {
				new UriRef(profileDocumentUriString)
			}

			/**
			 * As the webid identifies a person an not a document, a webid without hash sign
			 * should redirect to the profile document
			 */
			private lazy val redirectLocationString = {
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

			/**
			 * A webbid identifying a person should redirect to the uri identifying the document,
			 * it is possible that it redirects directly to the "correct" representation, this is why
			 * we set this to prefer rdf over other formats
			 */
			private lazy val acceptHeader = "application/rdf+xml,*/*;q.1"


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
				tcManager.getMGraph(profileDocumentUri)
			}

			def localPublicUserData: LockableMGraph = {
				if (isLocal) {
					new UnionMGraph(tcManager.getMGraph(profileDocumentUri), systemTriples)
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
				proxy.getGraph(profileDocumentUri)
			}
		}

	}
}