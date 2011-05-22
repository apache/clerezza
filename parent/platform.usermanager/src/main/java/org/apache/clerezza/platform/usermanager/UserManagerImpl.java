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
package org.apache.clerezza.platform.usermanager;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.platform.security.auth.WebIdPrincipal;
import org.apache.clerezza.platform.users.WebIdGraphsService;
import org.apache.clerezza.platform.users.WebIdInfo;
import org.apache.clerezza.rdf.core.*;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.SecuredMGraph;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.OSGI;
import org.apache.clerezza.rdf.ontologies.PERMISSION;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.SIOC;
import org.apache.clerezza.rdf.utils.GraphNode;

import javax.security.auth.Subject;

/**
 * @author hasan, tio
 */
@Component
@Service(value=UserManager.class)
public class UserManagerImpl implements UserManager {

	@Reference
	private ContentGraphProvider cgProvider;

	@Reference
	TcManager tcManager;

	@Reference
	WebIdGraphsService webIdGraphsService;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Reference(target = SystemConfig.SYSTEM_GRAPH_FILTER)
	private MGraph securedSystemGraph;

	private LockableMGraph getSystemGraph() {
		return ((SecuredMGraph) securedSystemGraph).getUnsecuredMGraph();
	}

	@Override
	public void storeRole(String title) {
		if (title == null) {
			return;
		}
		if (getRoleByTitle(title) != null) {
			return;
		}
		BNode role = new BNode();
		LockableMGraph systemGraph = getSystemGraph();
		Lock writeLock = systemGraph.getLock().writeLock();
		writeLock.lock();
		try {
			systemGraph.add(new TripleImpl(role, RDF.type, PERMISSION.Role));
			systemGraph.add(new TripleImpl(role, DC.title,
					new PlainLiteralImpl(title)));
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public NonLiteral getRoleByTitle(String title) {
		LockableMGraph systemGraph = getSystemGraph();
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			Iterator<Triple> triples = systemGraph.filter(null, DC.title,
					new PlainLiteralImpl(title));
			NonLiteral role = null;
			while (triples.hasNext()) {
				role = triples.next().getSubject();
				if (systemGraph.filter(role, RDF.type, PERMISSION.Role).hasNext()) {
					return role;
				}
			}
		} finally {
			readLock.unlock();
		}
		return null;
	}

	@Override
	public boolean roleExists(String title) {
		return getRoleByTitle(title) != null;
	}

	@Override
	public Iterator<NonLiteral> getRoles() {
		LockableMGraph systemGraph = getSystemGraph();
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			final Iterator<NonLiteral> allRolesIter = getResourcesOfType(PERMISSION.Role);
			final Set<NonLiteral> allRolesSet = new HashSet<NonLiteral>();
			while (allRolesIter.hasNext()) {
				allRolesSet.add(allRolesIter.next());
			}
			final Set<NonLiteral> nonBaseRolesSet = new HashSet<NonLiteral>();
			for (NonLiteral role : allRolesSet) {
				if (!systemGraph.filter(role, RDF.type, PERMISSION.BaseRole).hasNext()) {
					nonBaseRolesSet.add(role);
				}
			}
			return nonBaseRolesSet.iterator();
		} finally {
			readLock.unlock();
		}		
	}

