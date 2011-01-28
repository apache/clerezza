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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.util.parameterparser.KeyValuePair;
import org.wymiwyg.wrhapi.util.parameterparser.ParameterValue;
import org.wymiwyg.wrhapi.util.parameterparser.URLEncodedParameterCollection;

/**
 *  utility for query string parsing
 * 
 * @author szalay
 * @version $Id: $
 */
public class QueryStringParser {

	public static MultivaluedMap<String, String> getMatrix(String query, boolean encode) {
		List<PathSegment> segments = getPathSegments(query, encode);

		if ((segments == null) || (segments.size() == 0)) {
			return null;
		}

		PathSegment lastPathSegment = (PathSegment) segments.toArray()[segments.size() - 1];
		return lastPathSegment.getMatrixParameters();
	}

	public static List<String> getParameterValues(String query, boolean encode, String value) {

		if (query == null) {
			return null;
		}
		MultivaluedMap<String, String> allParameters = extractPathParameters(query, "&", encode);
		List<String> values = allParameters.get(value);
		return values;
	}

	public static MultivaluedMap<String, String> getFormParameters(MessageBody body, boolean encode) {

		if (body == null) {
			return null;
		}

		MultivaluedMap<String, String> result = new MultivaluedMapImpl();

		URLEncodedParameterCollection coll = new URLEncodedParameterCollection(body);
		Iterator<KeyValuePair<ParameterValue>> i = coll.iterator();

		while (i.hasNext()) {
			KeyValuePair<ParameterValue> aVP = i.next();
			result.add(aVP.getKey(), aVP.getValue().toString());
		}

		return result;
	}

	/**
	 * Extract the path segments from the path
	 * TODO: This is not very efficient
	 */
	public static List<PathSegment> getPathSegments(String path, boolean decode) {
		List<PathSegment> pathSegments = new LinkedList<PathSegment>();

		if (path == null) {
			return pathSegments;        // TODO the extraction algorithm requires an absolute path
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}

		String[] subPaths = path.split("/");
		if (subPaths.length == 0) {
			PathSegment pathSegment = new PathSegmentImpl("", new MultivaluedMapImpl());
			pathSegments.add(pathSegment);
			return pathSegments;
		}

		for (String subPath : subPaths) {
			if (subPath.length() == 0) {
				continue;
			}
			MultivaluedMap<String, String> matrixMap = null;

			int colon = subPath.indexOf(';');
			if (colon != -1) {
				String matrixParameters = subPath.substring(colon + 1);
				subPath = (colon == 0) ? "" : subPath.substring(0, colon);
				matrixMap = extractPathParameters(matrixParameters, ";", decode);
			}

			if (decode) {
				subPath = UriEncoder.decodeString(subPath);
			}
			PathSegment pathSegment = new PathSegmentImpl(subPath, matrixMap);
			pathSegments.add(pathSegment);
		}

		return pathSegments;
	}

	/**
	 * TODO: This is not very efficient
	 */
	public static MultivaluedMap<String, String> extractPathParameters(
			String parameters, String deliminator, boolean decode) {

		MultivaluedMap<String, String> map = new MultivaluedMapImpl();

		if (parameters == null) {
			return map;
		}

		for (String s : parameters.split(deliminator)) {
			if (s.length() == 0) {
				continue;
			}
			String[] keyVal = s.split("=");
			String key = (decode)
					? UriEncoder.decodeQuery(keyVal[0])
					: keyVal[0];
			if (key.length() == 0) {
				continue;            // parameter may not have a value, if so default to "";
			}
			String val = (keyVal.length == 2) ? (decode)
					? UriEncoder.decodeQuery(keyVal[1])
					: keyVal[1] : "";
			map.add(key, val);
		}

		return map;
	}
}// $Log: $

