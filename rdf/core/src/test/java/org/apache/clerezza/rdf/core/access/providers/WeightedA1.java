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

import java.util.HashSet;
import java.util.Set;

import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.access.TcManagerTest;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleMGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;

/**
 * Same weight as WeightedA, but later in string-ordering
 *
 * @author reto
 */
public class WeightedA1 implements WeightedTcProvider {
    private Set<IRI> mGraphList = new HashSet<IRI>();
    @Override
    public int getWeight() {
        return 5;
    }

    @Override
    public ImmutableGraph getImmutableGraph(IRI name) throws NoSuchEntityException {
        if (name.equals(TcManagerTest.uriRefA)) {
            Graph mResult = new SimpleMGraph();
            mResult.add(new TripleImpl(TcManagerTest.uriRefA1, 
                    TcManagerTest.uriRefA1, TcManagerTest.uriRefA1));
            mGraphList.add(name);
            return mResult.getImmutableGraph();
        }
        throw new NoSuchEntityException(name);
    }

    @Override
    public Graph getMGraph(IRI name) throws NoSuchEntityException {
        throw new NoSuchEntityException(name);
    }

    @Override
    public Graph getGraph(IRI name) throws NoSuchEntityException {
        return getImmutableGraph(name);
    }

    @Override
    public Graph createGraph(IRI name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImmutableGraph createImmutableGraph(IRI name, Graph triples) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteGraph(IRI name) throws NoSuchEntityException, 
            EntityUndeletableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<IRI> getNames(ImmutableGraph ImmutableGraph) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<IRI> listImmutableGraphs() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<IRI> listMGraphs() {
        return mGraphList;
    }

    @Override
    public Set<IRI> listGraphs() {
        return listMGraphs();
    }
}
