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
package org.apache.clerezza.triaxrs.providers.provided;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import org.apache.clerezza.triaxrs.util.StreamDataSource;

/**
 * This class wraps the Java Activation Framework functionality,
 * so it can be use as <code>MessageBodyReader</code>.
 * The function <code>isReadable()</code> is not supported, because
 * this provider can not be provided through the
 * <code>ProvidersImpl</code> class and therefore the function
 * is not needed.
 * 
 * @author mir
 */
public class JafMessageBodyReader<T> implements MessageBodyReader<T> {

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public T readFrom(Class<T> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		DataSource dataSource = new StreamDataSource(entityStream, mediaType
				.toString());
		DataHandler dataHandler = new DataHandler(dataSource);
		try {
			Object content = dataHandler.getContent();
			if (content.getClass().isAssignableFrom(type)) {
				return (T) content;
			}
		} catch (IOException ex) {
			return null;
		}
		return null;
	}
}
