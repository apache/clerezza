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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

/**
 *
 * @author mir
 */
public class ReadLockDebug extends ReadLock {

    ReentrantReadWriteLockTracker lock;
    StackTraceElement[] stackTrace;

    ReadLock readLock;
    public ReadLockDebug(ReentrantReadWriteLockTracker lock) {
        super(lock);
        this.lock = lock;
        this.readLock = lock.realReadLock();
    }

    @Override
    public void lock() {
        readLock.lock();
        lock.addLockedReadLock(this);
        stackTrace = Thread.currentThread().getStackTrace();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        readLock.lockInterruptibly();
    }

    @Override
    public Condition newCondition() {
        return readLock.newCondition();
    }

    @Override
    public String toString() {
        return readLock.toString();
    }

    @Override
    public boolean tryLock() {
        return readLock.tryLock();
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return readLock.tryLock(timeout, unit);
    }

    @Override
    public void unlock() {
        readLock.unlock();
        lock.removeReadLock(this);
        stackTrace = null;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }

}
