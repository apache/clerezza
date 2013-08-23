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
import org.apache.clerezza.rdf.core.sparql.query.PropertyPathExpression;
import org.apache.clerezza.rdf.core.sparql.query.PropertyPathExpressionOrVariable;
import org.apache.clerezza.rdf.core.sparql.query.PropertyPathPattern;
import org.apache.clerezza.rdf.core.sparql.query.ResourceOrVariable;
import org.apache.clerezza.rdf.core.sparql.query.Variable;

/**
 *
 * @author hasan
 */
public class SimplePropertyPathPattern implements PropertyPathPattern {

    private ResourceOrVariable subject;
    private PropertyPathExpressionOrVariable propertyPathExpression;
    private ResourceOrVariable object;

    public SimplePropertyPathPattern(ResourceOrVariable subject,
            PropertyPathExpressionOrVariable propertyPathExpression,
            ResourceOrVariable object) {
        if (subject == null) {
            throw new IllegalArgumentException("Invalid subject: null");
        }
        if (propertyPathExpression == null) {
            throw new IllegalArgumentException("Invalid property path expression: null");
        }
        if (object == null) {
            throw new IllegalArgumentException("Invalid object: null");
        }
        this.subject = subject;
        this.propertyPathExpression = propertyPathExpression;
        this.object = object;
    }

    public SimplePropertyPathPattern(Variable subject, Variable propertyPathExpression, Variable object) {
        this(new ResourceOrVariable(subject), new PropertyPathExpressionOrVariable(propertyPathExpression),
                new ResourceOrVariable(object));
    }

    public SimplePropertyPathPattern(NonLiteral subject, Variable propertyPathExpression, Variable object) {
        this(new ResourceOrVariable(subject), new PropertyPathExpressionOrVariable(propertyPathExpression),
                new ResourceOrVariable(object));
    }

    public SimplePropertyPathPattern(Variable subject, Variable propertyPathExpression, Resource object) {
        this(new ResourceOrVariable(subject), new PropertyPathExpressionOrVariable(propertyPathExpression),
                new ResourceOrVariable(object));
    }

    public SimplePropertyPathPattern(NonLiteral subject, Variable propertyPathExpression, Resource object) {
        this(new ResourceOrVariable(subject), new PropertyPathExpressionOrVariable(propertyPathExpression),
                new ResourceOrVariable(object));
    }

    public SimplePropertyPathPattern(Variable subject, PropertyPathExpression propertyPathExpression, Variable object) {
        this(new ResourceOrVariable(subject), new PropertyPathExpressionOrVariable(propertyPathExpression),
                new ResourceOrVariable(object));
    }

    public SimplePropertyPathPattern(NonLiteral subject, PropertyPathExpression propertyPathExpression, Variable object) {
        this(new ResourceOrVariable(subject), new PropertyPathExpressionOrVariable(propertyPathExpression),
                new ResourceOrVariable(object));
    }

    public SimplePropertyPathPattern(Variable subject, PropertyPathExpression propertyPathExpression, Resource object) {
        this(new ResourceOrVariable(subject), new PropertyPathExpressionOrVariable(propertyPathExpression),
                new ResourceOrVariable(object));
    }

    public SimplePropertyPathPattern(NonLiteral subject, PropertyPathExpression propertyPathExpression, Resource object) {
        this(new ResourceOrVariable(subject), new PropertyPathExpressionOrVariable(propertyPathExpression),
                new ResourceOrVariable(object));
    }

    @Override
    public ResourceOrVariable getSubject() {
        return subject;
    }

    @Override
    public PropertyPathExpressionOrVariable getPropertyPathExpression() {
        return propertyPathExpression;
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
        if (!(obj instanceof PropertyPathPattern)) {
            return false;
        }
        final PropertyPathPattern other = (PropertyPathPattern) obj;
        if (!this.subject.equals(other.getSubject())) {
            return false;
        }
        if (!this.propertyPathExpression.equals(other.getPropertyPathExpression())) {
            return false;
        }
        if (!this.object.equals(other.getObject())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (subject.hashCode() >> 1) ^ propertyPathExpression.hashCode() ^ (object.hashCode() << 1);
    }
}
