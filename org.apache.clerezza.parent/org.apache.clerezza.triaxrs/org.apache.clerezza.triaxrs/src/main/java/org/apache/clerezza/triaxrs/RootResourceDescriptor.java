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

import java.util.Map;

import javax.ws.rs.ext.Providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.triaxrs.parameterinjectors.UnsupportedFieldType;
import org.apache.clerezza.triaxrs.util.URITemplate;
import org.wymiwyg.wrhapi.HandlerException;

class RootResourceDescriptor implements Comparable<RootResourceDescriptor> {

	//questionable
	//private Set<Class> httpMethodsOfClass;
	private Class<?> clazz;
	private URITemplate uriTemplate;
	private Object instance;
	private Logger logger = LoggerFactory.getLogger(RootResourceDescriptor.class);

	public RootResourceDescriptor(Class<?> clazz, String pathTemplate) {
		super();
		uriTemplate = new URITemplate(pathTemplate);
		this.clazz = clazz;
	//store methods of the class
	//httpMethodsOfClass = ReflectionUtil.getMethodsOfClass(clazz);
	}

	public RootResourceDescriptor(Class<?> clazz, Object instance,
			String pathTemplate, Providers providers) {
		super();
		uriTemplate = new URITemplate(pathTemplate);
		this.clazz = clazz;
		this.instance = instance;
		if (instance != null) {
			try {
				WebRequest requestProxy = WebRequestProxy.createProxy();
				InjectionUtilities.injectFields(requestProxy, null, providers, instance);
			} catch (HandlerException ex) {
				logger.debug("Exception {}", ex);
				throw new RuntimeException(ex);
			} catch (UnsupportedFieldType ex) {
				logger.debug("Exception {}", ex);
				throw new RuntimeException(ex);
			}
		}
	}

	public Object getInstance(WebRequest request, Map<String, String> pathParams) {
		if (instance != null) {
			return instance;
		} else {
			try {
				return InjectionUtilities.createPreparedInstance(request, pathParams, JaxRsHandler.providers, clazz);
			} catch (HandlerException e) {
				throw new RuntimeException(e);
			} catch (UnsupportedFieldType e) {
				throw new RuntimeException(e);
			}
		}
	}

	public URITemplate getUriTemplate() {
		return uriTemplate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RootResourceDescriptor other = (RootResourceDescriptor) obj;
		if (this.clazz != other.clazz && (this.clazz == null || !this.clazz.
				equals(other.clazz))) {
			return false;
		}
		if (this.uriTemplate != other.uriTemplate && (this.uriTemplate == null || !this.uriTemplate.
				equals(other.uriTemplate))) {
			return false;
		}
		if (this.instance != other.instance && (this.instance == null || !this.instance.
				equals(other.instance))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
		hash =
				37 * hash + (this.instance != null ? this.instance.hashCode() : 0);
		return hash;
	}

	@Override
	public int compareTo(RootResourceDescriptor o) {

		//compare uri template. If Uri template is the same, use the instance as the second key
		int uriCompare = uriTemplate.compareTo(o.uriTemplate);

		if (uriCompare != 0) {
			return uriCompare;
		}

		//instance is first
		if ((this.instance != null) && (o.instance == null)) {
			return -1;
		} else if ((o.instance != null) && (this.instance == null)) {
			return 1;
		}

		return 0;
	}
}
