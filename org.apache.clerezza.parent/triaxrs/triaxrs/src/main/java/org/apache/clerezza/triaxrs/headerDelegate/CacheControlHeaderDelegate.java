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
package org.apache.clerezza.triaxrs.headerDelegate;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

public class CacheControlHeaderDelegate implements HeaderDelegate<CacheControl> {

	private static final String S_MAXAGE = "s-maxage";
	private static final String MAX_AGE = "max-age";
	private static final String PROXY_REVALIDATE = "proxy-revalidate";
	private static final String MUST_REVALIDATE = "must-revalidate";
	private static final String NO_TRANSFORM = "no-transform";
	private static final String NO_STORE = "no-store";
	private static final String NO_CACHE = "no-cache";
	private static final String PRIVATE = "private";

	@Override
	public CacheControl fromString(String value) throws IllegalArgumentException {
		throw new UnsupportedOperationException(
				"JAX-RS CacheControl type is designed to support only cache-response-directives");
	}

	@Override
	public String toString(CacheControl header) {
		if (header == null) {
			throw new IllegalArgumentException("CacheControl header is null");
		}

		StringBuffer cacheControlHeader = new StringBuffer();

		if (header.isPrivate()) {
			appendDirectiveWithValues(cacheControlHeader,
					PRIVATE,
					generateDirectiveValuesList(header.getPrivateFields()));
		}
		if (header.isNoCache()) {
			appendDirectiveWithValues(cacheControlHeader,
					NO_CACHE,
					generateDirectiveValuesList(header.getNoCacheFields()));
		}
		if (header.isNoStore()) {
			appendDirective(cacheControlHeader, NO_STORE);
		}
		if (header.isNoTransform()) {
			appendDirective(cacheControlHeader, NO_TRANSFORM);
		}
		if (header.isMustRevalidate()) {
			appendDirective(cacheControlHeader, MUST_REVALIDATE);
		}
		if (header.isProxyRevalidate()) {
			appendDirective(cacheControlHeader, PROXY_REVALIDATE);
		}
		if (header.getMaxAge() != -1) {
			appendDirective(cacheControlHeader, MAX_AGE, header.getMaxAge());
		}
		if (header.getSMaxAge() != -1) {
			appendDirective(cacheControlHeader, S_MAXAGE, header.getSMaxAge());
		}

		// Add extension cache control directives
		Set<Entry<String, String>> entrySet = header.getCacheExtension().entrySet();
		if (entrySet != null && entrySet.size() > 0) {
			for (Entry<String, String> entry : entrySet) {
				appendExtentionDirective(cacheControlHeader, entry.getKey(), quoteValue(entry.getValue()));
			}
		}

		return cacheControlHeader.toString();
	}

	private void appendDirective(StringBuffer cacheControlHeader, String directive) {
		if (cacheControlHeader.length() > 0) {
			cacheControlHeader.append(", "); //$NON-NLS-1$
		}
		cacheControlHeader.append(directive);

	}

	/**
	 * Generate a list of directive values, separated by ","
	 *
	 * @param values - list of directive values
	 * @return String - string that holds concatenation of directive values
	 *         separated by ", " delimiter
	 */
	private String generateDirectiveValuesList(List<String> values) {
		StringBuffer stringBuffer = new StringBuffer();
		for (String value : values) {
			if (stringBuffer.length() > 0) {
				stringBuffer.append(", "); //$NON-NLS-1$
			}
			stringBuffer.append(value);
		}
		return stringBuffer.toString();

	}

	/**
	 * @param cacheControlHeader
	 * @param directive
	 * @param directiveValue
	 */
	private void appendDirectiveWithValues(StringBuffer cacheControlHeader,
			String directive,
			String directiveValue) {
		appendDirective(cacheControlHeader, directive);
		if (directiveValue != null && directiveValue.length() > 0) {
			cacheControlHeader.append("=\""); //$NON-NLS-1$
			cacheControlHeader.append(directiveValue);
			cacheControlHeader.append("\""); //$NON-NLS-1$
		}
	}

	/**
	 * Appends directive to a Cache Control Header
	 *
	 * @param cacheControlHeader - String buffer that holds Cache Control Header
	 * @param directive - directive to append
	 * @param value - directive's value
	 */
	private void appendExtentionDirective(StringBuffer cacheControlHeader,
			String directive,
			String value) {
		appendDirective(cacheControlHeader, directive);
		if (value != null && value.length() > 0) {
			cacheControlHeader.append("="); //$NON-NLS-1$
			cacheControlHeader.append(value);
		}
	}

	/**
	 * Appends directive to a Cache Control Header
	 *
	 * @param cacheControlHeader - String buffer that holds Cache Control Header
	 * @param directive - directive to append
	 * @param value - directive's value
	 */
	private void appendDirective(StringBuffer cacheControlHeader, String directive, int value) {
		appendDirective(cacheControlHeader, directive);
		cacheControlHeader.append("="); //$NON-NLS-1$
		cacheControlHeader.append(value);
	}

	/**
	 * Quotes extension cache control directive value if it contains space
	 *
	 * @param value - directives value
	 * @return - directive value, quoted if contains space
	 */
	private String quoteValue(String value) {
		if (value == null) {
			return null;
		}
		if (value.replaceAll("\\s+", "").length() < value.length()) { //$NON-NLS-1$ //$NON-NLS-2$
			return "\"" + value + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		}

		return value;
	}
}
