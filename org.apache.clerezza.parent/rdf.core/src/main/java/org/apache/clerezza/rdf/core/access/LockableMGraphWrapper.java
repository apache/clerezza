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
package org.apache.clerezza.rdf.core.access;

import org.apache.clerezza.rdf.core.access.debug.ReentrantReadWriteLockTracker;
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
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;

/**
 * Wrappes an MGraph as a LockableMGraph, this class is used by TcManager to
 * support TcProviders that do not privide <code>LockableMGraph</code>.
 *
 * @author rbn
 */
public class LockableMGraphWrapper implements LockableMGraph {


	private static final String DEBUG_MODE = "rdfLocksDebugging";
	private final ReadWriteLock lock;
	{
		String debugMode = System.getProperty(DEBUG_MODE);
		if (debugMode != null && debugMode.toLowerCase().equals("true")) {
			lock = new ReentrantReadWriteLockTracker();
		} else {
			lock = new ReentrantReadWriteLock();
		}
	}
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	private final MGraph wrapped;

	/**
	 * Constructs a LocalbleMGraph for an MGraph.
	 *
	 * @param providedMGraph a non-lockable mgraph
	 */
	public LockableMGraphWrapper(final MGraph providedMGraph) {
		this.wrapped = providedMGraph;
	}

	@Override
	public ReadWriteLock getLock() {
		return lock;
	}

	@Override
	public Graph getGraph() {
		readLock.lock();
		try {
			return wrapped.getGraph();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Iterator<Triple> filter(NonLiteral subject, UriRef predicate, Resource object) {
		readLock.lock();
		try {
			return new LockingIterator(wrapped.filter(subject, predicate, object), lock);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public int size() {
		readLock.lock();
		try {
			return wrapped.size();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public boolean isEmpty() {
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
		readLock.lock();
		try {
			return wrapped.contains(o);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Iterator<Triple> iterator() {
		readLock.lock();
		try {
			return new LockingIterator(wrapped.iterator(), lock);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Object[] toArray() {
		readLock.lock();
		try {
			return wrapped.toArray();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public <T> T[] toArray(T[] a) {
		readLock.lock();
		try {
			return wrapped.toArray(a);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		readLock.lock();
		try {
			return wrapped.containsAll(c);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public boolean add(Triple e) {
		writeLock.lock();
		try {
			return wrapped.add(e);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean remove(Object o) {
		writeLock.lock();
		try {
			return wrapped.remove(o);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean addAll(Collection<? extends Triple> c) {
		writeLock.lock();
		try {
			return wrapped.addAll(c);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		writeLock.lock();
		try {
			return wrapped.removeAll(c);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		writeLock.lock();
		try {
			return wrapped.retainAll(c);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void clear() {
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
