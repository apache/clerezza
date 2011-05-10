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
package org.apache.clerezza.platform.security.auth.basic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.AccessControlException;
import java.util.Collections;

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
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.util.MessageBody2Read;

import javax.security.auth.Subject;

/**
 *
 * @author mir
 *
 */
@Component
@Service(WeightedAuthenticationMethod.class)
@Property(name = "weight", intValue = 10)
public class BasicAuthentication implements WeightedAuthenticationMethod {

	/**
	 *	weight of the authentication method
	 */
	private int weight = 10;
	
	@Reference
	AuthenticationService authenticationService;

	public void activate(ComponentContext componentContext) {
		weight = (Integer) componentContext.getProperties().get("weight");
	}

	@Override
	public boolean authenticate(Request request, Subject subject) throws LoginException, HandlerException {
		String[] authorizationValues = request.getHeaderValues(HeaderName.AUTHORIZATION);
		if (authorizationValues != null && authorizationValues.length > 0) {
			String authorization = authorizationValues[0];
			String authBase64 = authorization.substring(authorization.indexOf(' ') + 1);
			String[] credentials = new String(Base64.decode(authBase64)).split(":");
			String userName = credentials[0];
			String password;
			if (credentials.length > 1) {
				password = credentials[1];
			} else {
				password = "";
			}
			try {
				if (authenticationService.authenticateUser(userName, password)) {
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

	@Override
	public boolean writeLoginResponse(Request request, Response response, Throwable cause) throws HandlerException {
		if (cause == null || cause instanceof AccessControlException) {
			setUnauthorizedResponse(response,
					"<html><body>unauthorized</body></html>");
			return true;
		}
		if (cause instanceof LoginException) {
			LoginException loginException = (LoginException) cause;
			String type = loginException.getType();
			if (type.equals(LoginException.PASSWORD_NOT_MATCHING)) {
				setUnauthorizedResponse(response,
						"<html><body>Username and password do not match</body></html>");
				return true;
			}
			if (type.equals(LoginException.USER_NOT_EXISTING)) {
				setUnauthorizedResponse(response,
						"<html><body>User does not exist</body></html>");
				return true;
			}
		}
		return false;
	}

	private void setUnauthorizedResponse(final Response response, String message)
			throws HandlerException {
		response.setResponseStatus(ResponseStatus.UNAUTHORIZED);
		response.addHeader(HeaderName.WWW_AUTHENTICATE,
				"Basic realm=\"Clerezza Platform authentication needed\"");
		final java.io.InputStream pipedIn = new ByteArrayInputStream(message.getBytes());
		response.setHeader(HeaderName.CONTENT_LENGTH, message.getBytes().length);
		response.setBody(new MessageBody2Read() {

			@Override
			public ReadableByteChannel read() throws IOException {
				return Channels.newChannel(pipedIn);
			}
		});
	}

	@Override
	public int getWeight() {
		return weight;
	}
}
