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
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.utils.security.PermissionParser;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Literal;

/**
 * Controls the permissions needed to access a triple collection provided by
 * <code>TcManager</code>.
 *
 * Clients with a ConfigureTcAcessPermission can set the permissions required to
 * access a Graph. These permissions are stored persistently in an
 * Graph named urn:x-localinstance:/graph-access.graph
 *
 * Clients should get an instance from TcManager.getTcAccessController()
 * 
 * @author reto
 */
public abstract class TcAccessController {

    private final TcManager tcManager;
    private final IRI permissionGraphName = new IRI("urn:x-localinstance:/graph-access.graph");
    //we can't rely on ontology plugin in rdf core
    private String ontologyNamespace = "http://clerezza.apache.org/2010/07/10/graphpermssions#";
    private final IRI readPermissionListProperty = new IRI(ontologyNamespace + "readPermissionList");
    private final IRI readWritePermissionListProperty = new IRI(ontologyNamespace + "readWritePermissionList");
    /**
     * The first item in the subject RDF list.
     */
    public static final IRI first = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#first");
    /**
     * The rest of the subject RDF list after the first item.
     */
    public static final IRI rest = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#rest");
    public static final IRI rdfNil = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
    private final Map<IRI, Collection<Permission>> readPermissionCache =
            Collections.synchronizedMap(new HashMap<IRI, Collection<Permission>>());
    private final Map<IRI, Collection<Permission>> readWritePermissionCache =
            Collections.synchronizedMap(new HashMap<IRI, Collection<Permission>>());

    /**
     *
     * @param tcManager the tcManager used to locate urn:x-localinstance:/graph-access.graph
     */
    public TcAccessController() {
        this.tcManager = getTcManager();
    }

