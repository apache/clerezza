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
package org.apache.clerezza.triaxrs.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Hashtable;

import java.util.Map;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.util.ResponseBase;

/**
 *
 * @author szalay
 */
public class ResponseImpl extends ResponseBase {

    private Hashtable<HeaderName, Object> headers = new Hashtable<HeaderName, Object>();
    private MessageBody body;
    private ResponseStatus status;
    private byte[] bodyBytes;

    @Override
    public void setBody(MessageBody arg0) throws HandlerException {
        this.body = arg0;
    }

    @Override
    public void setResponseStatus(ResponseStatus arg0) throws HandlerException {
        this.status = arg0;
    }

    public Map<HeaderName, String[]> getHeaders() {
        return getHeaderMap();
    }

    public MessageBody getBody() {
        return body;
    }

    public ResponseStatus getStatus() {
        return status;
    }
    
    public byte[] getBodyBytes() {
		return bodyBytes;
	}

	public void consumeBody() {
    	if (body != null) {
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		try {
				body.writeTo(Channels.newChannel(baos));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
    		bodyBytes = baos.toByteArray();
    	}
    }

    
}

