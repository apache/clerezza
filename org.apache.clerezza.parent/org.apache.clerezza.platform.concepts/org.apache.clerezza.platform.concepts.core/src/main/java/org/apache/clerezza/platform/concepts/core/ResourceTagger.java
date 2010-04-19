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
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.osgi.service.component.ComponentContext;

/**
 * This JAX-RS resource can be used for adding concepts to a resource.
 *
 * The URI path of this service is /concepts/tagger.
 *
 * @author tio
 */
@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/concepts/tagger/")
public class ResourceTagger {

	@Reference
	protected ContentGraphProvider cgProvider;
	@Reference
	private RenderletManager renderletManager;

	protected void activate(ComponentContext context)
			throws URISyntaxException {

		URL template = getClass().getResource("concept-existing-subjects.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(template.toURI().toString()),
				RDFS.Resource, "concept-existing-subjects-naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);

		template = getClass().getResource("concept-tagging.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(template.toURI().toString()),
				RDFS.Resource, "concept-tagging-naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);

		template = getClass().getResource("concept-find-create.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(template.toURI().toString()),
				RDFS.Resource, "concept-find-create-naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);

		template = getClass().getResource("selected-concepts.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(template.toURI().toString()),
				RDFS.Resource, "selectedconcepts-naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}

	/**
	 * Adds concepts to a resource. If parameters uri and
	 * concepts are not defined a response with code BAD_REQUEST is returned. If
	 * the relation is succefully added a redirect to /concepts/generic-resource with
	 * the UriRef of the resource as parameter.
	 *
	 * @param uri specifies the uri of a resource
	 * @param concepts specifies a list of concept uris.
	 *
	 * @return
	 *		A Response
	 */
	@POST
	@Path("set")
	public Response updateConcepts(@FormParam("uri") UriRef uri,
			@FormParam("concepts") List<String> concepts,
			@Context UriInfo uriInfo) {

		
		if (uri != null) {
			GraphNode node = new GraphNode(uri, cgProvider.getContentGraph());
			node.deleteProperties(DCTERMS.subject);
			if(concepts != null) {
				for (String subject : concepts) {
					node.addProperty(DCTERMS.subject, new UriRef(subject));
				}
			}
		} else {
			throw new WebApplicationException(Response.status(
					Status.BAD_REQUEST).entity("No resource uri defined.").build());
		}
		return RedirectUtil.createSeeOtherResponse("/concepts/generic-resource?uri=" +
				uri.getUnicodeString(), uriInfo);
	}
}
