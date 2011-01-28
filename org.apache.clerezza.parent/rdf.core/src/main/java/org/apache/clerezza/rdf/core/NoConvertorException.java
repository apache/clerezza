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

import java.lang.reflect.Type;

/**
 * This exception is thrown when no convertor is available to do a required
 * java-object to literal or literal to java-object conversion.
 *
 * @since 0.3
 * @author reto
 */
public class NoConvertorException extends RuntimeException {

	/**
	 * Create an instance of <code>NoConvertorException</code>
	 * indicating that no convertor is available for the type.
	 *
	 * @param type the type for which no convertor is available
	 */
	public NoConvertorException(Type type) {
		super("No convertor available for type "+type);
	}
}
