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
 * Defines an operation in an {@link Expression}. An operation has an operator
 * and one or more operands.
 *
 * @author hasan
 */
public abstract class AbstractOperation implements Expression {
	private String operator;

	public AbstractOperation(String operator) {
		this.operator = operator;
	}

	/**
	 * A string representation of the operator
	 * @return The operator as a string
	 */
	public String getOperatorString() {
		return operator;
	}
}