    public void checkReadPermission(IRI GraphUri) {
        if (GraphUri.equals(permissionGraphName)) {
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
                Collection<Permission> perms = getRequiredReadPermissions(GraphUri);
                if (perms.size() > 0) {
                    for (Permission permission : perms) {
                        AccessController.checkPermission(permission);
                    }
                } else {
                    AccessController.checkPermission(new TcPermission(
                            GraphUri.getUnicodeString(), TcPermission.READ));
                }
            }
        }
    }

    public void checkReadWritePermission(IRI GraphUri) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            //will AllPermissions the rest is obsolete
            try {
                AccessController.checkPermission(new AllPermission());
            } catch (AccessControlException e) {
                if (GraphUri.equals(permissionGraphName)) {
                    AccessController.checkPermission(new TcPermission(
                            GraphUri.getUnicodeString(), TcPermission.READWRITE));
                } else {
                    Collection<Permission> perms = getRequiredReadWritePermissions(GraphUri);
                    if (perms.size() > 0) {
                        for (Permission permission : perms) {
                            AccessController.checkPermission(permission);
                        }
                    } else {
                        AccessController.checkPermission(new TcPermission(
                                GraphUri.getUnicodeString(), TcPermission.READWRITE));
                    }
                }
            }
        }
    }

    /**
     * Set the set of permissions required for read access to a triple-collection, if
     * the set is non-empty the default TCPermisson is no longer required.
     *
     * @param GraphUri
     * @param permissionDescriptions
     */
    public void setRequiredReadPermissionStrings(IRI GraphUri,
            Collection<String> permissionDescriptions) {
        readPermissionCache.remove(GraphUri);
        final Graph permissionMGraph = getOrCreatePermisionGraph();
        Lock l = permissionMGraph.getLock().writeLock();
        try {
            l.lock();
            removeExistingRequiredReadPermissions(GraphUri, permissionMGraph);
            final BlankNodeOrIRI permissionList = createList(permissionDescriptions.iterator(), permissionMGraph);
            permissionMGraph.add(new TripleImpl(GraphUri,
                    readPermissionListProperty, permissionList));
        } finally {
            l.unlock();
        }
    }

    /**
     * Set the set of permissions required for read access to a triple-collection, if
     * the set is non-empty the default TCPermisson is no longer required.
     *
     * @param GraphUri
     * @param permissionDescriptions
     */
    public void setRequiredReadPermissions(IRI GraphUri,
            Collection<Permission> permissions) {
        Collection<String> permissionStrings = new ArrayList<String>();
        for (Permission permission : permissions) {
            permissionStrings.add(permission.toString());
        }
        setRequiredReadPermissionStrings(GraphUri, permissionStrings);
    }

    /**
     * Set the set of permissions required for read-write access to a
     * triple-collection, if
     * the set is non-empty the default TCPermisson is no longer required.
     *
     * @param GraphUri
     * @param permissionDescriptions
     */
    public void setRequiredReadWritePermissionStrings(IRI GraphUri,
            Collection<String> permissionDescriptions) {
        readWritePermissionCache.remove(GraphUri);
        final Graph permissionMGraph = getOrCreatePermisionGraph();
        Lock l = permissionMGraph.getLock().writeLock();
        try {
            l.lock();
            removeExistingRequiredReadPermissions(GraphUri, permissionMGraph);
            final BlankNodeOrIRI permissionList = createList(permissionDescriptions.iterator(), permissionMGraph);
            permissionMGraph.add(new TripleImpl(GraphUri,
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
     * @param GraphUri
     * @param permissionDescriptions
     */
    public void setRequiredReadWritePermissions(IRI GraphUri,
            Collection<Permission> permissions) {
        Collection<String> permissionStrings = new ArrayList<String>();
        for (Permission permission : permissions) {
            permissionStrings.add(permission.toString());
        }
        setRequiredReadWritePermissionStrings(GraphUri, permissionStrings);
    }

    /**
     * Get the set of permissions required for read access to the
     * triple-collection, the set may be empty meaning that the default
     * TCPermission is required.
     *
     * @param GraphUri
     * @return the collection of permissions
     */
    public Collection<Permission> getRequiredReadPermissions(IRI GraphUri) {
        Collection<Permission> result = readPermissionCache.get(GraphUri);
        if (result == null) {
            result = new ArrayList<Permission>();
            Collection<String> permissionStrings = getRequiredReadPermissionStrings(GraphUri);
            for (String string : permissionStrings) {
                result.add(PermissionParser.getPermission(string, getClass().getClassLoader()));
            }
            readPermissionCache.put(GraphUri, result);
        }
        return result;
    }

    /**
     * Get the set of permissions required for read-write access to the
     * triple-collection, the set may be empty meaning that the default
     * TCPermission is required.
     *
     * @param GraphUri
     * @return the collection of permissions
     */
    public Collection<Permission> getRequiredReadWritePermissions(IRI GraphUri) {
        Collection<Permission> result = readWritePermissionCache.get(GraphUri);
        if (result == null) {
            result = new ArrayList<Permission>();
            Collection<String> permissionStrings = getRequiredReadWritePermissionStrings(GraphUri);
            for (String string : permissionStrings) {
                result.add(PermissionParser.getPermission(string, getClass().getClassLoader()));
            }
            readWritePermissionCache.put(GraphUri, result);
        }
        return result;
    }

    private BlankNodeOrIRI createList(Iterator<String> iterator, Graph permissionMGraph) {
        if (!iterator.hasNext()) {
            return rdfNil;
        }
        final BlankNode result = new BlankNode();
        permissionMGraph.add(new TripleImpl(result, first,
                LiteralFactory.getInstance().createTypedLiteral(iterator.next())));
        permissionMGraph.add(new TripleImpl(result, rest,
                createList(iterator, permissionMGraph)));
        return result;

    }

    //called withiong write-lock
    private void removeExistingRequiredReadPermissions(IRI GraphUri,
            Graph permissionMGraph) {
        try {
            Triple t = permissionMGraph.filter(GraphUri, readPermissionListProperty, null).next();
            RDFTerm list = t.getObject();
            removeList((BlankNodeOrIRI) list, permissionMGraph);
            permissionMGraph.remove(t);
        } catch (NoSuchElementException e) {
            //There was no existing to remove
        }
    }

    private void removeList(BlankNodeOrIRI list, Graph permissionMGraph) {
        try {
            Triple t = permissionMGraph.filter(list, rest, null).next();
            RDFTerm restList = t.getObject();
            removeList((BlankNodeOrIRI) restList, permissionMGraph);
            permissionMGraph.remove(t);
            Iterator<Triple> iter = permissionMGraph.filter(list, first, null);
            iter.next();
            iter.remove();
        } catch (NoSuchElementException e) {
            //if it has no rest its rdf:NIL and has no first
        }
    }

    private Collection<String> getRequiredReadWritePermissionStrings(final IRI GraphUri) {
        return getRequiredPermissionStrings(GraphUri, readWritePermissionListProperty);
    }
    private Collection<String> getRequiredReadPermissionStrings(final IRI GraphUri) {
        return getRequiredPermissionStrings(GraphUri, readPermissionListProperty);
    }
    private Collection<String> getRequiredPermissionStrings(final IRI GraphUri, IRI property) {
        try {
            final Graph permissionMGraph = tcManager.getMGraph(permissionGraphName);
            Lock l = permissionMGraph.getLock().readLock();
            try {
                l.lock();
                Triple t = permissionMGraph.filter(GraphUri, property, null).next();
                BlankNodeOrIRI list = (BlankNodeOrIRI) t.getObject();
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

    private void readList(BlankNodeOrIRI list, Graph permissionMGraph, LinkedList<String> target) {
        if (list.equals(rdfNil)) {
            return;
        }
        Triple restTriple = permissionMGraph.filter(list, rest, null).next();
        BlankNodeOrIRI restList = (BlankNodeOrIRI) restTriple.getObject();
        readList(restList, permissionMGraph, target);
        Triple firstTriple = permissionMGraph.filter(list, first, null).next();
        Literal firstValue = (Literal) firstTriple.getObject();
        String value = LiteralFactory.getInstance().createObject(String.class, firstValue);
        target.addFirst(value);
    }

    private Graph getOrCreatePermisionGraph() {
        try {
            return tcManager.getMGraph(permissionGraphName);
        } catch (NoSuchEntityException e) {
            return tcManager.createGraph(permissionGraphName);
        }
    }

    /**
     * Note that this will only be invoked once
     * @return 
     */
    protected abstract TcManager getTcManager();
}
