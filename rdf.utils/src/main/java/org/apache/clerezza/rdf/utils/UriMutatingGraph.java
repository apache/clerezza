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

package org.apache.clerezza.rdf.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.commons.rdf.BlankNodeOrIri;
import org.apache.commons.rdf.RdfTerm;
import org.apache.commons.rdf.Triple;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.ImmutableGraph;
import org.apache.commons.rdf.Iri;
import org.apache.commons.rdf.impl.utils.AbstractGraph;
import org.apache.commons.rdf.impl.utils.TripleImpl;
import org.apache.commons.rdf.impl.utils.simple.SimpleImmutableGraph;

/**
 * This wrapps a Triplecollection changing a prefix for the Iris contained
 * in subject or object position.
 *
 * Currently it only supports read opearations.
 *
 * @author reto
 */
public class UriMutatingGraph implements Graph {

    private final Graph base;
    private final String sourcePrefix;
    private final String targetPrefix;
    private final int sourcePrefixLength;
    private final int targetPrefixLength;

    public UriMutatingGraph(Graph base, String sourcePrefix,
            String targetPrefix) {
        this.base = base;
        this.sourcePrefix = sourcePrefix;
        sourcePrefixLength= sourcePrefix.length();
        this.targetPrefix = targetPrefix;
        targetPrefixLength= targetPrefix.length();
    }

    private <R extends RdfTerm> R toTargetRdfTerm(final R sourceRdfTerm) {
        if (sourceRdfTerm instanceof Iri) {
            final Iri sourceIri = (Iri) sourceRdfTerm;
            if (sourceIri.getUnicodeString().startsWith(sourcePrefix)) {
                final String uriRest = sourceIri.getUnicodeString()
                        .substring(sourcePrefixLength);
                return (R) new Iri(targetPrefix+uriRest);
            }
        }
        return sourceRdfTerm;            
    }

    private Triple toTargetTriple(Triple triple) {
        if (triple == null) {
            return null;
        }
        return new TripleImpl(toTargetRdfTerm(triple.getSubject()),
                triple.getPredicate(), toTargetRdfTerm(triple.getObject()));
    }

    private <R extends RdfTerm> R toSourceRdfTerm(final R targetRdfTerm) {
        if (targetRdfTerm instanceof Iri) {
            final Iri sourceIri = (Iri) targetRdfTerm;
            if (sourceIri.getUnicodeString().startsWith(targetPrefix)) {
                final String uriRest = sourceIri.getUnicodeString()
                        .substring(targetPrefixLength);
                return (R) new Iri(sourcePrefix+uriRest);
            }
        }
        return targetRdfTerm;
    }

    private Triple toSourceTriple(Triple triple) {
        if (triple == null) {
            return null;
        }
        return new TripleImpl(toSourceRdfTerm(triple.getSubject()),
                triple.getPredicate(), toSourceRdfTerm(triple.getObject()));
    }

    @Override
    public Iterator<Triple> filter(BlankNodeOrIri subject, Iri predicate, RdfTerm object) {
        final Iterator<Triple> baseIter = base.filter(toSourceRdfTerm(subject),
                predicate, toSourceRdfTerm(object));
        return new WrappedIteraror(baseIter);


    }


    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return base.contains(toSourceTriple((Triple)o));
    }

    @Override
    public Iterator<Triple> iterator() {
        return filter(null, null, null);
    }

    @Override
    public Object[] toArray() {
        Object[] result = base.toArray();
        for (int i = 0; i < result.length; i++) {
            Triple triple = (Triple) result[i];
            result[i] = toTargetTriple(triple);
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        T[] result = base.toArray(a);
        for (int i = 0; i < result.length; i++) {
            Triple triple = (Triple) result[i];
            result[i] = (T) toTargetTriple(triple);
        }
        return result;
    }

    @Override
    public boolean add(Triple e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object object : c) {
            if (!contains(object)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Triple> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ImmutableGraph getImmutableGraph() {
        return new SimpleImmutableGraph(this);
    }

    @Override
    public ReadWriteLock getLock() {
        return base.getLock();
    }

    class WrappedIteraror implements Iterator<Triple>{
        private final Iterator<Triple> baseIter;

        private WrappedIteraror(Iterator<Triple> baseIter) {
            this.baseIter = baseIter;
        }

        @Override
        public boolean hasNext() {
            return baseIter.hasNext();
        }

        @Override
        public Triple next() {
            return toTargetTriple(baseIter.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
