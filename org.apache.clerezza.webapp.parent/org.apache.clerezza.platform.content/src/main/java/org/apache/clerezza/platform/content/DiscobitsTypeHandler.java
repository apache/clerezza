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
package org.apache.clerezza.platform.content;

import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.RuntimeDelegate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typehandlerspace.SupportedTypes;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * This Class allows getting and putting content structured using the
 * Discobits ontology.
 *
 * Is an implementation of DiscobitsHandler and additionally registers as
 * TypeHanlder to allow HTTP GET and PUT.
 *
 * @author reto, tho
 * 
 * @scr.component
 * @scr.service interface="java.lang.Object"
 * @scr.service interface="org.apache.clerezza.platform.content.DiscobitsHandler"
 * @scr.property name="org.apache.clerezza.platform.typehandler" type="Boolean" value="true"
 * 
 */
@SupportedTypes(types = { "http://www.w3.org/2000/01/rdf-schema#Resource" }, prioritize = false)
public class DiscobitsTypeHandler extends AbstractDiscobitsHandler
		implements DiscobitsHandler {

	/**
	 * @scr.reference
	 */
	private ContentGraphProvider cgProvider;
	
	private static final Logger logger = LoggerFactory.getLogger(DiscobitsTypeHandler.class);


	/**
	 * TypeHandle method for rdf types "TitledContext", "InfoDiscoBit",
	 * "OrderedContent" and "XHTMLInfoDiscoBit".
	 * 
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Produces({"*/*"})
	public Object getResource(@Context UriInfo uriInfo) {
		final MGraph mGraph = cgProvider.getContentGraph();
		final UriRef uri = new UriRef(uriInfo.getAbsolutePath().toString());
		final GraphNode graphNode = new GraphNode(uri, mGraph);
		final Iterator<Triple> typeStmts = mGraph.filter(uri, RDF.type, null);
		InfoDiscobit infoDiscobit = InfoDiscobit.createInstance(graphNode);
		if (infoDiscobit != null) {
			return infoDiscobit;
		} else {
			if (mGraph.filter(uri, null, null).hasNext() ||
					mGraph.filter(null, null, uri).hasNext()) {
				return graphNode;
			}
		}
		throw new WebApplicationException(RuntimeDelegate.getInstance()
				.createResponseBuilder().status(Status.NOT_FOUND)
				.entity("Sorry, we know nothing about this resource.").build());
	}
	
	/**
	 * Creates an <code>InfoDiscoBit</code> at the specified location
	 *
	 * @param uriInfo the uri of the InforDiscoBit to be created
	 * @param data the content of the upload
	 */
	@PUT
	public Response putInfoDiscobit(@Context UriInfo uriInfo, @Context HttpHeaders headers, byte[] data) {
		final String contentType;
		{
			final List<String> contentTypeHeaders = headers.getRequestHeader(HttpHeaders.CONTENT_TYPE);
			if (contentTypeHeaders == null) {
				logger.warn("Content-Type not specified");
				throw new WebApplicationException(RuntimeDelegate.getInstance()
					.createResponseBuilder().status(Status.BAD_REQUEST)
					.entity("Content-Type not specified").build());
			}
			contentType = contentTypeHeaders.get(0);
		}
		final UriRef infoDiscoBitUri = new UriRef(uriInfo.getAbsolutePath().toString());
		put(infoDiscoBitUri, MediaType.valueOf(contentType), data);
		return Response.status(Status.CREATED).build();
	}

	@Override
	protected MGraph getMGraph() {
		return cgProvider.getContentGraph();
	}

}
