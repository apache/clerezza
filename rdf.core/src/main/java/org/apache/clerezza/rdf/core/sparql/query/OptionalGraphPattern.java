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
 * Specifying an optional graph pattern implies the existence of a main graph
 * pattern.
 * The main graph pattern is an empty group pattern if it is not specified.
 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#optionals">
 * SPARQL Query Language: 6 Including Optional Values</a>
 * 
 * @author hasan
 */
public interface OptionalGraphPattern extends GraphPattern {

	/**
	 *
	 * @return
	 *		the main graph pattern to match
	 */
	public GraphPattern getMainGraphPattern();

	/**
	 *
	 * @return
	 *		the optional graph pattern to match
	 */
	public GroupGraphPattern getOptionalGraphPattern();
}
