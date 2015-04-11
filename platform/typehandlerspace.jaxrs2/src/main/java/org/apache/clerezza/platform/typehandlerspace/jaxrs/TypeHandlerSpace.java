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
package org.apache.clerezza.platform.typehandlerspace.jaxrs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.clerezza.rdf.utils.graphnodeprovider.GraphNodeProvider;
import org.apache.clerezza.platform.typehandlerspace.TypeHandlerDiscovery;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;


/**
 * When handling a request <code>TypeHandlerSpace</code> checks if a resource
 * with the requested URI exists in the content graph. If that is the case, then a
 * TypeHandler according the rdf-type(s) of the requested resource is used to
 * handle the request and resource. If there is no resource, then a fallback
 * TypeHandler will be used to handle the request. A TypeHandler is a jaxrs
 * resource, that is registered with TypeHandlerDiscovery to handle a specific
 * rdf-type.
 * 
 * 
  */
@Component
@Service(Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Path("")
public class TypeHandlerSpace {

    @Reference
    GraphNodeProvider gnp;

    @Reference
    TypeHandlerDiscovery typeHandlerDiscovery;
    
    private final String DESCRIPTION_SUFFIX = "-description";
    private DescriptionHandler descriptionHandler = new DescriptionHandler();

    /**
     * Returns a TypeHandler according the most important rdf-type of the
     * requested resource.
     * 
     * @param uriInfo
     * @param request
     * @return
     */
    @Path("{path : .*}")
    public Object getTypeHandler(@Context UriInfo uriInfo, 
            @Context Request request) {
        String absoluteUriPath = uriInfo.getAbsolutePath().toString();
        if (absoluteUriPath.endsWith(DESCRIPTION_SUFFIX)) {
            if (request.getMethod().equalsIgnoreCase("GET")) {
                return descriptionHandler;
            }
        }
        return getTypeHandler(absoluteUriPath);
                
    }

    private Object getTypeHandler(String absoluteUriPath) {
        IRI uri = new IRI(absoluteUriPath);
        if (gnp.existsLocal(uri)) {
            GraphNode node = gnp.getLocal(uri);
            Lock lock =node.readLock();
            lock.lock();
            try {
                Set<IRI> rdfTypes = getRdfTypesOfIri(node);
                return typeHandlerDiscovery.getTypeHandler(rdfTypes);
            } finally {
                lock.unlock();
            }
        }
        
        return null;
    }

    private Set<IRI> getRdfTypesOfIri(GraphNode node) {
        Set<IRI> rdfTypes = new HashSet<IRI>();
        Iterator<RDFTerm> types = node.getObjects(RDF.type);
        while (types.hasNext()) {
            RDFTerm typeStmtObj = types.next();
            if (!(typeStmtObj instanceof IRI)) {
                throw new RuntimeException(
                        "RDF type is expected to be a URI but is " + typeStmtObj
                        + "(of " + node.getNode() + ")");
            }
            IRI rdfType = (IRI) typeStmtObj;
            rdfTypes.add(rdfType);
        }
        
        return rdfTypes;
    }

    public class DescriptionHandler {

        @GET
        public Object getDescription(@Context UriInfo uriInfo){
            String absoluteUriPath = uriInfo.getAbsolutePath().toString();
            //Graph contentGraph = cgp.getContentGraph();
                IRI uri = new IRI(absoluteUriPath.substring(0,
                        absoluteUriPath.length() - DESCRIPTION_SUFFIX.length()));
                GraphNode graphNode = gnp.getLocal(uri);
                return graphNode.getNodeContext();
        }
    }
}
