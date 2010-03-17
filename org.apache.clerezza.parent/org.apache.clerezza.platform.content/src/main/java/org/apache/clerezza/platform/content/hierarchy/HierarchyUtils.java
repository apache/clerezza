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
package org.apache.clerezza.platform.content.hierarchy;

import org.apache.clerezza.rdf.core.UriRef;

/**
 * This class provides utilities for hierarchy related tasks.
 *
 * @author mir
 */
public class HierarchyUtils {

	/**
	 * Extracts the URI of the parent collection from the specified URI.
	 * @param nodeUri
	 * @return URI of the parent collection of the specified URI
	 */
	public static UriRef extractParentCollectionUri(UriRef nodeUri) {
		String uri = nodeUri.getUnicodeString();
		if (uri.endsWith("/")) {
			uri = uri.substring(0, uri.length() - 1);
		}
		uri = uri.substring(0, uri.lastIndexOf("/") + 1);
		return new UriRef(uri);
	}

	static void ensureNonCollectionUri(UriRef uri) {
		if (uri.getUnicodeString().endsWith("/")) {
			throw new IllegalArgumentException(uri + " must not end with a slash");
		}
	}

	static void ensureCollectionUri(UriRef uri) {
		if (!uri.getUnicodeString().endsWith("/")) {
			throw new IllegalArgumentException(uri + " does not end with a slash");
		}
	}

	/**
	 * Makes a collection uri out of the specifed uri. If the provided uri is
	 * already a collection uri (if it ends with '/') then it is returned
	 * unchanged.
	 * @param uri
	 * @return
	 */
	public static UriRef makeCollectionUriRef(UriRef uri) {
		String uriString = uri.getUnicodeString();
		if (uriString.endsWith("/")) {
			return uri;
		} else {
			return new UriRef(uriString + "/");
		}
	}

	/**
	 * If the specified uri is a collection uri, then a resource uri is returned.
	 * If the specified uri is a resource uri, then a collection uri is returned.
	 * @param uri
	 * @return
	 */
	public static UriRef makeOppositeUriRef(UriRef uri) {
		String uriString = uri.getUnicodeString();
		if (uriString.endsWith("/")) {
			return new UriRef(uriString.substring(0, uriString.length() - 1));
		} else {
			return new UriRef(uriString + "/");
		}
	}

	/**
	 * Returns the name contained in the specified uri. The name is the last
	 * segment of the uri path.
	 * @param uri
	 * @return
	 */
	public static String getName(UriRef uri) {
		String uriString = uri.getUnicodeString();
		if (uriString.endsWith("/")) {
			uriString = uriString.substring(0, uriString.length() - 1);
		}
		return uriString.substring(uriString.lastIndexOf("/") + 1, uriString.length());
	}
}
