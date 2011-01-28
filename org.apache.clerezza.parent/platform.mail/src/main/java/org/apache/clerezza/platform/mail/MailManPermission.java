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
package org.apache.clerezza.platform.mail;

import java.security.Permission;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.clerezza.platform.security.UserUtil;

/**
 * A permission to send e-mails as a specific user.
 *
 * @author mir
 */
public class MailManPermission extends Permission {
	private String namePattern;
	private SortedSet<String> actions;
	
	public static final String SEND_FROM = "send from";
	public static final String SEND_MAIL = "send mail";
	
	public static final String SELF_ACTION = "<self>";

	public MailManPermission(String namePattern, String actions)  {
		super(namePattern);
		this.namePattern = namePattern;
		this.actions = new TreeSet<String>(Arrays.asList(actions.split(",")));
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof MailManPermission) {
			MailManPermission other = (MailManPermission) permission;

			if(other.actions.contains(SEND_FROM)) {
				if (!patternImplies(other.namePattern)) {
					return false;
				}
			}
			if (!actionsImplies(other.actions)) {
				return false;
			}
			return true;
			
			
		}
		return false;
	}

	private boolean actionsImplies(Set<String> actionsOther) {
		return actions.containsAll(actionsOther);
		
		
	}

	private boolean patternImplies(String namePatternOther) {
		if (namePattern.equals(namePatternOther)) {
			return true;
		}

		if (namePattern.equals(SELF_ACTION)) {
			String userName = UserUtil.getCurrentUserName();
			if (userName == null) {
				return false;
			}
			return userName.equals(namePatternOther);
		}
		return namePatternOther.matches(namePattern);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MailManPermission other = (MailManPermission) obj;
		if (this.namePattern != other.namePattern 
				&& (this.namePattern == null
				|| !this.namePattern.equals(other.namePattern))) {
			return false;
		}
		if (this.actions != other.actions 
				&& (this.actions == null
				|| !this.actions.equals(other.actions))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + (this.namePattern != null ?
			this.namePattern.hashCode() : 0);
		return hash;
	}

	

	@Override
	public String getActions() {
		StringBuffer sb = new StringBuffer();
		Iterator<String> it = actions.iterator();
		while(it.hasNext()) {
			String action = it.next();
			sb.append(action);
			if (it.hasNext()) {
				sb.append(",");	
			}					
		}
		return sb.toString();
	}
}
