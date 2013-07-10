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
package org.apache.clerezza.rdf.core.test;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraphWrapper;

/**
 * Wrappes an MGraph as a LockableMGraph. If a method is called that reads
 * or modifies the wrapped graph and the appropriate lock is not set, then a
 * RuntimeException is thrown.
 *
 * @author rbn, mir
 */
public class LockableMGraphWrapperForTesting extends LockableMGraphWrapper {

    private final ReentrantReadWriteLock lock = (ReentrantReadWriteLock) getLock();
    private final Lock readLock = lock.readLock();
    private final MGraph wrapped;

    /**
     * Constructs a LocalbleMGraph for an MGraph.
     *
     * @param providedMGraph a non-lockable mgraph
     */
    public LockableMGraphWrapperForTesting(final MGraph providedMGraph) {
        super(providedMGraph);
        this.wrapped = providedMGraph;
    }

    @Override
    public Iterator<Triple> filter(NonLiteral subject, UriRef predicate, Resource object) {
        LockChecker.checkIfReadLocked(lock);
        readLock.lock();
        try {
            return new LockingIteratorForTesting(wrapped.filter(subject, predicate, object), lock);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Iterator<Triple> iterator() {
        LockChecker.checkIfReadLocked(lock);
        readLock.lock();
        try {
            return new LockingIteratorForTesting(wrapped.iterator(), lock);
        } finally {
            readLock.unlock();
        }
    }

}
