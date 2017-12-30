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

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.clerezza.commons.rdf.Triple;

/**
 * Wrapps an iterator<Triple> reading entering a read-lock on every invocation
 * of hasNext and next
 * @author reto
 */
class LockingIterator implements Iterator<Triple> {

    private Iterator<Triple> base;
    private Lock readLock;
    private Lock writeLock;

    public LockingIterator(Iterator<Triple> iterator, ReadWriteLock lock) {
        base = iterator;
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public boolean hasNext() {
        readLock.lock();
        try {
            return base.hasNext();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Triple next() {
        readLock.lock();
        try {
            return base.next();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void remove() {
        writeLock.lock();
        try {
            base.remove();
        } finally {
            writeLock.unlock();
        }
    }

}
