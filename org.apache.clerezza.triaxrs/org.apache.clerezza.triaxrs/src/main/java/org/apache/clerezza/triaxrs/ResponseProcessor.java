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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.UnsupportedDataTypeException;
import javax.security.auth.Subject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.triaxrs.providers.provided.JafMessageBodyWriter;
import org.apache.clerezza.triaxrs.util.AcceptHeader;
import org.apache.clerezza.triaxrs.util.FirstByteActionOutputStream;
import org.apache.clerezza.triaxrs.util.MediaTypeComparator;
import org.apache.clerezza.triaxrs.util.BodyStoringResponse;
import org.apache.clerezza.triaxrs.util.InconsistentMediaTypeComparator;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.util.MessageBody2Write;

/**
 * @author mir
 *
 */
class ResponseProcessor {

	final static private Logger logger = LoggerFactory.getLogger(ResponseProcessor.class);

	static void handleReturnValue(final WebRequest request, Response response,
			ProcessableResponse processableResponse) throws HandlerException {
		try {
			Annotation[] annotations = processableResponse.getAnnotations();
			Set<MediaType> methodProducibleMediaTypes = processableResponse.getMethodProducibleTypes();

			processJaxResponse(request, response, processableResponse, annotations,
					methodProducibleMediaTypes);
		} catch (IOException ex) {
			throw new HandlerException(ex);
		}

	}

	static void processJaxResponse(final WebRequest request,
			final Response response, javax.ws.rs.core.Response jaxResponse,
			final Annotation[] annotations,
			Set<MediaType> methodProducibleMediaTypes)
			throws HandlerException, IOException {

		Object entity = jaxResponse.getEntity();

		final MultivaluedMap<String, Object> headerMap = jaxResponse.getMetadata();

		int responseStatus = jaxResponse.getStatus();
		if (responseStatus == -1) {
			responseStatus = ResponseStatus.SUCCESS.getCode();
		}
		if (entity == null) {
			response.setHeader(HeaderName.CONTENT_LENGTH, 0);
			if (responseStatus == ResponseStatus.SUCCESS.getCode()) {
				response.setResponseStatus(ResponseStatus.NO_CONTENT);
				flushHeaders(headerMap, response);
				return;
			} else {
				if (responseStatus > 400) {
					entity = getExplanation(responseStatus);
					headerMap.putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_TYPE);
				} else {
					response.setResponseStatus(ResponseStatus.getInstanceByCode(responseStatus));
					flushHeaders(headerMap, response);
					return;
				}
			}

		}
		final Type entityType;
		if (entity instanceof GenericEntity) {
			entityType = ((GenericEntity<?>) entity).getType();
			entity = ((GenericEntity<?>) entity).getEntity();
		} else {
			entityType = null;
		}

		// get best writer
		List<MediaType> methodProducibleMediaTypesList = new ArrayList<MediaType>(
				methodProducibleMediaTypes);
		AcceptHeader acceptHeader = request.getAcceptHeader();
		if (methodProducibleMediaTypesList.size() == 0) {
			methodProducibleMediaTypesList.add(MediaType.WILDCARD_TYPE);
		}
		final List<MediaType> expandedMethodProducibleMediaTypesList = expandListWithConcreterTypesFromAccept(methodProducibleMediaTypesList, acceptHeader);
		MessageBodyWriter<Object> writer = null;
		List<Set<MediaType>> expandedMethodProducibleMediaTypeClasses
				= getSortedClasses(expandedMethodProducibleMediaTypesList,
				new InconsistentMediaTypeComparator(acceptHeader));
		Collections.sort(methodProducibleMediaTypesList,
				new MediaTypeComparator(acceptHeader));
		MediaType relevantMethodProducibleType = null;

		for (Set<MediaType> preferenceClass : expandedMethodProducibleMediaTypeClasses) {
			int lastWriterConcreteness = -1;
			for (MediaType mediaType : preferenceClass) {
				MessageBodyWriter<Object> currentWriter = (MessageBodyWriter<Object>) JaxRsHandler.providers.getMessageBodyWriter(entity.getClass(), entityType,
						annotations, mediaType);
				if (currentWriter != null) {
					int writerConcreteness = getWriterConcreteness(currentWriter, mediaType);
					if (writerConcreteness > lastWriterConcreteness) {
						for (MediaType methodMediaType : methodProducibleMediaTypesList) {
							if (methodMediaType.isCompatible(mediaType)) {
								relevantMethodProducibleType = methodMediaType;
								break;
							}
						}
						writer = currentWriter;
					}
				}
			}
			if (writer != null) {
				break;
			}
		}


