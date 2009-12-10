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

import java.util.List;
import org.apache.clerezza.rdf.core.UriRef;

/**
 * Defines a function call which is one form of {@link Expression}.
 * A function call has a name of type {@link UriRef} and a list of arguments,
 * where each argument is an {@link Expression}.
 *
 * @author hasan
 */
public class FunctionCall implements Expression {

	private final UriRef name;
	private final List<Expression> arguments;

	public FunctionCall(UriRef name, List<Expression> arguments) {
		this.name = name;
		this.arguments = arguments;
	}

	public UriRef getName() {
		return name;
	};

	public List<Expression> getArguements() {
		return arguments;
	}
}
