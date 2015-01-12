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

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.rdf.BlankNodeOrIri;
import org.apache.commons.rdf.RdfTerm;
import org.apache.commons.rdf.Triple;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.ReadOnlyException;
import org.apache.commons.rdf.ImmutableGraph;
import org.apache.commons.rdf.event.FilterTriple;
import org.apache.commons.rdf.event.GraphListener;

/**
 *
 * This is a wrapper object for <code>Graph</code>. If <code>SecurityManger</code> 
 * is not <code>null</code> <code>TcManager</code> checks the <code>TcPermission</code>. 
 * If read-only permissions are set this wrapper is used instead of <code>Graph</code> 
 * and throws exceptions when add or remove methods are called.
 *
 * @author tsuy
 */
public class WriteBlockedGraph extends AbstractCollection<Triple>
        implements Graph {

    private Graph triples;

    public WriteBlockedGraph(Graph triples) {
        this.triples = triples;
    }

    @Override
    public int size() {
        return triples.size();
    }

    @Override
    public Iterator<Triple> filter(final BlankNodeOrIri subject, final Iri predicate, final RdfTerm object) {
        final Iterator<Triple> baseIter = triples.filter(subject, predicate, object);
        return new Iterator<Triple>() {
            
            @Override
            public boolean hasNext() {
                return baseIter.hasNext();
            }

            @Override
            public Triple next() {
                return baseIter.next();
            }

            @Override
            public void remove() {
                throw new ReadOnlyException("remove");
            }

            
        };
    }

    @Override
    public boolean add(Triple e) {
        throw new ReadOnlyException("add");
    }

    @Override
    public boolean addAll(Collection<? extends Triple> c) {
        throw new ReadOnlyException("add all");
    }

    @Override
    public void clear() {
        throw new ReadOnlyException("clear");
    }

    @Override
    public boolean remove(Object o) {
        throw new ReadOnlyException("remove");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new ReadOnlyException("remove all");
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new ReadOnlyException("retain all");
    }

    @Override
    public void addGraphListener(GraphListener listener, FilterTriple filter,
            long delay) {
        triples.addGraphListener(listener, filter, delay);
    }

    @Override
    public void addGraphListener(GraphListener listener, FilterTriple filter) {
        triples.addGraphListener(listener, filter);
    }

    @Override
    public void removeGraphListener(GraphListener listener) {
        triples.removeGraphListener(listener);
    }

    @Override
    public Iterator iterator() {
        return filter(null, null, null);
    }
    
    @Override
    public ImmutableGraph getImmutableGraph() {
        return this.triples.getImmutableGraph();
    }
}
