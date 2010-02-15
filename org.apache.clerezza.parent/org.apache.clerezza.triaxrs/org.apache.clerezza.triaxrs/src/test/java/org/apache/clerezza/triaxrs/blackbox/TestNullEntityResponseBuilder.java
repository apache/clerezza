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

import static org.easymock.EasyMock.*;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response.Status;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.delegate.RuntimeDelegateImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;

/**
 *
 * @author cwilding
 */
public class TestNullEntityResponseBuilder {

    public TestNullEntityResponseBuilder() {
    }

	@Test
	public void testStatusCreated() {
		Assert.assertEquals(new RuntimeDelegateImpl().createResponseBuilder()
				.status(Status.CREATED).build().getStatus(), Status.CREATED.getStatusCode());
	}

	@Test
	public void testNullEntity() {
		Assert.assertEquals(new RuntimeDelegateImpl().createResponseBuilder()
				.status(Status.CREATED).build().getEntity(), null);
	}

	@Path("/")
	public static class MyResource {

		@PUT
		public javax.ws.rs.core.Response handlePut() {
			return new RuntimeDelegateImpl().createResponseBuilder().status(Status.CREATED).build();
		}
	}

	@Test
	public void testResponseCode() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(new MyResource());
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		responseMock.setResponseStatus(ResponseStatus.CREATED);
		expect(requestMock.getMethod()).andReturn(Method.PUT).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		verify(responseMock);

	}
}