		if (writer == null) {
			for (MediaType mediaType : expandedMethodProducibleMediaTypesList) {
				try {
					writer = new JafMessageBodyWriter<Object>(entity, mediaType);
				} catch (UnsupportedDataTypeException ex) {
					logger.debug("No JafMessageBodyWriter for {}", mediaType);
				}
				if (writer != null) {
					relevantMethodProducibleType = mediaType;
					break;
				}
			}
		}
		if (writer == null) {
			javax.ws.rs.core.Response r = javax.ws.rs.core.Response.status(
					Status.INTERNAL_SERVER_ERROR).entity(
					"No suitable MessageBodyWriter available").type(
					MediaType.TEXT_PLAIN_TYPE).build();
			throw new WebApplicationException(r);
		}

		// gain producible concreteness by looking at writer's @Produces
		MediaType relevantProducibleType = null;
		if ((relevantMethodProducibleType == null) || (MediaTypeComparator.countWildChars(relevantMethodProducibleType) > 0)) {

			List<MediaType> writerProducibleMediaTypes = getWriterProduces(writer);
			Collections.sort(writerProducibleMediaTypes,
					new MediaTypeComparator(acceptHeader));
			if (relevantMethodProducibleType != null) {
				Iterator<MediaType> writerProducibleMediaTypesIter = writerProducibleMediaTypes.iterator();
				while (writerProducibleMediaTypesIter.hasNext()) {
					MediaType currentProducible = writerProducibleMediaTypesIter.next();
					if (!relevantMethodProducibleType.isCompatible(currentProducible)) {
						continue;
					}
					if (MediaTypeComparator.compareByWildCardCount(
							relevantMethodProducibleType, currentProducible) == 1) {
						relevantProducibleType = currentProducible;
					} else {
						relevantProducibleType = relevantMethodProducibleType;
					}
					break;
				}
			} else {
				relevantProducibleType = writerProducibleMediaTypes.get(0);
			}
		} else {
			relevantProducibleType = relevantMethodProducibleType;
		}
		if (relevantProducibleType == null) {
			throw new RuntimeException("The relevantMethodProducibleType " + relevantMethodProducibleType + " is not compatible with any of the producible types of " + writer);
		}

		// if relevantProducibleType is not concrete get the first matching
		// concrete from accept
		MediaType mediaType;
		if (MediaTypeComparator.countWildChars(relevantProducibleType) == 0) {
			mediaType = relevantProducibleType;
		} else {
			mediaType = relevantProducibleType;
			for (MediaType acceptType : acceptHeader.getEntries()) {
				if (acceptType.isCompatible(relevantProducibleType)) {
					if (MediaTypeComparator.compareByWildCardCount(acceptType,
							mediaType) == 1) {
						mediaType = acceptType;
					}
				}
			}
		}

		if (acceptHeader.getAcceptingMediaType(mediaType).isEmpty()) {
			if (!mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
				throw new WebApplicationException(406);
			}
		}

		mediaType = getConcreteMediaTypeFromPattern(mediaType);

		response.setResponseStatus(ResponseStatus.getInstanceByCode(responseStatus));

		long size = writer.getSize(entity, entity.getClass(), entityType,
				annotations, mediaType);

		if (size != -1) {
			response.setHeader(HeaderName.CONTENT_LENGTH, size);
		}

		List<Object> contentTypeList = jaxResponse.getMetadata().get(
				HttpHeaders.CONTENT_TYPE);
		if ((contentTypeList != null) && (contentTypeList.size() > 0)) {
			Object mediaTypeObject = contentTypeList.get(0);
			if (mediaTypeObject instanceof MediaType) {
				mediaType = (MediaType) mediaTypeObject;
			} else {
				String mediaTypeString = getStringValueFromHeader(mediaTypeObject);
				mediaType = MediaType.valueOf(mediaTypeString);
			}
		} else {
			headerMap.add(HttpHeaders.CONTENT_TYPE, mediaType);
		}

