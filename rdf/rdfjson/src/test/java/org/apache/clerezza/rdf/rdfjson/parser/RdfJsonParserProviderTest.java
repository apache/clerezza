/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.clerezza.rdf.rdfjson.parser;

import java.io.InputStream;
import java.util.Iterator;
import org.apache.clerezza.commons.rdf.BlankNode;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;

/**
 * @author tio, hasan
 */
public class RdfJsonParserProviderTest {

    @Test
    public void testParsingOfObjectBlankNode() {
        ParsingProvider provider = new RdfJsonParsingProvider();
        InputStream jsonIn = getClass().getResourceAsStream("test-object-bnode.json");
        Graph parsedGraph = new SimpleGraph();
        provider.parse(parsedGraph, jsonIn, "application/rdf+json", null);
        Assert.assertEquals(parsedGraph.size(), 1);
        Iterator<Triple> triples = parsedGraph.filter(new IRI("http://example.org/node1"),
                new IRI("http://example.org/prop1"), null);
        Assert.assertTrue(triples.hasNext());
        Assert.assertTrue(triples.next().getObject() instanceof BlankNode);
    }

    @Test
    public void testParsingOfSubjectBlankNode() {
        ParsingProvider provider = new RdfJsonParsingProvider();
        InputStream jsonIn = getClass().getResourceAsStream("test-subject-bnode.json");
        Graph parsedGraph = new SimpleGraph();
        provider.parse(parsedGraph, jsonIn, "application/rdf+json", null);
        Assert.assertEquals(3, parsedGraph.size());
        Iterator<Triple> triples = parsedGraph.filter(null, new IRI("http://example.org/prop1"),
                new IRI("http://example.org/node1"));
        Assert.assertTrue(triples.hasNext());
        BlankNodeOrIRI subject = triples.next().getSubject();
        Assert.assertTrue(subject instanceof BlankNode);

        triples = parsedGraph.filter(null, new IRI("http://example.org/prop2"),
                new IRI("http://example.org/node2"));
        Assert.assertTrue(triples.hasNext());
        Assert.assertTrue(subject.equals(triples.next().getSubject()));

        triples = parsedGraph.filter(null, new IRI("http://example.org/prop3"),
                new IRI("http://example.org/node3"));
        Assert.assertTrue(triples.hasNext());
        Assert.assertFalse(subject.equals(triples.next().getSubject()));
    }

    @Test
    public void testParser() {
        ParsingProvider provider = new RdfJsonParsingProvider();
        InputStream jsonIn = getClass().getResourceAsStream("test.json");
        Graph deserializedGraph = new SimpleGraph();
        provider.parse(deserializedGraph, jsonIn, "application/rdf+json", null);
        Assert.assertEquals(deserializedGraph.size(), 6);
        Iterator<Triple> triples = deserializedGraph.filter(new IRI("http://base/child1"), null, null);
        while (triples.hasNext()) {
            IRI uri = triples.next().getPredicate();
            Assert.assertEquals(uri.getUnicodeString(), "http://base/propertyB");
        }
    }
}
