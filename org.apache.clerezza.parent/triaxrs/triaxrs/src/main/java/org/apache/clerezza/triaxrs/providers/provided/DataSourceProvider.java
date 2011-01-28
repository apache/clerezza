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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.activation.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
@Produces("*/*")
@Consumes("*/*")
public class DataSourceProvider implements MessageBodyReader<DataSource>,
		MessageBodyWriter<DataSource> {

	@Override
	public boolean isReadable(Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return type.isAssignableFrom(DataSource.class);
	}

	@Override
	public DataSource readFrom(Class<DataSource> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException,
			WebApplicationException {
		return new ByteArrayDataSource(entityStream, (mediaType == null) ? null : mediaType.toString());
	}

	@Override
	public long getSize(DataSource t,
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
		return DataSource.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(DataSource t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException {
		InputStream inputStream = t.getInputStream();
		final byte[] data = new byte[2048];
		int read;
		while ((read = inputStream.read(data)) != -1) {
			entityStream.write(data, 0, read);
		}
		entityStream.flush();
	}

	public static class ByteArrayDataSource implements DataSource {

		private final String contentType;
		private final byte[] buffer;
		private final int length;

		public ByteArrayDataSource(InputStream is, String contentType) throws IOException {
			DataStoreByteArrayOutputStream os = new DataStoreByteArrayOutputStream();
			byte[] buf = new byte[8192];
			int len;
			while ((len = is.read(buf)) > 0) {
				os.write(buf, 0, len);
			}

			this.buffer = os.getBuf();
			this.length = os.getCount();
			this.contentType = contentType;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(buffer, 0, length);
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public OutputStream getOutputStream() {
			throw new UnsupportedOperationException();
		}

		static class DataStoreByteArrayOutputStream extends ByteArrayOutputStream {

			public byte[] getBuf() {
				return buf;
			}

			public int getCount() {
				return count;
			}
		}
	}
}
