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

import java.util.List;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.easymock.EasyMock;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.blackbox.resources.ConversionTst;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;

public class TestQueryParam {

	static String handleGetParamValue;
	static List<String> handleGetParamValue1;
	static List<ConversionTst> handleGetParamValue2;

	@Path("/foo")
	public static class MyResource {

		@GET
		public void handleGet(@DefaultValue("default") @QueryParam("key") String param) {
			handleGetParamValue = param;
		}
	}

	@Path("/foo")
	public static class MyResource2 {

		@GET
		public void handleGet(
				@DefaultValue("default1") @QueryParam("key1") List<String> param1,
				@DefaultValue("default2") @QueryParam("key2") List<ConversionTst> param2) {
			handleGetParamValue1 = param1;
			handleGetParamValue2 = param2;
		}
	}

	@Test
	public void testQueryParam() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/foo").anyTimes();
		expect(requestURI.getQuery()).andReturn("key=value").anyTimes();
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertEquals("value", handleGetParamValue);

	}

	@Test
	public void testQueryParamWithSpace() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);

		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/foo").anyTimes();
		expect(requestURI.getQuery()).andReturn("key=foo+bar").anyTimes();
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertEquals("foo bar", handleGetParamValue);

	}

	@Test
	public void testMultiValueQueryParam() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource2.class);

		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/foo").anyTimes();
		expect(requestURI.getQuery())
				.andReturn("key1=value1&key1=value2&key2=value3&key2=value4")
				.anyTimes();
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertEquals("value1", handleGetParamValue1.get(0));
		assertEquals("value2", handleGetParamValue1.get(1));
		assertEquals(2, handleGetParamValue1.size());
		assertEquals("value3", handleGetParamValue2.get(0).getValue());
		assertEquals("value4", handleGetParamValue2.get(1).getValue());
		assertEquals(2, handleGetParamValue2.size());
	}
}
