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
import org.apache.clerezza.rdf.core.Triple;

/**
 * Wrapps an iterator<Triple> reading all elements to a cache on construction
 * and returning them from that cache.
 * @author reto
 */
class LockingIteratorForTesting implements Iterator<Triple> {

    private Iterator<Triple> base;
    private Lock readLock;
    private Lock writeLock;
    private ReentrantReadWriteLock lock;

    public LockingIteratorForTesting(Iterator<Triple> iterator, ReentrantReadWriteLock lock) {
        base = iterator;
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        this.lock = lock;
    }

    @Override
    public boolean hasNext() {
        LockChecker.checkIfReadLocked(lock);
        readLock.lock();
        try {
            return base.hasNext();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Triple next() {
        LockChecker.checkIfReadLocked(lock);
        readLock.lock();
        try {
            return base.next();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void remove() {
        LockChecker.checkIfWriteLocked(lock);
        writeLock.lock();
        try {
            base.remove();
        } finally {
            writeLock.unlock();
        }
    }


}
