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
import org.apache.clerezza.rdf.core.BNode;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;

/**
 * @author tio, hasan
 */
public class RdfJsonParserProviderTest {

	@Test
	public void testParsingOfObjectBNode() {
		ParsingProvider provider = new RdfJsonParsingProvider();
		InputStream jsonIn = getClass().getResourceAsStream("test-object-bnode.json");
		MGraph parsedMGraph = new SimpleMGraph();
		provider.parse(parsedMGraph, jsonIn, "application/rdf+json", null);
		Assert.assertEquals(parsedMGraph.size(), 1);
		Iterator<Triple> triples = parsedMGraph.filter(new UriRef("http://example.org/node1"),
				new UriRef("http://example.org/prop1"), null);
		Assert.assertTrue(triples.hasNext());
		Assert.assertTrue(triples.next().getObject() instanceof BNode);
	}

	@Test
	public void testParsingOfSubjectBNode() {
		ParsingProvider provider = new RdfJsonParsingProvider();
		InputStream jsonIn = getClass().getResourceAsStream("test-subject-bnode.json");
		MGraph parsedMGraph = new SimpleMGraph();
		provider.parse(parsedMGraph, jsonIn, "application/rdf+json", null);
		Assert.assertEquals(3, parsedMGraph.size());
		Iterator<Triple> triples = parsedMGraph.filter(null, new UriRef("http://example.org/prop1"),
				new UriRef("http://example.org/node1"));
		Assert.assertTrue(triples.hasNext());
		NonLiteral subject = triples.next().getSubject();
		Assert.assertTrue(subject instanceof BNode);

		triples = parsedMGraph.filter(null, new UriRef("http://example.org/prop2"),
				new UriRef("http://example.org/node2"));
		Assert.assertTrue(triples.hasNext());
		Assert.assertTrue(subject.equals(triples.next().getSubject()));

		triples = parsedMGraph.filter(null, new UriRef("http://example.org/prop3"),
				new UriRef("http://example.org/node3"));
		Assert.assertTrue(triples.hasNext());
		Assert.assertFalse(subject.equals(triples.next().getSubject()));
	}

	@Test
	public void testParser() {
		ParsingProvider provider = new RdfJsonParsingProvider();
		InputStream jsonIn = getClass().getResourceAsStream("test.json");
		MGraph deserializedMGraph = new SimpleMGraph();
		provider.parse(deserializedMGraph, jsonIn, "application/rdf+json", null);
		Assert.assertEquals(deserializedMGraph.size(), 6);
		Iterator<Triple> triples = deserializedMGraph.filter(new UriRef("http://base/child1"), null, null);
		while (triples.hasNext()) {
			UriRef uri = triples.next().getPredicate();
			Assert.assertEquals(uri.getUnicodeString(), "http://base/propertyB");
		}
	}
}
