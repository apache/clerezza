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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.util.MessageBody2Write;

/**
 * 
 * @author hasan
 *
 * since version 0.5
 */

public class TestCascadingProviders {

	static final String RESOURCE_PATH = "/rsrc";
	static final String MESSAGE_1 = "My Message 1";
	static final String MESSAGE_2 = "My Message 2";
	static MyMessage receivedBody;

	@Path(RESOURCE_PATH)
	public static class MyResource {

		@PUT
		public void handlePut(MyMessage body) {
			receivedBody = body;
		}
	}

	public static interface MyMessage {

	}

	@Provider
	@Consumes("application/x-mymessage")
	public static class MyReader1 implements MessageBodyReader<MyMessage> {

		@Override
		public boolean isReadable(Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType) {
			return MyMessage.class.isAssignableFrom(type);
		}

		@Override
		public MyMessage readFrom(Class<MyMessage> type, Type genericType,
				Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, String> httpHeaders,
				InputStream entityStream) throws IOException,
				WebApplicationException {
			return new MyMessage() {

				@Override
				public String toString() {
					return MESSAGE_1;
				}

			};
		}

	}

	@Provider
	@Consumes("application/x-mymessage")
	public static class MyReader2 implements MessageBodyReader<MyMessage> {

		@Override
		public boolean isReadable(Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType) {
			return MyMessage.class.isAssignableFrom(type);
		}

		@Override
		public MyMessage readFrom(Class<MyMessage> type, Type genericType,
				Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, String> httpHeaders,
				InputStream entityStream) throws IOException,
				WebApplicationException {
			return new MyMessage() {

				@Override
				public String toString() {
					return MESSAGE_2;
				}

			};
		}

	}

	@Test
	public void testCascadingProviders1() throws Exception {

		JaxRsHandler handler = getJaxRsHandlerUsingRegComp("/foo", "/bar");
		testCascadingProviders(handler, "/foo"+RESOURCE_PATH, MESSAGE_1);

		handler = getJaxRsHandlerUsingApplConfig("/foo", "/bar");
		testCascadingProviders(handler, "/foo"+RESOURCE_PATH, MESSAGE_1);
	}

	@Test
	public void testCascadingProviders2() throws Exception {

		JaxRsHandler handler = getJaxRsHandlerUsingRegComp("/foo/bar", "/foo");
		testCascadingProviders(handler, "/foo/bar"+RESOURCE_PATH, MESSAGE_1);

		handler = getJaxRsHandlerUsingApplConfig("/foo/bar", "/foo");
		testCascadingProviders(handler, "/foo/bar"+RESOURCE_PATH, MESSAGE_1);
	}

	@Test
	public void testCascadingProviders3() throws Exception {

		JaxRsHandler handler = new JaxRsHandler() {
			{
				registerComponent(new MyResource(), "/foo/bar");
				registerComponent(new MyReader2(), "/foo");

			}
		};
		testCascadingProviders(handler, "/foo/bar"+RESOURCE_PATH, MESSAGE_2);
	}

	@Test
	public void testCascadingProviders4() throws Exception {

		JaxRsHandler handler = new JaxRsHandler() {
			{
				registerComponent(new MyResource(), "/foo/bar");
				registerComponent(new MyReader1(), "/foo");
				MyReader2 mbr = new MyReader2();
				registerComponent(mbr, "/foo/bar");
				unregisterComponent(mbr, "/foo/bar");
			}
		};
		testCascadingProviders(handler, "/foo/bar"+RESOURCE_PATH, MESSAGE_1);
	}

	private void testCascadingProviders(JaxRsHandler handler,
			String currentRequestUri, String expectedMessage) throws Exception {

		receivedBody = null;

		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);

		expect(requestMock.getMethod()).andReturn(Method.PUT).anyTimes();
		String[] contentTypeHeader = { "application/x-mymessage" };
		expect(requestMock.getHeaderValues(HeaderName.CONTENT_TYPE)).andReturn(
				contentTypeHeader).anyTimes();
		// this redundancy makes me prefer not to use mocks
		Set<HeaderName> headerNames = new HashSet<HeaderName>();
		headerNames.add(HeaderName.CONTENT_TYPE);
		expect(requestMock.getHeaderNames()).andReturn(headerNames).anyTimes();
		final String message = "The message in the body";
		expect(requestMock.getMessageBody()).andReturn(new MessageBody2Write() {

			@Override
			public void writeTo(WritableByteChannel out) throws IOException {
				out.write(ByteBuffer.wrap(message.getBytes()));
			}
		});
		expect(requestURI.getPath()).andReturn(currentRequestUri);
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);

		handler.handle(requestMock, responseMock);

		Assert.assertNotNull(receivedBody);
		Assert.assertEquals(expectedMessage, receivedBody.toString());
	}

	public static JaxRsHandler getJaxRsHandlerUsingRegComp(
			final String pathPrefix1, final String pathPrefix2) {

		JaxRsHandler handler = new JaxRsHandler() {
			{
				registerComponent(new MyResource(), pathPrefix1);
				registerComponent(new MyResource(), pathPrefix2);
				registerComponent(new MyReader1(), pathPrefix1);
				registerComponent(new MyReader2(), pathPrefix2);

			}
		};
		return handler;
	}

	public static JaxRsHandler getJaxRsHandlerUsingApplConfig(
			final String pathPrefix1, final String pathPrefix2) {

		final Application applicationConfig = new Application() {

			@Override
			public Set<Class<?>> getClasses() {
				Set<Class<?>> result = new HashSet<Class<?>>();

				result.add(MyResource.class);
				result.add(MyReader1.class);

				return result;
			}
		};

		JaxRsHandler handler = new JaxRsHandler() {
			{
				registerComponent(new MyResource(), pathPrefix2);
				registerComponent(new MyReader2(), pathPrefix2);

				registerApplicationConfig(applicationConfig, pathPrefix1);
			}
		};
		return handler;
	}
}
