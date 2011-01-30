/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.clerezza.rdf.sesame.parser;

import java.util.HashMap;
import java.util.Map;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.openrdf.model.Statement;

/**
 *
 * @author hasan
 */
public class SesameScbConverter {

	private final Map<org.openrdf.model.BNode, BNode> bNodesMap = new HashMap<org.openrdf.model.BNode, BNode>();

	/**
	 * Create a {@link org.apache.clerezza.rdf.core.Triple} instance from a {@link org.openrdf.model.Statement}
	 *
	 * @param statement
	 * @return a new TripleImpl instance
	 */
	public Triple createTriple(Statement statement) {
		NonLiteral subject = createNonLiteral(statement.getSubject());
		UriRef predicate = createUriRef(statement.getPredicate());
		Resource resource = createResource(statement.getObject());
		return new TripleImpl(subject, predicate, resource);
	}

	/**
	 * Create a {@link org.apache.clerezza.rdf.core.NonLiteral} instance from a {@link org.openrdf.model.Resource}
	 * 
	 * @param resource
	 * @return a new UriRef or a BNode instance. The BNode instance is new if it does not exist before
	 */
	public NonLiteral createNonLiteral(org.openrdf.model.Resource resource) {
		if (resource instanceof org.openrdf.model.BNode) {
			org.openrdf.model.BNode sesameBNode = (org.openrdf.model.BNode) resource;
			return getOrCreateBNode(sesameBNode);
		} else {
			org.openrdf.model.URI uri = (org.openrdf.model.URI) resource;
			return createUriRef(uri);
		}
	}

	private org.apache.clerezza.rdf.core.BNode getOrCreateBNode(org.openrdf.model.BNode sesameBNode) {
		BNode result = bNodesMap.get(sesameBNode);

		if (result == null) {
			result = new BNode();
			bNodesMap.put(sesameBNode, result);
		}
		return result;
	}

	/**
	 * Create a {@link org.apache.clerezza.rdf.core.UriRef} instance from a {@link org.openrdf.model.URI}
	 *
	 * @param uri
	 * @return a new UriRef instance
	 */
	public UriRef createUriRef(org.openrdf.model.URI uri) {
		return new UriRef(uri.stringValue());
	}

	/**
	 * Create a {@link org.apache.clerezza.rdf.core.Resource} instance from a {@link org.openrdf.model.Value}
	 *
	 * @param value
	 * @return a new PlainLiteralImpl, a new TypedLiteralImpl, a new UriRef, or a BNode instance
	 */
	public Resource createResource(org.openrdf.model.Value value) {
		if (value instanceof org.openrdf.model.Literal) {
			return createLiteral((org.openrdf.model.Literal) value);
		} else {
			return createNonLiteral((org.openrdf.model.Resource) value);
		}
	}

	/**
	 * Create a {@link org.apache.clerezza.rdf.core.Literal} instance from a {@link org.openrdf.model.Literal}
	 *
	 * @param literal
	 * @return a new PlainLiteralImpl or TypedLiteralImpl instance
	 */
	public Literal createLiteral(org.openrdf.model.Literal literal) {
		org.openrdf.model.URI dataType = literal.getDatatype();
		if (dataType == null) {
			String languageString = literal.getLanguage();
			Language language = languageString == null ? null : new Language(languageString);
			return new PlainLiteralImpl(literal.getLabel(), language);
		} else {
			return new TypedLiteralImpl(literal.getLabel(), createUriRef(dataType));
		}
	}
}
