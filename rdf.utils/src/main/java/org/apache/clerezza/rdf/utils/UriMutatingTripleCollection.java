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

import java.util.Collection;
import java.util.Iterator;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.core.impl.TripleImpl;

/**
 * This wrapps a Triplecollection changing a prefix for the UriRefs contained
 * in subject or object position.
 *
 * Currently it only supports read opearations.
 *
 * @author reto
 */
public class UriMutatingTripleCollection implements TripleCollection {

	private final TripleCollection base;
	private final String sourcePrefix;
	private final String targetPrefix;
	private final int sourcePrefixLength;
	private final int targetPrefixLength;

	public UriMutatingTripleCollection(TripleCollection base, String sourcePrefix,
			String targetPrefix) {
		this.base = base;
		this.sourcePrefix = sourcePrefix;
		sourcePrefixLength= sourcePrefix.length();
		this.targetPrefix = targetPrefix;
		targetPrefixLength= targetPrefix.length();
	}

	private <R extends Resource> R toTargetResource(final R sourceResource) {
		if (sourceResource instanceof UriRef) {
			final UriRef sourceUriRef = (UriRef) sourceResource;
			if (sourceUriRef.getUnicodeString().startsWith(sourcePrefix)) {
				final String uriRest = sourceUriRef.getUnicodeString()
						.substring(sourcePrefixLength);
				return (R) new UriRef(targetPrefix+uriRest);
			}
		}
		return sourceResource;			
	}

	private Triple toTargetTriple(Triple triple) {
		if (triple == null) {
			return null;
		}
		return new TripleImpl(toTargetResource(triple.getSubject()),
				triple.getPredicate(), toTargetResource(triple.getObject()));
	}

	private <R extends Resource> R toSourceResource(final R targetResource) {
		if (targetResource instanceof UriRef) {
			final UriRef sourceUriRef = (UriRef) targetResource;
			if (sourceUriRef.getUnicodeString().startsWith(targetPrefix)) {
				final String uriRest = sourceUriRef.getUnicodeString()
						.substring(targetPrefixLength);
				return (R) new UriRef(sourcePrefix+uriRest);
			}
		}
		return targetResource;
	}

	private Triple toSourceTriple(Triple triple) {
		if (triple == null) {
			return null;
		}
		return new TripleImpl(toSourceResource(triple.getSubject()),
				triple.getPredicate(), toSourceResource(triple.getObject()));
	}

	@Override
	public Iterator<Triple> filter(NonLiteral subject, UriRef predicate, Resource object) {
		final Iterator<Triple> baseIter = base.filter(toSourceResource(subject),
				predicate, toSourceResource(object));
		return new WrappedIteraror(baseIter);


	}

	@Override
	public void addGraphListener(GraphListener listener, FilterTriple filter, long delay) {
	}

	@Override
	public void addGraphListener(GraphListener listener, FilterTriple filter) {
	}

	@Override
	public void removeGraphListener(GraphListener listener) {
	}

	@Override
	public int size() {
		return base.size();
	}

	@Override
	public boolean isEmpty() {
		return base.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return base.contains(toSourceTriple((Triple)o));
	}

	@Override
	public Iterator<Triple> iterator() {
		return filter(null, null, null);
	}

	@Override
	public Object[] toArray() {
		Object[] result = base.toArray();
		for (int i = 0; i < result.length; i++) {
			Triple triple = (Triple) result[i];
			result[i] = toTargetTriple(triple);
		}
		return result;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		T[] result = base.toArray(a);
		for (int i = 0; i < result.length; i++) {
			Triple triple = (Triple) result[i];
			result[i] = (T) toTargetTriple(triple);
		}
		return result;
	}

	@Override
	public boolean add(Triple e) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object object : c) {
			if (!contains(object)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends Triple> c) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Not supported.");
	}

	class WrappedIteraror implements Iterator<Triple>{
		private final Iterator<Triple> baseIter;

		private WrappedIteraror(Iterator<Triple> baseIter) {
			this.baseIter = baseIter;
		}

		@Override
		public boolean hasNext() {
			return baseIter.hasNext();
		}

		@Override
		public Triple next() {
			return toTargetTriple(baseIter.next());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

	}
}
