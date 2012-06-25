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
 * This interface models logical, relational, and numeric expression.
 * This includes terms and factors in mathematical formulas which can contain
 * variables, literals, and function calls.
 * In a SPARQL query, expressions can occur in an ORDER BY clause or
 * a FILTER constraint.
 *
 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modOrderBy">
 * SPARQL Query Language: 9.1 ORDER BY</a>
 *
 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#termConstraint">
 * SPARQL Query Language: 3 RDF Term Constraints (Informative)</a>
 * 
 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#evaluation">
 * SPARQL Query Language: 11.2 Filter Evaluation</a>
 *
 * @author hasan
 */
public interface Expression {
}
