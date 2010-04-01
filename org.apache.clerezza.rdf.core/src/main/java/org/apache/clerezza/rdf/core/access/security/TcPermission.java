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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

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
	public final static String READWRITE = "readwrite";
	public final static String READ = "read";

	private String tcNamePattern;
	/**
	 * true if readwrite granted false if only read
	 */
	private boolean allowReadWrite = false;

	final static Pattern actionPattern = Pattern.compile(",( *)");
	/**
	 * Conststructs a TcPermission for a specified name pattern and a list of
	 * actions.
	 *
	 * @param tcNamePattern see class description
	 * @param actions a comma separated list of the strings "read" and "readwrite",
	 *        the canonical form is just "read" or "readwrite" as "readwrite"
	 *        implies "read".
	 */
	public TcPermission(String tcNamePattern, String actions)  {
		super(tcNamePattern);
		this.tcNamePattern = tcNamePattern;
		//check and set actions
		final Set actionSet = new HashSet(Arrays.asList(actionPattern.split(actions)));
		if (actionSet.remove(READWRITE)) {
			allowReadWrite = true;
		} else {
			if (!actionSet.contains(READ)) {
				throw new IllegalArgumentException("actions must be either \"read\" or \"readwrite\"");
			}
		}
		actionSet.remove(READ);
		if (actionSet.size() > 0) {
			throw new IllegalArgumentException("actions must only contain \"read\" and \"readwrite\"");
		}
	}

	@Override
	public boolean implies(Permission permission) {
		if (permission instanceof TcPermission) {
			TcPermission other = (TcPermission) permission;
			if (!patternImplies(other.tcNamePattern)) {
				return false;
			}
			if (!actionsImplies(other.allowReadWrite)) {
				return false;
			}
			return true;
			
			
		}
		return false;
	}

	private boolean actionsImplies(boolean readwriteOther) {
		if (!readwriteOther) {
			return true;
		} else {
			return allowReadWrite;
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
		if (this.allowReadWrite != other.allowReadWrite) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + (this.tcNamePattern != null ?
			this.tcNamePattern.hashCode() : 0);
		if (allowReadWrite) {
			hash++;
		}
		return hash;
	}

	

	@Override
	public String getActions() {
		return allowReadWrite ? READWRITE : READ;
	}

}
