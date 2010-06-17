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
package org.apache.clerezza.rdf.rdfjson.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SerializingProvider;
import org.apache.clerezza.rdf.rdfjson.parser.RdfJsonParsingProvider;

/**
 * Serializes a Graph to rdf+json
 * 
 * see http://n2.talis.com/wiki/RDF_JSON_Specification
 * 
 * @author tio
 */
public class RdfJsonSerializerProviderTest {

	private MGraph mGraph;

	private void initializeGraph() {
		String baseUri = "http://base/";
		mGraph = new SimpleMGraph();
		mGraph.add(new TripleImpl(new UriRef(baseUri + "root"), new UriRef(
				baseUri + "propertyA"), new PlainLiteralImpl("A")));
		mGraph
				.add(new TripleImpl(new UriRef(baseUri + "root"), new UriRef(
						baseUri + "resourcePropertyB"), new UriRef(baseUri
						+ "child1")));
		mGraph.add(new TripleImpl(new UriRef(baseUri + "child1"), new UriRef(
				baseUri + "propertyB"), new PlainLiteralImpl("B")));
		BNode bNode = new BNode();
		mGraph.add(new TripleImpl(bNode, new UriRef(baseUri + "propertyC"),
				new PlainLiteralImpl("C")));
		mGraph.add(new TripleImpl(bNode, new UriRef(baseUri + "propertyE"),
				LiteralFactory.getInstance().createTypedLiteral("E")));
		mGraph.add(new TripleImpl(new UriRef(baseUri + "root"), new UriRef(
				baseUri + "resourcePropertyD"), bNode));
	}

	/*
	 * Serializes a Graph and parse it.
	 */
	@Test
	public void testSerializer() {
		initializeGraph();
		SerializingProvider provider = new RdfJsonSerializingProvider();
		ByteArrayOutputStream serializedGraph = new ByteArrayOutputStream();
		provider.serialize(serializedGraph, mGraph.getGraph(),
				"application/rdf+json");
		ParsingProvider parsingProvider = new RdfJsonParsingProvider();
		ByteArrayInputStream jsonIn = new ByteArrayInputStream(serializedGraph
				.toByteArray());
		Graph graphFromJsonRdf = parsingProvider.parse(jsonIn,
				"application/rdf+json", null);
		Assert.assertEquals(graphFromJsonRdf.size(), 6);

	}
}
