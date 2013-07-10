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
package org.apache.clerezza.rdf.core.sparql.query.impl;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.sparql.query.ResourceOrVariable;
import org.apache.clerezza.rdf.core.sparql.query.TriplePattern;
import org.apache.clerezza.rdf.core.sparql.query.UriRefOrVariable;
import org.apache.clerezza.rdf.core.sparql.query.Variable;

/**
 *
 * @author hasan
 */
public class SimpleTriplePattern implements TriplePattern {

    private ResourceOrVariable subject;
    private UriRefOrVariable predicate;
    private ResourceOrVariable object;

    public SimpleTriplePattern(ResourceOrVariable subject,
            UriRefOrVariable predicate,
            ResourceOrVariable object) {
        if (subject == null) {
            throw new IllegalArgumentException("Invalid subject: null");
        }
        if (predicate == null) {
            throw new IllegalArgumentException("Invalid predicate: null");
        }
        if (object == null) {
            throw new IllegalArgumentException("Invalid object: null");
        }
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public SimpleTriplePattern(Variable subject, Variable predicate, Variable object) {
        this(new ResourceOrVariable(subject), new UriRefOrVariable(predicate),
                new ResourceOrVariable(object));
    }

    public SimpleTriplePattern(NonLiteral subject, Variable predicate, Variable object) {
        this(new ResourceOrVariable(subject), new UriRefOrVariable(predicate),
                new ResourceOrVariable(object));
    }

    public SimpleTriplePattern(Variable subject, UriRef predicate, Variable object) {
        this(new ResourceOrVariable(subject), new UriRefOrVariable(predicate),
                new ResourceOrVariable(object));
    }

    public SimpleTriplePattern(NonLiteral subject, UriRef predicate, Variable object) {
        this(new ResourceOrVariable(subject), new UriRefOrVariable(predicate),
                new ResourceOrVariable(object));
    }

    public SimpleTriplePattern(Variable subject, Variable predicate, Resource object) {
        this(new ResourceOrVariable(subject), new UriRefOrVariable(predicate),
                new ResourceOrVariable(object));
    }

    public SimpleTriplePattern(NonLiteral subject, Variable predicate, Resource object) {
        this(new ResourceOrVariable(subject), new UriRefOrVariable(predicate),
                new ResourceOrVariable(object));
    }

    public SimpleTriplePattern(Variable subject, UriRef predicate, Resource object) {
        this(new ResourceOrVariable(subject), new UriRefOrVariable(predicate),
                new ResourceOrVariable(object));
    }

    public SimpleTriplePattern(NonLiteral subject, UriRef predicate, Resource object) {
        this(new ResourceOrVariable(subject), new UriRefOrVariable(predicate),
                new ResourceOrVariable(object));
    }

    @Override
    public ResourceOrVariable getSubject() {
        return subject;
    }

    @Override
    public UriRefOrVariable getPredicate() {
        return predicate;
    }

    @Override
    public ResourceOrVariable getObject() {
        return object;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TriplePattern)) {
            return false;
        }
        final TriplePattern other = (TriplePattern) obj;
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
        return (subject.hashCode() >> 1) ^ subject.hashCode() ^ (subject.hashCode() << 1);
    }
}
