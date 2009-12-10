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

import java.util.UUID;

/**
 * Defines a Variable. A Variable can occur in an {@link Expression} and is
 * itself an {@link Expression}.
 * 
 * @author hasan
 */
public class Variable implements Expression {

	private String name;

	public Variable() {
		this.name = UUID.randomUUID().toString();
	}

	/**
	 * Creates a variable with the specified name
	 *
	 * @param name
	 */
	public Variable(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name may not be null");
		}
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Variable other = (Variable) obj;
		return name.equals(other.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
