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
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import java.util.Collections;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;

/**
 *
 * @author reto
 */
public class WriterAcceptTest {
	
	private static String entity = "0123456789";
	private static Boolean writerCalled = false;

	@Path("/")
	public static class MyResource {

		@GET
		public String handleGet() {
			return entity;
		}
	}
	
	@Path("/")
	public static class MyResource2 {

		@Produces("test/string2")
		@GET
		public String handleGet() {
			return entity;
		}
	}

	@Provider
	@Produces( {"test/string1", "test/string2", "test/string3"})
	public static class MultiFormatWriter implements MessageBodyWriter<String> {

		@Override
		public boolean isWriteable(Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType) {
			return String.class.isAssignableFrom(type);
		}

		@Override
		public long getSize(String t, java.lang.Class<?> type,
				java.lang.reflect.Type genericType,
				java.lang.annotation.Annotation[] annotations,
				MediaType mediaType) {
			return t.getBytes().length;
		}

		@Override
		public void writeTo(String t, Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, Object> httpHeaders,
				OutputStream entityStream) throws IOException,
				WebApplicationException {
			entityStream.write(t.getBytes());
			writerCalled = true;
		}
	}

	@Provider
	@Produces( {"test/string1", "test/string3"})
	public static class MultiFormatWriterWithWrongProduces
			implements MessageBodyWriter<String> {

		@Override
		public boolean isWriteable(Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType) {
			return String.class.isAssignableFrom(type);
		}

		@Override
		public long getSize(String t, java.lang.Class<?> type,
				java.lang.reflect.Type genericType,
				java.lang.annotation.Annotation[] annotations,
				MediaType mediaType) {
			return t.getBytes().length;
		}

		@Override
		public void writeTo(String t, Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, Object> httpHeaders,
				OutputStream entityStream) throws IOException,
				WebApplicationException {
			entityStream.write(t.getBytes());
			writerCalled = true;
		}
	}
	
	@Test
	public void testResponseObject() throws Exception {

		Object[] components = {new MultiFormatWriter(), new MyResource()};
		JaxRsHandler handler = HandlerCreator.getHandler("", components);
		Request requestMock = EasyMock.createNiceMock(Request.class);
		ResponseImpl responseImpl = new ResponseImpl();

		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		String[] acceptHeaders = {"test/string2;q=.8", "test/string1;q=.7"};
		expect(requestMock.getHeaderNames()).andReturn(
				Collections.singleton(HeaderName.ACCEPT)).anyTimes();
		expect(requestMock.getHeaderValues(HeaderName.ACCEPT))
				.andReturn(acceptHeaders).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		handler.handle(requestMock, responseImpl);
		responseImpl.consumeBody();
		assertTrue(writerCalled);
		Assert.assertArrayEquals(entity.getBytes(), responseImpl.getBodyBytes());

		String[] contentType = responseImpl.getHeaders().get(HeaderName.CONTENT_TYPE);
		Assert.assertTrue(contentType.length == 1);
		Assert.assertEquals("test/string2",contentType[0]);
		String[] contentLength = responseImpl.getHeaders().get(HeaderName.CONTENT_LENGTH);
		Assert.assertTrue(contentLength.length == 1);
		Assert.assertEquals(Integer.toString(entity.length()), contentLength[0]);
		Assert.assertEquals(ResponseStatus.SUCCESS, responseImpl.getStatus());
	}
	
	@Test
	public void testNoMatchingAcceptAndProduce() throws Exception {
		writerCalled = false;
		Object[] components = {new MultiFormatWriter(), new MyResource2()};
		JaxRsHandler handler = HandlerCreator.getHandler("", components);

		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/");
		String[] headervalues = new String[1];
		headervalues[0] = "test/string3";
		request.setHeader(HeaderName.ACCEPT, headervalues);
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		ResponseImpl response = new ResponseImpl();
		handler.handle(request, response);

		assertTrue(!writerCalled);
	}

	@Test
	public void testWrongContentTypeInResponse() throws Exception {

		Object[] components = {new MultiFormatWriterWithWrongProduces(), new MyResource()};
		JaxRsHandler handler = HandlerCreator.getHandler("", components);
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);

		makeThreadSafe(responseMock, true);

		//triaxrs has default writer for String, so the following isn't true:
		//responseMock.setResponseStatus(ResponseStatus.NOT_ACCEPTABLE);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		String[] acceptHeaders = {"test/string2;q=.8", "test/string4;q=.7"};
		expect(requestMock.getHeaderNames()).andReturn(
				Collections.singleton(HeaderName.ACCEPT)).anyTimes();
		expect(requestMock.getHeaderValues(HeaderName.ACCEPT))
				.andReturn(acceptHeaders).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertTrue(!writerCalled);
		verify(responseMock);
	}
}
