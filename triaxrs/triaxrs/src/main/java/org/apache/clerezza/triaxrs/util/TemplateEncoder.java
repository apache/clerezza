package org.apache.clerezza.triaxrs.util;
/*
 *
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
 *
*/


import java.io.StringWriter;
import java.net.URLDecoder;
import org.apache.clerezza.utils.UriException;
import org.apache.clerezza.utils.UriUtil;

public class TemplateEncoder {
	
	/**
	 * You can't call the constructor.
	 */
	private TemplateEncoder() {
	}

	/**
	 * Translates a string into <code>application/x-www-form-urlencoded</code>
	 * format using a specific encoding scheme. This method uses the supplied
	 * encoding scheme to obtain the bytes for unsafe characters.
	 * <p>
	 * <em><strong>Note:</strong> The <a href=
	 * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
	 * World Wide Web Consortium Recommendation</a> states that
	 * UTF-8 should be used. Not doing so may introduce
	 * incompatibilites.</em>
	 * 
	 * @param pathTemplate
	 *            <code>String</code> to be translated.
	 * @param enc
	 *            The name of a supported <a
	 *            href="../lang/package-summary.html#charenc">character
	 *            encoding</a>.
	 * @return the translated <code>String</code>.
	 * @exception UnsupportedEncodingException
	 *                If the named encoding is not supported
	 * @see URLDecoder#decode(java.lang.String, java.lang.String)
	 * @since 1.4
	 */
	public static String encode(String pathTemplate, String enc)
			throws UriException {
		StringWriter sw = new StringWriter();
		int index = 0;
		while(true) {
			int openCurlyIndex = pathTemplate.indexOf('{', index);
			if (openCurlyIndex == -1) {
				break;
			}
			int closeCurlyIndex = pathTemplate.indexOf('}', openCurlyIndex);
			if (closeCurlyIndex == -1) {
				break;
			}
			sw.append(UriUtil.encodePartlyEncodedPath(pathTemplate.substring(index, openCurlyIndex), enc));
			sw.append(pathTemplate.substring(openCurlyIndex, closeCurlyIndex + 1));
			index = closeCurlyIndex + 1;
		}
		sw.append(UriUtil.encodePartlyEncodedPath(pathTemplate.substring(index, pathTemplate.length()),enc));
		return sw.toString();
	}
	
}
