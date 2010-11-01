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

import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.triaxrs.WebRequest;
import org.apache.clerezza.triaxrs.util.QueryStringParser;
import org.wymiwyg.wrhapi.HandlerException;

/**
 * 
 * @author reto
 */
public class QueryParameterInjector implements ParameterInjector<QueryParam> {
	
	private Logger logger = LoggerFactory.getLogger(QueryParameterInjector.class);

	@Override
	public <T> T getValue(WebRequest request, Map<String, String> pathParam,
			Providers providers, Type parameterType, QueryParam annotation,
			boolean encodingDisabled, String defaultValue)
			throws UnsupportedFieldType {
		try {
			final String query = request.getWrhapiRequest().getRequestURI()
					.getQuery();
			List<String> values = null;
			if (query != null) {
				values = QueryStringParser.getParameterValues(query,
						!encodingDisabled, annotation.value());
			}
			if (values == null && defaultValue != null) {
				values = Collections.singletonList(defaultValue);
			}
			return (T) ConversionUtil.convert(values, parameterType);
		} catch (HandlerException ex) {
			
			logger.debug("Exception {}", ex);
			throw new RuntimeException(ex);
		}
	}

}
