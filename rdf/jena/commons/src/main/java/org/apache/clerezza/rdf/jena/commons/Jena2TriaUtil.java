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
import org.apache.commons.rdf.BlankNode;
import org.apache.commons.rdf.BlankNodeOrIri;
import org.apache.commons.rdf.Iri;
import org.apache.commons.rdf.Language;
import org.apache.commons.rdf.Literal;
import org.apache.commons.rdf.RdfTerm;
import org.apache.commons.rdf.Triple;
import org.apache.commons.rdf.impl.utils.LiteralImpl;
import org.apache.commons.rdf.impl.utils.TripleImpl;

/**
 *
 * @author rbn
 */
public class Jena2TriaUtil {
    private final Map<Node, BlankNode> tria2JenaBNodes;

    public Jena2TriaUtil(Map<Node,BlankNode> tria2JenaBNodes) {
        this.tria2JenaBNodes = tria2JenaBNodes;
    }

    private BlankNode convertJenaNode2TriaBlankNode(Node node) {
        BlankNode result = tria2JenaBNodes.get(node);
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
        final String datatypeString = node.getLiteralDatatypeURI();
        final String languageTag = node.getLiteralLanguage();
        Language language = null;
        if ((languageTag != null) && !languageTag.equals("")) {
             language = new Language(languageTag);
        }
        Iri dataType = null;
        if (datatypeString != null) {
            dataType = new Iri(datatypeString);
        } else {
            dataType = new Iri("http://www.w3.org/2001/XMLSchema#string");
        }
        return new LiteralImpl(lexicalForm, dataType, language);
    }

    /**
     * Converts a URI in jena node to a Iri
     * @param node
     * @return Iri
     */
    public Iri convertJenaUri2UriRef(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("null argument not allowed");
        }
        return new Iri(node.getURI());
    }

    /**
     * Converts a jena node to a resource
     * @param node
     * @return BlankNode if it is a Blank Node, Iri if it is a URI and Literal if it is a literal.
     */
    public RdfTerm convertJenaNode2Resource(Node node) {
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
        throw new RuntimeException("cannot convert " + node + " to RdfTerm");
    }

    /**
     * Converts a node to a BlankNode if it is a Blank Node otherwise to a Iri.
     * If node is a BlankNode and no mapping to a Blank Node exists, then null is
     * returned, otherwise the existing mapping.
     *
     * @param node
     * @return BlankNode if it is a Blank Node otherwise a Iri
     */
    public BlankNodeOrIri convertNonLiteral(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("null argument not allowed");
        }
        if (node.isBlank()) {
            return convertJenaNode2TriaBlankNode(node);
        }
        if (node.isURI()) {
            return convertJenaUri2UriRef(node);
        }
        throw new RuntimeException("cannot convert " + node + " to BlankNodeOrIri");
    }

    public Triple convertTriple(com.hp.hpl.jena.graph.Triple triple) {
        BlankNodeOrIri subject = convertNonLiteral(triple.getSubject());
        Iri predicate = convertJenaUri2UriRef(triple.getPredicate());
        RdfTerm object = convertJenaNode2Resource(triple.getObject());
        if (subject == null || object == null) {
            return null;
        }
        return new TripleImpl(subject, predicate, object);
    }
}
