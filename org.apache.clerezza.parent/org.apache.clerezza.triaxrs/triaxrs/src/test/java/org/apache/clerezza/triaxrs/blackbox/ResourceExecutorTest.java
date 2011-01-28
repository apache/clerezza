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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.easymock.EasyMock;
import org.junit.Test;
import org.apache.clerezza.jaxrs.extensions.MethodResponse;
import org.apache.clerezza.jaxrs.extensions.ResourceMethodException;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.RootResourceExecutorImpl;
import org.apache.clerezza.triaxrs.WebRequest;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;

/**
 * 
 * @author mir
 *
 */
public class ResourceExecutorTest {

	private static JaxRsHandler handler;
	static boolean methodInvoked1 = false;
	static boolean methodInvoked2 = false;
	static boolean methodInvoked3 = false;
	
	static String prevSegParamThree;
	static String idThree;

	@Path("/foobar")
	public static class MyResource {

		@GET
		public MethodResponse handleOnOtherResource(@Context WebRequest request, @Context UriInfo uriInfo) throws ResourceMethodException {
			Object resource = new MyResource2();
			
			MethodResponse methodResponse = null;
	
			Map<String, String> parameters = new HashMap<String, String>();
			new RootResourceExecutorImpl().execute(request, resource, "", parameters);

			parameters = new HashMap<String, String>();
			new RootResourceExecutorImpl().execute(request, resource, "two", parameters);

			parameters = new HashMap<String, String>();
			parameters.put("paramOfPrevSegment", "paramXY");
			new RootResourceExecutorImpl().execute(request, resource,
					"three/myID", parameters);

			parameters = new HashMap<String, String>();
			methodResponse = new RootResourceExecutorImpl().execute(request, resource, "four", parameters);

			return methodResponse;
		}
	}
	
	@Path("/otherresource")
	public static class MyResource2 {

		@GET
		public void handlerMethodOne() {
			methodInvoked1 = true;
		}
		
		@GET
		@Path("two")
		public void handlerMethodTwo() {
			methodInvoked2 = true;
		}
		
		@GET
		@Path("three/{id}")
		public void handlerMethodThree(@PathParam("paramOfPrevSegment") String segParam, @PathParam("id") String id) {
			methodInvoked3 = true;
			prevSegParamThree = segParam;
			idThree = id;
		}
		
		@GET
		@Path("four")
		public String handlerMethodFour(@Context UriInfo uriInfo) {
			return "Welcome to " + uriInfo.getPath();
		}
	}

	@Test
	public void testHandleOnResource() throws Exception {

		handler = HandlerCreator.getHandler(MyResource.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/foobar").anyTimes();
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);

		ResponseImpl responseImpl = new ResponseImpl();
		handler.handle(requestMock, responseImpl);
		responseImpl.consumeBody();
		assertTrue(methodInvoked1);
		assertTrue(methodInvoked2);
		assertTrue(methodInvoked3);
		
		assertEquals(prevSegParamThree, "paramXY");
		assertEquals(idThree, "myID");
				
		assertArrayEquals("Welcome to /foobar".getBytes(), responseImpl.getBodyBytes());
	}
}

