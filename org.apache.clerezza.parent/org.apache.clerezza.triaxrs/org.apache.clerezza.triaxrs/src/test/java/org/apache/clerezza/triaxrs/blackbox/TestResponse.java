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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.URIScheme;

public class TestResponse {

	private static byte[] entity = "a text-plain body".getBytes();
	private static boolean postMethodInvoked = false;

	@Path("/")
	public static class MyResource {

		@GET
		public javax.ws.rs.core.Response handleGet() {
			javax.ws.rs.core.Response r = javax.ws.rs.core.Response.ok()
					.entity(entity).type(MediaType.TEXT_PLAIN_TYPE).build();
			return r;
		}

		@POST
		public javax.ws.rs.core.Response handlePost() throws URISyntaxException {
			postMethodInvoked = true;
			javax.ws.rs.core.Response r = javax.ws.rs.core.Response.created(
					new URI("http://localhost:8000/newresource"))
					.build();
			return r;
		}
	}

	@Test
	public void testResponseObject() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
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
		Assert.assertArrayEquals(entity, responseImpl.getBodyBytes());


	}

	@Test
	public void testPostResponseObjectWithEmptyEntity() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		expect(requestMock.getMethod()).andReturn(Method.POST).anyTimes();
		expect(requestMock.getScheme()).andReturn(URIScheme.HTTP).anyTimes();
		String[] hostValues = {"example.org:8282"};
		expect(requestMock.getHeaderValues(HeaderName.HOST)).andReturn(
				hostValues).anyTimes();

		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/").anyTimes();

		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);

		ResponseImpl response = new ResponseImpl();
		handler.handle(requestMock, response);

		assertTrue(postMethodInvoked);

		String[] location = response.getHeaders().get(HeaderName.LOCATION);
		Assert.assertTrue(location.length == 1);
		assertEquals("http://localhost:8000/newresource", location[0]);
	}
}