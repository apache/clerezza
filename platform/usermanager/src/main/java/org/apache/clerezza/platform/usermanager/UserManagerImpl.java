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
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.SecurityPermission;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.BlankNodeOrIri;
import org.apache.clerezza.commons.rdf.RdfTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.SecuredGraph;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.OSGI;
import org.apache.clerezza.rdf.ontologies.PERMISSION;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.SIOC;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.commons.rdf.Literal;

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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference(target = SystemConfig.SYSTEM_GRAPH_FILTER)
    private Graph securedSystemGraph;

    private Graph getSystemGraph() {
        return ((SecuredGraph) securedSystemGraph).getUnsecuredGraph();
    }

    @Override
    public void storeRole(String title) {
        if (title == null) {
            return;
        }
        if (getRoleByTitle(title) != null) {
            return;
        }
        BlankNode role = new BlankNode();
        Graph systemGraph = getSystemGraph();
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
    public BlankNodeOrIri getRoleByTitle(String title) {
        Graph systemGraph = getSystemGraph();
        Lock readLock = systemGraph.getLock().readLock();
        readLock.lock();
        try {
            Iterator<Triple> triples = systemGraph.filter(null, DC.title,
                    new PlainLiteralImpl(title));
            BlankNodeOrIri role = null;
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
    public Iterator<BlankNodeOrIri> getRoles() {
        Graph systemGraph = getSystemGraph();
        Lock readLock = systemGraph.getLock().readLock();
        readLock.lock();
        try {
            final Iterator<BlankNodeOrIri> allRolesIter = getResourcesOfType(PERMISSION.Role);
            final Set<BlankNodeOrIri> allRolesSet = new HashSet<BlankNodeOrIri>();
            while (allRolesIter.hasNext()) {
                allRolesSet.add(allRolesIter.next());
            }
            final Set<BlankNodeOrIri> nonBaseRolesSet = new HashSet<BlankNodeOrIri>();
            for (BlankNodeOrIri role : allRolesSet) {
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
    public Iterator<BlankNodeOrIri> getRolesOfUser(BlankNodeOrIri user){
        Graph systemGraph = getSystemGraph();
        Lock readLock = systemGraph.getLock().readLock();
        readLock.lock();
        try {
            final Iterator<Triple> triples = systemGraph.filter(user,SIOC.has_function, null);
            Set<BlankNodeOrIri> userRoles = new HashSet<BlankNodeOrIri>();
            while (triples.hasNext()) {
                userRoles.add((BlankNodeOrIri) triples.next().getObject());
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

        BlankNodeOrIri role = getRoleByTitle(title);
        if (role == null) {
            return;
        }
        if (isBaseRole(role)) {
            return;
        }
        deleteTriplesOfASubject(role);
    }

    private boolean isBaseRole(BlankNodeOrIri role) {
        Graph systemGraph = getSystemGraph();
        GraphNode roleNode = new GraphNode(role, systemGraph);
        Lock readLock = roleNode.readLock();
        readLock.lock();
        try {
            return roleNode.hasProperty(RDF.type, PERMISSION.BaseRole);
        } finally {
            readLock.unlock();
        }
        
    }

    private void deleteTriplesOfASubject(BlankNodeOrIri subject) {
        Graph systemGraph = getSystemGraph();
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

    private void addPermissionEntriesForARole(BlankNodeOrIri role,
            String id, List<String> permissionEntries) {
        AccessController.checkPermission(new SecurityPermission("getPolicy"));
        if (role == null) {
            logger.debug("Cannot assign permissions: {} does not exist", id);
            return;
        }
        if (permissionEntries.isEmpty()) {
            return;
        }
        Graph systemGraph = getSystemGraph();
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
    private BlankNodeOrIri getPermissionOfAJavaPermEntry(
            String permissionString) {
        Graph systemGraph = getSystemGraph();
        Literal javaPermEntry = new PlainLiteralImpl(permissionString);
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
            BlankNode result = new BlankNode();
            systemGraph.add(new TripleImpl(result,
                    PERMISSION.javaPermissionEntry, javaPermEntry));
            return result;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Iterator<BlankNodeOrIri> getPermissionsOfRole(BlankNodeOrIri role) {
        Graph systemGraph = getSystemGraph();
        Lock readLock = systemGraph.getLock().readLock();
        readLock.lock();
        try {
            final Iterator<Triple> triples = systemGraph.filter(role,
                    PERMISSION.hasPermission, null);
            Set<BlankNodeOrIri> permissions = new HashSet<BlankNodeOrIri>();
            while (triples.hasNext()) {
                permissions.add((BlankNodeOrIri) triples.next().getObject());
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

    private void deletePermissionEntriesOfARole(BlankNodeOrIri role,
            String id, List<String> permissionEntries) {
        AccessController.checkPermission(new SecurityPermission("getPolicy"));
        if (role == null) {
            logger.debug("Cannot delete permissions: {} does not exist", id);
            return;
        }
        if (permissionEntries.isEmpty()) {
            return;
        }
        Graph systemGraph = getSystemGraph();
        Lock writeLock = systemGraph.getLock().writeLock();
        writeLock.lock();
        try {
            for (String permissionEntry : permissionEntries) {
                BlankNodeOrIri permission = getPermissionOfAJavaPermEntry(permissionEntry);
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

    private void deleteAllPermissionEntriesOfARole(BlankNodeOrIri role) {
        AccessController.checkPermission(new SecurityPermission("getPolicy"));
        if (role == null) {
            return;
        }
        Graph systemGraph = getSystemGraph();
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
        BlankNode user = new BlankNode();
        Graph systemGraph = getSystemGraph();
        Lock writeLock = systemGraph.getLock().writeLock();
        writeLock.lock();
        try {
            systemGraph.add(new TripleImpl(user, RDF.type, FOAF.Agent));
            systemGraph.add(new TripleImpl(user, PLATFORM.userName,
                    new PlainLiteralImpl(name)));
            if (email != null) {
                systemGraph.add(new TripleImpl(user, FOAF.mbox,
                        new Iri("mailto:" + email)));
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
     *        null if the email does not exist in the graph,
     *        otherwise returns the name of the user who owns the email
     * @throws org.apache.clerezza.platform.usermanager.UserHasNoNameException
     */
    @Override
    public String getNameByEmail(String email)
            throws UserHasNoNameException {
        if (email == null) {
            return null;
        }
        Graph systemGraph = getSystemGraph();
        Lock readLock = systemGraph.getLock().readLock();
        readLock.lock();
        try {
            Iterator<Triple> triples = systemGraph.filter(null, FOAF.mbox,
                    new Iri("mailto:" + email));
            if (!triples.hasNext()) {
                return null;
            }
            BlankNodeOrIri user = triples.next().getSubject();
            triples = systemGraph.filter(user, PLATFORM.userName, null);
            if (!triples.hasNext()) {
                throw new UserHasNoNameException("User with email address" + email
                        + " does not have a name");
            }
            return ((Literal) triples.next().getObject()).getLexicalForm();
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
        BlankNodeOrIri user = getUserByUserName(name);
        if (user == null) {
            throw new UserNotExistsException(name);
        }
        Graph systemGraph = getSystemGraph();
        GraphNode userGraphNode = new GraphNode(user, systemGraph);
        Lock writeLock = userGraphNode.writeLock();
        writeLock.lock();
        try {
            if (email != null) {
                updateProperty(userGraphNode, FOAF.mbox, new Iri("mailto:" + email));
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
                addRolesToUser(assignedRoles, (BlankNodeOrIri) userGraphNode.getNode());
                //refresh the policy so it will recheck the permissions
                Policy.getPolicy().refresh();
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void addRolesToUser(Collection<String> assignedRoles, BlankNodeOrIri user) throws RoleUnavailableException {
        Graph systemGraph = getSystemGraph();
        for (String roleTitle : assignedRoles) {
            // skip empty strings
            if ((roleTitle == null) || (roleTitle.trim().length() == 0)) {
                continue;
            }
            BlankNodeOrIri role = getRoleByTitle(roleTitle);
            if (role == null) {
                throw new RoleUnavailableException(roleTitle);
            }
            systemGraph.add(new TripleImpl(user, SIOC.has_function, role));
        }
    }

    private void updateProperty(GraphNode node, Iri predicate, RdfTerm object) {
        node.deleteProperties(predicate);
        node.addProperty(predicate, object);
    }

    @Override
    public boolean nameExists(String name) {
        Graph systemGraph = getSystemGraph();
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
        Graph systemGraph = getSystemGraph();
        Lock readLock = systemGraph.getLock().readLock();
        readLock.lock();
        try {
            return systemGraph.filter(null, FOAF.mbox,
                    new Iri("mailto:" + email)).hasNext();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public BlankNodeOrIri getUserByName( String name) {
        return getUserByUserName(name);
    }

    @Override
    public Iterator<BlankNodeOrIri> getUsers() {
        return getResourcesOfType(FOAF.Agent);
    }

    private Iterator<BlankNodeOrIri> getResourcesOfType(Iri type) {
        Graph systemGraph = getSystemGraph();
        Lock readLock = systemGraph.getLock().readLock();
        readLock.lock();
        try {
            final Iterator<Triple> triples = systemGraph.filter(null, RDF.type, type);
            Set<BlankNodeOrIri> userRoles = new HashSet<BlankNodeOrIri>();
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

        BlankNodeOrIri user = getUserByUserName(name);
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
    public Iterator<BlankNodeOrIri> getPermissionsOfUser(BlankNodeOrIri user) {
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
     *        array of bytes to be converted to a String of hexadecimal numbers
     * @return
     *        String of hexadecimal numbers representing the byte array
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
        Graph systemGraph = getSystemGraph();
        BlankNodeOrIri user = getUserByUserName(name);
        if (user != null) {
            return new GraphNode(user, systemGraph);
        } else {
            return null;
        }
    }

    @Override
    public GraphNode getUserInContentGraph(final String name) {
        final Graph contentGraph =  cgProvider.getContentGraph();
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
            BlankNodeOrIri user = AccessController.doPrivileged(
                    new PrivilegedAction<BlankNodeOrIri>() {

                        @Override
                        public BlankNodeOrIri run() {
                            return getUserByUserName(name);
                        }
                    });
            if (user != null) {
                Lock writeLock = contentGraph.getLock().writeLock();
                writeLock.lock();
                try {
                    resultNode = new GraphNode(new BlankNode(), contentGraph);
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
    public GraphNode getUserGraphNode(final String name) {
        Graph systemGraph = getSystemGraph();
        BlankNodeOrIri user = getUserByUserName(name);
        if (user != null) {
            GraphNode userNodeInSystemGraph =
                    new GraphNode(getUserByUserName(name), systemGraph);
            Graph copiedUserContext = new SimpleGraph(userNodeInSystemGraph.getNodeContext());
            return new GraphNode(userNodeInSystemGraph.getNode(),
                    copiedUserContext);
        } else {
            return null;
        }
    }

    private BlankNodeOrIri getUserByUserName(String name) {
        Graph systemGraph = getSystemGraph();
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
