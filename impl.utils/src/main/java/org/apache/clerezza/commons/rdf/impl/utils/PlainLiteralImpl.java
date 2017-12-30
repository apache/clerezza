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
package org.apache.clerezza.commons.rdf.impl.utils;

import java.io.Serializable;
import org.apache.clerezza.commons.rdf.IRI;

import org.apache.clerezza.commons.rdf.Language;
import org.apache.clerezza.commons.rdf.Literal;

/**
 *
 * @author reto
 */
public class PlainLiteralImpl extends AbstractLiteral implements Literal, Serializable {

    private final String lexicalForm;
    private final Language language;

    public PlainLiteralImpl(String value) {
        this(value, null);
    }

    public PlainLiteralImpl(String value, Language language) {
        if (value == null) {
            throw new IllegalArgumentException("The literal string cannot be null");
        }
        this.lexicalForm = value;
        this.language = language;
        if (language == null) {
            dataType = XSD_STRING;
        } else {
            dataType = RDF_LANG_STRING;
        }
    }

    @Override
    public String getLexicalForm() {
        return lexicalForm;
    }

    @Override
    public Language getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append('\"').append(lexicalForm).append('\"');
        if (language != null) {
            result.append("@").append(language.toString());
        }
        return result.toString();
    }

    @Override
    public IRI getDataType() {
        return dataType;
    }
    private final IRI dataType;
    private static final IRI XSD_STRING = new IRI("http://www.w3.org/2001/XMLSchema#string");
    private static final IRI RDF_LANG_STRING = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString");
}
