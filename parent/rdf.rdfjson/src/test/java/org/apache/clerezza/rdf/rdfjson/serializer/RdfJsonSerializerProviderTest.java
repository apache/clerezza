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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SerializingProvider;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
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

    private final static LiteralFactory lf = LiteralFactory.getInstance();
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
	private final static TypedLiteral typedLiteralA = lf.createTypedLiteral("A");

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
//        System.out.println(serializedGraph.toString());
		ParsingProvider parsingProvider = new RdfJsonParsingProvider();
		ByteArrayInputStream jsonIn = new ByteArrayInputStream(serializedGraph.toByteArray());
		MGraph parsedMGraph = new SimpleMGraph();
		parsingProvider.parse(parsedMGraph, jsonIn, "application/rdf+json", null);

		Assert.assertEquals(6, parsedMGraph.size());
		Assert.assertEquals(mGraph.getGraph(), parsedMGraph.getGraph());
	}
	
	/**
	 * For local performance testing
	 */
	@Test
	public void testBigGraph() throws Exception {
		//reduced Graph size to 5000 to allow equals test between the
		//serialised and parsed RDF graphs. Equals tests on bigger graphs
		//would take to much time
		int NUM_TRIPLES = 5000;
		//randoms are in the range [0..3]
		double l = 1.0; //literal
		double i = l / 3; //int
		double d = l * 2 / 3;//double
		double b = 2.0;//bNode
		double nb = b - (l * 2 / 3); //create new bNode
		double random;
		NonLiteral subject = null;
		UriRef predicate = null;
		List<UriRef> predicateList = new ArrayList<UriRef>();
		predicateList.add(RDF.first);
		predicateList.add(RDF.rest);
		predicateList.add(RDF.type);
		predicateList.add(RDFS.label);
		predicateList.add(RDFS.comment);
		predicateList.add(RDFS.range);
		predicateList.add(RDFS.domain);
		predicateList.add(FOAF.name);
		predicateList.add(FOAF.nick);
		predicateList.add(FOAF.homepage);
		predicateList.add(FOAF.age);
		predicateList.add(FOAF.depiction);
		String URI_PREFIX = "http://www.test.org/bigGraph/ref";
		Language DE = new Language("de");
		Language EN = new Language("en");
		Iterator<UriRef> predicates = predicateList.iterator();
		List<BNode> bNodes = new ArrayList<BNode>();
		bNodes.add(new BNode());
		for (int count = 0; mGraph.size() < NUM_TRIPLES; count++) {
			random = Math.random() * 3;
			if (random >= 2.5 || count == 0) {
				if (random <= 2.75) {
					subject = new UriRef(URI_PREFIX + count);
				} else {
					int rndIndex = (int) ((random - 2.75) * bNodes.size() / (3.0 - 2.75));
					subject = bNodes.get(rndIndex);
				}
			}
			if (random > 2.0 || count == 0) {
				if (!predicates.hasNext()) {
					Collections.shuffle(predicateList);
					predicates = predicateList.iterator();
				}
				predicate = predicates.next();
			}
			if (random <= l) { //literal
				if (random <= i) {
					mGraph.add(new TripleImpl(subject, predicate, lf.createTypedLiteral(count)));
				} else if (random <= d) {
					mGraph.add(new TripleImpl(subject, predicate, lf.createTypedLiteral(random)));
				} else {
					PlainLiteral text;
					if (random <= i) {
						text = new PlainLiteralImpl("Literal for " + count);
					} else if (random <= d) {
						text = new PlainLiteralImpl("An English literal for " + count, EN);
					} else {
						text = new PlainLiteralImpl("Ein Dutsches Literal für " + count, DE);
					}
					mGraph.add(new TripleImpl(subject, predicate, text));
				}
			} else if (random <= b) { //bnode
				BNode bnode;
				if (random <= nb) {
					bnode = new BNode();
					bNodes.add(bnode);
				} else { //>nb <b
					int rndIndex = (int) ((random - nb) * bNodes.size() / (b - nb));
					bnode = bNodes.get(rndIndex);
				}
				mGraph.add(new TripleImpl(subject, predicate, bnode));
			} else { //UriRef
				mGraph.add(new TripleImpl(subject, predicate,
						new UriRef(URI_PREFIX + (int) count * random)));
			}
		}
		//Asserts the correct sorting of the triples in the graph by the
		//Comparator used by the JSON serializer
        Set<NonLiteral> subjects = new HashSet<NonLiteral>();
		Triple[] sortedTriples = mGraph.toArray(new Triple[mGraph.size()]);
        Arrays.sort(sortedTriples, RdfJsonSerializingProvider.SUBJECT_COMPARATOR);
        NonLiteral current = sortedTriples[0].getSubject();
        for(Triple triple : sortedTriples){
            if(!triple.getSubject().equals(current)){
                subjects.add(current);
                current = triple.getSubject();
                Assert.assertFalse(subjects.contains(current));
            }
        }
        sortedTriples = null;
        subjects = null;
        
		int originalSize = mGraph.size();
		
		SerializingProvider provider = new RdfJsonSerializingProvider();
		ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();
		long start = System.currentTimeMillis();
		provider.serialize(serializedGraph, mGraph, "application/rdf+json");
		System.out.println("Serialized " + mGraph.size() + " Triples in " + (System.currentTimeMillis() - start) + " ms");
        ParsingProvider parsingProvider = new RdfJsonParsingProvider();
        ByteArrayInputStream jsonIn = new ByteArrayInputStream(serializedGraph.toByteArray());
        MGraph parsedMGraph = new SimpleMGraph();
        parsingProvider.parse(parsedMGraph, jsonIn, "application/rdf+json", null);
        Assert.assertEquals(originalSize, parsedMGraph.size());
        sortedTriples = parsedMGraph.toArray(new Triple[parsedMGraph.size()]);
        Arrays.sort(sortedTriples, RdfJsonSerializingProvider.SUBJECT_COMPARATOR);
        Assert.assertEquals(mGraph.getGraph(), parsedMGraph.getGraph());
	}
}
