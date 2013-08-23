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
package org.apache.clerezza.rdf.core.sparql.query;

import java.util.Set;

/**
 * Defines a basic graph pattern that supports property path expressions.
 * A {@link PathSupportedBasicGraphPattern} is a set of {@link PropertyPathPattern}s.
 * A {@link PropertyPathPattern} is a generalization of a {@link TriplePattern} to include
 * a {@link PropertyPathExpression} in the property position.
 * Therefore, a {@link PathSupportedBasicGraphPattern} can be seen as a generalization of a {@link BasicGraphPattern}
 * @see <a href="http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#sparqlBasicGraphPatterns">
 * SPARQL 1.1 Query Language: 18.1.6 Basic Graph Patterns</a>
 * and <a href="http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#sparqlPropertyPaths">
 * SPARQL 1.1 Query Language: 18.1.7 Property Path Patterns</a>
 *
 * @author hasan
 */
public interface PathSupportedBasicGraphPattern extends GraphPattern {

    /**
     *
     * @return a set of all property path patterns to match.
     */
    public Set<PropertyPathPattern> getPropertyPathPatterns();
}
