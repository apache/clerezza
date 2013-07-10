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
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.sparql.update.UpdateOperation;

/**
 *
 * @author hasan
 */
public abstract class SimpleUpdateOperation implements UpdateOperation {

    protected Set<UriRef> inputGraphs = new HashSet<UriRef>();
    protected Set<UriRef> destinationGraphs = new HashSet<UriRef>();
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
    public Set<UriRef> getInputGraphs(UriRef defaultGraph, TcProvider tcProvider) {
        return getGraphs(defaultGraph, tcProvider, inputGraphSpec, inputGraphs);
    }

    private Set<UriRef> getGraphs(UriRef defaultGraph, TcProvider tcProvider, GraphSpec graphSpec, Set<UriRef> graphs) {
        switch (graphSpec) {
            case DEFAULT:
                Set<UriRef> result = new HashSet<UriRef>();
                result.add(defaultGraph);
                return result;
            case NAMED:
            case ALL:
                return tcProvider.listTripleCollections();
            default:
                return graphs;
        }
    }

    @Override
    public Set<UriRef> getDestinationGraphs(UriRef defaultGraph, TcProvider tcProvider) {
        return getGraphs(defaultGraph, tcProvider, destinationGraphSpec, destinationGraphs);
    }

    public void addInputGraph(UriRef graph) {
        inputGraphs.add(graph);
    }

    public void addDestinationGraph(UriRef graph) {
        destinationGraphs.add(graph);
    }
}
