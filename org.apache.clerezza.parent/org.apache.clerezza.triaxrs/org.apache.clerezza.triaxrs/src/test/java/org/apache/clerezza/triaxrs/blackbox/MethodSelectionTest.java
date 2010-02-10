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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.util.MessageBody2Read;

public class MethodSelectionTest {

	private static String value = null;
	private static String body = null;

	@Path("foo")
	public static class MyRootResource {

		@POST
		public void post1(@QueryParam("name") String name, String st) {
			value = name;
			body = st;
		}
		
		@POST
		public void post2(@Context UriInfo uriInfo) {
			value = "post2";
		}
	}

	@Before
	public void before() {
		value = null;
		body = null;
	}

	@Test
	public void tesPost1() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyRootResource.class);
		RequestURIImpl uri = new RequestURIImpl();
		RequestImpl request = new RequestImpl();
		ResponseImpl response = new ResponseImpl();

		String queryParam = "post1";
		final String bodyString = "body";
		uri.setPath("foo");
		uri.setQuery("name="+queryParam);
		request.setRequestURI(uri);
		String[] headervalues = new String[1];
		headervalues[0]="text/plain";
		request.setHeader(HeaderName.CONTENT_TYPE, headervalues);
		MessageBody messageBody = new MessageBody2Read() {

			@Override
			public ReadableByteChannel read() throws IOException {
				return Channels.newChannel(new ByteArrayInputStream(bodyString.getBytes()));
			}
		};

		request.setMessageBody(messageBody);

		request.setMethod(Method.POST);

		handler.handle(request, response);

		Assert.assertEquals(queryParam, value);
		Assert.assertEquals(bodyString, body);
	}

	@Test
	public void testPost2() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyRootResource.class);
		RequestURIImpl uri = new RequestURIImpl();
		RequestImpl request = new RequestImpl();
		ResponseImpl response = new ResponseImpl();

		uri.setPath("foo");
		request.setRequestURI(uri);
		request.setMethod(Method.POST);

		handler.handle(request, response);

		Assert.assertEquals("post2", value);
		Assert.assertNull(body);
	}

	@Test
	public void tesPost3() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyRootResource.class);
		RequestURIImpl uri = new RequestURIImpl();
		RequestImpl request = new RequestImpl();
		ResponseImpl response = new ResponseImpl();

		String queryParam = "post1";
		uri.setPath("foo");
		uri.setQuery("name="+queryParam);
		request.setRequestURI(uri);

		request.setMethod(Method.POST);

		handler.handle(request, response);

		Assert.assertEquals("post2", value);
		Assert.assertEquals(null, body);
	}
}