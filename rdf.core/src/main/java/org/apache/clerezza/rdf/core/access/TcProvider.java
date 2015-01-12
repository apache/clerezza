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
package org.apache.clerezza.rdf.core.access;

import java.util.Set;

import org.apache.commons.rdf.ImmutableGraph;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.Iri;

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
    ImmutableGraph getGraph(Iri name) throws NoSuchEntityException;

    /**
     * Get an <code>Graph</code> by its name. The instances
     * returned in different invocations are <code>equals</code>.
     *
     * @param the name of the <code>Graph</code>
     * @return name the <code>Graph</code> with the specified name
     * @throws NoSuchEntityException if there is no <code>Graph</code>
     *         with the specified name
     */
    Graph getMGraph(Iri name) throws NoSuchEntityException;
    
    /**
     * This method is used to get a <code>Graph</code> indifferently
     * whether it's a ImmutableGraph or an Graph. If the <code>name</code> names an 
     * <code>Graph</code> the result is the same as when invoking 
     * <code>getMGraph</code> with that argument, analogously for 
     * <code>ImmutableGraph</code>S the method returns an instance equals to what 
     * <code>getGraph</code> would return. 
     * 
     * @param name the name of the <Code>ImmutableGraph</code> or <code>Graph</code>
     * @return the <Code>ImmutableGraph</code> or <code>Graph</code>
     * @throws NoSuchEntityException if there is no <code>ImmutableGraph</code>
     *         or <code>Graph</code> with the specified name
     */
    Graph getTriples(Iri name) throws NoSuchEntityException;

    /**
     * Lists the name of the <Code>ImmutableGraph</code>s available through this
     * <code>TcProvider</code>, implementations may take into account the
     * security context and omit <Code>ImmutableGraph</code>s for which access is not
     * allowed.
     *
     * @return the list of <Code>ImmutableGraph</code>s
     */
    Set<Iri> listImmutableGraphs();

    /**
     * Lists the name of the <Code>Graph</code>s available through this
     * <code>TcProvider</code>, implementations may take into account the
     * security context and omit <Code>Graph</code>s for which access is not
     * allowed.
     *
     * @return the list of <Code>Graph</code>s
     */
    Set<Iri> listMGraphs();

    /**
     * Lists the name of the <Code>Graph</code>s available through this
     * <code>TcProvider</code> indifferently whether they are Graphs or an
     * MGraphs, implementations may take into account the security context and
     * omit <Code>Graph</code>s for which access is not allowed.
     *
     * @return the list of <Code>Graph</code>s
     */
    Set<Iri> listGraphs();

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
    Graph createMGraph(Iri name) throws UnsupportedOperationException, 
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
    ImmutableGraph createGraph(Iri name, Graph triples) 
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
    void deleteGraph(Iri name) throws UnsupportedOperationException,
            NoSuchEntityException, EntityUndeletableException;

    /**
     * get a set of the names of a <code>ImmutableGraph</code>
     *
     * @param ImmutableGraph
     * @return the set names of <code>ImmutableGraph</code>, the set is empty if
     *         <code>ImmutableGraph</code> is unknown
     */
    Set<Iri> getNames(ImmutableGraph ImmutableGraph);
}
