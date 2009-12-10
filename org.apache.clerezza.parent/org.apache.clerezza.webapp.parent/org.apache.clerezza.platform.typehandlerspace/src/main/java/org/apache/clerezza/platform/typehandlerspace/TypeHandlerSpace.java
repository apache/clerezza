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
package org.apache.clerezza.platform.typehandlerspace;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.clerezza.jaxrs.extensions.HttpRequest;
import org.apache.clerezza.jaxrs.extensions.MethodResponse;
import org.apache.clerezza.jaxrs.extensions.ResourceMethodException;
import org.apache.clerezza.jaxrs.extensions.RootResourceExecutor;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;


/**
 * When handling a request <code>TypeHandlerSpace</code> checks if a resource
 * with the requested URI exists in the content graph. If that is the case, then a
 * TypeHandler according the rdf-type(s) of the requested resource is used to
 * handle the request and resource. If there is no resource, then a fallback
 * TypeHandler will be used to handle the request. A TypeHandler is a jaxrs
 * resource, that is registered with TypeHandlerDiscovery to handle a specific
 * rdf-type.
 * 
 * @author mir
 * 
 * @scr.component
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true"
 */
@Path("/")
public class TypeHandlerSpace {

	/**
	 * @scr.reference
	 */
	TcManager tcManager;

	/**
	 * @scr.reference
	 */
	RootResourceExecutor resourceExecutor;

	/**
	 * @scr.reference cardinality "1..1"
	 */
	TypeHandlerDiscovery typeHandlerDiscovery;

	public static final UriRef CONTENT_GRAPH_URI = new UriRef(
			"http://tpf.localhost/content.graph");
	
	private final String DESCRIPTION_SUFFIX = "-description";

	/**
	 * See the <code>handlerGet</code> method. this method is to provide a
	 * resource method (not a sub-resource method) as well
	 */
	@GET
	public Object handleGetOnRoot(@Context UriInfo uriInfo,
			@Context HttpRequest request) throws ResourceMethodException {
		return handleGet(uriInfo, request);
	}

	/**
	 * Handles requests, which have the http-method GET. The requests are
	 * handled with the TypeHandler according the most important rdf-type of the
	 * resource.
	 * 
	 * If the request-URI ends with "-description" then context of the resource
	 * in the content-graph is returned.
	 * 
	 * @param uriInfo
	 * @param request
	 * @return MethodResponse or a Graph
	 */
	@GET
	@Path("{path:.+}")
	public Object handleGet(@Context UriInfo uriInfo,
			@Context HttpRequest request) throws ResourceMethodException {
		String absoluteUriPath = uriInfo.getAbsolutePath().toString();
		UriRef uri;
		if (absoluteUriPath.endsWith(DESCRIPTION_SUFFIX)) {
			MGraph contentMGraph = tcManager.getMGraph(CONTENT_GRAPH_URI);
			uri = new UriRef(absoluteUriPath.substring(0, absoluteUriPath
					.length()
					- DESCRIPTION_SUFFIX.length()));
			GraphNode graphNode = new GraphNode(uri, contentMGraph);
			return graphNode.getNodeContext();
		} else {
			return handleWithTypeHandler(request, absoluteUriPath);
		}
	}


	/**
	 * See the <code>handleHttpMethods</code> method. this method is to provide a
	 * resource method (not a sub-resource method) as well
	 */
	@PUT
	@POST
	@DELETE
	@HEAD
	@OPTIONS
	public MethodResponse handleHttpMethodsRoot(@Context UriInfo uriInfo,
			@Context HttpRequest request) throws ResourceMethodException {
		return handleHttpMethods(uriInfo, request);
	}
	/**
	 * Handles requests, which have the http-methods PUT, POST, DELETE, HEAD and
	 * OPTIONS. The requests are handled with the TypeHandler according the most
	 * important rdf-type of the requested resource.
	 * 
	 * @param uriInfo
	 * @param request
	 * @return MethodResponse
	 */
	@PUT
	@POST
	@DELETE
	@HEAD
	@OPTIONS
	@Path("{path:.+}")
	public MethodResponse handleHttpMethods(@Context UriInfo uriInfo,
			@Context HttpRequest request) throws ResourceMethodException {
		String absoluteUriPath = uriInfo.getAbsolutePath().toString();
		return handleWithTypeHandler(request, absoluteUriPath);
	}

	private MethodResponse handleWithTypeHandler(HttpRequest request,
			String absoluteUriPath) throws ResourceMethodException {
		MGraph contentMGraph = tcManager.getMGraph(CONTENT_GRAPH_URI);
		UriRef uri = new UriRef(absoluteUriPath);

		Set<UriRef> rdfTypes = getRdfTypesOfUriRef(contentMGraph, uri);

		Object typeHandler = typeHandlerDiscovery.getTypeHandler(rdfTypes);
		MethodResponse MethodResponse = executeOnTypeHandler(request,
				typeHandler);
		return MethodResponse;
	}

	private Set<UriRef> getRdfTypesOfUriRef(MGraph contentMGraph, UriRef uri) {
		Iterator<Triple> typeStmts = contentMGraph.filter(uri, RDF.type, null);
		Set<UriRef> rdfTypes = new HashSet<UriRef>();
		while (typeStmts.hasNext()) {
			Triple triple = typeStmts.next();
			Resource typeStmtObj = triple.getObject();
			if (!(typeStmtObj instanceof UriRef)) {
				throw new RuntimeException(
						"RDF type is expected to be a URI but is "+typeStmtObj+
						"(in "+triple+")");
			}
			UriRef rdfType = (UriRef) typeStmtObj;
			rdfTypes.add(rdfType);
		}
		return rdfTypes;
	}

	private MethodResponse executeOnTypeHandler(HttpRequest request,
			Object typeHandler) throws ResourceMethodException {
		MethodResponse MethodResponse = null;
		
		MethodResponse = resourceExecutor.execute(request,
				typeHandler,"", null);
		
		return MethodResponse;
	}
}
