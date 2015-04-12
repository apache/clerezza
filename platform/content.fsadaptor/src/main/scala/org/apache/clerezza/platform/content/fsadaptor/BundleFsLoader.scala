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
import org.apache.clerezza.commons.rdf.ImmutableGraph
import org.apache.clerezza.commons.rdf.Graph
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI
import org.apache.clerezza.commons.rdf.RDFTerm
import org.apache.clerezza.commons.rdf.Graph
import org.apache.clerezza.commons.rdf.Triple
import org.apache.clerezza.commons.rdf.IRI
import org.apache.clerezza.rdf.core.access.NoSuchEntityException
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.access.WeightedTcProvider
import org.apache.clerezza.rdf.core.access.security.TcPermission
import org.apache.clerezza.commons.rdf.impl.utils.AbstractGraph
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph
import org.apache.clerezza.utils.osgi.BundlePathNode
import org.osgi.framework.Bundle
import org.osgi.framework.BundleEvent
import org.osgi.framework.BundleListener
import org.osgi.service.component.ComponentContext
import org.osgi.service.startlevel.StartLevel
import org.slf4j.LoggerFactory
import org.wymiwyg.commons.util.dirbrowser.MultiPathNode
import org.wymiwyg.commons.util.dirbrowser.PathNode
import scala.util._

/**
 * This weighted TcProvider provides a graph named urn:x-localinstance/web-resources.graph which contains descriptions
 * of the files below the CLEREZZA-INF/web-resources directory in every bundles. The
 * name of these descriptions (i.e. the rdf resources) use the urn:x-localinstance uri scheme to indicate that
 * they are local to the instance and they will thus be returned as description
 * for all uris with a local authority and the specified path-section.
 */
class BundleFsLoader extends BundleListener with Logger with WeightedTcProvider {

  private val RESOURCE_MGRAPH_URI = new IRI(Constants.URN_LOCAL_INSTANCE+"/web-resources.graph")
  private val cacheGraphPrefix = Constants.URN_LOCAL_INSTANCE+"/web-resources-cache.graph"
  private var currentCacheUri: IRI = null

  private var tcManager: TcManager = null
  private var cgProvider: ContentGraphProvider = null
  private var startLevel: StartLevel = null
  private var pathNodes: List[PathNode] = Nil 
  private var bundleList = List[Bundle]()
  private var currentCacheGraph: Graph = null
  
  private var frequentUpdateDirectory: Option[PathNode] = None

  private val virtualGraph: Graph = new AbstractGraph() {
    
    private def baseGraph: Graph = frequentUpdateDirectory match {
        case Some(p) => new DirectoryOverlay(p, currentCacheGraph)
        case None => currentCacheGraph
    }
    
    override def performFilter(s: BlankNodeOrIRI, p: IRI,
                      o: RDFTerm): java.util.Iterator[Triple] = {
      val baseIter = baseGraph.filter(s,p,o)
      new java.util.Iterator[Triple]() {
        override def next = {
          baseIter.next
        }
        override def hasNext = baseIter.hasNext
        override def remove = throw new UnsupportedOperationException
      }
    }

    override def performSize = baseGraph.size
    
    override def toString = "BundleFsLoader virtual graph"
    
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
    for(mGraphUri <- tcManager.listGraphs) {
      if(mGraphUri.getUnicodeString.startsWith(cacheGraphPrefix)) {
        tcManager.deleteGraph(mGraphUri);
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
      updateCache
      tcManager.getTcAccessController.setRequiredReadPermissions(
          RESOURCE_MGRAPH_URI, Collections.singleton(new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READ)))
      cgProvider.addTemporaryAdditionGraph(RESOURCE_MGRAPH_URI)
      updateThread = new UpdateThread()
      context.getBundleContext().addBundleListener(this);
    }
  }
  protected def deactivate(context: ComponentContext) {
    synchronized {
      context.getBundleContext().removeBundleListener(this);
      updateThread.interrupt()
      cgProvider.removeTemporaryAdditionGraph(RESOURCE_MGRAPH_URI)
      tcManager.deleteGraph(currentCacheUri);
      updateThread == null;
    }
  }

  private def updateCache() = {
    def getVirtualGraph(bundles: Seq[Bundle]): Graph = {
      if (bundles.isEmpty) {
        new SimpleGraph()
      } else {
        val pathNode = new BundlePathNode(bundles.head, "CLEREZZA-INF/web-resources");
        if (pathNode.isDirectory) {
          BundleFsLoader.log.debug("Creating directory overlay for "+bundles.head)
          new DirectoryOverlay(pathNode, getVirtualGraph(bundles.tail))
        } else {
          getVirtualGraph(bundles.tail)
        }
      }
    }
    synchronized {
      val sortedList = Sorting.stableSort(bundleList, (b:Bundle) => -startLevel.getBundleStartLevel(b))
      val newCacheUri = new IRI(cacheGraphPrefix+System.currentTimeMillis)
      val newChacheGraph = tcManager.createGraph(newCacheUri);
      tcManager.getTcAccessController.setRequiredReadPermissions(
          newCacheUri, Collections.singleton(new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READ)))
      newChacheGraph.addAll(getVirtualGraph(sortedList))
      currentCacheGraph = newChacheGraph
      val oldCacheUri = currentCacheUri
      currentCacheUri = newCacheUri
      if (oldCacheUri != null) tcManager.deleteGraph(oldCacheUri);
      BundleFsLoader.log.debug("updated web-resource cache")
    }
  }

  

  override def getWeight() = 30

  override def getGraph(name: IRI) = {
    if (name.equals(RESOURCE_MGRAPH_URI)) {
      virtualGraph
    } else {
      throw new NoSuchEntityException(name);
    }
  }
  
  override def getMGraph(name: IRI) = getGraph(name);

  override def getImmutableGraph(name: IRI) = throw new NoSuchEntityException(name);
  
  override def listGraphs(): java.util.Set[IRI] = {
    java.util.Collections.singleton(RESOURCE_MGRAPH_URI);
  }
  
  override def listMGraphs() = listGraphs();
  
  override def listImmutableGraphs(): java.util.Set[IRI] = java.util.Collections.emptySet();


  override def createGraph(name: IRI) =  {
    throw new UnsupportedOperationException("Not supported.");
  }

  override def createImmutableGraph(name: IRI, triples: Graph): ImmutableGraph = {
    throw new UnsupportedOperationException("Not supported.");
  }

  override def deleteGraph(name: IRI) {
    throw new UnsupportedOperationException("Not supported.");
  }

  override def getNames(graph: ImmutableGraph) = {
    val result = new java.util.HashSet[IRI]();
    result;
  }


  def bundleChanged(event: BundleEvent) {
    val updateThread = this.updateThread
    if (updateThread == null) {
      BundleFsLoader.log.error("UpdateThread is null, yet we get bundle Events")
    }
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
  
  def bindPathNode(pathNode: PathNode) {
    this.pathNodes ::= pathNode;
    frequentUpdateDirectory = Some(new MultiPathNode(pathNodes: _*))
  }
  
  def unbindPathNode(pathNode: PathNode) {
    this.pathNodes = this.pathNodes.filter(_ != pathNode);
    frequentUpdateDirectory = pathNodes match {
      case Nil => None
      case _ => Some(new MultiPathNode(pathNodes: _*))
    }
  }
  
}
object BundleFsLoader {
  private val log = LoggerFactory.getLogger(classOf[BundleFsLoader])
}