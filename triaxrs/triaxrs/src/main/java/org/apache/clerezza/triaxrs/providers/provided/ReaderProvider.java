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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
@Produces("*/*")
@Consumes("*/*")
public final class ReaderProvider implements MessageBodyReader<Reader>, MessageBodyWriter<Reader> {

	public static final Charset UTF8 = Charset.forName("UTF-8"); //$NON-NLS-1$

	public static final Charset getCharset(MediaType m) {
		String name = (m == null) ? null : m.getParameters().get("charset"); //$NON-NLS-1$
		return (name == null) ? UTF8 : Charset.forName(name);
	}

	@Override
	public boolean isReadable(Class<?> type,
			Type genericType,
			Annotation annotations[],
			MediaType mediaType) {
		return type.isAssignableFrom(Reader.class);
	}

	@Override
	public Reader readFrom(Class<Reader> type,
			Type genericType,
			Annotation annotations[],
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {
		return new BufferedReader(new InputStreamReader(entityStream, getCharset(mediaType)));
	}

	@Override
	public boolean isWriteable(Class<?> type,
			Type genericType,
			Annotation annotations[],
			MediaType mediaType) {
		return Reader.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(Reader t,
			Class<?> type,
			Type genericType,
			Annotation annotations[],
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException {
		try {
			//mediaType = MediaTypeUtils.setDefaultCharsetOnMediaTypeHeader(httpHeaders, mediaType);
			writeTo(t, new OutputStreamWriter(entityStream, getCharset(mediaType)));
		} finally {
			t.close();
		}
	}

	@Override
	public long getSize(Reader t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	public static final void writeTo(Reader in, Writer out) throws IOException {
		int read;
		final char[] data = new char[2048];
		while ((read = in.read(data)) != -1) {
			out.write(data, 0, read);
		}
		out.flush();
	}
}
