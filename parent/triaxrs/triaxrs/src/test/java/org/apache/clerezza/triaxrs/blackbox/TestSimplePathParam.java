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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.easymock.EasyMock;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;


public class TestSimplePathParam {

	static String handlePathParamValue;

	@Path("prefix")
	public static class MyResource {

		@GET
		public void handleGet() {
			handlePathParamValue = "no-sub";
		}

		@GET
		@Path("{value:.+}")
		public void handleSubPath() {
			handlePathParamValue = "sub";
		}
	}

	@Test
	public void testQueryParam() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		String value = "a/path/with/slashes";
		expect(requestURI.getPath()).andReturn("/prefix/"+value).anyTimes();
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertEquals("sub", handlePathParamValue);

	}

	@Test
	public void testNoSlashAfterClassPaths() throws Exception {

		handlePathParamValue = null;
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);

		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/prefixwithotherstuff/bla/bla").anyTimes();
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertEquals(null, handlePathParamValue);

	}
}

