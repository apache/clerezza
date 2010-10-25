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
package org.apache.clerezza.triaxrs.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.triaxrs.InjectionUtilities;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.RootResources;
import org.apache.clerezza.triaxrs.WebRequest;
import org.apache.clerezza.triaxrs.WebRequestProxy;
import org.apache.clerezza.triaxrs.parameterinjectors.UnsupportedFieldType;
import org.wymiwyg.wrhapi.HandlerException;

/**
 * CascadingProviders is an implementation of {@link Providers} and it
 * contains a tree of {@link ProvidersImpl}s where each {@link ProvidersImpl}
 * is assigned a certain path prefix
 *
 * @author hasan
 *
 * since version 0.5
 */
public class CascadingProviders implements Providers {

	private Logger logger = LoggerFactory.getLogger(CascadingProviders.class);

	CascadeNode root = new CascadeNode();

	public void addClass(Class<?> componentClass, String pathPrefix) {
		try {
			addInstance(componentClass.newInstance(), pathPrefix);
		} catch (InstantiationException ex) {
			logger.error("Exception {}", ex);
		} catch (IllegalAccessException ex) {
			logger.error("Exception {}", ex);
		}
	}

	public void addInstance(Object component, String pathPrefix) {
		injectContext(component);
		String[] pathSections = getPathSections(pathPrefix);
		CascadeNode current = root;
		for (String section : pathSections) {
			if (!section.equals("")) {
				current = current.createChild(section);
			}
		}
		current.getProviders().addInstance(component);
	}

	/**
	 * Injects context object in the fields of the provider annotated with
	 * @Context.
	 *
	 * @param provider
	 */
	private void injectContext(Object provider) {
		WebRequest requestProxy = WebRequestProxy.createProxy();
		try {
			InjectionUtilities.injectFields(requestProxy, null, JaxRsHandler.providers, provider);
		} catch (HandlerException ex) {
			logger.debug("Exception {}", ex);
			throw new RuntimeException(ex);
		} catch (UnsupportedFieldType ex) {
			logger.debug("Exception {}", ex);
			throw new RuntimeException(ex);
		}
	}

	private String[] getPathSections(String pathPrefix) {
		if (pathPrefix.startsWith("/")) {
			return pathPrefix.substring(1).split("/");
		} else {
			return pathPrefix.split("/");
		}
	}

	public void removeInstance(Object component, String pathPrefix) {
		String[] pathSections = getPathSections(pathPrefix);
		CascadeNode current = root;
		CascadeNode prev = current;
		int pos = 0;
		for (String section : pathSections) {
			if (current == null) {
				break;
			}
			pos++;
			prev = current;
			if (!section.equals("")) {
				current = current.getChild(section);
			}
		}
		if (current != null) {
			ProvidersImpl providers = current.getProviders();
			providers.removeInstance(component);
			if (providers.isEmpty()) {
				if (!current.hasChild() && (pos > 0)) {
					prev.deleteChild(pathSections[pos - 1]);
				}
			}
		}
	}

	@Override
	public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t,
			Annotation[] as, MediaType mediaType) {

		return getMessageBodyReader(c, t, as, mediaType,
				getRequestUriWithoutStartingSlash());
	}

	private String getRequestUriWithoutStartingSlash() {
		String requestUri = RootResources.getCurrentRequestUri();
		if (requestUri.startsWith("/")) {
			return requestUri.substring(1);
		} else {
			return requestUri;
		}
	}

	/**
	 * an intermediate method with package visibility against which the tests run
	 */
	<T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t,
			Annotation[] as, MediaType mediaType, String currentRequestUri) {

		String[] pathSections = currentRequestUri.split("/");
		return getMessageBodyReader(c, t, as, mediaType, root, pathSections, 0);
	}

	private <T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t,
			Annotation[] as, MediaType mediaType, CascadeNode node,
			String[] pathSections, int pos) {

		MessageBodyReader<T> result = null;
		if (pos < pathSections.length) {
			CascadeNode child = node.getChild(pathSections[pos]);
			if (child != null) {
				result = getMessageBodyReader(c, t, as, mediaType, child,
						pathSections, pos + 1);
			}
		}
		if (result == null) {
			return node.getProviders().getMessageBodyReader(c, t, as, mediaType);
		} else {
			return result;
		}
	}

	@Override
	public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> c,
			Type t, Annotation[] as, MediaType mediaType) {

		String currentRequestUri = getRequestUriWithoutStartingSlash();
		String[] pathSections = currentRequestUri.split("/");

		return getMessageBodyWriter(c, t, as, mediaType, root, pathSections, 0);
	}

	private <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> c, Type t,
			Annotation[] as, MediaType mediaType, CascadeNode node,
			String[] pathSections, int pos) {

		MessageBodyWriter<T> result = null;
		if (pos < pathSections.length) {
			CascadeNode child = node.getChild(pathSections[pos]);
			if (child != null) {
				result = getMessageBodyWriter(c, t, as, mediaType, child,
						pathSections, pos + 1);
			}
		}
		if (result == null) {
			return node.getProviders().getMessageBodyWriter(c, t, as, mediaType);
		} else {
			return result;
		}
	}

	@Override
	public <T> ContextResolver<T> getContextResolver(Class<T> contextType,
			MediaType mediaType) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(
			final Class<T> c) {

		String currentRequestUri = getRequestUriWithoutStartingSlash();
		String[] pathSections = currentRequestUri.split("/");

		return getExceptionMapper(c, root, pathSections, 0);
	}

	private <T extends Throwable> ExceptionMapper<T> getExceptionMapper(
			final Class<T> c, CascadeNode node,	String[] pathSections, int pos) {

		ExceptionMapper<T> result = null;
		if (pos < pathSections.length) {
			CascadeNode child = node.getChild(pathSections[pos]);
			if (child != null) {
				result = getExceptionMapper(c, child, pathSections, pos + 1);
			}
		}
		if (result == null) {
			return node.getProviders().getExceptionMapper(c);
		} else {
			return result;
		}
	}
}
