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
package org.apache.clerezza.commons.rdf.impl.utils;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.clerezza.commons.rdf.event.GraphEvent;
import org.apache.clerezza.commons.rdf.event.GraphListener;


/**
 *
 * @author reto
 */
class DelayedNotificator {

    private static final Logger log = Logger.getLogger(DelayedNotificator.class.getName());
    private static Timer timer = new Timer("Event delivery timer",true);

    static class ListenerHolder {

        long delay;
        List<GraphEvent> events = null;
        WeakReference<GraphListener> listenerRef;

        public ListenerHolder(GraphListener listener, long delay) {
            this.listenerRef = new WeakReference<GraphListener>(listener);
            this.delay = delay;
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
                            GraphListener listener = listenerRef.get();
                            if (listener == null) {
                                log.fine("Ignoring garbage collected listener");
                            } else {
                                try {
                                    listener.graphChanged(eventsLocal);
                                } catch (Exception e) {
                                    log.log(Level.WARNING, "Exception delivering ImmutableGraph event", e);
                                }
                            }
                        }
                    }, delay);
                } else {
                    events.add(event);
                }
            }
        }
    }
    
    private final Map<GraphListener, ListenerHolder> map = Collections.synchronizedMap(
            new WeakHashMap<GraphListener, ListenerHolder>());

    void addDelayedListener(GraphListener listener, long delay) {
        map.put(listener, new ListenerHolder(listener, delay));
    }

    /**
     * removes a Listener, this doesn't prevent the listenerRef from receiving
     * events alreay scheduled.
     *
     * @param listenerRef
     */
    void removeDelayedListener(GraphListener listener) {
        map.remove(listener);
    }

    /**
     * if the listenerRef has not been registered as delayed listenerRef te events is
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
