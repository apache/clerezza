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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;

public class TestApplicationProvidedWriter {

	private static String entity = "0123456789";
	private static String hiddenEntity = "**********";

	@Path("/")
	public static class MyResource {

		@GET
		public String handleGet() {
			return entity;
		}
	}
	
	@Provider
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

	@Test
	public void testResponseObject() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class, HidingWriter.class);
	
		Request requestMock = EasyMock.createNiceMock(Request.class);
		ResponseImpl responseImpl = new ResponseImpl();
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		handler.handle(requestMock, responseImpl);
		responseImpl.consumeBody();
		Assert.assertArrayEquals(hiddenEntity.getBytes(), responseImpl.getBodyBytes());

	}
}
