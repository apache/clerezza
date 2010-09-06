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

import java.util.ArrayList;
import java.util.List;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.util.ResponseWrapper;

/**
 *
 * @author rbn
 */
class WrappedResponse extends ResponseWrapper implements ResponseStatusInfo {
	private String XHTML_TYPE = "application/xhtml+xml";
	private String HTML_TYPE = "text/html";
	private boolean convertXhtml2Html = false;
	private List<Object> contentLengths = new ArrayList<Object>();

	public WrappedResponse(Response response) {
		super(response);
	}

	@Override
	public void addHeader(HeaderName headerName, Object value) throws HandlerException {
		if (headerName.equals(HeaderName.CONTENT_LENGTH)) {
			if (convertXhtml2Html) {
				// do nothing
			} else {
				contentLengths.add(value);
			}
			return;
		}
		String stringValue = value.toString();
		if (headerName.equals(HeaderName.CONTENT_TYPE) && value.toString().startsWith(XHTML_TYPE)) {
			super.addHeader(headerName, HTML_TYPE+stringValue.substring(XHTML_TYPE.length()));
			convertXhtml2Html = true;
		} else {
			super.addHeader(headerName, value);
		}
	}

	@Override
	public void setHeader(HeaderName headerName, Object value) throws HandlerException {
		if (headerName.equals(HeaderName.CONTENT_LENGTH)) {
			if (convertXhtml2Html) {
				// do nothing
			} else {
				contentLengths.clear();
				contentLengths.add(value);
			}
			return;
		}
		String stringValue = value.toString();
		if (headerName.equals(HeaderName.CONTENT_TYPE) && stringValue.startsWith(XHTML_TYPE)) {
			super.setHeader(headerName, HTML_TYPE+stringValue.substring(XHTML_TYPE.length()));
			convertXhtml2Html = true;
		} else {
			super.setHeader(headerName, value);
		}
	}

	@Override
	public void setBody(MessageBody body) throws HandlerException {
		super.setBody(new Xhtml2HtmlConvertingBody(body, this));
	}

	@Override
	public boolean convertXhtml2Html() {
		return convertXhtml2Html;
	}

	void setContentLengthIfNoConversion() throws HandlerException {
		if (contentLengths.size() > 0 && !convertXhtml2Html) {
			super.setHeader(HeaderName.CONTENT_LENGTH, contentLengths.toArray());
		}
	}
}
