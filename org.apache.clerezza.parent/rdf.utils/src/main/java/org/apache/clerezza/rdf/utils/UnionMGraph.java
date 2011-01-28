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
package org.apache.clerezza.rdf.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.core.impl.AbstractMGraph;

/**
 * 
 * This class represents the union of multiple triple collections. A 
 * UnionGraph appears like a merge of the different graphs (see. 
 * http://www.w3.org/TR/rdf-mt/#graphdefs).
 * 
 * @author hasan
 */
public class UnionMGraph extends AbstractMGraph implements LockableMGraph {

	private TripleCollection[] baseTripleCollections;
	private Lock readLock;
	private Lock writeLock;

	/**
	 * Constructs a UnionMGraph over the specified baseTripleCollections. Write
	 * and delete operations are forwarded to the first baseTripleCollections. 
	 * 
	 * @param baseTripleCollections the baseTripleCollections
	 */
	public UnionMGraph(TripleCollection... baseTripleCollections) {
		this.baseTripleCollections = baseTripleCollections;
		readLock = getPartialReadLock(0);
		writeLock = createWriteLock();
	}

	@Override
	public int size() {
		int size = 0;
		for (TripleCollection tripleCollection : baseTripleCollections) {
			size += tripleCollection.size();
		}
		return size;
	}

	@Override
	public Iterator<Triple> performFilter(final NonLiteral subject,
			final UriRef predicate, final Resource object) {
		if (baseTripleCollections.length == 0) {
			return new HashSet<Triple>(0).iterator();
		}
		return new Iterator<Triple>() {

			int currentBaseTC = 0;
			Iterator<Triple> currentBaseIter = baseTripleCollections[0].filter(
					subject, predicate, object);
			private Triple lastReturned;

			@Override
			public boolean hasNext() {
				if (currentBaseIter.hasNext()) {
					return true;
				}
				if (currentBaseTC == baseTripleCollections.length - 1) {
					return false;
				}
				currentBaseTC++;
				currentBaseIter = baseTripleCollections[currentBaseTC].filter(
						subject, predicate, object);
				return hasNext();
			}

			@Override
			public Triple next() {
				lastReturned = hasNext() ? currentBaseIter.next() : null;
				return lastReturned;
			}

			@Override
			public void remove() {
				if (lastReturned == null) {
					throw new IllegalStateException();
				}
				if (currentBaseTC == 0) {
					currentBaseIter.remove();
				}
				lastReturned = null;
			}
		};
	}

	@Override
	public boolean add(Triple e) {
		if (baseTripleCollections.length == 0) {
			throw new RuntimeException("no base graph for adding triples");
		}
		return baseTripleCollections[0].add(e);
	}

	@Override
	public boolean remove(Object e) {
		if (baseTripleCollections.length == 0) {
			throw new RuntimeException("no base graph for removing triples");
		}
		return baseTripleCollections[0].remove(e);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj.getClass().equals(getClass()))) {
			return false;
		}
		UnionMGraph other = (UnionMGraph) obj;
		Set<TripleCollection> otherGraphs =
				new HashSet(Arrays.asList(other.baseTripleCollections));
		Set<TripleCollection> thisGraphs =
				new HashSet(Arrays.asList(baseTripleCollections));
		return thisGraphs.equals(otherGraphs)
				&& baseTripleCollections[0].equals(other.baseTripleCollections[0]);
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (TripleCollection graph : baseTripleCollections) {
			hash += graph.hashCode();
		}
		hash *= baseTripleCollections[0].hashCode();
		return hash;
	}

	@Override
	public void addGraphListener(GraphListener listener, FilterTriple filter) {
		for (TripleCollection tripleCollection : baseTripleCollections) {
			tripleCollection.addGraphListener(listener, filter);
		}
	}

	@Override
	public void addGraphListener(GraphListener listener, FilterTriple filter, long delay) {
		for (TripleCollection tripleCollection : baseTripleCollections) {
			tripleCollection.addGraphListener(listener, filter, delay);
		}
	}

	@Override
	public void removeGraphListener(GraphListener listener) {
		for (TripleCollection tripleCollection : baseTripleCollections) {
			tripleCollection.removeGraphListener(listener);
		}
	}

	@Override
	public ReadWriteLock getLock() {
		return readWriteLock;
	}
	private ReadWriteLock readWriteLock = new ReadWriteLock() {

		@Override
		public Lock readLock() {
			return readLock;
		}

		@Override
		public Lock writeLock() {
			return writeLock;
		}
	};


	private Lock getPartialReadLock(int startPos) {
		ArrayList<Lock> resultList = new ArrayList<Lock>();
		for (int i = startPos; i < baseTripleCollections.length; i++) {
			TripleCollection tripleCollection = baseTripleCollections[i];
			if (tripleCollection instanceof LockableMGraph) {
				final Lock lock = ((LockableMGraph) tripleCollection).getLock().readLock();
				resultList.add(lock);
			}
		}
		return new UnionLock(resultList.toArray(new Lock[resultList.size()]));
	}

	
	private Lock createWriteLock() {
		Lock partialReadLock =  getPartialReadLock(1);
		if (baseTripleCollections[0] instanceof LockableMGraph) {
			Lock baseWriteLock =
					((LockableMGraph)baseTripleCollections[0]).getLock().writeLock();
			return new UnionLock(baseWriteLock, partialReadLock);

		} else {
			return partialReadLock;
		}
	};


	private static class UnionLock implements Lock {

		Lock[] locks;
		public UnionLock(Lock... locks) {
			this.locks = locks;
		}


		@Override
		public void lock() {
			for(Lock lock : locks) {
				lock.lock();
			}
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			Set<Lock> aquiredLocks = new HashSet<Lock>();
			try {
				for(Lock lock : locks) {
					lock.lockInterruptibly();
					aquiredLocks.add(lock);
				}
			} catch (InterruptedException e) {
				for (Lock lock : aquiredLocks) {
					lock.unlock();
				}
				throw e;
			}
		}

		@Override
		public boolean tryLock() {
			Set<Lock> aquiredLocks = new HashSet<Lock>();
			for(Lock lock : locks) {
				if (!lock.tryLock()) {
					for (Lock aquiredLock : aquiredLocks) {
						aquiredLock.unlock();
					}
					return false;
				}
				aquiredLocks.add(lock);
			}
			return true;
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			Set<Lock> aquiredLocks = new HashSet<Lock>();
			long timeInNanos = unit.convert(time, TimeUnit.NANOSECONDS);
			long startTime = System.nanoTime();
			try {
				for(Lock lock : locks) {
					if (!lock.tryLock((timeInNanos+startTime)-System.nanoTime(),
							TimeUnit.NANOSECONDS)) {
						for (Lock aquiredLock : aquiredLocks) {
							aquiredLock.unlock();
						}
						return false;
					}
					aquiredLocks.add(lock);
				}
			} catch (InterruptedException e) {
				for (Lock lock : aquiredLocks) {
					lock.unlock();
				}
				throw e;
			}
			return true;
		}

		@Override
		public void unlock() {
			for(Lock lock : locks) {
				lock.unlock();
			}
		}

		@Override
		public Condition newCondition() {
			throw new UnsupportedOperationException("Conditions not supported.");
		}
	}
}
