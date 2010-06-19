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

package org.apache.clerezza.foafssl

import java.net.HttpURLConnection
import java.net.URL
import org.apache.clerezza.rdf.core.TripleCollection
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.core.access.NoSuchEntityException
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.serializedform.Parser
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.osgi.service.component.ComponentContext


/**
 * delivers the description of a resource. This description is based on local 
 * data as well as (cached) web-data
 */
class WebDescriptionProvider {
	
	private var tcManager: TcManager = null;

	protected def bindTcManager(tcManager: TcManager) = {
		this.tcManager = tcManager
	}
	
	protected def unbindTcManager(tcManager: TcManager) = {
		this.tcManager = null
	}
	
	private lazy val parser = Parser.getInstance
	
	private val systemGraphUri = new UriRef("http://tpf.localhost/system.graph")
	
	private val authoritativeLocalGraphs = Array(
		systemGraphUri,
		new UriRef("http://tpf.localhost/config.graph"))
	
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
	
	lazy val acceptHeader = {
		import scala.collection.JavaConversions._
		(for (f <- parser.getSupportedFormats) yield {
				val qualityOfFormat = {
					f match {
						//the default format
						case SupportedFormat.RDF_XML => "1.0";
						//n3 is a bit less well defined and/or many parsers supports only subsets
						case SupportedFormat.N3 => "0.5";
						case _ => "0.8";
					}
				}
				f+"; q="+qualityOfFormat+","
		}).mkString +" *; q=.1"
	}
	
	/**
	 * @return a GraphNode describing uri
	 */
	def getWebDescription(uri: UriRef, update: Boolean): GraphNode = {
		lazy val representationGraphUriString = {
			val uriString = uri.getUnicodeString
			val hashPos = uriString.indexOf('#')
			if (hashPos != -1) {
				uriString.substring(0, hashPos)
			} else {
				finalRedirectLocation
			}
		}
		
		lazy val representationGraphUri = {
			new UriRef(representationGraphUriString)
		}
		lazy val finalRedirectLocation = {
			finalRedirectLocationFor(uri.getUnicodeString)
		}
		def finalRedirectLocationFor(us: String): String = {	
			val url = new URL(us)
			val connection = url.openConnection()
			connection match {
				case hc : HttpURLConnection => {
						hc.setRequestMethod("HEAD");
						hc.setInstanceFollowRedirects(false)
						hc.addRequestProperty("Accept:",  acceptHeader)
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
		
		lazy val localCacheUri = {
			new UriRef(representationGraphUriString+".cache")
		}

		def updateLocalCache() = {
			val url = new URL(representationGraphUriString)
			val connection = url.openConnection()
			
			
			connection match {
				case hc: HttpURLConnection => hc.addRequestProperty("Accept:",  acceptHeader);
			}
			
			val mediaType = connection.getContentType()
			val in = url.openStream
			val remoteTriples = parser.parse(in, mediaType, representationGraphUri)
			val localCache = try {
				val g = tcManager.getMGraph(localCacheUri)
				g.clear()
				g
			} catch {
				case e: NoSuchEntityException => tcManager.createMGraph(localCacheUri)
			}
			localCache.addAll(remoteTriples)
		}
		if (update) {
					updateLocalCache()
		}
		
		val cacheGraphOption: Option[TripleCollection] = try {
			Some(tcManager.getTriples(localCacheUri))
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
