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

import java.util.Iterator;
import org.apache.clerezza.jaxrs.extensions.ResourceMethodException;
import org.apache.clerezza.jaxrs.extensions.HttpRequest;
import org.apache.clerezza.jaxrs.extensions.MethodResponse;
import org.apache.clerezza.jaxrs.extensions.RootResourceExecutor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessControlException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.MessageBodyReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.triaxrs.parameterinjectors.UnsupportedFieldType;
import org.apache.clerezza.triaxrs.util.AcceptHeader;
import org.apache.clerezza.triaxrs.util.MediaTypeComparator;
import org.apache.clerezza.triaxrs.util.MethodUtil;
import org.apache.clerezza.triaxrs.util.PathMatching;
import org.apache.clerezza.triaxrs.util.TemplateEncoder;
import org.apache.clerezza.triaxrs.util.URITemplate;
import org.apache.clerezza.utils.UriException;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;

/**
 * @scr.component
 * @scr.service interface="org.apache.clerezza.jaxrs.extensions.RootResourceExecutor"
 * 
 * @author mir, reto
 * 
 */
public class RootResourceExecutorImpl implements RootResourceExecutor {

	@Override
	public MethodResponse execute(HttpRequest httpRequest, Object resource, String subResourcePath, Map<String, String> pathParams) throws ResourceMethodException {
		WebRequest request;
		try {
			request = (WebRequest) httpRequest;
		} catch (ClassCastException e) {
			throw new RuntimeException("Only Triaxrs HttpRequests supported");
		}
		try {
			Map<Method, Map<String, String>> method2PathParams = new HashMap<Method, Map<String, String>>();
				MethodsAndInstance candidateMethodsAndInstance = getCandidateMethods(
						request, resource, subResourcePath,
						method2PathParams, pathParams);
			Set<Method> candidateMethods = candidateMethodsAndInstance.methods;
				resource = candidateMethodsAndInstance.instance;
			final org.wymiwyg.wrhapi.Method httpMethod = request
					.getWrhapiRequest().getMethod();
			Set<Method> httpMatchingMethods = MethodUtil.filterByHttpMethod(
					candidateMethods, httpMethod);
			if (httpMatchingMethods.size() == 0) {
				if (httpMethod.equals(org.wymiwyg.wrhapi.Method.HEAD)) {
					httpMatchingMethods = MethodUtil.filterByHttpMethod(
							candidateMethods, org.wymiwyg.wrhapi.Method.GET);
				}
				if (httpMethod.equals(org.wymiwyg.wrhapi.Method.OPTIONS)) {
					return responsDefaultOption(request, candidateMethods);
				}
			}
			if (httpMatchingMethods.size() == 0) {
				throw new WebApplicationException(405);
			}

			Set<MethodAndInputType> invocableMethods = filterByConsumedType(
					httpMatchingMethods, request);
			AcceptHeader acceptHeader = request.getAcceptHeader();
			SortedSet<MethodAndConsumedAndProducibleTypes> acceptableMethods = new TreeSet<MethodAndConsumedAndProducibleTypes>(
					filterByAcceptHeader(invocableMethods, acceptHeader));

			if (acceptableMethods.size() == 0) {
				throw new WebApplicationException(406);
			}
			final MethodAndConsumedAndProducibleTypes firstAcceptable = acceptableMethods
					.first();
			// take discardReturnedEntity into account (setting NullWriter)
			Method selectedMethod = firstAcceptable.method;

			Object methodReturnValue = handleWithMethod(request,
					method2PathParams.get(selectedMethod), resource,
					selectedMethod);

			return processReturnValue(methodReturnValue, selectedMethod,
					firstAcceptable.producibleTypes);
		} catch (UnsupportedFieldType ex) {
			throw new WebApplicationException(500);
		} catch (HandlerException ex) {
			throw new RuntimeException(ex);
		}
	}


	private static class MethodsAndInstance {

