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

import org.apache.clerezza.rdf.core.impl.SimpleLiteralFactory;

/**
 * This class provides methods to convert java objects to typed literals and
 * vice versa. While the default implementation will provide literal objects
 * storing the data's lexical form in memory, other implementations may
 * create literal optimized for processing within the store.
 *
 * Note: this class uses the notion of "Convertor" (in the Exception naming), 
 * but does not currently provide a mechanism to register such
 * <code>Convertor</code>s. An implementation is said to provide
 * <code>Convertor</code>s for the types it supports.
 *
 * @since 0.3
 * @author reto
 */
public abstract class LiteralFactory {

	private static LiteralFactory instance = new SimpleLiteralFactory();

	/**
	 * Get a <code>LiteralFactory</code>. If this has not been set using
	 * setInstance it returns an instance of
	 * {@link org.apache.clerezza.model.impl.SimpleLiteralFactory}.
	 *
	 * @return a concrete <code>LiteralFactory</code>
	 */
	public static LiteralFactory getInstance() {
		return instance;
	}

	/**
	 * Set the instance returned by <code>getInstance</code>.
	 *
	 * @param instance the new default <code>LiteralFactory</code>
	 */
	public static void setInstance(LiteralFactory instance) {
		LiteralFactory.instance = instance;
	}

	/**
	 * Create a typed literal for the specified object
	 *
	 * @param value the value of the literal to be created
	 * @return a TypedLiteral representing the value
	 * @throws NoConvertorException thrown if <code>value</code> is of an invalid type
	 */
	public abstract TypedLiteral createTypedLiteral(Object value)
			throws NoConvertorException;

	/**
	 * Converts a literal to an instance of the specified class
	 *
	 * @param <T>
	 * @param type the <code>Class</code> of the returned object
	 * @param literal the literal to be converted
	 * @return a java object representing the value of the literal
	 * @throws NoConvertorException thrown if <code>type</code> is unsupported
	 * @throws InvalidLiteralTypeException if the literal type doesn't match the requested java type
	 */
	public abstract <T> T createObject(Class<T> type, TypedLiteral literal)
			throws NoConvertorException, InvalidLiteralTypeException;
}
