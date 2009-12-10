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
package org.apache.clerezza.templating.seedsnipe.datastructure;

/**
 * This Exception is thrown by DataFieldResover on an attempt to resolve an
 * invalid element-name
 * 
 * @author reto
 * 
 * @see DataFieldResolver
 */
public class InvalidElementException extends Exception {	
	private static final long serialVersionUID = 3509023467187582727L;
	private final String message;

	/**
	 * Constructor.
	 * 
	 * @param fieldName  The name of the field that cannot be resolved.
	 */
	public InvalidElementException(final String fieldName) {
		final StringBuffer message = new StringBuffer("Could not resolve "
				+ fieldName);
		this.message = message.toString();
	}

	@Override
	public String getMessage() {
		return message;
	}
}