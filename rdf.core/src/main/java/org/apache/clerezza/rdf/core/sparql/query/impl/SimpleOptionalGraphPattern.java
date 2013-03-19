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

import org.apache.clerezza.rdf.core.sparql.query.GraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.GroupGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.OptionalGraphPattern;

/**
 * This class implements {@link OptionalGraphPattern}.
 *
 * @author hasan
 */
public class SimpleOptionalGraphPattern implements OptionalGraphPattern {

    private GraphPattern mainGraphPattern;
    private GroupGraphPattern optionalGraphPattern;

    /**
     * Constructs an {@link OptionalGraphPattern} out of a {@link GraphPattern}
     * as the main graph pattern and a {@link GroupGraphPattern} as the 
     * optional pattern.
     * 
     * @param mainGraphPattern
     *        a {@link GraphPattern} specifying the main pattern.
     * @param optionalGraphPattern
     *        a {@link GroupGraphPattern} specifying the optional pattern.
     */
    public SimpleOptionalGraphPattern(GraphPattern mainGraphPattern,
            GroupGraphPattern optionalGraphPattern) {
        if (optionalGraphPattern == null) {
            throw new IllegalArgumentException("Optional graph pattern may not be null");
        }
        if (mainGraphPattern == null) {
            this.mainGraphPattern = new SimpleGroupGraphPattern();
        } else {
            this.mainGraphPattern = mainGraphPattern;
        }
        this.optionalGraphPattern = optionalGraphPattern;
    }

    @Override
    public GraphPattern getMainGraphPattern() {
        return mainGraphPattern;
    }

    @Override
    public GroupGraphPattern getOptionalGraphPattern() {
        return optionalGraphPattern;
    }

}
