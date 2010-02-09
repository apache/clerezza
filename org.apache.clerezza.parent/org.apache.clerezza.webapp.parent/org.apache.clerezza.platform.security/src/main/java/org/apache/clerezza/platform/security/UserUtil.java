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
package org.apache.clerezza.platform.security;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import javax.security.auth.Subject;

/**
 * Utility methods for retrieving user information.
 *
 *
 * @author mir, tio
 */
public class UserUtil {

	/**
	 *
	 * @return the name of user which is associated to the current thread
	 */
	public static String getCurrentUserName() {
		Subject subject;
		final AccessControlContext context = AccessController.getContext();
		try {
			subject = AccessController.doPrivileged(new PrivilegedExceptionAction<Subject>() {

				@Override
				public Subject run() throws Exception {
					return Subject.getSubject(context);
				}
			});
		} catch (PrivilegedActionException ex) {
			Exception cause = (Exception)ex.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw new RuntimeException(cause);
		}
		Iterator<Principal> iter = subject.getPrincipals().iterator();
		String name = null;
		if (iter.hasNext()) {
				name = iter.next().getName();
		}
		return name;
	}
}
