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
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.junit.Before;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.ResponseStatus;

/**
 *
 * @author reto
 */
public class SelectWriterBasedOnAcceptTest {
	
	private static String entity = "0123456789";

	private JaxRsHandler handler;
	private String1Writer string1Writer;
	private String2Writer string2Writer;
	private StringWildcardWriter stringWildcardWriter;


	@Before
	public void before() {
		string1Writer = new String1Writer();
		string2Writer = new String2Writer();
		stringWildcardWriter = new StringWildcardWriter();

		Object[] components = {string1Writer,string2Writer,
		stringWildcardWriter,new MyResource()};
		handler = HandlerCreator.getHandler("", components);
	}

	@Path("/")
	public static class MyResource {

		@GET
		public String handleGet() {
			return entity;
		}
	}
	

	@Provider
	@Produces( {"test/string1"})
	public static class String1Writer implements MessageBodyWriter<String> {
		public boolean writerCalled;

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
	@Produces( {"test/string2"})
	public static class String2Writer implements MessageBodyWriter<String> {
		public boolean writerCalled;


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
	@Produces( {"*/*"})
	public static class StringWildcardWriter implements MessageBodyWriter<String> {
		public boolean writerCalled;


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
	public void testString1() throws Exception {
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		ResponseImpl responseImpl = new ResponseImpl();

		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		String[] acceptHeaders = {"test/string1;q=.8", "test/string2;q=.7"};
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
		assertTrue(string1Writer.writerCalled);
		Assert.assertFalse(string2Writer.writerCalled);
		Assert.assertArrayEquals(entity.getBytes(), responseImpl.getBodyBytes());
		String[] contentType = responseImpl.getHeaders().get(HeaderName.CONTENT_TYPE);
		Assert.assertTrue(contentType.length == 1);
		Assert.assertEquals("test/string1",contentType[0]);
		String[] contentLength = responseImpl.getHeaders().get(HeaderName.CONTENT_LENGTH);
		Assert.assertTrue(contentLength.length == 1);
		Assert.assertEquals(Integer.toString(entity.length()), contentLength[0]);
		Assert.assertEquals(ResponseStatus.SUCCESS, responseImpl.getStatus());

	}

	@Test
	public void testString2() throws Exception {
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
		assertTrue(string2Writer.writerCalled);
		Assert.assertFalse(string1Writer.writerCalled);
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
	public void testString2NotWildcard() throws Exception {
		Request requestMock = EasyMock.createNiceMock(Request.class);
		ResponseImpl responseImpl = new ResponseImpl();

		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		String[] acceptHeaders = {"test/string2", "*/*"};
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
		assertTrue(string2Writer.writerCalled);
		Assert.assertFalse(string1Writer.writerCalled);
		Assert.assertFalse(stringWildcardWriter.writerCalled);
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
	public void testWildcard() throws Exception {
		Request requestMock = EasyMock.createNiceMock(Request.class);
		ResponseImpl responseImpl = new ResponseImpl();

		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		String[] acceptHeaders = {"test/string2;q=.9", "anything/else"};
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
		assertTrue(stringWildcardWriter.writerCalled);
		Assert.assertFalse(string1Writer.writerCalled);
		Assert.assertFalse(string2Writer.writerCalled);
		Assert.assertArrayEquals(entity.getBytes(), responseImpl.getBodyBytes());
		String[] contentLength = responseImpl.getHeaders().get(HeaderName.CONTENT_LENGTH);
		Assert.assertTrue(contentLength.length == 1);
		Assert.assertEquals(Integer.toString(entity.length()), contentLength[0]);
		Assert.assertEquals(ResponseStatus.SUCCESS, responseImpl.getStatus());
	}
	
}
