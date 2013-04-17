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
package org.apache.clerezza.platform.typehandlerspace.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.clerezza.jaxrs.extensions.ResourceMethodException;
import org.apache.clerezza.platform.graphnodeprovider.GraphNodeProvider;
import org.apache.clerezza.platform.typehandlerspace.TypeHandlerDiscovery;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.wink.osgi.WinkRequestProcessor;


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
@Component(immediate = true, metatype = false)
@Service(value = javax.servlet.Filter.class)
@Properties({
    @Property(name ="pattern", value=".*"),
    @Property(name ="service.ranking", intValue=100)
    
})
public class TypeHandlerSpace implements Filter {

    @Reference
    GraphNodeProvider gnp;

    @Reference
    TypeHandlerDiscovery typeHandlerDiscovery;

    
    @Reference
    private WinkRequestProcessor winkProvider;
    
    private final String DESCRIPTION_SUFFIX = "-description";
    private DescriptionHandler descriptionHandler = new DescriptionHandler();

    
    public void init(FilterConfig filterConfig) throws ServletException {
        //no initialization needed
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
        } else {
            chain.doFilter(request, response);
        }    
    }
    
    public void doFilter(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        try {
            Object resource = getTypeHandler(request.getRequestURL().toString());
            if (resource != null) {
                winkProvider.handleRequest(request, response, resource);
            } else {
                chain.doFilter(request, response);
            }
        } catch (ResourceMethodException ex) {
            throw new ServletException(ex);
        }
        
    }

    public void destroy() {
        //not
    }

    /**
     * Returns a TypeHandler according the most important rdf-type of the
     * requested resource.
     * 
     * @param uriInfo
     * @param request
     * @return
     * @throws ResourceMethodException
     */
    /*@Path("{path:.*}")
    public Object getHandler(@Context UriInfo uriInfo,
            @Context Request request) throws ResourceMethodException  {
        String absoluteUriPath = uriInfo.getAbsolutePath().toString();
        if (absoluteUriPath.endsWith(DESCRIPTION_SUFFIX)) {
            if (request.getMethod().equalsIgnoreCase("GET")) {
                return descriptionHandler;
            }
        }
        return getTypeHandler(absoluteUriPath);
    }*/

    private Object getTypeHandler(String absoluteUriPath) throws ResourceMethodException {
        UriRef uri = new UriRef(absoluteUriPath);
        if (gnp.existsLocal(uri)) {
            GraphNode node = gnp.getLocal(uri);
            Lock lock =node.readLock();
            lock.lock();
            try {
                Set<UriRef> rdfTypes = getRdfTypesOfUriRef(node);
                return typeHandlerDiscovery.getTypeHandler(rdfTypes);
            } finally {
                lock.unlock();
            }
        }
        
        return null;
    }

    private Set<UriRef> getRdfTypesOfUriRef(GraphNode node) {
        Set<UriRef> rdfTypes = new HashSet<UriRef>();
        Iterator<Resource> types = node.getObjects(RDF.type);
        while (types.hasNext()) {
            Resource typeStmtObj = types.next();
            if (!(typeStmtObj instanceof UriRef)) {
                throw new RuntimeException(
                        "RDF type is expected to be a URI but is " + typeStmtObj
                        + "(of " + node.getNode() + ")");
            }
            UriRef rdfType = (UriRef) typeStmtObj;
            rdfTypes.add(rdfType);
        }
        
        return rdfTypes;
    }

    public class DescriptionHandler {

        @GET
        public Object getDescription(@Context UriInfo uriInfo){
            String absoluteUriPath = uriInfo.getAbsolutePath().toString();
            //MGraph contentMGraph = cgp.getContentGraph();
                UriRef uri = new UriRef(absoluteUriPath.substring(0,
                        absoluteUriPath.length() - DESCRIPTION_SUFFIX.length()));
                GraphNode graphNode = gnp.getLocal(uri);
                return graphNode.getNodeContext();
        }
    }
}
