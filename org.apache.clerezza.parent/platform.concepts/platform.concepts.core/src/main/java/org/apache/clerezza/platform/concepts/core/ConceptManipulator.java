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

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.ontologies.SKOS;
import org.apache.clerezza.rdf.utils.UriRefUtil;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * This JAX-RS resource can be used to add free concepts to the content graph.
 * The URI path of this resource is /concepts/manipulator.
 * 
 * @author hasan
 */
@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/concepts/manipulator")
public class ConceptManipulator {

	public static String FREE_CONCEPT_SCHEME = "concept-scheme/free-concepts";

	@Reference
	protected ContentGraphProvider cgProvider;

	@Reference
	private PlatformConfig platformConfig;

	/**
	 * Creates and stores a concept with the specified prefLabel and comment
	 * into the content graph if a concept with this prefLabel does not already
	 * exist in the graph.
	 *
	 * @param prefLabel
	 *		if it is an empty string no concept is generated
	 * @param lang
	 *		the language of the prefLabel
	 * @param comment
	 *		is a human-readable description of the concept
	 * @return
	 *		- BAD REQUEST response if prefLabel is undefined or empty
	 *		- CONFLICT response if a concept with the same prefLabel and lang exists
	 *		- CREATED if everything is ok
	 */
	@POST
	@Path("add-concept")
	public Response addConcept(@FormParam("pref-label") String prefLabel,
			@FormParam("lang") String lang,
			@FormParam("comment") String comment) {

		if ((prefLabel == null) || (prefLabel.isEmpty())) {
			return Response.status(Status.BAD_REQUEST)
					.entity("A concept must have a label!")
					.build();
		}
		MGraph contentGraph = cgProvider.getContentGraph();
		PlainLiteral preferredLabel = new PlainLiteralImpl(prefLabel,
				new Language(lang));

		if (contentGraph.filter(null, SKOS.prefLabel, preferredLabel).hasNext()) {
			return Response.status(Status.CONFLICT)
					.entity("A concept with the same label and language already exists!")
					.build();
		}
		
		UriRef concept = getConceptUriRef(platformConfig, prefLabel);
		contentGraph.add(new TripleImpl(concept, RDF.type,
				SKOS.Concept));
		String baseUri = platformConfig.getDefaultBaseUri().getUnicodeString();
		contentGraph.add(new TripleImpl(concept, SKOS.inScheme,
				new UriRef(baseUri + FREE_CONCEPT_SCHEME)));
		contentGraph.add(new TripleImpl(concept, SKOS.prefLabel, preferredLabel));
		if (!comment.isEmpty()) {
			contentGraph.add(new TripleImpl(concept, RDFS.comment,
					new PlainLiteralImpl(comment, new Language(lang))));
		}
		return Response.status(Status.CREATED).entity(concept.getUnicodeString())
				.build();
	}

	static UriRef getConceptUriRef(PlatformConfig platformConfig, String prefLabel) {
		String baseUri = platformConfig.getDefaultBaseUri().getUnicodeString();
		return new UriRef(baseUri + "concept/" +
				UriRefUtil.stripNonUriRefChars(prefLabel));
	}
}