		Set<Method> methods;
		Object instance;

		public MethodsAndInstance(Set<Method> methods, Object instance) {
			this.methods = methods;
			this.instance = instance;
		}
	}

	private static class MethodAndInputType {

		Method method;
		MediaType consumedType;

		MethodAndInputType(Method method, MediaType consumedType) {
			this.method = method;
			this.consumedType = consumedType;
		}
	}

	private static class MethodAndConsumedAndProducibleTypes extends
			MethodAndInputType implements
			Comparable<MethodAndConsumedAndProducibleTypes> {

		private Set<MediaType> producibleTypes;
		private float highestQValueInAccept;

		private MethodAndConsumedAndProducibleTypes(Method method,
				MediaType consumedType, int highestQValueInAccept,
				Set<MediaType> producibleTypes) {
			super(method, consumedType);
			this.producibleTypes = producibleTypes;
			this.highestQValueInAccept = highestQValueInAccept;
		}

		@Override
		public int compareTo(MethodAndConsumedAndProducibleTypes o) {
			if (equals(o)) {
				return 0;
			}
			final int consumedComparison = MediaTypeComparator
					.inconsistentCompare(consumedType, o.consumedType);
			if (consumedComparison != 0) {
				return consumedComparison;
			}
			if (highestQValueInAccept < o.highestQValueInAccept) {
				return 1;
			}
			if (highestQValueInAccept > o.highestQValueInAccept) {
				return -1;
			}
			if (getMaxConcreteness() > o.getMaxConcreteness()) {
				return 1;
			}
			if (getMaxConcreteness() < o.getMaxConcreteness()) {
				return -1;
			}
			return toString().compareTo(o.toString());
		}

		/**
		 * returns the number of * in the most concrete media-type
		 * 
		 * @return 0, 1 or 2
		 */
		private int getMaxConcreteness() {
			int maxConcreteness = 0;
			for (MediaType producibleType : producibleTypes) {
				int contreteness = MediaTypeComparator
						.countWildChars(producibleType);
				if (contreteness > maxConcreteness) {
					maxConcreteness = contreteness;
				}
			}
			return maxConcreteness;
		}
	}

	final static private Logger logger = LoggerFactory.getLogger(RootResourceExecutorImpl.class);

	

	/**
	 * Returns the candidate methodAndInputType following part 2 of section
	 * 3.7.2 of jax-rs alongside an instance of the class conating the methods
	 * (the passed instance or a subresource)
	 * 
	 * Not to overload the result object path-params are mapped from the method
	 * in method2PathParams, the invoke must pass a mutable map
	 * 
	 * @param request
	 * @param response
	 * @param instance
	 * @param method2PathParams
	 * @param pathMatching
	 * @return
	 */
	private MethodsAndInstance getCandidateMethods(WebRequest request,
			Object instance, String remainingPath,
			Map<Method, Map<String, String>> method2PathParams,
			Map<String, String> inheritedPathParams) throws HandlerException,
			UnsupportedFieldType {
		Set<Method> result;
		if (remainingPath.equals("/") || (remainingPath.length() == 0)) {
			result = getResourceMethods(instance.getClass());
			for (Method method : result) {
				method2PathParams.put(method, inheritedPathParams);
			}
			
			if(result.size() == 0){
				return getSubResourceMethods(request, instance, remainingPath,
						method2PathParams, inheritedPathParams);
			} else {
				return new MethodsAndInstance(result, instance);
			}
			
		} else {
			return getSubResourceMethods(request, instance, remainingPath,
					method2PathParams, inheritedPathParams);
		}
	}

