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
import javax.ws.rs.POST;
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

public class EmptyPathSubResourceLocaterTest {

	private static String value = null;

	@Path("foo")
	public static class MyRootResource {

		@Path("{path:.*}")
		public MySubResource getSubResource() {
			return new MySubResource();
		}
	}

	public static class MySubResource {

		@GET
		public void get() {
			value = "subGet";
		}

		@POST
		public void post() {
			value = "subPost";
		}

	}

	@Before
	public void before() {
		value = null;
	}

	@Test
	public void nonEmptySubResourcePath() throws Exception{
		JaxRsHandler handler = HandlerCreator.getHandler(MyRootResource.class, MySubResource.class);
		RequestURIImpl uri = new RequestURIImpl();
		RequestImpl request = new RequestImpl();
		ResponseImpl response = new ResponseImpl();

		uri.setPath("foo/sub");
		request.setRequestURI(uri);
		request.setMethod(Method.GET);

		handler.handle(request, response);

		Assert.assertEquals("subGet", value);
	}

	@Test
	public void emptySubResourcePath() throws Exception{
		JaxRsHandler handler = HandlerCreator.getHandler(MyRootResource.class, MySubResource.class);
		RequestURIImpl uri = new RequestURIImpl();
		RequestImpl request = new RequestImpl();
		ResponseImpl response = new ResponseImpl();

		uri.setPath("foo");
		request.setRequestURI(uri);
		request.setMethod(Method.POST);

		handler.handle(request, response);

		Assert.assertEquals("subPost", value);
	}

}
