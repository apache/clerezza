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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.clerezza.rdf.core.event.GraphEvent;
import org.apache.clerezza.rdf.core.event.GraphListener;

/**
 *
 * @author reto
 */
class DelayedNotificator {

	private static Timer timer = new Timer("Event delivery timer",true);

	static class ListenerHolder {

		long delay;
		List<GraphEvent> events = null;
		GraphListener listener;

		public ListenerHolder(GraphListener listener, long delay) {
			this.listener = listener;
			this.delay = delay;
		}

		private ListenerHolder(long delay) {
			throw new UnsupportedOperationException("Not yet implemented");
		}

		private void registerEvent(GraphEvent event) {
			synchronized (this) {
				if (events == null) {
					events = new ArrayList<GraphEvent>();
					events.add(event);
					timer.schedule(new TimerTask() {

						@Override
						public void run() {
							List<GraphEvent> eventsLocal;
							synchronized (ListenerHolder.this) {
								eventsLocal = events;
								events = null;
							}
							listener.graphChanged(eventsLocal);
						}
					}, delay);
				} else {
					events.add(event);
				}
			}
		}
	}
	private Map<GraphListener, ListenerHolder> map = Collections.synchronizedMap(
			new HashMap<GraphListener, ListenerHolder>());

	void addDelayedListener(GraphListener listener, long delay) {
		map.put(listener, new ListenerHolder(listener, delay));
	}

	/**
	 * removes a Listener, this doesn't prevent the listener from receiving 
	 * events alreay scheduled.
	 *
	 * @param listener
	 */
	void removeDelayedListener(GraphListener listener) {
		map.remove(listener);
	}

	/**
	 * if the listener has not been registered as delayed listener te events is
	 * forwarded synchroneously
	 * @param event
	 */
	void sendEventToListener(GraphListener listener, GraphEvent event) {
		ListenerHolder holder = map.get(listener);
		if (holder == null) {
			listener.graphChanged(Collections.singletonList(event));
		} else {
			holder.registerEvent(event);
		}
	}
}
