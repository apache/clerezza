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
package org.apache.clerezza.rdf.core.access.providers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.impl.SimpleGraph;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;

/**
 *
 * @author mir
 */
public class WeightedDummy implements WeightedTcProvider {

    private Map<UriRef, TripleCollection> tripleMap = new HashMap<UriRef, TripleCollection>();

    @Override
    public Graph createGraph(UriRef name, TripleCollection triples)
            throws EntityAlreadyExistsException {
        if ((name == null) || (name.getUnicodeString() == null)
                || (name.getUnicodeString().trim().length() == 0)) {
            throw new IllegalArgumentException("Name must not be null");
        }

        try {
            // throws NoSuchEntityException if a TripleCollection with that name
            // already exists
            this.getTriples(name);
        } catch (NoSuchEntityException e) {
            Graph result;
            if (Graph.class.isAssignableFrom(triples.getClass())) {
                result = (Graph) triples;
            } else {
                result = new SimpleGraph(triples);
            }
            tripleMap.put(name, result);

            return result;
        }
        throw new EntityAlreadyExistsException(name);
    }

    @Override
    public MGraph createMGraph(UriRef name) throws EntityAlreadyExistsException {
        if ((name == null) || (name.getUnicodeString() == null)
                || (name.getUnicodeString().trim().length() == 0)) {
            throw new IllegalArgumentException("Name must not be null");
        }

        try {
            // throws NoSuchEntityException if a TripleCollection with that name
            // already exists
            this.getTriples(name);
        } catch (NoSuchEntityException e) {
            MGraph result = new SimpleMGraph();
            tripleMap.put(name, result);
            return result;
        }
        throw new EntityAlreadyExistsException(name);
    }

    @Override
    public void deleteTripleCollection(UriRef name)
            throws NoSuchEntityException, EntityUndeletableException {
        if (tripleMap.remove(name) == null) {
            throw new NoSuchEntityException(name);
        }
    }

    @Override
    public Graph getGraph(UriRef name) throws NoSuchEntityException {
        TripleCollection tripleCollection = tripleMap.get(name);
        if (tripleCollection == null) {
            throw new NoSuchEntityException(name);
        } else if (Graph.class.isAssignableFrom(tripleCollection.getClass())) {
            return (Graph) tripleCollection;
        }
        throw new NoSuchEntityException(name);
    }

    @Override
    public MGraph getMGraph(UriRef name) throws NoSuchEntityException {
        TripleCollection tripleCollection = tripleMap.get(name);
        if (tripleCollection == null) {
            throw new NoSuchEntityException(name);
        } else if (MGraph.class.isAssignableFrom(tripleCollection.getClass())) {
            return (MGraph) tripleCollection;
        }
        throw new NoSuchEntityException(name);
    }

    @Override
    public Set<UriRef> getNames(Graph graph) {
        throw new UnsupportedOperationException(
                "Not supported yet. equals() has to be implemented first");
    }

    @Override
    public TripleCollection getTriples(UriRef name)
            throws NoSuchEntityException {
        TripleCollection tripleCollection = tripleMap.get(name);
        if (tripleCollection == null) {
            throw new NoSuchEntityException(name);
        } else {
            return tripleCollection;
        }
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public Set<UriRef> listGraphs() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<UriRef> listMGraphs() {
        return new HashSet<UriRef>();
    }

    @Override
    public Set<UriRef> listTripleCollections() {
        return listMGraphs();
    }
}
