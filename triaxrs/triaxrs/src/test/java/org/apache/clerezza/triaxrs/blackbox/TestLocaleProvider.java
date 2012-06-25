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
package org.apache.clerezza.triaxrs.blackbox;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Locale;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.headerDelegate.LocaleProvider;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Response;

/**
 * 
 * @author mir
 */
public class TestLocaleProvider {
	static Locale mylocale;
	static Locale mylocale2;

	@Path("/")
	public static class MyResource {

		@GET
		public void handleGet(@Context HttpHeaders headers,
				@HeaderParam("accept-language") Locale locale) {
			if (locale != null) {
				mylocale2 = locale;
			}
			if (headers.getAcceptableLanguages().size() > 0) {
				mylocale = headers.getAcceptableLanguages().get(0);
			}
		}
	}

	@Test
	public void testHttpHeadersAndHeaderParam() throws Exception {

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);

		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/");
		String[] headervalues = new String[1];
		headervalues[0] = "en-uk-cockney";
		request.setHeader(HeaderName.ACCEPT_LANGUAGE, headervalues);
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		Response response = new ResponseImpl();
		handler.handle(request, response);

		assertTrue(mylocale != null);
		assertEquals("en_UK_cockney", mylocale.toString());
		assertTrue(mylocale2 != null);
		assertEquals("en_UK_cockney", mylocale2.toString());
	}

	@Test
	public void testIllegalArgumentHandling() throws Exception {
		mylocale = null;
		mylocale2 = null;

		JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);

		RequestImpl request = new RequestImpl();
		RequestURIImpl uri = new RequestURIImpl();
		uri.setPath("/");
		String[] headervalues = new String[1];
		// set a RFC 1766 language tag that can't be converted to a Locale
		headervalues[0] = "i-klingon";
		request.setHeader(HeaderName.ACCEPT_LANGUAGE, headervalues);
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		Response response = new ResponseImpl();
		handler.handle(request, response);

		assertEquals(mylocale, null);
		assertEquals(mylocale2, null);
	}

	@Test
	public void testLocaleProviderFromString() {
		LocaleProvider lp = new LocaleProvider();
		boolean illegaleArugmentOccured = false;

		assertEquals("en_UK", lp.fromString("en-uk;q = .7").toString());
		assertEquals("us__slang", lp.fromString("us-slang").toString());
		assertEquals("en_UK_cockney", lp.fromString("en-uk-cockney").toString());

		// only 2 letter language tags are allowed
		try {
			lp.fromString("abc-oaeu");
		} catch (IllegalArgumentException e) {
			illegaleArugmentOccured = true;
		}
		assertTrue(illegaleArugmentOccured);

		illegaleArugmentOccured = false;
		// the first sub-tag must not be longer than 8 chars
		try {
			lp.fromString("ab-abcdefghij");
		} catch (IllegalArgumentException e) {
			illegaleArugmentOccured = true;
		}
		assertTrue(illegaleArugmentOccured);

		illegaleArugmentOccured = false;
		// the first sub-tag must not be shorter than 2 chars
		try {
			lp.fromString("ab-a");
		} catch (IllegalArgumentException e) {
			illegaleArugmentOccured = true;
		}
		assertTrue(illegaleArugmentOccured);
	}

	@Test
	public void testLocaleProviderToString() {
		LocaleProvider lp = new LocaleProvider();
		Locale locale1 = new Locale("en", "uk", "cockney");
		Locale locale2 = new Locale("en", "", "cockney");

		// toLowerCase() because RFC 1766 says:
		// All tags are to be treated as case insensitive
		assertEquals("en-uk-cockney", lp.toString(locale1).toLowerCase());
		assertEquals("en-cockney", lp.toString(locale2).toLowerCase());
	}

}
