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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.ext.Providers;

import org.apache.clerezza.triaxrs.WebRequest;

/**
 * A ParameterInjector creates an object to set the value of a field or parameter 
 * with an annotation A considering its type.
 * @author reto
 */
public interface ParameterInjector<A extends Annotation> {

	public <T> T getValue(WebRequest request, Map<String, String> pathParams,
			Providers providers, Type parameterType, A annotation,
			boolean encodingDisabled, String defaultValue)
			throws UnsupportedFieldType;
}
