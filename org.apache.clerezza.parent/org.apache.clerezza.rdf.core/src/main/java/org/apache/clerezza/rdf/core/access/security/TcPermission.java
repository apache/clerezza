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
package org.apache.clerezza.rdf.core.access.security;

import java.security.Permission;

/**
 * A permission to access <code>TripleCollection<code>s matching a specified 
 * name pattern. A pattern is matched if and only if the pattern is equals
 * to name of the <code>TripleCollection<code> or the pattern ends with "/*" and
 * the name of the <code>TripleCollection<code> starts with the characters
 * preceding the '*' in the pattern.
 *
 * @author reto, tsuy
 */
public class TcPermission extends Permission {
	private String tcNamePattern;
	private String actions;

	public TcPermission(String tcNamePattern, String actions)  {
		super(tcNamePattern);
		this.tcNamePattern = tcNamePattern;
		this.actions = actions;
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof TcPermission) {
			TcPermission other = (TcPermission) permission;
			if (!patternImplies(other.tcNamePattern)) {
				return false;
			}
			if (!actionsImplies(other.actions)) {
				return false;
			}
			return true;
			
			
		}
		return false;
	}

	private boolean actionsImplies(String actionsOther) {
		if (actionsOther.equals("read")) {
			return true;
		} else {
			return actions.contains("write");
		}
		
	}

	private boolean patternImplies(String tcNamePatternOther) {
		if (tcNamePattern.equals(tcNamePatternOther)) {
			return true;
		}
		if (tcNamePattern.endsWith("/*")) {
			return tcNamePatternOther.startsWith(
					tcNamePattern.substring(0, tcNamePattern.length()-1));
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TcPermission other = (TcPermission) obj;
		if (this.tcNamePattern != other.tcNamePattern 
				&& (this.tcNamePattern == null
				|| !this.tcNamePattern.equals(other.tcNamePattern))) {
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
		int hash = 3;
		hash = 97 * hash + (this.tcNamePattern != null ?
			this.tcNamePattern.hashCode() : 0);
		return hash;
	}

	

	@Override
	public String getActions() {
		return this.actions;
	}

}
