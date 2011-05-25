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
package org.apache.clerezza.platform.documentation;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.impl.SimpleGraph;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.ontologies.DOCUMENTATION;
import org.apache.clerezza.rdf.ontologies.OSGI;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.UnionMGraph;

/**
 * The DocumentationProvider gathers the documentations of bundles and provides
 * a graph containing all documentations.
 * It is an implementation of <code>WeightedTcProvider</code> with the default
 * weight 30. 
 * @author mir, hasan
 */
@Component
@Services({
	@Service(WeightedTcProvider.class),
	@Service(DocumentationProvider.class)})
@Property(name="weight", intValue=30)

public class DocumentationProvider implements WeightedTcProvider, BundleListener {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * File path to the file containing the documentation of the bundle
	 */
	private static final String DOCUMENTATION_FILE = "/META-INF/documentation.nt";

	@Reference
	private PlatformConfig config;
	
	@Reference
	private Parser parser;

	/**
	 * Contains the map between bundles and their documentation-graph
	 */
	private Map<Bundle, MGraph> bundle2DocGraphMap = new HashMap<Bundle, MGraph>();

	/**
	 * UnionGraph which contains all documenation-graphs of the registered
	 * bundles
	 */
	private Graph unitedDocumentations;

	/**
	 * The URI of the graph containing the documentations
	 */
	public static final UriRef DOCUMENTATION_GRAPH_URI =
			new UriRef("urn:x-localinstance:/documentation.graph");

	private int weight = 30;	

	@Override
	public int getWeight() {
		return weight;
	}

	@Override
	public Graph getGraph(UriRef name) throws NoSuchEntityException {
		if (name.equals(DOCUMENTATION_GRAPH_URI)) {
			return unitedDocumentations;
		}
		throw new NoSuchEntityException(name);
	}

	@Override
	public MGraph getMGraph(UriRef name) throws NoSuchEntityException {
		throw new NoSuchEntityException(name);
	}

	@Override
	public TripleCollection getTriples(UriRef name) throws NoSuchEntityException {
		return getGraph(name);
	}

	@Override
	public Set<UriRef> listGraphs() {
		return Collections.singleton(DOCUMENTATION_GRAPH_URI);
	}

	@Override
	public Set<UriRef> listMGraphs() {
		return new HashSet<UriRef>();
	}

	@Override
	public Set<UriRef> listTripleCollections() {
		return Collections.singleton(DOCUMENTATION_GRAPH_URI);
	}

	@Override
	public MGraph createMGraph(UriRef name)
			throws UnsupportedOperationException, EntityAlreadyExistsException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public Graph createGraph(UriRef name, TripleCollection triples)
			throws UnsupportedOperationException, EntityAlreadyExistsException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void deleteTripleCollection(UriRef name)
			throws UnsupportedOperationException, NoSuchEntityException,
			EntityUndeletableException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public Set<UriRef> getNames(Graph graph) {
		final HashSet<UriRef> result = new HashSet<UriRef>();
		if (unitedDocumentations.equals(graph)) {
			result.add(DOCUMENTATION_GRAPH_URI);
		}
		return result;
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		Bundle bundle = event.getBundle();
		switch (event.getType()) {
			case BundleEvent.STARTED:
				registerDocumentation(bundle);
				break;
			case BundleEvent.STOPPED:
				unregisterDocumentation(bundle);
				break;
		}
		createUnionGraph();
	}

	protected void activate(final ComponentContext componentContext) {
		componentContext.getBundleContext().addBundleListener(this);
		weight = (Integer) componentContext.getProperties().get("weight");
		registerExistingDocumentations(componentContext);
		createUnionGraph();
	}

	protected void deactivate(final ComponentContext componentContext) {
		componentContext.getBundleContext().removeBundleListener(this);
	}	

	private void registerExistingDocumentations(ComponentContext componentContext) {
		Bundle[] bundles = componentContext.getBundleContext().getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getState() == Bundle.ACTIVE) {
				registerDocumentation(bundle);
			}
		}
	}

	private void registerDocumentation(Bundle bundle) {
		URL entry = bundle.getEntry(DOCUMENTATION_FILE);
		if (entry == null) {
			return;
		}
		MGraph docMGraph = getDocumentationMGraph(entry, bundle.getSymbolicName());
		addAdditionalTriples(bundle, docMGraph);
		bundle2DocGraphMap.put(bundle, docMGraph);
		logger.info("Registered documentation of bundle: {}",
				bundle.getSymbolicName());
	}

	private MGraph getDocumentationMGraph(URL docUrl, String symbolicName) {
		try {
			Graph parsedGraph = parser.parse(docUrl.openStream(),
					SupportedFormat.N_TRIPLE);
			UriRef baseUri = config.getDefaultBaseUri();
			return new SimpleMGraph(new UriMutatorIterator(
					parsedGraph.iterator(), baseUri.getUnicodeString(), symbolicName));
		} catch (IOException ex) {
			logger.warn("Cannot parse documentation at URL: {}", docUrl);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Adds triples that point from the bundle resource to its documentations.
	 *
	 * @param bundle
	 * @param docMGraph
	 */
	private void addAdditionalTriples(Bundle bundle, MGraph docMGraph) {
		UriRef bundleUri = new UriRef(bundle.getLocation());
		Triple triple = new TripleImpl(bundleUri, RDF.type, OSGI.Bundle);
		docMGraph.add(triple);
		Iterator<Triple> titledContents = docMGraph.filter(null, RDF.type,
				DISCOBITS.TitledContent);
		Set<Triple> newTriples = new HashSet<Triple>();
		for (Iterator<Triple> it = titledContents; it.hasNext();) {
			NonLiteral titledContent = it.next().getSubject();
			if (docMGraph.filter(null, DISCOBITS.holds, titledContent).hasNext()) {
				continue;
			}
			triple = new TripleImpl(bundleUri, DOCUMENTATION.documentation,
					titledContent);
			newTriples.add(triple);
		}
		docMGraph.addAll(newTriples);
	}

	private void unregisterDocumentation(Bundle bundle) {
		bundle2DocGraphMap.remove(bundle);
		logger.info("Unregistered documentation of bundle: {}",
				bundle.getSymbolicName());
	}

	private void createUnionGraph() {
		MGraph[] docGraphs = bundle2DocGraphMap.values().
				toArray(new MGraph[bundle2DocGraphMap.size()]);
		unitedDocumentations = new SimpleGraph(new UnionMGraph(docGraphs), true);
	}
}
