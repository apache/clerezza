/*
 * Copyright (c) 2008-2009 trialox.org (trialox AG, Switzerland).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.clerezza.platform.content;

import java.net.URL;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.clerezza.platform.content.WebDavUtils.PropertyMap;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.platform.content.hierarchy.CollectionNode;
import org.apache.clerezza.platform.content.hierarchy.HierarchyNode;
import org.apache.clerezza.platform.content.hierarchy.NodeDoesNotExistException;
import org.apache.clerezza.platform.content.hierarchy.UnknownRootExcetpion;
import org.apache.clerezza.platform.content.webdav.COPY;
import org.apache.clerezza.platform.content.webdav.LOCK;
import org.apache.clerezza.platform.content.webdav.UNLOCK;
import org.apache.clerezza.platform.typehandlerspace.SupportedTypes;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.MGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.HIERARCHY;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.osgi.service.component.ComponentContext;

/**
 * Provides HTTP Methods for WebDav
 * 
 * @author ali
 */

@Component
@Service(Object.class)
@Property(name = "org.apache.clerezza.platform.typehandler", boolValue = true)
@SupportedTypes(types = { "http://clerezza.org/2009/09/hierarchy#Collection" }, prioritize = true)
public class CollectionTypeHandler extends DiscobitsTypeHandler{

	private Logger logger = LoggerFactory.getLogger(CollectionTypeHandler.class);
	
	@Reference
	private RenderletManager renderletManager;

	/**
	 * The activate method is called when SCR activates the component configuration.
	 * This method gets the system graph or create a new one if it doesn't exist.
	 *
	 * @param componentContext
	 */
	protected void activate(ComponentContext componentContext) {
		URL templateURL = getClass().getResource("collection.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(templateURL.toString()), HIERARCHY.Collection,
				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);

		logger.info("CollectionTypeHandler activated.");
	}

	/**
	 * Returns a GraphNode of the requested collection
	 * @return 
	 */
	@GET
	@Override
	public GraphNode getResource(@Context UriInfo uriInfo) {
		final UriRef uri = new UriRef(uriInfo.getAbsolutePath().toString());
		MGraph mGraph = new UnionMGraph(new SimpleMGraph(), cgProvider.getContentGraph());
		final GraphNode graphNode = new GraphNode(uri, mGraph);
		graphNode.addProperty(RDF.type, PLATFORM.HeadedPage);

		UriRef collectionUri = new UriRef(uriInfo.getAbsolutePath().toString());
		CollectionNode collection = null;
		try {
			collection = hierarchyService.getCollectionNode(collectionUri);
		} catch (NodeDoesNotExistException ex) {
			throw new WebApplicationException(ex);
		} catch (UnknownRootExcetpion ex) {
			throw new WebApplicationException(ex);
		}
		CollectionNode parent = collection.getParent();
		if (parent != null){
			graphNode.addProperty(HIERARCHY.parent, parent.getNode());
		}
		return graphNode;
	}

	@Override
	Map<UriRef, PropertyMap> getPropNames(HierarchyNode node, String depthHeader) {
		return WebDavUtils.getCollectionProps(null, null, null, (CollectionNode) node,
							depthHeader, false /* doesNotIncludeValues */);
	}

	@Override
	Map<UriRef, PropertyMap> getPropsByName(Node requestNode, HierarchyNode node,
			String depthHeader) {
		Map<UriRef, PropertyMap> result;
		NodeList children = requestNode.getChildNodes();
		result = WebDavUtils.getPropsByName(children, (CollectionNode) node, depthHeader,
				true /* includeValues */);
		return result;
	}

	@Override
	Map<UriRef, PropertyMap> getAllProps(HierarchyNode node, String depthHeader) {
		return WebDavUtils.getCollectionProps(null, null, null, (CollectionNode) node,
							depthHeader, true /* includeValues */);
	}

	/*-----------------------*
	 * Not Supported Methods * 
	 *-----------------------*/
	
	/**
	 * Locks a resource
	 *
	 * @return returns a 501 Not Implemented response
	 */
	@LOCK
	public Object lock() {
		return Response.status(501/* Not Implemented */).build();
	}

	/**
	 * Unlocks a resource
	 *
	 * @return returns a 501 Not Implemented response
	 */
	@UNLOCK
	public Object unlock() {
		return Response.status(501/* Not Implemented */).build();
	}

	/**
	 * Copies a resource
	 *
	 * @return returns a 501 Not Implemented response
	 */
	@COPY
	public Object copy() {
		return Response.status(501/* Not Implemented */).build();
	}
}
