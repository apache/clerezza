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
package org.apache.clerezza.platform.accountcontrolpanel;


import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;


import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.security.UserUtil;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.platform.usermanager.UserManager;
import org.apache.clerezza.platform.users.WebIdGraphsService;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.clerezza.web.fileserver.FileServer;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Presents a panel where the user can create a webid and edit her profile.
 * 
 * @author reto
 */
@Component
@Service(value = Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/user/{id}/profile")
public class ProfilePanel extends FileServer {
	
	private static final Logger logger = LoggerFactory.getLogger(ProfilePanel.class);
	
	@Reference
	private UserManager userManager;
	
	@Reference
	private TcManager tcManager;
	
	@Reference
	private RenderletManager renderletManager;
	
	@Reference
	private WebIdGraphsService webIdGraphsService;
	
	@Reference
	private PlatformConfig platformConfig;
	
	protected void activate(ComponentContext componentContext) {
		URL templateURL = getClass().getResource("profile-panel.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(templateURL.toString()), CONTROLPANEL.ProfilePage,
				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);
		configure(componentContext.getBundleContext(), "profile-staticweb");
	}
	
	@GET
	public GraphNode getPersonalProfilePage(@Context UriInfo uriInfo, @PathParam(value = "id") String userName) {
		TrailingSlash.enforceNotPresent(uriInfo);
		GraphNode resultNode = getPersonalProfile(userName);
		resultNode.addProperty(RDF.type, PLATFORM.HeadedPage);
		resultNode.addProperty(RDF.type, CONTROLPANEL.ProfilePage);
		return resultNode;
	}
	
	private	GraphNode getPersonalProfile(final String userName) {
		return AccessController.doPrivileged(new PrivilegedAction<GraphNode>() {
			@Override
			public GraphNode run() {
				GraphNode userInSystemGraph = userManager.getUserInSystemGraph(userName);
				NonLiteral userNodeInSystemGraph = (NonLiteral)userInSystemGraph.getNode();
				if (userNodeInSystemGraph instanceof BNode) {
					//no personal profile without web-id
					SimpleMGraph simpleMGraph = new SimpleMGraph();
					GraphNode profileNode = new GraphNode(new BNode(), simpleMGraph);
					profileNode.addProperty(CONTROLPANEL.isLocalProfile, 
							LiteralFactory.getInstance().createTypedLiteral(true));
					UriRef suggestedPPDUri = getSuggestedPPDUri(userName);
					profileNode.addProperty(CONTROLPANEL.suggestedPPDUri, 
							LiteralFactory.getInstance().createTypedLiteral(suggestedPPDUri));
					NonLiteral agent = new BNode();
					profileNode.addProperty(FOAF.primaryTopic, agent);
					simpleMGraph.add(new TripleImpl(agent, PLATFORM.userName, 
							LiteralFactory.getInstance().createTypedLiteral(userName)));
					return profileNode;
				} else {
					return getProfileInUserGraph((UriRef)userNodeInSystemGraph);
				}
			}
		});
	}

	private UriRef getSuggestedPPDUri(String userName) {
		return new UriRef(platformConfig.getDefaultBaseUri().getUnicodeString()+
							"user/"+userName+"/profile");
	}
	
	private GraphNode getProfileInUserGraph(UriRef webId) {
		WebIdGraphsService.WebIdGraphs webIdGraphs = webIdGraphsService.getWebIdGraphs(webId);
		MGraph userGraph = webIdGraphs.publicUserGraph();
		logger.debug("got publicUserGraph of size {}.", userGraph.size());
		GraphNode userGraphNode = new GraphNode(webId, userGraph);
		GraphNode resultNode = new GraphNode(new BNode(), 
				new UnionMGraph(new SimpleMGraph(), userGraphNode.getGraph()));
		resultNode.addProperty(CONTROLPANEL.isLocalProfile, 
				LiteralFactory.getInstance().createTypedLiteral(webIdGraphs.isLocal()));
		resultNode.addProperty(FOAF.primaryTopic, userGraphNode.getNode());
		return resultNode;
	}
	
	@POST
	@Path("set-existing-webid")
	public Response setExistingWebId(@Context final UriInfo uriInfo, 
			@FormParam("webid") final UriRef webId, @PathParam(value = "id") final String userName) {
		//TODO check that its not local
		//TODO check its not an existing user
		return AccessController.doPrivileged(new PrivilegedAction<Response>() {
			@Override
			public Response run() {
				GraphNode userInSystemGraph = userManager.getUserInSystemGraph(userName);
				userInSystemGraph.replaceWith(webId);
				return RedirectUtil.createSeeOtherResponse("../profile", uriInfo);
			}
		});
	}

	@POST
	@Path("create-new-web-id")
	public Response createNewWebId(@Context final UriInfo uriInfo,
			@PathParam(value = "id") final String userName) {
		//TODO check its not an existing user
		final UriRef ppd = getSuggestedPPDUri(userName);
		final UriRef webId = new UriRef(ppd.getUnicodeString()+"#me");
		final WebIdGraphsService.WebIdGraphs webIdGraphs = webIdGraphsService.getWebIdGraphs(webId);
		webIdGraphs.localGraph().add(new TripleImpl(ppd, FOAF.primaryTopic, webId));
		webIdGraphs.localGraph().add(new TripleImpl(ppd, RDF.type, FOAF.PersonalProfileDocument));
		return AccessController.doPrivileged(new PrivilegedAction<Response>() {
			@Override
			public Response run() {
				GraphNode userInSystemGraph = userManager.getUserInSystemGraph(userName);
				userInSystemGraph.replaceWith(webId);
				return RedirectUtil.createSeeOtherResponse("../profile", uriInfo);
			}
		});
	}

	@POST
	@Path("modify")
	public Response modifyProfile(@Context final UriInfo uriInfo,
			@PathParam(value = "id") final String userName,
			@FormParam("webId") final UriRef webId,
			@FormParam("name") final String name,
			@FormParam("description") final String description) {
		final WebIdGraphsService.WebIdGraphs webIdGraphs = webIdGraphsService.getWebIdGraphs(webId);
		final GraphNode agent = new GraphNode(webId, webIdGraphs.localGraph());
		agent.deleteProperties(FOAF.name);
		agent.addPropertyValue(FOAF.name, name);
		agent.deleteProperties(DC.description);
		agent.addPropertyValue(DC.description, description);
		logger.info("desc:"+description);
		logger.info("local graph (uri: {}) is now of size {}", webIdGraphs.localGraphUri(), webIdGraphs.localGraph().size());
		return RedirectUtil.createSeeOtherResponse("../profile", uriInfo);
	}
	
}
