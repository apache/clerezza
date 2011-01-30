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

import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.sparql.ParseException;
import org.apache.clerezza.rdf.core.sparql.QueryParser;
import org.apache.clerezza.rdf.core.sparql.query.ConstructQuery;

/**
 * This {@link ConceptProvider} operates on concepts available in the localhost.
 *
 * @author tio, hasan
 */
public class LocalConceptProvider implements ConceptProvider {

	private TcManager tcManager = null;
	private ContentGraphProvider contentGraphProvider = null;
	private UriRef selectedScheme = null;

	/**
	 * Constructs a {@link LocalConceptProvider} with the specified parameters.
	 *
	 * @param tcManager
	 *		Reference to the {@link TcManager}
	 * @param contentGraphProvider
	 *		Reference to a {@link ContentGraphProvider}
	 * @param selectedScheme
	 *		The scheme in which concepts are to be searched.
	 */
	public LocalConceptProvider(TcManager tcManager,
			ContentGraphProvider contentGraphProvider, UriRef selectedScheme) {
		this.tcManager = tcManager;
		this.contentGraphProvider = contentGraphProvider;
		this.selectedScheme = selectedScheme;
	}

	public UriRef getSelectedScheme() {
		return this.selectedScheme;
	}

	@Override
	public Graph retrieveConcepts(String searchTerm) {
		QueryParser queryParser = QueryParser.getInstance();

		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX skos: <http://www.w3.org/2008/05/skos#> " +
				"CONSTRUCT {?concept a skos:Concept; skos:prefLabel ?prefLabel; " +
				"rdfs:comment ?comment; owl:sameAs ?sameConcept .} " +
				"WHERE {?concept skos:inScheme " + selectedScheme.toString() +
				"; skos:prefLabel ?prefLabel . " +
				"OPTIONAL { ?concept skos:altLabel ?altLabel .} " +
				"OPTIONAL { ?concept rdfs:comment ?comment .} " +
				"OPTIONAL { ?concept owl:sameAs ?sameConcept .} " +
				"FILTER (REGEX(STR(?prefLabel), '" +
				searchTerm + "', 'i') || REGEX(STR(?altLabel), '" +
				searchTerm + "', 'i'))}";

		ConstructQuery constructQuery;
		try {
			constructQuery = (ConstructQuery) queryParser.parse(query);
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}

		MGraph defaultGraph = contentGraphProvider.getContentGraph();
		return tcManager.executeSparqlQuery(constructQuery, defaultGraph);
	}
}
