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
package org.apache.clerezza.triaxrs.blackbox.jaf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.activation.DataHandler;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.blackbox.jaf.resources.JafSerializableObj;
import org.apache.clerezza.triaxrs.blackbox.jaf.resources.MyDchFactory;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.util.MessageBody2Read;

/**
 * 
 * @author mir
 */
public class TestJafProvider {

	@Path("/")
	public static class MyResource {
		static JafSerializableObj myTestobj;

		@GET
		@Consumes("application/testobj")
		public void getTestObj(JafSerializableObj tobj) {
			myTestobj = tobj;
		}

		@GET
		@Produces("application/testobj")
		public JafSerializableObj createTestObj() {
			return new JafSerializableObj("Object", "Test");
		}

	}

	
	public void setup() {
		DataHandler.setDataContentHandlerFactory(new MyDchFactory());
	}
	
	@Test
	public void testJAFProviderBodyReader() throws Exception {
		setup();
		
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
		
		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/");
		String[] headervalues = new String[1];
		headervalues[0] = "application/testobj";
		request.setHeader(HeaderName.CONTENT_TYPE, headervalues);

		JafSerializableObj testObj = new JafSerializableObj("foo", "bar");
		
		// Serialize testObj
		final ByteArrayOutputStream bous = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bous);
		out.writeObject(testObj);
		out.close();
		MessageBody body = new MessageBody2Read() {

			@Override
			public ReadableByteChannel read() throws IOException {
				return Channels.newChannel(new ByteArrayInputStream(bous.toByteArray()));
			}

		};
		
		request.setMessageBody(body);
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		Response response = new ResponseImpl();
		handler.handle(request, response);
		assertTrue(MyResource.myTestobj != null);
		assertEquals("foo", MyResource.myTestobj.getField1());
		assertEquals("bar", MyResource.myTestobj.getField2());
	}

	@Test
	public void testJAFProviderBodyWriter() throws Exception {
		
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);

		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/");
		String[] headervalues = new String[1];
		headervalues[0] = "application/testobj";
		request.setHeader(HeaderName.ACCEPT, headervalues);
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		ResponseImpl response = new ResponseImpl();
		handler.handle(request, response);
		response.consumeBody();
		
        JafSerializableObj testobj = null;
        
        //deserialize object
        try {
			InputStream is = new ByteArrayInputStream(response.getBodyBytes());
			ObjectInput oi = new ObjectInputStream(is);
			testobj = (JafSerializableObj) oi.readObject();
			oi.close();
		} catch (ClassNotFoundException e) {
			System.out.println("TestJAFProvider: " + e.toString());
			assertTrue(false);
		} catch (StreamCorruptedException e) {
			System.out.println("TestJAFProvider: " + e.toString());
			e.printStackTrace();
			assertTrue(false);
		}

		assertEquals("Object", testobj.getField1());
		assertEquals("Test", testobj.getField2());
	}
}
