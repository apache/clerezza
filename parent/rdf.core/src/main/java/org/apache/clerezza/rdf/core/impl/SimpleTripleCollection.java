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
package org.apache.clerezza.rdf.core.impl;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;

/**
 * For now this is a minimalistic implementation, without any indexes or other
 * optimizations.
 *
 * This class is not public, implementations should use {@link SimpleGraph} or
 * {@link SimpleMGraph}.
 *
 * @author reto
 */
class SimpleTripleCollection extends AbstractTripleCollection {

	final Set<Triple> triples;

	private boolean checkConcurrency = false;

	class SimpleIterator implements Iterator<Triple> {

		private Iterator<Triple> listIter;
		private boolean isValid = true;

		public SimpleIterator(Iterator<Triple> listIter) {
			this.listIter = listIter;
		}
		private Triple currentNext;

		@Override
		public boolean hasNext() {
			checkValidity();
			return listIter.hasNext();
		}

		@Override
		public Triple next() {
			checkValidity();
			currentNext = listIter.next();
			return currentNext;
		}		

		@Override
		public void remove() {
			checkValidity();
			listIter.remove();
			triples.remove(currentNext);			
			invalidateIterators(this);			
		}

		private void checkValidity() throws ConcurrentModificationException {
			if (checkConcurrency && !isValid) {
				throw new ConcurrentModificationException();
			}
		}

		private void invalidate() {
			isValid = false;
		}
	}	
	
	private final Set<SoftReference<SimpleIterator>> iterators =
			Collections.synchronizedSet(new HashSet<SoftReference<SimpleIterator>>());
	
	/**
	 * Creates an empty SimpleTripleCollection
	 */
	public SimpleTripleCollection() {
		triples = Collections.synchronizedSet(new HashSet<Triple>());
	}

	/**
	 * Creates a SimpleTripleCollection using the passed iterator, the iterator 
	 * is consumed before the constructor returns
	 * 
	 * @param iterator
	 */
	public SimpleTripleCollection(Iterator<Triple> iterator) {
		triples = new HashSet<Triple>();
		while (iterator.hasNext()) {
			Triple triple = iterator.next();
			triples.add(triple);
		}
	}

	/**
	 * Creates a SimpleTripleCollection for the specified set of triples, 
	 * subsequent modification of baseSet do affect the created instance.
	 * 
	 * @param baseSet
	 */
	public SimpleTripleCollection(Set<Triple> baseSet) {
		this.triples = baseSet;
	}

	/**
	 * Creates a SimpleTripleCollection for the specified collection of triples,
	 * subsequent modification of baseSet do not affect the created instance.
	 *
	 * @param baseSet
	 */
	public SimpleTripleCollection(Collection<Triple> baseCollection) {
		this.triples = new HashSet<Triple>(baseCollection);
	}

	@Override
	public int size() {
		return triples.size();
	}

	@Override
	public Iterator<Triple> performFilter(final NonLiteral subject, final UriRef predicate, final Resource object) {
		final List<Triple> tripleList = new ArrayList<Triple>();
		synchronized (triples) {
			Iterator<Triple> baseIter = triples.iterator();
			while (baseIter.hasNext()) {
				Triple triple = baseIter.next();
				if ((subject != null)
						&& (!triple.getSubject().equals(subject))) {
					continue;
				}
				if ((predicate != null)
						&& (!triple.getPredicate().equals(predicate))) {
					continue;
				}
				if ((object != null)
						&& (!triple.getObject().equals(object))) {
					continue;
				}
				tripleList.add(triple);
			}

			final Iterator<Triple> listIter = tripleList.iterator();
			SimpleIterator resultIter = new SimpleIterator(listIter);
			if (checkConcurrency) {
				iterators.add(new SoftReference<SimpleIterator>(resultIter));
			}
			return resultIter;
		}
	}


	@Override
	public boolean performAdd(Triple e) {
		boolean modified = triples.add(e);
		if (modified) {
			invalidateIterators(null);
		}
		return modified;
	}
	
	private void invalidateIterators(SimpleIterator caller) {
		if (!checkConcurrency) {
			return;
		}
		Set<SoftReference> oldReferences = new HashSet<SoftReference>();
		synchronized(iterators) {
			for (SoftReference<SimpleTripleCollection.SimpleIterator> softReference : iterators) {
				SimpleIterator simpleIterator = softReference.get();
				if (simpleIterator == null) {
					oldReferences.add(softReference);
					continue;
				}
				if (simpleIterator != caller) {
					simpleIterator.invalidate();
				}
			}
		}
		iterators.removeAll(oldReferences);
	}

	/**
	 * Specifies whether or not to throw <code>ConcurrentModificationException</code>s,
	 * if this simple triple collection is modified concurrently. Concurrency
	 * check is set to false by default.
	 *
	 * @param bool Specifies whether or not to check concurrent modifications.
	 */
	public void setCheckConcurrency(boolean bool) {
		checkConcurrency = bool;
	}
}
