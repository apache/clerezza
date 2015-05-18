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
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleImmutableGraph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.ontologies.DOCUMENTATION;
import org.apache.clerezza.rdf.ontologies.OSGI;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.UnionGraph;

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
    private Map<Bundle, Graph> bundle2DocGraphMap = new HashMap<Bundle, Graph>();

    /**
     * UnionGraph which contains all documenation-graphs of the registered
     * bundles
     */
    private ImmutableGraph unitedDocumentations;

    /**
     * The URI of the graph containing the documentations
     */
    public static final IRI DOCUMENTATION_GRAPH_URI =
            new IRI("urn:x-localinstance:/documentation.graph");

    private int weight = 30;    

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public ImmutableGraph getImmutableGraph(IRI name) throws NoSuchEntityException {
        if (name.equals(DOCUMENTATION_GRAPH_URI)) {
            return unitedDocumentations;
        }
        throw new NoSuchEntityException(name);
    }

    @Override
    public Graph getMGraph(IRI name) throws NoSuchEntityException {
        throw new NoSuchEntityException(name);
    }

    @Override
    public Graph getGraph(IRI name) throws NoSuchEntityException {
        return getImmutableGraph(name);
    }

    @Override
    public Set<IRI> listImmutableGraphs() {
        return Collections.singleton(DOCUMENTATION_GRAPH_URI);
    }

    @Override
    public Set<IRI> listMGraphs() {
        return new HashSet<IRI>();
    }

    @Override
    public Set<IRI> listGraphs() {
        return Collections.singleton(DOCUMENTATION_GRAPH_URI);
    }

    @Override
    public Graph createGraph(IRI name)
            throws UnsupportedOperationException, EntityAlreadyExistsException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ImmutableGraph createImmutableGraph(IRI name, Graph triples)
            throws UnsupportedOperationException, EntityAlreadyExistsException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void deleteGraph(IRI name)
            throws UnsupportedOperationException, NoSuchEntityException,
            EntityUndeletableException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<IRI> getNames(ImmutableGraph graph) {
        final HashSet<IRI> result = new HashSet<IRI>();
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
        Graph docGraph = getDocumentationGraph(entry, bundle.getSymbolicName());
        addAdditionalTriples(bundle, docGraph);
        bundle2DocGraphMap.put(bundle, docGraph);
        logger.info("Registered documentation of bundle: {}",
                bundle.getSymbolicName());
    }

    private Graph getDocumentationGraph(URL docUrl, String symbolicName) {
        try {
            ImmutableGraph parsedGraph = parser.parse(docUrl.openStream(),
                    SupportedFormat.N_TRIPLE);
            IRI baseUri = config.getDefaultBaseUri();
            return new SimpleGraph(new UriMutatorIterator(
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
     * @param docGraph
     */
    private void addAdditionalTriples(Bundle bundle, Graph docGraph) {
        IRI bundleUri = new IRI(bundle.getLocation());
        Triple triple = new TripleImpl(bundleUri, RDF.type, OSGI.Bundle);
        docGraph.add(triple);
        Iterator<Triple> titledContents = docGraph.filter(null, RDF.type,
                DISCOBITS.TitledContent);
        Set<Triple> newTriples = new HashSet<Triple>();
        for (Iterator<Triple> it = titledContents; it.hasNext();) {
            BlankNodeOrIRI titledContent = it.next().getSubject();
            if (docGraph.filter(null, DISCOBITS.holds, titledContent).hasNext()) {
                continue;
            }
            triple = new TripleImpl(bundleUri, DOCUMENTATION.documentation,
                    titledContent);
            newTriples.add(triple);
        }
        docGraph.addAll(newTriples);
    }

    private void unregisterDocumentation(Bundle bundle) {
        bundle2DocGraphMap.remove(bundle);
        logger.info("Unregistered documentation of bundle: {}",
                bundle.getSymbolicName());
    }

    private void createUnionGraph() {
        Graph[] docGraphs = bundle2DocGraphMap.values().
                toArray(new Graph[bundle2DocGraphMap.size()]);
        if (docGraphs.length > 0) {
            unitedDocumentations = new SimpleImmutableGraph(new UnionGraph(docGraphs), true);
        } else {
            unitedDocumentations = new SimpleImmutableGraph(new SimpleGraph(), true);
        }
    }
}
