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
package org.apache.clerezza.platform.graphprovider.content;

import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A utility class to rename used in snapshot versions to the naming in the first released version
 *
 * @author reto
 */
public class GraphNameTransitioner {

	private final static Logger logger = LoggerFactory.getLogger(GraphNameTransitioner.class);

	/**
	 * a hack to prevent double execution on the same start up process
	 */
	private static boolean alreadyExecuted = false;

	synchronized static public void renameGraphsWithOldNames(TcManager tcManager) {
		if (alreadyExecuted) {
			return;
		} else {
			alreadyExecuted = true;
		}

		/*try {
			//just to see that nothing bad happens
			Thread.sleep(20*1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}*/

		final String OLD_SYSTEM_GRAPH_URI_STRING =
				"http://tpf.localhost/system.graph";
		final UriRef OLD_SYSTEM_GRAPH_URI =
				new UriRef(OLD_SYSTEM_GRAPH_URI_STRING);

		final String OLD_CONFIG_GRAPH_URI_STRING =
				"http://tpf.localhost/config.graph";
		final UriRef OLD_CONFIG_GRAPH_URI =
				new UriRef(OLD_CONFIG_GRAPH_URI_STRING);

		final String OLD_CONTENT_GRAPH_URI_STRING =
				"http://tpf.localhost/content.graph";
		final UriRef OLD_CONTENT_GRAPH_URI =
				new UriRef(OLD_CONTENT_GRAPH_URI_STRING);

		renameGraphs(tcManager, OLD_SYSTEM_GRAPH_URI, Constants.SYSTEM_GRAPH_URI);
		renameGraphs(tcManager, OLD_CONFIG_GRAPH_URI, Constants.CONFIG_GRAPH_URI);
		renameGraphs(tcManager, OLD_CONTENT_GRAPH_URI, Constants.CONTENT_GRAPH_URI);
		renameGraphs(tcManager, new UriRef("http://zz.localhost/graph-access.graph"),
				new UriRef(Constants.URN_LOCAL_INSTANCE+"/graph-access.graph"));

	}

	private static void renameGraphs(TcManager tcManager, UriRef oldGraphUri, UriRef graphUri) {
		try {
			MGraph oldMGraph = tcManager.getMGraph(oldGraphUri);
			MGraph newMGraph = tcManager.createMGraph(graphUri);
			logger.info("renaming "+oldGraphUri+" to "+graphUri);
			newMGraph.addAll(oldMGraph);
			tcManager.deleteTripleCollection(oldGraphUri);
		} catch (NoSuchEntityException ex) {
		} catch (EntityAlreadyExistsException ex) {
			logger.warn("could not rename " + oldGraphUri + " to " + 
					graphUri + " because target graph already exists", ex);
		}
	}
}
