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
package org.apache.clerezza.commons.rdf.impl.utils;

import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.WatchableGraph;
import org.apache.clerezza.commons.rdf.event.AddEvent;
import org.apache.clerezza.commons.rdf.event.FilterTriple;
import org.apache.clerezza.commons.rdf.event.GraphEvent;
import org.apache.clerezza.commons.rdf.event.GraphListener;
import org.apache.clerezza.commons.rdf.event.RemoveEvent;
import org.apache.clerezza.commons.rdf.impl.utils.debug.ReentrantReadWriteLockTracker;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleImmutableGraph;

/**
 * An abstract implementation of <code>Graph</code> implementing
 * <code>iterator</code> and <code>contains</code> calling <code>filter</code>.
 *
 * @author reto
 */
public abstract class AbstractGraph extends AbstractCollection<Triple>
        implements Graph {

    
    private static final String DEBUG_MODE = "rdfLocksDebugging";
    private final ReadWriteLock lock;

    private final Lock readLock;
    private final Lock writeLock;

    /**
     * Constructs a LocalbleMGraph for an Graph.
     *
     * @param providedMGraph a non-lockable graph
     */
    public AbstractGraph() {
        {
            String debugMode = System.getProperty(DEBUG_MODE);
            if (debugMode != null && debugMode.toLowerCase().equals("true")) {
                lock = new ReentrantReadWriteLockTracker();
            } else {
                lock = new ReentrantReadWriteLock();
            }
        }
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }
    
    public AbstractGraph(final ReadWriteLock lock) {
        this.lock = lock;
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public ReadWriteLock getLock() {
        return lock;
    }

    @Override
    public ImmutableGraph getImmutableGraph() {
        readLock.lock();
        try {
            return performGetImmutableGraph();
        } finally {
            readLock.unlock();
        }
    }
    
    public ImmutableGraph performGetImmutableGraph() {
        return new SimpleImmutableGraph(this);
    }

    @Override
    public Iterator<Triple> filter(BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
        readLock.lock();
        try {
            return new LockingIterator(performFilter(subject, predicate, object), lock);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public int size() {
        readLock.lock();
        try {
            return performSize();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        try {
            return performIsEmpty();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public boolean contains(Object o) {
        readLock.lock();
        try {
            return performContains(o);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Iterator<Triple> iterator() {
        readLock.lock();
        try {
            return new LockingIterator(performIterator(), lock);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        readLock.lock();
        try {
            return performToArray();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        readLock.lock();
        try {
            return performToArray(a);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        readLock.lock();
        try {
            return performContainsAll(c);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public boolean add(Triple e) {
        writeLock.lock();
        try {
            return performAdd(e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        writeLock.lock();
        try {
            return performRemove(o);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends Triple> c) {
        writeLock.lock();
        try {
            return performAddAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        writeLock.lock();
        try {
            return performRemoveAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        writeLock.lock();
        try {
            return performRetainAll(c);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        try {
            performClear();
        } finally {
            writeLock.unlock();
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        /*if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }*/
        return this == obj;
    }


    protected abstract Iterator<Triple> performFilter(BlankNodeOrIRI subject, IRI predicate, RDFTerm object);

    protected abstract int performSize();

    protected boolean performIsEmpty() {
        return super.isEmpty();
    }

    protected Object[] performToArray() {
        return super.toArray();
    }

    protected boolean performRemove(Object o) {
        return super.remove(o);
    }

    protected boolean performAddAll(Collection<? extends Triple> c) {
        return super.addAll(c);
    }

    protected boolean performRemoveAll(Collection<?> c) {
        return super.removeAll(c);
    }

    protected boolean performRetainAll(Collection<?> c) {
        return super.retainAll(c);
    }

    protected void performClear() {
        super.clear();
    }

    protected boolean performContains(Object o) {
        return super.contains(o);
    }

    protected Iterator<Triple> performIterator() {
        return performFilter(null, null, null);
    }

    protected boolean performContainsAll(Collection<?> c) {
        return super.containsAll(c);
    }

    protected <T> T[] performToArray(T[] a) {
        return super.toArray(a);
    }

    protected boolean performAdd(Triple e) {
        return super.add(e);
    }

 
}
