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
package org.apache.clerezza.triaxrs.util;

import org.apache.clerezza.triaxrs.util.uri.UriEncoder;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

public final class PathSegmentImpl implements PathSegment {

	private String path;
	private MultivaluedMap<String, String> matrixParameters;

	PathSegmentImpl(String path,
			MultivaluedMap<String, String> matrixParameters) {
		super();
		this.path = path;
		this.matrixParameters = matrixParameters;
	}

	public String getPath() {
		return path;
	}

	public MultivaluedMap<String, String> getMatrixParameters() {
		return matrixParameters;
	}

	@Override
	public String toString() {
		return "[Path: " + path + "," + matrixParameters + "]";
	}

	public static PathSegment parse(String string, boolean disableDecoding) {
		MultivaluedMap<String, String> matrixMap = null;

		int colonPos = string.indexOf(';');
		String path;
		if (colonPos != -1) {
			String matrixParameters = string.substring(colonPos + 1);
			path = (colonPos == 0) ? "" : string.substring(0, colonPos);
			matrixMap = QueryStringParser.extractPathParameters(matrixParameters, ";", !disableDecoding);
		} else {
			path = string;
		}

		if (!disableDecoding) {
			path = UriEncoder.decodeString(path);
		}
		return new PathSegmentImpl(path, matrixMap);

	}
}
