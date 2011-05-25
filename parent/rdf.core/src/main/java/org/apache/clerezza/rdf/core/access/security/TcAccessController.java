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
package org.apache.clerezza.rdf.core.access.security;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.utils.security.PermissionParser;

/**
 * Controls the permissions needed to access a triple collection provided by
 * <code>TcManager</code>.
 *
 * Clients with a ConfigureTcAcessPermission can set the permissions required to
 * access a TripleCollection. These permissions are stored persistently in an
 * MGraph named urn:x-localinstance:/graph-access.graph
 *
 * @author reto
 */
public class TcAccessController {

	private final TcManager tcManager;
	private final UriRef permissionGraphName = new UriRef("urn:x-localinstance:/graph-access.graph");
	//we can't rely on ontology plugin in rdf core
	private String ontologyNamespace = "http://clerezza.apache.org/2010/07/10/graphpermssions#";
	private final UriRef readPermissionListProperty = new UriRef(ontologyNamespace + "readPermissionList");
	private final UriRef readWritePermissionListProperty = new UriRef(ontologyNamespace + "readWritePermissionList");
	/**
	 * The first item in the subject RDF list.
	 */
	public static final UriRef first = new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");
	/**
	 * The rest of the subject RDF list after the first item.
	 */
	public static final UriRef rest = new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");
	public static final UriRef rdfNil = new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
	private final Map<UriRef, Collection<Permission>> readPermissionCache =
			Collections.synchronizedMap(new HashMap<UriRef, Collection<Permission>>());
	private final Map<UriRef, Collection<Permission>> readWritePermissionCache =
			Collections.synchronizedMap(new HashMap<UriRef, Collection<Permission>>());

	/**
	 *
	 * @param tcManager the tcManager used to locate urn:x-localinstance:/graph-access.graph
	 */
	public TcAccessController(TcManager tcManager) {
		this.tcManager = tcManager;
	}

