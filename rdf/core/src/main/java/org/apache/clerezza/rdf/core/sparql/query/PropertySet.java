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
package org.apache.clerezza.rdf.core.sparql.query;

import java.util.HashSet;
import java.util.Set;

/**
 * A property set is intended to store only predicate paths and inverse predicate paths.
 *
 * @author hasan
 */
public class PropertySet implements PropertyPathExpression {
    private Set<PropertyPathExpression> propertySet = new HashSet<PropertyPathExpression>();

    /**
     * 
     * @param propertyPathExpression expected value is a predicate path or an inverse predicate path
     */
    public void addElement(PropertyPathExpression propertyPathExpression) {
        this.propertySet.add(propertyPathExpression);
    }

    public Set<PropertyPathExpression> getPropertySet() {
        return propertySet;
    }

}
