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
 * <p>This interface represents a SPARQL SELECT query.</p>
 *
 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#describe">
 * SPARQL Query Language: 10.4 DESCRIBE (Informative)</a>
 *
 * @author hasan
 */
public interface DescribeQuery extends QueryWithSolutionModifier {

    /**
     * <p>Tests if all variables in the query should be described.</p>
     * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#describe">
     * SPARQL Query Language: 10.4 DESCRIBE (Informative)</a>
     * @return <code>true</code> if the query should return all variables.
     */
    public boolean isDescribeAll();

    /**
     * <p>Gets the list of {@link ResourceOrVariable}s to describe.
     * If {@link #isDescribeAll()} returns <code>true</code> then
     * this list contains all the variables from the query.</p>
     * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#describe">
     * SPARQL Query Language: 10.4 DESCRIBE (Informative)</a>
     * @return A list of {@link ResourceOrVariable}s to describe.
     */
    public List<ResourceOrVariable> getResourcesToDescribe();
}
