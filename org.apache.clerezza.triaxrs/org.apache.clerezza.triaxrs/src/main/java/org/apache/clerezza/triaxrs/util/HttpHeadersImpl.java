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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.triaxrs.WebRequest;
import org.apache.clerezza.triaxrs.headerDelegate.CookieHeaderDelegate;
import org.apache.clerezza.triaxrs.headerDelegate.LocaleProvider;
import org.apache.clerezza.triaxrs.headerDelegate.MediaTypeHeaderDelegate;

/**
*
* @author mir
*/
public class HttpHeadersImpl implements HttpHeaders {

	private WebRequest request;
	private Logger logger = LoggerFactory.getLogger(HttpHeadersImpl.class);
	
	public HttpHeadersImpl(WebRequest request) {
		this.request = request;
	}

	@Override
	public List<Locale> getAcceptableLanguages() {
		List<Locale> locales = new ArrayList<Locale>();
		List<String> languages = request.getHeaders().get(HttpHeaders.ACCEPT_LANGUAGE);
		LocaleProvider lp = new LocaleProvider();
		
		if(languages == null) {
			return null;
		}
		
		for (String language : languages) {
			try{
				locales.add(lp.fromString(language));
			} catch (IllegalArgumentException ex) {
				//log and continue
				logger.error("Exception {}", ex);
			}
		}
		
		return locales;
	}

	@Override
	public List<MediaType> getAcceptableMediaTypes() {
		return request.getAcceptHeader().getEntries();
	}

	@Override
	public Map<String, Cookie> getCookies() {
		Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();
		List<String> cookieStrings = request.getHeaders().get(HttpHeaders.COOKIE);
		CookieHeaderDelegate cp = new CookieHeaderDelegate();
		
		if(cookieStrings == null) {
			return null;
		}
		
		for (String cookieString : cookieStrings){
			Cookie cookie = cp.fromString(cookieString);
			cookies.put(cookie.getName(), cookie);		
		}
		return cookies;
	}

	@Override
	public Locale getLanguage() {
		List<String> clang = request.getHeaders().get(HttpHeaders.CONTENT_LANGUAGE);
		Locale locale = null;
		if (clang == null) {
			return null;
		}
		
		try{
			locale = new LocaleProvider().fromString(clang.get(0));
		} catch (IllegalArgumentException ex) {
			logger.error("Exception {}", ex);
			return null;
		}
		
		return locale;
	}

	@Override
	public MediaType getMediaType() {
		List<String> mediatype = request.getHeaders().get(HttpHeaders.CONTENT_TYPE);
		
		if (mediatype == null) {
			return null;
		}
		
		return new MediaTypeHeaderDelegate().fromString(mediatype.get(0));
	}

	@Override
	public List<String> getRequestHeader(String name) {
		return request.getHeaders().get(name);
	}

	@Override
	public MultivaluedMap<String, String> getRequestHeaders() {
		return request.getHeaders();
	}

}
