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

/**
 * This exception is thrown when information needed for authentication is
 * missing or invalid.
 *
 * @author mir
 */
public class LoginException extends Exception {

	public static final String USER_NOT_EXISTING = "user not existing";
	public static final String PASSWORD_NOT_MATCHING = "password did not match";
	private String type;

	public LoginException(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
