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
import java.util.concurrent.locks.Lock;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.apache.clerezza.jaxrs.extensions.ResourceMethodException;
import org.apache.clerezza.jaxrs.extensions.RootResourceExecutor;
import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
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
 * @author mir, agron
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

	
	private final String DESCRIPTION_SUFFIX = "-description";
	private DescriptionHandler descriptionHandler = new DescriptionHandler();


	/**
	 * Returns a TypeHandler according the most important rdf-type of the
	 * requested resource.
	 * 
	 * @param uriInfo
	 * @param request
	 * @return
	 * @throws ResourceMethodException
	 */
	@Path("{path:.*}")
	public Object getHandler(@Context UriInfo uriInfo,
			@Context Request request) throws ResourceMethodException  {
		String absoluteUriPath = uriInfo.getAbsolutePath().toString();
		if (absoluteUriPath.endsWith(DESCRIPTION_SUFFIX)) {
			if (request.getMethod().equalsIgnoreCase("GET")) {
				return descriptionHandler;
			}
		}
		return getTypeHandler(absoluteUriPath);
	}

	private Object getTypeHandler(String absoluteUriPath) throws ResourceMethodException {
		LockableMGraph contentMGraph = tcManager.getMGraph(Constants.CONTENT_GRAPH_URI);
		UriRef uri = new UriRef(absoluteUriPath);

		Set<UriRef> rdfTypes = getRdfTypesOfUriRef(contentMGraph, uri);

		return typeHandlerDiscovery.getTypeHandler(rdfTypes);
		
	}

	private Set<UriRef> getRdfTypesOfUriRef(LockableMGraph contentMGraph, UriRef uri) {
		Set<UriRef> rdfTypes = new HashSet<UriRef>();
		Lock readLock = contentMGraph.getLock().readLock();
		readLock.lock();
		try {
			Iterator<Triple> typeStmts = contentMGraph.filter(uri, RDF.type, null);

			while (typeStmts.hasNext()) {
				Triple triple = typeStmts.next();
				Resource typeStmtObj = triple.getObject();
				if (!(typeStmtObj instanceof UriRef)) {
					throw new RuntimeException(
							"RDF type is expected to be a URI but is " + typeStmtObj
							+ "(in " + triple + ")");
				}
				UriRef rdfType = (UriRef) typeStmtObj;
				rdfTypes.add(rdfType);
			}
		} finally {
			readLock.unlock();
		}
		return rdfTypes;
	}

	public class DescriptionHandler {

		@GET
		public Object getDescription(@Context UriInfo uriInfo){
			String absoluteUriPath = uriInfo.getAbsolutePath().toString();
			MGraph contentMGraph = tcManager.getMGraph(Constants.CONTENT_GRAPH_URI);
				UriRef uri = new UriRef(absoluteUriPath.substring(0,
						absoluteUriPath.length() - DESCRIPTION_SUFFIX.length()));
				GraphNode graphNode = new GraphNode(uri, contentMGraph);
				return graphNode.getNodeContext();
		}
	}
}
