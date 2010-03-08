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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.HIERARCHY;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.utils.UriException;

/**
 * This class represents a collection in a hierarchy. A collection can have
 * one or more <code>HierarchyNode</code> as members.
 *
 * @author mir
 */
public class CollectionNode extends HierarchyNode {

	private HierarchyService hierarchyService;

	CollectionNode(UriRef collectionNode, TripleCollection graph,
			HierarchyService hierarchyService) throws UriException {
		super(collectionNode, graph, hierarchyService);
		this.hierarchyService = hierarchyService;
	}

	boolean isValid() {
		return this.hasProperty(RDF.type, HIERARCHY.Collection);
	}

	/**
	 * Adds the specified hierarchy node to this collection nodes members list
	 * @param node the hierarchy node that is added this collection
	 * @param pos the position of hierarchy node in the memebers list of this
	 *		collection
	 */
	void addMember(HierarchyNode node, int pos) {
		List<Resource> membersList = this.getMembersRdf();
		membersList.add(pos, node.getNode());
	}

	/**
	 * Removes the specified hierarchy node from this collection nodes members list
	 * @param node the hierarchy node to be removed
	 * @return true if the members list changed.
	 */
	boolean removeMember(HierarchyNode node) {
		List<Resource> membersList = this.getMembersRdf();
		return membersList.remove(node.getNode());
	}

	/**
	 * returns an unmodifiable list containing all member <code>HierarchyNode</code>S
	 * of this collection node.
	 * @return
	 */
	public List<HierarchyNode> getMembers() {
		List<Resource> membersListRdf = getMembersRdf();
		List<HierarchyNode> nodes =
				new ArrayList<HierarchyNode>(membersListRdf.size());
		Iterator<Resource> membersIter = membersListRdf.iterator();
		while (membersIter.hasNext()) {
			UriRef uri = (UriRef) membersIter.next();
			try {
				nodes.add(hierarchyService.getHierarchyNode(uri));
			} catch (NodeDoesNotExistException ex) {
				throw new RuntimeException(ex);
			} catch (UnknownRootExcetpion ex) {
				throw new RuntimeException(ex);
			}
		}
		return Collections.unmodifiableList(nodes);
	}
	
	/**
	 * returns the list containing the URIs of all members of this collection node.
	 * @return
	 */
	List<Resource> getMembersRdf() {
		Iterator<Resource> members = this.getObjects(HIERARCHY.members);
		RdfList membersList;
		if (members.hasNext()) {
			membersList = new RdfList((BNode)members.next(), getGraph());
		} else {
			BNode newMembers = new BNode();
			this.addProperty(HIERARCHY.members, newMembers);
			membersList = new RdfList(newMembers, getGraph());
		}
		return membersList;
	}

	@Override
	public CollectionNode replaceWith(NonLiteral replacement) {
		if(!(replacement instanceof UriRef)) {
			throw new IllegalArgumentException("Hierarchy node has to be an UriRef");
		}
		UriRef newUri = (UriRef) replacement;
		HierarchyUtils.ensureCollectionUri(newUri);
		super.replaceWith(newUri);
		CollectionNode movedNode;
		try {
			movedNode = new CollectionNode(newUri, getGraph(), hierarchyService);
		} catch (UriException ex) {
			throw new IllegalArgumentException(ex);
		}
		movedNode.updateMembers();
		return movedNode;
	}

	private void updateMembers() {
		List<Resource> membersList = getMembersRdf();
		Iterator<Resource> membersIter = membersList.iterator();
		while (membersIter.hasNext()) {
			UriRef memberUri = (UriRef) membersIter.next();
			updateMember(memberUri);
		}
	}

	@Override
	public HierarchyNode move(CollectionNode newParentCollection, int pos) throws NodeAlreadyExistsException,
			IllegalMoveException {
		if (newParentCollection.isSubcollectionOf(this)) {
			throw new IllegalMoveException("Collection can not be moved into itself or a subcollection of itself");
		}
		return super.move(newParentCollection, pos);
	}

	private void updateMember(UriRef memberUri) {
		try {
			CollectionNode memberCollection = new CollectionNode(memberUri,
					getGraph(), hierarchyService);
			if (memberCollection.isValid()) {
				UriRef newUri = new UriRef(getNode().getUnicodeString()
						+ memberCollection.getName() + "/");
				memberCollection.replaceWith(newUri);
				memberCollection.updateMembers();
			} else {
				HierarchyNode memberResource = new HierarchyNode(memberUri,
						getGraph(), hierarchyService);
				UriRef newUri = new UriRef(getNode().getUnicodeString()
						+ memberResource.getName());
				memberResource.replaceWith(newUri);
			}
		} catch (UriException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void delete() {
		List<HierarchyNode> members = getMembers();
		Iterator<HierarchyNode> membersIter = members.iterator();
		while (membersIter.hasNext()) {
			HierarchyNode hierarchyNode = membersIter.next();
			hierarchyNode.delete();
		}
		deleteCollection();
		super.delete();
		if (hierarchyService.getRoots().contains(this)) {
			hierarchyService.removeRoot(this);
		}
	}

	private void deleteCollection() {
		deleteProperty(RDF.type, HIERARCHY.Collection);
		deleteProperties(HIERARCHY.members);
	}

	private boolean isSubcollectionOf(CollectionNode collection) {
		return getNode().getUnicodeString().startsWith(
				collection.getNode().getUnicodeString());
				
	}

}
