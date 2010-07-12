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

package org.apache.clerezza.utils;

import java.util.Collection;
import java.util.Iterator;

/**
 * Flattens an Iterator of Iterators to an Iterator
 * 
 * @author reto
 */
public class IteratorMerger<T> implements Iterator<T> {

	protected Iterator<Iterator<T>> baseIterators;
	protected Iterator<T> current;

	/**
	 * constructs an iterator that will return the elements of the baseIterators
	 * 
	 * @param baseIterators
	 */
	public IteratorMerger(Iterator<Iterator<T>> baseIterators) {
		init(baseIterators);
	}

	/**
	 * Constructs an iterator that iterates over all elements of the collections
	 * contained in the collection.
	 * 
	 * @param collectionOfCollections
	 */
	public IteratorMerger(Collection<Collection<T>> collectionOfCollections) {
		final Iterator<Collection<T>> setIter = collectionOfCollections.iterator();
		init(new Iterator<Iterator<T>>() {

			@Override
			public boolean hasNext() {
				return setIter.hasNext();
			}

			@Override
			public Iterator<T> next() {
				return setIter.next().iterator();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});
	}

	private void init(Iterator<Iterator<T>> baseIterators) {
		this.baseIterators = baseIterators;
		current = baseIterators.next();
	}

	private void updateCurrentIfNeeded() {
		while (!current.hasNext()) {
			if (baseIterators.hasNext()) {
				current = baseIterators.next();
			} else {
				return;
			}
		}
	}

	@Override
	public boolean hasNext() {
		updateCurrentIfNeeded();
		return current.hasNext();
	}

	@Override
	public T next() {
		updateCurrentIfNeeded();
		return current.next();
	}

	@Override
	public void remove() {
		current.remove();
	}

}
