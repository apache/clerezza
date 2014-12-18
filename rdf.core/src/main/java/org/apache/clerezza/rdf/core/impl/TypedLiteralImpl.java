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
package org.apache.clerezza.rdf.core.impl;

import java.io.Serializable;

import org.apache.commons.rdf.Iri;
import org.apache.commons.rdf.Language;
import org.apache.commons.rdf.Literal;

/**
 *
 * @author reto
 */
public class TypedLiteralImpl implements Literal, Serializable {
    private String lexicalForm;
    private Iri dataType;
    private int hashCode;

    /**
     * @param lexicalForm 
     * @param dataType 
     */
    public TypedLiteralImpl(String lexicalForm, Iri dataType) {
        this.lexicalForm = lexicalForm;
        this.dataType = dataType;
        this.hashCode = lexicalForm.hashCode()+dataType.hashCode();
    }
    
    public Iri getDataType() {
        return dataType;
    }

    /* (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.LiteralNode#getLexicalForm()
     */
    @Override
    public String getLexicalForm() {
        return lexicalForm;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Literal) {
            Literal other = (Literal) obj;
            if (other.getLanguage() != null) {
                return false;
            }
            boolean res = getDataType().equals(other.getDataType())
                    && getLexicalForm().equals(other.getLexicalForm());
            return res;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append('\"');
        result.append(getLexicalForm());
        result.append('\"');
        result.append("^^");
        result.append(getDataType());
        return result.toString();
    }

    @Override
    public Language getLanguage() {
        return null;
    }

}
