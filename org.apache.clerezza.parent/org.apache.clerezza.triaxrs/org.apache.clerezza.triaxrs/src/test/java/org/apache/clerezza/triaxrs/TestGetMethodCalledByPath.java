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


import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testResource.TestResource;
import org.apache.clerezza.triaxrs.testResource.TestResourceWithTemplateInMethod;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.ResponseStatus;

/**
 *
 * @author szalay
 * @version $Id: $
 */
public class TestGetMethodCalledByPath {


    

    
    @Test
    public void testGetMethodPathNotFound() throws Exception {
        
    	JaxRsHandler handler = HandlerCreator.getHandler(TestResource.class,
				TestResourceWithTemplateInMethod.class);
        RequestImpl request = new RequestImpl();
        RequestURIImpl uri = new RequestURIImpl();
        uri.setPath("/test-resource/blabla");
        request.setRequestURI(uri);
        request.setMethod(Method.GET);
        
        ResponseImpl response = new ResponseImpl();
        

        handler.handle(request, response);
 
		assertEquals(ResponseStatus.NOT_FOUND, response.getStatus());
        
        
    }
    
}// $Log: $

