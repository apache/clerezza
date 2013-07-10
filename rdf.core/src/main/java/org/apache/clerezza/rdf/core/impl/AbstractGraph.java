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

import java.util.Collection;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.impl.graphmatching.GraphMatcher;

/**
 * <code>AbstractGraph</code> is an abstract implementation of <code>Graph</code> 
 * implementing the <code>equals</code> and the <code>hashCode</code> methods.
 * 
 * @author reto
 * 
 */
public abstract class AbstractGraph extends AbstractTripleCollection
        implements Graph {

    public final synchronized int hashCode() {
        int result = 0;
        for (Iterator<Triple> iter = iterator(); iter.hasNext();) {
            result += getBlankNodeBlindHash(iter.next());
        }
        return result;
    }

    /**
     * @param triple
     * @return hash without BNode hashes
     */
    private int getBlankNodeBlindHash(Triple triple) {
        int hash = triple.getPredicate().hashCode();
        Resource subject = triple.getSubject();

        if (!(subject instanceof BNode)) {
            hash ^= subject.hashCode() >> 1;
        }
        Resource object = triple.getObject();
        if (!(object instanceof BNode)) {
            hash ^= object.hashCode() << 1;
        }

        return hash;
    }

    @Override
    public boolean add(Triple e) {
        throw new UnsupportedOperationException("Graphs are not mutable, use MGraph");

    }

    @Override
    public boolean addAll(Collection<? extends Triple> c) {
        throw new UnsupportedOperationException("Graphs are not mutable, use MGraph");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Graphs are not mutable, use MGraph");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Graphs are not mutable, use MGraph");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Graphs are not mutable, use MGraph");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Graph)) {
            return false;
        }
        if (hashCode() != obj.hashCode()) {
            return false;
        }
        return GraphMatcher.getValidMapping(this, (Graph) obj) != null;
    }
}
