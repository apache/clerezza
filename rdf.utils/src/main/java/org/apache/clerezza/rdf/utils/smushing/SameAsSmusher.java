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
package org.apache.clerezza.rdf.utils.smushing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility to equate duplicate nodes in an Mgraph. This unifies owl:sameAs
 * resources.
 *
 * @author reto
 */
public class SameAsSmusher extends BaseSmusher {
    
    static final Logger log = LoggerFactory.getLogger(SameAsSmusher.class);
    
    /**
     * This will ensure that all properties of sameAs resources are associated
     * to the preferedIri as returned by {@code getPreferedIri}
     * @param mGraph
     * @param owlSameStatements 
     * @param addCanonicalSameAsStatements if true owl:sameAsStatements with the preferedIri as object will be added
     */
    public void smush(LockableMGraph mGraph, 
            TripleCollection owlSameStatements,
            boolean addCanonicalSameAsStatements) {
    	
    	log.info("Starting smushing");
        
    	// This hashmap contains a uri (key) and the set of equivalent uris (value)
    	final Map<NonLiteral, Set<NonLiteral>> node2EquivalenceSet = new HashMap<NonLiteral, Set<NonLiteral>>();
    	
    	log.info("Creating the sets of equivalent uris of each subject or object in the owl:sameAs statements");
    	// Determines for each subject and object in all the owl:sameAs statements the set of ewquivalent uris 
    	for (Iterator<Triple> it = owlSameStatements.iterator(); it.hasNext();) {            
    		final Triple triple = it.next();
            final UriRef predicate = triple.getPredicate();
            if (!predicate.equals(OWL.sameAs)) {
                throw new RuntimeException("Statements must use only <http://www.w3.org/2002/07/owl#sameAs> predicate.");
            }
            final NonLiteral subject = triple.getSubject();
            //literals not yet supported
            final NonLiteral object = (NonLiteral)triple.getObject();
            
            Set<NonLiteral> equivalentNodes = node2EquivalenceSet.get(subject);
            
            // if there is not a set of equivalent uris then create a new set
            if (equivalentNodes == null) {
            	equivalentNodes = node2EquivalenceSet.get(object);
            	if (equivalentNodes == null) {
                    equivalentNodes = new HashSet<NonLiteral>();
                }
            } else {
                Set<NonLiteral> objectSet = node2EquivalenceSet.get(object);
                if (object != null) {
                    //merge two sets
                    for (NonLiteral res : objectSet) {
                        node2EquivalenceSet.remove(res);
                    }
                    for (NonLiteral res : objectSet) {
                        node2EquivalenceSet.put(res,equivalentNodes);
                    }
                    equivalentNodes.addAll(objectSet);
                }
            }
            
            // add both subject and object of the owl:sameAs statement to the set of equivalent uris
            equivalentNodes.add(subject);
            equivalentNodes.add(object);
            
            // use both uris in the owl:sameAs statement as keys for the set of equivalent uris
            node2EquivalenceSet.put(subject, equivalentNodes);
            node2EquivalenceSet.put(object, equivalentNodes);
            
            log.info("Sets of equivalent uris created.");
        
    	}
    	
    	// This set contains the sets of equivalent uris
    	Set<Set<NonLiteral>> unitedEquivalenceSets = new HashSet<Set<NonLiteral>>(node2EquivalenceSet.values());
        smush(mGraph, unitedEquivalenceSets, addCanonicalSameAsStatements);
    }

    
}
