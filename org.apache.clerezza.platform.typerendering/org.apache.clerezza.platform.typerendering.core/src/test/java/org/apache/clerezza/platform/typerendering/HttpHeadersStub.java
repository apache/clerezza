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
package org.apache.clerezza.platform.typerendering;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author mir
 */
public class HttpHeadersStub implements HttpHeaders{
	private List<MediaType> acceptableMediaTypes;

	public HttpHeadersStub(MediaType... acceptableMediaTypes) {
		this.acceptableMediaTypes = Arrays.asList(acceptableMediaTypes);
	}

	@Override
	public List<String> getRequestHeader(String name) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MultivaluedMap<String, String> getRequestHeaders() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<MediaType> getAcceptableMediaTypes() {
		return acceptableMediaTypes;
	}

	@Override
	public List<Locale> getAcceptableLanguages() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MediaType getMediaType() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Locale getLanguage() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Map<String, Cookie> getCookies() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
