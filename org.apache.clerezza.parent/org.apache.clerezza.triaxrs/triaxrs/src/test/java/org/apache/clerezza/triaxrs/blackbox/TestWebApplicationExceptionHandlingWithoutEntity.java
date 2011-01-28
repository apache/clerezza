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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
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

public class TestWebApplicationExceptionHandlingWithoutEntity {

	static String errMsg = "missing foo";

	@Path("/")
	public static class MyResource {

		@GET
		public void handleGet() throws Exception {

			javax.ws.rs.core.Response r = javax.ws.rs.core.Response.status(
					Status.BAD_REQUEST).build();
			assertNull(r.getEntity());
			throw new WebApplicationException(r);
		}
	}

	@Provider
	public static class MyExceptionMapper implements
			ExceptionMapper<WebApplicationException> {

		@Override
		public Response toResponse(WebApplicationException exception) {
			javax.ws.rs.core.Response r = javax.ws.rs.core.Response.status(
					Status.BAD_REQUEST).entity(errMsg).type(
					MediaType.TEXT_PLAIN_TYPE).build();
			return r;
		}
	}

	@Test
	public void testExceptions() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class,
				MyExceptionMapper.class);

		Request requestMock = EasyMock.createNiceMock(Request.class);

		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);

		ResponseImpl responseImpl = new ResponseImpl();
		handler.handle(requestMock, responseImpl);
		assertNotNull(responseImpl.getStatus());
		assertNotNull(responseImpl.getHeaders());
        responseImpl.consumeBody();
        Assert.assertArrayEquals(errMsg.getBytes(), responseImpl.getBodyBytes());
	}
}
