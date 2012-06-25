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
package org.apache.clerezza.platform.scripting;

import java.util.Iterator;
import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.ws.rs.GET;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typehandlerspace.SupportedTypes;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.SCRIPT;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * This class handles HTTP GET requests to resources of type ScriptGeneratedResource.
 *
 * @author daniel, hasan
 * 
 * @see org.apache.clerezza.rdf.ontologies.SCRIPT#ScriptGeneratedResource
 *
 */
@Component
@Service(Object.class)
@Property(name="org.apache.clerezza.platform.typehandler", boolValue=true)
@SupportedTypes(types = { "http://clerezza.org/2009/07/script#ScriptGeneratedResource" }, prioritize = true)
public class ScriptGeneratedResourceTypeHandler {

	private static final Logger logger =
			LoggerFactory.getLogger(ScriptGeneratedResourceTypeHandler.class);

        @Reference
	private ContentGraphProvider cgProvider;

        @Reference
	private ScriptExecution scriptExecution;

	/**
	 * Handles HTTP GET requests for resources of type ScriptGeneratedResource.
	 *
	 * The generated resource is either a GraphNode or a response with
	 * the media type specified by the producedType property
	 * of the associated script.
	 *
	 * GraphNodes can be rendered by Renderlets registered for the type
	 * of the GraphNode.
	 *
	 * {@code UriInfo}, {@code Request}, and {@code HttpHeaders} are provided
	 * to the generating script for use in execution. In a script, they are
	 * accessible under the names: uriInfo, request, and httpHeaders respectively.
	 *
	 * @param uriInfo
	 *			info about the request URI.
	 * @param request
	 *			helper for request processing
	 * @param httpHeaders
	 *			provides access to HTTP header information
	 * 
	 * @return	The generated resource. If no script is found, a NOT FOUND (404)
	 *			response is returned.
	 *
	 * @see org.apache.clerezza.rdf.ontologies.SCRIPT#producedType
	 * @see org.apache.clerezza.platform.typerendering.Renderlet
	 */
	@GET
	public Object get(@Context UriInfo uriInfo, @Context Request request,
			@Context HttpHeaders httpHeaders) {
		TrailingSlash.enforceNotPresent(uriInfo);

		UriRef requestUri = new UriRef(uriInfo.getAbsolutePath().toString());
		Iterator<Triple> it = cgProvider.getContentGraph().
				filter(requestUri, SCRIPT.scriptSource, null);

		if(it.hasNext()) {
			NonLiteral scriptResource = (NonLiteral) it.next().getObject();
			try {
				Bindings bindings = new SimpleBindings();
				bindings.put("uriInfo", uriInfo);
				bindings.put("request", request);
				bindings.put("httpHeaders", httpHeaders);
				return scriptExecution.execute(scriptResource, bindings);
			} catch (ScriptException ex) {
				logger.warn("Exception while executing script {}",
						((UriRef) scriptResource).getUnicodeString());
				throw new WebApplicationException(
						Response.status(Status.INTERNAL_SERVER_ERROR).
						entity(ex.getMessage()).build());
			}
		}

		logger.warn("There is no script associated with {}",
				requestUri.getUnicodeString());
		return Response.status(Status.NOT_FOUND).build();
	}

}
