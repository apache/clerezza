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
package org.apache.clerezza.rdf.facade.blackbox;


import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType;
import com.hp.hpl.jena.rdf.model.Literal;
import junit.framework.Assert;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RSIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.io.StringWriter;
import org.junit.Test;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.jena.facade.JenaGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;

/**
 *
 * @author reto
 */
public class TestBasics {
    
    @Test
    public void serializeGraph() {
        final String uriString = "http://example.org/foo#bar";
        IRI uri = new IRI(uriString);
        Graph mGraph = new SimpleGraph();
        mGraph.add(new TripleImpl(uri, uri, new PlainLiteralImpl("bla bla")));
        com.hp.hpl.jena.graph.Graph graph = new JenaGraph(mGraph);
        Model model = ModelFactory.createModelForGraph(graph);
        StringWriter writer = new StringWriter();
        model.write(writer);
        Assert.assertTrue(writer.toString().contains("about=\""+uriString));
    }
    
    @Test
    public void graphSize() {
        IRI uri = new IRI("http://example.org/foo#bar");
        Graph mGraph = new SimpleGraph();
        mGraph.add(new TripleImpl(uri, uri, new PlainLiteralImpl("bla bla")));
        com.hp.hpl.jena.graph.Graph graph = new JenaGraph(mGraph);
        Assert.assertEquals(1, graph.size());
    }

    @Test
    public void modifyingJenaGraph() {
        Graph mGraph = new SimpleGraph();
        com.hp.hpl.jena.graph.Graph graph = new JenaGraph(mGraph);
        Model model = ModelFactory.createModelForGraph(graph);
        model.add(RDFS.Class, RDF.type, RDFS.Class);
        Assert.assertEquals(1, mGraph.size());
    }
    
    @Test
    public void typedLiterals() {
        Graph mGraph = new SimpleGraph();
        com.hp.hpl.jena.graph.Graph graph = new JenaGraph(mGraph);
        Model model = ModelFactory.createModelForGraph(graph);
        Literal typedLiteral = model.createTypedLiteral("<elem>foo</elem>", XMLLiteralType.theXMLLiteralType);
        model.add(RDFS.Class, RDFS.label, typedLiteral);
        Assert.assertEquals(1, mGraph.size());
        StmtIterator iter = model.listStatements(RDFS.Class, RDFS.label, (RDFNode)null);
        Assert.assertTrue(iter.hasNext());
        RDFNode gotObject = iter.nextStatement().getObject();
        Assert.assertEquals(typedLiteral, gotObject);
    }
    
    @Test
    public void reifications() {
        Graph mGraph = new SimpleGraph();
        com.hp.hpl.jena.graph.Graph graph = new JenaGraph(mGraph);
        //Model memModel = ModelFactory.createDefaultModel();
        Model model = ModelFactory.createModelForGraph(graph);
        model.add(RDFS.Resource, RDF.type, RDFS.Resource);
        Resource bnode = model.createResource();
        model.add(bnode, RDF.type, RDF.Statement);
        model.add(bnode, RDF.subject, RDFS.Resource);
        model.add(bnode, RDF.predicate, RDF.type);
        model.add(bnode, RDF.object, RDFS.Resource);
        model.add(bnode, RDFS.comment, "we knew that before");
        StmtIterator stmts = model.listStatements(RDFS.Resource, null, (RDFNode)null);
        Statement returnedStmt = stmts.nextStatement();
        RSIterator rsIterator = returnedStmt.listReifiedStatements();
        Assert.assertTrue("got back reified statement", rsIterator.hasNext());
        //recreating jena-graph
        graph = new JenaGraph(mGraph);
        model = ModelFactory.createModelForGraph(graph);
        stmts = model.listStatements(RDFS.Resource, null, (RDFNode)null);
        returnedStmt = stmts.nextStatement();
        rsIterator = returnedStmt.listReifiedStatements();
        Assert.assertTrue("got back reified statement on recreated graph",
                rsIterator.hasNext());
    }

}
