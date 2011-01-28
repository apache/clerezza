/*******************************************************************************
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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/
package org.apache.clerezza.triaxrs.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import org.apache.clerezza.triaxrs.util.MediaTypeUtils;
import org.apache.clerezza.triaxrs.util.MultivaluedMapImpl;
import org.apache.clerezza.triaxrs.util.PathSegmentImpl;
import org.apache.clerezza.triaxrs.util.RestConstants;
import org.apache.clerezza.triaxrs.util.StringUtils;
import org.apache.clerezza.triaxrs.util.uri.UriEncoder;
import org.apache.clerezza.triaxrs.util.uri.UriPathNormalizer;

/**
 * Various methods for URI/URL manipulation.
 */
public class UriHelper {

	// no instances
	private UriHelper() {
	}

	/**
	 * Normalize URI:
	 * <ul>
	 * <li>The last character in URI is NOT '/'</li>
	 * </ul>
	 *
	 * @param uri input URI
	 * @return normalized URI
	 */
	public static String removeTrailingSlash(String uri) {

		if (uri != null && uri.length() > 0 && uri.lastIndexOf('/') == uri.length() - 1) {
			return uri.substring(0, uri.length() - 1);
		}
		return uri;
	}

	/**
	 * Remove parameters for the url
	 *
	 * @param requestUri input URI
	 * @return input URI without query string
	 */
	public static String stripQueryString(String requestUri) {

		int idx;
		if (requestUri != null && (idx = requestUri.lastIndexOf('?')) != -1) {
			requestUri = requestUri.substring(0, idx);
			return removeTrailingSlash(requestUri);
		}
		return requestUri;
	}

	/**
	 * Get query string.
	 *
	 * @param requestUri input URI
	 * @return query string from input URI
	 */
	public static String getQueryString(String requestUri) {

		int idx;
		if (requestUri != null && (idx = requestUri.lastIndexOf('?')) != -1) {
			return requestUri.substring(idx + 1, requestUri.length());
		}
		return null;
	}

	/**
	 * Strip name (last section in the path).
	 *
	 * @param requestUri input URI
	 * @return part of URI after the last slash
	 */
	public static String stripName(String requestUri) {

		int idx;
		if (requestUri != null) {
			requestUri = removeTrailingSlash(requestUri);
			if ((idx = requestUri.lastIndexOf('/')) != -1) {
				return requestUri.substring(0, idx);
			}
		}
		return null;
	}

	/**
	 * Get the last section of the path.
	 *
	 * @param uri URI.
	 * @return name.
	 */
	public static String getNameFromPath(String uri) {

		if (uri != null) {
			uri = removeTrailingSlash(uri);
			return uri.substring(uri.lastIndexOf('/') + 1, uri.length());
		}
		return null;
	}

	/**
	 * Hide password from the URL.
	 *
	 * @param url the URL to hide the password from
	 * @return URL with user information removed
	 */
	public static String hidePassword(URI url) {

		if (url == null) {
			return null;
		}

		String ui = url.getRawUserInfo();
		if (ui == null) {
			return url.toString();
		}

		String str = url.toString();

		int pidx = str.indexOf(ui);
		if (pidx < 0) {
			return str;
		}

		return str.substring(0, pidx) + str.substring(pidx + ui.length() + 1); // 1
		// for
		// @
	}

	/**
	 * Hide password from the URL.
	 *
	 * @param url the URL to hide the password from
	 * @return URL with user information removed
	 */
	public static String hidePassword(String url) {

		if (url == null) {
			return null;
		}
		return hidePassword(URI.create(url));
	}

	/**
	 * Hides password from all the urls in the list.
	 *
	 * @param urls the list of urls to hide the password from
	 * @return URL with user information removed
	 */
	public static List<String> hidePassword(List<String> urls) {

		if (urls == null) {
			return urls;
		}
		List<String> ret = new ArrayList<String>(urls.size());
		for (String url : urls) {
			ret.add(hidePassword(url));
		}
		return ret;
	}

