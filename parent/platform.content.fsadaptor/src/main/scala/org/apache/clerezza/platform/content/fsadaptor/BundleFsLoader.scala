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

package org.apache.clerezza.platform.content.fsadaptor

import java.util.Collections
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.rdf.core.Graph
import org.apache.clerezza.rdf.core.MGraph
import org.apache.clerezza.rdf.core.NonLiteral
import org.apache.clerezza.rdf.core.Resource
import org.apache.clerezza.rdf.core.TripleCollection
import org.apache.clerezza.rdf.core.Triple
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.core.access.NoSuchEntityException
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.access.WeightedTcProvider
import org.apache.clerezza.rdf.core.access.security.TcPermission
import org.apache.clerezza.rdf.core.impl.AbstractMGraph
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.utils.osgi.BundlePathNode
import org.osgi.framework.Bundle
import org.osgi.framework.BundleEvent
import org.osgi.framework.BundleListener
import org.osgi.service.component.ComponentContext
import org.osgi.service.startlevel.StartLevel
import org.slf4j.LoggerFactory
import scala.util._

/**
 * This weighted TcProvider provides a graph named urn:x-localinstance/web-resources.graph which contains descriptions
 * of the files below the CLEREZZA-INF/web-resources directory in every bundles. The
 * name of these descriptions (i.e. the rdf resources) use the urn:x-localinstance uri scheme to indicate that
 * they are local to the instance and they will thus be returned as description
 * for all uris with a local authority and the specified path-section.
 */
class BundleFsLoader extends BundleListener with Logger with WeightedTcProvider {

	private val RESOURCE_MGRAPH_URI = new UriRef(Constants.URN_LOCAL_INSTANCE+"/web-resources.graph")
	private val cacheGraphPrefix = Constants.URN_LOCAL_INSTANCE+"/web-resources-cache.graph"
	private var currentCacheUri: UriRef = null

	private var tcManager: TcManager = null
	private var cgProvider: ContentGraphProvider = null
	private var startLevel: StartLevel = null
	private var bundleList = List[Bundle]()
	private var currentCacheMGraph: MGraph = null

	private val virtualMGraph = new AbstractMGraph() {
		override def performFilter(s: NonLiteral, p: UriRef,
				o: Resource): java.util.Iterator[Triple] = {
			currentCacheMGraph.filter(s,p,o)
		}

		override def size = currentCacheMGraph.size
	}

	class UpdateThread extends Thread {

		private var updateRequested = false;

		start()

		override def run() {
			try {
				while (!isInterrupted) {
					synchronized {
						while (!updateRequested) wait();
					}
					updateRequested = false
					updateCache()
				}
			} catch {
				case e: InterruptedException => BundleFsLoader.log.debug("Update thread interrupted");
			}
		}

		def update() = {
			synchronized {
				updateRequested = true
				notify();
			}
		}
	}
	
	private var updateThread: UpdateThread = null


	private def deleteCacheGraphs() {
		import collection.JavaConversions._
		for(mGraphUri <- tcManager.listMGraphs) {
			if(mGraphUri.getUnicodeString.startsWith(cacheGraphPrefix)) {
				tcManager.deleteTripleCollection(mGraphUri);
			}
		}
	}

