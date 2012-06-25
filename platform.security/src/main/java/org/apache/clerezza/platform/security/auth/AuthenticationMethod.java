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
package org.apache.clerezza.platform.security.auth;

import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;

import javax.security.auth.Subject;

/**
 * Classes implementing this interface provide a method to authenticate a
 * a user with the information provided in a http request.
 *
 * @author mir
 */
public interface AuthenticationMethod {

	/**
	 * Returns the Subject of the authenticate user containing the principal
	 * of the authentication and possibly some credentials.  If the authentication failed, an
	 * <code>LoginException</code> will be thrown. If no authentication
	 * information are available null is returned.
	 * @param request containing the information to authenticate a subject
	 * @param subject to add authentication information to
	 * @return true if this method did authenticate, false otherwise
	 * @throws LoginException This exception is thrown in case
	 * the login procedure failed.
	 * @throws HandlerException
	 */
	public boolean authenticate(Request request, Subject subject)
		throws LoginException, HandlerException;

	/**
	 * Modifies the specified <code>Response</code> according the specified
	 * <code>Request</code> and <code>Throwable</code>
	 * (e.g. <code>LoginException</code> or <code>AccessControllException</code>.
	 * The response leads to or provides further instructions for a client to
	 * log in.
	 * @return true, iff the response was modified
	 */
	public boolean writeLoginResponse(Request request,Response response,
			Throwable cause) throws HandlerException;

}
