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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import org.apache.clerezza.triaxrs.util.QueryStringParser;


/**
 *
 * @author szalay
 * @version $Id: $
 */
public class TestQueryParserUtil {

    @Test
    public void testGetMatrixEmpty() {
        String query = "";
        boolean encode = true;
        
        MultivaluedMap<String,String> matrix = QueryStringParser.getMatrix(query, encode);
        
        assertNotNull(matrix);
        assertEquals(0, matrix.size());
    }
    
    @Test
    public void testGetMatrix() {
        String query = "//moremaps.com/map/color;lat=50;long=20;scale=32000";
        boolean encode = true;
        String defaultName = null;
        
        MultivaluedMap<String,String> matrix = QueryStringParser.getMatrix(query, encode);
        
        assertNotNull(matrix);
        assertEquals("[lat=50,long=20,scale=32000]", matrix.toString());
    }
    
    @Test
    public void testGetQueryParam(){
        
        String query = "test=myTestValue";
        boolean encode = true;
        String defaultName = null;
        
        List<String> queryParam = QueryStringParser.getParameterValues(query, encode, "test");
        
        assertNotNull(queryParam);
        assertEquals("[myTestValue]", queryParam.toString());
        
    }
}

// $Log: $

