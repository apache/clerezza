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
package org.apache.clerezza.triaxrs;

/**
 * Defines standard names for the Triaxrs environment and Manifest header
 * attribute keys.
 * 
 * <p>
 * The values associated with these keys are of type
 * <code>java.lang.String</code>, unless otherwise indicated.
 * </p>
 * 
 * @author mir
 */
public interface Constants {
	/**
	 * Manifest header (named &quot;Triaxrs-PathPrefix&quot;) of a bundle which
	 * offers <code>javax.ws.rs.core.Application</code> or individual
	 * resource(s) as service(s) for a TriaxRs bundle. The header value
	 * specifies the prefix which will be prepended to the paths of the
	 * resources provided by the <code>javax.ws.rs.core.Application</code> or of
	 * the individual resource(s).
	 */
	public static final String TRIAXRS_PATH_PREFIX = "Triaxrs-PathPrefix";
}
