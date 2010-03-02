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

import static org.junit.Assert.assertEquals;


import org.junit.Before;
import org.junit.Test;
import org.apache.clerezza.triaxrs.delegate.RuntimeDelegateImpl;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testResource.TestResource;
import org.apache.clerezza.triaxrs.testResource.TestResourceWithTemplateInMethod;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.ResponseStatus;

/**
 *
 * @author szalay
 * @version $Id: $
 */
public class TestTemplate {

	@Before
	public void setUp() {
		RuntimeDelegateImpl.setInstance(new RuntimeDelegateImpl());
	}
	
    @Test
    public void testWithParameterNotFound() throws Exception {
        
    	JaxRsHandler handler = HandlerCreator.getHandler(TestResource.class,
				TestResourceWithTemplateInMethod.class);
        RequestImpl request = new RequestImpl();
        RequestURIImpl uri = new RequestURIImpl();
        uri.setPath("/test-resource/postToMe");
        request.setRequestURI(uri);
        request.setMethod(Method.DELETE);
        ResponseImpl response = new ResponseImpl();
        
         boolean expThrown = false;
        
        try{
            handler.handle(request, response);
        } catch(HandlerException he){
            assertEquals(ResponseStatus.METHOD_NOT_ALLOWED, he.getStatus());
            expThrown = true;
        } 
		assertEquals(ResponseStatus.METHOD_NOT_ALLOWED, response.getStatus());
      
    }

       
	
}

// $Log: $

