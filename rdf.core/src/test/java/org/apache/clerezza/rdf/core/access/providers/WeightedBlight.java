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

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.access.TcManagerTest;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;

/**
 *
 * @author reto
 */
public class WeightedBlight implements WeightedTcProvider {

    @Override
    public int getWeight() {
        return 2;
    }

    @Override
    public Graph getGraph(UriRef name) throws NoSuchEntityException {
        if (name.equals(TcManagerTest.uriRefB)) {
            MGraph mResult = new SimpleMGraph();
            mResult.add(new TripleImpl(TcManagerTest.uriRefB, TcManagerTest.uriRefB, TcManagerTest.uriRefB));
            return mResult.getGraph();
        }
        if (name.equals(TcManagerTest.uriRefA)) {
            MGraph mResult = new SimpleMGraph();
            //empty graph
            return mResult.getGraph();
        }
        throw new NoSuchEntityException(name);
    }

    @Override
    public MGraph getMGraph(UriRef name) throws NoSuchEntityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TripleCollection getTriples(UriRef name) throws NoSuchEntityException {
        return getGraph(name);
    }

    @Override
    public MGraph createMGraph(UriRef name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Graph createGraph(UriRef name, TripleCollection triples) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteTripleCollection(UriRef name) throws NoSuchEntityException, 
            EntityUndeletableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<UriRef> getNames(Graph graph) {
        throw new UnsupportedOperationException("Not supported yet.");
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
