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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.clerezza.triaxrs.WebRequest;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.util.EnhancedRequest;

/**
 *
 * @author szalay
 */
public class UriInfoImpl implements UriInfo {

	private WebRequest request;

	/**
	 *  constructor
	 * 
	 * @param request
	 */
	public UriInfoImpl(WebRequest request) {
		this.request = request;
	}

	@Override
	public URI getAbsolutePath() {
		try {
			return new URI(new EnhancedRequest(this.request.getWrhapiRequest()).getRequestURLWithoutParams().toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public UriBuilder getAbsolutePathBuilder() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public URI getBaseUri() {
		try {
			return new URL(getAbsolutePath().toURL(), "/").toURI();
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public UriBuilder getBaseUriBuilder() {
		return UriBuilder.fromUri(getBaseUri());
	}

	@Override
	public String getPath() {
		try {
			return request.getWrhapiRequest().getRequestURI().getPath();
		} catch (HandlerException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getPath(boolean arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters(boolean arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<PathSegment> getPathSegments() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<PathSegment> getPathSegments(boolean arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters() {
		return getQueryParameters(true);
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
		try {
			String query = request.getWrhapiRequest().getRequestURI().getQuery();
			return QueryStringParser.extractPathParameters(query, "&", decode);
		} catch (HandlerException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public URI getRequestUri() {
		try {
			return new URI(new EnhancedRequest(this.request.getWrhapiRequest()).getFullRequestURL().toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public UriBuilder getRequestUriBuilder() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<String> getMatchedURIs() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<String> getMatchedURIs(boolean arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<Object> getMatchedResources() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
