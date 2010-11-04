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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 */
public class StringUtils {

	public final static String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$

	private StringUtils() {
		// this class should never be created
	}

	public static String valueOf(Map<?, ?> map) {
		if (map == null) {
			return "null"; //$NON-NLS-1$
		}
		StringBuilder builder = new StringBuilder();
		builder.append("{"); //$NON-NLS-1$
		builder.append(lineSeparator);
		for (Iterator<?> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<?, ?> entry = (Entry<?, ?>) iterator.next();
			builder.append(String.valueOf(entry.getKey()));
			builder.append("="); //$NON-NLS-1$
			builder.append(String.valueOf(entry.getValue()));
			builder.append(lineSeparator);
		}
		builder.append("}"); //$NON-NLS-1$
		return builder.toString();
	}

	public static String valueOf(List<?> list) {
		if (list == null) {
			return "null"; //$NON-NLS-1$
		}
		StringBuilder builder = new StringBuilder();
		builder.append("["); //$NON-NLS-1$
		builder.append(lineSeparator);
		for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			builder.append(String.valueOf(object));
			builder.append(lineSeparator);
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}

	public static String[] fastSplit(String string, String delimiter) {
		return fastSplit(string, delimiter, true);
	}

	public static String[] fastSplit(String string, String delimiter, boolean strict) {
		return fastSplit(string, delimiter, strict, false);
	}

	public static String[] fastSplitTemplate(String string, String delimiter) {
		return fastSplitTemplate(string, delimiter, true);
	}

	public static String[] fastSplitTemplate(String string, String delimiter, boolean strict) {
		return fastSplit(string, delimiter, strict, true);
	}

	public static String[] fastSplit(String string,
			String delimiter,
			boolean strict,
			boolean template) {
		if (string == null) {
			return new String[0];
		}

		if (string.equals("")) { //$NON-NLS-1$
			return new String[]{""}; //$NON-NLS-1$
		}

		List<String> tmpResults = new ArrayList<String>();
		int delimiterLength = delimiter.length();
		int delimiterIndex = 0;
		int fromIndex = 0;
		int stringLen = string.length();
		int index = 0;

		// collect all the tokens
		while ((index != -1)) {

			// if the delimiter is at the end of the string
			if (fromIndex >= string.length()) {
				if (strict) {
					tmpResults.add(""); //$NON-NLS-1$
				}
				break;
			}

			if (template) {
				boolean done = false;
				int brackets = 0;
				index = fromIndex;
				int maxIndex = string.length();
				while (!done) {
					if (index >= maxIndex) {
						index = -1;
						done = true;
					} else if (string.startsWith(delimiter, index) && brackets == 0) {
						done = true;
					} else {
						if (string.charAt(index) == '{') {
							++brackets;
						} else if (string.charAt(index) == '}') {
							brackets = (brackets == 0 ? 0 : brackets - 1);
						}
						++index;
					}
				}
			} else {
				index = string.indexOf(delimiter, fromIndex);
			}

			if (index == -1) {
				delimiterIndex = stringLen;
			} else {
				delimiterIndex = index;
			}

			tmpResults.add(string.substring(fromIndex, delimiterIndex));
			fromIndex = delimiterIndex + delimiterLength;
		}

		return tmpResults.toArray(new String[tmpResults.size()]);
	}

	public static boolean isEmptyArray(Object object) {
		if (object != null) {
			if (object instanceof Object[] && ((Object[]) object).length == 0) {
				return true;
			}
			if (object instanceof Collection<?> && ((Collection<?>) object).size() == 0) {
				return true;
			}
		}
		return false;
	}
}
