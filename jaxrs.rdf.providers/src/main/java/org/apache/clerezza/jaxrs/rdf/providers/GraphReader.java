/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */
package org.apache.clerezza.jaxrs.rdf.providers;

import org.apache.clerezza.Graph;
import org.apache.clerezza.implementation.in_memory.SimpleGraph;
import org.apache.clerezza.representation.Parser;
import org.apache.clerezza.representation.SupportedFormat;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;

@Component(service = Object.class, property = {"javax.ws.rs=true"})
@Provider
@Consumes({SupportedFormat.N3, SupportedFormat.N_TRIPLE,
        SupportedFormat.RDF_XML, SupportedFormat.TURTLE,
        SupportedFormat.X_TURTLE, SupportedFormat.RDF_JSON})
public class GraphReader implements MessageBodyReader<Graph> {

    private Parser parser = Parser.getInstance();

    @Reference
    public synchronized void setParser(Parser parser) {
        this.parser = parser;
    }

    public synchronized void unsetParser(Parser parser) {
        if (Objects.equals(this.parser, parser)) {
            this.parser = Parser.getInstance();
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType,
                              Annotation[] annotations, MediaType mediaType) {
        return type.isAssignableFrom(Graph.class);
    }

    @Override
    public Graph readFrom(Class<Graph> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        Graph result = new SimpleGraph();
        return parser.parse(entityStream, mediaType.toString());
    }
}
