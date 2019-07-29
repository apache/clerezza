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
package org.apache.clerezza.dataset.providers;

import org.apache.clerezza.Graph;
import org.apache.clerezza.IRI;
import org.apache.clerezza.ImmutableGraph;
import org.apache.clerezza.impl.TripleImpl;
import org.apache.clerezza.impl.graph.SimpleMGraph;
import org.apache.clerezza.dataset.EntityUndeletableException;
import org.apache.clerezza.dataset.NoSuchEntityException;
import org.apache.clerezza.dataset.TcManagerTest;
import org.apache.clerezza.dataset.WeightedTcProvider;

import java.util.HashSet;
import java.util.Set;

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
