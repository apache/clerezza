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
import org.apache.clerezza.rdf.core.MGraph
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.core.access.NoSuchEntityException
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.rdf.core.access.security.TcPermission
import org.apache.clerezza.utils.osgi.BundlePathNode
import org.osgi.framework.Bundle
import org.osgi.framework.BundleEvent
import org.osgi.framework.BundleListener
import org.osgi.service.component.ComponentContext
import org.slf4j.LoggerFactory

object BundleFsLoader {
	private val log = LoggerFactory.getLogger(classOf[BundleFsLoader])
}
class BundleFsLoader extends BundleListener with Logger {

	private val MGRAPH_NAME = new UriRef("http://zz.localhost/web-resources.graph")

	private var tcManager: TcManager = null
	private var cgProvider: ContentGraphProvider = null

	protected def activate(context: ComponentContext) {
		try {
			tcManager.getMGraph(MGRAPH_NAME);
		} catch {
			case e: NoSuchEntityException => {
				tcManager.createMGraph(MGRAPH_NAME);
				tcManager.getTcAccessController.setRequiredReadPermissions(
					MGRAPH_NAME, Collections.singleton(new TcPermission(Constants.CONTENT_GRAPH_URI_STRING, TcPermission.READ)))
			}
			case e => throw e
		}
		for (bundle <- context.getBundleContext().getBundles()) {
			addToGraph(bundle);
		}
		context.getBundleContext().addBundleListener(this);
		cgProvider.addTemporaryAdditionGraph(MGRAPH_NAME)
	}
	protected def deactivate(context: ComponentContext) {
		context.getBundleContext().removeBundleListener(this);
		cgProvider.removeTemporaryAdditionGraph(MGRAPH_NAME)
		tcManager.deleteTripleCollection(MGRAPH_NAME);
	}

	def bundleChanged(event: BundleEvent) {
		val bundle = event.getBundle();
		event.getType() match  {
			case BundleEvent.STARTED =>
				addToGraph(bundle);
			case BundleEvent.STOPPED =>
				removeFromGraph(bundle);
			case _ => BundleFsLoader.log.debug("only reacting on bundle start and stop")
		}
	}
	
	def addToGraph(bundle: Bundle) {
		val pathNode = new BundlePathNode(bundle, "CLEREZZA-INF/web-resources");
		val mGraph: MGraph = tcManager.getMGraph(MGRAPH_NAME);
		PathNode2MGraph.describeInGraph(pathNode, mGraph);
		BundleFsLoader.log.info("size of mgraph after adding resources of {}: {}", bundle, mGraph.size)
		//log.info("size of mgraph after adding resources of {}: {}", bundle, mGraph.size)
	}

	def removeFromGraph(bundle: Bundle) {

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
}
