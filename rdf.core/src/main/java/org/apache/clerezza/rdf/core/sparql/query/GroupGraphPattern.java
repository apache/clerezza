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
import java.util.Set;

/**
 * Defines a group graph pattern.
 * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#GroupPatterns">
 * SPARQL Query Language: 5.2 Group Graph Patterns</a>
 *
 * @author hasan
 */
public interface GroupGraphPattern extends GraphPattern {

	/**
	 *
	 * @return 
     *      true if it wraps a {@link SelectQuery}, false otherwise.
	 */
	public boolean isSubSelect();

	/**
	 *
	 * @return 
     *      the wrapped subselect if it wraps a {@link SelectQuery}, null otherwise.
	 */
	public SelectQuery getSubSelect();

	/**
	 *
	 * @return
     *      null if it wraps a {@link SelectQuery}, otherwise
     *      a set of all patterns, ANDed together.
	 */
	public Set<GraphPattern> getGraphPatterns();

	/**
	 * @return 
     *      null if it wraps a {@link SelectQuery}, otherwise
	 *		a list of filter expressions for all patterns in the group if any.
	 */
	public List<Expression> getFilter();
}
