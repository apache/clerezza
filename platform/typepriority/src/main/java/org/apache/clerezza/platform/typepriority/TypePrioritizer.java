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
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.commons.rdf.RdfTerm;
import org.apache.clerezza.commons.rdf.Iri;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.commons.rdf.Graph;
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
        referenceInterface=Graph.class,
        target=SystemConfig.SYSTEM_GRAPH_FILTER)})
public class TypePrioritizer {
    public static final Iri typePriorityListUri = new Iri("urn:x-localinstance:/typePriorityList");

    private List<Iri> typePriorityList;
    private static final Logger log = LoggerFactory.getLogger(TypePrioritizer.class);
    
    Graph systemGraph;

    protected void bindSystemGraph(Graph systemGraph) {
        Lock l = systemGraph.getLock().readLock();
        l.lock();
        try {
            List<RdfTerm> rdfTypePriorityList = new RdfList(
                 typePriorityListUri, systemGraph);
            typePriorityList  = new ArrayList<Iri>(rdfTypePriorityList.size());
            for (RdfTerm resource : rdfTypePriorityList) {
                if (resource instanceof Iri) {
                    typePriorityList.add((Iri) resource);
                } else {
                    log.warn("Type priority list contains a resource "
                            + "that is not a uri, skipping.");
                }
            }
        } finally {
            l.unlock();
        }
        this.systemGraph = (Graph) systemGraph;
    }

    protected void unbindSystemGraph(Graph systemGraph) {
        typePriorityList = null;
        this.systemGraph = null;
    }

    /**
     *
     * @param rdfTypes the rdf types to be sorted
     * @return a sorted iterator of the types
     */
    public Iterator<Iri> iterate(final Collection<Iri> rdfTypes) {
        return new Iterator<Iri>() {
            final Set<Iri> remaining = new HashSet<Iri>(rdfTypes);
            boolean rdfsResourceRemovedAndNotYetReturned = remaining.remove(RDFS.Resource);
            final Iterator<Iri> typePriorityIter = typePriorityList.iterator();
            Iterator<Iri> remainingIter = null;
            Iri next = prepareNext();
            
            private Iri prepareNext() {
                while (typePriorityIter.hasNext()) {
                    Iri nextPriority = typePriorityIter.next();
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
            public Iri next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                Iri current = next;
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
