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
import org.apache.clerezza.rdf.core.sparql.query.MinusGraphPattern;

/**
 * This class implements {@link MinusGraphPattern}.
 *
 * @author hasan
 */
public class SimpleMinusGraphPattern implements MinusGraphPattern {

    private GraphPattern minuendGraphPattern;
    private GroupGraphPattern subtrahendGraphPattern;

    /**
     * Constructs a {@link MinusGraphPattern} out of a {@link GraphPattern}
     * as the minuend graph pattern and a {@link GroupGraphPattern} as the 
     * subtrahend pattern.
     * 
     * @param minuendGraphPattern
     *        a {@link GraphPattern} specifying the minuend pattern.
     * @param subtrahendGraphPattern
     *        a {@link GroupGraphPattern} specifying the subtrahend pattern.
     */
    public SimpleMinusGraphPattern(GraphPattern minuendGraphPattern, GroupGraphPattern subtrahendGraphPattern) {
        if (subtrahendGraphPattern == null) {
            throw new IllegalArgumentException("Subtrahend graph pattern may not be null");
        }
        if (minuendGraphPattern == null) {
            this.minuendGraphPattern = new SimpleGroupGraphPattern();
        } else {
            this.minuendGraphPattern = minuendGraphPattern;
        }
        this.subtrahendGraphPattern = subtrahendGraphPattern;
    }

    @Override
    public GraphPattern getMinuendGraphPattern() {
        return minuendGraphPattern;
    }

    @Override
    public GroupGraphPattern getSubtrahendGraphPattern() {
        return subtrahendGraphPattern;
    }

}
