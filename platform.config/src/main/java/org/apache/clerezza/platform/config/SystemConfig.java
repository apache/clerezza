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
package org.apache.clerezza.platform.config;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

/**
 * When the
 * <code>SystemConfig</code> component is activated it checks if the system
 * graph exists, in case it does not exist then it creates the system graph and
 * writes the default platform configuration into it.
 *
 * @author mir
 */
@Component
@Service(WeightedTcProvider.class)
public class SystemConfig implements WeightedTcProvider {

    public static final String CONFIG_FILE = "default-system-graph.rdf";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     *
     * @deprecated use org.apache.clerezza.platform.Contants instead
     */
    @Deprecated
    public static final UriRef SYSTEM_GRAPH_URI = Constants.SYSTEM_GRAPH_URI;
    /**
     * A filter that can be used to get the system graph as OSGi service, that
     * is provided by
     * <code>org.apache.clerezza.rdf.core.access.TcManager</code>.
     */
    public static final String SYSTEM_GRAPH_FILTER =
            "(name=" + Constants.SYSTEM_GRAPH_URI_STRING + ")";
    public static final String PARSER_FILTER =
            "(supportedFormat=" + SupportedFormat.RDF_XML + ")";
    @Reference(target = PARSER_FILTER)
    private ParsingProvider parser;
    private MGraph loadedFile;

    @Activate
    protected void activate(ComponentContext componentContext) {
        //yould be good to use IndexedMGraph to be faster
        loadedFile = new SimpleMGraph();
        readConfigGraphFile(loadedFile);
        logger.info("Add initial configuration to system graph");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        loadedFile = null;
    }

    private void readConfigGraphFile(MGraph mGraph) {
        URL config = getClass().getResource(CONFIG_FILE);
        if (config == null) {
            throw new RuntimeException("no config file found");
        }
        try {
            parser.parse(mGraph, config.openStream(),
                    SupportedFormat.RDF_XML, null);
        } catch (IOException ex) {
            logger.warn("Cannot parse coniguration at URL: {}", config);
            throw new RuntimeException(ex);
        }
    }

    /*
     * Reason to be high: don't allow overwriting of system graph (by accident or as an attack)
     * Reason to be low: avoid that TcManager always first tries to create TripleCollections using this provider
     */
    @Override
    public int getWeight() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Graph getGraph(UriRef name) throws NoSuchEntityException {
        throw new NoSuchEntityException(name);
    }

    @Override
    public MGraph getMGraph(UriRef name) throws NoSuchEntityException {
        if (name.equals(Constants.SYSTEM_GRAPH_URI)) {
            return loadedFile;
        } else {
            throw new NoSuchEntityException(name);
        }
    }

    @Override
    public TripleCollection getTriples(UriRef name) throws NoSuchEntityException {
        return getMGraph(name);
    }

    @Override
    public Set<UriRef> listGraphs() {
        return Collections.emptySet();
    }

    @Override
    public Set<UriRef> listMGraphs() {
        return Collections.singleton(Constants.SYSTEM_GRAPH_URI);
    }

    @Override
    public Set<UriRef> listTripleCollections() {
        return listMGraphs();
    }

    @Override
    public MGraph createMGraph(UriRef name) throws UnsupportedOperationException, EntityAlreadyExistsException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph createGraph(UriRef name, TripleCollection triples) throws UnsupportedOperationException, EntityAlreadyExistsException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void deleteTripleCollection(UriRef name) throws UnsupportedOperationException, NoSuchEntityException, EntityUndeletableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<UriRef> getNames(Graph graph) {
        return Collections.emptySet();
    }
}
