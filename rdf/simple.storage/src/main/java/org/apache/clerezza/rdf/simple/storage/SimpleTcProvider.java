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
package org.apache.clerezza.rdf.simple.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleImmutableGraph;
import org.osgi.service.component.annotations.Component;



@Component(service = WeightedTcProvider.class, property = TcManager.GENERAL_PURPOSE_TC+"=true")
public class SimpleTcProvider implements WeightedTcProvider {

    private Map<Iri, Graph> tripleMap = new HashMap<Iri, Graph>();

    @Override
    public ImmutableGraph createImmutableGraph(Iri name, Graph triples)
            throws EntityAlreadyExistsException {
        if ((name == null) || (name.getUnicodeString() == null)
                || (name.getUnicodeString().trim().length() == 0)) {
            throw new IllegalArgumentException("Name must not be null");
        }

        try {
            // throws NoSuchEntityException if a Graph with that name
            // already exists
            this.getGraph(name);
        } catch (NoSuchEntityException e) {
            ImmutableGraph result;
            if (triples == null) {
                result = new SimpleImmutableGraph(new SimpleGraph());
            } else {
                if (ImmutableGraph.class.isAssignableFrom(triples.getClass())) {
                    result = (ImmutableGraph) triples;
                } else {
                    result = new SimpleImmutableGraph(triples);
                }
            }
            tripleMap.put(name, result);

            return result;
        }
        throw new EntityAlreadyExistsException(name);
    }

    @Override
    public Graph createGraph(Iri name) throws EntityAlreadyExistsException {
        if ((name == null) || (name.getUnicodeString() == null)
                || (name.getUnicodeString().trim().length() == 0)) {
            throw new IllegalArgumentException("Name must not be null");
        }

        try {
            // throws NoSuchEntityException if a Graph with that name
            // already exists
            this.getGraph(name);
        } catch (NoSuchEntityException e) {
            Graph result = new SimpleGraph();
            tripleMap.put(name, result);
            return result;
        }
        throw new EntityAlreadyExistsException(name);
    }

    @Override
    public void deleteGraph(Iri name)
            throws NoSuchEntityException, EntityUndeletableException {
        if (tripleMap.remove(name) == null) {
            throw new NoSuchEntityException(name);
        }
    }

    @Override
    public ImmutableGraph getImmutableGraph(Iri name) throws NoSuchEntityException {
        Graph tripleCollection = tripleMap.get(name);
        if (tripleCollection == null) {
            throw new NoSuchEntityException(name);
        } else if (ImmutableGraph.class.isAssignableFrom(tripleCollection.getClass())) {
            return (ImmutableGraph) tripleCollection;
        }
        throw new NoSuchEntityException(name);
    }

    @Override
    public Graph getMGraph(Iri name) throws NoSuchEntityException {
        Graph tripleCollection = tripleMap.get(name);
        if (tripleCollection == null) {
            throw new NoSuchEntityException(name);
        } else if (!ImmutableGraph.class.isAssignableFrom(tripleCollection.getClass())) {
            return (Graph) tripleCollection;
        }
        throw new NoSuchEntityException(name);
    }

    @Override
    public Set<Iri> getNames(ImmutableGraph graph) {
        throw new UnsupportedOperationException(
                "Not supported yet. equals() has to be implemented first");
    }

    @Override
    public Graph getGraph(Iri name)
            throws NoSuchEntityException {
        Graph tripleCollection = tripleMap.get(name);
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
    public Set<Iri> listImmutableGraphs() {
        Set<Iri> result = new HashSet<Iri>();
        for (Iri uriRef : listGraphs()) {
            if (tripleMap.get(uriRef) instanceof ImmutableGraph) {
                result.add(uriRef);
            }
        }
        return result;
    }

    @Override
    public Set<Iri> listMGraphs() {
        Set<Iri> result = new HashSet<Iri>();
        for (Iri uriRef : listGraphs()) {
            if (!(tripleMap.get(uriRef) instanceof ImmutableGraph)) {
                result.add(uriRef);
            }
        }
        return result;
    }

    @Override
    public Set<Iri> listGraphs() {
        return tripleMap.keySet();
    }
}
