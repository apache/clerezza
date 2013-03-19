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
package org.apache.clerezza.rdf.core.access;

import java.security.AccessControlException;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.security.TcAccessController;
import org.apache.clerezza.rdf.core.impl.SimpleGraph;
import org.apache.clerezza.rdf.core.impl.WriteBlockedMGraph;

/**
 * A SecuredMGraph is a LockableMGraph that wraps a LockableMGraph checking each
 * access for the rights on a the graph for which the uri is passed to the 
 * constructor.
 *
 * @author mir
 */
public class SecuredMGraph extends SecuredTripleCollection implements LockableMGraph {

    private LockableMGraph wrapped;

    public SecuredMGraph(LockableMGraph wrapped, UriRef name,
            TcAccessController tcAccessController) {
        super(wrapped, name,  tcAccessController);
        this.wrapped = wrapped;
    }

    @Override
    public Graph getGraph() {
        return new SimpleGraph(this);
    }

    @Override
    public ReadWriteLock getLock() {
        return wrapped.getLock();
    }

    /**
     * Returns the wrapped LockableMGraph if the caller has all access rights.
     * If the caller has only the read access right, then a write-blocked
     * LockableMGraph is returned. If the caller has neither the read nor the write
     * access right then an AccessControlException is thrown.
     *
     * @return the wrapped LockableMGraph or a write-block LockableMGraph depending
     *        on the access rights of the caller.
     */
    public LockableMGraph getUnsecuredMGraph() {
        try {
            checkWrite();
            return wrapped;
        } catch (AccessControlException ex) {
            checkRead();
            return new WriteBlockedMGraph(wrapped);
        }
        
    }

}
