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

import org.apache.commons.rdf.BlankNodeOrIri;
import org.apache.commons.rdf.RdfTerm;
import org.apache.commons.rdf.Triple;
import org.apache.commons.rdf.Iri;

/**
 *
 * @author reto
 */
public class TripleImpl implements Triple {

    private final BlankNodeOrIri subject;
    private final Iri predicate;
    private final RdfTerm object;

    /**
     * Creates a new <code>TripleImpl</code>.
     *
     * @param subject  the subject.
     * @param predicate  the predicate.
     * @param object  the object.
     * @throws IllegalArgumentException  if an attribute is <code>null</code>.
     */
    public TripleImpl(BlankNodeOrIri subject, Iri predicate, RdfTerm object) {
        if (subject == null) {
            throw new IllegalArgumentException("Invalid subject: null");
        } else if (predicate == null) {
            throw new IllegalArgumentException("Invalid predicate: null");
        } else if (object == null) {
            throw new IllegalArgumentException("Invalid object: null");
        }
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Triple)) {
            return false;
        }
        final Triple other = (Triple) obj;
        if (!this.subject.equals(other.getSubject())) {
            return false;
        }
        if (!this.predicate.equals(other.getPredicate())) {
            return false;
        }
        if (!this.object.equals(other.getObject())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (subject.hashCode() >> 1) ^ predicate.hashCode() ^ (object.hashCode() << 1);
    }

    @Override
    public BlankNodeOrIri getSubject() {
        return subject;
    }

    public Iri getPredicate() {
        return predicate;
    }

    public RdfTerm getObject() {
        return object;
    }

    @Override
    public String toString() {
        return subject + " " + predicate + " " + object + ".";
    }
}
