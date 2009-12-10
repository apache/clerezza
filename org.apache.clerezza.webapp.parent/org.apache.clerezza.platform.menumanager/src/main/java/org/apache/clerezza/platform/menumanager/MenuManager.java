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
package org.apache.clerezza.platform.menumanager;

import java.util.Iterator;
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.platform.content.hierarchy.CollectionNode;
import org.apache.clerezza.platform.content.hierarchy.HierarchyNode;
import org.apache.clerezza.platform.content.hierarchy.HierarchyService;
import org.apache.clerezza.platform.content.hierarchy.NodeDoesNotExistException;
import org.apache.clerezza.rdf.ontologies.MENU;
import org.apache.clerezza.rdf.ontologies.RDF;


/**
 * The menu consists of hierarchy node that are marked as menu nodes. 
 * The menu manager provides for methods marking and unmarking hierarchy nodes
 * in the content graph.
 * This is a JAX-RS resource with the path "/tools/menu-manager".
 * 
 * @author mir
 */
@Component
@Service(value = Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/tools/menu-manager")
public class MenuManager {

	@Reference
	private HierarchyService hierarchyService;

	/**
	 * Marks the specified hierarchy node as menu node. The parents of the
	 * specified hierarchy node will also be marked recursively as menu nodes,
	 * if they are not already.
	 * @request.param {@name hierarchyNode}
     *                  {@style header}
     *                  {@type {http://www.w3.org/2001/XMLSchema}anyURI}
     *                  {@doc The URI where the created item is accessable.}
	 * @param hierarchyNode the node to be marked
	 * @return
	 */
	@POST
	@Path("mark")
	public Response markMenuNode(
			@FormParam(value="hierarchyNode") UriRef hierarchyNode) {
		HierarchyNode node;
		try {
			node = hierarchyService.getHierarchyNode(hierarchyNode);
		} catch (NodeDoesNotExistException ex) {
			return getResourceNotExistingResponse(hierarchyNode);
		}
		markHierarchyNode(node);
		return Response.ok().build();
	}

	private Response getResourceNotExistingResponse(UriRef hierarchyNode) {
		return Response.status(Response.Status.CONFLICT).
					entity("The resource " + hierarchyNode.getUnicodeString()
					+ " does not exist.").type(MediaType.TEXT_PLAIN).
					build();
	}

	private void markHierarchyNode(HierarchyNode hierarchyNode) {
		hierarchyNode.addProperty(RDF.type, MENU.MenuNode);
		HierarchyNode parent = hierarchyNode.getParent();
		if (parent != null) {
			if (!parent.hasProperty(RDF.type, MENU.MenuNode)) {
				markHierarchyNode(parent);
			}
		}
	}

	/**
	 * Marks the specified hierarchy node as menu node. If the node is a
	 * Collection node, then all members and sub-members are recursively marked
	 * as menu nodes. The parents of the specified hierarchy node will also be
	 * marked recursively as menu nodes, if they are not already.
	 *
	 * @param hierarchyNode the node to be marked
	 * @return
	 */
	@POST
	@Path("markAll")
	public Response markAsMenuNodeInclusiveMembers(
			@FormParam(value="hierarchyNode") UriRef hierarchyNode) {
		HierarchyNode node;
		try {
			node = hierarchyService.getHierarchyNode(hierarchyNode);
		} catch (NodeDoesNotExistException ex) {
			return getResourceNotExistingResponse(hierarchyNode);
		}
		markHierarchyNodeInclusiveMembers(node);
		return Response.ok().build();
	}

	private void markHierarchyNodeInclusiveMembers(HierarchyNode hierarchyNode) {
		markHierarchyNode(hierarchyNode);
		if (hierarchyNode instanceof CollectionNode) {
			List<HierarchyNode> list = ((CollectionNode) hierarchyNode).getMembers();
			Iterator<HierarchyNode> members = list.iterator();
			while (members.hasNext()) {
				HierarchyNode member = members.next();
				markHierarchyNodeInclusiveMembers(member);
			}
		}
	}

	/**
	 * Removes the menu node mark from the specified hierarchy node.
	 *
	 * @param hierarchyNode the node to be unmarked
	 * @return
	 */
	@POST
	@Path("unmark")
	public Response unmarkMenuNode(
			@FormParam(value="hierarchyNode") UriRef hierarchyNode) {
		HierarchyNode node;
		try {
			node = hierarchyService.getHierarchyNode(hierarchyNode);
		} catch (NodeDoesNotExistException ex) {
			return getResourceNotExistingResponse(hierarchyNode);
		}
		unmarkHierarchyNode(node);
		return Response.ok().build();
	}

	/**
	 * Removes the menu node mark from the specified hierarchy node. If the node
	 * is a Collection node, then all marks of the members and sub-members are
	 * recursively removed.
	 *
	 * @param hierarchyNode the node to be unmarked
	 * @return
	 */
	@POST
	@Path("unmarkAll")
	public Response unmarkMenuNodeInclusiveMembers(
			@FormParam(value="hierarchyNode") UriRef hierarchyNode) {
		HierarchyNode node;
		try {
			node = hierarchyService.getHierarchyNode(hierarchyNode);
		} catch (NodeDoesNotExistException ex) {
			return getResourceNotExistingResponse(hierarchyNode);
		}
		unmarkHierarchyNodeInclusiveMembers(node);
		return Response.ok().build();
	}

	private void unmarkHierarchyNode(HierarchyNode hierarchyNode) {
		hierarchyNode.deleteProperty(RDF.type, MENU.MenuNode);
	}

	private void unmarkHierarchyNodeInclusiveMembers(HierarchyNode hierarchyNode) {
		unmarkHierarchyNode(hierarchyNode);
		if (hierarchyNode instanceof CollectionNode) {
			List<HierarchyNode> list = ((CollectionNode) hierarchyNode).getMembers();
			Iterator<HierarchyNode> members = list.iterator();
			while (members.hasNext()) {
				HierarchyNode member = members.next();
				unmarkHierarchyNodeInclusiveMembers(member);
			}
		}
	}
}
