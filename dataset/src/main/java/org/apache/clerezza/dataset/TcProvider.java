/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */
package org.apache.clerezza.dataset;

import org.apache.clerezza.Graph;
import org.apache.clerezza.IRI;
import org.apache.clerezza.ImmutableGraph;

import java.util.Set;

/**
 * A TC (Graph) Provider allows access to and optionally 
 * creation of named {@link ImmutableGraph}s and {@link Graph}s (mutable graphs)
 *
 * @author reto
 */
public interface TcProvider {

    /**
     * Get a <code>ImmutableGraph</code> by its name
     *
     * @param name the name of the ImmutableGraph
     * @return the <code>ImmutableGraph</code> with the specified name
     * @throws NoSuchEntityException if there is no <code>ImmutableGraph</code>
     *         with the specified name
     */
    ImmutableGraph getImmutableGraph(IRI name) throws NoSuchEntityException;

    /**
     * Get an <code>Graph</code> taht is not <code>ImmutableGrah</code>. The instances
     * returned in different invocations are <code>equals</code>.
     *
     * @param name the name of the <code>Graph</code>
     * @return <code>Graph</code> with the specified name
     * @throws NoSuchEntityException if there is no <code>Graph</code>
     *         with the specified name
     */
    Graph getMGraph(IRI name) throws NoSuchEntityException;

    /**
     * This method is used to get a <code>Graph</code> indifferently
     * whether it's a ImmutableGraph or not. If the <code>name</code> names an
     * <code>Graph</code> the result is the same as when invoking
     * <code>getMGraph</code> with that argument, analogously for
     * <code>ImmutableGraph</code>S the method returns an instance equals to what
     * <code>getImmutableGraph</code> would return.
     *
     * @param name the name of the <Code>ImmutableGraph</code> or <code>Graph</code>
     * @return the <Code>ImmutableGraph</code> or <code>Graph</code>
     * @throws NoSuchEntityException if there is no <code>ImmutableGraph</code>
     *         or <code>Graph</code> with the specified name
     */
    Graph getGraph(IRI name) throws NoSuchEntityException;

    /**
     * Lists the name of the <Code>ImmutableGraph</code>s available through this
     * <code>TcProvider</code>, implementations may take into account the
     * security context and omit <Code>ImmutableGraph</code>s for which access is not
     * allowed.
     *
     * @return the list of <Code>ImmutableGraph</code>s
     */
    Set<IRI> listImmutableGraphs();

    /**
     * Lists the name of the <Code>Graph</code>s available through this
     * <code>TcProvider</code> that are not <Code>ImmutableGraph</code>, implementations may take into account the
     * security context and omit <Code>Graph</code>s for which access is not
     * allowed.
     *
     * @return the list of <Code>Graph</code>s
     */
    Set<IRI> listMGraphs();

    /**
     * Lists the name of the <Code>Graph</code>s available through this
     * <code>TcProvider</code> indifferently whether they are mutables or
     * immutables, implementations may take into account the security context and
     * omit <Code>Graph</code>s for which access is not allowed.
     *
     * @return the list of <Code>Graph</code>s
     */
    Set<IRI> listGraphs();

    /**
     * Creates an initially empty <code>Graph</code> with a specified name
     *
     * @param name names the new <code>Graph</code>
     * @return the newly created <code>Graph</code>
     * @throws UnsupportedOperationException if this provider doesn't support
     *         creating <code>Graph</code>S
     * @throws EntityAlreadyExistsException if an Graph with the specified name
     *         already exists
     */
    Graph createGraph(IRI name) throws UnsupportedOperationException,
            EntityAlreadyExistsException;

    /**
     * Creates a <code>ImmutableGraph</code> with a specified name
     *
     * @param name the name of the <code>ImmutableGraph</code> to be created
     * @param triples the triples of the new <code>ImmutableGraph</code>
     * @return the newly created <code>ImmutableGraph</code>
     * @throws UnsupportedOperationException if this provider doesn't support
     *         creating <code>ImmutableGraph</code>S
     * @throws EntityAlreadyExistsException if a ImmutableGraph with the specified name
     *         already exists
     */
    ImmutableGraph createImmutableGraph(IRI name, Graph triples)
            throws UnsupportedOperationException, EntityAlreadyExistsException;

    /**
     * Deletes the <code>ImmutableGraph</code> or <code>Graph</code> of a specified name.
     * If <code>name</code> references a ImmutableGraph and the ImmutableGraph has other names, it
     * will still be available with those other names.
     *
     * @param name the entity to be removed
     * @throws UnsupportedOperationException if this provider doesn't support
     *         entities deletion.
     * @throws NoSuchEntityException if <code>name</code> doesn't refer to a
     *           <code>ImmutableGraph</code> or an <code>Graph</code>.
     * @throws EntityUndeletableException if the specified ImmutableGraph is undeletable
     */
    void deleteGraph(IRI name) throws UnsupportedOperationException,
            NoSuchEntityException, EntityUndeletableException;

    /**
     * get a set of the names of a <code>ImmutableGraph</code>
     *
     * @param immutableGraph
     * @return the set names of <code>ImmutableGraph</code>, the set is empty if
     *         <code>ImmutableGraph</code> is unknown
     */
    Set<IRI> getNames(ImmutableGraph immutableGraph);
}
