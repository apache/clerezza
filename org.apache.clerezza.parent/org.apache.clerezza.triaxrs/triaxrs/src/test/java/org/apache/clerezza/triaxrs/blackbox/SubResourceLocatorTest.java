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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wymiwyg.wrhapi.Method;

public class SubResourceLocatorTest {

	private static String value = null;

	@Path("")
	public static class MyRootResource {

		@GET
		public void get() {
			value = "get";
		}

		@Path("sub")
		public MySubResource getSubResource() {
			return new MySubResource();
		}
	}

	public static class MySubResource {

		@GET
		public void get() {
			value = "get2";
		}

		@GET
		@Path("sub")
		public void get2() {
			value = "subGet";
		}

	}

	@Before
	public void before() {
		value = null;
	}

	@Test
	public void testGetOnMySubResource() throws Exception{
		JaxRsHandler handler = HandlerCreator.getHandler(MyRootResource.class, MySubResource.class);
		RequestURIImpl uri = new RequestURIImpl();
		RequestImpl request = new RequestImpl();
		ResponseImpl response = new ResponseImpl();

		uri.setPath("sub");
		request.setRequestURI(uri);
		request.setMethod(Method.GET);

		handler.handle(request, response);

		Assert.assertEquals("get2", value);
	}

	@Test
	public void testGetOnMyRootResource() throws Exception{
		JaxRsHandler handler = HandlerCreator.getHandler(MyRootResource.class, MySubResource.class);
		RequestURIImpl uri = new RequestURIImpl();
		RequestImpl request = new RequestImpl();
		ResponseImpl response = new ResponseImpl();

		uri.setPath("");
		request.setRequestURI(uri);
		request.setMethod(Method.GET);

		handler.handle(request, response);

		Assert.assertEquals("get", value);
	}

	@Test
	public void testSubMethodOnMySubResource() throws Exception{
		JaxRsHandler handler = HandlerCreator.getHandler(MyRootResource.class, MySubResource.class);
		RequestURIImpl uri = new RequestURIImpl();
		RequestImpl request = new RequestImpl();
		ResponseImpl response = new ResponseImpl();

		uri.setPath("sub/sub");
		request.setRequestURI(uri);
		request.setMethod(Method.GET);

		handler.handle(request, response);

		Assert.assertEquals("subGet", value);
	}

}
