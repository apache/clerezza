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
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.clerezza.triaxrs.util.ProviderUtils;

@Provider
@Consumes({MediaType.WILDCARD, MediaType.APPLICATION_OCTET_STREAM})
@Produces({MediaType.WILDCARD, MediaType.APPLICATION_OCTET_STREAM})
public class InputStreamProvider implements MessageBodyReader<InputStream>,
		MessageBodyWriter<InputStream> {

	@Override
	public boolean isReadable(Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return type != null && type.isAssignableFrom(InputStream.class);
	}

	@Override
	public InputStream readFrom(Class<InputStream> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException,
			WebApplicationException {
		return entityStream;
	}

	@Override
	public long getSize(InputStream t,
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
		return type != null && InputStream.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(InputStream t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException {
		try {
			ProviderUtils.copyStream(t, entityStream);
		} finally {
			t.close();
		}
	}
}
