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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.security.UserUtil;
import org.apache.clerezza.platform.usermanager.UserManager;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.HIERARCHY;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * The hierarchy service is an OSGi service that provides methods for managing
 * the hierarchy in the content graph.
 * @author mir
 */
@Component
@Service(value=HierarchyService.class)
public class HierarchyService {

	@Reference
	ContentGraphProvider cgProvider;

	@Reference
	PlatformConfig config;

	@Reference(target = SystemConfig.SYSTEM_GRAPH_FILTER)
	MGraph systemGraph;

	@Reference
	private UserManager userManager;

	private Set<CollectionNode> roots = new HashSet<CollectionNode>();

	/**
	 * Returns the roots of all available hierarchies.
	 * @return
	 */
	public Set<CollectionNode> getRoots() {
		return roots;
	}

	/**
	 * Returns the appropriate subclass of HierarchyNode according to the
	 * specified resource.
	 * @throws NodeDoesNotExistException Thrown if the specified node does not 
	 *		exist.
	 * @param uri
	 */
	public HierarchyNode getHierarchyNode(UriRef uri)
			throws NodeDoesNotExistException{
		HierarchyNode hierarchyNode;
		try {
			hierarchyNode = 
					new CollectionNode(uri, cgProvider.getContentGraph(), this);
		} catch(IllegalArgumentException e) {
			hierarchyNode = 
					new HierarchyNode(uri, cgProvider.getContentGraph(), this);
		}
		checkExistence(hierarchyNode);
		return hierarchyNode;
	}

	private void checkExistence(HierarchyNode node) throws NodeDoesNotExistException {
		if (!getRoots().contains(node)) {
                        CollectionNode parent;
                        UriRef nodeUri = node.getNode();
                        try {
                            parent = node.getParent();
                        } catch(IllegalArgumentException ex){
                            throw new NodeDoesNotExistException(nodeUri);
                        }			
			if (!parent.getMembersRdf().contains(nodeUri)) {
				throw new NodeDoesNotExistException(nodeUri);
			}
		}
	}

	/**
	 * Returns the <code>CollectionNode</code> at the specified uri.
	 * @throws NodeDoesNotExistException Thrown if the specified node does not 
	 *		exist.
	 * @throws IllegalArgumentException Thrown if the node at the specified uri
	 *		is not a CollectionNode.
	 * @param uri
	 */
	public CollectionNode getCollectionNode(UriRef uri)
			throws NodeDoesNotExistException{

		CollectionNode collectionNode =
				new CollectionNode(uri, cgProvider.getContentGraph(), this);
		checkExistence(collectionNode);
		return collectionNode;
	}

	/**
	 * Creates a new {@link HierarchyNode} which is not a {@link CollectionNode}
	 * and adds it to its parent collections member list at the specified position
	 * posInParent.
	 * If the specified uri does not start with an existing root uri, then
	 * the base URI ('http://[host]/') of uri is added as root.
	 *
	 * @param uri the uri where the node should be created
	 * @param posInParent the position of the node in the members list of its parent
	 *		collection
	 * @throws NodeAlreadyExistsException Thrown if the specified node already
	 *		exists.
	 * @throws IllegalArgumentException Thrown if uri ends with a '/'
	 * @return the created hierarchy node.
	 */
	public HierarchyNode createNonCollectionNode(UriRef uri, int posInParent)
			throws NodeAlreadyExistsException {
		HierarchyUtils.ensureNonCollectionUri(uri);
		handleRootOfUri(uri);
		HierarchyNode hierarchyNode = new HierarchyNode(uri,
				cgProvider.getContentGraph(), this);
		addToParent(hierarchyNode, posInParent);
		addCreationProperties(hierarchyNode);
		return hierarchyNode;
	}

	/**
	 * Creates a new {@link HierarchyNode} which is not a {@link CollectionNode}
	 * and adds it to its parent collections member list at the specified position
	 * posInParent.
	 * If the specified uri does not start with an existing root uri, then
	 * the base URI ('http://[host]/') of uri is added as root.
	 *
	 * @param parentCollection the uri of parent collection
	 * @param name the name of non collection node
	 * @param posInParent the position of the non collection node in the
	 *		members list of its parent collection
	 * @throws NodeAlreadyExistsException Thrown if the specified node already
	 *		exists.
	 * @throws IllegalArgumentException Thrown if uri ends with a '/'
	 * @return the created non collection node.
	 */
	public HierarchyNode createNonCollectionNode(UriRef parentCollection, String name, int posInParent)
			throws NodeAlreadyExistsException {
		UriRef uri = createNonCollectionUri(parentCollection, name);
		return createNonCollectionNode(uri, posInParent);
	}

