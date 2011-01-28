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
package org.apache.clerezza.rdf.core.access;

import java.security.AccessControlException;

/**
 * is thrown on an attempt to add or remove triples to a 
 * read-only mutable graph 
 *
 * @author tsuy
 */
public class ReadOnlyException extends AccessControlException {

	private String action;

	/**
	 * creates an exception indicating that the mutable graph is read-only
	 * 
	 * @param action
	 */
	public ReadOnlyException(String action) {
		super("read only mutable graph, not allowed to "+action);
		this.action = action;
	}

	/**
	 * 
	 * @return the name of the action
	 */
	public String getAction() {
		return action;
	}
}
