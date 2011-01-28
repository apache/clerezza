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
package org.apache.clerezza.platform.dashboard.webinterface;

import java.net.URL;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.clerezza.platform.dashboard.ontologies.DASHBOARD;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Services;

/**
 *
 * @author tio
 */
@Component
@Services({
	@Service(Object.class),
	@Service(GlobalMenuItemsProvider.class)
})

@Property(name="javax.ws.rs", boolValue=true)
@Path("/dashboard")
public class DashBoard implements GlobalMenuItemsProvider{

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Reference
	private TcManager tcMgr;
	
	@Reference
	private RenderletManager renderletManager;

	protected void activate(ComponentContext cCtx) throws Exception {
		logger.debug("Activating DashBoard");	
	
		URL template = getClass().getResource("dashboard-template.xhtml");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(), 
				new UriRef(template.toURI().toString()), DASHBOARD.DashBoard,
				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}
	
	/**
	 * Redirects to the overview page
	 * 
	 * @return {@link Response}
	 * 
	 */
	@GET
	public Response redirectToHomePage(@Context UriInfo uriInfo) {
		if (uriInfo.getAbsolutePath().toString().endsWith("/")) {
			return RedirectUtil.createSeeOtherResponse("overview", uriInfo);
		}
		return RedirectUtil.createSeeOtherResponse(
				"dashboard/overview", uriInfo);
	}

	/**
	 * Returns the overview page of the dashboard.
	 * 
	 * @return {@link GraphNode}
	 * 
	 */
	@GET
	@Path("overview")
	public GraphNode getHomePage(@Context UriInfo uriInfo) {

		TrailingSlash.enforceNotPresent(uriInfo);

		MGraph mGraph = new SimpleMGraph(); 
		NonLiteral overview = new BNode();
		mGraph.add(new TripleImpl(overview, RDF.type, DASHBOARD.DashBoard));
		mGraph.add(new TripleImpl(overview, RDF.type, PLATFORM.HeadedPage));
		return new GraphNode(overview, mGraph);
	}

	@Override
	public Set<GlobalMenuItem> getMenuItems() {
		Set<GlobalMenuItem> items = new HashSet<GlobalMenuItem>();
		items.add(new GlobalMenuItem("/dashboard", "DHB", "Dashboard", 10000,
				null));
		return items;
	}

}
