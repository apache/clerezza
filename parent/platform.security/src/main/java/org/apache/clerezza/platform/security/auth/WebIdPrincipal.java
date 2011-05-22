/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.clerezza.platform.security.auth;

import org.apache.clerezza.rdf.core.UriRef;

import java.security.Principal;

/**
 * A Principal for WebIDs
 * (as there can be some for Social Security numbers,...)
 *
 * @author bblfish
 * @created: 21/05/2011
 */
public class WebIdPrincipal implements Principal {
	protected UriRef webid;

	public WebIdPrincipal(UriRef webid) {
		this.webid = webid;
	}

	public UriRef getWebId() { return webid; }

    @Override
	public boolean equals(Object obj) {
		if (!(obj instanceof WebIdPrincipal)) {
			return false;
		}
		return webid.equals(((WebIdPrincipal)obj).webid);
	}

	@Override
	public String toString() {
		return "WebId Principal: '"+getName()+"'";
	}

	@Override
	public int hashCode() {
		return webid.hashCode();
	}

	@Override
	public String getName() {
		return webid.getUnicodeString();
	}
}
