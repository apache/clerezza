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

import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;

/**
 * This class wraps a <code>Response</code>. It stores the message body and 
 * provides a method for retrieving it.
 * 
 * @author mir
 */
public class BodyStoringResponse implements Response {

	private MessageBody body;
	private Response wrappedResponse;

	public BodyStoringResponse(Response response) {
		this.wrappedResponse = response;
	}

	@Override
	public void addHeader(HeaderName headerName, Object value) throws HandlerException {
		wrappedResponse.addHeader(headerName, value);
	}

	@Override
	public void setBody(MessageBody body) throws HandlerException {
		this.body = body;
	}

	@Override
	public void setHeader(HeaderName headerName, Object value) throws HandlerException {
		wrappedResponse.setHeader(headerName, value);
	}

	@Override
	public void setResponseStatus(ResponseStatus status) throws HandlerException {
		wrappedResponse.setResponseStatus(status);
	}

	/**
	 * Returns the message body of this response
	 * @return
	 */
	public MessageBody getBody() {
		return body;
	}
}

