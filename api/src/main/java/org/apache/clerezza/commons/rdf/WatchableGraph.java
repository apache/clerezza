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
package org.apache.clerezza.commons.rdf;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.clerezza.commons.rdf.event.FilterTriple;
import org.apache.clerezza.commons.rdf.event.GraphListener;


/**
 * An extension to the Graph interface that allows to add throws events
 * on modifications.
 *
 * @author reto
 */
public interface WatchableGraph extends Graph {
   

    /**
     * Adds the specified <code>GraphListener</code> to the graph. This listener
     * will be notified, when the graph is modified and the <code>Triple</code>
     * that was part of the modifiaction matched the specified
     * <code>FilterTriple</code>. The notification will be passed to the
     * listener after the specified delay time (in milli-seconds) has passed.
     * If more matching events occur during the delay period, then they are
     * passed all together at the end of the delay period. If the the listener
     * unregisters or the platform is stopped within the period then the already
     * occurred events may not be delivered.
     *
     * All implementations support this method, immutable implementations will
     * typically provide an empty implementation, they shall not throw an
     * exception.
     *
     * Implementation of which the triples change over time without add- and
     * remove-methods being called (e.g. implementation dynamically generating
     * their triples on invocation of the filer-method) may choose not to, or
     * only partially propagate their changes to the listener. They should
     * describe the behavior in the documentation of the class.
     *
     * Implementations should keep weak references the listeners, so that the
     * listener can be garbage collected if its no longer referenced by another
     * object.
     *
     * If delay is 0 notification will happen synchroneously.
     *
     * @param listener The listener that will be notified
     * @param filter The triple filter with which triples are tested,
     *        that were part of the modification.
     * @param delay The time period afer which the listener will be notified in milliseconds.
     */
    public void addGraphListener(GraphListener listener, FilterTriple filter,
            long delay);

    /**
     * Adds the specified <code>GraphListener</code> to the graph. This listener
     * will be notified, when the graph is modified and the <code>Triple</code>
     * that was part of the modifiaction matched the specified
     * <code>FilterTriple</code>. The notification will be passed without delay.
     *
     * Same as <code>addGraphListener(listener, filter, 0).
     *
     * @param listener The listener that will be notified
     * @param filter The triple filter with which triples are tested,
     *        that were part of the modification.
     */
    public void addGraphListener(GraphListener listener, FilterTriple filter);

    /**
     * Removes the specified <code>GraphListener</code> from the graph. This
     * listener will no longer be notified, when the graph is modified.
     *
     * @param listener The listener to be removed.
     */
    public void removeGraphListener(GraphListener listener);
  
}
