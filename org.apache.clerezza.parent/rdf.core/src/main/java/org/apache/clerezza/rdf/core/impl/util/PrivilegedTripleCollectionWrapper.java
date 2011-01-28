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
package org.apache.clerezza.rdf.core.impl.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Iterator;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;

/**
 * Calls the methods of the wrapped <code>TripleCollection</code> as privileged
 * code, because they may need permissions like writing to disk or accessing       
 * network.
 *
 * @author mir
 */
public class PrivilegedTripleCollectionWrapper implements TripleCollection {

	private TripleCollection tripleCollection;

	public PrivilegedTripleCollectionWrapper(TripleCollection tripleCollection) {
		this.tripleCollection = tripleCollection;
	}

	@Override
	public Iterator<Triple> filter(final NonLiteral subject, final UriRef predicate,
			final Resource object) {
		return AccessController.doPrivileged(new PrivilegedAction<Iterator<Triple>>() {

			@Override
			public Iterator<Triple> run() {
				return tripleCollection.filter(subject, predicate, object);
			}
		});
	}

	@Override
	public int size() {
		return AccessController.doPrivileged(new PrivilegedAction<Integer>() {

			@Override
			public Integer run() {
				return tripleCollection.size();
			}
		});
	}

	@Override
	public boolean isEmpty() {
		return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return tripleCollection.isEmpty();
			}
		});
	}

	@Override
	public boolean contains(final Object o) {
		return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return tripleCollection.contains(o);
			}
		});
	}

	@Override
	public Iterator<Triple> iterator() {
		return AccessController.doPrivileged(new PrivilegedAction<Iterator<Triple>>() {

			@Override
			public Iterator<Triple> run() {
				return tripleCollection.iterator();
			}
		});
	}

	@Override
	public Object[] toArray() {
		return AccessController.doPrivileged(new PrivilegedAction<Object[]>() {

			@Override
			public Object[] run() {
				return tripleCollection.toArray();
			}
		});
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return AccessController.doPrivileged(new PrivilegedAction<T[]>() {

			@Override
			public T[] run() {
				return tripleCollection.toArray(a);
			}
		});
	}

	@Override
	public boolean add(final Triple triple) {
		return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return tripleCollection.add(triple);
			}
		});
	}

	@Override
	public boolean remove(final Object o) {
		return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return tripleCollection.remove(o);
			}
		});
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return tripleCollection.containsAll(c);
			}
		});
	}

	@Override
	public boolean addAll(final Collection<? extends Triple> c) {
		return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return tripleCollection.addAll(c);
			}
		});
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return tripleCollection.removeAll(c);
			}
		});
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

			@Override
			public Boolean run() {
				return tripleCollection.retainAll(c);
			}
		});
	}

	@Override
	public void clear() {
		AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Object run() {
				tripleCollection.clear();
				return null;
			}
		});
	}

	@Override
	public void addGraphListener(GraphListener listener, FilterTriple filter, long delay) {
		tripleCollection.addGraphListener(listener, filter, delay);
	}

	@Override
	public void addGraphListener(GraphListener listener, FilterTriple filter) {
		tripleCollection.addGraphListener(listener, filter);
	}

	@Override
	public void removeGraphListener(GraphListener listener) {
		tripleCollection.removeGraphListener(listener);
	}

	private static class PriviledgedTripleIterator implements Iterator<Triple> {

		private final Iterator<Triple> wrappedIterator;

		public PriviledgedTripleIterator(Iterator<Triple> wrappedIterator) {
			this.wrappedIterator = wrappedIterator;
		}

		@Override
		public boolean hasNext() {
			return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

				@Override
				public Boolean run() {
					return wrappedIterator.hasNext();
				}
			});
		}

		@Override
		public Triple next() {
			return AccessController.doPrivileged(new PrivilegedAction<Triple>() {

				@Override
				public Triple run() {
					return wrappedIterator.next();
				}
			});
		}

		@Override
		public void remove() {
			AccessController.doPrivileged(new PrivilegedAction<Object>() {

				@Override
				public Object run() {
					wrappedIterator.remove();
					return null;
				}
			});
		}
	}
}
