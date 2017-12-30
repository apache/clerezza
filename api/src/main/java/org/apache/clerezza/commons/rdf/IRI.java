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

import java.io.Serializable;

/**
 * Represents an RDF URI Reference
 * 
 * RDF URI References are defined in section 6.4 RDF URI References of
 * http://www.w3.org/TR/2004/REC-rdf-concepts-20040210/#section-Graph-URIref
 * 
 * Note that an RDF URI Reference is not the same as defined by RFC3986, 
 * RDF URI References support most unicode characters 
 * 
 * @author reto
 */
public class IRI implements BlankNodeOrIRI, Serializable {

    private String unicodeString;

    public IRI(String unicodeString) {
        this.unicodeString = unicodeString;
    }

    /** 
     * @return the unicode string that produces the URI
     */
    public String getUnicodeString() {
        return unicodeString;
    }

    /**
     * Returns true iff <code>obj</code> == <code>UriRef</code>
     * 
     * @param obj
     * @return true if obj is an instanceof UriRef with 
     * the same unicode-string, false otherwise
     */
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof IRI)) {
            return false;
        }

        return unicodeString.equals(((IRI) obj).getUnicodeString());
    }

    /**
     * @return 5 + the hashcode of the string
     */
    @Override
    public int hashCode() {
        int hash = 5 + unicodeString.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('<');
        buffer.append(unicodeString);
        buffer.append('>');
        return buffer.toString();
    }
}