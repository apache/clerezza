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
package org.apache.clerezza.commons.rdf.event;

import java.util.List;

/**
 * A class that is interested in graph events implements this interface and
 * is then added as listener to a <code>ListenableTripleCollection</code> or
 * one of its subclasses. When the <code>ListenableTripleCollection</code> is
 * modified, then the <code>GraphListener</code> is notified.
 *
 * @author mir
 */
public interface GraphListener {

    /**
     * This method is called when a <code>ListenableTripleCollection</code> was
     * modified, to which this <code>GraphListener</code> was added. A
     * <code>List</code> containing <code>GraphEvent</code>s are passed as
     * argument. The list contains all events in which a triple was part of
     * the modification that matched the <code>FilterTriple</code> which was passed
     * as argument when the listener was added.
     * @param events
     */
    public void graphChanged(List<GraphEvent> events);
}
