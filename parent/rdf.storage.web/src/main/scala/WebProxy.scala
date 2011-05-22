/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.clerezza.rdf.storage.web


import org.osgi.service.component.ComponentContext
import java.io.IOException
import java.net.{HttpURLConnection, URL}
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat
import org.apache.clerezza.rdf.core.serializedform.Parser
import java.security.{PrivilegedExceptionAction, PrivilegedActionException, AccessController}

import org.slf4j.scala._
import org.apache.clerezza.rdf.core.access._
import org.apache.clerezza.rdf.core.impl.AbstractMGraph
import org.apache.clerezza.rdf.core._

/**
 * The Web Proxy Service enables applications to request remote (and local) graphs.
 * It keeps cached version of the remote graphs in store for faster delivery.
 *
 */
class WebProxy extends WeightedTcProvider with Logging {

	private var tcProvider: TcProviderMultiplexer = new TcProviderMultiplexer

	/**
	 * Register a provider
	 *
	 * @param provider
	 *            the provider to be registered
	 */
	protected def bindWeightedTcProvider(provider: WeightedTcProvider): Unit = {
		tcProvider.addWeightedTcProvider(provider)
	}

	/**
	 * Deregister a provider
	 *
	 * @param provider
	 *            the provider to be deregistered
	 */
	protected def unbindWeightedTcProvider(provider: WeightedTcProvider): Unit = {
		tcProvider.removeWeightedTcProvider(provider)
	}

	/**OSGI method, called on activation */
	protected def activate(context: ComponentContext) = {

	}


	private var parser: Parser = null

	protected def bindParser(p: Parser) = {
		parser = p
	}

	protected def unbindParser(p: Parser) = {
		parser = null
	}

	def getWeight: Int = {
		return 0
	}

	/**
	 * Any TripleCollection is available as Graph as well as immutable MGraph
	 *
	 * @param name
	 * @return
	 * @throws NoSuchEntityException
	 */
	def getMGraph(name: UriRef): MGraph = {
		val graph = getGraph(name)
		return new AbstractMGraph() {
			protected def performFilter(subject: NonLiteral, predicate: UriRef, `object` : Resource): java.util.Iterator[Triple] = {
				graph.filter(subject, predicate, `object`)
			}

			def size = graph.size
		}
	}

	def getGraph(name: UriRef): Graph = {
		try {
			getGraph(name, Cache.Fetch)
		} catch {
			case e: IOException => {
					logger.debug("could not get graph by dereferencing uri", e)
					throw new NoSuchEntityException(name)
			}

		}
	}

	def getTriples(name: UriRef): TripleCollection = {
		return getMGraph(name)
	}

	def createMGraph(name: UriRef): MGraph = {
		throw new UnsupportedOperationException
	}

	def createGraph(name: UriRef, triples: TripleCollection): Graph = {
		throw new UnsupportedOperationException
	}

	def deleteTripleCollection(name: UriRef): Unit = {
		throw new UnsupportedOperationException
	}

	def getNames(graph: Graph): java.util.Set[UriRef] = {
		var result: java.util.Set[UriRef] = new java.util.HashSet[UriRef]
		import collection.JavaConversions._
		for (name <- listGraphs) {
			if (getGraph(name).equals(graph)) {
				result.add(name)
			}
		}
		return result
	}

	def listTripleCollections: java.util.Set[UriRef] = {
		var result: java.util.Set[UriRef] = new java.util.HashSet[UriRef]
		result.addAll(listGraphs)
		result.addAll(listMGraphs)
		return result
	}

	def listGraphs: java.util.Set[UriRef] = {
		//or should we list graphs for which we have a cached version?
		return java.util.Collections.emptySet[UriRef]
	}

	def listMGraphs: java.util.Set[UriRef] = {
		return java.util.Collections.emptySet[UriRef]
	}

	/**
	 * The semantics of this resource
	 * @param update if a remote URI, update information on the resource first
	 */
	def getGraph(name: UriRef, updatePolicy: Cache.Value): Graph = {
		logger.debug("getting graph " + name)
		if (name.getUnicodeString.indexOf('#') != -1) {
			logger.debug("not dereferencing URI with hash sign. Please see CLEREZZA-533 for debate.")
			throw new NoSuchEntityException(name)
		}
		val cacheGraphName = new UriRef("urn:x-localinstance:/cache/" + name.getUnicodeString)
		//todo: follow redirects and keep track of them
		//todo: keep track of headers especially date and etag. test for etag similarity
		//todo: for https connection allow user to specify his webid and send his key: ie allow web server to be an agent
		//todo: add GRDDL functionality, so that other return types can be processed too
		//todo: enable ftp and other formats (though content negotiation won't work there)
		def updateGraph() {
			val url = new URL(name.getUnicodeString)
			val connection = url.openConnection()
			connection match {
				case hc: HttpURLConnection => hc.addRequestProperty("Accept", acceptHeader);
			}
			connection.connect()
			val in = connection.getInputStream()
			val mediaType = connection.getContentType()
			val remoteTriples = parser.parse(in, mediaType, name)
			tcProvider.synchronized {
				try {
					tcProvider.deleteTripleCollection(cacheGraphName)
				} catch {
					case e: NoSuchEntityException =>;
				}
				tcProvider.createGraph(cacheGraphName, remoteTriples)
			}
		}
		try {
			//the logic here is not quite right, as we don't look at time of previous fetch.
			updatePolicy match {
				case Cache.Fetch => try {
					tcProvider.getGraph(cacheGraphName)
				} catch {
					case e: NoSuchEntityException => updateGraph(); tcProvider.getGraph(cacheGraphName)
				}
				case Cache.ForceUpdate => updateGraph(); tcProvider.getGraph(cacheGraphName)
				case Cache.CacheOnly => tcProvider.getGraph(cacheGraphName)
			}
		} catch {
			case ex: PrivilegedActionException => {
				var cause: Throwable = ex.getCause
				if (cause.isInstanceOf[UnsupportedOperationException]) {
					throw cause.asInstanceOf[UnsupportedOperationException]
				}
				if (cause.isInstanceOf[EntityAlreadyExistsException]) {
					throw cause.asInstanceOf[EntityAlreadyExistsException]
				}
				if (cause.isInstanceOf[RuntimeException]) {
					throw cause.asInstanceOf[RuntimeException]
				}
				throw new RuntimeException(cause)
			}
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

object Cache extends Enumeration {
	/**fetch if not in cache, if version in cache is out of date, or return cache */
	val Fetch = Value
	/**fetch from source whatever is in cache */
	val ForceUpdate = Value
	/**only get cached version. If none exists return empty graph */
	val CacheOnly = Value
}