	/**
	 * Relativize URI path. Can add ".." and "." segments.
	 *
	 * @param basePath relative against this path
	 * @param pathToRelativize path to change
	 * @return a relativized path
	 */
	public static String relativize(String basePath, String pathToRelativize) {
		String normalizeBasePath = removeStartSlash(UriPathNormalizer.normalize(basePath));
		String[] splitBase = StringUtils.fastSplit(normalizeBasePath, "/", false); //$NON-NLS-1$
		String normalizedPathToRelativize =
				removeStartSlash(UriPathNormalizer.normalize(pathToRelativize));
		String[] splitPath = StringUtils.fastSplit(normalizedPathToRelativize, "/", false); //$NON-NLS-1$
		int pos = 0;
		while (pos < splitBase.length && pos < splitPath.length
				&& splitBase[pos].equals(splitPath[pos])) {
			pos++;
		}
		StringBuilder result = new StringBuilder();
		if (pos == splitBase.length && !normalizeBasePath.endsWith("/")) { // at //$NON-NLS-1$
			// the
			// end
			// of
			// base
			// .../baseEnd
			String lastSegment = splitBase[pos - 1];
			result.append(lastSegment);
			for (; pos < splitPath.length; pos++) {
				if (result.length() != 0) {
					result.append('/');
				}
				result.append(splitPath[pos]);
			}
		} else if (pos == splitBase.length) { // at the end of base .../baseEnd/
			for (; pos < splitPath.length; pos++) {
				if (result.length() != 0) {
					result.append('/');
				}
				result.append(splitPath[pos]);
			}
		} else {
			for (int posBase = pos; posBase < splitBase.length - 1; posBase++) {
				if (result.length() != 0) {
					result.append('/');
				}
				result.append(".."); //$NON-NLS-1$
			}
			if (normalizeBasePath.endsWith("/")) { //$NON-NLS-1$
				if (result.length() != 0) {
					result.append('/');
				}
				result.append(".."); //$NON-NLS-1$
			}
			for (; pos < splitPath.length; pos++) {
				if (result.length() != 0) {
					result.append('/');
				}
				result.append(splitPath[pos]);
			}
		}
		if (result.length() == 0) {
			result.append('.');
		}
		return result.toString();
	}

	/**
	 * @param s a non-null string
	 * @return s value with optional first character '/' removed
	 */
	public static String removeStartSlash(String s) {
		if (s.startsWith("/")) { //$NON-NLS-1$
			return s.substring(1);
		} else {
			return s;
		}
	}

	/**
	 * Appends a path a baseUri.
	 *
	 * @param baseUri the base uri, can but needn't to end with a '/', not
	 *            <code>null</code>
	 * @param path path append, can but needn't to start with a '/'
	 * @return a string with path appended path-wise correctly to the path
	 */
	public static String appendPathToBaseUri(String baseUri, String path) {
		if (baseUri == null) {
			throw new NullPointerException("baseUri"); //$NON-NLS-1$
		}
		if (path == null || path.length() == 0) {
			return baseUri;
		}
		boolean endSlash = baseUri.endsWith("/"); //$NON-NLS-1$
		boolean startSlash = path.charAt(0) == '/';
		if (startSlash && endSlash) {
			return baseUri + path.substring(1);
		} else if (startSlash || endSlash) {
			return baseUri + path;
		} else {
			return baseUri + '/' + path;
		}
	}

	/**
	 * Append an alt parameter to an uri.
	 *
	 * @param uri an uri without any parameters, not null
	 * @param mediaType the value of the alt parameter, not null
	 * @return string containing an uri, media type is uri-encoded
	 */
	public static String appendAltToPath(String uri, MediaType mediaType) {
		if (uri == null) {
			throw new NullPointerException("uri"); //$NON-NLS-1$
		}
		if (mediaType == null) {
			throw new NullPointerException("mediaType"); //$NON-NLS-1$
		}
		StringBuilder result = new StringBuilder(uri);
		if (uri.length() == 0) {
			result.append('.');
		}
		char appendCharacter = '?';
		// if the uri already has query parameters
		if ((uri.indexOf('?') != -1)) {
			appendCharacter = '&';
		}
		result.append(appendCharacter);
		result.append(RestConstants.REST_PARAM_MEDIA_TYPE);
		result.append('=');
		result.append(MediaTypeUtils.toEncodedString(mediaType));
		return result.toString();
	}

	/**
	 * Constructs a string with URI in unescaped form. Use as woraround with
	 * unencoding/double encoding problems with URI constructor,
	 * <code>new Uri(constructUri(...)).toASCIIString()</code> should be the
	 * original URI.
	 *
	 * @param scheme URI.getScheme
	 * @param userInfo URI.getRawUserInfo
	 * @param host URI.getRawHost
	 * @param port URI.getPort; -1 = no port
	 * @param path URI.getRawPath
	 * @param query URI.getRawQuery
	 * @param fragment URI.getRawFragment
	 * @return URI with reserved characters escaped
	 */
	public static String contructUri(String scheme,
			String userInfo,
			String host,
			int port,
			String path,
			String query,
			String fragment) {
		StringBuilder buffer = new StringBuilder();
		if (scheme != null) {
			buffer.append(scheme);
			buffer.append("://"); //$NON-NLS-1$
		}
		if (userInfo != null) {
			buffer.append(userInfo);
			buffer.append("@"); //$NON-NLS-1$
		}
		if (host != null) {
			buffer.append(host);
		}
		if (port != -1) {
			buffer.append(":"); //$NON-NLS-1$
			buffer.append(port);
		}
		if (path != null) {
			buffer.append(path);
		}
		if (query != null) {
			buffer.append("?"); //$NON-NLS-1$
			buffer.append(query);
		}
		if (fragment != null) {
			buffer.append("#"); //$NON-NLS-1$
			buffer.append(fragment);
		}
		return buffer.toString();
	}

