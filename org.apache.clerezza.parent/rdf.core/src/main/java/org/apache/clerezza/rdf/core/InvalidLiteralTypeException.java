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
package org.apache.clerezza.rdf.core;

/**
 * Thrown when a literal is of the wrong type for conversion to a java-type
 *
 * @author reto
 */
public class InvalidLiteralTypeException extends RuntimeException {
	
	/**
	 * Constructs the exception to be thrown when a literal cannot be 
	 * converted to an instance of the specified class
	 *
	 * @param javaType the <code>Class</code> to convert to
	 * @param literalType the literalType which can't be converted
	 */
	public InvalidLiteralTypeException(Class<?> javaType, UriRef literalType) {
		super("Cannot create a "+javaType+" from a literal of type "+literalType);
	}
}