	private MethodsAndInstance getSubResourceMethods(WebRequest request,
			Object instance, String remainingPath,
			Map<Method, Map<String, String>> method2PathParams,
			Map<String, String> inheritedPathParams) throws HandlerException,
			UnsupportedFieldType {
		SortedSet<MethodDescriptor> methodDescriptors = getSubThingMethodDescriptors(instance.getClass());
		Set<Method> result;
		result = new HashSet<Method>();
		URITemplate uriTemplateOfFirstMatchingRM = null;
		Map<String, String> subPathParam = null;
		for (MethodDescriptor methodDescriptor : methodDescriptors) {
			final URITemplate currentUriTemplate = methodDescriptor.getUriTemplate();
			if (uriTemplateOfFirstMatchingRM != null) {
				if (uriTemplateOfFirstMatchingRM.equals(currentUriTemplate)
						&& !methodDescriptor.isSubResourceLocator()) {
					result.add(methodDescriptor.getMethod());
					method2PathParams.put(methodDescriptor.getMethod(),
							subPathParam);
					continue;
				}
				break;
			}
			PathMatching subPathMatching = currentUriTemplate.match(remainingPath);
			if (subPathMatching == null) {
				continue;
			}
			subPathParam = new HashMap<String, String>(inheritedPathParams);
			subPathParam.putAll(subPathMatching.getParameters());
			if (methodDescriptor.isSubResourceLocator()) {
				return getCandidateMethods(request, getSubResource(
						instance, methodDescriptor.getMethod(), request,
						subPathMatching), subPathMatching.getRemainingURIPath(), method2PathParams,
						subPathParam);
			}
			if (subPathMatching.isSlashOrEmpty()) {
				if (!methodDescriptor.isSubResourceLocator()) {
					Method method = methodDescriptor.getMethod();
					result.add(method);
					uriTemplateOfFirstMatchingRM = currentUriTemplate;
					method2PathParams.put(method, subPathParam);
				}
			}
		}
		if (result.size() > 0) {
			return new MethodsAndInstance(result, instance);
		}
		throw new WebApplicationException(404);
	}

	/**
	 * get descriptor for sub-resource methodAndInputTypes and - locators
	 */
	private SortedSet<MethodDescriptor> getSubThingMethodDescriptors(
			Class<?> clazz) {
		SortedSet<MethodDescriptor> result = new TreeSet<MethodDescriptor>();
		Set<Method> methods = MethodUtil.getAnnotatedMethods(clazz);
		for (Method method : methods) {
			final Path pathAnnotation = method.getAnnotation(Path.class);
			if (pathAnnotation != null) {
				result.add(new MethodDescriptor(method,
						templateUrlEncode(pathAnnotation.value())));
			}
		}
		return result;
	}

