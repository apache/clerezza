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
package org.apache.clerezza.platform.concepts.core;

import org.apache.clerezza.rdf.core.Graph;

/**
 * An implementation of this interface provides a function to retrieve
 * concepts based on a search term.
 * 
 * @author hasan
 */
public interface ConceptProvider {
	/**
	 * Returns a graph containing all concepts whose SKOS:prefLabels
	 * or SKOS:altLabels contain the search term.
	 *
	 * @param searchTerm a filter condition that specifies the term that must
	 *		be contained within SKOS:prefLabels or SKOS:altLabels of a concept.
	 * @return a Graph containing all concepts that meet the filter condition.
	 *
	 */
	public Graph retrieveConcepts(String searchTerm);
}
