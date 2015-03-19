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
package org.apache.clerezza.rdf.utils;

import org.apache.commons.rdf.BlankNode;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.Iri;
import org.apache.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class IfpSmushTest {

    private Graph ontology = new SimpleGraph();
    {
        ontology.add(new TripleImpl(FOAF.mbox, RDF.type, OWL.InverseFunctionalProperty));
    }

    @Test
    public void simpleBlankNode()  {
        Graph mGraph = new SimpleGraph();
        Iri mbox1 = new Iri("mailto:foo@example.org");
        final BlankNode bNode1 = new BlankNode();
        mGraph.add(new TripleImpl(bNode1, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(bNode1, RDFS.comment, 
                new PlainLiteralImpl("a comment")));
        final BlankNode bNode2 = new BlankNode();
        mGraph.add(new TripleImpl(bNode2, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(bNode2, RDFS.comment, 
                new PlainLiteralImpl("another comment")));
        Smusher.smush(mGraph, ontology);
        Assert.assertEquals(3, mGraph.size());
    }

    @Test
    public void overlappingEquivalenceClasses()  {
        Graph mGraph = new SimpleGraph();
        Iri mbox1 = new Iri("mailto:foo@example.org");
        final BlankNode bNode1 = new BlankNode();
        mGraph.add(new TripleImpl(bNode1, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(bNode1, RDFS.comment,
                new PlainLiteralImpl("a comment")));
        final BlankNode bNode2 = new BlankNode();
        Iri mbox2 = new Iri("mailto:bar@example.org");
        mGraph.add(new TripleImpl(bNode2, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(bNode2, FOAF.mbox, mbox2));
        mGraph.add(new TripleImpl(bNode2, RDFS.comment,
                new PlainLiteralImpl("another comment")));
        final BlankNode bNode3 = new BlankNode();
        mGraph.add(new TripleImpl(bNode3, FOAF.mbox, mbox2));
        mGraph.add(new TripleImpl(bNode3, RDFS.comment,
                new PlainLiteralImpl("yet another comment")));
        Smusher.smush(mGraph, ontology);
        Assert.assertEquals(5, mGraph.size());
    }

    @Test
    public void oneIri()  {
        Graph mGraph = new SimpleGraph();
        Iri mbox1 = new Iri("mailto:foo@example.org");
        final Iri resource = new Iri("http://example.org/");
        mGraph.add(new TripleImpl(resource, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(resource, RDFS.comment,
                new PlainLiteralImpl("a comment")));
        final BlankNode bNode2 = new BlankNode();
        mGraph.add(new TripleImpl(bNode2, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(bNode2, RDFS.comment,
                new PlainLiteralImpl("another comment")));
        Smusher.smush(mGraph, ontology);
        Assert.assertEquals(3, mGraph.size());
    }

    @Test
    public void twoIris()  {
        Graph mGraph = new SimpleGraph();
        Iri mbox1 = new Iri("mailto:foo@example.org");
        final Iri resource1 = new Iri("http://example.org/");
        mGraph.add(new TripleImpl(resource1, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(resource1, RDFS.comment,
                new PlainLiteralImpl("a comment")));
        final Iri resource2 = new Iri("http://2.example.org/");
        mGraph.add(new TripleImpl(resource2, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(resource2, RDFS.comment,
                new PlainLiteralImpl("another comment")));
        Smusher.smush(mGraph, ontology);
        Assert.assertEquals(4, mGraph.size());
    }

}
