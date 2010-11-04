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

import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.Providers;

import org.apache.clerezza.triaxrs.WebRequest;
import org.apache.clerezza.triaxrs.headerDelegate.CookieHeaderDelegate;
import org.wymiwyg.wrhapi.HeaderName;

/**
 *
 * @author reto
 */
public class CookieParameterInjector implements ParameterInjector<CookieParam> {

	final static CookieHeaderDelegate cookieProvider = new CookieHeaderDelegate();

	@Override
	public <T> T getValue(WebRequest request, Map<String, String> pathParam,
			Providers providers, Type parameterType, CookieParam annotation,
			boolean encodingDisabled, String defaultValue)
			throws UnsupportedFieldType {

		List<String> cookieValues = request.getHeaders().get(
				HeaderName.COOKIE.toString());
		List<String> cookieValue = null;
		if (cookieValues != null) {		
			for (String cookie : cookieValues){
				if (cookie.substring(0, cookie.indexOf("=")).trim().equals(annotation.value())){
					cookieValue = Collections.singletonList(cookie);
					break;
				}
			}
		}
		if (cookieValue == null && defaultValue != null){
			cookieValue = Collections.singletonList(defaultValue);
		}
		return (T) ConversionUtil.convert(cookieValue, parameterType,
				new ConversionUtil.Convertor<Cookie>() {

			@Override
			public Cookie convert(String string) {
				return cookieProvider.fromString(string);
			}
		});
	}
}
