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
 * in an invalid Dimension.
 * 
 * @author reto
 */
public class FieldIndexOutOfBoundsException extends Exception {
	
	private static final long serialVersionUID = 3632533344966727297L;
	private final String message;
	private final int dimension; 

	/**
	 * Constructor.
	 * 
	 * @param fieldName  The name of the field that has exceeded its bounds.
	 * @param arrayPos  The indices of the field exceeding its bounds.
	 * @param dimension  The arrayPos dimension that caused the exception
	 * 						 (equals the index of arrayPos used).  
	 */
	public FieldIndexOutOfBoundsException(final String fieldName, final int[] arrayPos, final int dimension) {
		this.dimension = dimension;
		final StringBuffer message = new StringBuffer("Could not resolve "
				+ fieldName + " with arrayPos (");
		for (int i = 0; i < arrayPos.length; i++) {
			message.append(arrayPos[i] + ",");
		}
		message.append(")");
		this.message = message.toString();

	}
	
	/**
	 * Returns which dimension (equals the index of arrayPos) that has been used
	 * when the Exception has been thrown.
	 * 
	 * @return  The dimension.
	 */
	public int getDimension() {
		return dimension;
	}

	@Override
	public String getMessage() {
		return message;
	}
}