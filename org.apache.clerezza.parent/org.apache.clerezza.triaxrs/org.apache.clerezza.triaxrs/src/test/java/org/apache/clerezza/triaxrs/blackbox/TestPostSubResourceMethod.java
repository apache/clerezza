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
import static org.junit.Assert.assertTrue;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.easymock.EasyMock;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;


public class TestPostSubResourceMethod {

	static boolean methodInvokedForPost = false;

	@Path("/")
	public static class MyResource {

		@GET
		@Path("path")
		public void handleGet() {
			throw new RuntimeException("this should not be invoked");
		}

		@POST
		@Path("path")
		public void handlePost() {
			methodInvokedForPost = true;
		}
	}

	@Test
	public void testOptions() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);

		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.POST).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/path");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		expect(requestURI.getType()).andReturn(null).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertTrue(methodInvokedForPost);

	}
}

