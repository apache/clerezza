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

import org.apache.clerezza.rdf.core.sparql.query.GroupGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.ServiceGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.UriRefOrVariable;

/**
 *
 * @author hasan
 */
public class SimpleServiceGraphPattern implements ServiceGraphPattern {

    private UriRefOrVariable service;
    private GroupGraphPattern groupGraphPattern;
    private boolean silent;

    public SimpleServiceGraphPattern(UriRefOrVariable service,
            GroupGraphPattern groupGraphPattern) {
        if (service == null) {
            throw new IllegalArgumentException("Service endpoint may not be null");
        }
        if (groupGraphPattern == null) {
            throw new IllegalArgumentException("Group Graph Pattern may not be null");
        }
        this.service = service;
        this.groupGraphPattern = groupGraphPattern;
        this.silent = false;
    }

    @Override
    public UriRefOrVariable getService() {
        return service;
    }

    @Override
    public GroupGraphPattern getGroupGraphPattern() {
        return groupGraphPattern;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isSilent() {
        return silent;
    }

}
