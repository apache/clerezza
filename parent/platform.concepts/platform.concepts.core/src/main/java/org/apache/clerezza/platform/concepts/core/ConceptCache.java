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

import java.util.Date;
import java.util.Iterator;
import org.apache.clerezza.platform.concepts.ontologies.CONCEPTS;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.ontologies.SKOS;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * This class manages caches of SPARQL query results against a specific EndPoint
 * and Graph.
 *
 * @author hasan
 */
class ConceptCache {
	private UriRef sparqlEndPoint;
	private UriRef defaultGraph;
	private UriRef CONCEPT_CACHE_MGRAPH = new UriRef("urn:x-localinstance:/concept.cache");

	/**
	 * Constructs a {@link ConceptCache} for the specified SPARQL EndPoint and
	 * Graph.
	 *
	 * @param sparqlEndPoint
	 *		the SPARQL EndPoint used in the query.
	 * @param defaultGraph
	 *		the Graph against which the query was executed.
	 *
	 */
	public ConceptCache(UriRef sparqlEndPoint, UriRef defaultGraph) {
		this.sparqlEndPoint = sparqlEndPoint;
		this.defaultGraph = defaultGraph;
	}

	/**
	 * Caches the query results available in the specified Graph under a
	 * particular search term.
	 *
	 * @param searchTerm
	 *		the term used for querying concepts.
	 * @param parsedGraph
	 *		the Graph which contains query results.
	 */
	void cache(String searchTerm, Graph parsedGraph) {
		MGraph conceptCacheMGraph = getConceptCacheMGraph();
		NonLiteral conceptCacheNode = getConceptCacheNode(conceptCacheMGraph);
		if (conceptCacheNode != null) {
			removeCachedConcepts(searchTerm, conceptCacheNode, conceptCacheMGraph);
		}
		addCacheEntry(searchTerm, conceptCacheNode, conceptCacheMGraph,
				parsedGraph);
	}

	private MGraph getConceptCacheMGraph() {
		MGraph conceptCacheMGraph = null;
		TcManager tcManager = TcManager.getInstance();
		try {
			conceptCacheMGraph = tcManager.getMGraph(CONCEPT_CACHE_MGRAPH);
		} catch (NoSuchEntityException nsee) {
			conceptCacheMGraph = tcManager.createMGraph(CONCEPT_CACHE_MGRAPH);
		}
		return conceptCacheMGraph;
	}

	private NonLiteral getConceptCacheNode(MGraph conceptCacheMGraph) {
		Iterator<Triple> conceptCaches = conceptCacheMGraph.filter(
				null, RDF.type, CONCEPTS.ConceptCache);
		while (conceptCaches.hasNext()) {
			NonLiteral conceptCacheNode = conceptCaches.next().getSubject();
			if (!conceptCacheMGraph.filter(conceptCacheNode,
					CONCEPTS.sparqlEndPoint, sparqlEndPoint).hasNext()) {
				continue;
			}
			if (defaultGraph != null) {
				if (!conceptCacheMGraph.filter(conceptCacheNode,
						CONCEPTS.defaultGraph, defaultGraph).hasNext()) {
					continue;
				}
			}
			return conceptCacheNode;
		}
		return null;
	}

	private void removeCachedConcepts(String searchTerm,
			NonLiteral conceptCacheNode, MGraph conceptCacheMGraph) {
		NonLiteral conceptCacheEntryNode = getConceptCacheEntryNode(
				searchTerm, conceptCacheNode, conceptCacheMGraph);
		if (conceptCacheEntryNode == null) {
			return;
		}
		GraphNode conceptCacheEntryGraphNode = new GraphNode(
				conceptCacheEntryNode, conceptCacheMGraph);
		removeCachedConcepts(conceptCacheEntryGraphNode);
		conceptCacheMGraph.remove(new TripleImpl(conceptCacheNode,
				CONCEPTS.cacheEntry, conceptCacheEntryNode));
	}

	private NonLiteral getConceptCacheEntryNode(String searchTerm,
			NonLiteral conceptCacheNode, MGraph conceptCacheMGraph) {
		TypedLiteral searchLiteral = LiteralFactory.getInstance()
				.createTypedLiteral(searchTerm);
		Iterator<Triple> cacheEntries = conceptCacheMGraph.filter(
				conceptCacheNode, CONCEPTS.cacheEntry, null);
		while (cacheEntries.hasNext()) {
			NonLiteral conceptCacheEntryNode = (NonLiteral) cacheEntries.next()
					.getObject();
			if (!conceptCacheMGraph.filter(conceptCacheEntryNode,
					CONCEPTS.searchTerm, searchLiteral).hasNext()) {
				continue;
			}
			return conceptCacheEntryNode;
		}
		return null;
	}

	private void removeCachedConcepts(GraphNode conceptCacheEntryGraphNode) {
		Iterator<Resource> searchResults =
				conceptCacheEntryGraphNode.getObjects(CONCEPTS.searchResult);
		while (searchResults.hasNext()) {
			Resource concept = searchResults.next();
			GraphNode conceptGraphNode = new GraphNode(concept,
					conceptCacheEntryGraphNode.getGraph());
			conceptGraphNode.deleteProperties(OWL.sameAs);
			conceptGraphNode.deleteProperties(RDFS.comment);
			conceptGraphNode.deleteProperties(SKOS.prefLabel);
			conceptGraphNode.deleteProperties(RDF.type);
		}
		conceptCacheEntryGraphNode.deleteProperties(CONCEPTS.searchResult);
		conceptCacheEntryGraphNode.deleteProperties(DC.date);
		conceptCacheEntryGraphNode.deleteProperties(CONCEPTS.searchTerm);
		conceptCacheEntryGraphNode.deleteProperties(RDF.type);
	}