	/**
	 * Creates a new {@link HierarchyNode} which is not a {@link CollectionNode}
	 * and adds it at the end of its parent collections member list.
	 * If the specified uri does not start with an existing root uri, then
	 * the base URI ('http://[host]/') of uri is added as root.
	 *
	 * @param parentCollection the uri of parent collection
	 * @param name the name of non collection node
	 * @throws NodeAlreadyExistsException Thrown if the specified node already
	 *		exists.
	 * @throws IllegalArgumentException Thrown if uri ends with a '/'
	 * @return the created non collection node.
	 */
	public HierarchyNode createNonCollectionNode(UriRef parentCollection, String name)
			throws NodeAlreadyExistsException {
		UriRef uri = createNonCollectionUri(parentCollection, name);
		return createNonCollectionNode(uri);
	}

	/**
	 * Checks if the uri starts with one of the roots. If not, then it adds
	 * the base URI of uri as new root.
	 * @param uri The Uri to be checked.
	 */
	private void handleRootOfUri(UriRef uri) {
		Iterator<CollectionNode> rootsIter = roots.iterator();
		while (rootsIter.hasNext()) {
			CollectionNode root = rootsIter.next();
			UriRef rootUri = root.getNode();
			if (uri.getUnicodeString().startsWith(rootUri.getUnicodeString())) {
				return;
			}
		}
		UriRef baseUri = extractBaseUri(uri);
		systemGraph.add(new TripleImpl(PLATFORM.Instance, PLATFORM.baseUri, baseUri));
		addRoot(baseUri);
	}

