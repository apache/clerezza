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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;

/**
 * Wrappes an MGraph as a LockableMGraph. If a method is called that reads
 * or modifies the wrapped graph and the appropriate lock is not set, then a
 * RuntimeException is thrown.
 *
 * @author rbn, mir
 */
public class LockableMGraphWrapperForTesting implements LockableMGraph {

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	private final MGraph wrapped;

	/**
	 * Constructs a LocalbleMGraph for an MGraph.
	 *
	 * @param providedMGraph a non-lockable mgraph
	 */
	public LockableMGraphWrapperForTesting(final MGraph providedMGraph) {
		this.wrapped = providedMGraph;
	}

	@Override
	public ReadWriteLock getLock() {
		return lock;
	}

	@Override
	public Graph getGraph() {
		LockChecker.checkIfReadLocked(lock);
		readLock.lock();
		try {
			return wrapped.getGraph();
		} finally {
			readLock.unlock();
		}
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
	public int size() {
		LockChecker.checkIfReadLocked(lock);
		readLock.lock();
		try {
			return wrapped.size();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public boolean isEmpty() {
		LockChecker.checkIfReadLocked(lock);
		readLock.lock();
		try {
			return wrapped.isEmpty();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	@SuppressWarnings("element-type-mismatch")
	public boolean contains(Object o) {
		LockChecker.checkIfReadLocked(lock);
		readLock.lock();
		try {
			return wrapped.contains(o);
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

	@Override
	public Object[] toArray() {
		LockChecker.checkIfReadLocked(lock);
		readLock.lock();
		try {
			return wrapped.toArray();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		LockChecker.checkIfReadLocked(lock);
		readLock.lock();
		try {
			return wrapped.toArray(a);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		LockChecker.checkIfReadLocked(lock);
		readLock.lock();
		try {
			return wrapped.containsAll(c);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public boolean add(Triple e) {
		LockChecker.checkIfWriteLocked(lock);
		writeLock.lock();
		try {
			return wrapped.add(e);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean remove(Object o) {
		LockChecker.checkIfWriteLocked(lock);
		writeLock.lock();
		try {
			return wrapped.remove(o);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean addAll(Collection<? extends Triple> c) {
		LockChecker.checkIfWriteLocked(lock);
		writeLock.lock();
		try {
			return wrapped.addAll(c);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		LockChecker.checkIfWriteLocked(lock);
		writeLock.lock();
		try {
			return wrapped.removeAll(c);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		LockChecker.checkIfWriteLocked(lock);
		writeLock.lock();
		try {
			return wrapped.retainAll(c);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void clear() {
		LockChecker.checkIfWriteLocked(lock);
		writeLock.lock();
		try {
			wrapped.clear();
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void addGraphListener(GraphListener listener, FilterTriple filter, long delay) {
		wrapped.addGraphListener(listener, filter, delay);
	}

	@Override
	public void addGraphListener(GraphListener listener, FilterTriple filter) {
		wrapped.addGraphListener(listener, filter);
	}

	@Override
	public void removeGraphListener(GraphListener listener) {
		wrapped.removeGraphListener(listener);
	}

}
