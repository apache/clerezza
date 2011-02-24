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
package org.apache.clerezza.rdf.rdfjson.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SerializingProvider;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.rdfjson.parser.RdfJsonParsingProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * 
 * @author tio, hasan
 */
public class RdfJsonSerializerProviderTest {

	private final static UriRef RDF_NIL = new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
	private final static UriRef node1 = new UriRef("http://example.org/node1");
	private final static UriRef node2 = new UriRef("http://example.org/node2");
	private final static UriRef prop1 = new UriRef("http://example.org/prop1");
	private final static UriRef prop2 = new UriRef("http://example.org/prop2");
	private final static UriRef prop3 = new UriRef("http://example.org/prop3");
	private final static UriRef prop4 = new UriRef("http://example.org/prop4");
	private final static UriRef prop5 = new UriRef("http://example.org/prop5");
	private final static UriRef prop6 = new UriRef("http://example.org/prop6");
	private final static BNode blank1 = new BNode();
	private final static BNode blank2 = new BNode();
	private final static PlainLiteralImpl plainLiteralA = new PlainLiteralImpl("A");
	private final static PlainLiteralImpl plainLiteralB = new PlainLiteralImpl("B");
	private final static PlainLiteralImpl plainLiteralC = new PlainLiteralImpl("C");
	private final static TypedLiteral typedLiteralA = LiteralFactory.getInstance().createTypedLiteral("A");

	private MGraph mGraph;

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

    @Before
    public void setUp() {
		mGraph = new SimpleMGraph();
    }

    @After
    public void tearDown() {
    }

	@Test
	public void testSerializationOfBNode() {
		mGraph.add(new TripleImpl(node1, prop1, blank1));
		SerializingProvider provider = new RdfJsonSerializingProvider();
		ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();
		provider.serialize(serializedGraph, mGraph, "application/rdf+json");
		Assert.assertTrue(serializedGraph.toString().contains("_:"));
	}

	@Test
	public void testSerializationOfRdfList() {
		mGraph.add(new TripleImpl(blank1, RDF.first, blank2));
		mGraph.add(new TripleImpl(blank1, RDF.rest, RDF_NIL));
		mGraph.add(new TripleImpl(blank2, prop1, node1));

//		System.out.println(mGraph);

		SerializingProvider provider = new RdfJsonSerializingProvider();
		ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();
		provider.serialize(serializedGraph, mGraph, "application/rdf+json");

//		System.out.println(serializedGraph.toString());

		ParsingProvider parsingProvider = new RdfJsonParsingProvider();
		ByteArrayInputStream jsonIn = new ByteArrayInputStream(serializedGraph.toByteArray());
		MGraph parsedMGraph = new SimpleMGraph();
		parsingProvider.parse(parsedMGraph, jsonIn, "application/rdf+json", null);

		Assert.assertEquals(mGraph.getGraph(), parsedMGraph.getGraph());
	}

	/*
	 * serializes a graph and parse it back.
	 */
	@Test
	public void testSerializer() {
		mGraph.add(new TripleImpl(node1, prop1, plainLiteralA));
		mGraph.add(new TripleImpl(node1, prop2, node2));
		mGraph.add(new TripleImpl(node2, prop3, plainLiteralB));
		mGraph.add(new TripleImpl(blank1, prop4, plainLiteralC));
		mGraph.add(new TripleImpl(blank1, prop5, typedLiteralA));
		mGraph.add(new TripleImpl(node1, prop6, blank1));

		SerializingProvider provider = new RdfJsonSerializingProvider();
		ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();
		provider.serialize(serializedGraph, mGraph, "application/rdf+json");
		ParsingProvider parsingProvider = new RdfJsonParsingProvider();
		ByteArrayInputStream jsonIn = new ByteArrayInputStream(serializedGraph.toByteArray());
		MGraph parsedMGraph = new SimpleMGraph();
		parsingProvider.parse(parsedMGraph, jsonIn, "application/rdf+json", null);

		Assert.assertEquals(6, parsedMGraph.size());
		Assert.assertEquals(mGraph.getGraph(), parsedMGraph.getGraph());
	}
}
