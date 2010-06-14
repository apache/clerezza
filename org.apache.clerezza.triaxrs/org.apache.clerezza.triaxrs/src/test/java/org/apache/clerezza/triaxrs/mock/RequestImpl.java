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

import java.net.InetAddress;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.URIScheme;

/**
 *
 * @author szalay
 * @version $Id: $
 */
public class RequestImpl implements Request {

    private Object body;
    private Map<HeaderName, String[]> headers = new HashMap<HeaderName, String[]>();
    private MessageBody messageBody;
    private Method method;
    private int port;
    private InetAddress remoteHost;
    private RequestURI requestURI;
    private URIScheme scheme;

    @Override
    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    @Override
    public Set<HeaderName> getHeaderNames() {
        return headers.keySet();
    }

    /*public void setHeaderNames(Set<HeaderName> headerNames) {
        this.headerNames = headerNames;
    }*/

    @Override
    public String[] getHeaderValues(HeaderName headerName) {
        return headers.get(headerName);
    }

	public void setHeader(HeaderName headerName, String[] values) {
		headers.put(headerName, values);
	}

    /*public void setHeaderValues(String[] headerValues) {
        this.headerValues = headerValues;
    }*/

    @Override
    public MessageBody getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(MessageBody messageBody) {
        this.messageBody = messageBody;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public InetAddress getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(InetAddress remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public RequestURI getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(RequestURI requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public URIScheme getScheme() {
        return scheme;
    }

    public void setScheme(URIScheme scheme) {
        this.scheme = scheme;
    }

	public X509Certificate[] getCertificates() {
		return null;
	}
    
  
}

