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
package org.apache.clerezza.triaxrs.util;

import java.util.Map;

/**
 * This class holds information to decide which root resource and 
 * resource method to call. Furthermore it keeps the value of path
 * parameters, which later can be injected into resource method and/or
 * fields.
 * 
 * @author reto
 *
 */
public class PathMatching {

	Map<String, String> parameters;
	String remainingURIPath;
	public PathMatching(Map<String, String> parameters, String remainingURIPath) {
		super();
		this.parameters = parameters;
		this.remainingURIPath = remainingURIPath;
	}
	public Map<String, String> getParameters() {
		return parameters;
	}
	public String getRemainingURIPath() {
		return remainingURIPath;
	}
	
	public boolean isSlashOrEmpty() {
		return remainingURIPath.length() == 0 || remainingURIPath.equals("/");
	}
}