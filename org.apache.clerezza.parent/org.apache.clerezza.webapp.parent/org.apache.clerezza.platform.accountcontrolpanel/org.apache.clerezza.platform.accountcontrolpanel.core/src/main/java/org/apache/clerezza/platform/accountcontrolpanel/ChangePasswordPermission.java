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
package org.apache.clerezza.platform.accountcontrolpanel;

import java.security.Permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Permission to change the password
 * @author ali
 *
 */
public class ChangePasswordPermission extends Permission {
	private final Logger logger = LoggerFactory.getLogger(UserBundlePermission.class);
	private String accountName;
	
	public ChangePasswordPermission(String accountName, String actions) {
		super(accountName);
		this.accountName = accountName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ChangePasswordPermission other = (ChangePasswordPermission) obj;
		return accountName.equals(other.accountName);
	}

	@Override
	public String getActions() {
		return "";
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + (this.accountName != null ?
			this.accountName.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean implies(Permission permission) {
		logger.debug("checking for {} in {}", permission, this);
		boolean result = equals(permission);
		logger.debug("result {}", result);
		return result;
	}

}
