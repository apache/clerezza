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
package org.apache.clerezza.platform.language;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.ontologies.LINGVOJ;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * An object of this class keeps information about a language.
 *
 * @author mir
 */
public class LanguageDescription {

	private GraphNode resource;
	private Language language;

	LanguageDescription(GraphNode resource) {
		this.resource = resource;
		Literal iso1Literal = null;
		TripleCollection configGraph = resource.getGraph();
		if (configGraph instanceof LockableMGraph) {
			LockableMGraph lockableConfigGraph = (LockableMGraph)configGraph;
			Lock readLock = lockableConfigGraph.getLock().readLock();
			readLock.lock();
			try {
				iso1Literal = (Literal) resource.getObjects(LINGVOJ.iso1).next();
			} finally {
				readLock.unlock();
			}
		} else {
			iso1Literal = (Literal) resource.getObjects(LINGVOJ.iso1).next();
		}
		if (iso1Literal == null) {
			throw new RuntimeException("No iso1 code for " +resource.getNode());
		}
		String iso1 = iso1Literal.getLexicalForm();
		this.language = new Language(iso1);
	}

	/**
	 * Returns a <code>Language</code> object which represents the language
	 * described by this object.
	 * @return the described language
	 */
	public Language getLanguage() {
		return language;
	}

	/**
	 * Returns a graph node in the content graph which leads to further
	 * information about the language described by this object.
	 * The information are those provided by http://www.lingvoj.org/lingvoj.rdf.
	 * @return the graph node leading to further information about the described
	 *		language
	 */
	public GraphNode getResource() {
		return resource;
	}

	/**
	 * Returns the label of the language described by this object in the
	 * specified language, or null if no lable is available in that language
	 * @param lang the language in which the label should be.
	 * @return
	 */
	public String getLabel(Language lang) {
		Lock readLock = null;
		TripleCollection configGraph = resource.getGraph();
		if (configGraph instanceof LockableMGraph) {
			LockableMGraph lockableConfigGraph = (LockableMGraph)configGraph;
			readLock = lockableConfigGraph.getLock().readLock();
			readLock.lock();
		}
		try {
			Iterator<Resource> labels = resource.getObjects(RDFS.label);
			while (labels.hasNext()) {
				PlainLiteral label = (PlainLiteral) labels.next();
				if (label.getLanguage().equals(lang)) {
					return label.getLexicalForm();
				}
			}
			return null;
		} finally {
			if (readLock != null) {
				readLock.unlock();
			}
		}
	}
}
