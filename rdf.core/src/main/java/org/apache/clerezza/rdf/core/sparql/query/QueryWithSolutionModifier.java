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

import java.util.List;

/**
 * <p>This interface represents a SPARQL query which contains a specification
 * of solution modifiers: ORDER BY, OFFSET, and LIMIT.</p>
 *
 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#solutionModifiers">
 * SPARQL Query Language: 9 Solution Sequences and Modifiers</a>
 *
 * @author hasan
 */
public interface QueryWithSolutionModifier extends Query {

    /**
     * <p>Gets the list of required ordering conditions in decreasing ordering
     * priority.</p>
     * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modOrderBy">
     * SPARQL Query Language: 9.1 ORDER BY</a>
     * @return A list of {@link OrderCondition}s, in order of priority.
     */
    public List<OrderCondition> getOrderConditions();

    /**
     * <p>Gets the numeric offset of the first row to be returned by the query. 
     * The default offset is 0, meaning to start at the beginning.</p>
     * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modOffset">
     * SPARQL Query Language: 9.4 OFFSET</a>
     * @return The number of rows to skip in the result.
     */
    public int getOffset();

    /**
     * <p>Gets the maximum number of results to be returned by the query. 
     * A limit of -1 means no limit (return all results).
     * A limit of 0 means that no results should be returned.</p>
     * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modResultLimit">
     * SPARQL Query Language: 9.5 LIMIT</a>
     * @return The maximum number of rows to returned by the query.
     */
    public int getLimit();
}
