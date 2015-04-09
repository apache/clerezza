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

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.security.TcAccessController;

/**
 * @see <a href="http://www.osgi.org/javadoc/r4v41/org/osgi/framework/ServiceFactory.html">
 * Interface ServiceFactory</a>
 *
 * @author mir
 */
public class ImmutableGraphServiceFactory implements ServiceFactory {
    
    private final TcManager tcManager;
    private final Iri name;
    private final TcAccessController tcAccessController;

    ImmutableGraphServiceFactory(TcManager tcManager, Iri name,
            TcAccessController tcAccessController) {
        this.tcManager = tcManager;
        this.name = name;
        this.tcAccessController = tcAccessController;
    }

    @Override
    public Object getService(Bundle arg0, ServiceRegistration arg1) {
        Graph tc = 
                new SecuredGraph(tcManager.getImmutableGraph(name), name,
                tcAccessController);
        return tc.getImmutableGraph();
    }

    @Override
    public void ungetService(Bundle arg0, ServiceRegistration arg1, Object arg2) {
    }
}
