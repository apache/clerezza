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
package org.apache.clerezza.rdf.core.impl;

import java.util.Iterator;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;

/**
 *
 * @author reto
 */
public class SimpleGraph extends AbstractGraph {

    private TripleCollection tripleCollection;
    
    /**
     * Creates a graph with the triples in tripleCollection
     * 
     * @param tripleCollection the collection of triples this Graph shall consist of
     */
    public SimpleGraph(TripleCollection tripleCollection) {
        this.tripleCollection = new SimpleTripleCollection(tripleCollection.iterator());
    }

    /**
     * Creates a graph with the triples in tripleCollection.
     *
     * This construction allows to specify if the tripleCollection might change
     * in future. If tripleCollectionWillNeverChange is set to true it will
     * assume that the collection never changes, in this case the collection
     * isn't copied making things more efficient.
     *
     * @param tripleCollection the collection of triples this Graph shall consist of
     * @param tripleCollectionWillNeverChange true if the caller promises tripleCollection will never change
     */
    public SimpleGraph(TripleCollection tripleCollection, boolean tripleCollectionWillNeverChange) {
        if (!tripleCollectionWillNeverChange) {
            this.tripleCollection = new SimpleTripleCollection(tripleCollection.iterator());
        } else {
            this.tripleCollection = tripleCollection;
        }
    }
    
    public SimpleGraph(Iterator<Triple> tripleIter) {
        this.tripleCollection = new SimpleTripleCollection(tripleIter);
    }

    @Override
    public int size() {
        return tripleCollection.size();
    }

    @Override
    public Iterator<Triple> performFilter(NonLiteral subject, UriRef predicate, Resource object) {
        return tripleCollection.filter(subject, predicate, object);
    }
}
