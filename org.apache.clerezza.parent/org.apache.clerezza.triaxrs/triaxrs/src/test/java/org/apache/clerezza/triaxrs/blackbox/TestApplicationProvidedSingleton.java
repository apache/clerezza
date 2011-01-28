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

import java.util.Collections;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;


public class TestApplicationProvidedSingleton {

	boolean methodInvokedForGet = false;
	int initCount = 0;

	@Path("/")
	public class MyResource {
		
		public MyResource() {
			initCount++;
		}

		@GET
		public void handleGet() {
			methodInvokedForGet = true;
		}
	}

	@Test
	public void testOptions() throws Exception {
		
		final Application applicationConfig = new Application() {

			@Override
			public Set<Class<?>> getClasses() {
				Set<Class<?>> result = new HashSet<Class<?>>();
				return result;
			}

			@Override
			public Set<Object> getSingletons() {
				return Collections.singleton((Object)new MyResource());
			}
				
		};
		
		JaxRsHandler handler = new JaxRsHandler() {

			{ //this code is in an initializer to be able to call protected methods	
				registerApplicationConfig(applicationConfig, "");
			}
		};

		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertTrue(methodInvokedForGet);
		Assert.assertEquals(1, initCount);

	}
}

