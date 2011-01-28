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
package org.apache.clerezza.jaxrs.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.jaxrs.testutils.TestWebServer;

/**
 * @author mir
 *
 */
public class TrailingSlashTest {

	private String path;
	
	@Path("/foo")
	public class MyResource {
		
		@Path("bar")
		@GET
		public void myMethod(@Context UriInfo uriInfo){
			TrailingSlash.enforcePresent(uriInfo);
			path = uriInfo.getAbsolutePath().toString();
		}
	}
	
	@Path("/one")
	public class MyResource2 {
		
		@Path("two")
		@GET
		public void myMethod(@Context UriInfo uriInfo){
			TrailingSlash.enforceNotPresent(uriInfo);
			path = uriInfo.getAbsolutePath().toString();
		}
	}	
	
	@Test
	public void testEnforceSlash() throws IOException {		
		final TestWebServer testWebServer = createTestWebServer(new MyResource());
		int port = testWebServer.getPort();
		URL serverURL = new URL("http://localhost:" + port + "/foo/bar");
		HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
		connection = (HttpURLConnection) serverURL.openConnection();
		connection.setRequestMethod("GET");
		connection.addRequestProperty("Accept", "text/html, */*; q=.2");	
		Assert.assertEquals(204, connection.getResponseCode());
		Assert.assertTrue(path.endsWith("/"));
		testWebServer.stop();
	}
	
	@Test
	public void testEnforceNoSlash() throws IOException {
		final TestWebServer testWebServer = createTestWebServer(new MyResource2());
		int port = testWebServer.getPort();
		URL serverURL = new URL("http://localhost:" + port + "/one/two/");
		HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
		connection = (HttpURLConnection) serverURL.openConnection();
		connection.setRequestMethod("GET");

		connection.addRequestProperty("Accept", "text/html, */*; q=.2");
		Assert.assertEquals(204, connection.getResponseCode());
		Assert.assertFalse(path.endsWith("/"));
		testWebServer.stop();
	}
	
	
	private TestWebServer createTestWebServer(final Object resource) {
		return new TestWebServer(new Application() {

			@Override
			public Set<Class<?>> getClasses() {
				Set<Class<?>> result = new HashSet<Class<?>>();
				return result;
			}
			
			@Override
			public Set<Object> getSingletons() {
				Set<Object> result = new HashSet<Object>();
				result.add(resource);
				return result;
			}
		});
	}

	
}
