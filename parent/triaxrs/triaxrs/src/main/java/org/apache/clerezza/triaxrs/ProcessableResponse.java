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
package org.apache.clerezza.triaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.clerezza.jaxrs.extensions.MethodResponse;

/**
 * Keeps all information needed to process a response
 * 
 * @author mir
 *
 */
public class ProcessableResponse extends MethodResponse {

	private Response response;
	private Annotation[] annotations;
	private Set<MediaType> methodProducibleTypes;
	private Method invokedMethod;

	private ProcessableResponse(Response response, Annotation[] annotations,
			Set<MediaType> methodProducibleTypes, Method invokedMethod) {
		super();
		this.response = response;
		this.annotations = annotations;
		this.methodProducibleTypes = methodProducibleTypes;
		this.invokedMethod = invokedMethod;
	}

	static public ProcessableResponse createProcessableResponse(
			Object methodReturnValue, Annotation[] annotations,
			Set<MediaType> methodProducibleTypes, Type genericMethodReturnType,
			Method invokedMethod) {
		if (methodReturnValue instanceof ProcessableResponse) {
			return (ProcessableResponse) methodReturnValue;
		}

		Response jaxResponse;
		if (methodReturnValue instanceof Response) {
			jaxResponse = (javax.ws.rs.core.Response) methodReturnValue;
		} else {
			GenericEntity<Object> entity;
			if (!(methodReturnValue instanceof GenericEntity)
					&& methodReturnValue != null) {
				entity = new GenericEntity<Object>(methodReturnValue,
						genericMethodReturnType);
			} else {
				entity = (GenericEntity) methodReturnValue;
			}
			jaxResponse = javax.ws.rs.core.Response.ok(entity).build();
		}
		return new ProcessableResponse(jaxResponse, annotations,
				methodProducibleTypes, invokedMethod);
	}

	public Annotation[] getAnnotations() {
		return annotations;
	}

	public Set<MediaType> getMethodProducibleTypes() {
		return methodProducibleTypes;
	}

	@Override
	public Object getEntity() {
		return response.getEntity();
	}

	@Override
	public int getStatus() {
		return response.getStatus();
	}

	@Override
	public MultivaluedMap<String, Object> getMetadata() {
		return response.getMetadata();
	}

	@Override
	public Method getGeneratingMethod() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
