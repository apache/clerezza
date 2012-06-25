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
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.ontologies.SKOS;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * This class stores description of concepts resulted from searches
 * performed by {@link RemoteConceptProvider}s.
 *
 * @author hasan
 */
public class RemoteConceptsDescriptionManager {

	private UriRef REMOTE_CONCEPTS_DESCRIPTION_MGRAPH =
			new UriRef("urn:x-localinstance:/remote.concepts.description");

	/**
	 * Stores SKOS:prefLabel and RDFS.comment of concepts available in the
	 * specified Graph.
	 *
	 * @param graph
	 *		the Graph which contains concepts and their descriptions.
	 */
	void storeConceptsDescription(Graph graph) {
		MGraph remoteConceptsDescriptionMGraph =
				getRemoteConceptsDescriptionMGraph();

		Iterator<Triple> concepts = graph.filter(null, RDF.type, SKOS.Concept);
		while (concepts.hasNext()) {
			UriRef concept = (UriRef) concepts.next().getSubject();
			copyConceptDescription(new GraphNode(concept, graph),
					new GraphNode(concept, remoteConceptsDescriptionMGraph));
		}
	}

	/**
	 * This method creates an {@link MGraph} to store concepts' descriptions
	 * if this graph does not already exist.
	 *
	 * @return
	 *		an {@link MGraph}
	 */
	public MGraph getRemoteConceptsDescriptionMGraph() {
		MGraph remoteConceptsDescriptionMGraph = null;
		TcManager tcManager = TcManager.getInstance();
		try {
			remoteConceptsDescriptionMGraph =
					tcManager.getMGraph(REMOTE_CONCEPTS_DESCRIPTION_MGRAPH);
		} catch (NoSuchEntityException nsee) {
			remoteConceptsDescriptionMGraph =
					tcManager.createMGraph(REMOTE_CONCEPTS_DESCRIPTION_MGRAPH);
		}
		return remoteConceptsDescriptionMGraph;
	}

	private void copyConceptDescription(GraphNode sourceGraphNode,
			GraphNode destinationGraphNode) {

		destinationGraphNode.deleteNodeContext();

		Iterator<Literal> prefLabelStatements =
				sourceGraphNode.getLiterals(SKOS.prefLabel);
		if (prefLabelStatements.hasNext()) {
			destinationGraphNode.addProperty(SKOS.prefLabel,
					prefLabelStatements.next());
		}
		Iterator<Literal> commentStatements =
				sourceGraphNode.getLiterals(RDFS.comment);
		while (commentStatements.hasNext()) {
			destinationGraphNode.addProperty(RDFS.comment,
					commentStatements.next());
		}
	}
}
