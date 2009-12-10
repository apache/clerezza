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

import java.io.IOException;
import org.apache.clerezza.templating.seedsnipe.simpleparser.DefaultParser;

/**
 * Used by {@link DefaultParser} to resolve data fields.
 * 
 * @author reto
 * 
 */
public abstract class DataFieldResolver {

	/**
	 * This method resolves the specified <code>fieldName</code> to its object value,
	 * if any level of the <code>fieldName</code> is an array, 
	 * the corresponding entry in <code>arrayPos</code> is used for resolution. 
	 * 
	 * @param fieldName  name of the data field to resolve.
	 * @param arrayPos  An array containing array indices (the loop variables) 
	 * 					for array fields.
	 * @return  The java object the field maps to.
	 * 	
	 * @throws FieldDoesNotHaveDimensionException  
	 * 				If <code>arrayPos</code> is longer than the field has dimensions.
	 * @throws FieldIndexOutOfBoundsException
	 * 				If a value of <code>arrayPos</code> is larger than the 
	 * 				number of elements in field.
	 * @throws InvalidElementException
	 * 				If field-name is invalid.
	 */
	public abstract Object resolveAsObject(String fieldName, int[] arrayPos)
			throws FieldDoesNotHaveDimensionException,
			FieldIndexOutOfBoundsException, InvalidElementException,
			IOException;

	/**
	 * invokes resolveAsObject and transforms its result to a String
	 * 
	 * @param fieldName  name of the data field to resolve.
	 * @param arrayPos  An array containing array indices (the loop variables) 
	 * 					for array fields.
	 * @return  The string representation (obtained by invoking .toString()) of the resolved object.
	 * @throws FieldDoesNotHaveDimensionException
	 * 				If <code>arrayPos</code> is longer than the field has dimensions.
	 * @throws FieldIndexOutOfBoundsException  
	 * 				If a value of <code>arrayPos</code> is larger than the 
	 * 				number of elements in field.
	 * @throws InvalidElementException  
	 * 				If field-name is invalid.
	 */
	public String resolve(String fieldName, int[] arrayPos)
			throws FieldDoesNotHaveDimensionException,
			FieldIndexOutOfBoundsException, InvalidElementException,
			IOException {
		return resolveAsObject(fieldName, arrayPos).toString();
	}
}