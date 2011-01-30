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
 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#select">
 * SPARQL Query Language: 10.1 SELECT</a>
 *
 * @author hasan
 */
public interface SelectQuery extends QueryWithSolutionModifier {

	/**
	 * <p>Tests if this query should return distinct results.</p>
	 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modDistinct">
	 * SPARQL Query Language: 9.3.1 DISTINCT</a>
	 * @return <code>true</code> if the query should return distinct results.
	 */
	public boolean isDistinct();

	/**
	 * <p>Tests if this query should return reduced results.</p>
	 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modReduced">
	 * SPARQL Query Language: 9.3.2 REDUCED</a>
	 * @return <code>true</code> if the query should return reduced results.
	 */
	public boolean isReduced();

	/**
	 * <p>Tests if this query returns all its variables, and not a projected subset.</p>
	 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#solutionModifiers">
	 * SPARQL Query Language: 9 Solution Sequences and Modifiers</a>
	 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modProjection">
	 * SPARQL Query Language: 9.2 Projection</a>
	 * @return <code>true</code> if the query should return all variables.
	 */
	public boolean isSelectAll();

	/**
	 * <p>Gets the list of {@link Variable}s to project the solution to.
	 * If {@link #isSelectAll()} returns <code>true</code> then
	 * this list contains all the variables from the query.</p>
	 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#select">
	 * SPARQL Query Language: 10.1 SELECT</a>
	 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#modProjection">
	 * SPARQL Query Language: 9.2 Projection</a>
	 * @return A list of {@link Variable}s to return from the query.
	 */
	public List<Variable> getSelection();
}
