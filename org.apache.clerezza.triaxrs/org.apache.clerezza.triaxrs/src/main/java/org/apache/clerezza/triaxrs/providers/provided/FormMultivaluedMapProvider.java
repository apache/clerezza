/*******************************************************************************
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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/
package org.apache.clerezza.triaxrs.providers.provided;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.clerezza.triaxrs.util.MultivaluedMapImpl;
import org.apache.clerezza.triaxrs.util.ProviderUtils;
import org.apache.clerezza.triaxrs.util.uri.UriEncoder;

@Provider
@Produces(MediaType.APPLICATION_FORM_URLENCODED)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class FormMultivaluedMapProvider implements
		MessageBodyWriter<MultivaluedMap<String, ? extends Object>>,
		MessageBodyReader<MultivaluedMap<String, String>> {

	@Override
	public long getSize(MultivaluedMap<String, ? extends Object> t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return MultivaluedMap.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(MultivaluedMap<String, ? extends Object> t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException {
		// StringBuilder builder = new StringBuilder();
		// String delim = "";
		// for (String key : t.keySet()) {
		// for (String value : t.get(key)) {
		// builder.append(delim);
		// String encodedKey = URLEncoder.encode(key, "UTF-8");
		// builder.append(encodedKey);
		// if (value != null) {
		// builder.append('=');
		// String encodedValue = URLEncoder.encode(value, "UTF-8");
		// builder.append(encodedValue);
		// }
		// delim = "&";
		// }
		// }
		String string = MultivaluedMapImpl.toString(t, "&"); //$NON-NLS-1$
		string = UriEncoder.encodeQuery(string, true);
		ProviderUtils.writeToStream(string, entityStream, mediaType);
	}

	@Override
	public boolean isReadable(Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		// must be a multivalued map and parameterized with Strings
		if (!(MultivaluedMap.class == type) || !(genericType instanceof ParameterizedType)) {
			return false;
		}

		ParameterizedType pType = (ParameterizedType) genericType;
		Type[] actualTypeArguments = pType.getActualTypeArguments();
		Type type1 = actualTypeArguments[0];
		Type type2 = actualTypeArguments[1];
		if (!(type1 instanceof Class<?>) || !((Class<?>) type1).equals(String.class)
				|| !(type2 instanceof Class<?>)
				|| !((Class<?>) type2).equals(String.class)) {
			return false;
		}
		return true;

	}

	@Override
	public MultivaluedMap<String, String> readFrom(Class<MultivaluedMap<String, String>> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException,
			WebApplicationException {

		MultivaluedMap<String, String> map = new MultivaluedMapImpl();
		String string = ProviderUtils.readFromStreamAsString(entityStream, mediaType);
		StringTokenizer tokenizer = new StringTokenizer(string, "&");
		String token;
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();
			int idx = token.indexOf('=');
			if (idx < 0) {
				map.add(URLDecoder.decode(token, "UTF-8"), null);
			} else if (idx > 0) {
				map.add(URLDecoder.decode(token.substring(0, idx), "UTF-8"),
						URLDecoder.decode(token.substring(idx + 1), "UTF-8"));
			}
		}
		return map;
	}
}
