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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.apache.clerezza.triaxrs.WebRequest;
import org.apache.clerezza.triaxrs.headerDelegate.CookieHeaderDelegate;
import org.apache.clerezza.triaxrs.util.HttpHeadersImpl;
import org.apache.clerezza.triaxrs.util.UriInfoImpl;

/**
 *
 * @author reto
 */
public class ContextInjector implements ParameterInjector<Context> {

	final static CookieHeaderDelegate cookieProvider = new CookieHeaderDelegate();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(WebRequest request, Map<String, String> pathParam, Providers providers,
			Type parameterType, Context annotation,
			boolean encodingDisabled, String defaultValue) throws UnsupportedFieldType {

		Class clazz;
		if (parameterType instanceof ParameterizedType) {
			clazz = (Class) ((ParameterizedType) parameterType).getRawType();
		} else if (parameterType instanceof Class) {
			clazz = (Class) parameterType;
		} else {
			throw new IllegalArgumentException("Can't handle type: " +
					parameterType + " having getClass(): " +
					parameterType.getClass());
		}

		if (clazz.isAssignableFrom(UriInfo.class)) {
			return (T) new UriInfoImpl(request);
		} else if (clazz.isAssignableFrom(WebRequest.class)) {
			//this also injects javax.ws.rs.core.Request
			return (T) request;
		} else if (clazz.isAssignableFrom(HttpHeaders.class)) {
			return (T) new HttpHeadersImpl(request);
		} else if (clazz.isAssignableFrom(SecurityContext.class)) {
			throw new UnsupportedOperationException("Not supported yet.");
		} else if (clazz.isAssignableFrom(Providers.class)) {
			return (T) providers;
		}
		return null;
	}
}
