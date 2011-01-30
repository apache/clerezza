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
package org.apache.clerezza.triaxrs.testResource;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import javax.ws.rs.core.PathSegment;
import org.junit.Ignore;

/**
 *
 * @author szalay
 * @version $Id: $
 */
@Ignore
@Path("/test-resource")
public class TestResource {

    private String matrix;
    private Object queryParam;
    private PathSegment segment;
    private String input;
    private String username;
    private String password;
    
    @GET
    @Produces("text/html")
    public String getWelcomeText() {
        return "Welcome";
    }

    @GET
    @Produces("text/html")
    @Path("showForm")
    public String addUserForm() {
        return "form";
    }
    
    @POST
    @Path("postToMe")
    public void post(String input){
        this.input = input;
    }

    @POST
    @Path("setForm")
    public void setForm(@FormParam(value="username") String username, 
                        @FormParam(value="password") String password){
        
        this.username = username;
        this.password = password;
    }
    
    @Path("subresource")
    public TestSubResource findSubResource(@QueryParam("id") String id){
        
        TestSubResource r = new TestSubResource();
        r.setContent(id);
        return r;
    }
    
    @POST
    @Path("add")
    public void setMatrix(@MatrixParam(value = "scale") String matrixParam, @QueryParam(value = "test") String testValue) {
        this.matrix = matrixParam;
        this.queryParam = testValue;
    }
	@POST
    @Path("path/{value}")
    public void postingSegment(@PathParam(value = "value") PathSegment segment) {
        this.segment = segment;
    }
    
    public PathSegment getSegment(){
        return this.segment;
    }

    public Object getMatrix() {
        return matrix;
    }

    public Object getQueryParam() {
        return queryParam;
    }

    public String getInput() {
        return input;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}// $Log: $

