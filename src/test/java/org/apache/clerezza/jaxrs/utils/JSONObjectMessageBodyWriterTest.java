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
package org.apache.clerezza.jaxrs.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.clerezza.jaxrs.testutils.TestWebServer;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author hasan
 */
public class JSONObjectMessageBodyWriterTest {

    @Path("/foo")
    public class MyResource {

        @Path("bar")
        @GET
        public JSONObject myMethod(@QueryParam("name") String name){
            JSONObject obj = new JSONObject();
            obj.put("name", JSONObject.escape(name));
            return obj;
        }
    }

    @Test
    public void testMbwWithoutUmlaut() throws IOException {
        testMbw("foobar");
    }

//CLEREZZA-681: Disabled test because it fails on the Jenkins Build Server. 
//See https://issues.apache.org/jira/browse/CLEREZZA-681
//    @Test
//    public void testMbwWithUmlaut() throws IOException {
//        testMbw("foob\u00E4r"); // foobär
//    }

    private void testMbw(String param) throws IOException {
        final TestWebServer testWebServer = createTestWebServer(new MyResource());
        int port = testWebServer.getPort();
        URL serverURL = new URL("http://localhost:" + port + "/foo/bar?name=" + param);
        HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Accept", "application/json, */*; q=.2");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String line = br.readLine();
//        System.out.println(line);
        Assert.assertEquals("{\"name\":\""+param+"\"}", line);
        testWebServer.stop();
    }

    private TestWebServer createTestWebServer(final Object resource) {
        return new TestWebServer(new Application() {

            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> result = new HashSet<Class<?>>();
                return result;
            }

            @Override
            public Set<Object> getSingletons() {
                Set<Object> result = new HashSet<Object>();
                result.add(resource);
                result.add(new JSONObjectMessageBodyWriter());
                return result;
            }
        });
    }

//CLEREZZA-681: Disabled test because it fails on the Jenkins Build Server. 
//See https://issues.apache.org/jira/browse/CLEREZZA-681
//    @Test
    public void testWriteToAndGetSizeWithUmlaut() throws Exception {
        testWriteToAndGetSize("foob\u00E4r"); // foobär
    }

    @Test
    public void testWriteToAndGetSizeWithoutUmlaut() throws Exception {
        testWriteToAndGetSize("foobar");
    }

    private void testWriteToAndGetSize(String name) throws Exception {
        JSONObject value = new JSONObject();
        value.put("name", JSONObject.escape(name));
        Class<?> type = null;
        Type genericType = null;
        Annotation[] annotations = null;
        MediaType mediaType = null;
        MultivaluedMap<String, Object> httpHeaders = null;

        OutputStream out = new ByteArrayOutputStream();
        JSONObjectMessageBodyWriter instance = new JSONObjectMessageBodyWriter();
        long size = instance.getSize(value, type, genericType, annotations, mediaType);
//        System.out.println(size);
        instance.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, out);
        long len = out.toString().getBytes("UTF-8").length;
//        System.out.println(out.toString());
//        System.out.println(len);
        Assert.assertEquals(size, len);
    }
}
