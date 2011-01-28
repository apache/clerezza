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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.delegate.RuntimeDelegateImpl;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testResource.TestSubResource;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.util.MessageBody2Write;

/**
 *
 * @author mir
 */
public class DefaultValueAnnotationTest {

	@Before
	public void setUp() {
		RuntimeDelegateImpl.setInstance(new RuntimeDelegateImpl());
	}
	
	@Ignore
	@Path("/test-resource")
	public static class TestResourceForDefaultValue {

		static String matrix;
		static Object queryParam;
		static String username;
		static String password;
		static Cookie cookie;
		static String date;
		static String pathParam;
		static String id;

	   
		@POST
		@Path("setForm")
		public void setForm(@DefaultValue(value = "defaultFP1") @FormParam(value="username") String username,
				@DefaultValue(value = "defaultFP2")@FormParam(value="password") String password) {
			
			TestResourceForDefaultValue.username = username;
			TestResourceForDefaultValue.password = password;
		}
		
		@Path("subresource")
		public TestSubResource findSubResource(@QueryParam("id") String id){
			
			TestSubResource r = new TestSubResource();
			r.setContent(id);
			return r;
		}

		@Path("addheader")
		@GET
		public void setHeader(@DefaultValue(value="1.1.1970") @HeaderParam("expires") String date) {
			TestResourceForDefaultValue.date = date;
		}
		
		@Path("addcookie")
		@GET
		public void setCookie(@DefaultValue(value="defaultCookie=foobar")
				@CookieParam(value="name") Cookie cookie) {
			TestResourceForDefaultValue.cookie = cookie;
		}
		
		@Path("path/{id}")
		@GET
		public void setPathParam(@DefaultValue(value="123") @PathParam("id")
				String pathParam){
			TestResourceForDefaultValue.pathParam = pathParam;
		}
		
		@POST
		@Path("add")
		public void setMatrix(@DefaultValue(value = "defaultMP") @MatrixParam(value = "scale")
				String matrixParam, @DefaultValue(value = "defaultQP")
				@QueryParam(value = "test") String testValue) {
			matrix = matrixParam;
			queryParam = testValue;
		}

		@GET
		@Path("getId")
		public void setMatrix(@DefaultValue(value = "123") @QueryParam(value = "id") String idParam) {
			id = idParam;
		}
		
	}// $Log: $
	
	@Path("/field")
	public static class MyFieldRessource {
		@DefaultValue(value="defaultFV")
		@QueryParam("value")
		public static String fieldValue;
		
		@GET
		public void function(){
		}
	}
	
	@Path("/constr")
	public static class MyConstructorRessource {
		static String constructorValue;
		
		public MyConstructorRessource(@DefaultValue(value="defaultCV")
				@HeaderParam("referer") String constructorValue){
			MyConstructorRessource.constructorValue =constructorValue;
		}
		
		@GET
		public void function(){
		}
	}
	
	@Test
	public void testPathParamInjectionIntoMethod() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(TestResourceForDefaultValue.class);

		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/test-resource/path/");

		request.setRequestURI(uri);
		request.setMethod(Method.GET);

		Response response = new ResponseImpl();
		handler.handle(request, response);

		assertNotNull(TestResourceForDefaultValue.pathParam);
		assertEquals("123", TestResourceForDefaultValue.pathParam);
	}

	
	@Test
	public void testMatrixParamInjectionIntoMethod() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(TestResourceForDefaultValue.class);

		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/test-resource/add");
		//uri.setQuery(";lat=50;long=20");
		request.setRequestURI(uri);
		request.setMethod(Method.POST);

		Response response = new ResponseImpl();
		handler.handle(request, response);

		assertNotNull(TestResourceForDefaultValue.matrix);
		assertEquals("defaultMP", TestResourceForDefaultValue.matrix);
	}

	
	@Test
	public void testQueryParamInjectionIntoMethodPost() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(TestResourceForDefaultValue.class);

		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/test-resource/add");
		uri.setQuery("test2=mySecondValue");
		request.setRequestURI(uri);
		request.setMethod(Method.POST);


		Response response = new ResponseImpl();
		handler.handle(request, response);

		assertNotNull(TestResourceForDefaultValue.queryParam);
		assertEquals("defaultQP", TestResourceForDefaultValue.queryParam.toString());
	}

	@Test
	public void testQueryParamInjectionIntoMethodGet() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(TestResourceForDefaultValue.class);

		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/test-resource/getId");
		request.setRequestURI(uri);
		request.setMethod(Method.GET);

		Response response = new ResponseImpl();
		handler.handle(request, response);

		assertNotNull(TestResourceForDefaultValue.id);
		assertEquals("123", TestResourceForDefaultValue.id);
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
								String str = new String("foo=test&bar=pwd");
								out.write(str.getBytes());
							}
						});
		
		request.setRequestURI(uri);
		request.setMethod(Method.POST);
		
		Response response = new ResponseImpl();
		handler.handle(request, response);

		assertEquals("defaultFP1", TestResourceForDefaultValue.username);
		assertEquals("defaultFP2", TestResourceForDefaultValue.password);
	}
	
  
	@Test
	public void testCookieParamInjectionIntoMethod() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(TestResourceForDefaultValue.class);
		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/test-resource/addcookie");
		String[] values = new String[2];
		values[0] = "xyz=22eueu.uo";
		values[1] = "abc=1231241";
		request.setHeader(HeaderName.COOKIE, values);
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		Response response = new ResponseImpl();
		handler.handle(request, response);
		
		assertEquals("defaultCookie", TestResourceForDefaultValue.cookie.getName());
		assertEquals("foobar", TestResourceForDefaultValue.cookie.getValue());
	}
	

	
	@Test
	public void testHeaderParamInjectionIntoMethod() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(TestResourceForDefaultValue.class);
		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/test-resource/addheader");
		String[] values = new String[1];
		values[0] = "6.10.2012";
		request.setHeader(HeaderName.LAST_MODIFIED, values);
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		Response response = new ResponseImpl();
		handler.handle(request, response);
		
		assertEquals("1.1.1970", TestResourceForDefaultValue.date);
	}	
	
	@Test
	public void testInjectionIntoField() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyFieldRessource.class);
		
		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/field");
		uri.setQuery("wert=MyValue");
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		Response response = new ResponseImpl();
		handler.handle(request, response);
		
		assertEquals("defaultFV", MyFieldRessource.fieldValue);
	}
	
	@Test
	public void testInjectionIntoConstructor() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler(MyConstructorRessource.class);

		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/constr");
 		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		String[] values = new String[1];
		values[0] = "http://example.com";
		request.setHeader(HeaderName.HOST, values);
		Response response = new ResponseImpl();
		handler.handle(request, response);
		
		assertEquals("defaultCV", MyConstructorRessource.constructorValue);
	}
}