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
package org.apache.clerezza.commons.rdf.event;

import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;

/**
 * This class represent a modification event that occured on a
 * <code>TripleCollection</code>. A <code>GraphEvent</code> object keeps
 * information about this event. These information are: The <code>Triple</code>
 * that was part of the modification, the type of modification (addition or
 * removal) and the <code>TripleCollection</code> that was modified.
 *
 * @author mir
 */
public class GraphEvent {

    private Graph graph;
    private Triple triple;

    protected GraphEvent(Graph graph, Triple triple) {
        this.graph = graph;
        this.triple = triple;
    }

    /**
     * Returns the <code>TripleCollection</code> that was modified in the event.
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }


    /**
     * Return the <code>Triple</code> that was part of the modification.
     * @return the triple
     */
    public Triple getTriple() {
        return triple;
    }
}
