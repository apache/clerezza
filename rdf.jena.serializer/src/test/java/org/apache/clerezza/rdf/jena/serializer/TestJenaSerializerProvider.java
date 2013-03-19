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
package org.apache.clerezza.rdf.jena.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.SerializingProvider;
import org.apache.clerezza.rdf.jena.facade.JenaGraph;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;

/**
 * Serializes a Graph to different formats
 * 
 * @author mir
 */
public class TestJenaSerializerProvider {

    private MGraph mGraph;

    private void initializeGraph() {
        mGraph = new SimpleMGraph();
        com.hp.hpl.jena.graph.Graph graph = new JenaGraph(mGraph);
        Model model = ModelFactory.createModelForGraph(graph);
        // create the resource
        // and add the properties cascading style
        String URI = "http://example.org/";
        model.createResource(URI + "A").addProperty(
                model.createProperty(URI + "B"), "C").addProperty(
                model.createProperty(URI + "D"),
                model.createResource().addProperty(
                        model.createProperty(URI + "E"), "F").addProperty(
                        model.createProperty(URI + "G"), "H"));
        mGraph.add(new TripleImpl(new UriRef("http://foo/bar"),
                new UriRef("http://foo/bar"),
                LiteralFactory.getInstance().createTypedLiteral("foo")));
        mGraph.add(new TripleImpl(new UriRef("http://foo/bar"),
                new UriRef("http://foo/bar"),
                LiteralFactory.getInstance().createTypedLiteral(54675)));
        mGraph.add(new TripleImpl(new BNode(),
                new UriRef("http://foo/bar"),
                new UriRef("http://foo/bar")));
    }

    /*
     * Serialize Graph to turtle format and deserialize.
     */
    @Test
    public void testTurtleSerializer() {
        initializeGraph();
        SerializingProvider provider = new JenaSerializerProvider();

        ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();
        provider.serialize(serializedGraph, mGraph.getGraph(),
                "text/turtle");
        InputStream in = new ByteArrayInputStream(serializedGraph.toByteArray());

        Graph deserializedGraph = parse(in, "TURTLE");
        // due to http://issues.trialox.org/jira/browse/RDF-6 we cannot just
        // check
        // that the two graphs are equals
        Assert.assertEquals(deserializedGraph.size(), mGraph.getGraph().size());
        Assert.assertEquals(deserializedGraph.hashCode(), mGraph.getGraph()
                .hashCode());
        // isomorphism delegated to jena
        JenaGraph jenaGraphFromNTriples = new JenaGraph(deserializedGraph);
        JenaGraph jenaGraphFromTurtle = new JenaGraph(mGraph.getGraph());
        Assert.assertTrue(jenaGraphFromNTriples
                .isIsomorphicWith(jenaGraphFromTurtle));
    }

    @Test
    public void testTurtleSerializerWithParam() {
        initializeGraph();
        SerializingProvider provider = new JenaSerializerProvider();

        ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();
        provider.serialize(serializedGraph, mGraph.getGraph(),
                "text/turtle;param=test");
        InputStream in = new ByteArrayInputStream(serializedGraph.toByteArray());

        Graph deserializedGraph = parse(in, "TURTLE");
        Assert.assertEquals(mGraph.getGraph(), deserializedGraph);

    }

    
    /*
     * Serialize Graph to rdf+xml format and deserialize.
     */
    @Test
    public void testRdfXmlSerializer() {
        initializeGraph();

        SerializingProvider provider = new JenaSerializerProvider();

        ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();
        provider.serialize(serializedGraph, mGraph.getGraph(),
                "application/rdf+xml");
        InputStream in = new ByteArrayInputStream(serializedGraph.toByteArray());

        Graph deserializedGraph = parse(in, "RDF/XML-ABBREV");
        Assert.assertEquals(mGraph.getGraph(), deserializedGraph);
    }

    /*
     * Serialize Graph to rdf+nt format and deserialize.
     */
    @Test
    public void testRdfNtSerializer() {
        initializeGraph();
        SerializingProvider provider = new JenaSerializerProvider();

        ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();
        provider.serialize(serializedGraph, mGraph.getGraph(), "text/rdf+nt");
        InputStream in = new ByteArrayInputStream(serializedGraph.toByteArray());

        Graph deserializedGraph = parse(in, "N-TRIPLE");
        Assert.assertEquals(mGraph.getGraph(), deserializedGraph);
    }

    /*
     * Serialize Graph to rdf+n3 format and deserialize.
     */
    @Test
    public void testRdfN3Serializer() {
        initializeGraph();
        SerializingProvider provider = new JenaSerializerProvider();

        ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();
        provider.serialize(serializedGraph, mGraph.getGraph(), "text/rdf+n3");
        InputStream in = new ByteArrayInputStream(serializedGraph.toByteArray());

        Graph deserializedGraph = parse(in, "N3");
        Assert.assertEquals(mGraph.getGraph(), deserializedGraph);
    }

    private Graph parse(InputStream serializedGraph, String jenaFormat) {
        MGraph mResult = new SimpleMGraph();
        com.hp.hpl.jena.graph.Graph graph = new JenaGraph(mResult);
        Model model = ModelFactory.createModelForGraph(graph);
        String base = "urn:x-relative:";
        model.read(serializedGraph, base, jenaFormat);
        return mResult.getGraph();
    }
}
