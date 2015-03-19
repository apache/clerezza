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
package org.apache.clerezza.rdf.core.test;

import java.util.Iterator;
import java.util.UUID;
import org.apache.commons.rdf.BlankNode;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.BlankNodeOrIri;
import org.apache.commons.rdf.RdfTerm;
import org.apache.commons.rdf.Triple;
import org.apache.commons.rdf.Iri;
import org.apache.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.commons.rdf.impl.utils.TripleImpl;
import org.apache.commons.lang.RandomStringUtils;

/**
 * A <code>Graph</code> wrapper that allows growing and shrinking of
 * the wrapped mgraph.
 *
 * @author mir
 */
public class RandomGraph extends GraphWrapper {
    
    private int interconnectivity = 2;

    public RandomGraph(Graph mGraph, int interconnectivity) {
        super(mGraph);
        this.interconnectivity = interconnectivity;
    }

    /**
     * Creates a new random mutual graph.
     *
     * @param initialSize Determines the initial size of the content graph
     * @param interconnectivity Determines the probability of using already existing
     *        resource when creating a new triple. The probability of using an existing
     *        resource over creating a new resouce is 1-(1/interconnectivity).
     * @param mGraph
     */
    public RandomGraph(int initialSize, int interconnectivity, Graph mGraph) {
        super(mGraph);
        if (interconnectivity <= 0) {
            throw new IllegalArgumentException("growth speed and the interconnectivity "
                    + "value have to be equals or highter one");
        }
        this.interconnectivity = interconnectivity;

        setupInitialSize(initialSize);
    }

    /**
     * Add or removes randomly a triple.
     *
     * @return the triple that was added or removed.
     */
    public Triple evolve() {
        Triple triple;
        int random = rollDice(2);
        if (random == 0 && size() != 0) {
            triple = getRandomTriple();
            remove(triple);
        } else {
            triple = createRandomTriple();
            add(triple);
        }
        return triple;
    }

    /**
     * Removes a random triple.
     *
     * @return the triple that was removed.
     */
    public Triple removeRandomTriple() {
        Triple randomTriple = getRandomTriple();
        remove(randomTriple);
        return randomTriple;
    }

    /**
     * Adds a random triple.
     *
     * @return the triple that was added.
     */
    public Triple addRandomTriple() {
        Triple randomTriple;
        do {
         randomTriple = createRandomTriple();
        } while(contains(randomTriple));
        
        add(randomTriple);
        return randomTriple;
    }
    
    private Triple createRandomTriple() {
        return new TripleImpl(getSubject(), getPredicate(), getObject());
    }

    private BlankNodeOrIri getSubject() {
        int random = rollDice(interconnectivity);
        if (size() == 0) {
            random = 0;
        }
        switch (random) {
            case 0: // create new BlankNodeOrIri
                RdfTerm newRdfTerm;
                do {
                    newRdfTerm = createRandomRdfTerm();
                } while (!(newRdfTerm instanceof BlankNodeOrIri));
                return (BlankNodeOrIri) newRdfTerm;
            default: // get existing BlankNodeOrIri
                RdfTerm existingRdfTerm;
                do {
                    existingRdfTerm = getExistingRdfTerm();
                    if (existingRdfTerm == null) {
                        random = 0;
                    }
                } while (!(existingRdfTerm instanceof BlankNodeOrIri));

                return (BlankNodeOrIri) existingRdfTerm;
        }
    }

    private Iri getPredicate() {
        int random = rollDice(interconnectivity);
        if (size() == 0) {
            random = 0;
        }
        switch (random) {
            case 0: // create new Iri
                return createRandomIri();
            default: // get existing Iri
                RdfTerm existingRdfTerm;
                do {
                    existingRdfTerm = getExistingRdfTerm();
                    if (existingRdfTerm == null) {
                        random = 0;
                    }
                } while (!(existingRdfTerm instanceof Iri));
                return (Iri) existingRdfTerm;
        }
    }

    private RdfTerm getObject() {
        int random = rollDice(interconnectivity);
        if (size() == 0) {
            random = 0;
        }        
        switch (random) {
            case 0: // create new resource
                return createRandomRdfTerm();
            default: // get existing resource
                RdfTerm existingRdfTerm = getExistingRdfTerm();
                if (existingRdfTerm == null) {
                    random = 0;
                }
                return existingRdfTerm;
        }
    }

    private static int rollDice(int faces) {
        return Double.valueOf(Math.random() * faces).intValue();
    }

    private RdfTerm createRandomRdfTerm() {
        switch (rollDice(3)) {
            case 0:
                return new BlankNode();
            case 1:
                return createRandomIri();
            case 2:
                return new PlainLiteralImpl(RandomStringUtils.random(rollDice(100) + 1));
        }
        throw new RuntimeException("in createRandomRdfTerm()");
    }

    private RdfTerm getExistingRdfTerm() {
        Triple triple = getRandomTriple();
        if (triple == null) {
            return null;
        }
        switch (rollDice(3)) {
            case 0:
                return triple.getSubject();
            case 1:
                return triple.getPredicate();
            case 2:
                return triple.getObject();
        }
        return null;
    }

    private Iri createRandomIri() {
        return new Iri("http://" + UUID.randomUUID().toString());
    }

    /**
     * Returns a random triple contained in the Graph.
     */
    public Triple getRandomTriple() {
        int size = this.size();
        if (size == 0) {
            return null;
        }
        Iterator<Triple> triples = iterator();
        while (triples.hasNext()) {
            Triple triple = triples.next();
            if (rollDice(this.size()) == 0) {
                return triple;
            }
        }
        return getRandomTriple();
    }

    private void setupInitialSize(int initialSize) {
        for (int i = 0; i < initialSize; i++) {
            addRandomTriple();
        }
    }
}
