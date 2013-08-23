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

/**
 * Wraps either a {@link PropertyPathExpression} or a {@link Variable}
 *
 * @author hasan
 */
public class PropertyPathExpressionOrVariable {

    private final PropertyPathExpression propertyPathExpression;
    private final Variable variable;

    public PropertyPathExpressionOrVariable(PropertyPathExpression propertyPathExpression) {
        if (propertyPathExpression == null) {
            throw new IllegalArgumentException("Invalid propertyPathExpression: null");
        }
        this.propertyPathExpression = propertyPathExpression;
        variable = null;
    }

    public PropertyPathExpressionOrVariable(Variable variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Invalid variable: null");
        }
        this.variable = variable;
        propertyPathExpression = null;
    }

    /**
     *
     * @return
     *        true if it is a {@link Variable}, false if it is a {@link PropertyPathExpression}
     */
    public boolean isVariable() {
        return propertyPathExpression == null;
    }

    /**
     * 
     * @return
     *        the wrapped PropertyPathExpression if it is a PropertyPathExpression, null otherwise
     */
    public PropertyPathExpression getPropertyPathExpression() {
        return propertyPathExpression;
    }
    
    /**
     * 
     * @return
     *        the wrapped Variable if it is a Variable, null otherwise
     */
    public Variable getVariable() {
        return variable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PropertyPathExpressionOrVariable)) {
            return false;
        }
        final PropertyPathExpressionOrVariable other = (PropertyPathExpressionOrVariable) obj;
        if (this.isVariable() != other.isVariable()) {
            return false;
        }
        if (this.isVariable()) {
            if (!this.getVariable().equals(other.getVariable())) {
                return false;
            }
        } else {
            if (!this.getPropertyPathExpression().equals(other.getPropertyPathExpression())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (isVariable()
                ? 13 * getVariable().hashCode() + 17
                : 13 * getPropertyPathExpression().hashCode() + 17);
    }
}
