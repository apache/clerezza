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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.activation.DataHandler;
import javax.activation.UnsupportedDataTypeException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * This class wraps the Java Activation Framework functionality,
 * so it can be use as <code>MessageBodyWriter</code>.
 * The function <code>isWriteable()</code> is not supported, because
 * this provider can not be provided through the
 * <code>ProvidersImpl</code> class and therefore the function is
 * not needed.
 * 
 * @author mir
 */
public class JafMessageBodyWriter<T> implements MessageBodyWriter<T> {

	private MediaType mediaType;
	
	/**
	 * Creates a <code>JAFMessageBodyWriter</code> that writes entities of
	 * the type T/<code>MediaType</code> into Request Header.
	 * The entity parameter of this constructor and the entity you provide to
	 * the <code>writeTo()</code>-function don't have to be the same.
	 * 
	 * @param entity Object of the type of object you want to convert
	 * @param mediaType
	 */
	public JafMessageBodyWriter(T entity, MediaType mediaType) throws IOException,
			UnsupportedDataTypeException {
		DataHandler dataHandler = new DataHandler(entity, mediaType.toString());
		// if the dataHandler is able to return a InputStream, then the conversion
		// is possible. Otherwise it throws an Exception
		dataHandler.getInputStream();
		this.mediaType = mediaType;
	}
	
	@Override
	public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		try {
			DataHandler dataHandler = new DataHandler((Object) t, mediaType
					.toString());
			InputStream in = dataHandler.getInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			final int BUF_SIZE = 1 << 8; // 1KiB buffer
			byte[] buffer = new byte[BUF_SIZE];
			int bytesRead = -1;
			while ((bytesRead = in.read(buffer)) > -1) {
				out.write(buffer, 0, bytesRead);
			}
			in.close();
			return out.size();
		} catch (IOException ex) {
			return -1;
		}
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public void writeTo(T t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		if (!mediaType.equals(this.mediaType)) {
			throw new IOException(
					"Wrong usage of JAFProvider. MediaType changed!");
		}
		DataHandler dataHandler = new DataHandler(t, mediaType.toString());
		dataHandler.getInputStream();
		writeTo(dataHandler.getInputStream(), entityStream);
	}

	 private static final void writeTo(InputStream in, OutputStream out) throws IOException {
        int read;
        final byte[] data = new byte[2048];
        while ((read = in.read(data)) != -1)
            out.write(data, 0, read);
    }
}
