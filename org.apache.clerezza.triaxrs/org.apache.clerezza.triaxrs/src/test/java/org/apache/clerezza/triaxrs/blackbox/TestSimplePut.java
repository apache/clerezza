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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.util.MessageBody2Write;


public class TestSimplePut {

	static String receivedBody;

	@Path("/")
	public static class MyResource {

		@PUT
		public void handlePut(String body) {
			receivedBody = body;
		}
	}

	@Test
	public void performPut() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.PUT).anyTimes();
		String[] contentTypeHeader = {"text/plain"};
		expect(requestMock.getHeaderValues(HeaderName.CONTENT_TYPE)).andReturn(contentTypeHeader).anyTimes();
		//this redundancy makes me prefer not to use mocks
		Set<HeaderName> headerNames = new HashSet<HeaderName>();
		headerNames.add(HeaderName.CONTENT_TYPE);
		expect(requestMock.getHeaderNames()).andReturn(headerNames).anyTimes();
		final String message = "The message in the body";
		expect(requestMock.getMessageBody()).andReturn(new MessageBody2Write() {

			@Override
			public void writeTo(WritableByteChannel out) throws IOException {
				out.write(ByteBuffer.wrap(message.getBytes()));
			}
		});
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		Assert.assertEquals(message, receivedBody);

	}
}

