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
 * <p>This interface represents a SPARQL CONSTRUCT query.</p>
 *
 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#construct">
 * SPARQL Query Language: 10.2 CONSTRUCT</a>
 *
 * @author hasan
 */
public interface ConstructQuery extends QueryWithSolutionModifier {

    /**
     * <p>Gets the template for constructing triples in a CONSTRUCT query.</p>
     * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#construct">
     * SPARQL Query Language: 10.2 CONSTRUCT</a>
     * @return a template as a set of triple patterns for constructing
     *         new triples.
     */
    public Set<TriplePattern> getConstructTemplate();
}
