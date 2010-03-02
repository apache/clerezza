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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.delegate.RuntimeDelegateImpl;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.util.MessageBody2Write;

/**
 *
 * @author mir
 */
public class FormParamTest {

	@Before
	public void setUp() {
		RuntimeDelegateImpl.setInstance(new RuntimeDelegateImpl());
	}
	
	@Ignore
	@Path("/test-resource")
	public static class TestResourceForDefaultValue {

		static String value;
	   
		@POST
		@Path("setForm")
		public void setForm(@FormParam(value="value") String value) {
			
			TestResourceForDefaultValue.value = value;
		}
	}
	@Test
	public void testFormParamInjectionIntoMethod() throws Exception {		
		JaxRsHandler handler = HandlerCreator.getHandler(TestResourceForDefaultValue.class);
		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/test-resource/setForm");
		String[] values = new String[1];
		values[0] = "application/x-www-form-urlencoded";
		request.setHeader(HeaderName.CONTENT_TYPE, values);
		request.setMessageBody(new MessageBody2Write() {

							@Override
							public void writeTo(WritableByteChannel cout)
									throws IOException {
							   
								OutputStream out = Channels.newOutputStream(cout);
								String str = new String("value=foo+bar");
								out.write(str.getBytes());
							}
						});		
		request.setRequestURI(uri);
		request.setMethod(Method.POST);		
		Response response = new ResponseImpl();
		handler.handle(request, response);
		assertEquals("foo bar", TestResourceForDefaultValue.value);
	}  
}