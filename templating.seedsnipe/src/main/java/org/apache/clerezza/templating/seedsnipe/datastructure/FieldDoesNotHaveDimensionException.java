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
 * This Exception is thrown by DataFieldResover on an attempt to resolve a field
 * overspecifying the array-positions. This exception contains a solution
 * obtained by reducing the dimensions of arrayPos
 * 
 * @author reto
 *
 */
public class FieldDoesNotHaveDimensionException extends Exception {
	
	private static final long serialVersionUID = 6643439103753447016L;
	private final Object solutionObtainedReducingDimensions;

	/**
	 * Constructor.
	 * 
	 * @param fieldName  The name of the data field that caused the exception.
	 * @param arrayPos  The indices belonging to this field 
	 * 					at the time of the exception.
	 * @param solutionObtainedReducingDimensions The corrected field. 
	 */
	public FieldDoesNotHaveDimensionException(final String fieldName, final int[] arrayPos,
			final Object solutionObtainedReducingDimensions) {
		super("Could not resolve " + fieldName + " with " + arrayPos.length
				+ " dimension");
		this.solutionObtainedReducingDimensions = solutionObtainedReducingDimensions;
	}

	/**
	 * Returns the corrected field.
	 * 
	 * @return The corrected field.
	 */
	public Object getSolutionObtainedReducingDimensions() {
		return solutionObtainedReducingDimensions;
	}
}