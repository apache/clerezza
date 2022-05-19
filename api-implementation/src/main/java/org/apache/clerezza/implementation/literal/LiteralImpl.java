/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */
package org.apache.clerezza.implementation.literal;

import org.apache.clerezza.IRI;
import org.apache.clerezza.Language;

import java.io.Serializable;

/**
 * @author reto
 */
public class LiteralImpl extends AbstractLiteral implements Serializable {
    private String lexicalForm;
    private IRI dataType;
    private int hashCode;
    private Language language;

    /**
     * @param lexicalForm
     * @param dataType
     * @param language    the language of this literal
     */
    public LiteralImpl(String lexicalForm, IRI dataType, Language language) {
        this.lexicalForm = lexicalForm;
        this.dataType = dataType;
        this.language = language;
        this.hashCode = super.hashCode();
    }

    public IRI getDataType() {
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
        return language;
    }

}
