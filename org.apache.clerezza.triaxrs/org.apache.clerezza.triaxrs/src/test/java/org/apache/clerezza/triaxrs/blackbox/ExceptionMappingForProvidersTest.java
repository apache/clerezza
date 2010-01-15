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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.util.MessageBody2Read;

public class ExceptionMappingForProvidersTest {
	public static final String TEST_BLA_TYPE = "test/bla";

	public static class MyException extends RuntimeException {

	}

	static boolean exceptionMapperUsed;
	static final String BODY = "body";

	public static class MyObject {}

	@Path("/")
	public static class MyResource {

		@POST
		@Path("reader")
		@Consumes(TEST_BLA_TYPE)
		public void handleGetReader(MyObject obj) throws Exception {
			throw new RuntimeException();
		}

		@GET
		@Path("writer")
		public MyObject handleGetWriter() throws Exception {
			return new MyObject();
		}
	}

	@Provider
	public static class MyExceptionMapper implements
			ExceptionMapper<MyException> {

		@Override
		public Response toResponse(MyException exception) {
			exceptionMapperUsed = true;
			return Response.ok(BODY).build();
		}
	}

	@Provider
	@Consumes(TEST_BLA_TYPE)
	public static class MyMessageBodyReader implements
			MessageBodyReader<MyObject> {

		@Override
		public boolean isReadable(Class<?> type, Type genericType, Annotation[]
				annotations, MediaType mediaType) {
			return true;
		}

		@Override
		public MyObject readFrom(Class<MyObject> type, Type genericType,
				Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, String> httpHeaders,
				InputStream entityStream) throws IOException, WebApplicationException {
			throw new MyException();
		}
	}

	@Provider
	@Produces(TEST_BLA_TYPE)
	public static class MyMessageBodyWriter implements	MessageBodyWriter<MyObject> {

		@Override
		public boolean isWriteable(Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType) {
			return true;
		}

		@Override
		public long getSize(MyObject t, Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType) {
			return 200;
		}

		@Override
		public void writeTo(MyObject t, Class<?> type, Type genericType,
				Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
				throws IOException, WebApplicationException {
			throw new MyException();
		}
	}

	@Test
	public void testMappingForMessageBodyReader() throws Exception {
		exceptionMapperUsed = false;
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class,
				MyExceptionMapper.class, MyMessageBodyReader.class,
				MyMessageBodyWriter.class);
		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/reader");
		request.setRequestURI(uri);
		request.setMethod(Method.POST);
		String[] type = {TEST_BLA_TYPE};
		request.setHeader(HeaderName.CONTENT_TYPE, type);
		request.setMessageBody(new MessageBody2Read() {

			@Override
			public ReadableByteChannel read() throws IOException {
				return Channels.newChannel(new ByteArrayInputStream("body".getBytes()));
			}
		});
		ResponseImpl responseImpl = new ResponseImpl();
		handler.handle(request, responseImpl);
		assertTrue(exceptionMapperUsed);
	}

	@Test
	public void testMappingForMessageBodyWriter() throws Exception {
		exceptionMapperUsed = false;
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class,
				MyExceptionMapper.class, MyMessageBodyReader.class,
				MyMessageBodyWriter.class);
		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/writer");
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		ResponseImpl responseImpl = new ResponseImpl();
		handler.handle(request, responseImpl);
		responseImpl.consumeBody();
		assertTrue(exceptionMapperUsed);
		assertEquals(ResponseStatus.SUCCESS, responseImpl.getStatus());
		assertEquals(BODY, new String(responseImpl.getBodyBytes()));

		String[] contentLength = responseImpl.getHeaders().get(HeaderName.CONTENT_LENGTH);
		assertTrue(contentLength.length == 1);
		assertEquals(new Long(BODY.length()), new Long(contentLength[0]));
	}
}
