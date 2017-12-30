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
 * Represents a language as expressed by the RDF 4646 language tag
 *
 * @author reto
 */
public class Language {

    private String id;

    /**
     * Constructs the language tag defined by RDF 4646, normalized to lowercase.
     *
     * @param the id as defined by RDF 4646, normalized to lowercase.
     */
    public Language(String id) {
        if ((id == null) || (id.equals(""))) {
            throw new IllegalArgumentException("A language id may not be null or empty");
        }
        this.id = id.toLowerCase();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof Language) {
            return id.equals(((Language) other).id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
