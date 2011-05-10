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
package org.apache.clerezza.platform.security.auth.cookie;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.AccessControlException;
import java.security.Principal;
import java.util.*;
import javax.security.auth.Subject;
import javax.ws.rs.core.Cookie;

import org.apache.clerezza.platform.security.UserUtil;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.platform.security.auth.*;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.wymiwyg.commons.util.Base64;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;

/**
 *
 * @author mir
 *
 */
@Component
@Service(WeightedAuthenticationMethod.class)
@Property(name="weight", intValue=20)
public class CookieAuthentication implements WeightedAuthenticationMethod{

	/**
	 * Constant to identify the cause for the login. Causes are transmitted as
	 * query params.
	 */
	protected static final int NOT_ENOUGH_PERMISSIONS = 1;

	/**
	 *	weight of the authentication method
	 */
	private int weight = 20;

	@Reference
	AuthenticationService authenticationService;

	public void activate(ComponentContext componentContext) {
		weight = (Integer) componentContext.getProperties().get("weight");
	}

	@Override
	public boolean authenticate(Request request, Subject subject) throws LoginException, HandlerException {
		String[] cookieValues = request.getHeaderValues(HeaderName.COOKIE);
		if (cookieValues != null && cookieValues.length > 0) {
			Map<String, Cookie> cookies = parseCookies(cookieValues[0]);		
			Cookie authCookie = cookies.get(CookieLogin.AUTH_COOKIE_NAME);
			if (authCookie == null) {
				return false;
			}

			String authBase64 = authCookie.getValue();
			String[] credentials = new String(Base64.decode(authBase64)).split(":");
			String userName = credentials[0];
			String password;
			if (credentials.length > 1) {
				password = credentials[1];
			} else {
				password = "";
			}
			try {
				if (authenticationService.authenticateUser(userName, password)){
					subject.getPrincipals().remove(UserUtil.ANONYMOUS);
					subject.getPrincipals().add(new PrincipalImpl(userName));
					return true;
				} else {
					throw new LoginException(LoginException.PASSWORD_NOT_MATCHING);
				}
			} catch (NoSuchAgent ex) {
				throw new LoginException(LoginException.USER_NOT_EXISTING);
			}
		} else {
			return false;
		}
	}

	public void readHeadersFromRequest(Request request)
			throws HandlerException {

		Set<HeaderName> names = request.getHeaderNames();

		if (names == null) {
			return;
		}

		for (HeaderName headerName : names) {

			String[] headerValues = request.getHeaderValues(headerName);
			System.out.println(Arrays.toString(headerValues));

		}
		return;
	}


	@Override
	public boolean writeLoginResponse(Request request,Response response, Throwable cause) throws HandlerException{

		if (!request.getMethod().equals(Method.GET)) {
			return false;
		}
		response.setResponseStatus(ResponseStatus.getInstanceByCode(307)); // Temporary Redirect

		String location;
		try {
			location = "/login?referer=" + URLEncoder.encode(fixCurlyBrackets(
					request.getRequestURI().getAbsPath()), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		if (cause != null) {
			if (cause instanceof AccessControlException) {
				location += "&cause=" + NOT_ENOUGH_PERMISSIONS;
			}

			if (cause instanceof LoginException) {
				response.addHeader(HeaderName.SET_COOKIE, CookieLogout.getLogoutCookie());
				location = request.getRequestURI().getAbsPath();
			}
		} 
		response.addHeader(HeaderName.LOCATION, location);
		return true;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	private static Map<String, Cookie> parseCookies(String header) {
		String bites[] = header.split("[;,]");
		Map<String, Cookie> cookies = new LinkedHashMap<String, Cookie>();
		int version = 0;
		MutableCookie cookie = null;
		for (String bite : bites) {
			String crumbs[] = bite.split("=", 2);
			String name = crumbs.length > 0 ? crumbs[0].trim() : "";
			String value = crumbs.length > 1 ? crumbs[1].trim() : "";
			if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
				value = value.substring(1, value.length() - 1);
			}
			if (!name.startsWith("$")) {
				if (cookie != null) {
					cookies.put(cookie.name, cookie.getImmutableCookie());
				}
				cookie = new MutableCookie(name, value);
				cookie.version = version;
			} else if (name.startsWith("$Version")) {
				version = Integer.parseInt(value);
			} else if (name.startsWith("$Path") && cookie != null) {
				cookie.path = value;
			} else if (name.startsWith("$Domain") && cookie != null) {
				cookie.domain = value;
			}
		}
		if (cookie != null) {
			cookies.put(cookie.name, cookie.getImmutableCookie());
		}
		return cookies;
	}

	private static class MutableCookie {

		String name;
		String value;
		int version = Cookie.DEFAULT_VERSION;
		String path = null;
		String domain = null;

		public MutableCookie(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public Cookie getImmutableCookie() {
			return new Cookie(name, value, path, domain, version);
		}
	}

	/** Many browsers (Firefox and Konqueror) have been seen to send '{' and
	 * '}' in their URIs. This violates RFC 3986 which would cause problem in
	 * {@link URI} construction. This method fixes this bug.
	 *
	 * @param uri the URI string to fix
	 * @return string where all curly brackets are encoded
	 */
	private String fixCurlyBrackets(String uri) throws UnsupportedEncodingException {
		return uri.replace("{", URLEncoder.encode("{", "UTF-8"))
				.replace("}", URLEncoder.encode("}", "UTF-8"));
	}
}
