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
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.ext.Providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.triaxrs.WebRequest;
import org.apache.clerezza.triaxrs.headerDelegate.CookieHeaderDelegate;
import org.apache.clerezza.triaxrs.headerDelegate.LocaleProvider;

/**
 *
 * @author reto
 */
public class HeaderParameterInjector implements ParameterInjector<HeaderParam> {

	final static CookieHeaderDelegate cookieProvider = new CookieHeaderDelegate();
	private Logger logger = LoggerFactory.getLogger(HeaderParameterInjector.class);

	@Override
	public <T> T getValue(WebRequest request, Map<String, String> pathParam,
			Providers providers, Type parameterType, HeaderParam annotation,
			boolean encodingDisabled, String defaultValue)
			throws UnsupportedFieldType {

		String headerName = annotation.value();
		List<String> headerValues = request.getHeaders().get(headerName);
		if (headerValues == null && defaultValue != null) {
			headerValues = Collections.singletonList(defaultValue);
		}
		return (T) ConversionUtil.convert(headerValues, parameterType,
				new ConversionUtil.Convertor<Locale>() {

			@Override
			public Locale convert(String string) {
				try{
					return new LocaleProvider().fromString(string);
				} catch (IllegalArgumentException ex) {
					logger.error("Exception {}", ex);
					return null;
				}
			}
		});
	}
}