	protected def activate(context: ComponentContext) {
		synchronized {
			deleteCacheGraphs()
			for (bundle <- context.getBundleContext().getBundles();
					if bundle.getState == Bundle.ACTIVE) {
				bundleList ::= bundle
			}
			context.getBundleContext().addBundleListener(this);
			updateCache
			tcManager.getTcAccessController.setRequiredReadPermissions(
					RESOURCE_MGRAPH_URI, Collections.singleton(new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READ)))
			cgProvider.addTemporaryAdditionGraph(RESOURCE_MGRAPH_URI)
			updateThread = new UpdateThread()
		}
	}
	protected def deactivate(context: ComponentContext) {
		synchronized {
			context.getBundleContext().removeBundleListener(this);
			updateThread.interrupt()
			cgProvider.removeTemporaryAdditionGraph(RESOURCE_MGRAPH_URI)
			tcManager.deleteTripleCollection(currentCacheUri);
		}
	}

	private def updateCache() = {
		def getVirtualTripleCollection(bundles: Seq[Bundle]): TripleCollection = {
			if (bundles.isEmpty) {
				new SimpleMGraph()
			} else {
				val pathNode = new BundlePathNode(bundles.head, "CLEREZZA-INF/web-resources");
				if (pathNode.isDirectory) {
					BundleFsLoader.log.debug("Creating directory overlay for "+bundles.head)
					new DirectoryOverlay(pathNode, getVirtualTripleCollection(bundles.tail))
				} else {
					getVirtualTripleCollection(bundles.tail)
				}
			}
		}
		synchronized {
			val sortedList = Sorting.stableSort(bundleList, (b:Bundle) => -startLevel.getBundleStartLevel(b))
			val newCacheUri = new UriRef(cacheGraphPrefix+System.currentTimeMillis)
			val newChacheMGraph = tcManager.createMGraph(newCacheUri);
			tcManager.getTcAccessController.setRequiredReadPermissions(
					newCacheUri, Collections.singleton(new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READ)))
			newChacheMGraph.addAll(getVirtualTripleCollection(sortedList))
			currentCacheMGraph = newChacheMGraph
			val oldCacheUri = currentCacheUri
			currentCacheUri = newCacheUri
			if (oldCacheUri != null) tcManager.deleteTripleCollection(oldCacheUri);
			BundleFsLoader.log.debug("updated web-resource cache")
		}
	}

	

	override def getWeight() = 30

	override def getMGraph(name: UriRef) = {
		if (name.equals(RESOURCE_MGRAPH_URI)) {
			virtualMGraph
		} else {
			throw new NoSuchEntityException(name);
		}
	}

	override def getTriples(name: UriRef) = {
		getMGraph(name);
	}

	override def getGraph(name: UriRef) = {
		throw new NoSuchEntityException(name);
	}


	override def listMGraphs(): java.util.Set[UriRef] = {
		java.util.Collections.singleton(RESOURCE_MGRAPH_URI);
	}

	override def listGraphs() = {
		new java.util.HashSet[UriRef]();
	}

	override def listTripleCollections() = {
		Collections.singleton(RESOURCE_MGRAPH_URI);
	}

	override def createMGraph(name: UriRef) =  {
		throw new UnsupportedOperationException("Not supported.");
	}

	override def createGraph(name: UriRef, triples: TripleCollection): Graph = {
		throw new UnsupportedOperationException("Not supported.");
	}

	override def deleteTripleCollection(name: UriRef) {
		throw new UnsupportedOperationException("Not supported.");
	}

	override def getNames(graph: Graph) = {
		val result = new java.util.HashSet[UriRef]();
		result;
	}


	def bundleChanged(event: BundleEvent) {
		val bundle = event.getBundle();
		event.getType() match  {
			case BundleEvent.STARTED => {
				bundleList ::= bundle
				updateThread.update()
			}
			case BundleEvent.STOPPED => {
				bundleList = bundleList.filterNot(b => b == bundle)
				updateThread.update()
			}
			case _ => BundleFsLoader.log.debug("only reacting on bundle start and stop")
		}
	}


	def bindTcManager(tcManager: TcManager) {
		this.tcManager = tcManager;
	}

	def unbindTcManager(tcManager: TcManager) {
		this.tcManager = null;
	}

	def bindContentGraphProvider(p: ContentGraphProvider) {
		cgProvider = p
	}

	def unbindContentGraphProvider(p: ContentGraphProvider) {
		cgProvider = null
	}

	def bindStartLevel(startLevel: StartLevel) {
		this.startLevel = startLevel;
	}

	def unbindStartLevel(startLevel: StartLevel) {
		this.startLevel = null;
	}
	
}
object BundleFsLoader {
	private val log = LoggerFactory.getLogger(classOf[BundleFsLoader])
}