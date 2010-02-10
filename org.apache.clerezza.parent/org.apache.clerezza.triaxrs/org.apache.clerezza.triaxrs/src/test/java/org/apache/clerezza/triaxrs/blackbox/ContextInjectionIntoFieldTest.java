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
package org.apache.clerezza.triaxrs.blackbox;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.WebRequest;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.BodyCheckerThread;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;

/**
 * 
 * @author mir
 * 
 */
public class ContextInjectionIntoFieldTest {

	static String methodString = "method string";
	static Boolean thread1Failure = false;
	static Boolean thread2Failure = false;

	@Provider
	@Produces("test/string")
	public static class MyMessageBodyWriter implements MessageBodyWriter<String> {

		@Context
		public WebRequest request;

		@Override
		public long getSize(String t, Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType) {
			String query = getQueryString();
			return query.length();
		}

		@Override
		public boolean isWriteable(Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType) {
			return String.class.isAssignableFrom(type);
		}

		@Override
		public void writeTo(String t, Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, Object> httpHeaders,
				OutputStream entityStream) throws IOException,
				WebApplicationException {
			String bodyString = getQueryString();
			entityStream.write(bodyString.getBytes());
		}

		private String getQueryString() {
			String query = "";
			if (request != null) {
				try {
					query = request.getWrhapiRequest().getRequestURI()
							.getQuery();
				} catch (HandlerException e) {
					e.printStackTrace();
				}
			}
			return query;
		}

	}

	@Path("/")
	public static class MyResource {

		@Produces("test/string")
		@GET
		public String handleGet() {
			return methodString;
		}
	}

	@Path("/test")
	public static class MyResource2 {

		@Context
		public WebRequest requestProxy;

		@GET
		public void injectResponse(@Context WebRequest request) {
			Assert.assertEquals(requestProxy.getWrhapiRequest(), request
					.getWrhapiRequest());
		}
	}

	@Test
	public void injectIntoProviderField() throws Exception {

		Object[] components = { new MyMessageBodyWriter(), new MyResource() };
		JaxRsHandler handler = HandlerCreator.getHandler("", components);

		Request requestMock = EasyMock.createNiceMock(Request.class);
		EasyMock.makeThreadSafe(requestMock, true);
		ResponseImpl responseImpl = new ResponseImpl();
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		EasyMock.makeThreadSafe(requestURI, true);
		String queryString = "key=value";
		expect(requestURI.getQuery()).andReturn(queryString).anyTimes();
		expect(requestURI.getPath()).andReturn("/").anyTimes();
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);

		handler.handle(requestMock, responseImpl);
		responseImpl.consumeBody();	
		Assert.assertArrayEquals(queryString.getBytes(), responseImpl.getBodyBytes());
	}

	@Test
	public void testConcurrency() throws Exception {
		Object[] components = { new MyMessageBodyWriter(), new MyResource() };
		JaxRsHandler handler = HandlerCreator.getHandler("", components);

		// Request of thread 1
		RequestImpl requestMock = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/");
		String queryString = "key=thread1";
		uri.setQuery(queryString);
		requestMock.setRequestURI(uri);
		requestMock.setMethod(Method.GET);

		// Request of thread 2
		RequestImpl requestMock2 = new RequestImpl();
		RequestURIImpl uri2 = new RequestURIImpl();
		uri2.setPath("/");
		String queryString2 = "key=thread2";
		uri2.setQuery(queryString2);
		requestMock2.setRequestURI(uri2);
		requestMock2.setMethod(Method.GET);

		long iterations = 100;	
		BodyCheckerThread thread1 = new BodyCheckerThread(handler, requestMock,
				queryString.getBytes(), iterations);
		BodyCheckerThread thread2 = new BodyCheckerThread(handler,
				requestMock2, queryString2.getBytes(), iterations);
		thread1.start();
		thread2.start();
		thread1.join();
		thread2.join();
		Assert.assertFalse(thread1.hasFailed());
		Assert.assertFalse(thread2.hasFailed());
	}

	@Test
	public void testFieldInjectionIntoResourceField() throws Exception {
		Object[] components = { new MyResource2() };
		JaxRsHandler handler = HandlerCreator.getHandler("", components);

		RequestImpl requestMock = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/test");
		requestMock.setRequestURI(uri);
		requestMock.setMethod(Method.GET);

		ResponseImpl response = new ResponseImpl();
		handler.handle(requestMock, response);
	}
}
