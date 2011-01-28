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
package org.apache.clerezza.triaxrs.parameterinjectors;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Providers;

import org.apache.clerezza.triaxrs.WebRequest;

/**
 *
 * @author reto
 */
public class FormParameterInjector implements ParameterInjector<FormParam> {

	@Override
	public <T> T getValue(WebRequest request, Map<String, String> pathParam,
			Providers providers, Type parameterType, FormParam annotation,
			boolean encodingDisabled, String defaultValue)
			throws UnsupportedFieldType {

		// TODO better way to get generic type for MultivaluedMap<String,String>
		Type bodyType;
		try {
			bodyType = new Object() {

				@SuppressWarnings("unused")
				public Type multiStringTakingMethod(
						MultivaluedMap<String, String> p) {
					return null;

				}
			}.getClass().getMethod(
					"multiStringTakingMethod", MultivaluedMap.class).
					getGenericParameterTypes()[0];
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		MultivaluedMap<String, String> formParams = request.getBodyObject(
				MultivaluedMap.class, bodyType, null);
		List<String> values = formParams.get(annotation.value());
		
		if (values == null && defaultValue != null){
			values = Collections.singletonList(defaultValue);
		}
		return (T) ConversionUtil.convert(values, parameterType);
	}
}
