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
package org.apache.clerezza.triaxrs.blackbox.writers;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import static org.easymock.EasyMock.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.easymock.EasyMock;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.blackbox.BodyMatcher;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.util.MessageBody2Read;

public class TestRuntimeWriterSelection {

	private static String entity = "0123456789";
	private static String hiddenEntity = "**********";

	@Path("/")
	public static class MyResource {

		@GET
		@Produces("*/*")
		public Object handleGet() {
			return new StringBuffer(entity);
		}
	}
	
	@Provider
	@Produces("test/encoded-string")
	public static class HidingWriter implements MessageBodyWriter<String> {

		@Override
		public boolean isWriteable(Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType) {
			 return String.class.isAssignableFrom(type);
		}

		@Override
		public long getSize(String t, java.lang.Class<?> type,
				java.lang.reflect.Type genericType,
				java.lang.annotation.Annotation[] annotations, MediaType mediaType) {
			return t.getBytes().length;
		}

		@Override
		public void writeTo(String t,
				Class<?> type, Type genericType, Annotation[] annotations,
				MediaType mediaType,
				MultivaluedMap<String, Object> httpHeaders,
				OutputStream entityStream) throws IOException, WebApplicationException {
			for (int i = 0; i < getSize(t, type, genericType, annotations, mediaType); i++) {
				entityStream.write('*');
			}
		}
	}
	
	@Provider
	@Produces("test/encoded-buffer")
	public static class HidingWriter2 implements MessageBodyWriter<StringBuffer> {

		@Override
		public boolean isWriteable(Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType) {
			 return StringBuffer.class.isAssignableFrom(type);
		}

		@Override
		public long getSize(StringBuffer t, java.lang.Class<?> type,
				java.lang.reflect.Type genericType,
				java.lang.annotation.Annotation[] annotations, MediaType mediaType) {
			return t.length();
		}
		
		@Override
		public void writeTo(StringBuffer t,
				Class<?> type, Type genericType, Annotation[] annotations,
				MediaType mediaType,
				MultivaluedMap<String, Object> httpHeaders,
				OutputStream entityStream) throws IOException, WebApplicationException {
			for (int i = 0; i < getSize(t, type, genericType, annotations, mediaType); i++) {
				entityStream.write('*');
			}
		}
	}

	@Test
	public void testResponseObject() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(new HidingWriter2(), 
				new HidingWriter(), new MyResource());
		Request requestMock = EasyMock.createNiceMock(Request.class);
		final Response responseMock = EasyMock.createNiceMock(Response.class);
		
		makeThreadSafe(responseMock, true);
		final MessageBody body = new MessageBody2Read() {

			@Override
			public ReadableByteChannel read() throws IOException {
				return Channels.newChannel(new ByteArrayInputStream(hiddenEntity.getBytes()));
			}

		};
		responseMock.setBody(BodyMatcher.eqBody(body));
		responseMock.addHeader(HeaderName.CONTENT_TYPE, "test/encoded-buffer");
		responseMock.setHeader(HeaderName.CONTENT_LENGTH, (long)entity.length());
		responseMock.setResponseStatus(ResponseStatus.SUCCESS);
		expect(requestMock.getMethod()).andReturn(Method.GET);
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/");
		expect(requestMock.getRequestURI()).andReturn(requestURI);
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		Thread thread = new Thread() {

			@Override
			public void run() {
				try {
					responseMock.setResponseStatus(ResponseStatus.SUCCESS);
					responseMock.setHeader(HeaderName.CONTENT_LENGTH, (long)entity.length());
					responseMock.addHeader(HeaderName.get(HeaderName.CONTENT_TYPE.toString()), "test/encoded-buffer");
					responseMock.setBody(body);
				} catch (HandlerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
		};
		thread.start();
		thread.join();
				//handler.handle(requestMock, responseMock);
		verify(responseMock);
	}
}
