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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * Static utility methods to detect the type of methods and to get 
 * annotated methods of a provided class.
 * 
 * @author reto
 */
public class MethodUtil {

	public static Set<Method> filterByHttpMethod(Set<Method> methods,
			org.wymiwyg.wrhapi.Method httpMethod) {
		Set<Method> result = new HashSet<Method>();
		for (Method method : methods) {
			if (getHttpMethodAnnotation(method).equals(httpMethod)) {
				result.add(method);
			}
		}
		return result;
	}

	private static org.wymiwyg.wrhapi.Method getHttpMethodAnnotation(
			Method method) {
		Annotation[] annotations = method.getAnnotations();
		for (Annotation annotation : annotations) {
			org.wymiwyg.wrhapi.Method result = getHttpMethodAnnotation(annotation);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * if annotation is itself annotated with HttpMethod, returns its value
	 * 
	 * @param annotation
	 * @return
	 */
	private static org.wymiwyg.wrhapi.Method getHttpMethodAnnotation(
			Annotation annotation) {
		HttpMethod httpMethod = annotation.annotationType().getAnnotation(
				HttpMethod.class);
		if (httpMethod == null) {
			return null;
		}
		org.wymiwyg.wrhapi.Method wrhapiMethod = org.wymiwyg.wrhapi.Method
				.get(httpMethod.value());
		return wrhapiMethod;
	}

	public static boolean isResourceMethod(Method method) {
		return getHttpMethodAnnotation(method) != null;
	}

	/**
	 * Returns a Set containing Method Objects reflecting all methods of the
	 * specified <code>Class</code>, which have a jaxrs annotation. If a method
	 * of the class has no annotations then the annotated method of the
	 * superclass or interface is returned. Annotations on a super-class take
	 * precedence over those on an implemented interface.
	 * 
	 * @param clazz
	 * @return Set of methods which have an jaxrs annotation
	 */
	public static Set<Method> getAnnotatedMethods(Class<?> clazz) {
		Map<MethodSignature, Method> annotatedmethods = getAnnotatedSuperClassMethods(clazz);
		Map<MethodSignature, Method> annotatedinterfacemethods = new HashMap<MethodSignature, Method>();

		for (Class<?> iface : clazz.getInterfaces()) {
			annotatedinterfacemethods
					.putAll(getAnnotatedInterfaceMethods(iface));
		}

		for (Method method : annotatedinterfacemethods.values()) {
			MethodSignature signature = new MethodSignature(method);
			if (!annotatedmethods.containsKey(signature)) {
				annotatedmethods.put(signature, method);
			}
		}
		
		return new HashSet<Method>(annotatedmethods.values());
	}

	private static Map<MethodSignature, Method> getAnnotatedSuperClassMethods(
			Class<?> clazz) {

		Map<MethodSignature, Method> annotatedmethods;

		if (clazz.getSuperclass() != null) {
			annotatedmethods = getAnnotatedSuperClassMethods(clazz
					.getSuperclass());
		} else {
			return new HashMap<MethodSignature, Method>();
		}

		for (Method method : clazz.getMethods()) {
			if (hasJaxRsAnnotation(method)) {
				annotatedmethods.put(new MethodSignature(method), method); 
			}
		}

		return annotatedmethods;
	}

	private static Map<MethodSignature, Method> getAnnotatedInterfaceMethods(
			Class<?> clazz) {

		Map<MethodSignature, Method> annotatedmethods = new HashMap<MethodSignature, Method>();

		for (Class<?> iface : clazz.getInterfaces()) {
			if (iface != null) {
				annotatedmethods.putAll(getAnnotatedInterfaceMethods(iface));
			}
		}

		for (Method method : clazz.getMethods()) {
			if (hasJaxRsAnnotation(method)) {
				annotatedmethods.put(new MethodSignature(method), method); 
			}
		}

		return annotatedmethods;
	}

	private static boolean hasJaxRsAnnotation(Method method) {
		for (Annotation annotation : method.getAnnotations()) {
			Class<?> annotationType = annotation.annotationType();

			HttpMethod httpType = (HttpMethod) annotationType
					.getAnnotation(HttpMethod.class);

			if (httpType != null || annotationType.equals(Consumes.class)
					|| annotationType.equals(Produces.class)
					|| annotationType.equals(Path.class)
					|| annotationType.equals(PathParam.class)
					|| annotationType.equals(QueryParam.class)
					|| annotationType.equals(FormParam.class)
					|| annotationType.equals(MatrixParam.class)
					|| annotationType.equals(CookieParam.class)
					|| annotationType.equals(HeaderParam.class)
					|| annotationType.equals(Encoded.class)
					|| annotationType.equals(DefaultValue.class)
					|| annotationType.equals(javax.ws.rs.core.Context.class)
					|| annotationType.equals(HttpMethod.class)) {
				return true;
			}

		}

		return false;
	}
}