	public void checkReadPermission(UriRef tripleCollectionUri) {
		if (tripleCollectionUri.equals(permissionGraphName)) {
			//This is world readable, as this prevents as from doingf things as
			//priviledged during verfification
			return;
		}
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			//will AllPermissions the rest is obsolete
			try {
				AccessController.checkPermission(new AllPermission());
			} catch (AccessControlException e) {
				Collection<Permission> perms = getRequiredReadPermissions(tripleCollectionUri);
				if (perms.size() > 0) {
					for (Permission permission : perms) {
						AccessController.checkPermission(permission);
					}
				} else {
					AccessController.checkPermission(new TcPermission(
							tripleCollectionUri.getUnicodeString(), TcPermission.READ));
				}
			}
		}
	}

	public void checkReadWritePermission(UriRef tripleCollectionUri) {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			//will AllPermissions the rest is obsolete
			try {
				AccessController.checkPermission(new AllPermission());
			} catch (AccessControlException e) {
				if (tripleCollectionUri.equals(permissionGraphName)) {
					AccessController.checkPermission(new TcPermission(
							tripleCollectionUri.getUnicodeString(), TcPermission.READWRITE));
				} else {
					Collection<Permission> perms = getRequiredReadWritePermissions(tripleCollectionUri);
					if (perms.size() > 0) {
						for (Permission permission : perms) {
							AccessController.checkPermission(permission);
						}
					} else {
						AccessController.checkPermission(new TcPermission(
								tripleCollectionUri.getUnicodeString(), TcPermission.READWRITE));
					}
				}
			}
		}
	}

	/**
	 * Set the set of permissions required for read access to a triple-collection, if
	 * the set is non-empty the default TCPermisson is no longer required.
	 *
	 * @param tripleCollectionUri
	 * @param permissionDescriptions
	 */
	public void setRequiredReadPermissionStrings(UriRef tripleCollectionUri,
			Collection<String> permissionDescriptions) {
		readPermissionCache.remove(tripleCollectionUri);
		final LockableMGraph permissionMGraph = getOrCreatePermisionGraph();
		Lock l = permissionMGraph.getLock().writeLock();
		l.lock();
		try {
			removeExistingRequiredReadPermissions(tripleCollectionUri, permissionMGraph);
			final NonLiteral permissionList = createList(permissionDescriptions.iterator(), permissionMGraph);
			permissionMGraph.add(new TripleImpl(tripleCollectionUri,
					readPermissionListProperty, permissionList));
		} finally {
			l.unlock();
		}
	}

	/**
	 * Set the set of permissions required for read access to a triple-collection, if
	 * the set is non-empty the default TCPermisson is no longer required.
	 *
	 * @param tripleCollectionUri
	 * @param permissionDescriptions
	 */
	public void setRequiredReadPermissions(UriRef tripleCollectionUri,
			Collection<Permission> permissions) {
		Collection<String> permissionStrings = new ArrayList<String>();
		for (Permission permission : permissions) {
			permissionStrings.add(permission.toString());
		}
		setRequiredReadPermissionStrings(tripleCollectionUri, permissionStrings);
	}

	/**
	 * Set the set of permissions required for read-write access to a
	 * triple-collection, if
	 * the set is non-empty the default TCPermisson is no longer required.
	 *
	 * @param tripleCollectionUri
	 * @param permissionDescriptions
	 */
	public void setRequiredReadWritePermissionStrings(UriRef tripleCollectionUri,
			Collection<String> permissionDescriptions) {
		readWritePermissionCache.remove(tripleCollectionUri);
		final LockableMGraph permissionMGraph = getOrCreatePermisionGraph();
		Lock l = permissionMGraph.getLock().writeLock();
		l.lock();
		try {
			removeExistingRequiredReadPermissions(tripleCollectionUri, permissionMGraph);
			final NonLiteral permissionList = createList(permissionDescriptions.iterator(), permissionMGraph);
			permissionMGraph.add(new TripleImpl(tripleCollectionUri,
					readWritePermissionListProperty, permissionList));
		} finally {
			l.unlock();
		}
	}

	/**
	 * Set the set of permissions required for read-write access to a
	 * triple-collection, if
	 * the set is non-empty the default TCPermisson is no longer required.
	 *
	 * @param tripleCollectionUri
	 * @param permissionDescriptions
	 */
	public void setRequiredReadWritePermissions(UriRef tripleCollectionUri,
			Collection<Permission> permissions) {
		Collection<String> permissionStrings = new ArrayList<String>();
		for (Permission permission : permissions) {
			permissionStrings.add(permission.toString());
		}
		setRequiredReadWritePermissionStrings(tripleCollectionUri, permissionStrings);
	}

	/**
	 * Get the set of permissions required for read access to the
	 * triple-collection, the set may be empty meaning that the default
	 * TCPermission is required.
	 *
	 * @param tripleCollectionUri
	 * @return the collection of permissions
	 */
	public Collection<Permission> getRequiredReadPermissions(UriRef tripleCollectionUri) {
		Collection<Permission> result = readPermissionCache.get(tripleCollectionUri);
		if (result == null) {
			result = new ArrayList<Permission>();
			Collection<String> permissionStrings = getRequiredReadPermissionStrings(tripleCollectionUri);
			for (String string : permissionStrings) {
				result.add(PermissionParser.getPermission(string, getClass().getClassLoader()));
			}
			readPermissionCache.put(tripleCollectionUri, result);
		}
		return result;
	}

	/**
	 * Get the set of permissions required for read-write access to the
	 * triple-collection, the set may be empty meaning that the default
	 * TCPermission is required.
	 *
	 * @param tripleCollectionUri
	 * @return the collection of permissions
	 */
	public Collection<Permission> getRequiredReadWritePermissions(UriRef tripleCollectionUri) {
		Collection<Permission> result = readWritePermissionCache.get(tripleCollectionUri);
		if (result == null) {
			result = new ArrayList<Permission>();
			Collection<String> permissionStrings = getRequiredReadWritePermissionStrings(tripleCollectionUri);
			for (String string : permissionStrings) {
				result.add(PermissionParser.getPermission(string, getClass().getClassLoader()));
			}
			readWritePermissionCache.put(tripleCollectionUri, result);
		}
		return result;
	}

	private NonLiteral createList(Iterator<String> iterator, LockableMGraph permissionMGraph) {
		if (!iterator.hasNext()) {
			return rdfNil;
		}
		final BNode result = new BNode();
		permissionMGraph.add(new TripleImpl(result, first,
				LiteralFactory.getInstance().createTypedLiteral(iterator.next())));
		permissionMGraph.add(new TripleImpl(result, rest,
				createList(iterator, permissionMGraph)));
		return result;

	}

	//called withiong write-lock
	private void removeExistingRequiredReadPermissions(UriRef tripleCollectionUri,
			LockableMGraph permissionMGraph) {
		try {
			Triple t = permissionMGraph.filter(tripleCollectionUri, readPermissionListProperty, null).next();
			Resource list = t.getObject();
			removeList((NonLiteral) list, permissionMGraph);
			permissionMGraph.remove(t);
		} catch (NoSuchElementException e) {
			//There was no existing to remove
		}
	}

	private void removeList(NonLiteral list, LockableMGraph permissionMGraph) {
		try {
			Triple t = permissionMGraph.filter(list, rest, null).next();
			Resource restList = t.getObject();
			removeList((NonLiteral) restList, permissionMGraph);
			permissionMGraph.remove(t);
			Iterator<Triple> iter = permissionMGraph.filter(list, first, null);
			iter.next();
			iter.remove();
		} catch (NoSuchElementException e) {
			//if it has no rest its rdf:NIL and has no first
		}
	}

	private Collection<String> getRequiredReadWritePermissionStrings(final UriRef tripleCollectionUri) {
		return getRequiredPermissionStrings(tripleCollectionUri, readWritePermissionListProperty);
	}
	private Collection<String> getRequiredReadPermissionStrings(final UriRef tripleCollectionUri) {
		return getRequiredPermissionStrings(tripleCollectionUri, readPermissionListProperty);
	}
	private Collection<String> getRequiredPermissionStrings(final UriRef tripleCollectionUri, UriRef property) {
		try {
			final LockableMGraph permissionMGraph = tcManager.getMGraph(permissionGraphName);
			Lock l = permissionMGraph.getLock().readLock();
			l.lock();
			try {
				Triple t = permissionMGraph.filter(tripleCollectionUri, property, null).next();
				NonLiteral list = (NonLiteral) t.getObject();
				LinkedList<String> result = new LinkedList<String>();
				readList(list, permissionMGraph, result);
				return result;
			} catch (NoSuchElementException e) {
				return new ArrayList<String>(0);
			} finally {
				l.unlock();
			}
		} catch (NoSuchEntityException e) {
			return new ArrayList<String>(0);
		}
	}

	private void readList(NonLiteral list, LockableMGraph permissionMGraph, LinkedList<String> target) {
		if (list.equals(rdfNil)) {
			return;
		}
		Triple restTriple = permissionMGraph.filter(list, rest, null).next();
		NonLiteral restList = (NonLiteral) restTriple.getObject();
		readList(restList, permissionMGraph, target);
		Triple firstTriple = permissionMGraph.filter(list, first, null).next();
		TypedLiteral firstValue = (TypedLiteral) firstTriple.getObject();
		String value = LiteralFactory.getInstance().createObject(String.class, firstValue);
		target.addFirst(value);
	}

	private LockableMGraph getOrCreatePermisionGraph() {
		try {
			return tcManager.getMGraph(permissionGraphName);
		} catch (NoSuchEntityException e) {
			return tcManager.createMGraph(permissionGraphName);
		}
	}
}
