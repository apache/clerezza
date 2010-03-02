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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.clerezza.triaxrs.util.PathMatching;
import org.apache.clerezza.triaxrs.util.TemplateEncoder;
import org.apache.clerezza.utils.UriException;
import org.apache.clerezza.utils.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wymiwyg.wrhapi.HandlerException;

/**
 * Manages a set of root-resources delivering a suitable root-resource given a
 * path.
 * 
 * @author reto
 */
public class RootResources {

	/**
	 * @author mir
	 * 
	 */
	public static class ResourceAndPathMatching {

		private Object rootResource;
		private PathMatching pathMatching;

		public ResourceAndPathMatching(Object rootResource,
				PathMatching pathMatching) {
			this.rootResource = rootResource;
			this.pathMatching = pathMatching;
		}

		public Object getRootResource() {
			return rootResource;
		}

		public PathMatching getPathMatching() {
			return pathMatching;
		}

	}

	private static ThreadLocal<String> requestUri = new ThreadLocal<String>() {
	};

	public static String getCurrentRequestUri() {
		return requestUri.get();
	}

	final static private Logger logger = LoggerFactory
			.getLogger(RootResources.class);

	/**
	 * we store the resource descriptors
	 */
	SortedSet<RootResourceDescriptor> rootResourceDescriptors = new TreeSet<RootResourceDescriptor>();

	/**
	 * adds a {@link RootResourceDescriptor}
	 * 
	 * @param descriptor
	 */
	public void add(RootResourceDescriptor descriptor) {
		// TODO Flag an error if the set includes more than one instance
		// of the same class.
		rootResourceDescriptors.add(descriptor);
	}

	/**
	 * removes a {@link RootResourceDescriptor}
	 * 
	 * @param descriptor
	 */
	public void remove(RootResourceDescriptor descriptor) {
		rootResourceDescriptors.remove(descriptor);
	}

	/**
	 * 
	 * @return the number of available root resources
	 */
	public int size() {
		return rootResourceDescriptors.size();
	}

	/**
	 * Get the best matching root-resource for a path
	 * 
	 * @param uriPath
	 * @return the matching root-resource or null if no root resources matches
	 */
	public RootResourceDescriptor getDescriptor(String uriPath) {
		for (RootResourceDescriptor descriptor : rootResourceDescriptors) {

			PathMatching pathMatching = descriptor.getUriTemplate().match(
					uriPath);
			if (pathMatching != null) {
				return descriptor;
			}
		}
		return null;
	}

	ResourceAndPathMatching getResourceAndPathMatching(WebRequest request)
			throws HandlerException, NoMatchingRootResourceException {
		String uriPath;
		try {
			uriPath = UriUtil.encodePartlyEncodedPath(request.getWrhapiRequest().getRequestURI().getPath(), "UTF-8");
		} catch (UriException ex) {
			throw new RuntimeException(ex);
		}
		requestUri.set(uriPath);

		PathMatching pathMatching = null;
		RootResourceDescriptor descriptor = null;
		Iterator<RootResourceDescriptor> descriptorIter = rootResourceDescriptors
				.iterator();
		while (descriptorIter.hasNext()) {
			descriptor = descriptorIter.next();
			pathMatching = descriptor.getUriTemplate().match(uriPath);
			if (pathMatching != null) {
				break;
			}
		}

		if (pathMatching == null) {
			throw new NoMatchingRootResourceException();
		}
		return new ResourceAndPathMatching(descriptor.getInstance(request,
				pathMatching.getParameters()), pathMatching);
	}
}