		final MessageBodyWriter<Object> finalWriter = writer;
		final Object finalEntity = entity;
		final MediaType finalMediaType = mediaType;
		response.setBody(new MessageBody2Write() {

			AccessControlContext context = AccessController.getContext();
			Subject subject = AccessController.doPrivileged(new PrivilegedAction<Subject>() {
				@Override
				public Subject run() {
					return Subject.getSubject(context);
				}
			});
			
			@Override
			public void writeTo(final WritableByteChannel out) throws IOException {
				final OutputStream headerFlushingOutputStream = new FirstByteActionOutputStream(
						Channels.newOutputStream(out), new Runnable() {
					// this is executed when the first character is written to
					// the stream

					@Override
					public void run() {
						try {
							setDefaultCacheControlHeader(headerMap);
							flushHeaders(headerMap, response);
						} catch (HandlerException ex) {
							logger.error("Exception {}", ex.toString(), ex);
						}
					}
				});

				if (subject != null) {
					try {
						Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {

							@Override
							public Object run() throws Exception {
								writeTo(headerFlushingOutputStream, out);
								return null;
							}
						});
					} catch (PrivilegedActionException ex) {
						Throwable cause = ex.getCause();
						if (cause instanceof RuntimeException) {
							throw (RuntimeException) cause;
						} else {
							throw new RuntimeException(cause);
						}
					}
				} else {
					writeTo(headerFlushingOutputStream, out);
				}
			}

			private void writeTo(OutputStream firstByteActionOut, final WritableByteChannel out)
					throws IOException{
				try {
					finalWriter.writeTo(finalEntity, finalEntity.getClass(), entityType,
							annotations, finalMediaType, headerMap, firstByteActionOut);
					firstByteActionOut.close();
					JaxRsHandler.localRequest.remove();
				} catch (Exception ex) {
					try {
						BodyStoringResponse responseFake = new BodyStoringResponse(response);
						JaxRsHandler.handleException(ex, request, responseFake);
						final MessageBody body = responseFake.getBody();
						if (body != null) {
							try {
								//doing priviledged as this might invoke doAs to
								//write the body as subject
								AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

									@Override
									public Subject run() throws IOException {
										body.writeTo(out);
										return null;
									}
								});
							} catch (PrivilegedActionException privEx) {
								Throwable cause = privEx.getCause();
								if (cause instanceof IOException) {
									throw (IOException)cause;
								}
								if (cause instanceof RuntimeException) {
									throw (RuntimeException)cause;
								}
								if (cause instanceof Error) {
									throw (Error)cause;
								}
								throw new RuntimeException(cause);
							}
							
						}
					} catch (HandlerException e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
	}

	private static List<MediaType> expandListWithConcreterTypesFromAccept(List<MediaType> mediaTypesList, AcceptHeader acceptHeader) {
		Collection<MediaType> addition = new HashSet<MediaType>();
		for (MediaType mediaType : mediaTypesList) {
			for (MediaType acceptingType : acceptHeader.getAcceptingMediaType(mediaType)) {
				if (MediaTypeComparator.compareByWildCardCount(acceptingType, mediaType) == -1) {
					addition.add(acceptingType);
				}
			}
		}
		final List<MediaType> result = new ArrayList<MediaType>();
		result.addAll(mediaTypesList);
		result.addAll(addition);
		return result;
	}

	private static String getExplanation(int responseStatus) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter out = new PrintWriter(stringWriter);
		out.println("<html>");
		out.println("<head>");
		out.print("<title>");
		out.print("Error: ");
		out.print(responseStatus);
		out.println("</title>");
		out.print("<body>");
		if (responseStatus >= 500) {
			writeServerError(out, responseStatus);
		} else {
			writeClientError(out, responseStatus);
		}
		out.print("</body>");
		out.println("</head>");
		out.println("</html>");
		return stringWriter.toString();
	}

	private static void writeServerError(PrintWriter out, int responseStatus) {
		out.println("<h1>Server error</h1>");
		out.println("<p>An error occurred and the server was unable to process your " +
				"request. We apologize for the inconveniences</p>");

	}

	private static void writeClientError(PrintWriter out, int responseStatus) {
		if (responseStatus == 404) {
			out.println("<h1>Not found</h1>");
			out.println("<p>The requested resource does not exist on this server. " +
					"Please check the entered address (URI). If you followed a link " +
					"please inform the creator of the page containing the link.</p>");
		} else {
			out.println("<h1>Client error</h1>");
			out.println("<p>The server was unable to process your request. This might " +
					"be caused by properties of your request incompatible with the " +
					"capacities of the server for the requested resource.</p>");
		}
	}

	private static List<MediaType> getWriterProduces(MessageBodyWriter<?> writer) {
		List<MediaType> result = new ArrayList<MediaType>();
		Produces writerProduces = writer.getClass().getAnnotation(
				Produces.class);
		if (writerProduces != null) {
			for (String produced : writerProduces.value()) {
				MediaType producibleType = MediaType.valueOf(produced);
				result.add(producibleType);
			}
		} else {
			result.add(MediaType.WILDCARD_TYPE);
		}
		return result;
	}

