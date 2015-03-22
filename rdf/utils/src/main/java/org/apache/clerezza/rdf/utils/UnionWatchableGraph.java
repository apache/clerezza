/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.clerezza.rdf.utils;

import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.WatchableGraph;
import org.apache.commons.rdf.event.FilterTriple;
import org.apache.commons.rdf.event.GraphListener;

/**
 *
 * @author developer
 */
public class UnionWatchableGraph extends UnionGraph implements WatchableGraph {
    
    public UnionWatchableGraph(WatchableGraph... baseTripleCollections) {
        super(baseTripleCollections);
    }
        @Override
    public void addGraphListener(GraphListener listener, FilterTriple filter) {
        for (Graph graph : baseTripleCollections) {
            ((WatchableGraph)graph).addGraphListener(listener, filter);
        }
    }

    @Override
    public void addGraphListener(GraphListener listener, FilterTriple filter, long delay) {
        for (Graph graph : baseTripleCollections) {
            ((WatchableGraph)graph).addGraphListener(listener, filter, delay);
        }
    }

    @Override
    public void removeGraphListener(GraphListener listener) {
        for (Graph graph : baseTripleCollections) {
            ((WatchableGraph)graph).removeGraphListener(listener);
        }
    }

    
}
