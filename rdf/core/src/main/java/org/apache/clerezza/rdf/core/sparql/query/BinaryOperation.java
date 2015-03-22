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
 * Defines an operation with two operands: a left hand side and a right hand side
 * operand.
 *
 * @author hasan
 */
public class BinaryOperation extends AbstractOperation {

    private Expression lhsOperand;
    private Expression rhsOperand;

    public BinaryOperation(String operator,
            Expression lhsOperand, Expression rhsOperand) {
        super(operator);
        this.lhsOperand = lhsOperand;
        this.rhsOperand = rhsOperand;
    }

    public Expression getLhsOperand() {
        return lhsOperand;
    }

    public Expression getRhsOperand() {
        return rhsOperand;
    }
}