	/**
	 * The method builds the query parameters String e.g.
	 * param1=value1&param2=value2
	 *
	 * @param queryParams Map of query parameters
	 * @param escapeKeyParam indicates if to escape the parameter's name and
	 *            value
	 * @return String the query parameters String
	 */
	public static String getQueryParamsStr(Map<String, String[]> queryParams, boolean escapeKeyParam) {
		if (queryParams == null || queryParams.size() < 1) {
			return ""; //$NON-NLS-1$
		}
		StringBuilder queryParamsStr = new StringBuilder();
		Set<String> queryParamsKey = queryParams.keySet();
		String[] params;
		String appendStr = ""; //$NON-NLS-1$
		for (String key : queryParamsKey) {
			params = queryParams.get(key);
			if (params == null) {
				continue;
			}
			for (String paramInArray : params) {
				queryParamsStr.append(appendStr);
				queryParamsStr.append(escapeKeyParam ? UriEncoder.encodeString(key) : key).append("=").append(escapeKeyParam ? UriEncoder.encodeString(paramInArray) //$NON-NLS-1$
						: paramInArray);
				appendStr = "&"; //$NON-NLS-1$
			}

		}
		return queryParamsStr.toString();
	}

	/**
	 * The method appends query parameters to path
	 *
	 * @param uri
	 * @param queryParams Map of query parameters
	 * @param escapeKeyParam indicates if to escape the parameter's name and
	 *            value
	 * @return String the path with query parameters
	 */
	public static String appendQueryParamsToPath(String uri,
			Map<String, String[]> queryParams,
			boolean escapeKeyParam) {
		if (uri == null) {
			throw new NullPointerException("uri"); //$NON-NLS-1$
		}
		if (queryParams == null || queryParams.size() < 1) {
			return uri;
		}
		StringBuilder result = new StringBuilder(uri);
		if (uri.length() == 0) {
			result.append('.');
		}
		char appendCharacter = '?';
		// if the uri already has query parameters
		if ((uri.indexOf('?') != -1)) {
			appendCharacter = '&';
		}

		return uri + appendCharacter + getQueryParamsStr(queryParams, escapeKeyParam);
	}

	/**
	 * Parses a uri path into a list of PathSegements
	 *
	 * @param path the path to parse
	 * @return list of PathSegement instances
	 */
	public static List<PathSegment> parsePath(String path) {
		String[] segmentsArray = StringUtils.fastSplitTemplate(path, "/", true); //$NON-NLS-1$
		List<PathSegment> pathSegments = new ArrayList<PathSegment>(segmentsArray.length);
		// go over all the segments and add them
		for (String segment : segmentsArray) {
			pathSegments.add(new PathSegmentImpl(segment, null));
		}
		return pathSegments;
	}

	/**
	 * Parses a query string (without the leading '?') into a map of parameters
	 * and values
	 *
	 * @param queryStr the query strin to parse
	 * @return a map of query parameters and values
	 */
	public static MultivaluedMap<String, String> parseQuery(String queryStr) {
		MultivaluedMap<String, String> query = new MultivaluedMapImpl<String, String>();
		if (queryStr == null || queryStr.length() == 0) {
			return query;
		}
		String[] paramsArray = StringUtils.fastSplit(queryStr, "&"); //$NON-NLS-1$
		for (String param : paramsArray) {
			int index = param.indexOf('=');
			String name = param;
			String value = null;
			if (index != -1) {
				name = param.substring(0, index);
				value = param.substring(index + 1);
			}
			query.add(name, value);
		}
		return query;
	}

	/**
	 * Normalize input uri according to <a>
	 * href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a> section 6.2.2. -
	 * Syntax-Based Normalization
	 *
	 * @param string
	 * @return normalized instance of uri
	 */
	public static String normalize(String uri) {

		// Path Segment Normalization
		uri = UriPathNormalizer.normalize(uri);

		// Percent-Encoding Normalization & Case Normalization
		uri = UriEncoder.normalize(uri);

		return uri;
	}
}
