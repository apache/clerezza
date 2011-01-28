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
package org.apache.clerezza.triaxrs.providers.provided;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import java.nio.charset.Charset;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 *  message body writer for string
 * 
 * @author szalay
 */
@Produces({"text/plain", "*/*"})
public final class StringMessageBodyWriter implements
        MessageBodyWriter<String>, Comparable {
	public static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    public int compareTo(Object o) {

        if (o instanceof StringMessageBodyWriter) {
            return 0;
        }

        return -1;
    }
     
   
    @Override
    public long getSize(String string, java.lang.Class<?> type, java.lang.reflect.Type genericType,
             java.lang.annotation.Annotation[] annotations, MediaType mediaType) {	
		return string.getBytes(UTF8).length;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return type == String.class;
    }

    @Override
    public void writeTo(String value, Class<?> arg1, Type arg2,
            Annotation[] arg3, MediaType arg4,
            MultivaluedMap<String, Object> arg5, OutputStream out)
            throws IOException, WebApplicationException {
        out.write(value.getBytes(UTF8));
        out.flush();
    }
    
    @Override
    public String toString(){
        return "[StringMessageBodyWriter: String.class]";
    }
}