	@Override
	public Iterator<NonLiteral> getRolesOfUser(NonLiteral user){
		LockableMGraph systemGraph = getSystemGraph();
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			final Iterator<Triple> triples = systemGraph.filter(user,SIOC.has_function, null);
			Set<NonLiteral> userRoles = new HashSet<NonLiteral>();
			while (triples.hasNext()) {
				userRoles.add((NonLiteral) triples.next().getObject());
			}
			return userRoles.iterator();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void deleteRole(String title) {
		if (title == null) {
			return;
		}

		NonLiteral role = getRoleByTitle(title);
		if (role == null) {
			return;
		}
		if (isBaseRole(role)) {
			return;
		}
		deleteTriplesOfASubject(role);
	}

	private boolean isBaseRole(NonLiteral role) {
		LockableMGraph systemGraph = getSystemGraph();
		GraphNode roleNode = new GraphNode(role, systemGraph);
		Lock readLock = roleNode.readLock();
		readLock.lock();
		try {
			return roleNode.hasProperty(RDF.type, PERMISSION.BaseRole);
		} finally {
			readLock.unlock();
		}
		
	}

	private void deleteTriplesOfASubject(NonLiteral subject) {
		LockableMGraph systemGraph = getSystemGraph();
		Lock writeLock = systemGraph.getLock().writeLock();
		writeLock.lock();
		try {
			Iterator<Triple> triples = systemGraph.filter(subject, null, null);
			while (triples.hasNext()) {
				triples.next();
				triples.remove();
			}
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void assignPermissionsToRole(String title,
			List<String> permissionEntries) {

		if (title == null) {
			return;
		}

		addPermissionEntriesForARole(
				getRoleByTitle(title), title,
				permissionEntries);
	}

	private void addPermissionEntriesForARole(NonLiteral role,
			String id, List<String> permissionEntries) {
		AccessController.checkPermission(new SecurityPermission("getPolicy"));
		if (role == null) {
			logger.debug("Cannot assign permissions: {} does not exist", id);
			return;
		}
		if (permissionEntries.isEmpty()) {
			return;
		}
		LockableMGraph systemGraph = getSystemGraph();
		Lock writeLock = systemGraph.getLock().writeLock();
		writeLock.lock();
		try {
			for (String permissionEntry : permissionEntries) {
				if (permissionEntry.trim().length() == 0) {
					continue;
				}
				systemGraph.add(new TripleImpl(role, PERMISSION.hasPermission,
						getPermissionOfAJavaPermEntry(permissionEntry)));
			}
		} finally {
			writeLock.unlock();
		}
		//refresh the policy so it will recheck the permissions
		Policy.getPolicy().refresh();
	}

	/**
	 * Get the permission node having the specified java permission entry.
	 * If the node does not exist, a new node is created.
	 *
	 * @param graph
	 * @param permissionString the specified java permission entry
	 * @return permission node
	 */
	private NonLiteral getPermissionOfAJavaPermEntry(
			String permissionString) {
		LockableMGraph systemGraph = getSystemGraph();
		PlainLiteral javaPermEntry = new PlainLiteralImpl(permissionString);
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			Iterator<Triple> javaPermTriples = systemGraph.filter(null,
					PERMISSION.javaPermissionEntry, javaPermEntry);
			if (javaPermTriples.hasNext()) {
				return javaPermTriples.next().getSubject();
			}
		} finally {
			readLock.unlock();
		}

		Lock writeLock = systemGraph.getLock().writeLock();
		writeLock.lock();
		try {
			BNode result = new BNode();
			systemGraph.add(new TripleImpl(result,
					PERMISSION.javaPermissionEntry, javaPermEntry));
			return result;
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public Iterator<NonLiteral> getPermissionsOfRole(NonLiteral role) {
		LockableMGraph systemGraph = getSystemGraph();
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			final Iterator<Triple> triples = systemGraph.filter(role,
					PERMISSION.hasPermission, null);
			Set<NonLiteral> permissions = new HashSet<NonLiteral>();
			while (triples.hasNext()) {
				permissions.add((NonLiteral) triples.next().getObject());
			}
			return permissions.iterator();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void deletePermissionsOfRole(String title,
			List<String> permissionEntries) {

		if (title == null) {
			return;
		}

		deletePermissionEntriesOfARole(
				getRoleByTitle(title), title,
				permissionEntries);
	}

	private void deletePermissionEntriesOfARole(NonLiteral role,
			String id, List<String> permissionEntries) {
		AccessController.checkPermission(new SecurityPermission("getPolicy"));
		if (role == null) {
			logger.debug("Cannot delete permissions: {} does not exist", id);
			return;
		}
		if (permissionEntries.isEmpty()) {
			return;
		}
		LockableMGraph systemGraph = getSystemGraph();
		Lock writeLock = systemGraph.getLock().writeLock();
		writeLock.lock();
		try {
			for (String permissionEntry : permissionEntries) {
				NonLiteral permission = getPermissionOfAJavaPermEntry(permissionEntry);
				systemGraph.remove(new TripleImpl(role, PERMISSION.hasPermission,
						permission));
			}
		} finally {
			writeLock.unlock();
		}
		//refresh the policy so it will recheck the permissions
		Policy.getPolicy().refresh();
	}

	@Override
	public void deleteAllPermissionsOfRole(String title) {
		if (title == null) {
			return;
		}

		deleteAllPermissionEntriesOfARole(
				getRoleByTitle(title));
	}

	private void deleteAllPermissionEntriesOfARole(NonLiteral role) {
		AccessController.checkPermission(new SecurityPermission("getPolicy"));
		if (role == null) {
			return;
		}
		LockableMGraph systemGraph = getSystemGraph();
		GraphNode graphNode = new GraphNode(role, systemGraph);
		Lock writeLock = systemGraph.getLock().writeLock();
		writeLock.lock();
		try {
			graphNode.deleteProperties(PERMISSION.hasPermission);
		} finally {
			writeLock.unlock();
		}		
		//refresh the policy so it will recheck the permissions
		Policy.getPolicy().refresh();
	}

	@Override
	public void storeUser(String name, String email, String password,
			List<String> assignedRoles, String pathPrefix) {

		if (name == null) {
			return;
		}

		if (getUserByUserName(name) != null) {
			throw new UserAlreadyExistsException(name);
		}
		if (email != null) {
			String storedName = getNameByEmail(email);
			if (storedName != null && !name.equals(storedName)) {
				throw new EmailAlreadyAssignedException(email, storedName);
			}
		}
		BNode user = new BNode();
		LockableMGraph systemGraph = getSystemGraph();
		Lock writeLock = systemGraph.getLock().writeLock();
		writeLock.lock();
		try {
			systemGraph.add(new TripleImpl(user, RDF.type, FOAF.Agent));
			systemGraph.add(new TripleImpl(user, PLATFORM.userName,
					new PlainLiteralImpl(name)));
			if (email != null) {
				systemGraph.add(new TripleImpl(user, FOAF.mbox,
						new UriRef("mailto:" + email)));
			}
			if (password != null) {
				try {
					String pswSha1 = bytes2HexString(MessageDigest.getInstance("SHA1").digest(password.getBytes("UTF-8")));
					systemGraph.add(new TripleImpl(user, PERMISSION.passwordSha1, new PlainLiteralImpl(pswSha1)));
				} catch (UnsupportedEncodingException ex) {
					throw new RuntimeException(ex);
				} catch (NoSuchAlgorithmException ex) {
					throw new RuntimeException(ex);
				}
			}
			if (pathPrefix != null && pathPrefix.trim().length() != 0) {
				systemGraph.add(new TripleImpl(user, OSGI.agent_path_prefix,
						new PlainLiteralImpl(pathPrefix)));
			}
			if (!assignedRoles.isEmpty()) {
				addRolesToUser(assignedRoles, user);
			}			
		} finally {
			writeLock.unlock();
		}
		
	}

	/**
	 *
	 * @param graph
	 * @param email
	 * @return
	 *		null if the email does not exist in the graph,
	 *		otherwise returns the name of the user who owns the email
	 * @throws org.apache.clerezza.platform.usermanager.UserHasNoNameException
	 */
	@Override
	public String getNameByEmail(String email)
			throws UserHasNoNameException {
		if (email == null) {
			return null;
		}
		LockableMGraph systemGraph = getSystemGraph();
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			Iterator<Triple> triples = systemGraph.filter(null, FOAF.mbox,
					new UriRef("mailto:" + email));
			if (!triples.hasNext()) {
				return null;
			}
			NonLiteral user = triples.next().getSubject();
			triples = systemGraph.filter(user, PLATFORM.userName, null);
			if (!triples.hasNext()) {
				throw new UserHasNoNameException("User with email address" + email
						+ " does not have a name");
			}
			return ((PlainLiteral) triples.next().getObject()).getLexicalForm();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void updateUser(String name, String email, String password,
			Collection<String> assignedRoles, String pathPrefix) {

		AccessController.checkPermission(new SecurityPermission("getPolicy"));
		if (name == null) {
			throw new IllegalArgumentException("userName may not be null");
		}
		NonLiteral user = getUserByUserName(name);
		if (user == null) {
			throw new UserNotExistsException(name);
		}
		LockableMGraph systemGraph = getSystemGraph();
		GraphNode userGraphNode = new GraphNode(user, systemGraph);
		Lock writeLock = userGraphNode.writeLock();
		writeLock.lock();
		try {
			if (email != null) {
				updateProperty(userGraphNode, FOAF.mbox, new UriRef("mailto:" + email));
			}

			if (password != null) {
				try {
					String pswSha1 = bytes2HexString(MessageDigest.getInstance("SHA1").digest(password.getBytes("UTF-8")));
					updateProperty(userGraphNode, PERMISSION.passwordSha1, new PlainLiteralImpl(pswSha1));
				} catch (UnsupportedEncodingException ex) {
					throw new RuntimeException(ex);
				} catch (NoSuchAlgorithmException ex) {
					throw new RuntimeException(ex);
				}
			}
			if (pathPrefix != null && pathPrefix.trim().length() != 0) {
				updateProperty(userGraphNode, OSGI.agent_path_prefix,
						new PlainLiteralImpl(pathPrefix));
			}
			if (!assignedRoles.isEmpty()) {
				userGraphNode.deleteProperties(SIOC.has_function);
				addRolesToUser(assignedRoles, (NonLiteral) userGraphNode.getNode());
				//refresh the policy so it will recheck the permissions
				Policy.getPolicy().refresh();
			}
		} finally {
			writeLock.unlock();
		}
	}

	private void addRolesToUser(Collection<String> assignedRoles, NonLiteral user) throws RoleUnavailableException {
		LockableMGraph systemGraph = getSystemGraph();
		for (String roleTitle : assignedRoles) {
			// skip empty strings
			if ((roleTitle == null) || (roleTitle.trim().length() == 0)) {
				continue;
			}
			NonLiteral role = getRoleByTitle(roleTitle);
			if (role == null) {
				throw new RoleUnavailableException(roleTitle);
			}
			systemGraph.add(new TripleImpl(user, SIOC.has_function, role));
		}
	}

	private void updateProperty(GraphNode node, UriRef predicate, Resource object) {
		node.deleteProperties(predicate);
		node.addProperty(predicate, object);
	}

	@Override
	public boolean nameExists(String name) {
		LockableMGraph systemGraph = getSystemGraph();
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			return systemGraph.filter(null, PLATFORM.userName,
				new PlainLiteralImpl(name)).hasNext();
		} finally {
			readLock.unlock();
		}		
	}

	@Override
	public boolean emailExists(String email) {
		LockableMGraph systemGraph = getSystemGraph();
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			return systemGraph.filter(null, FOAF.mbox,
					new UriRef("mailto:" + email)).hasNext();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public NonLiteral getUserByName( String name) {
		return getUserByUserName(name);
	}

	@Override
	public Iterator<NonLiteral> getUsers() {
		return getResourcesOfType(FOAF.Agent);
	}

	private Iterator<NonLiteral> getResourcesOfType(UriRef type) {
		LockableMGraph systemGraph = getSystemGraph();
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			final Iterator<Triple> triples = systemGraph.filter(null, RDF.type, type);
			Set<NonLiteral> userRoles = new HashSet<NonLiteral>();
			while (triples.hasNext()) {
				userRoles.add(triples.next().getSubject());
			}
			return userRoles.iterator();
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void deleteUser(String name) {
		if (name == null) {
			return;
		}

		NonLiteral user = getUserByUserName(name);
		if (user != null) {
			deleteTriplesOfASubject(user);
		}
	}

	@Override
	public void assignPermissionsToUser(String name,
			List<String> permissionEntries) {

		if (name == null) {
			return;
		}

		addPermissionEntriesForARole(
				getUserByUserName(name), name,
				permissionEntries);
	}

	@Override
	public Iterator<NonLiteral> getPermissionsOfUser(NonLiteral user) {
		return getPermissionsOfRole(user);
	}

	@Override
	public void deletePermissionsOfUser(String name,
			List<String> permissionEntries) {

		if (name == null) {
			return;
		}

		deletePermissionEntriesOfARole(
				getUserByUserName(name), name,
				permissionEntries);
	}

	@Override
	public void deleteAllPermissionsOfUser(String name) {
		if (name == null) {
			return;
		}

		deleteAllPermissionEntriesOfARole(
				getUserByUserName(name));
	}

	/**
	 * @param bytes
	 *		array of bytes to be converted to a String of hexadecimal numbers
	 * @return
	 *		String of hexadecimal numbers representing the byte array
	 */
	private char[] HEXDIGITS = "0123456789abcdef".toCharArray();

	private String bytes2HexString(byte[] bytes) {
		char[] result = new char[bytes.length << 1];
		for (int i = 0, j = 0; i < bytes.length; i++) {
			result[j++] = HEXDIGITS[bytes[i] >> 4 & 0xF];
			result[j++] = HEXDIGITS[bytes[i] & 0xF];
		}
		return new String(result);
	}

	@Override
	public GraphNode getUserInSystemGraph(final String name) {
		LockableMGraph systemGraph = getSystemGraph();
		NonLiteral user = getUserByUserName(name);
		if (user != null) {
			return new GraphNode(user, systemGraph);
		} else {
			return null;
		}
	}

	@Override
	public GraphNode getUserInContentGraph(final String name) {
		final LockableMGraph contentGraph = (LockableMGraph) cgProvider.getContentGraph();
		Iterator<Triple> triples = contentGraph.filter(null, PLATFORM.userName,
				new PlainLiteralImpl(name));
		GraphNode resultNode = null;
		if (triples.hasNext()) {
			Lock readLock = contentGraph.getLock().readLock();
			readLock.lock();
			try {
				resultNode = new GraphNode(triples.next().getSubject(), contentGraph);
			} finally {
				readLock.unlock();
			}			
		} else {
			NonLiteral user = AccessController.doPrivileged(
					new PrivilegedAction<NonLiteral>() {

						@Override
						public NonLiteral run() {
							return getUserByUserName(name);
						}
					});
			if (user != null) {
				Lock writeLock = contentGraph.getLock().writeLock();
				writeLock.lock();
				try {
					resultNode = new GraphNode(new BNode(), contentGraph);
				resultNode.addProperty(PLATFORM.userName,
						new PlainLiteralImpl(name));
				} finally {
					writeLock.unlock();
				}
				
			}
		}
		return resultNode;
	}

	@Override
	public GraphNode getUserGraphNode(final Subject subject) {
		LockableMGraph systemGraph = getSystemGraph();
		NonLiteral user = getUserBySubject(subject);

		if (user != null) {
			GraphNode userNodeInSystemGraph = new GraphNode(user, systemGraph);
			MGraph copiedUserContext = new SimpleMGraph(userNodeInSystemGraph.getNodeContext());
			if (user instanceof UriRef) {
				WebIdInfo webIdInfo = webIdGraphsService.getWebIdInfo((UriRef) user);
				Graph graph = new GraphNode(user, webIdInfo.publicProfile()).getNodeContext();
				copiedUserContext.addAll(graph);
			}
			return new GraphNode(user,copiedUserContext);
		} else {
			return null;
		}
	}

	private NonLiteral getUserBySubject(final Subject subject) {
		LockableMGraph systemGraph = getSystemGraph();
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			for (Principal principal : subject.getPrincipals()) {
				//here we can verify that all principals point to the same subject
				//but currently we just take the first. Also the method could return a list of resources
				if (principal instanceof WebIdPrincipal) {
					return ((WebIdPrincipal) principal).getWebId();
				} else return getUserByName(principal.getName());
			}
		} finally {
			readLock.unlock();
		}
		return null;
	}


	private NonLiteral getUserByUserName(final String name) {
		LockableMGraph systemGraph = getSystemGraph();
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			Iterator<Triple> triples = systemGraph.filter(null, PLATFORM.userName,
					new PlainLiteralImpl(name));
			if (triples.hasNext()) {
				return triples.next().getSubject();
			}
			return null;
		} finally {
			readLock.unlock();
		}

	}
}
