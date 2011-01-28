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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;

import org.apache.clerezza.triaxrs.providers.provided.JafMessageBodyReader;
import org.apache.clerezza.triaxrs.util.AcceptHeader;
import org.apache.clerezza.triaxrs.util.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;

/**
 * Wrapps a wrhapi-request and returns the parsed body.
 * 
 * @author reto
 */
public class WebRequestImpl implements WebRequest {

	private static Logger logger = LoggerFactory.getLogger(WebRequestImpl.class);
	
	static class BodyKey {
		private Annotation[] transformationAnnotation;
		private Type genericType;
		private Class<?> type;
		
		public BodyKey(Class<?> type, Type genericType,
				Annotation[] transformationAnnotation) {
			this.type = type;
			this.genericType = genericType;
			this.transformationAnnotation = transformationAnnotation;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final BodyKey other = (BodyKey) obj;
			if (this.transformationAnnotation != other.transformationAnnotation
					&& (this.transformationAnnotation == null || !this.transformationAnnotation
							.equals(other.transformationAnnotation))) {
				return false;
			}
			if (this.genericType != other.genericType
					&& (this.genericType == null || !this.genericType
							.equals(other.genericType))) {
				return false;
			}
			if (this.type != other.type
					&& (this.type == null || !this.type.equals(other.type))) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 89
					* hash
					+ (this.transformationAnnotation != null ? this.transformationAnnotation
							.hashCode()
							: 0);
			hash = 89
					* hash
					+ (this.genericType != null ? this.genericType.hashCode()
							: 0);
			hash = 89 * hash + (this.type != null ? this.type.hashCode() : 0);
			return hash;
		}

	}

	private Request wrhapiRequest;
	private MultivaluedMap<String, String> headers = null;
	private Providers providers;
	private BodyKey bodyKey = null;
	private Object body = null;
	private AcceptHeader acceptHeader = null;

	/**
	 * Constructs a WebRequest with the specified request and using the
	 * body-readers specified in providers
	 * 
	 * @param wrhapiRequest
	 * @param providers
	 */
	WebRequestImpl(Request wrhapiRequest, Providers providers) {
		this.wrhapiRequest = wrhapiRequest;
		this.providers = providers;
	}

	public Request getWrhapiRequest() {
		return wrhapiRequest;
	}

	public MultivaluedMap<String, String> getHeaders() {
		if (headers == null) {
			try {
				headers = getHeadersFromRequest(wrhapiRequest);
			} catch (HandlerException ex) {
				throw new RuntimeException(ex);
			}
		}
		return headers;
	}

	@Override
	public AcceptHeader getAcceptHeader() {
		if (acceptHeader == null) {
			final List<String> acceptHeaderStrings = getHeaders().get(HttpHeaders.ACCEPT);
			logger.debug("Accept-Header: {}", acceptHeaderStrings);
			acceptHeader = new AcceptHeader(acceptHeaderStrings);
		}
		return acceptHeader;
	}

	public <T> T getBodyObject(Class<T> type, Type genericType,
			Annotation[] transformationAnnotation) {

		BodyKey requestBodyKey = new BodyKey(type, genericType,
				transformationAnnotation);
		if (bodyKey != null) {
			if (bodyKey.equals(requestBodyKey)) {
				return (T) body;
			} else {
				throw new RuntimeException(
						"Body already returned as something else");
			}
		}
		bodyKey = requestBodyKey;

		InputStream inputStream = null;
		try {
			final String[] contentTypeHeaders = wrhapiRequest
					.getHeaderValues(HeaderName.CONTENT_TYPE);
			MediaType mediaType;
			if ((contentTypeHeaders == null)
					|| (contentTypeHeaders.length == 0)) {
				mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
			} else {
				mediaType = MediaType.valueOf(contentTypeHeaders[0]);
			}

			MessageBodyReader<T> reader = providers.getMessageBodyReader(type,
					genericType, transformationAnnotation, mediaType);
			inputStream = Channels.newInputStream(wrhapiRequest
					.getMessageBody().read());
			if (reader == null) {
				// try the body-conversion with the Java Activation Framework
				reader = new JafMessageBodyReader<T>();
			}

			T result = reader.readFrom(type, genericType,
					transformationAnnotation, mediaType, headers, inputStream);
			body = result;
			return result;
		} catch (HandlerException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

	}

	/**
	 * helper method to convert the request header to the required map type
	 */
	private MultivaluedMap<String, String> getHeadersFromRequest(Request request)
			throws HandlerException {

		Set<HeaderName> names = request.getHeaderNames();
		MultivaluedMap<String, String> result = new CaseInsensitiveMap<String>();

		if (names == null) {
			return result;
		}

		for (HeaderName headerName : names) {

			String[] headerValues = request.getHeaderValues(headerName);

			for (String s : headerValues) {
				result.add(headerName.toString(), s);
			}
		}

		return result;
	}

	@Override
	public ResponseBuilder evaluatePreconditions(EntityTag arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder evaluatePreconditions(Date arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder evaluatePreconditions(Date arg0, EntityTag arg1) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getMethod() {
		try {
            Method method = wrhapiRequest.getMethod();
            if (method != null) {
                return (method.toString()).substring(8);
            } else {
                return null;
            }
        } catch (HandlerException ex) {
            throw new RuntimeException(ex);
        }
	}

	@Override
	public Variant selectVariant(List<Variant> arg0)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