	private UriRef extractBaseUri(UriRef uriRef) {
		try {
			URI uri = new URI(uriRef.getUnicodeString());
			if (uri.getHost() == null) {
				throw new IllegalArgumentException("Host name missing in " + uri);
			}
			String port = "";
			if (uri.getPort() != -1) {
				port = ":" + uri.getPort();
			}
			return new UriRef(uri.getScheme() + "://" + uri.getHost() + port + "/");
		} catch (URISyntaxException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Adds the hierarchy node as a member to its parent collection. If the
	 * parent collection does not exist then it will be created first. If
	 * pos == -1 then the node will be appended to the list.
	 * @param node The node that will be added to its parent collection.
	 * @param pos the member position of the node in the members list of the parent.
	 * @throws NodeAlreadyExistsException thrown if a node with same uri already
	 *		exists.
	 */
	private void addToParent(HierarchyNode node, int pos)
			throws NodeAlreadyExistsException {
		CollectionNode parentCollection;
		try {
			UriRef parentCollectionUri = HierarchyUtils.
					extractParentCollectionUri(node.getNode());
			parentCollection = (CollectionNode) getHierarchyNode(parentCollectionUri);
		} catch (NodeDoesNotExistException ex) {
			try {
				parentCollection = createCollectionNode(ex.getNodeUri());
			} catch (NodeAlreadyExistsException e) {
				throw new RuntimeException(e);
			}
		}
		if (pos == -1) {
			pos = parentCollection.getMembersRdf().size();
		}
		if (parentCollection.getMembers().contains(node)) {
			throw new NodeAlreadyExistsException(node);
		}
		parentCollection.addMember(node, pos);
	}

	/**
	 * Creates a new {@link HierarchyNode} which is not a {@link CollectionNode}
	 * and adds it at the end of its parent collections member list.
	 *
	 * @see #createNonCollectionNode(org.apache.clerezza.rdf.core.UriRef, int)
	 */
	public HierarchyNode createNonCollectionNode(UriRef uri)
			throws NodeAlreadyExistsException {
		return createNonCollectionNode(uri, -1);
	}

	/**
	 * Creates a new {@link CollectionNode} at the specified uri. If at the
	 * specified uri a collection node already exists, then a
	 * <code>NodeAlreadyExistsException</code> is thrown. The newly created
	 * collection node will be added to its parent collections member list
	 * at the specified position posInParent.
	 * If the specified uri does not start with an existing root uri, then
	 * the base URI ('http://[host]/') of uri is added as root.
	 *
	 * @param uri the uri where the collection should be created.
	 * @param posInParent the position of the collection in the memebers list of 
	 *		its parent collection.
	 * @throws NodeAlreadyExistsException Thrown if the specified node already
	 *		exists.
	 * @throws IllegalArgumentException Thrown if uri ends not with a '/'
	 * @return the collection node at the specified uri.
	 */
	public CollectionNode createCollectionNode(UriRef uri, int posInParent)
			throws NodeAlreadyExistsException {
		HierarchyUtils.ensureCollectionUri(uri);
		handleRootOfUri(uri);
		addCollectionTypeTriple(uri);
		CollectionNode collectionNode = new CollectionNode(uri,
				cgProvider.getContentGraph(), this);
		addToParent(collectionNode, posInParent);
		addCreationProperties(collectionNode);
		return collectionNode;
	}

	/**
	 * Creates a new {@link CollectionNode} in the specified parent collection with
	 * the specified name. If in the parent collection already contains a collection
	 * with that name, then a <code>NodeAlreadyExistsException</code> is thrown.
	 * The newly created collection node will be added to its parent collection's
	 * member list at the specified position posInParent. If the specified uri
	 * does not start with an existing root uri, then the base URI ('http://[host]/')
	 * of uri is added as root.
	 *
	 * @param parentCollection the uri of the parent collection.
	 * @param name the name of the collection
	 * @param posInParent the position of the collection in the members list of
	 *		its parent collection.
	 * @throws NodeAlreadyExistsException Thrown if the specified node already
	 *		exists.
	 * @throws IllegalArgumentException Thrown if uri ends not with a '/'
	 * @return the created collection node.
	 */
	public HierarchyNode createCollectionNode(UriRef parentCollection, String name, int posInParent)
			throws NodeAlreadyExistsException {
		UriRef uri = createCollectionUri(parentCollection, name);
		return createCollectionNode(uri, posInParent);
	}

	/**
	 * Creates a new {@link CollectionNode} in the specified parent collection with
	 * the specified name. If in the parent collection already contains a collection
	 * with that name, then a <code>NodeAlreadyExistsException</code> is thrown.
	 * The newly created collection node will be added at the end of its parent
	 * collection's member list. If the specified uri does not start with an
	 * existing root uri, then the base URI ('http://[host]/') of uri is added
	 * as root.
	 *
	 * @param parentCollection the uri of the parent collection.
	 * @param name the name of the collection
	 * @throws NodeAlreadyExistsException Thrown if the specified node already
	 *		exists.
	 * @throws IllegalArgumentException Thrown if uri ends not with a '/'
	 * @return the created collection node.
	 */
	public HierarchyNode createCollectionNode(UriRef parentCollection, String name)
			throws NodeAlreadyExistsException {
		UriRef uri = createCollectionUri(parentCollection, name);
		return createCollectionNode(uri);
	}

	private void addCollectionTypeTriple(UriRef uri) {
		Triple collectionTypeTriple = new TripleImpl(uri, RDF.type,
				HIERARCHY.Collection);
		cgProvider.getContentGraph().add(collectionTypeTriple);
	}

	/**
	 * Creates a new CollectionNode at the specified uri. If at the specified
	 * uri a collection node already exists, then a
	 * <code>NodeAlreadyExistsException</code> is thrown. The newly
	 * created collection node will be added at the end of its parent collection's
	 * member list.
	 *
	 * @param uri the uri where the collection should be created.
	 * @throws NodeAlreadyExistsException Thrown if the specified node already
	 *		exists.
	 * @return the collection node at the specified uri.
	 *
	 * @see #createCollectionNode(org.apache.clerezza.rdf.core.UriRef, int)
	 */
	public CollectionNode createCollectionNode(UriRef uri)
			throws NodeAlreadyExistsException {
		return createCollectionNode(uri, -1);
	}


	protected void activate(ComponentContext componentContext) {
		Iterator<UriRef> baseUris = config.getBaseUris().iterator();
		while (baseUris.hasNext()) {
			UriRef baseUri = baseUris.next();
			addRoot(baseUri);
		}
	}

	private void addRoot(UriRef baseUri) {
		addCollectionTypeTriple(baseUri);
		roots.add(new CollectionNode(baseUri, cgProvider.getContentGraph(), this));
	}

	protected void deactivate(ComponentContext componentContext) {
		roots.clear();
	}

	void removeRoot(CollectionNode root) {
		roots.remove(root);
		systemGraph.remove(new TripleImpl(PLATFORM.Instance, PLATFORM.baseUri, root.getNode()));
	}

	private void addCreationProperties(HierarchyNode node) {
		GraphNode agentNode = getCreator();
		if(!(node.getObjects(FOAF.maker).hasNext())) {

			Iterator<Triple> agents = node.getGraph().filter(null, PLATFORM.userName,
					agentNode.getObjects(PLATFORM.userName).next());

			NonLiteral agent = null;
			if(agents.hasNext()) {
				agent = (NonLiteral) agents.next().getSubject();
			} else {
				agent = (NonLiteral) agentNode.getNode();
			}
			node.addProperty(FOAF.maker, agent);
			node.getGraph().add(new TripleImpl(agent,
					PLATFORM.userName, agentNode.getObjects(
					PLATFORM.userName).next()));
		} 
		node.addProperty(DCTERMS.created,
				LiteralFactory.getInstance().createTypedLiteral(new Date()));
	}

	protected GraphNode getCreator() {
		return userManager.getUserGraphNode(UserUtil.getCurrentUserName());
	}

	void deleteCreationProperties(HierarchyNode node) {
		node.deleteProperties(FOAF.maker);
		node.deleteProperties(DCTERMS.created);
	}

	/**
	 * Creates a uri that ends with a slash ('/').
	 * @param parrentCollectionUri the URI of the parent collection
	 * @param name the name of the collection
	 * @return
	 */
	UriRef createCollectionUri(UriRef parrentCollectionUri, String name) {
		return new UriRef(
				createNonCollectionUri(parrentCollectionUri, name).getUnicodeString() + "/");
	}

	UriRef createNonCollectionUri(UriRef parentCollectionUri, String name) {
		try {
			return new UriRef(parentCollectionUri.getUnicodeString() +
					URLEncoder.encode(name, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}
}
