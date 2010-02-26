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
package org.apache.clerezza.platform.security.auth.cookie.onotology;

import org.apache.clerezza.rdf.core.UriRef;

public class LOGIN {
	//Classes

	/**
	 * A page which provides an interface to log in.
	 */
	public static final UriRef LoginPage = new UriRef("http://clerezza.org/2009/07/login#LoginPage");

	// Properties

	/**
	 * Points to a message for the client
	 */
	public static final UriRef message = new UriRef("http://clerezza.org/2009/07/login#message");

	/**
	 * Points to the URI of the page that caused the login process.
	 */
	public static final UriRef refererUri = new UriRef("http://clerezza.org/2009/07/login#refererUri");
}
