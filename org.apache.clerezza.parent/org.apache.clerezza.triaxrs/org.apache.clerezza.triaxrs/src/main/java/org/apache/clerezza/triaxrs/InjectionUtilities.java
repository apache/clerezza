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
package org.apache.clerezza.triaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.triaxrs.parameterinjectors.ContextInjector;
import org.apache.clerezza.triaxrs.parameterinjectors.CookieParameterInjector;
import org.apache.clerezza.triaxrs.parameterinjectors.FormParameterInjector;
import org.apache.clerezza.triaxrs.parameterinjectors.HeaderParameterInjector;
import org.apache.clerezza.triaxrs.parameterinjectors.MatrixParameterInjector;
import org.apache.clerezza.triaxrs.parameterinjectors.ParameterInjector;
import org.apache.clerezza.triaxrs.parameterinjectors.PathParameterInjector;
import org.apache.clerezza.triaxrs.parameterinjectors.QueryParameterInjector;
import org.apache.clerezza.triaxrs.parameterinjectors.UnsupportedFieldType;
import org.apache.clerezza.triaxrs.util.MethodUtil;
import org.wymiwyg.wrhapi.HandlerException;

/**
 * utilities to create parameters for method invocation and inject other
 * stuff
 * 
 * @author szalay
 */
public class InjectionUtilities {

	private static final Logger logger = LoggerFactory.getLogger(
			InjectionUtilities.class);
	private static Map<Class<? extends Annotation>, ParameterInjector> parameterInjectorMap 
			= new HashMap<Class<? extends Annotation>, ParameterInjector>() {
		//should the ParameterInjector be injected, if yes, OSGI(-DS) or lookup from Providers? 

		{
			put(MatrixParam.class, new MatrixParameterInjector());
			put(QueryParam.class, new QueryParameterInjector());
			put(PathParam.class, new PathParameterInjector());
			put(CookieParam.class, new CookieParameterInjector());
			put(HeaderParam.class, new HeaderParameterInjector());
			put(Context.class, new ContextInjector());
			put(FormParam.class, new FormParameterInjector());
		}
	};

	/**
	 * Creates an instance of resourceClass injecting parameters using constructor properties and fields
	 * 
	 * @param request
	 * @param resourceClass
	 * @return an instance of resourceClass 
	 * @throws org.wymiwyg.wrhapi.HandlerException
	 * @throws org.apache.clerezza.triaxrs.parameterinjectors.UnsupportedFieldType
	 */
	public static Object createPreparedInstance(WebRequest request,
			Map<String, String> pathParams,
			Providers providers, Class<?> resourceClass)
			throws HandlerException, UnsupportedFieldType {

		Object instance = null;

		boolean encodingDisabled =
				resourceClass.getAnnotation(Encoded.class) != null;

		// check constructor parameters and call constructor
		Constructor<?>[] constructors = resourceClass.getConstructors();
		int countParameters = -1;
		Constructor<?> constructor = null;

		// we must use the constructor with the most parameters
		for (Constructor<?> c : constructors) {
			Class<?>[] parameters = c.getParameterTypes();
			int count = parameters.length;

			if (count > countParameters) {
				constructor = c;
				countParameters = count; //update max parameters size found so far
			}
		}

		if (constructor == null) {
			throw new HandlerException(
					"No constructor found for resource class: " + resourceClass.getName());
		}

		logger.debug("Constructor found, injecting parameters...");

		Class<?>[] parameterClasses = constructor.getParameterTypes();
		boolean encodingDisabledForConstructor = encodingDisabled 
				|| (constructor.getAnnotation(Encoded.class) != null);
		Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
		Object[] parameters = new Object[parameterClasses.length];

		for (int i = 0; i < parameterClasses.length; i++) {

			Annotation[] as = parameterAnnotations[i];

			if (as.length == 0) {
				//TODO make sure another constructor is tried
				throw new HandlerException(
						"Class has constructor with arguments we cannot unterstand: " +
						"(the parameter of type " + parameterClasses[i] 
						+ " has no annotation)");
			}

			Object paramValue = getInjectionValueForAnnotation(request,
					pathParams, providers, as,
					parameterClasses[i], encodingDisabledForConstructor);
			parameters[i] = paramValue;
		}
		
		
		logger.debug("Calling constructor {} with parameters: {}", constructor, parameters);
		try {
			instance = constructor.newInstance(parameters);
		} catch (Exception e) {
			throw new HandlerException("Error in initializing: " + e, e);
		}
		injectFields(request, pathParams, providers, instance);

		return instance;
	}

