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
package org.apache.clerezza.commons.rdf.event;

import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.IRI;

/**
 * The <code>FilterTriple</code> class provides a match()-method that tests
 * if a <code>Triple</code> match a certain triple pattern.
 *
 * @author mir
 */
public class FilterTriple {

    private BlankNodeOrIRI subject;
    private IRI predicate;
    private RDFTerm object;
    
    /**
     * Creates a new <code>FilterTriple</code>. The specified subject,
     * predicate and object are used to test a given <code>Triple</code>. Any
     * of these values can be null, which acts as wildcard in the test.
     *
     * @param subject  the subject.
     * @param predicate  the predicate.
     * @param object  the object.
     */
    public FilterTriple (BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    /**
     * Returns true if the subject, predicate and object of the specified
     * <code>Triple</code> match the subject, predicate and object of this
     * <code>FilterTriple</code>. Null values in the <code>FilterTriple</code>
     * act as wildcards.
     * @param triple
     * @return
     */
    public boolean match(Triple triple) {
        boolean subjectMatch, predicateMatch, objectMatch;
        if (this.subject == null) {
            subjectMatch = true;            
        } else {
            subjectMatch = this.subject.equals(triple.getSubject());
        }
        if (this.predicate == null) {
            predicateMatch = true;
        } else {
            predicateMatch = this.predicate.equals(triple.getPredicate());
        }
        if (this.object == null) {
            objectMatch = true;
        } else {
            objectMatch = this.object.equals(triple.getObject());
        }
        return subjectMatch && predicateMatch && objectMatch;
    }

    @Override
    public String toString() {
        return "FilterTriples: "+subject+" "+predicate+" "+object;
    }

}
