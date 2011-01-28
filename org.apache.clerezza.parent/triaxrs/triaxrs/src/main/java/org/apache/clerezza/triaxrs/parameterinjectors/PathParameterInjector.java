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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.ext.Providers;

import org.apache.clerezza.triaxrs.WebRequest;
import org.apache.clerezza.triaxrs.util.PathSegmentImpl;
import org.apache.clerezza.triaxrs.util.uri.UriEncoder;

/**
 *
 * @author reto
 */
public class PathParameterInjector implements ParameterInjector<PathParam> {

	@Override
	public <T> T getValue(WebRequest request, Map<String, String> pathParam,
			Providers providers, Type parameterType, PathParam annotation,
			final boolean encodingDisabled, String defaultValue)
			throws UnsupportedFieldType {

		//TODO should allow multiple path-segments with same name
		String value = pathParam.get(annotation.value());
		List<String> stringValues = new ArrayList<String>();
		if (value == null || value.equals("") && defaultValue != null) {
			value = defaultValue;
		}
		
		stringValues.add(value);

		return (T) ConversionUtil.convert(stringValues, parameterType,
				new ConversionUtil.Convertor<PathSegment>() {

					@Override
					public PathSegment convert(String string) {
						if (string == null) {
							return null;
							
						}
						return PathSegmentImpl.parse(string, encodingDisabled);
					}
				},
				new ConversionUtil.Convertor<String>() {

					@Override
					public String convert(String string) {						
						if (encodingDisabled) {
							return string;
						} else {
							return UriEncoder.decodeString(string);
						}
					}
				});
	}
}
