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
package org.apache.clerezza.platform.concepts.core;

import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.clerezza.platform.concepts.ontologies.CONCEPTS;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.clerezza.utils.UriException;
import org.apache.clerezza.web.fileserver.FileServer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/**
 * This JAX-RS resource can be used to show selected concepts of a resource.
 * The URI path of this service is /concepts/generic-resource.
 * 
 * @author tio
 */
@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/concepts/generic-resource")
public class GenericResourcePage extends FileServer {

	@Reference
	protected ContentGraphProvider cgProvider;
	@Reference
	private RenderletManager renderletManager;

	private RemoteConceptsDescriptionManager remoteConceptsDescriptionManager = null;

	protected void activate(ComponentContext context)
			throws URISyntaxException {
		
		configure(context.getBundleContext());
		
		URL template = getClass().getResource("generic-resource-page.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(template.toURI().toString()),
				CONCEPTS.GenericResourcePage, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);

		remoteConceptsDescriptionManager = new RemoteConceptsDescriptionManager();
	}

	/**
	 * Retrieves a resource and its associated concepts to be rendered with a template
	 * registered for CONCEPTS.GenericResourcePage.
	 * 
	 * @param uri specifies the uri of a resource
	 * 
	 * @return GraphNode
	 */
	@GET
	public GraphNode GenericResourcePage(@QueryParam("uri") UriRef uri,
			@Context UriInfo uriInfo) {

		GraphNode node = new GraphNode(new BNode(), new UnionMGraph(new SimpleMGraph(),
				cgProvider.getContentGraph(),
				remoteConceptsDescriptionManager.getRemoteConceptsDescriptionMGraph()));
		node.addProperty(RDF.type, PLATFORM.HeadedPage);
		node.addProperty(RDF.type, CONCEPTS.GenericResourcePage);
		if (uri != null) {
			node.addProperty(CONCEPTS.resource, uri);
		} else {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
					.entity("No resource uri defined.").build());
		}
		return node;
	}
}
