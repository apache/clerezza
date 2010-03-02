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
package org.apache.clerezza.jaxrs.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import org.junit.Assert;
import org.junit.Test;

/**
 * This tests the {@link TestWebServer} and also demonstrates how it is
 * intended to be use (instead of MyResource you use the resource(s) to be
 * tested.
 *
 * @author reto
 */
public class TestTestWebServer {
	

	@Path("/")
	public static class MyResource {

		@GET
		public byte[] handleGet() {
			byte[] result = {1,2};
			return result;
		}
	}

	@Test
	public void testWebServerRuns() throws IOException {
		TestWebServer testWebServer = new TestWebServer(new Application() {

			@Override
			public Set<Class<?>> getClasses() {
				Set<Class<?>> result = new HashSet<Class<?>>();
				result.add(MyResource.class);
				return result;
			}

		});
		int port = testWebServer.getPort();
		URL serverURL = new URL("http://localhost:"
				+ port + "/");
		URLConnection connection = serverURL.openConnection();
		//here we can do things like:
		connection.addRequestProperty("Accept", "application/x-test");
		//now we actually open the connection
		InputStream responseStream = connection.getInputStream();
		Assert.assertEquals(1, responseStream.read());
		Assert.assertEquals(2, responseStream.read());
		testWebServer.stop();
		
	}
}
