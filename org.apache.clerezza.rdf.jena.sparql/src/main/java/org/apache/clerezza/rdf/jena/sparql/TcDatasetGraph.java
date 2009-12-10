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
package org.apache.clerezza.rdf.jena.sparql;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import java.util.Iterator;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.jena.facade.JenaGraph;

/**
 *
 * @author rbn
 */
class TcDatasetGraph implements DatasetGraph {

	private TcManager tcManager;
	private TripleCollection defaultGraph;

	TcDatasetGraph(TcManager tcManager, TripleCollection defaultGraph) {
		this.tcManager = tcManager;
		this.defaultGraph = defaultGraph;
	
	}

	@Override
	public Graph getDefaultGraph() {
		final JenaGraph jenaGraph = new JenaGraph(defaultGraph);
		return jenaGraph;
	}

	@Override
	public Graph getGraph(Node node) {
		final JenaGraph jenaGraph = new JenaGraph(
				tcManager.getTriples(new UriRef(node.getURI())));
		return jenaGraph;
	}

	@Override
	public boolean containsGraph(Node node) {
		try {
			tcManager.getTriples(new UriRef(node.getURI()));
			return true;
		} catch (NoSuchEntityException e) {
			return false;
		}
	}

	@Override
	public Iterator listGraphNodes() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Lock getLock() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void close() {
	}

}