	private void addCacheEntry(String searchTerm, NonLiteral conceptCacheNode,
			MGraph conceptCacheMGraph, Graph parsedGraph) {
		GraphNode conceptCacheGraphNode = null;
		if (conceptCacheNode == null) {
			conceptCacheGraphNode = addConceptCacheNode(conceptCacheMGraph);
		} else {
			conceptCacheGraphNode = new GraphNode(conceptCacheNode,
					conceptCacheMGraph);
		}
		GraphNode conceptCacheEntryGraphNode = new GraphNode(new BNode(),
				conceptCacheMGraph);
		conceptCacheGraphNode.addProperty(CONCEPTS.cacheEntry,
				conceptCacheEntryGraphNode.getNode());
		conceptCacheEntryGraphNode.addProperty(RDF.type, CONCEPTS.SearchTerm);
		conceptCacheEntryGraphNode.addProperty(CONCEPTS.searchTerm,
				LiteralFactory.getInstance().createTypedLiteral(searchTerm));
		conceptCacheEntryGraphNode.addProperty(DC.date,
				LiteralFactory.getInstance().createTypedLiteral(new Date()));

		Iterator<Triple> concepts = parsedGraph.filter(null, RDF.type, SKOS.Concept);
		while (concepts.hasNext()) {
			conceptCacheEntryGraphNode.addProperty(CONCEPTS.searchResult,
					concepts.next().getSubject());
		}
		conceptCacheMGraph.addAll(parsedGraph);
	}

	private GraphNode addConceptCacheNode(MGraph conceptCacheMGraph) {
		GraphNode conceptCacheGraphNode = new GraphNode(new BNode(),
				conceptCacheMGraph);
		conceptCacheGraphNode.addProperty(RDF.type, CONCEPTS.ConceptCache);
		conceptCacheGraphNode.addProperty(CONCEPTS.sparqlEndPoint,
				sparqlEndPoint);
		if (defaultGraph != null) {
			conceptCacheGraphNode.addProperty(CONCEPTS.defaultGraph,
					defaultGraph);
		}
		return 	conceptCacheGraphNode;
	}

	/**
	 * Retrieves concepts from the cache stored under the specified search term.
	 * Cache entries are invalid if they are older than the specified date.
	 *
	 * @param searchTerm
	 *		the search term under which the concepts are cached.
	 * @param acceptableOldestCachingDate
	 *		the Date before which a cache entry is considered invalid.
	 * @return
	 *		an {@link MGraph} containing the valid cached concepts.
	 */
	MGraph retrieve(String searchTerm, Date acceptableOldestCachingDate) {
		MGraph conceptCacheMGraph = getConceptCacheMGraph();

		NonLiteral conceptCacheNode = getConceptCacheNode(conceptCacheMGraph);
		if (conceptCacheNode == null) {
			return null;
		}
		NonLiteral conceptCacheEntryNode = getConceptCacheEntryNode(searchTerm,
				conceptCacheNode, conceptCacheMGraph);
		if (conceptCacheEntryNode == null) {
			return null;
		}
		GraphNode conceptCacheEntryGraphNode = new GraphNode(
				conceptCacheEntryNode, conceptCacheMGraph);
		Date cachingDate = getCachingDate(conceptCacheEntryGraphNode);
		if (cachingDate == null || cachingDate.before(acceptableOldestCachingDate)) {
			removeCachedConcepts(conceptCacheEntryGraphNode);
			conceptCacheMGraph.remove(new TripleImpl(conceptCacheNode,
					CONCEPTS.cacheEntry, conceptCacheEntryNode));
			return null;
		}
		return getCachedConcepts(conceptCacheEntryGraphNode);
	}

	private Date getCachingDate(GraphNode conceptCacheEntryGraphNode) {
		Iterator<Literal> cachingDates = conceptCacheEntryGraphNode.getLiterals(
				DC.date);
		if (cachingDates.hasNext()) {
			return LiteralFactory.getInstance().createObject(Date.class,
					(TypedLiteral) cachingDates.next());
		}
		return null;
	}

	private MGraph getCachedConcepts(GraphNode conceptCacheEntryGraphNode) {
		MGraph resultMGraph = new SimpleMGraph();
		MGraph conceptCacheMGraph = (MGraph) conceptCacheEntryGraphNode.getGraph();
		NonLiteral conceptCacheNode = (NonLiteral) conceptCacheEntryGraphNode.getNode();
		Iterator<Triple> triples = conceptCacheMGraph.filter(
				conceptCacheNode, CONCEPTS.searchResult, null);
		while (triples.hasNext()) {
			UriRef concept = (UriRef) triples.next().getObject();
			GraphNode graphNode = new GraphNode(concept, conceptCacheMGraph);
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
		return resultMGraph;
	}
}
