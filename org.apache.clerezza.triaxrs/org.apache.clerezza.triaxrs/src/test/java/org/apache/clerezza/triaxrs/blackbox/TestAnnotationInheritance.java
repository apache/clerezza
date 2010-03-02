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
import static org.junit.Assert.assertTrue;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;


import org.easymock.EasyMock;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;

/**
*
* @author mir
*/
public class TestAnnotationInheritance {

	static boolean handleGetInvoked = false;
	static boolean superHandleGetInvoked = false;
	
	public static interface MyInterface {
		@GET
		@Path("interface")
		public void handleGet();
	}
	
	public static class MySuperClass {	
		@GET
		@Path("superclass")
		public void handleGet() {
			superHandleGetInvoked = true;
		}
	}
	
	@Path("/")
	public static class MyResource implements MyInterface{
		
		public void handleGet() {
			handleGetInvoked = true;
		}
	}
	
	@Path("/")
	public static class MyResource2 extends MySuperClass{

		@Override
		public void handleGet() {
			handleGetInvoked = true;
		}
		

	}
	
	@Path("/")
	public static class MyResource3 extends MySuperClass implements MyInterface {

		public void handleGet() {
			handleGetInvoked = true;
		}

	}
	
	@Path("/")
	public static class MyResource4 implements MyInterface {

		@PUT
		@Path("resource")
		@Override
		public void handleGet() {
			handleGetInvoked = true;
		}
	}
	
	@Path("/")
	public static class MyResource5 extends MySuperClass {
		
		@PUT
		@Path("resource")
		@Override
		public void handleGet() {
			handleGetInvoked = true;
		}
	}	

	/**
	 *  The following specification requirement will be tested:
	 *  JAX-RS annotations MAY be used on the methods of an implemented interface.
	 *  Such annotations are inherited by a corresponding implementation class method
	 *  provided that method does not have any of its own JAX-RS annotations.
	 */
	@Test
	public void testInterfaceAnnotation() throws Exception {

		reset();
		
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);

		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/interface");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertTrue(handleGetInvoked);

	}
	
	/**
	 *  The following specification requirement will be tested:
	 *  JAX-RS annotations MAY be used on the methods of a super-class.
	 *  Such annotations are inherited by a corresponding sub-class method
	 *  provided that method does not have any of its own JAX-RS annotations.
	 */
	@Test
	public void testSuperClassAnnotation() throws Exception {

		reset();
		
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource2.class);

		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/superclass");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertTrue(handleGetInvoked && !superHandleGetInvoked);

	}
	
	/**
	 *  The following specification requirement will be tested:
	 *  Annotations on a super-class take precedence over those on an implemented 
	 *  interface.
	 */
	@Test
	public void testSuperClassPrecedence() throws Exception {

		reset();
		
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource3.class);
	
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/superclass");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertTrue(handleGetInvoked & !superHandleGetInvoked);
		
		reset();
		
		Request requestMock2 = EasyMock.createNiceMock(Request.class);
		Response responseMock2 = EasyMock.createNiceMock(Response.class);
		expect(requestMock2.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI2 = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI2.getPath()).andReturn("/interface");
		expect(requestMock2.getRequestURI()).andReturn(requestURI2).anyTimes();
		replay(requestMock2);
		replay(requestURI2);
		replay(responseMock2);
		handler.handle(requestMock2, responseMock2);
		assertTrue(!handleGetInvoked && !superHandleGetInvoked);
	}
	
	/**
	 *  The following specification requirement will be tested:
	 *  If a implementation method has any JAX-RS annotations then all 
	 *  of the annotations on interface method are ignored.
	 */
	@Test
	public void testClassIgnorsInterface() throws Exception {

		reset();
		
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource4.class);
		
		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.PUT).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/resource");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		assertTrue(handleGetInvoked);
		
		reset();
		
		Request requestMock2 = EasyMock.createNiceMock(Request.class);
		Response responseMock2 = EasyMock.createNiceMock(Response.class);
		expect(requestMock2.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI2 = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI2.getPath()).andReturn("/interface");
		expect(requestMock2.getRequestURI()).andReturn(requestURI2).anyTimes();
		replay(requestMock2);
		replay(requestURI2);
		replay(responseMock2);
		handler.handle(requestMock2, responseMock2);
		assertTrue(!handleGetInvoked);
	}
	
	/**
	 *  The following specification requirement will be tested:
	 *  If a subclass method has any JAX-RS annotations then all 
	 *  of the annotations on the super class method are ignored.
	 */
	@Test
	public void testClassIgnorsSuperClass() throws Exception {

		reset();
		
		JaxRsHandler handler = HandlerCreator.getHandler(MyResource5.class);

		Request requestMock = EasyMock.createNiceMock(Request.class);
		Response responseMock = EasyMock.createNiceMock(Response.class);
		expect(requestMock.getMethod()).andReturn(Method.PUT).anyTimes();
		RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI.getPath()).andReturn("/resource");
		expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
		replay(requestMock);
		replay(requestURI);
		replay(responseMock);
		handler.handle(requestMock, responseMock);
		
		assertTrue(handleGetInvoked && !superHandleGetInvoked);
		
		reset();
		
		Request requestMock2 = EasyMock.createNiceMock(Request.class);
		Response responseMock2 = EasyMock.createNiceMock(Response.class);
		expect(requestMock2.getMethod()).andReturn(Method.GET).anyTimes();
		RequestURI requestURI2 = EasyMock.createNiceMock(RequestURI.class);
		expect(requestURI2.getPath()).andReturn("/superclass");
		expect(requestMock2.getRequestURI()).andReturn(requestURI2).anyTimes();
		replay(requestMock2);
		replay(requestURI2);
		replay(responseMock2);
		handler.handle(requestMock2, responseMock2);
		assertTrue(!handleGetInvoked && !superHandleGetInvoked);
	}
	
	public static void reset() {
		handleGetInvoked = false;
		superHandleGetInvoked = false;
	}
	
	
}

