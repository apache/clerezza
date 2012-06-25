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
package org.apache.clerezza.platform.typepriority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.RdfList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rbn
 */
@Component
@Service(value=TypePrioritizer.class)
@References({
	@Reference(name="systemGraph",
		cardinality=ReferenceCardinality.MANDATORY_UNARY,
		referenceInterface=LockableMGraph.class,
		target=SystemConfig.SYSTEM_GRAPH_FILTER)})
public class TypePrioritizer {
	public static final UriRef typePriorityListUri = new UriRef("urn:x-localinstance:/typePriorityList");

	private List<UriRef> typePriorityList;
	private static final Logger log = LoggerFactory.getLogger(TypePrioritizer.class);
	
	LockableMGraph systemGraph;

	protected void bindSystemGraph(LockableMGraph systemGraph) {
		Lock l = systemGraph.getLock().readLock();
		l.lock();
		try {
			List<Resource> rdfTypePriorityList = new RdfList(
				 typePriorityListUri, systemGraph);
			typePriorityList  = new ArrayList<UriRef>(rdfTypePriorityList.size());
			for (Resource resource : rdfTypePriorityList) {
				if (resource instanceof UriRef) {
					typePriorityList.add((UriRef) resource);
				} else {
					log.warn("Type priority list contains a resource "
							+ "that is not a uri, skipping.");
				}
			}
		} finally {
			l.unlock();
		}
		this.systemGraph = (LockableMGraph) systemGraph;
	}

	protected void unbindSystemGraph(LockableMGraph systemGraph) {
		typePriorityList = null;
		this.systemGraph = null;
	}

	/**
	 *
	 * @param rdfTypes the rdf types to be sorted
	 * @return a sorted iterator of the types
	 */
	public Iterator<UriRef> iterate(final Collection<UriRef> rdfTypes) {
		return new Iterator<UriRef>() {
			final Set<UriRef> remaining = new HashSet<UriRef>(rdfTypes);
			boolean rdfsResourceRemovedAndNotYetReturned = remaining.remove(RDFS.Resource);
			final Iterator<UriRef> typePriorityIter = typePriorityList.iterator();
			Iterator<UriRef> remainingIter = null;
			UriRef next = prepareNext();
			
			private UriRef prepareNext() {
				while (typePriorityIter.hasNext()) {
					UriRef nextPriority = typePriorityIter.next();
					if (remaining.contains(nextPriority)) {
						remaining.remove(nextPriority);
						return nextPriority;
					}
				}
				if (remainingIter == null) {
					remainingIter = remaining.iterator();
				}
				if (remainingIter.hasNext()) {
					return remainingIter.next();
				} else {
					if (rdfsResourceRemovedAndNotYetReturned) {
						rdfsResourceRemovedAndNotYetReturned = false;
						return RDFS.Resource;
					} else {
						return null;
					}
				}
			}

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public UriRef next() {
				if (next == null) {
					throw new NoSuchElementException();
				}
				UriRef current = next;
				next = prepareNext();
				return current;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
	}
}
