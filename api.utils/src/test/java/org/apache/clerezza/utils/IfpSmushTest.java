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
package org.apache.clerezza.utils;

import org.apache.clerezza.BlankNode;
import org.apache.clerezza.Graph;
import org.apache.clerezza.IRI;
import org.apache.clerezza.impl.TripleImpl;
import org.apache.clerezza.impl.graph.SimpleGraph;
import org.apache.clerezza.impl.literal.PlainLiteralImpl;
import org.apache.clerezza.ontologies.FOAF;
import org.apache.clerezza.ontologies.OWL;
import org.apache.clerezza.ontologies.RDF;
import org.apache.clerezza.ontologies.RDFS;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author reto
 */
public class IfpSmushTest {

    private Graph ontology = new SimpleGraph();

    {
        ontology.add(new TripleImpl(FOAF.mbox, RDF.type, OWL.InverseFunctionalProperty));
    }

    @Test
    public void simpleBlankNode() {
        Graph mGraph = new SimpleGraph();
        IRI mbox1 = new IRI("mailto:foo@example.org");
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
    public void overlappingEquivalenceClasses() {
        Graph mGraph = new SimpleGraph();
        IRI mbox1 = new IRI("mailto:foo@example.org");
        final BlankNode bNode1 = new BlankNode();
        mGraph.add(new TripleImpl(bNode1, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(bNode1, RDFS.comment,
                new PlainLiteralImpl("a comment")));
        final BlankNode bNode2 = new BlankNode();
        IRI mbox2 = new IRI("mailto:bar@example.org");
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
    public void oneIRI() {
        Graph mGraph = new SimpleGraph();
        IRI mbox1 = new IRI("mailto:foo@example.org");
        final IRI resource = new IRI("http://example.org/");
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
    public void twoIRIs() {
        Graph mGraph = new SimpleGraph();
        IRI mbox1 = new IRI("mailto:foo@example.org");
        final IRI resource1 = new IRI("http://example.org/");
        mGraph.add(new TripleImpl(resource1, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(resource1, RDFS.comment,
                new PlainLiteralImpl("a comment")));
        final IRI resource2 = new IRI("http://2.example.org/");
        mGraph.add(new TripleImpl(resource2, FOAF.mbox, mbox1));
        mGraph.add(new TripleImpl(resource2, RDFS.comment,
                new PlainLiteralImpl("another comment")));
        Smusher.smush(mGraph, ontology);
        Assert.assertEquals(4, mGraph.size());
    }

}
