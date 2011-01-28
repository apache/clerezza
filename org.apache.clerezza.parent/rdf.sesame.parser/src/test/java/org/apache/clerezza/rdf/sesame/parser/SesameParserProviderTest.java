/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.clerezza.rdf.sesame.parser;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;

/**
 * Tests taken from http://www.w3.org/2001/sw/DataAccess/df1/tests/
 *
 * @author hasan
 */
public class SesameParserProviderTest {

	/*
	 * comparing result from nt and turtle parsing,
	 */
	@Test
	public void testTurtleParser() {
		ParsingProvider provider = new SesameParserProvider();
		InputStream nTriplesIn = getClass().getResourceAsStream("test-04.nt");
		InputStream turtleIn = getClass().getResourceAsStream("test-04.ttl");
		Graph graphFromNTriples = parse(provider, nTriplesIn, SupportedFormat.N_TRIPLE, null);
		Graph graphFromTurtle = parse(provider, turtleIn, SupportedFormat.TURTLE, null);
		Assert.assertEquals(graphFromNTriples, graphFromTurtle);
	}

	/*
	 * comparing result from nt and rdf/xml parsing,
	 */
	@Test
	public void testRdfXmlParser() {
		ParsingProvider provider = new SesameParserProvider();
		InputStream nTriplesIn = getClass().getResourceAsStream("test-04.nt");
		InputStream rdfXmlIn = getClass().getResourceAsStream("test-04.rdf");
		Graph graphFromNTriples = parse(provider, nTriplesIn, SupportedFormat.N_TRIPLE, null);
		Graph graphFromRdfXml = parse(provider, rdfXmlIn, SupportedFormat.RDF_XML, null);
		Assert.assertEquals(graphFromNTriples, graphFromRdfXml);
	}

	private Graph parse(ParsingProvider parsingProvider, InputStream in, String type, UriRef base) {
		MGraph simpleMGraph = new SimpleMGraph();
		parsingProvider.parse(simpleMGraph, in, type, base);
		return simpleMGraph.getGraph();
	}
}
