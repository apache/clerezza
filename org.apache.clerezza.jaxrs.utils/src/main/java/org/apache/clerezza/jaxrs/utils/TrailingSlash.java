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
package org.apache.clerezza.jaxrs.utils;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * The TrailingSlash utility checks if the URI given through
 * <code>UriInfo</code> ends or doesn't end with a slash ('/'). If the URI does not
 * conform, then a <code>WebApplicationException<code>
 * is thrown. This exception contains a response with status code 303 ("See-Other"),
 * causing a redirect to the correct URI.
 * 
 * @author mir
 * 
 */
public class TrailingSlash {

	/**
	 * Checks if an slash ('/') is present at the end of the URI given by
	 * <code>UriInfo</code>. If not then a redirect is triggered to the URI with
	 * the slash at the end.
	 * 
	 * @param uriInfo
	 */
	public static void enforcePresent(UriInfo uriInfo) {
		String absolutPath = uriInfo.getAbsolutePath().toString();
		if (!absolutPath.endsWith("/")) {
			String redirectUri = absolutPath + "/";
			Response response = createSeeOtherResponse(redirectUri);
			throw new WebApplicationException(response);
		}
	}

	/**
	 * Checks if no slash ('/') is present at the end of the URI given by
	 * <code>UriInfo</code>. If a slash is present, a redirect is triggered to
	 * the URI with no slash at the end.
	 * 
	 * @param uriInfo
	 */
	public static void enforceNotPresent(UriInfo uriInfo) {
		String absolutPath = uriInfo.getAbsolutePath().toString();
		if (absolutPath.endsWith("/")) {
			String redirectUri = absolutPath.substring(0,
					absolutPath.length() - 1);
			Response response = createSeeOtherResponse(redirectUri);
			throw new WebApplicationException(response);
		}
	}

	private static Response createSeeOtherResponse(String redirectUri) {
		URI seeOtherUri = null;
		try {
			seeOtherUri = new URI(redirectUri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		ResponseBuilder rb = Response.seeOther(seeOtherUri);
		return rb.build();
	}
}
