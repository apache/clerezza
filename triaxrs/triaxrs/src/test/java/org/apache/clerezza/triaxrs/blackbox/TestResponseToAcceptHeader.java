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
package org.apache.clerezza.triaxrs.blackbox;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;

public class TestResponseToAcceptHeader {

    private static String[] accept = {"text/html;q=0.9", "text/plain;q=0.8", "image/png", "*/*;q=0.5"};
    private static Set<HeaderName> headerNames = new HashSet<HeaderName>();

    //private static byte[] entity = "a plain text body".getBytes();
    @Path("/foo")
    public static class MyResource {

        @GET
        @Produces("text/plain")
        public String handleGet() {
            return "Hello World";
        }
    }

    @Test
    public void testResponse() throws Exception {

    	JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
    	
        Request requestMock = EasyMock.createNiceMock(Request.class);
        ResponseImpl responseImpl = new ResponseImpl();
        expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
        headerNames.add(HeaderName.ACCEPT);
        expect(requestMock.getHeaderNames()).andReturn(headerNames);
        expect(requestMock.getHeaderValues(HeaderName.ACCEPT)).andReturn(accept);
        RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
        expect(requestURI.getPath()).andReturn("/foo").anyTimes();
        expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
        //responseMock.addHeader(HeaderName.CONTENT_TYPE, MediaType.valueOf("text/plain;q=.8"));

        replay(requestMock);
        replay(requestURI);
        handler.handle(requestMock, responseImpl);
        responseImpl.consumeBody();
        String[] contentType = responseImpl.getHeaders().get(HeaderName.CONTENT_TYPE);
		Assert.assertTrue(contentType.length == 1);
        Assert.assertEquals("text/plain", contentType[0]);
    }
}

