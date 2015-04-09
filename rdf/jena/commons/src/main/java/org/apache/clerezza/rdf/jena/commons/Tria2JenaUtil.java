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

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import java.util.Map;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.BlankNodeOrIri;
import org.apache.clerezza.commons.rdf.Iri;
import org.apache.clerezza.commons.rdf.Literal;
import org.apache.clerezza.commons.rdf.RdfTerm;
import org.apache.clerezza.commons.rdf.Triple;


/**
 *
 * @author rbn
 */
public class Tria2JenaUtil {
    private Map<BlankNode, Node> tria2JenaBNodes;

    public Tria2JenaUtil(Map<BlankNode, Node> tria2JenaBNodes) {
        this.tria2JenaBNodes = tria2JenaBNodes;
    }

    public Node convert2JenaNode(BlankNodeOrIri nonLiteral, boolean createBlankNode) {
        if (nonLiteral instanceof Iri) {
            return convert2JenaNode((Iri)nonLiteral);
        } else {
            return convert2JenaNode((BlankNode)nonLiteral, createBlankNode);
        }
    }

    public Node convert2JenaNode(Literal literal) {
        if (literal == null) {
            throw new IllegalArgumentException("null argument not allowed");
        }
        return com.hp.hpl.jena.graph.NodeFactory.createLiteral(
                            literal.getLexicalForm(),
                            literal.getLanguage() == null ? null : literal.getLanguage().
                            toString(), TypeMapper.getInstance().
                            getSafeTypeByName(
                            literal.getDataType().getUnicodeString()));
    }

    public Node convert2JenaNode(RdfTerm resource) {
        return convert2JenaNode(resource, false);
    }

    public Node convert2JenaNode(RdfTerm resource, boolean createBlankNode) {
        if (resource instanceof BlankNodeOrIri) {
            return convert2JenaNode((BlankNodeOrIri)resource, createBlankNode);
        }
        return convert2JenaNode((Literal)resource);
    }

    public Node convert2JenaNode(Iri uriRef) {
        if (uriRef == null) {
            throw new IllegalArgumentException("null argument not allowed");
        }
        return com.hp.hpl.jena.graph.Node.createURI(
                        uriRef.getUnicodeString());
    }

    public Node convert2JenaNode(BlankNode bnode) {
        return convert2JenaNode(bnode, false);
    }
    
    public Node convert2JenaNode(BlankNode bnode, boolean createBlankNode) {
        if (bnode == null) {
            throw new IllegalArgumentException("null argument not allowed");
        }
        if (bnode instanceof JenaBNodeWrapper) {
            return ((JenaBNodeWrapper)bnode).node;
        }
        Node result = tria2JenaBNodes.get(bnode);
        if (result == null && createBlankNode) {
            result = com.hp.hpl.jena.graph.Node.createAnon();
            tria2JenaBNodes.put(bnode, result);
        }
        return result;
    }

    public com.hp.hpl.jena.graph.Triple convertTriple(Triple triple) {
        return convertTriple(triple, false);
    }

    public com.hp.hpl.jena.graph.Triple convertTriple(Triple triple, boolean createBlankNodes) {
        Node subject = convert2JenaNode(triple.getSubject(), createBlankNodes);
        Node predicate = convert2JenaNode(triple.getPredicate());
        Node object = convert2JenaNode(triple.getObject(), createBlankNodes);
        if (subject == null || object == null) {
            return null;
        }
        return new com.hp.hpl.jena.graph.Triple(subject, predicate, object);
    }
}
