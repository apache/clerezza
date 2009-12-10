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
package org.apache.clerezza.platform.xhtml2html;

import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.util.ResponseWrapper;

/**
 *
 * @author rbn
 */
class WrappedResponse extends ResponseWrapper {
	private String XHTML_TYPE = "application/xhtml+xml";
	private String HTML_TYPE = "text/html";

	public WrappedResponse(Response response) {
		super(response);
	}

	@Override
	public void addHeader(HeaderName headerName, Object value) throws HandlerException {
		if (headerName.equals(HeaderName.CONTENT_TYPE) && XHTML_TYPE.equals(value)) {
			super.addHeader(headerName, HTML_TYPE);
		} else {
			super.addHeader(headerName, value);
		}
	}

	@Override
	public void setHeader(HeaderName headerName, Object value) throws HandlerException {
		if (headerName.equals(HeaderName.CONTENT_TYPE) && XHTML_TYPE.equals(value)) {
			super.setHeader(headerName, HTML_TYPE);
		} else {
			super.setHeader(headerName, value);
		}
	}
}