	/**
	 * The media type of the request entity body (if any) is a supported input
	 * data format (see section 3.5). If no methodAndInputTypes support the
	 * media type of the request entity body an implementation MUST generate a
	 * WebApplicationException with an unsupported media type response (HTTP 415
	 * status) and no entity. The exception MUST be processed as described in
	 * section 3.3.4.
	 * 
	 * @return
	 */
	private Set<MethodAndInputType> filterByConsumedType(Set<Method> methods,
			WebRequest request) throws HandlerException {
		final String contentTypeString = request.getHeaders().getFirst(
				HttpHeaders.CONTENT_TYPE);
		MediaType mediaType;
		if (contentTypeString != null) {
			mediaType = MediaType.valueOf(contentTypeString);
		} else {
			if (request.getWrhapiRequest().getMessageBody() != null) {
				mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
			} else {
				mediaType = null;
			}
		}
		Set<MethodAndInputType> result = new HashSet<MethodAndInputType>();
		METHODS: for (Method method : methods) {
			Annotation[][] parameterAnnotations = method
					.getParameterAnnotations();
			Class<?>[] parameterTypes = method.getParameterTypes();
			Type[] parameterGenericTypes = method.getGenericParameterTypes();
			for (int i = 0; i < parameterTypes.length; i++) {
				if (!InjectionUtilities.isAnnotated(parameterAnnotations[i])) {
					if (mediaType == null) {
						continue METHODS;
					}
					Consumes consumes = method.getAnnotation(Consumes.class);
					SortedSet<MediaType> sortedConsumedType = new TreeSet<MediaType>(
							new MediaTypeComparator());
					if (consumes == null) {
						sortedConsumedType.add(MediaType.WILDCARD_TYPE);
					} else {
						for (String consumesString : consumes.value()) {
							sortedConsumedType.add(MediaType
									.valueOf(consumesString));
						}
					}
					for (MediaType consumedType : sortedConsumedType) {
						if (mediaType.isCompatible(consumedType)) {
							result.add(new MethodAndInputType(method,
									consumedType));
							continue METHODS;
						}

					}
					MessageBodyReader<?> messageBodyReader = JaxRsHandler.providers
							.getMessageBodyReader(parameterTypes[i],
									parameterGenericTypes[i],
									parameterAnnotations[i], mediaType);
					if (messageBodyReader == null) {
						continue METHODS;
					} else {
						// keep the messageBodyReader for possible future usage
					}
				}
			}
			
			result.add(new MethodAndInputType(method, null));
		}
		return result;
	}
	
	
	private Set<MethodAndConsumedAndProducibleTypes> filterByAcceptHeader(
			Set<MethodAndInputType> methodAndInputTypes,
			AcceptHeader acceptHeader) {
		Set<MethodAndConsumedAndProducibleTypes> result = new HashSet<MethodAndConsumedAndProducibleTypes>();
		for (MethodAndInputType methodAndInputType : methodAndInputTypes) {
			Produces produces = methodAndInputType.method
					.getAnnotation(Produces.class);
			Set<MediaType> producibleMediaTypes = new HashSet<MediaType>();
			if (produces == null) {
				// TODO look at class annotation
				// */* is always acceptable
				result.add(new MethodAndConsumedAndProducibleTypes(
						methodAndInputType.method,
						methodAndInputType.consumedType,
						0,
						producibleMediaTypes));
			} else {
				
				int bestQValue = 0;
				for (String produced : produces.value()) {
					MediaType producedType = MediaType.valueOf(produced);
	
					
					int qValue = acceptHeader.getAcceptedQuality(producedType);
					if (qValue == 0) {
						continue;
					}
					if (qValue > bestQValue) {
						bestQValue = qValue;
					}
					producibleMediaTypes.add(producedType);
				}
				if (producibleMediaTypes.size() > 0) {
					result.add(new MethodAndConsumedAndProducibleTypes(
							methodAndInputType.method,
							methodAndInputType.consumedType, bestQValue,
							producibleMediaTypes));
				}
				
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param pathParams
	 * @param instance
	 * @param method
	 * @param targetMediaType
	 *            the accept header entry for which the request is produced or
	 *            the @Produces annotation of the method, whatever is more
	 *            concrete
	 * @return the object the ivoked method returned
	 * @throws HandlerException
	 * @throws ResourceMethodException
	 */
	private Object handleWithMethod(WebRequest request,
			Map<String, String> pathParams, Object instance, Method method) throws HandlerException,
			ResourceMethodException {
		boolean encodingDisabled = instance.getClass().getAnnotation(
				Encoded.class) != null;
		Object[] methodParams;
		try {
			methodParams = InjectionUtilities.createParametersForRequest(
					method, request, pathParams, JaxRsHandler.providers, encodingDisabled);
		} catch (UnsupportedFieldType ex) {
			logger.error("Exception {}", ex);
			throw new WebApplicationException(500);
		}
		final Object methodReturnValue;
		try {
			methodReturnValue = method.invoke(instance, methodParams);
		} catch (IllegalAccessException ex) {
			logger.error("Exception {}", ex);
			throw new WebApplicationException(500);
		} catch (IllegalArgumentException ex) {
			logger.error("Exception {}", ex);
			throw new WebApplicationException(500);
		} catch (InvocationTargetException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof WebApplicationException) {
				throw (WebApplicationException) cause;
			} else {
				//if we get a ResourceMethodException this means the
				//resource method invoked the RootResourceExcutor-Service
				//it shall be handled as if the wrapped exception was thrown
				//by the resource method itself
				if (cause instanceof ResourceMethodException) {
					throw (ResourceMethodException) cause;
				} else {
					throw new ResourceMethodException(cause);
				}
			}
		}
		return methodReturnValue;
	}
	
	private ProcessableResponse processReturnValue(Object methodReturnValue,
			Method method,
			Set<MediaType> methodProducibleMediaTypes) throws HandlerException {

		Type genericMethodReturnType = method.getGenericReturnType();
		return ProcessableResponse.createProcessableResponse(methodReturnValue, 
				method.getAnnotations(), methodProducibleMediaTypes, 
				genericMethodReturnType, method);
	}
	
	/**
	 * returns the resource methodAndInputTypes (exclusing sub-resource
	 * methodAndInputTypes)
	 */
	private Set<Method> getResourceMethods(Class<?> clazz) {
		Set<Method> result = new HashSet<Method>();
		Set<Method> methods = MethodUtil.getAnnotatedMethods(clazz);
		for (Method method : methods) {
			if (method.getAnnotation(Path.class) != null) {
				continue;
			}
			if (MethodUtil.isResourceMethod(method)) {
				result.add(method);
			}
		}
		return result;
	}
	
	/**
	 * get the subresource returned by a sub-resource locator methodAndInputType
	 * injecting the parameters with the values from request and the parameters
	 * from pathMatching
	 * 
	 */
	private Object getSubResource(Object instance, Method method,
			WebRequest request, PathMatching pathMatching)
			throws HandlerException, UnsupportedFieldType {
		boolean encodingDisabled = instance.getClass().getAnnotation(
				Encoded.class) != null;
		Object[] paramValues = InjectionUtilities.createParametersForRequest(
				method, request, pathMatching.getParameters(), JaxRsHandler.providers,
				encodingDisabled);
		try {
			return method.invoke(instance, paramValues);
		} catch (IllegalAccessException ex) {
			logger.error("Exception {}", ex);
			throw new WebApplicationException(500);
		} catch (IllegalArgumentException ex) {
			logger.error("Exception {}", ex);
			throw new WebApplicationException(500);
		} catch (InvocationTargetException ex) {
			final Throwable cause = ex.getCause();
			if (cause instanceof AccessControlException) {
				throw (AccessControlException) cause;
			}
			if (cause instanceof WebApplicationException) {
				throw (WebApplicationException) cause;
			}
			logger.error("Exception {}", ex);
			throw new WebApplicationException(500);
		}
	}
	
	private ProcessableResponse responsDefaultOption(WebRequest request,
			Set<Method> candidateMethods) {
        
		ResponseBuilder builder = Response.ok();
		final Set<String> supportedMethods = new HashSet<String>();
		for (Method candidateMethod : candidateMethods) {
			Annotation[] declaredAnnotations = candidateMethod.getDeclaredAnnotations();
			for (Annotation annotation : declaredAnnotations) {
				final HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
				if (httpMethod != null) {
					supportedMethods.add(httpMethod.value());
				}
			}
		}
		final String allowHeader = concateNameWithComa(supportedMethods);
		builder.header(HeaderName.ALLOW.toString(), allowHeader);
		return ProcessableResponse.createProcessableResponse(builder.build(),
				null, null, null, null);
	}

	private String concateNameWithComa(Collection<String> collection) {
		if (collection.isEmpty()) return "";
		final Iterator<String> iterator = collection.iterator();
		final StringBuffer buffer = new StringBuffer(iterator.next());
		while (iterator.hasNext()) {
			buffer.append(", ");
			buffer.append(iterator.next());
		}
		return buffer.toString();
	}

	private String templateUrlEncode(String value) {
		try {
			return TemplateEncoder.encode(value, "utf-8");
		} catch (UriException e) {
			throw new RuntimeException(e);
		}
	}
}
