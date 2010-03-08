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
import org.apache.clerezza.rdf.utils.EncodedUriRef;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.utils.UriException;
import org.apache.clerezza.utils.UriUtil;

/**
 * This class represents a node in a hierarchy and provides methods for
 * managing the node.
 *
 * @author mir
 */
public class HierarchyNode extends GraphNode {

	private HierarchyService hierarchyService;

	HierarchyNode(UriRef hierarchyNode, TripleCollection graph,
			HierarchyService hierarchyService) throws UriException {
		super(new EncodedUriRef(hierarchyNode), graph);
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
			return hierarchyService.getCollectionNode(parentCollectionUri);
		} catch (UnknownRootExcetpion ex) {
			throw new RuntimeException(ex);
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
		return this.move(newParentCollection, null, pos);
	}
		
	/**
	 * Moves this node into the specified parent collection at the specified
	 * position pos. Optionally you can specifiy a new name.
	 *
	 * @param newParentCollection the collection into which this node should be moved
	 * @param newName the new name of the moved node. Can be null.
	 * @param pos the member position of the moved node in the members list of
	 * the new parent collection.
	 * @throws NodeAlreadyExistsException is thrown if the new parent collection
	 *		already contains a hierarchy node with the same name.
	 * @throws IllegalMoveException is thrown if the move operation is not allowed
	 * @return the HierarchyNode at the new location
	 */
	public HierarchyNode move(CollectionNode newParentCollection, String newName, int pos)
			throws NodeAlreadyExistsException, IllegalMoveException {
		String name;
		try {
			name = newName == null ? getName() : UriUtil.encodePartlyEncodedPath(newName, "UTF-8");
		} catch (UriException ex) {
			throw new RuntimeException(ex);
		}
		if (newParentCollection.equals(getParent())) {
			UriRef nodeUri = getNode();
			List<Resource> membersRdfList = newParentCollection.getMembersRdf();
			int oldPos = membersRdfList.indexOf(nodeUri);			
			if (oldPos < pos) {
				pos -= 1;
			}			
			if (name.equals(getName())) {				
				if (oldPos != pos) {
					membersRdfList.remove(nodeUri);
					membersRdfList.add(pos, nodeUri);
				}				
				return this;
			}
		}		
		String newUriString = newParentCollection.getNode().getUnicodeString() +
				name;
		String alternativeUriString = newUriString;
		if (this instanceof CollectionNode) {
			newUriString += "/";
		} else {
			alternativeUriString += "/";
		}
		UriRef newUri = new UriRef(newUriString);
		UriRef alternativeUri = new UriRef(alternativeUriString);
		List<Resource> parentMembers = newParentCollection.getMembersRdf();
		if (parentMembers.contains(newUri) || parentMembers.contains(alternativeUri)) {
			HierarchyNode existingNode = null;
			try {
				try {
					existingNode = hierarchyService.getHierarchyNode(newUri);
				} catch (NodeDoesNotExistException ex) {
					try {
						existingNode = hierarchyService.getHierarchyNode(alternativeUri);
					} catch (NodeDoesNotExistException e) {
						throw new RuntimeException(ex);
					}
				}
			} catch (UnknownRootExcetpion ex) {
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
		return HierarchyUtils.getName(this.getNode());
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
		try {
			return new HierarchyNode(newUri, getGraph(), hierarchyService);
		} catch (UriException ex) {
			throw new IllegalArgumentException(ex);
		}
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
