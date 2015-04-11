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
package org.apache.clerezza.rdf.core.sparql.update.impl;

import java.util.HashSet;
import java.util.Set;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.sparql.update.UpdateOperation;

/**
 *
 * @author hasan
 */
public abstract class BaseUpdateOperation implements UpdateOperation {

    protected Set<IRI> inputGraphs = new HashSet<IRI>();
    protected Set<IRI> destinationGraphs = new HashSet<IRI>();
    protected GraphSpec inputGraphSpec = GraphSpec.GRAPH;
    protected GraphSpec destinationGraphSpec = GraphSpec.GRAPH;

    public void setInputGraphSpec(GraphSpec inputGraphSpec) {
        this.inputGraphSpec = inputGraphSpec;
    }

    public GraphSpec getInputGraphSpec() {
        return inputGraphSpec;
    }

    public void setDestinationGraphSpec(GraphSpec destinationGraphSpec) {
        this.destinationGraphSpec = destinationGraphSpec;
    }

    public GraphSpec getDestinationGraphSpec() {
        return destinationGraphSpec;
    }

    @Override
    public Set<IRI> getInputGraphs(IRI defaultGraph, TcProvider tcProvider) {
        return getGraphs(defaultGraph, tcProvider, inputGraphSpec, inputGraphs);
    }

    private Set<IRI> getGraphs(IRI defaultGraph, TcProvider tcProvider, GraphSpec graphSpec, Set<IRI> graphs) {
        switch (graphSpec) {
            case DEFAULT:
                Set<IRI> result = new HashSet<IRI>();
                result.add(defaultGraph);
                return result;
            case NAMED:
            case ALL:
                return tcProvider.listGraphs();
            default:
                return graphs;
        }
    }

    @Override
    public Set<IRI> getDestinationGraphs(IRI defaultGraph, TcProvider tcProvider) {
        return getGraphs(defaultGraph, tcProvider, destinationGraphSpec, destinationGraphs);
    }

    public void addInputGraph(IRI ImmutableGraph) {
        inputGraphs.add(ImmutableGraph);
    }

    public void addDestinationGraph(IRI ImmutableGraph) {
        destinationGraphs.add(ImmutableGraph);
    }
}
