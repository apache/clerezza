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
package org.apache.clerezza.platform.content.hierarchy;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.HIERARCHY;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;


/**
 * The hierarchy manager provides methods for managing the hierarchy in the
 * content graph. This is a JAX-RS resource with the path
 * "/tools/hierarchy-manager".
 *
 * @author mir
 */
@Component
@Service(value = Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/tools/hierarchy-manager")
public class HierarchyManager {

	@Reference
	private ContentGraphProvider cgProvider;

	@Reference
	private HierarchyService hierarchyService;

	/**
	 * Creates a new resource as a member of the specified parent collection.
	 * It will be on the specified position in the members list of the parent,
	 * if no position is specified, then it is appended to the end of the list.
	 * The new resource will have the specified name.
	 * This resource method has the path "createResource"
	 * 
	 * @param parentCollectionUri The uri of the parent collection.
	 * @param pos The position in the parents members list
	 * @param name the name of the resource
	 * @return if sucessfully created then a Created (201) HTTP Response is returned.
	 */
	@POST
	@Path("createResource")
	public Response createResource(
			@FormParam(value = "parentCollectionUri") UriRef parentCollectionUri,
			@FormParam(value = "pos") Integer pos,
			@FormParam(value = "name") String name) {
		HierarchyNode node = null;
		try {			
			if (pos == null) {
				node = hierarchyService.
						createNonCollectionNode(parentCollectionUri, name);
			} else {
				node = hierarchyService.
						createNonCollectionNode(parentCollectionUri, name, pos);
			}
		} catch (NodeAlreadyExistsException ex) {
			return Response.status(Response.Status.CONFLICT).entity(ex.toString()).
					type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.toString()).
					type(MediaType.TEXT_PLAIN_TYPE).build();
		}
		return Response.created(URI.create(node.getNode().getUnicodeString())).build();
	}

	/**
	 * Creates a new collection as a member of the specified parent collection.
	 * It will be on the specified position in the members list of the parent,
	 * if no position is specified, then it is appended to the end of the list.
	 * The new collection will have the specified name.
	 * This resource method has the path "createCollection"
	 *
	 * @param parentCollectionUri The uri of the parent collection.
	 * @param pos The position in the parents members list
	 * @param name the name of the collection
	 * @return if sucessfully created then a Created (201) HTTP Response is returned.
	 *		Otherwise a Conflict (409) HTTP Response is returned.
	 */
	@POST
	@Path("createCollection")
	public Response createCollection(
			@FormParam(value = "parentCollectionUri") UriRef parentCollectionUri,
			@FormParam(value = "pos") Integer pos,
			@FormParam(value = "name") String name) {
		CollectionNode node = null;
		try {
			
			if (pos == null) {
				node = hierarchyService.createCollectionNode(parentCollectionUri, name);
			} else {
				node = hierarchyService.createCollectionNode(parentCollectionUri, name, pos);
			}
		} catch (NodeAlreadyExistsException e) {
			return Response.status(Response.Status.CONFLICT).entity(e.toString()).
					type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e.toString()).
					type(MediaType.TEXT_PLAIN_TYPE).build();
		}
		return Response.created(URI.create(node.getNode().getUnicodeString())).build();
	}

	/**
	 * Deletes the resource/collection of the specified URI.
	 * This resource method has the path "delete"
	 *
	 * @param nodeUri
	 * @return if the node was successfully deleted, then a No Content (204)
	 *		HTTP Response is returned. If the node does not exist,
	 *		then a Not Found (404) HTTP Response is returned.
	 */
	@POST
	@Path("delete")
	public Response delete(@FormParam(value = "nodeUri") UriRef nodeUri) {
		HierarchyNode hierarchyNode;
		try {
			hierarchyNode = hierarchyService.getHierarchyNode(nodeUri);
		} catch (NodeDoesNotExistException ex) {
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (UnknownRootExcetpion ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.toString()).
					type(MediaType.TEXT_PLAIN_TYPE).build();
		}

		hierarchyNode.delete();
		return Response.noContent().build();		
	}

	/**
	 * Moves the node into the specified collection as the member at
	 * the specified position pos. 
	 * This resource method has the path "move"
	 *
	 * @param oldPos
	 * @param newPos
	 * @param newName The new name of the moved node. This parameter is optionally.
	 * @return if sucessfully moved then a Created (201) HTTP Response is returned.
	 *		if the specified targetCollection is not a collection then a
	 *		Conflict (409) HTTP Response is returned. If the node to be moved or
	 *		the target collection does not exist, then a Not Found (404) HTTP
	 *		Response is returned.
	 */
	@POST
	@Path("move")
	public Response move(@FormParam(value = "nodeUri") UriRef nodeUri,
			@FormParam(value = "targetCollection") UriRef targetCollection,
			@FormParam(value = "pos") Integer newPos,
			@FormParam(value = "newName") String newName) {
		HierarchyNode hierarchyNode;
		CollectionNode targetCollectionNode;
		try {
			hierarchyNode = hierarchyService.getHierarchyNode(nodeUri);
			targetCollectionNode = hierarchyService.getCollectionNode(targetCollection);
		} catch (NodeDoesNotExistException ex) {
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (IllegalArgumentException e) {
			return Response.status(Response.Status.CONFLICT).entity(
					targetCollection.getUnicodeString() + " is not a Collection.").
					type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (UnknownRootExcetpion ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.toString()).
					type(MediaType.TEXT_PLAIN_TYPE).build();
		}
		try {
			hierarchyNode = hierarchyNode.move(targetCollectionNode, newName, newPos);
		} catch (NodeAlreadyExistsException ex) {
			return Response.status(Response.Status.CONFLICT).entity(
					nodeUri.getUnicodeString() + " already exists in " +
					"collection.").
					type(MediaType.TEXT_PLAIN_TYPE).build();
		} catch (IllegalMoveException ex) {
			return Response.status(Response.Status.CONFLICT).entity(ex.getMessage()).
					type(MediaType.TEXT_PLAIN_TYPE).build();
		}
		return Response.created(
				URI.create(hierarchyNode.getNode().getUnicodeString())).build();
	}
	
	/**
	 * Renames the specified node to the given name. E.g. if you rename 
	 * "http://localhost:8080/foo/bar" to "test" the new URI will be 
	 * "http://localhost:8080/foo/test".
	 * This resource method has the path "rename".
	 * 
	 * @param newName The new name of the moved node.
	 * @return if sucessfully renamed then a Created (201) HTTP Response is returned.
	 *		If the node does not exist, then a Not Found (404) HTTP Response is
	 *		returned.
	 */
	@POST
	@Path("rename")
	public Response rename(@FormParam(value = "nodeUri") UriRef nodeUri,
			@FormParam(value = "newName") String newName) {
		try {
			HierarchyNode hierarchyNode = hierarchyService.getHierarchyNode(nodeUri);
			CollectionNode parent = hierarchyNode.getParent();
			int pos = parent.getMembers().indexOf(hierarchyNode);
			return move(nodeUri, parent.getNode(), pos, newName);
		} catch (NodeDoesNotExistException ex) {
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (UnknownRootExcetpion ex) {
			return Response.status(Response.Status.BAD_REQUEST).entity(ex.toString()).
					type(MediaType.TEXT_PLAIN_TYPE).build();
		}
		
	}

	/**
	 * Returns a graph containng the graph context of the specified collection
	 * node URI and the RDF type and the labels of all members of the collection.
	 * If the specified hierarchy node is not collection node then a
	 * WebApplicationException containing a Bad Request (400) HTTP Response is
	 * thrown. The returned graph is enriched with HIERARCHY:membersNumber for
	 * the contained collections.
	 * This resource method has the path "getCollection"
	 *
	 * @param node a collection node
	 * @return a graph
	 */
	@GET
	@Path("getCollection")
	public TripleCollection getCollectionGraph(
			@QueryParam(value = "collectionNode") UriRef node) {
		CollectionNode collectionNode;
		try {
			collectionNode = hierarchyService.getCollectionNode(node);
		} catch (NodeDoesNotExistException ex) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST).build());
		} catch (UnknownRootExcetpion ex) {
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
					.entity(ex.toString()).type(MediaType.TEXT_PLAIN_TYPE).build());
		}
		MGraph result = new SimpleMGraph();
		result.addAll(collectionNode.getNodeContext());
		List<HierarchyNode> memberList = collectionNode.getMembers();
		result.add(new TripleImpl(collectionNode.getNode(),
				HIERARCHY.membersNumber,
				LiteralFactory.getInstance().createTypedLiteral(
				new Integer(memberList.size()))));
		Iterator<HierarchyNode> members = memberList.iterator();
		while (members.hasNext()) {
			HierarchyNode memberNode = members.next();
			Iterator<Resource> types = memberNode.getObjects(RDF.type);
			while (types.hasNext()) {
				Resource type = types.next();
				result.add(new TripleImpl(memberNode.getNode(), RDF.type, type));
			}
			Iterator<Resource> labels = memberNode.getObjects(RDFS.label);
			while (labels.hasNext()) {
				Resource label = labels.next();
				result.add(new TripleImpl(memberNode.getNode(), RDFS.label, label));
			}
			if (memberNode instanceof CollectionNode) {
				CollectionNode memberCollection = (CollectionNode) memberNode;
				result.add(new TripleImpl(memberNode.getNode(),
						HIERARCHY.membersNumber,
						LiteralFactory.getInstance().createTypedLiteral(
						Integer.valueOf(memberCollection.getMembers().size()))));
			}
		}
		return result;
	}

	/**
	 * Returns a count of all members and sub-members of the specified
	 * <code>collectionNode</code>.
	 * @param collectionNode the collection node whose members count is returned.
	 * @return 
	 */
	@GET
	@Path("getMembersCount")
	@Produces("text/plain")
	public String getCount(
			@QueryParam(value="collectionNode") UriRef collectionNode) {
		CollectionNode collection = null;
		try {
			collection = hierarchyService.getCollectionNode(collectionNode);
		} catch (NodeDoesNotExistException ex) {
			throw new WebApplicationException(
					Response.status(Response.Status.NOT_FOUND).build());
		} catch (UnknownRootExcetpion ex) {
			throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
					.entity(ex.toString()).type(MediaType.TEXT_PLAIN_TYPE).build());
		}
		return getMembersCount(collection).toString();
	}

	private Integer getMembersCount(CollectionNode collection) {
		Integer count = 0;
		Iterator<HierarchyNode> membersIter = collection.getMembers().iterator();
		while (membersIter.hasNext()) {
			HierarchyNode hierarchyNode = membersIter.next();
			count ++;
			if (hierarchyNode instanceof CollectionNode) {
				count += getMembersCount((CollectionNode) hierarchyNode);
			}
		}
		return count;
	}
}
