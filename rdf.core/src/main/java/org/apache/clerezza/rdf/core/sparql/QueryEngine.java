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
package org.apache.clerezza.rdf.core.sparql;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.sparql.query.Query;

/**
 * A QueryEngine can process SPARQL queries against an arbitrary set of graphs.
 * 
 * @author rbn
 */
public interface QueryEngine {

	/**
	 * Executes any sparql query. The type of the result object will vary
	 * depending on the type of the query.
	 * 
	 * @param tcManager
	 *            where the query originates.
	 * @param defaultGraph
	 *            the default graph against which to execute the query if no
	 *            FROM clause is present
	 * @param query
	 *            Query object to be executed
	 * @return the resulting ResultSet, Graph or Boolean value
	 */
	public Object execute(TcManager tcManager, TripleCollection defaultGraph,
			Query query);

	/**
	 * Executes any sparql query. The type of the result object will vary
	 * depending on the type of the query.
	 * 
	 * @param tcManager
	 *            where the query originates.
	 * @param defaultGraph
	 *            the default graph against which to execute the query if no
	 *            FROM clause is present
	 * @param query
	 *            string to be executed.
	 * @return the resulting ResultSet, Graph or Boolean value
	 */
	public Object execute(TcManager tcManager, TripleCollection defaultGraph,
			String query);
}
