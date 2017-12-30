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
package org.apache.clerezza.commons.rdf;


/**
 * Represents a literal value that can be a node in an RDF Graph. 
 * Literals are used to identify values such as numbers and dates by 
 * means of a lexical representation. There are two types of literals 
 * represented by the subinterfaces {@link PlainLiteral} 
 * and {@link TypedLiteral} 
 *
 * @author reto
 */
public interface Literal extends RDFTerm {
    
    /**
     * The lexical form of this literal, represented by a <a
     * href="http://www.unicode.org/versions/latest/">Unicode string</a>.
     *
     * @return The lexical form of this literal.
     * @see <a
     * href="http://www.w3.org/TR/rdf11-concepts/#dfn-lexical-form">RDF-1.1
     * Literal lexical form</a>
     */
    String getLexicalForm();

    /**
     * The IRI identifying the datatype that determines how the lexical form
     * maps to a literal value.
     *
     * @return The datatype IRI for this literal.
     * @see <a
     * href="http://www.w3.org/TR/rdf11-concepts/#dfn-datatype-iri">RDF-1.1
     * Literal datatype IRI</a>
     */
    IRI getDataType();
    
    /**
     * If and only if the datatype IRI is <a
     * href="http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"
     * >http://www.w3.org/1999/02/22-rdf-syntax-ns#langString</a>, the language
     * tag for this Literal is a language tag as defined by <a
     * href="http://tools.ietf.org/html/bcp47">BCP47</a>.<br>
     * If the datatype IRI is not <a
     * href="http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"
     * >http://www.w3.org/1999/02/22-rdf-syntax-ns#langString</a>, this method
     * must null.
     *
     * @return The language tag of the literal or null if no language tag is defined
     * @see <a
     * href="http://www.w3.org/TR/rdf11-concepts/#dfn-language-tag">RDF-1.1
     * Literal language tag</a>
     */
    public Language getLanguage();
    
    /** 
     * Returns true if <code>obj</code> is an instance of 
     * <code>literal</code> that is term-equal with this, false otherwise
     * 
     * Two literals are term-equal (the same RDF literal) if and only if the 
     * two lexical forms, the two datatype IRIs, and the two language tags (if 
     * any) compare equal, character by character.
     * 
     * @return true if obj equals this, false otherwise.
     */
    public boolean equals(Object obj);
    
    /**
     * Returns the hash code of the lexical form plus the hash code of the 
     * datatype plus if the literal has a language the hash code of the 
     * language. 
     * 
     * @return hash code
     */
    public int hashCode();
}