	private static MessageBodyWriter<Object> getJafMethodBodyWriter(
			Object entity, List<MediaType> methodProducibleMediaTypesList)
			throws IOException {
		MediaType mediaType;
		if (methodProducibleMediaTypesList.size() > 0) {
			mediaType = methodProducibleMediaTypesList.get(0);
		} else {
			mediaType = MediaType.WILDCARD_TYPE;
		}
		try {
			return new JafMessageBodyWriter<Object>(entity, mediaType);
		} catch (UnsupportedDataTypeException ex) {
			javax.ws.rs.core.Response r = javax.ws.rs.core.Response.status(
					Status.INTERNAL_SERVER_ERROR).entity(
					"No suitable MessageBodyWriter available").type(
					MediaType.TEXT_PLAIN_TYPE).build();
			throw new WebApplicationException(r);
		}
	}

	private static void flushHeaders(MultivaluedMap<String, Object> headerMap,
			Response response) throws HandlerException {
		for (String headerNameString : headerMap.keySet()) {
			List<Object> values = headerMap.get(headerNameString);
			for (Object object : values) {
				final String stringValue = getStringValueFromHeader(object);
				response.setHeader(HeaderName.get(headerNameString),
						stringValue);
			}
		}
	}

	private static <T> String getStringValueFromHeader(T headerObject) {
		HeaderDelegate<T> headerDelegate = RuntimeDelegate.getInstance().createHeaderDelegate((Class<T>)headerObject.getClass());
		String mediaTypeString = headerDelegate != null ? headerDelegate.toString(headerObject) : headerObject.toString();
		return mediaTypeString;
	}

	private static MediaType getConcreteMediaTypeFromPattern(
			final MediaType mediaType) {
		if (mediaType.isWildcardSubtype()) {
			return MediaType.APPLICATION_OCTET_STREAM_TYPE;
		}
		Map<String, String> params = mediaType.getParameters();
		if (!params.containsKey("q")) {
			return mediaType;
		}
		Map<String, String> resultParams = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			final String key = entry.getKey();
			if (!key.equals("q")) {
				resultParams.put(key, entry.getValue());
			}
		}
		return new MediaType(mediaType.getType(), mediaType.getSubtype(),
				resultParams);
	}

	/**
	 *
	 * @param <T>
	 * @param collection
	 * @param comparator
	 * @return a list containing sets of instances for which the comparator returns 0
	 * in oder
	 */
	private static <T> List<Set<T>> getSortedClasses(Collection<T> collection,
			Comparator<T> comparator) {
		List<Set<T>> result = new ArrayList<Set<T>>();
		if (collection.size() > 0) {
			Set<T> pivotSet = new HashSet<T>();
			Collection<T> before = new ArrayList<T>();
			Collection<T> after = new ArrayList<T>();
			Iterator<T> iterator = collection.iterator();
			T pivot = iterator.next();
			pivotSet.add(pivot);
			while (iterator.hasNext()) {
				T next = iterator.next();
				int comparison = comparator.compare(next, pivot);
				if (comparison > 0) {
					after.add(next);
				} else {
					if (comparison < 0) {
						before.add(next);
					} else {
						pivotSet.add(next);
					}
				}
			}
			result.addAll(getSortedClasses(before, comparator));
			result.add(pivotSet);
			result.addAll(getSortedClasses(after, comparator));
		}
		return result;

	}

	/**
	 * 
	 * @param writer
	 * @param mediaType
	 * @return 0 is mediaType ismatched only by wildcard in the @Produces of writer,
	 *	 1, if the supertype is concrete, 2 if the subtype is concrete too
	 */
	private static int getWriterConcreteness(MessageBodyWriter<Object> writer, MediaType mediaType) {
		Produces produces = writer.getClass().getAnnotation(Produces.class);
		int result = 0;
		if (produces != null) {
			for (String producedValue : produces.value()) {
				MediaType producesType = MediaType.valueOf(producedValue);
				if (producesType.isCompatible(mediaType)) {
					int concreteness = 2 - MediaTypeComparator.countWildChars(producesType);
					if (concreteness > result) {
						result = concreteness;
					}
				}
			}
		}
		return result;
	}

	private static void setDefaultCacheControlHeader(MultivaluedMap<String, Object> headerMap) {
		if (headerMap.containsKey(HeaderName.CACHE_CONTROL.toString()) ||
				headerMap.containsKey(HeaderName.EXPIRES.toString()) ||
				headerMap.containsKey(HeaderName.PRAGMA.toString()) ||
				headerMap.containsKey(HeaderName.LAST_MODIFIED.toString()) ||
				headerMap.containsKey("ETag")) {
			return;
		}
		headerMap.putSingle(HeaderName.CACHE_CONTROL.toString(), "no-cache");
	}
}
