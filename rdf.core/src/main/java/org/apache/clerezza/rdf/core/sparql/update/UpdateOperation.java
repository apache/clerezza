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
package org.apache.clerezza.rdf.core.sparql.update;

import java.util.Set;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcProvider;

/**
 * SPARQL Update Operation
 *
 * @author hasan
 */
public interface UpdateOperation {
    public enum GraphSpec {
        GRAPH, DEFAULT, NAMED, ALL
    }

    /**
     * 
     * @param defaultGraph
     *      if default graph is referred either implicitly or explicitly as an input graph in this operation
     *      the specified defaultGraph should be returned in the resulting set.
     * @param tcProvider
     *      the specified tcProvider is used to get the named graphs referred as input graphs in this operation.
     * @return a set of graphs referred as input graphs in this operation.
     */
    public Set<UriRef> getInputGraphs(UriRef defaultGraph, TcProvider tcProvider);

    /**
     * 
     * @param defaultGraph
     *      if default graph is referred either implicitly or explicitly as a destination graph in this operation
     *      the specified defaultGraph should be returned in the resulting set.
     * @param tcProvider
     *      the specified tcProvider is used to get the named graphs referred as destination graphs in this operation.
     * @return a set of graphs referred as destination graphs in this operation.
     */
    public Set<UriRef> getDestinationGraphs(UriRef defaultGraph, TcProvider tcProvider);
}
