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
 * A set of triples (as it doesn't allow duplicates), it does however
 * not extend {@link java.util.Set} as it doesn't inherit its
 * specification for <code>hashCode()</code> and <code>equals</code>.
 * It is possible to add <code>GraphListener</code> to listen for modifications
 * in the triples.
 *
 * @author reto
 */
public interface Graph extends Collection<Triple> {
    
    /**
     * Filters triples given a pattern. 
     * filter(null, null, null) returns the same as iterator()
     * 
     * @param subject
     * @param predicate
     * @param object
     * @return <code>Iterator</code>
     */
    public Iterator<Triple> filter(BlankNodeOrIRI subject, IRI predicate, 
            RDFTerm object);

    /**
     * Returns true if <code>other</code> describes the same graph and will 
     * always describe the same graph as this instance, false otherwise. 
     * It returns true if this == other or if it
     * is otherwise guaranteed that changes to one of the instances are
     * immediately reflected in the other or if both graphs are immutable.
     *
     * @param other
     * @return true if other == this
     */
    @Override
    public boolean equals(Object other);

    /**
     * Returns an ImutableGraph describing the graph at the current point in 
     * time. if <code>this</code> is an instance of ImmutableGraph this can 
     * safely return <code>this</code>.
     *
     * @return the current time slice of the possibly mutable graph represented by the instance.
     */
    public ImmutableGraph getImmutableGraph();
    
    /**
     * The lock provided by this methods allows to create read- and write-locks
     * that span multiple method calls. Having a read locks prevents other
     * threads from writing to this Graph, having a write-lock prevents other
     * threads from reading and writing. Implementations would typically
     * return a <code>java.util.concurrent.locks.ReentrantReadWriteLock</code>.
     * Immutable instances (such as instances of <code>ImmutableGraph</code>)
     * or instances used in transaction where concurrent acces of the same 
     * instance is not an issue may return a no-op ReadWriteLock (i.e. one
     * which returned ReadLock and WriteLock instances of which the methods do 
     * not do anything)
     *
     * @return the lock of this Graph
     */
    ReadWriteLock getLock();
}
