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
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 *
 * @author mir
 */
public class WriteLockDebug extends WriteLock {

    private ReentrantReadWriteLockTracker lock;
    private WriteLock writeLock;
    private StackTraceElement[] stackTrace;

    public WriteLockDebug(ReentrantReadWriteLockTracker lock) {
        super(lock);
        this.lock = lock;
        this.writeLock = lock.realWriteLock();
    }

    @Override
    public int getHoldCount() {
        return writeLock.getHoldCount();
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return writeLock.isHeldByCurrentThread();
    }

    @Override
    public void lock() {
        writeLock.lock();
        stackTrace = Thread.currentThread().getStackTrace();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        writeLock.lockInterruptibly();
    }

    @Override
    public Condition newCondition() {
        return writeLock.newCondition();
    }

    @Override
    public boolean tryLock() {
        return writeLock.tryLock();
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return writeLock.tryLock(timeout, unit);
    }

    @Override
    public void unlock() {
        writeLock.unlock();
        stackTrace = null;
    }

    public StackTraceElement[] getStackTrace() {
        return stackTrace;
    }


}
