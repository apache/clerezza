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
package org.apache.clerezza.sparql.update.impl;

import org.apache.clerezza.api.IRI;
import org.apache.clerezza.sparql.GraphStore;
import org.apache.clerezza.sparql.query.TriplePattern;
import org.apache.clerezza.sparql.query.UriRefOrVariable;
import org.apache.clerezza.sparql.update.UpdateOperation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author hasan
 */
public class UpdateOperationWithQuads implements UpdateOperation {

    private Quad defaultQuad = null;
    private List<Quad> quads = new ArrayList<Quad>();

    public UpdateOperationWithQuads() {
    }

    public void addQuad(Set<TriplePattern> triplePatterns) {
        if (defaultQuad == null) {
            defaultQuad = new Quad(null, triplePatterns);
        } else {
            defaultQuad.addTriplePatterns(triplePatterns);
        }
    }

    public void addQuad(UriRefOrVariable ImmutableGraph, Set<TriplePattern> triplePatterns) {
        if (ImmutableGraph == null) {
            addQuad(triplePatterns);
        } else {
            quads.add(new Quad(ImmutableGraph, triplePatterns));
        }
    }

    @Override
    public Set<IRI> getInputGraphs(IRI defaultGraph, GraphStore graphStore) {
        return new HashSet<IRI>();
    }

    @Override
    public Set<IRI> getDestinationGraphs(IRI defaultGraph, GraphStore graphStore) {
        Set<IRI> graphs = new HashSet<IRI>();
        if (defaultQuad != null) {
            graphs.add(defaultGraph);
        }
        for (Quad quad : quads) {
            UriRefOrVariable ImmutableGraph = quad.getGraph();
            if (!ImmutableGraph.isVariable()) {
                graphs.add(ImmutableGraph.getResource());
            }
        }
        return graphs;
    }
}
