/*
 *  Copyright 2010 mir.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.platform.security.auth;

import org.wymiwyg.wrhapi.HandlerException;

/**
 * A service that checks if a provided username and password matches a
 * username and password in credentials store.
 *
 * @author mir
 */
public interface AuthenticationChecker {

	/**
	 * Checks if the provided username and password matches a username and
	 * password in credentials store.
	 * @param userName
	 * @param password
	 * @return true if the password matched, false otherwise
	 * @throws org.wymiwyg.wrhapi.HandlerException
	 * @throws org.apache.clerezza.platform.security.auth.NoSuchAgent
	 */
	boolean authenticate(String userName, String password) throws HandlerException, NoSuchAgent;

}
