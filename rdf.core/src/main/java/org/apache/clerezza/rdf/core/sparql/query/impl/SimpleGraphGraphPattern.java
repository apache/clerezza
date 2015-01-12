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
package org.apache.clerezza.rdf.core.sparql.query.impl;

import org.apache.clerezza.rdf.core.sparql.query.GraphGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.GroupGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.UriRefOrVariable;

/**
 *
 * @author hasan
 */
public class SimpleGraphGraphPattern implements GraphGraphPattern {

    private UriRefOrVariable ImmutableGraph;
    private GroupGraphPattern groupGraphPattern;

    public SimpleGraphGraphPattern(UriRefOrVariable ImmutableGraph,
            GroupGraphPattern groupGraphPattern) {
        if (ImmutableGraph == null) {
            throw new IllegalArgumentException("ImmutableGraph may not be null");
        }
        if (groupGraphPattern == null) {
            throw new IllegalArgumentException("Group ImmutableGraph Pattern may not be null");
        }
        this.ImmutableGraph = ImmutableGraph;
        this.groupGraphPattern = groupGraphPattern;
    }

    @Override
    public UriRefOrVariable getGraph() {
        return ImmutableGraph;
    }

    @Override
    public GroupGraphPattern getGroupGraphPattern() {
        return groupGraphPattern;
    }
}
