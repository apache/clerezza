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
package org.apache.clerezza.rdf.web.core;

import java.net.URL;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.security.TcPermission;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.TCPROVIDER;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.web.ontologies.GRAPHMANAGEMENT;
import org.apache.felix.scr.annotations.Services;
import org.osgi.service.component.ComponentContext;

/**
 * This JAX-RS resource provides an interface designed to allow various management
 * functions on triple collections. The URI path of this resource is
 * "/admin/graph-management".
 * 
 * @author reto
 */
@Component
@Services({
	@Service(value = Object.class),
	@Service(value = GlobalMenuItemsProvider.class)
})
@Property(name="javax.ws.rs", boolValue=true)
@Path("/admin/graphs")
public class GraphManagement implements GlobalMenuItemsProvider {

	@Reference
	private TcManager tcManager;

	@Reference
	private RenderletManager renderletManager;

	protected void activate(ComponentContext componentContext) {
		URL templateURL = getClass().getResource("graph-management.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(templateURL.toString()), GRAPHMANAGEMENT.GraphManagementPage,
				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}

	@GET
	public GraphNode mainPage(@Context UriInfo uriInfo) {
		AccessController.checkPermission(new GraphManagementAppPermission());
		TrailingSlash.enforcePresent(uriInfo);
		final SimpleMGraph resultGraph = new SimpleMGraph();
		GraphNode graphNode = new GraphNode(new BNode(), resultGraph);
		Set<UriRef> tripleCollections = tcManager.listTripleCollections();
		for (UriRef uriRef : tripleCollections) {
			graphNode.addProperty(GRAPHMANAGEMENT.tripleCollection, uriRef);
			final TripleCollection tripleCollection = tcManager.getTriples(uriRef);
			resultGraph.add(new TripleImpl(uriRef,GRAPHMANAGEMENT.size,
					LiteralFactory.getInstance().createTypedLiteral(
					tripleCollection.size())));
			if (tripleCollection instanceof Graph) {
				resultGraph.add(new TripleImpl(uriRef,RDF.type, TCPROVIDER.Graph));
			} else {
				resultGraph.add(new TripleImpl(uriRef,RDF.type, TCPROVIDER.MGraph));
			}
		}
		graphNode.addProperty(RDF.type, GRAPHMANAGEMENT.GraphManagementPage);
		graphNode.addProperty(RDF.type, PLATFORM.HeadedPage);
		return graphNode;
	}

	@Override
	public Set<GlobalMenuItem> getMenuItems() {

		Set<GlobalMenuItem> items = new HashSet<GlobalMenuItem>();
		try {
			AccessController.checkPermission(
					new GraphManagementAppPermission());
		} catch (AccessControlException e) {
			return items;
		}
		String path = "/admin/graphs";
		items.add(new GlobalMenuItem(path, "GM", "Graphs", 5,"Administration"));
		return items;
	}

}
