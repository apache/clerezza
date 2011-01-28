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
package org.apache.clerezza.triaxrs.delegate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;

import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.util.MultivaluedMapImpl;
import org.apache.clerezza.triaxrs.util.UriInfoImpl;

/**
 *
 * @author reto
 */
class ResponseBuilderImpl extends ResponseBuilder {
	private int status = -1;
	private MultivaluedMap<String, Object> headers = new MultivaluedMapImpl<String, Object>();
	private Object entity;

	public ResponseBuilderImpl() {
	}

	@Override
	public Response build() {
		return new ResponseImpl(status, entity, headers);
	}

	@Override
	public ResponseBuilder clone() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder status(int status) {
		this.status = status;
		return this;
	}

	@Override
	public ResponseBuilder entity(Object entity) {
		this.entity = entity;
		return this;
	}

	@Override
	public ResponseBuilder type(MediaType type) {
		if (type == null) {
			headers.remove(HttpHeaders.CONTENT_TYPE);
		}
		headers.putSingle(HttpHeaders.CONTENT_TYPE, type);
		return this;
	}

	@Override
	public ResponseBuilder type(String type) {
		if (type == null) {
			headers.remove(HttpHeaders.CONTENT_TYPE);
		}
		headers.putSingle(HttpHeaders.CONTENT_TYPE, type);
		return this;
	}

	@Override
	public ResponseBuilder variant(Variant variant) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder variants(List<Variant> variants) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder language(String language) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder language(Locale language) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder location(URI location) {
		if (location == null) {
			headers.remove(HttpHeaders.LOCATION);
		}
		try {
			UriInfo uriInfo = new UriInfoImpl(JaxRsHandler.localRequest.get());
			URL baseUrl = uriInfo.getBaseUri().toURL();
			URL absolutizedUrl = new URL(baseUrl, location.toString());
			headers.putSingle(HttpHeaders.LOCATION, absolutizedUrl.toString());
			return this;
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public ResponseBuilder contentLocation(URI location) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder tag(EntityTag tag) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder tag(String tag) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder lastModified(Date lastModified) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder cacheControl(CacheControl cacheControl) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder expires(Date expires) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ResponseBuilder header(String name, Object value) {
		headers.add(name, value);
		return this;
	}

	@Override
	public ResponseBuilder cookie(NewCookie... cookies) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
