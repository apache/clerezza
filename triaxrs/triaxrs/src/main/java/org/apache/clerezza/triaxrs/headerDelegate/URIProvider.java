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
package org.apache.clerezza.triaxrs.headerDelegate;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 *  simple uri provider
 * 
 * @author szalay
 */
public class URIProvider implements HeaderDelegate<URI> {
   
    @Override
    public String toString(URI header) {
        return header.toASCIIString();
    }

    @Override
    public URI fromString(String header) {
        try {
            return new URI(header);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
}
