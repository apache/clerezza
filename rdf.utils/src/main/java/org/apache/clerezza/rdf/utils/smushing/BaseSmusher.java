/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.clerezza.rdf.utils.smushing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.OWL;

/**
 *
 * @author Reto
 */
public class BaseSmusher {

    /**
     * Smushes the resources in mGraph that belong to the same set in equivalenceSets,
     * i.e. it adds all properties to one of the resources in the equivalence set.
     * 
     * Optionally owl:sameAs statement are added that point from the IRIs that 
     * no longer have properties to the one with properties. If addOwlSameAs
     * is false the IRIs will just disappear from the graph.
     * 
     * @param mGraph the graph to smush
     * @param equivalenceSets sets of equivalent resources
     * @param addOwlSameAs whether owl:sameAs statements should be added
     */
    public void smush(LockableMGraph mGraph, Set<Set<NonLiteral>> equivalenceSets, boolean addOwlSameAs) {
        Map<NonLiteral, NonLiteral> current2ReplacementMap = new HashMap<NonLiteral, NonLiteral>();
        final MGraph owlSameAsGraph = new SimpleMGraph();
        for (Set<NonLiteral> equivalenceSet : equivalenceSets) {
            final NonLiteral replacement = getReplacementFor(equivalenceSet, owlSameAsGraph);
            for (NonLiteral current : equivalenceSet) {
                if (!current.equals(replacement)) {
                    current2ReplacementMap.put(current, replacement);
                }
            }
        }
        final Set<Triple> newTriples = new HashSet<Triple>();
        Lock l = mGraph.getLock().writeLock();
        l.lock();
        try {
            for (Iterator<Triple> it = mGraph.iterator(); it.hasNext();) {
                final Triple triple = it.next();
                final NonLiteral subject = triple.getSubject();
                NonLiteral subjectReplacement = current2ReplacementMap.get(subject);
                final Resource object = triple.getObject();
                @SuppressWarnings(value = "element-type-mismatch")
                Resource objectReplacement = current2ReplacementMap.get(object);
                if ((subjectReplacement != null) || (objectReplacement != null)) {
                    it.remove();
                    if (subjectReplacement == null) {
                        subjectReplacement = subject;
                    }
                    if (objectReplacement == null) {
                        objectReplacement = object;
                    }
                    newTriples.add(new TripleImpl(subjectReplacement, triple.getPredicate(), objectReplacement));
                }
            }
            for (Triple triple : newTriples) {
                mGraph.add(triple);
            }
            mGraph.addAll(owlSameAsGraph);
        } finally {
            l.unlock();
        }
    }
    
    private NonLiteral getReplacementFor(Set<NonLiteral> equivalenceSet, 
            MGraph owlSameAsGraph) {
        final Set<UriRef> uriRefs = new HashSet<UriRef>();
        for (NonLiteral nonLiteral : equivalenceSet) {
            if (nonLiteral instanceof UriRef) {
                uriRefs.add((UriRef) nonLiteral);
            }
        }
        switch (uriRefs.size()) {
            case 1:
                return uriRefs.iterator().next();
            case 0:
                return new BNode();
        }
        final UriRef preferedIri = getPreferedIri(uriRefs);
        final Iterator<UriRef> uriRefIter = uriRefs.iterator();
        while (uriRefIter.hasNext()) {
            UriRef uriRef = uriRefIter.next();
            if (!uriRef.equals(preferedIri)) {
                owlSameAsGraph.add(new TripleImpl(uriRef, OWL.sameAs, preferedIri));
            }
        }
        return preferedIri;
    }

    
    /**
     * Returns a prefered IRI for the IRIs in a set. Typically and in the
     * default implementation the IRI will be one of the set. Note however that 
     * subclass implementations may also return another IRI to be used.
     * 
     * @param uriRefs
     * @return 
     */
    protected UriRef getPreferedIri(Set<UriRef> uriRefs) {
        final Iterator<UriRef> uriRefIter = uriRefs.iterator();
        //instead of an arbitrary one we might either decide lexicographically
        //or look at their frequency in mGraph
        return uriRefIter.next();
    }
    
}
