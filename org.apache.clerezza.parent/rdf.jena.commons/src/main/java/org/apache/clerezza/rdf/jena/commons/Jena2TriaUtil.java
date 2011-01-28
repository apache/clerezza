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
package org.apache.clerezza.rdf.jena.commons;

import com.hp.hpl.jena.graph.Node;
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

/**
 *
 * @author rbn
 */
public class Jena2TriaUtil {
	private final Map<Node, BNode> tria2JenaBNodes;

	public Jena2TriaUtil(Map<Node,BNode> tria2JenaBNodes) {
		this.tria2JenaBNodes = tria2JenaBNodes;
	}

	private BNode convertJenaNode2TriaBlankNode(Node node) {
		BNode result = tria2JenaBNodes.get(node);
		if (result == null) {
			result = new JenaBNodeWrapper(node);
			//tria2JenaBNodes.put(node,result);
		}
		return result;
	}

	/**
	 * Converts a jena literal within a node to a literal.
	 *
	 * @param node
	 * @return TypedLiteral if data type exists otherwise a PlainLiteral
	 */
	private Literal convertJenaLiteral2Literal(Node node) {
		final String lexicalForm = node.getLiteralLexicalForm();
		String datatypeString = node.getLiteralDatatypeURI();
		if (datatypeString != null) {
			UriRef dtUriRef = new UriRef(datatypeString);
			return new TypedLiteralImpl(lexicalForm, dtUriRef);
		} else {
			String language = node.getLiteralLanguage();
			if ((language != null) && !language.equals("")) {
				return new PlainLiteralImpl(lexicalForm, new Language(language));
			} else {
				return new PlainLiteralImpl(lexicalForm);
			}
		}
	}

	/**
	 * Converts a URI in jena node to a UriRef
	 * @param node
	 * @return UriRef
	 */
	public UriRef convertJenaUri2UriRef(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("null argument not allowed");
		}
		return new UriRef(node.getURI());
	}

	/**
	 * Converts a jena node to a resource
	 * @param node
	 * @return BNode if it is a Blank Node, UriRef if it is a URI and Literal if it is a literal.
	 */
	public Resource convertJenaNode2Resource(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("null argument not allowed");
		}
		if (node.isBlank()) {
			return convertJenaNode2TriaBlankNode(node);
		}
		if (node.isURI()) {
			return convertJenaUri2UriRef(node);
		}
		if (node.isLiteral()) {
			return convertJenaLiteral2Literal(node);
		}
		throw new RuntimeException("cannot convert " + node + " to Resource");
	}

	/**
	 * Converts a node to a BNode if it is a Blank Node otherwise to a UriRef.
	 * If node is a BNode and no mapping to a Blank Node exists, then null is
	 * returned, otherwise the existing mapping.
	 *
	 * @param node
	 * @return BNode if it is a Blank Node otherwise a UriRef
	 */
	public NonLiteral convertNonLiteral(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("null argument not allowed");
		}
		if (node.isBlank()) {
			return convertJenaNode2TriaBlankNode(node);
		}
		if (node.isURI()) {
			return convertJenaUri2UriRef(node);
		}
		throw new RuntimeException("cannot convert " + node + " to NonLiteral");
	}

	public Triple convertTriple(com.hp.hpl.jena.graph.Triple triple) {
		NonLiteral subject = convertNonLiteral(triple.getSubject());
		UriRef predicate = convertJenaUri2UriRef(triple.getPredicate());
		Resource object = convertJenaNode2Resource(triple.getObject());
		if (subject == null || object == null) {
			return null;
		}
		return new TripleImpl(subject, predicate, object);
	}
}
