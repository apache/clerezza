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
package org.apache.clerezza.rdf.utils;


/**
 * A utility class for UriRef and String manipulations.
 *
 * @author tio
 */
public class UriRefUtil {

	/**
	 * Strips #x00 - #x1F and #x7F-#x9F from a Unicode string
	 * @see <a href="http://www.w3.org/TR/rdf-concepts/#dfn-URI-reference">
	 * http://www.w3.org/TR/rdf-concepts/#dfn-URI-reference</a> and
	 * replaces all US-ASCII space character with a "+".
	 *
	 * @param inputChars
	 * @return the stripped string
	 * 
	 */
	public static String stripNonUriRefChars(CharSequence inputChars) {

		if (inputChars == null) {
			return "";
		}

		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < inputChars.length(); i++) {
			char c = inputChars.charAt(i);

			if (!isIllegal(c)) {
				buffer.append(c);
			}
		}
		return buffer.toString().replaceAll("\\s+", "+");
	}

	private static boolean isIllegal(char ch) {
		if ((ch >= 0x7F) && (ch <= 0x9F)) {
			return true;
		}
		return false;
	}
}