	private static int accessModifiers(int m) {
		return m & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED);
	}

	/**
	 * this method determines which values have to injected into the method
	 * parameters and does it
	 * 
	 * @param encodingDisabled true if the class containing the method is annotaed with @Encoded
	 * @param providers 
	 */
	public static Object[] createParametersForRequest(Method method,
			WebRequest request, Map<String, String> pathParams,
			Providers providers, boolean encodingDisabled) throws HandlerException, UnsupportedFieldType {

		Class<?>[] parameters = method.getParameterTypes();

		if (parameters.length == 0) {
			return new Object[0];
		}

		Type[] parameterTypes = method.getGenericParameterTypes();
		Class<?>[] parameterClasses = method.getParameterTypes();
		Object[] result = new Object[parameters.length];
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		boolean encodingDisabledForMethod = encodingDisabled || (method.getAnnotation(
				Encoded.class) != null);
		for (int i = 0; i < parameters.length; i++) {

			Annotation[] annotationsForThisParameter = parameterAnnotations[i];

			logger.debug("Parameter {} has annotations: {}", 
					parameters[i].getName(), annotationsForThisParameter.length);

			if ((annotationsForThisParameter == null) 
					|| (annotationsForThisParameter.length == 0)) {

				// if the parameter has no annotations, the request body is
				// injected (this is the entity parameter)

				result[i] = request.getBodyObject(parameterClasses[i],
						parameterTypes[i], null);

				continue;
			}

			Object injectedParameter = getInjectionValueForAnnotation(request,
					pathParams,
					providers, annotationsForThisParameter, parameterTypes[i],
					encodingDisabledForMethod);
			logger.debug("Inject parameter {} to {}", i, injectedParameter);
			result[i] = injectedParameter;

		}
		return result;
	}

	/**
	 * get injection value for an annotation
	 * 
	 * @param request
	 * @param pathParams
	 * @param providers 
	 * @param annotations
	 * @param parameterClass
	 * @param encodingDisabled true if the @Encoded annotation has been set on a higher level (e.g. class or method)
	 * @return
	 * @throws org.wymiwyg.wrhapi.HandlerException
	 */
	private static Object getInjectionValueForAnnotation(WebRequest request,
			Map<String, String> pathParams,
			Providers providers, Annotation[] annotations,
			Type parameterType,
			boolean encodingDisabled) throws HandlerException, UnsupportedFieldType {
		String defaultValue = getDefaultValue(annotations);
		
		final Annotation injectionAnnotation = getInjectionAnnotation(annotations);
		if (injectionAnnotation == null) {
			logger.debug("No injection annotation, do nothing: {}", annotations);
			return null;
		}
		//not checking for supertypes of annotation, should we?
		ParameterInjector<Annotation> parameterInjector = parameterInjectorMap
				.get(injectionAnnotation.annotationType());
		if (parameterInjector != null) {
			return parameterInjector.getValue(request,
					pathParams, providers,
					parameterType, injectionAnnotation,
					encodingDisabled, defaultValue);
		}
		logger.debug("Unknown annotation, do nothing: {}",
				annotations[0].getClass().getName());
		return null;
	}

	/**
	 * 
	 * @param annotations
	 * @return true if annotations contains a jax.rs @*Param or @Context Annotation
	 */
	public static boolean isAnnotated(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (isInjectionAnnotation(annotation)) {
				return true;
			}
		}
		return false;
	}

	private static Annotation getInjectionAnnotation(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (isInjectionAnnotation(annotation)) {
				return annotation;
			}
		}
		return null;
	}
	
	private static String getDefaultValue(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if(annotation.annotationType().equals(DefaultValue.class)){
				DefaultValue dv = (DefaultValue) annotation;
				return dv.value();
			}
		}
		return null;
	}

	public static void injectFields(WebRequest request, Map<String, String> pathParams, 
			Providers providers, Object instance) throws HandlerException, UnsupportedFieldType {
		Class<?> resourceClass = instance.getClass();
		boolean encodingDisabled =
				resourceClass.getAnnotation(Encoded.class) != null;
		// check fields
		Field[] fields = resourceClass.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {

			Annotation[] as = fields[i].getAnnotations();

			if (as.length == 0) {
				continue;
			}

			boolean encodingDisabledForField = encodingDisabled || (fields[i].getAnnotation(
					Encoded.class) != null);

			Object fieldValue = getInjectionValueForAnnotation(request,
					pathParams, providers, as,
					fields[i].getType(), encodingDisabledForField);
			if (fieldValue != null) {
				try {
					fields[i].set(instance, fieldValue);
				} catch (IllegalAccessException iae) {
					throw new HandlerException("setting "+fields[i]+" to "+fieldValue, iae);
				}
				logger.debug("set field value: {} to {}", fields[i], fieldValue);
			}
		}

		logger.debug("Fields checked.");

		// check setter methods
		for (Method method : MethodUtil.getAnnotatedMethods(resourceClass)){
			
			int searchMod = Modifier.PUBLIC;
			int mods = accessModifiers(method.getModifiers());
			boolean modMatch = (searchMod == mods);

			if ((method.getName().startsWith("set")) && (modMatch) 
					&& (method.getParameterTypes().length == 1) 
					&& (method.getReturnType().equals(Void.TYPE))) {
				
				logger.debug("Method {} is a setter.", method);

				Annotation[] as = method.getAnnotations();
				if (as.length == 0) {
					continue;
				}
				
				boolean encodingDisabledForSetter = encodingDisabled 
						|| (method.getAnnotation(Encoded.class) != null);
				final Object value = getInjectionValueForAnnotation(request,
						pathParams, providers, as,
						method.getGenericParameterTypes()[0],
						encodingDisabledForSetter);

				if (value == null) {
					continue;
				}

				Object[] valuesToSet = {value};

				try {
					method.invoke(instance, valuesToSet);
				} catch (IllegalAccessException illegalAccessException) {
					throw new HandlerException(illegalAccessException);
				} catch (IllegalArgumentException illegalArgumentException) {
					throw new HandlerException(illegalArgumentException);
				} catch (InvocationTargetException invocationTargetException) {
					throw new HandlerException(invocationTargetException);
				}
			}
		}
	}

	private static boolean isInjectionAnnotation(Annotation annotation) {
		return ((annotation.annotationType().equals(Context.class)) ||
				(annotation.annotationType().equals(QueryParam.class)) ||
				(annotation.annotationType().equals(FormParam.class)) ||
				(annotation.annotationType().equals(MatrixParam.class)) ||
				(annotation.annotationType().equals(PathParam.class)) ||
				(annotation.annotationType().equals(CookieParam.class)) ||
				(annotation.annotationType().equals(HeaderParam.class)));
	}
}

