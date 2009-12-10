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
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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
public class TcDataset implements Dataset {

	private TcManager tcManager;
	private TripleCollection defaultGraph;

	TcDataset(TcManager tcManager, TripleCollection defaultGraph) {
		this.tcManager = tcManager;
		this.defaultGraph = defaultGraph;
	}

	@Override
	public Model getDefaultModel() {
		final JenaGraph jenaGraph = new JenaGraph(defaultGraph);
		final Model model = ModelFactory.createModelForGraph(jenaGraph);
		return model;
	}

	@Override
	public Model getNamedModel(String name) {
		final JenaGraph jenaGraph = new JenaGraph(
				tcManager.getTriples(new UriRef(name)));
		final Model model = ModelFactory.createModelForGraph(jenaGraph);
		return model;
	}

	@Override
	public boolean containsNamedModel(String name) {
		try {
			tcManager.getTriples(new UriRef(name));
			return true;
		} catch (NoSuchEntityException e) {
			return false;
		}
	}

	@Override
	public Iterator<String> listNames() {
		final Iterator<UriRef> tcs = tcManager.listTripleCollections().iterator();
		return new Iterator<String>() {

			@Override
			public boolean hasNext() {
				return tcs.hasNext();
			}

			@Override
			public String next() {
				UriRef next = tcs.next();
				return next != null ? next.getUnicodeString() : null;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};

	}

	@Override
	public Lock getLock() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public DatasetGraph asDatasetGraph() {
		return new TcDatasetGraph(tcManager, defaultGraph);
	}

	@Override
	public void close() {
	}
}
