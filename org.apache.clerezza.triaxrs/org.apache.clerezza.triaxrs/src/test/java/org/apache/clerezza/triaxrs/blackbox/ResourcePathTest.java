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

import static org.junit.Assert.assertTrue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.junit.Before;
import org.wymiwyg.wrhapi.Method;

public class ResourcePathTest {

	static boolean methodInvokedForGet = false;

	@Path("foo")
	public static class MyResource {

		@GET
		public void handleGet() {
			methodInvokedForGet = true;
		}

		@Path("bar%20foo")
		@GET
		public void handleGet2() {
			methodInvokedForGet = true;
		}

		@Path("da ja")
		@GET
		public void handleGet3() {
			methodInvokedForGet = true;
		}
	}

	@Path("test%20resource")
	public static class MyResource2 {

		@GET
		public void handleGet() {
			methodInvokedForGet = true;
		}
	}

	@Path("bla bla")
	public static class MyResource3 {

		@GET
		public void handleGet() {
			methodInvokedForGet = true;
		}
	}

	@Path("test+resource")
	public static class MyResource4 {

		@GET
		public void handleGet() {
			methodInvokedForGet = true;
		}
	}

	@Before
	public void reset() {
		methodInvokedForGet = false;
	}

	@Test
	public void requestOnResourcePathTest() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		RequestImpl requestMock = new RequestImpl();
		RequestURIImpl requestUri = new RequestURIImpl();
		requestUri.setPath("/foo");
		requestMock.setRequestURI(requestUri);
		requestMock.setMethod(Method.GET);
		handler.handle(requestMock, new ResponseImpl());
		assertTrue(methodInvokedForGet);
	}

	@Test
	public void requestOnResourcePathContainingEncodedSpaceTest() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource2.class);
		RequestImpl requestMock = new RequestImpl();
		RequestURIImpl requestUri = new RequestURIImpl();
		requestUri.setPath("/test%20resource");
		requestMock.setRequestURI(requestUri);
		requestMock.setMethod(Method.GET);
		handler.handle(requestMock, new ResponseImpl());
		assertTrue(methodInvokedForGet);
	}

	@Test
	public void requestOnResourcePathContainingPlusTest() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource4.class);
		RequestImpl requestMock = new RequestImpl();
		RequestURIImpl requestUri = new RequestURIImpl();
		requestUri.setPath("/test+resource");
		requestMock.setRequestURI(requestUri);
		requestMock.setMethod(Method.GET);
		handler.handle(requestMock, new ResponseImpl());
		assertTrue(methodInvokedForGet);
	}

	@Test
	public void requestOnResourcePathContainingSpaceTest() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource3.class);
		RequestImpl requestMock = new RequestImpl();
		RequestURIImpl requestUri = new RequestURIImpl();
		requestUri.setPath("/bla%20bla");
		requestMock.setRequestURI(requestUri);
		requestMock.setMethod(Method.GET);
		handler.handle(requestMock, new ResponseImpl());
		assertTrue(methodInvokedForGet);
	}

	@Test
	public void reqOnResMethodPathContainingEncSpaceTest() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		RequestImpl requestMock = new RequestImpl();
		RequestURIImpl requestUri = new RequestURIImpl();
		requestUri.setPath("/foo/bar%20foo");
		requestMock.setRequestURI(requestUri);
		requestMock.setMethod(Method.GET);
		handler.handle(requestMock, new ResponseImpl());
		assertTrue(methodInvokedForGet);
	}

	@Test
	public void reqOnResMethodPathContainingSpaceTest() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		RequestImpl requestMock = new RequestImpl();
		RequestURIImpl requestUri = new RequestURIImpl();
		requestUri.setPath("/foo/da%20ja");
		requestMock.setRequestURI(requestUri);
		requestMock.setMethod(Method.GET);
		handler.handle(requestMock, new ResponseImpl());
		assertTrue(methodInvokedForGet);
	}
}
