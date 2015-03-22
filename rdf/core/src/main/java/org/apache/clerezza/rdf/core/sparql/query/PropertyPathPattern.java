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

/**
 * Defines a property path pattern consisting of a subject, a property path expression, and an object.
 * The subject and object are of type {@link ResourceOrVariable}, whereas
 * the predicate is of type {@link PropertyPathExpressionOrVariable}.
 * @see <a href="http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#sparqlPropertyPaths">
 * SPARQL 1.1 Query Language: 18.1.7 Property Path Patterns</a>
 *
 * @author hasan
 */
public interface PropertyPathPattern {

    /**
     * @return the subject
     */
    public ResourceOrVariable getSubject();

    /**
     * @return the property path expression
     */
    public PropertyPathExpressionOrVariable getPropertyPathExpression();

    /**
     * @return the object
     */
    public ResourceOrVariable getObject();
}
