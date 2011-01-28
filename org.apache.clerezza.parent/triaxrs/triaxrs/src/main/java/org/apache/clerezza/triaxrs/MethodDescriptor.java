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

import java.lang.reflect.Method;

import org.apache.clerezza.triaxrs.util.MethodUtil;
import org.apache.clerezza.triaxrs.util.URITemplate;

/** Describes a sub-resource method or sub-resource locator.
 * 
 * Sortable using literal characters in each member as the primary key (descending
order), the number of capturing groups as a secondary key (descending order), and the source
of each member as tertiary key sorting sub-resource methods ahead of sub-resource locators.
 *
 * @author reto
 */
public class MethodDescriptor implements Comparable<MethodDescriptor> {
	
	private Method method;
	private URITemplate uriTemplate;
	private boolean subResourceLocator;
	
	/**
	 * 
	 * @param method a sub-resource method or sub-resource locator
	 * @param encodedPathTemplate
	 */
	MethodDescriptor(Method method, String encodedPathTemplate) {
		this.method = method;
		this.uriTemplate = new URITemplate(encodedPathTemplate);	
		subResourceLocator = !MethodUtil.isResourceMethod(method);
	}

	public Method getMethod() {
		return method;
	}

	public URITemplate getUriTemplate() {
		return uriTemplate;
	}
	
	boolean isSubResourceLocator() {
		return subResourceLocator;
	}

	@Override
	public int compareTo(MethodDescriptor o) {
		if (equals(o)) return 0;

		//compare uri template. If Uri template is the same, use the instance as the second key
		int uriCompare = uriTemplate.compareTo(o.uriTemplate);

		if (uriCompare != 0) {
			return uriCompare;
		}

		//sub-resource methods first
		if (!this.subResourceLocator && o.subResourceLocator) {
			return -1;
		} else if (this.subResourceLocator && !o.subResourceLocator) {
			return 1;
		}
		return method.toGenericString().compareTo(o.method.toGenericString());
	}

	@Override
	public String toString() {
		return method.getName()+" at "+uriTemplate;
	}



}
