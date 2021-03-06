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
package org.apache.clerezza.utils.smushing;

import org.apache.clerezza.*;
import org.apache.clerezza.implementation.TripleImpl;
import org.apache.clerezza.implementation.in_memory.SimpleGraph;
import org.apache.clerezza.ontologies.OWL;

import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * @author Reto
 */
public class BaseSmusher {

    /**
     * Smushes the resources in mGraph that belong to the same set in equivalenceSets,
     * i.e. it adds all properties to one of the resources in the equivalence set.
     * <p>
     * Optionally owl:sameAs statement are added that point from the IRIs that
     * no longer have properties to the one with properties. If addOwlSameAs
     * is false the IRIs will just disappear from the graph.
     *
     * @param mGraph          the graph to smush
     * @param equivalenceSets sets of equivalent resources
     * @param addOwlSameAs    whether owl:sameAs statements should be added
     */
    public void smush(Graph mGraph, Set<Set<BlankNodeOrIRI>> equivalenceSets, boolean addOwlSameAs) {
        Map<BlankNodeOrIRI, BlankNodeOrIRI> current2ReplacementMap = new HashMap<BlankNodeOrIRI, BlankNodeOrIRI>();
        final Graph owlSameAsGraph = new SimpleGraph();
        for (Set<BlankNodeOrIRI> equivalenceSet : equivalenceSets) {
            final BlankNodeOrIRI replacement = getReplacementFor(equivalenceSet, owlSameAsGraph);
            for (BlankNodeOrIRI current : equivalenceSet) {
                if (!current.equals(replacement)) {
                    current2ReplacementMap.put(current, replacement);
                }
            }
        }
        final Set<Triple> newTriples = new HashSet<Triple>();
        Lock l = mGraph.getLock().writeLock();
        l.lock();
        try {
            for (Iterator<Triple> it = mGraph.iterator(); it.hasNext(); ) {
                final Triple triple = it.next();
                final BlankNodeOrIRI subject = triple.getSubject();
                BlankNodeOrIRI subjectReplacement = current2ReplacementMap.get(subject);
                final RDFTerm object = triple.getObject();
                @SuppressWarnings(value = "element-type-mismatch")
                RDFTerm objectReplacement = current2ReplacementMap.get(object);
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

    private BlankNodeOrIRI getReplacementFor(Set<BlankNodeOrIRI> equivalenceSet,
                                             Graph owlSameAsGraph) {
        final Set<IRI> uriRefs = new HashSet<IRI>();
        for (BlankNodeOrIRI nonLiteral : equivalenceSet) {
            if (nonLiteral instanceof IRI) {
                uriRefs.add((IRI) nonLiteral);
            }
        }
        switch (uriRefs.size()) {
            case 1:
                return uriRefs.iterator().next();
            case 0:
                return new BlankNode();
        }
        final IRI preferedIRI = getPreferedIRI(uriRefs);
        final Iterator<IRI> uriRefIter = uriRefs.iterator();
        while (uriRefIter.hasNext()) {
            IRI uriRef = uriRefIter.next();
            if (!uriRef.equals(preferedIRI)) {
                owlSameAsGraph.add(new TripleImpl(uriRef, OWL.sameAs, preferedIRI));
            }
        }
        return preferedIRI;
    }


    /**
     * Returns a prefered IRI for the IRIs in a set. Typically and in the
     * default implementation the IRI will be one of the set. Note however that
     * subclass implementations may also return another IRI to be used.
     *
     * @param uriRefs
     * @return
     */
    protected IRI getPreferedIRI(Set<IRI> uriRefs) {
        final Iterator<IRI> uriRefIter = uriRefs.iterator();
        //instead of an arbitrary one we might either decide lexicographically
        //or look at their frequency in mGraph
        return uriRefIter.next();
    }

}
