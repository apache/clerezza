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
package org.apache.clerezza.commons.rdf.impl.utils.debug;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author mir
 */
public class ReentrantReadWriteLockTracker extends ReentrantReadWriteLock {


    private Set<ReadLockDebug> lockedReadLocks = Collections.synchronizedSet(new HashSet<ReadLockDebug>());
    private final WriteLockDebug writeLock = new WriteLockDebug(this);
    @Override
    protected Thread getOwner() {
        return super.getOwner();
    }

    @Override
    protected Collection<Thread> getQueuedReaderThreads() {
        return super.getQueuedReaderThreads();
    }

    @Override
    protected Collection<Thread> getQueuedThreads() {
        return super.getQueuedThreads();
    }

    @Override
    protected Collection<Thread> getQueuedWriterThreads() {
        return super.getQueuedWriterThreads();
    }

    @Override
    public int getReadHoldCount() {
        return super.getReadHoldCount();
    }

    @Override
    public int getReadLockCount() {
        return super.getReadLockCount();
    }

    @Override
    public int getWaitQueueLength(Condition condition) {
        return super.getWaitQueueLength(condition);
    }

    @Override
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        return super.getWaitingThreads(condition);
    }

    @Override
    public int getWriteHoldCount() {
        return super.getWriteHoldCount();
    }

    @Override
    public boolean hasWaiters(Condition condition) {
        return super.hasWaiters(condition);
    }

    @Override
    public boolean isWriteLocked() {
        return super.isWriteLocked();
    }

    @Override
    public boolean isWriteLockedByCurrentThread() {
        return super.isWriteLockedByCurrentThread();
    }

    @Override
    public ReadLock readLock() {
        return new ReadLockDebug(this);
    }

    ReadLock realReadLock() {
        return super.readLock();
    }

    WriteLock realWriteLock() {
        return super.writeLock();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public WriteLockDebug writeLock() {
        return writeLock;
    }

    void addLockedReadLock(ReadLockDebug lock) {
        lockedReadLocks.add(lock);
    }

    void removeReadLock(ReadLockDebug lock) {
        lockedReadLocks.remove(lock);
    }

    public Set<ReadLockDebug> getLockedReadLocks() {
        return lockedReadLocks;
    }


}
