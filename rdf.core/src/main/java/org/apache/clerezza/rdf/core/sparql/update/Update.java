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
package org.apache.clerezza.rdf.core.sparql.update;

import java.util.Set;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcProvider;

/**
 * <p>This interface represents a SPARQL Update.</p>
 *
 * @author hasan
 */
public interface Update {

    /**
     * 
     * @param defaultGraph
     *      if default graph is referred either implicitly or explicitly in a SPARQL {@link Update}
     *      the specified defaultGraph should be returned in the resulting set.
     * @param tcProvider
     *      the specified tcProvider is used to get the named graphs referred in the SPARQL {@link Update}.
     * @return a set of graphs referred in the {@link Update}.
     */
    public Set<UriRef> getReferredGraphs(UriRef defaultGraph, TcProvider tcProvider);

    public void addOperation(UpdateOperation updateOperation);

    /**
     *
     * @return A valid String representation of the {@link Update}.
     */
    @Override
    public abstract String toString();
}
