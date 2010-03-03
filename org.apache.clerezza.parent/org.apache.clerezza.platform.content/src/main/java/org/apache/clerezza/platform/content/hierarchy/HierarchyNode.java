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

import java.util.List;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * This class represents a node in a hierarchy and provides methods for
 * managing the node.
 *
 * @author mir
 */
public class HierarchyNode extends GraphNode {

	private HierarchyService hierarchyService;

	HierarchyNode(UriRef hierarchyNode, TripleCollection graph,
			HierarchyService hierarchyService) {
		super(hierarchyNode, graph);
		this.hierarchyService = hierarchyService;
	}

	/**
	 * Deletes the node from the hierarchy.
	 * @return Returns true if the node was successfully deleted.
	 */
	public void delete()  {
		deleteFromParent();
		hierarchyService.deleteCreationProperties(this);
	}

	/**
	 * Deletes this HierarchyNode from the members list of the parent collection.
	 * @return true, if the member of the parent collection changed.
	 */
	private void deleteFromParent() {
		CollectionNode parentCollection = getParent();
		if (parentCollection != null) {
			parentCollection.removeMember(this);
		}
	}

	/**
	 * Returns the CollectionNode which has this HierarchyNode as member.
	 * @return
	 */
	public CollectionNode getParent() {
		if (hierarchyService.getRoots().contains(this)) {
			return null;
		}
		try {
			UriRef parentCollectionUri = HierarchyUtils
					.extractParentCollectionUri(getNode());
			return hierarchyService.getCollectionNodeWithEncodedUri(parentCollectionUri);
		} catch (NodeDoesNotExistException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Moves this node into the specified parent collection at the specified
	 * position pos.
	 * @param newParentCollection the collection into which this node should be moved
	 * @param pos the member position of the moved node in the members list of
	 * the new parent collection.
	 * @throws NodeAlreadyExistsException is thrown if the new parent collection
	 *		already contains a hierarchy node with the same name.
	 * @throws IllegalMoveException is thrown if the move operation is not allowed
	 * @return the HierarchyNode at the new location
	 */
	public HierarchyNode move(CollectionNode newParentCollection, int pos) 
			throws NodeAlreadyExistsException, IllegalMoveException {
		
		if (newParentCollection.equals(getParent())) {
			UriRef nodeUri = getNode();
			List<Resource> membersRdfList = newParentCollection.getMembersRdf();
			int oldPos = membersRdfList.indexOf(nodeUri);			
			if (oldPos < pos) {
				pos -= 1;
			}
			if (oldPos == pos) {
				return this;
			}			
			membersRdfList.remove(nodeUri);
			membersRdfList.add(pos, nodeUri);
			return this;
		}
		String newName = newParentCollection.getNode().getUnicodeString() +
				getName();		
		if (this instanceof CollectionNode) {
			newName += "/";
		}
		UriRef newUri = new UriRef(newName);	
		if (newParentCollection.getMembersRdf().contains(newUri)) {
			HierarchyNode existingNode = null;
			try {
				existingNode = hierarchyService.getHierarchyNodeWithEncodedUri(newUri);
			} catch (NodeDoesNotExistException ex) {
				throw new RuntimeException(ex);
			}
			throw new NodeAlreadyExistsException(existingNode);
		}
		deleteFromParent();

		HierarchyNode movedNode = replaceWith(newUri);
		newParentCollection.addMember(movedNode, pos);
		return movedNode;
	}

	/**
	 * Returns the name of the hierarchy node. E.g. the name of
	 * "http://example.org/folder1/" is "folder1".
	 *
	 * @return the name of the hierarchy node.
	 */
	public String getName() {
		String uri = getNode().getUnicodeString();
		if (uri.endsWith("/")) {
			uri = uri.substring(0, uri.length() - 1);
		}
		return uri.substring(uri.lastIndexOf("/") + 1, uri.length());
	}

	@Override
	public UriRef getNode() {
		return (UriRef) super.getNode();
	}

	@Override
	public HierarchyNode replaceWith(NonLiteral replacement) {
		if(!(replacement instanceof UriRef)) {
			throw new IllegalArgumentException("Hierarchy node has to be an UriRef");
		}
		UriRef newUri = (UriRef) replacement;
		super.replaceWith(newUri);
		return new HierarchyNode(newUri, getGraph(), hierarchyService);
	}

	/**
	 *
	 * @param obj
	 * @return true if obj is an instance of the same class represening the same
	 * node
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj.getClass().equals(getClass()))) {
			return false;
		}
		HierarchyNode other = (HierarchyNode)obj;
		return getNode().equals(other.getNode());
	}

	@Override
	public int hashCode() {
		return 13 * getNode().hashCode();
	}	
}
