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
package org.apache.clerezza.triaxrs.blackbox.context;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Response;

/**
 *
 * @author reto
 */
public class TestHeadersInjection {
	static boolean methodInvokedForGet = false;
	static String headervalue = null;

	@Path("/")
	public static class MyResource {

		@GET
		public void handleGet(@Context HttpHeaders headers) {
			methodInvokedForGet = true;
			List<String>values = headers.getRequestHeader("TeSThEAder");

			headervalue = values.get(0);
		}
	}

	@Test
	public void testOptions() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/");
		String[] headervalues = new String[1];
		headervalues[0] = "testheadervalue";
		String headerName = "tEstHeaDER";
		request.setHeader(HeaderName.get(headerName), headervalues);
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		Response response = new ResponseImpl();
		handler.handle(request, response);

		assertTrue(methodInvokedForGet);
		assertEquals(headervalues[0], headervalue);

	}

}
