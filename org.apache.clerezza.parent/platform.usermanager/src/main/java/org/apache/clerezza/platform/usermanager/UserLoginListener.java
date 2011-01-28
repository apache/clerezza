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

package org.apache.clerezza.platform.usermanager;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.platform.security.auth.LoginListener;
import org.apache.clerezza.platform.usermanager.UserManager;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * Updates last login time stamp from the user who has successfully logged in.
 *
 *
 * @author tio
 */
@Component(enabled=true, immediate=true)
@Service(LoginListener.class)
public class UserLoginListener implements LoginListener{

	@Reference
	UserManager userManager;

	@Override
	public void userLoggedIn(String userName) {
		GraphNode userNode  = userManager.getUserInSystemGraph(userName);
		Lock l = userNode.writeLock();
		l.lock();
		try {
			userNode.deleteProperties(PLATFORM.lastLogin);
			userNode.addProperty(PLATFORM.lastLogin, LiteralFactory.getInstance().createTypedLiteral(new Date()));
		} finally {
			l.unlock();
		}
	}

}
