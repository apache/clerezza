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
package org.apache.clerezza.triaxrs.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.Assert.assertTrue;

import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.junit.Test;
import org.apache.clerezza.triaxrs.util.MethodSignature;
import org.apache.clerezza.triaxrs.util.MethodUtil;

public class TestMethodUtil {

	public static interface MySuperInterface {
		@Path("superinterface")
		public String interfaceD();
	}
	
	public static interface MySuperInterface2 {
		@Path("superinterface2")
		public String interfaceD();
	}
	
	public static interface MyInterface extends MySuperInterface, MySuperInterface2{

		@GET
		public void interfaceA();

		@PUT
		public void interfaceA(String text);

		@Produces("nothing")
		@Path("foo")
		public String interfaceB(String text);

		public void interfaceC(String text, Object obj);
		
		public String interfaceD();
	}
	
	public static class MySuperSuperClass {
		@GET
		public void superclassA(){
			
		}
		
		@Path("foobar")
		public void supersuperclassA(){
			
		}
	}
	
	public static class MySuperClass extends MySuperSuperClass{
		@Consumes
		public void superclassA() {
		}

		@GET
		@Encoded
		public void superclassB(int num) {
		}

		@Path("bar")
		@PathParam("{name}")
		public String interfaceB(String name) {
			return name;
		}
		
		@Override
		public void supersuperclassA(){
		}

	}

	public static class MyClass extends MySuperClass implements MyInterface {

		@Override
		public void interfaceA() {
			// TODO Auto-generated method stub

		}

		@Override
		public void interfaceA(String text) {
			// TODO Auto-generated method stub

		}

		@Override
		public String interfaceB(String bla) {
			return bla;
		}

		@GET
		@HeaderParam("aa")
		@Override
		public void interfaceC(String foo, Object bar) {
		}

		@Produces
		@Path("null")
		@Override
		public void superclassA() {
		}

		@Path("abc")
		public Object myFunction(Set<Object> so) {
			return null;
		}

		@Override
		public String interfaceD() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	@Test
	public void testGetAnnotatedMethods() {
		Set<Method> methods = MethodUtil.getAnnotatedMethods(MyClass.class);

		String methodsString = methodSet2String(methods);
		System.out.println(methodsString);
		
		assertTrue(methodsString
				.contains("Method: interfaceC Parameter: java.lang.String java.lang.Object Annotations: @javax.ws.rs.GET() @javax.ws.rs.HeaderParam(value=aa)")
				&& methodsString
						.contains("Method: myFunction Parameter: java.util.Set Annotations: @javax.ws.rs.Path(value=abc)")
				&& methodsString
						.contains("Method: interfaceB Parameter: java.lang.String Annotations: @javax.ws.rs.Path(value=bar) @javax.ws.rs.PathParam(value={name})")
				&& methodsString
						.contains("Method: superclassA Parameter: Annotations: @javax.ws.rs.Produces(value=[*/*]) @javax.ws.rs.Path(value=null)")
				&& methodsString
						.contains("Method: interfaceA Parameter: java.lang.String Annotations: @javax.ws.rs.PUT()")
				&& methodsString
						.contains("Method: interfaceA Parameter: Annotations: @javax.ws.rs.GET()")
				&& methodsString
						.contains("Method: superclassB Parameter: int Annotations: @javax.ws.rs.GET() @javax.ws.rs.Encoded()")
				&& methodsString
						.contains("Method: interfaceD Parameter: Annotations: @javax.ws.rs.Path(value=superinterface2)")
				&& methodsString
						.contains("Method: supersuperclassA Parameter: Annotations: @javax.ws.rs.Path(value=foobar)")
				&& !methodsString
						.contains("Method: interfaceB Parameter: java.lang.String Annotations: @javax.ws.rs.Produces(value=[nothing]) @javax.ws.rs.Path(value=foo)")
				&& !methodsString
						.contains("Method: superclassA Parameter: Annotations: @javax.ws.rs.Consumes(value=[*/*])")
				&& !methodsString
						.contains("Method: interfaceD Parameter: Annotations: @javax.ws.rs.Path(value=superinterface)"));

	}

	private static String methodSet2String(Set<Method> methods) {
		String string = "";
		for (Method method : methods) {
			string += ("\nMethod: " + method.getName() + " Parameter: ");

			for (Class<?> clazz : method.getParameterTypes()) {
				string += (clazz.getName() + " ");
			}
			string += ("Annotations: ");
			for (Annotation annotation : method.getAnnotations()) {
				string += (annotation.toString() + " ");
			}
		}

		return string;
	}
	
	public static void main(String arg[]){
		Class<?>[] myarray = {String.class};
		Class<?>[] myarray2 = {Integer.class};
		MethodSignature ms1 = new MethodSignature("test", myarray);
		MethodSignature ms2 = new MethodSignature("test", myarray2);
		
		if (ms1.equals(ms2)){
			System.out.println("True");
		}
		
		MethodUtil.getAnnotatedMethods(MyClass.class);
	}
}
