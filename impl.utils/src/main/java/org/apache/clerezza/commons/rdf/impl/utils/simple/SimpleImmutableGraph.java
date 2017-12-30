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
package org.apache.clerezza.commons.rdf.impl.utils.simple;

import org.apache.clerezza.commons.rdf.impl.utils.AbstractImmutableGraph;
import java.util.Iterator;

import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;

/**
 *
 * @author reto
 */
public class SimpleImmutableGraph extends AbstractImmutableGraph {

    private Graph graph;
    
    /**
     * Creates a ImmutableGraph with the triples in Graph
     * 
     * @param Graph the collection of triples this ImmutableGraph shall consist of
     */
    public SimpleImmutableGraph(Graph Graph) {
        this.graph = new SimpleGraph(Graph.iterator());
    }

    /**
     * Creates a ImmutableGraph with the triples in Graph.
     *
     * This construction allows to specify if the Graph might change
     * in future. If GraphWillNeverChange is set to true it will
     * assume that the collection never changes, in this case the collection
     * isn't copied making things more efficient.
     *
     * @param Graph the collection of triples this ImmutableGraph shall consist of
     * @param GraphWillNeverChange true if the caller promises Graph will never change
     */
    public SimpleImmutableGraph(Graph Graph, boolean GraphWillNeverChange) {
        if (!GraphWillNeverChange) {
            this.graph = new SimpleGraph(Graph.iterator());
        } else {
            this.graph = Graph;
        }
    }
    
    public SimpleImmutableGraph(Iterator<Triple> tripleIter) {
        this.graph = new SimpleGraph(tripleIter);
    }

    @Override
    public int performSize() {
        return graph.size();
    }

    @Override
    public Iterator<Triple> performFilter(BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
        return graph.filter(subject, predicate, object);
    }
}
