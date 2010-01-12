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

import java.util.Iterator;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.ontologies.SKOS;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * This JAX-RS resource can be used to search concepts accessible through
 * registered {@link ConceptProvider}s. Concept providers are prioritized.
 * The URI, SKOS:prefLabel and RDFS:comment of a concept from a provider of a 
 * higher priority will be used instead of those concepts having an OWL:sameAs
 * relation with this concept, but from a provider of lower priority.
 *
 * The URI path of this service is /concepts/find.
 *
 * @author hasan
 */
@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/concepts/find")
public class ConceptsFinder {

	@Reference
	protected ConceptProviderManager conceptProviderManager;

	/**
	 * Searches concepts for a specified search term. The actual search task
	 * is delegated to each {@link ConceptProvider} instance. The results from
	 * each {@link ConceptProvider} are merged into a single result graph.
	 * However, concepts from providers of lower priority are only considered if
	 * they are not staying in an OWL:sameAs relation with concepts from
	 * providers of higher priority.
	 *
	 * @param searchTerm
	 *		The search term in form of a String.
	 * @return
	 *		A GraphNode containing the search results.
	 */
	@GET
	public GraphNode findConcepts(@QueryParam(value="searchTerm")
			String searchTerm) {

		List<ConceptProvider> conceptProviderList = conceptProviderManager
				.getConceptProviders();

		MGraph resultMGraph = new SimpleMGraph();
		GraphNode resultNode = new GraphNode(new BNode(), resultMGraph);
		boolean first = true;
		for (ConceptProvider cp : conceptProviderList) {
			Graph graph = cp.retrieveConcepts(searchTerm);
			Iterator<Triple> concepts = graph.filter(null, RDF.type, SKOS.Concept);
			if (first) {
				while (concepts.hasNext()) {
					resultNode.addProperty(SKOS.member, concepts.next().getSubject());
				}
				resultMGraph.addAll(graph);
				first = false;
			} else {
				while (concepts.hasNext()) {
					NonLiteral concept = concepts.next().getSubject();
					GraphNode conceptGraphNode = new GraphNode(concept, graph);
					Iterator<Resource> sameAsConcepts =
							conceptGraphNode.getObjects(OWL.sameAs);
					if (!(hasSameAs(resultMGraph, concept)
							|| hasAnyConcept(resultMGraph, sameAsConcepts))) {
						resultNode.addProperty(SKOS.member, concept);
						addConceptToResultMGraph(resultMGraph, conceptGraphNode);
					}

				}
			}
		}
		resultNode.addProperty(RDF.type, SKOS.Collection);
		return resultNode;
	}

	private boolean hasSameAs(MGraph graph, NonLiteral sameAsConcept) {
		Iterator<Triple> concepts = graph.filter(null, RDF.type, SKOS.Concept);
		while (concepts.hasNext()) {
			NonLiteral concept = concepts.next().getSubject();
			if (graph.filter(concept, OWL.sameAs, sameAsConcept).hasNext()) {
				return true;
			}
		}
		return false;
	}

	private boolean hasAnyConcept(MGraph graph, Iterator<Resource> concepts) {
		while (concepts.hasNext()) {
			NonLiteral concept = (NonLiteral) concepts.next();
			if (graph.filter(concept, RDF.type, SKOS.Concept).hasNext()) {
				return true;
			}
		}
		return false;
	}

	private void addConceptToResultMGraph(MGraph resultMGraph,
			GraphNode graphNode) {
		NonLiteral concept = (NonLiteral) graphNode.getNode();
		resultMGraph.add(new TripleImpl(concept, RDF.type, SKOS.Concept));

		Iterator<Literal> prefLabelStatements = graphNode.getLiterals(SKOS.prefLabel);
		while (prefLabelStatements.hasNext()) {
			resultMGraph.add(new TripleImpl(concept, SKOS.prefLabel,
					prefLabelStatements.next()));
		}
		Iterator<Literal> commentStatements = graphNode.getLiterals(RDFS.comment);
		while (commentStatements.hasNext()) {
			resultMGraph.add(new TripleImpl(concept, RDFS.comment,
					commentStatements.next()));
		}
		Iterator<UriRef> sameAsStatements = graphNode.getUriRefObjects(OWL.sameAs);
		while (sameAsStatements.hasNext()) {
			resultMGraph.add(new TripleImpl(concept, OWL.sameAs,
					sameAsStatements.next()));
		}
	}
}
