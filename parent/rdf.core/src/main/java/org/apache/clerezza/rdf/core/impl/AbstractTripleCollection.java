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

import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import java.util.Set;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.event.AddEvent;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphEvent;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.core.event.RemoveEvent;

/**
 * An abstract implementation of <code>TripleCollection</code> implementing
 * <code>iterator</code> and <code>contains</code> calling <code>filter</code>.
 *
 * @author reto
 */
public abstract class AbstractTripleCollection extends AbstractCollection<Triple>
		implements TripleCollection {

	//all listeners
	private final Set<ListenerConfiguration> listenerConfigs = Collections.synchronizedSet(
			new HashSet<ListenerConfiguration>());
	private DelayedNotificator delayedNotificator = new DelayedNotificator();

	@Override
	public Iterator<Triple> iterator() {
		return filter(null, null, null);
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Triple)) {
			return false;
		}
		Triple t = (Triple) o;
		return filter(t.getSubject(), t.getPredicate(), t.getObject()).hasNext();
	}

	@Override
	public Iterator<Triple> filter(NonLiteral subject, UriRef predicate,
			Resource object) {
		final Iterator<Triple> baseIter = performFilter(subject, predicate, object);
		return new Iterator<Triple>() {

			Triple currentTriple = null;

			@Override
			public boolean hasNext() {
				return baseIter.hasNext();
			}

			@Override
			public Triple next() {
				currentTriple = baseIter.next();
				return currentTriple;
			}

			@Override
			public void remove() {
				baseIter.remove();
				dispatchEvent(new RemoveEvent(AbstractTripleCollection.this, currentTriple));
			}
		};
	}

	/**
	 * A subclass of <code>AbstractTripleCollection</code> should override 
	 * this method instead of <code>filter</code> for graph event support to be
	 * added. The Iterator returned by <code>filter</code> will dispatch a
	 * GraphEvent after invoking the remove method of the iterator returned by
	 * this method.
	 * 
	 * @param subject
	 * @param predicate
	 * @param object
	 * @return
	 */
	protected abstract Iterator<Triple> performFilter(NonLiteral subject, UriRef predicate,
			Resource object);

	@Override
	public boolean add(Triple triple) {
		boolean success = performAdd(triple);
		if (success) {
			dispatchEvent(new AddEvent(this, triple));
		}
		return success;
	}

	/**
	 * A subclass of <code>AbstractTripleCollection</code> should override 
	 * this method instead of <code>add</code> for graph event support to be
	 * added.
	 * 
	 * @param e The triple to be added to the triple collection
	 * @return
	 */
	protected boolean performAdd(Triple e) {
		return super.add(e);
	}

	@Override
	public boolean remove(Object o) {
		Triple triple = (Triple) o;
		boolean success = performRemove(triple);
		if (success) {
			dispatchEvent(new RemoveEvent(this, triple));
		}
		return success;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		for (Iterator<? extends Object> it = c.iterator(); it.hasNext();) {
			Object object = it.next();
			if (remove(object)) {
				modified = true;
			}
		}
		return modified;
	}

	/**
	 * A subclass of <code>AbstractTripleCollection</code> should override 
	 * this method instead of <code>remove</code> for graph event support to be
	 * added.
	 * 
	 * @param o The triple to be removed from the triple collection
	 * @return
	 */
	protected boolean performRemove(Triple triple) {
		Iterator<Triple> e = performFilter(null, null, null);
		while (e.hasNext()) {
			if (triple.equals(e.next())) {
				e.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Dispatches a <code>GraphEvent</code> to all registered listeners for which
	 * the specified <code>Triple</code> matches the <code>FilterTriple</code>s
	 * of the listeners.
	 * 
	 * @param triple The Triple that was modified
	 * @param type The type of modification
	 */
	protected void dispatchEvent(GraphEvent event) {
		synchronized(listenerConfigs) {
			Iterator<ListenerConfiguration> iter = listenerConfigs.iterator();
			while (iter.hasNext()) {
				ListenerConfiguration config = iter.next();
				GraphListener registeredListener = config.getListener();
				if (registeredListener == null) {
					iter.remove();
					continue;
				}
				if (config.getFilter().match(event.getTriple())) {
					delayedNotificator.sendEventToListener(registeredListener, event);
				}
			}
		}
	}

	@Override
	public void addGraphListener(GraphListener listener, FilterTriple filter) {
		addGraphListener(listener, filter, 0);
	}

	@Override
	public void addGraphListener(GraphListener listener, FilterTriple filter,
			long delay) {
		listenerConfigs.add(new ListenerConfiguration(listener, filter));
		if (delay > 0) {
			delayedNotificator.addDelayedListener(listener, delay);
		}
	}

	@Override
	public void removeGraphListener(GraphListener listener) {
		synchronized(listenerConfigs) {
			Iterator<ListenerConfiguration> iter = listenerConfigs.iterator();
			while (iter.hasNext()) {
				ListenerConfiguration listenerConfig = iter.next();
				GraphListener registeredListener = listenerConfig.getListener();
				if ((registeredListener == null) || (registeredListener.equals(listener))) {
					iter.remove();
				}
			}
		}
		delayedNotificator.removeDelayedListener(listener);
	}

	private static class ListenerConfiguration {

		private WeakReference<GraphListener> listenerRef;
		private FilterTriple filter;

		private ListenerConfiguration(GraphListener listener, FilterTriple filter) {
			this.listenerRef = new WeakReference<GraphListener>(listener);
			this.filter = filter;
		}

		/**
		 * @return the listener
		 */
		GraphListener getListener() {
			GraphListener listener = listenerRef.get();
			return listener;
		}

		/**
		 * @return the filter
		 */
		FilterTriple getFilter() {
			return filter;
		}
	}
}
