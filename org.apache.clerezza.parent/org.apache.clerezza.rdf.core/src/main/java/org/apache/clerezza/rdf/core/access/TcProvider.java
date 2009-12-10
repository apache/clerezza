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

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;

/**
 * A TC (TripleCollection) Provider allows access to and optionally 
 * creation of named {@link Graph}s and {@link MGraph}s (mutable graphs)
 *
 * @author reto
 */
public interface TcProvider {

	/**
	 * Get a <code>Graph</code> by its name
	 *
	 * @param name the name of the Graph
	 * @return the <code>Graph</code> with the specified name
	 * @throws NoSuchEntityException if there is no <code>Graph</code>
	 *         with the specified name
	 */
	Graph getGraph(UriRef name) throws NoSuchEntityException;

	/**
	 * Get an <code>MGraph</code> by its name. The instances
	 * returned in different invocations are <code>equals</code>.
	 *
	 * @param the name of the <code>MGraph</code>
	 * @return name the <code>MGraph</code> with the specified name
	 * @throws NoSuchEntityException if there is no <code>MGraph</code>
	 *         with the specified name
	 */
	MGraph getMGraph(UriRef name) throws NoSuchEntityException;
	
	/**
	 * This method is used to get a <code>TripleCollection</code> indifferently
	 * whether it's a Graph or an MGraph. If the <code>name</code> names an 
	 * <code>MGraph</code> the result is the same as when invoking 
	 * <code>getMGraph</code> with that argument, analogously for 
	 * <code>Graph</code>S the method returns an instance equals to what 
	 * <code>getGraph</code> would return. 
	 * 
	 * @param name the name of the <Code>Graph</code> or <code>MGraph</code>
	 * @return the <Code>Graph</code> or <code>MGraph</code>
	 * @throws NoSuchEntityException if there is no <code>Graph</code>
	 *         or <code>MGraph</code> with the specified name
	 */
	TripleCollection getTriples(UriRef name) throws NoSuchEntityException;

	/**
	 * Lists the name of the <Code>Graph</code>s available through this
	 * <code>TcProvider</code>, implementations may take into account the
	 * security context and omit <Code>Graph</code>s for which access is not
	 * allowed.
	 *
	 * @return the list of <Code>Graph</code>s
	 */
	Set<UriRef> listGraphs();

	/**
	 * Lists the name of the <Code>MGraph</code>s available through this
	 * <code>TcProvider</code>, implementations may take into account the
	 * security context and omit <Code>MGraph</code>s for which access is not
	 * allowed.
	 *
	 * @return the list of <Code>MGraph</code>s
	 */
	Set<UriRef> listMGraphs();

	/**
	 * Lists the name of the <Code>TripleCollection</code>s available through this
	 * <code>TcProvider</code> indifferently whether they are Graphs or an
	 * MGraphs, implementations may take into account the security context and
	 * omit <Code>TripleCollection</code>s for which access is not allowed.
	 *
	 * @return the list of <Code>TripleCollection</code>s
	 */
	Set<UriRef> listTripleCollections();

	/**
	 * Creates an initially empty <code>MGraph</code> with a specified name
	 *
	 * @param name names the new <code>MGraph</code>
	 * @return the newly created <code>MGraph</code>
	 * @throws UnsupportedOperationException if this provider doesn't support
	 *         creating <code>MGraph</code>S
	 * @throws EntityAlreadyExistsException if an MGraph with the specified name
	 *         already exists
	 */
	MGraph createMGraph(UriRef name) throws UnsupportedOperationException, 
			EntityAlreadyExistsException;

	/**
	 * Creates a <code>Graph</code> with a specified name
	 *
	 * @param name the name of the <code>Graph</code> to be created
	 * @param triples the triples of the new <code>Graph</code>
	 * @return the newly created <code>Graph</code>
	 * @throws UnsupportedOperationException if this provider doesn't support
	 *         creating <code>Graph</code>S
	 * @throws EntityAlreadyExistsException if a Graph with the specified name
	 *         already exists
	 */
	Graph createGraph(UriRef name, TripleCollection triples) 
			throws UnsupportedOperationException, EntityAlreadyExistsException;
	
	/**
	 * Deletes the <code>Graph</code> or <code>MGraph</code> of a specified name.
	 * If <code>name</code> references a Graph and the graph has other names, it
	 * will still be available with those other names.
	 * 
	 * @param name the entity to be removed
	 * @throws UnsupportedOperationException if this provider doesn't support
	 *         entities deletion.
	 * @throws NoSuchEntityException if <code>name</code> doesn't refer to a 
	 *		   <code>Graph</code> or an <code>MGraph</code>.
	 * @throws EntityUndeletableException if the specified Graph is undeletable
	 */
	void deleteTripleCollection(UriRef name) throws UnsupportedOperationException,
			NoSuchEntityException, EntityUndeletableException;

	/**
	 * get a set of the names of a <code>Graph</code>
	 *
	 * @param graph
	 * @return the set names of <code>Graph</code>, the set is empty if
	 *         <code>Graph</code> is unknown
	 */
	Set<UriRef> getNames(Graph graph);
}
