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

import java.util.concurrent.locks.ReadWriteLock;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.access.LockableMGraph;


/**
*
* This is a wrapper object for <code>MGraph</code>. If <code>SecurityManger</code> 
* is not <code>null</code> <code>TcManager</code> checks the <code>TcPermission</code>. 
* If read-only permissions are set this wrapper is used instead of <code>MGraph</code>.
*
* @author tsuy
*/
public class WriteBlockedMGraph extends WriteBlockedTripleCollection 
        implements LockableMGraph {

    private LockableMGraph mGraph;
    /**
     * Creates a wrapper of <code>SimpleMGraph</code>
     */
    public WriteBlockedMGraph(LockableMGraph mGraph) {
        super(mGraph);
        this.mGraph = mGraph;
    }

    @Override
    public Graph getGraph() {
        return this.mGraph.getGraph();
    }

    @Override
    public ReadWriteLock getLock() {
        return mGraph.getLock();
    }
}

    