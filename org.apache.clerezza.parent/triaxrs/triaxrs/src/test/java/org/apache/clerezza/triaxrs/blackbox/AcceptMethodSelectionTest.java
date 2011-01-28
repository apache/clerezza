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

import java.util.Collections;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;


public class AcceptMethodSelectionTest {

	static int methodInvoked = -1;

	@Path("/")
	public static class MyResource {

		@GET
		@Produces("test/string2")
		public void getString2() {
			methodInvoked = 1;
		}
		@GET
		@Produces("test/string3")
		public void getString3() {
			methodInvoked = 2;
		}
		
		@GET
		@Produces({"test/string4", "*/*"})
		public void getString4AndAll() {
			methodInvoked = 3;
		}
		
		@GET
		@Produces({"test/string5", "test/string6"})
		public void getString5And6() {
			methodInvoked = 4;
		}
	}
	@Before
	public void prepare() {
		methodInvoked = -1;
	}

	@Test
	public void acceptHeaderBasedMethodSelection() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		String[] acceptHeaders = {"test/string2"};
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
		assertEquals(1,methodInvoked);

	}
	
	@Test
	public void acceptHeaderBasedMethodSelectionWithQ() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		String[] acceptHeaders = {"test/string3;q=.7","test/string2;q=.9","*/*;q=.1"};
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
		assertEquals(1,methodInvoked);

	}
	
	@Test
	public void acceptHeaderBasedMethodSelectionWithQ2() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		String[] acceptHeaders = {"test/string3","test/string2;q=.9","*/*;q=.1"};
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
		assertEquals(2,methodInvoked);

	}
	
	@Test
	public void acceptHeaderBasedMethodSelectionWithQ3() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		String[] acceptHeaders = {"foo/bar"};
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
		assertEquals(3,methodInvoked);

	}
	@Test
	public void acceptHeaderBasedMethodSelectionWithQ4() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		String[] acceptHeaders = {"test/string6","test/string2;q=.9","*/*;q=.1"};
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
		assertEquals(4,methodInvoked);

	}
}

