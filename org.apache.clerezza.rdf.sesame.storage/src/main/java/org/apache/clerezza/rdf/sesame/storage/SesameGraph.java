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
package org.apache.clerezza.rdf.sesame.storage;

import java.io.File;
import java.util.Collection;

import org.openrdf.repository.RepositoryException;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;

/**
 *	Graph implementation (read-only version of SesameMGraph). 
 *  <br />
 *	This graph implementation just overrides MGraph and throws 
 *  UnsupportedOperationException on methods modifying data.
 * 
 * @author msy
 */
public class SesameGraph extends SesameMGraph implements Graph {

	/**
	 *	the initial triples of this graph
	 */
	private TripleCollection initialTriples = null;

	/**
	 *	Initialize a new sesame graph with a collection.
	 *  <br />
	 *	This method has to be called before the read methods on this graph
	 *  can be executed.
	 * 
	 * @param dir directory where the repository is (data directory)
	 * @param initialTriples or null
	 * @throws RepositoryException error initializing repository
	 */
	public void initialize(File dir, TripleCollection initialTriples)
			throws RepositoryException {

		super.initialize(dir);

		if (initialTriples != null) {

			//add the initial triples to the graph
			super.addAll(initialTriples);
			this.initialTriples = initialTriples;
		}
	}

	/**
	 *	Two SesameGraphs are equal if either both are empty or both have 
	 *  the same triples collection.<br />
	 * <br />
	 *  If <code>obj</code>is not a SesameGraph, this implementation returns
	 *  false.<br />
	 */
	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof SesameGraph)) {
			return false;
		}
		SesameGraph other = (SesameGraph) obj;
		
		if ((initialTriples == null) && (other.getInitialTriples() == null)) {
			return true;
		}

		if ((initialTriples != null) && (other.getInitialTriples() != null)) {
			return initialTriples.equals(other.getInitialTriples());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 79 * hash + (initialTriples != null ? initialTriples.hashCode() : 0);
		return hash;
	}

	@Override
	public synchronized boolean add(Triple e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean addAll(Collection<? extends Triple> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean removeAll(Collection<?> col) {
		throw new UnsupportedOperationException();
	}

	/**
	 *	getter for initial triples
	 * 
	 * @return initial triples of the graph
	 */
	public TripleCollection getInitialTriples() {
		return initialTriples;
	}
}
