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
package org.apache.clerezza.jaxrs.rdf.providers;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;


/**
 * @scr.component
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true"
 * 
 * @author reto
 */
@Provider
@Produces({SupportedFormat.N3, SupportedFormat.N_TRIPLE,
    SupportedFormat.RDF_XML, SupportedFormat.TURTLE,
    SupportedFormat.X_TURTLE, SupportedFormat.RDF_JSON})
public class GraphWriter implements MessageBodyWriter<TripleCollection> {

    /**
     * @scr.reference
     */
    private Serializer serializer;
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType, 
            Annotation[] annotations, MediaType mediaType) {
        return TripleCollection.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(TripleCollection t, Class<?> type, Type genericType, 
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(TripleCollection t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException, WebApplicationException {
        serializer.serialize(entityStream, t, mediaType.toString());
    }
}
