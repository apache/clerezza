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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.file.storage.FileGraph;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
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
@Component(immediate = true)
@Service(WeightedTcProvider.class)
public class SystemConfig implements WeightedTcProvider {

    public static final String DEFAULT_SYSTEM_GRAPH = "default-system-graph.rdf";
    private static final String DATA_FILE_SYSTEM_GRAPH = "system-graph.nt";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     *
     * @deprecated use org.apache.clerezza.platform.Contants instead
     */
    @Deprecated
    public static final IRI SYSTEM_GRAPH_URI = Constants.SYSTEM_GRAPH_URI;
    /**
     * A filter that can be used to get the system graph as OSGi service, that
     * is provided by
     * <code>org.apache.clerezza.rdf.core.access.TcManager</code>.
     */
    public static final String SYSTEM_GRAPH_FILTER =
            "(name=" + Constants.SYSTEM_GRAPH_URI_STRING + ")";
    
    public static final String PARSER_FILTER =
            "(&("+SupportedFormat.supportedFormat+"=" + SupportedFormat.RDF_XML + ") "+
            "("+SupportedFormat.supportedFormat+"=" + SupportedFormat.N_TRIPLE + "))";
    @Reference(target = PARSER_FILTER)
    private Parser parser;
    
    public static final String SERIALIZER_FILTER =
            "("+SupportedFormat.supportedFormat+"=" + SupportedFormat.N_TRIPLE + ")";
    @Reference(target = SERIALIZER_FILTER)
    private Serializer serializer;
    private Graph systemGraph;

    @Activate
    protected void activate(ComponentContext componentContext) {
        final BundleContext bundleContext = componentContext.getBundleContext();
        File systemGraphFile = bundleContext.getDataFile(DATA_FILE_SYSTEM_GRAPH);
        boolean dataFileExisted = systemGraphFile.exists();
        //yould be good to use IndexedGraph to be faster
        systemGraph = new FileGraph(systemGraphFile, parser, serializer);
        if (!dataFileExisted) {
            readConfigGraphFile(systemGraph);
            logger.info("Add initial configuration to system graph");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        systemGraph = null;
    }

    private void readConfigGraphFile(Graph mGraph) {
        URL config = getClass().getResource(DEFAULT_SYSTEM_GRAPH);
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
     * Reason to be low: avoid that TcManager always first tries to create Graphs using this provider
     */
    @Override
    public int getWeight() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ImmutableGraph getImmutableGraph(IRI name) throws NoSuchEntityException {
        throw new NoSuchEntityException(name);
    }

    @Override
    public Graph getMGraph(IRI name) throws NoSuchEntityException {
        if (name.equals(Constants.SYSTEM_GRAPH_URI)) {
            return systemGraph;
        } else {
            throw new NoSuchEntityException(name);
        }
    }

    @Override
    public Graph getGraph(IRI name) throws NoSuchEntityException {
        return getMGraph(name);
    }

    @Override
    public Set<IRI> listImmutableGraphs() {
        return Collections.emptySet();
    }

    @Override
    public Set<IRI> listMGraphs() {
        return Collections.singleton(Constants.SYSTEM_GRAPH_URI);
    }

    @Override
    public Set<IRI> listGraphs() {
        return listMGraphs();
    }

    @Override
    public Graph createGraph(IRI name) throws UnsupportedOperationException, EntityAlreadyExistsException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImmutableGraph createImmutableGraph(IRI name, Graph triples) throws UnsupportedOperationException, EntityAlreadyExistsException {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void deleteGraph(IRI name) throws UnsupportedOperationException, NoSuchEntityException, EntityUndeletableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<IRI> getNames(ImmutableGraph graph) {
        return Collections.emptySet();
    }
}
