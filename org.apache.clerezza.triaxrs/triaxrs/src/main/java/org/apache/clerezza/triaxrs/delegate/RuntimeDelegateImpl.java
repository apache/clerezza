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

import java.net.URI;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import org.apache.clerezza.triaxrs.headerDelegate.CacheControlHeaderDelegate;
import org.apache.clerezza.triaxrs.headerDelegate.CookieHeaderDelegate;
import org.apache.clerezza.triaxrs.headerDelegate.DateHeaderDelegate;
import org.apache.clerezza.triaxrs.headerDelegate.EntityTagHeaderDelegate;
import org.apache.clerezza.triaxrs.headerDelegate.LocaleProvider;
import org.apache.clerezza.triaxrs.headerDelegate.MediaTypeHeaderDelegate;
import org.apache.clerezza.triaxrs.headerDelegate.NewCookieHeaderDelegate;
import org.apache.clerezza.triaxrs.headerDelegate.StringProvider;
import org.apache.clerezza.triaxrs.headerDelegate.URIProvider;

/**
 * 
 * @author reto
 */
public class RuntimeDelegateImpl extends RuntimeDelegate {

	/**
	 * a hashtable of handler delegates
	 */
	Hashtable<Class, HeaderDelegate> headerDelegates = new Hashtable<Class, HeaderDelegate>();

	{
		// initiaze header delegates
		this.headerDelegates
				.put(CacheControl.class, new CacheControlHeaderDelegate());
		this.headerDelegates.put(Cookie.class, new CookieHeaderDelegate());
		this.headerDelegates.put(Date.class, new DateHeaderDelegate());
		this.headerDelegates.put(EntityTag.class, new EntityTagHeaderDelegate());
		this.headerDelegates.put(MediaType.class, new MediaTypeHeaderDelegate());
		this.headerDelegates.put(NewCookie.class, new NewCookieHeaderDelegate());
		this.headerDelegates.put(String.class, new StringProvider());
		this.headerDelegates.put(URI.class, new URIProvider());
		this.headerDelegates.put(Locale.class, new LocaleProvider());
	}

	@Override
	public <T> T createEndpoint(Application arg0, Class<T> arg1)
			throws IllegalArgumentException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> arg0) {

		try {
			HeaderDelegate<T> d = this.headerDelegates.get(arg0);
			return d;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public ResponseBuilder createResponseBuilder() {
		return new ResponseBuilderImpl() {
		};
	}

	@Override
	public UriBuilder createUriBuilder() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public VariantListBuilder createVariantListBuilder() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
