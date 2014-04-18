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
package org.apache.clerezza.rdf.virtuoso.storage;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.AbstractMGraph;
import org.apache.clerezza.rdf.virtuoso.storage.access.DataAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of MGraph for the Virtuoso quad store.
 * 
 * @author enridaga
 * 
 */
public class VirtuosoMGraph extends AbstractMGraph implements MGraph,
		LockableMGraph {
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(VirtuosoMGraph.class);

	/**
	 * The name of the graph
	 */
	private String name = null;
	// private int size = 0;

	private VirtuosoGraph readOnly = null;
	private DataAccess dataAccess = null;

	/**
	 * Creates a {@link VirtuosoMGraph} Virtuoso MGraph binds directly to the
	 * store.
	 * 
	 * @param connection
	 */
	public VirtuosoMGraph(String name, DataAccess dataAccess) {
		logger.trace("VirtuosoMGraph(String {}, DataAccess {})", name,
				dataAccess);
		this.name = name;
		// this.provider = provider;
		this.dataAccess = dataAccess;
	}

	@Override
	public ReadWriteLock getLock() {
		logger.trace("getLock()");
		return lock;
	}

	@Override
	public Graph getGraph() {
		logger.trace("getGraph()");
		return asVirtuosoGraph();
	}

	public VirtuosoGraph asVirtuosoGraph() {
		logger.trace("asVirtuosoGraph()");
		if (this.readOnly == null) {
			logger.trace("create embedded singleton read-only instance");
			this.readOnly = new VirtuosoGraph(name, getDataAccess());
		}
		return readOnly;
	}

	protected DataAccess getDataAccess() {
		return this.dataAccess;
	}

	@Override
	protected Iterator<Triple> performFilter(NonLiteral subject,
			UriRef predicate, Resource object) {
		readLock.lock();
		try{
			return getDataAccess().filter(getName(), subject,
				predicate, object);
		}finally{
			readLock.unlock();
		}
	}

	/**
	 * We load the size every time it is requested.
	 */
	@Override
	public int size() {
		logger.trace("size()");
		readLock.lock();
		try{
			return getDataAccess().size(getName());
		}finally{
			readLock.unlock();
		}
	}

	@Override
	public void clear() {
		logger.trace("clear()");
		writeLock.lock();
		try{
			getDataAccess().clearGraph(getName());
		}finally{
			writeLock.unlock();
		}
	}

	protected boolean performAdd(Triple triple) {
		logger.trace("performAdd(Triple {})", triple);
		writeLock.lock();
		try{
			getDataAccess().insertQuad(getName(), triple);
		}finally{
			writeLock.unlock();
		}
		return true;
	}

	protected boolean performRemove(Triple triple) {
		logger.trace("performRemove(Triple triple)", triple);
		writeLock.lock();
		try{
			getDataAccess().deleteQuad(getName(), triple);
		}finally{
			writeLock.unlock();
		}
		return true;
	}

	/**
	 * Returns the graph name
	 * 
	 * @return
	 */
	public String getName() {
		logger.trace("getName()");
		return name;
	}

	/**
	 * Must be a VirtuosoMGraph with the same name. Subclasses are not assumed
	 * to be equals (VirtuosoGraph is not the same as VirtuosoMGraph)
	 */
	public boolean equals(Object o) {
		logger.trace("equals({})", o);
		// It must be an instance of VirtuosoMGraph
		if (o.getClass().equals(VirtuosoMGraph.class)) {
			logger.trace("{} is a VirtuosoMGraph)", o);
			if (((VirtuosoMGraph) o).getName().equals(this.getName())) {
				logger.debug("Names are equal! They are equal!");
				return true;
			}
		} else {
			logger.trace("Not a VirtuosoMGraph instance: {}", o.getClass());
		}
		return false;
	}
}
