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
package org.apache.clerezza.platform.defaultacceptheader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.util.RequestWrapper;

/**
 * Adds a specified accept-header to the wrapped request.
 *
 * @author mir
 */
class AcceptAddingRequest extends RequestWrapper {

	private String[] acceptHeader;

	/**
	 * Constructs a <code>AcceptAddingRequest</code> wrapping the specified
	 * request. It will appear the same as the wrapped request, except its
	 * accept header will be the specified acceptHeader.
	 *
	 * @param request
	 * @param acceptHeader
	 */
	public AcceptAddingRequest(Request request, String[] acceptHeader) {
		super(request);
		this.acceptHeader = acceptHeader;
	}

	@Override
	public String[] getHeaderValues(HeaderName headerName) throws HandlerException {
		if (headerName.equals(HeaderName.ACCEPT)) {
			return acceptHeader;
		} else {
			return super.getHeaderValues(headerName);
		}
	}

	@Override
	public Set<HeaderName> getHeaderNames() throws HandlerException {
		Set<HeaderName> headerNames = super.getHeaderNames();
		if (headerNames.contains(HeaderName.ACCEPT)) {
			return headerNames;
		}
		final Set<HeaderName> headerNamesWithAcceptHeader =
				new HashSet<HeaderName>(headerNames);
		headerNamesWithAcceptHeader.add(HeaderName.ACCEPT);		
		return Collections.unmodifiableSet(headerNamesWithAcceptHeader);
	}

}
