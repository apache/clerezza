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
package org.apache.clerezza.rdf.core.sparql.update.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.sparql.update.Update;
import org.apache.clerezza.rdf.core.sparql.update.UpdateOperation;

/**
 *
 * @author hasan
 */
public class SimpleUpdate implements Update {
    protected List<UpdateOperation> operations = new ArrayList<UpdateOperation>();

    @Override
    public Set<UriRef> getReferredGraphs(UriRef defaultGraph, TcProvider tcProvider) {
        Set<UriRef> referredGraphs = new HashSet<UriRef>();
        for (UpdateOperation operation : operations) {
            referredGraphs.addAll(operation.getInputGraphs(defaultGraph, tcProvider));
            referredGraphs.addAll(operation.getDestinationGraphs(defaultGraph, tcProvider));
        }
        return referredGraphs;
    }

    @Override
    public void addOperation(UpdateOperation updateOperation) {
        operations.add(updateOperation);
    }
}
