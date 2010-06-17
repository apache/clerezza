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
package org.apache.clerezza.rdf.rdfjson.parser;

import java.io.InputStream;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;




/**
 * @author tio
 */
public class RdfJsonParserProviderTest {


	@Test
	public void testParser() {
		ParsingProvider provider = new RdfJsonParsingProvider();
		InputStream jsonIn = getClass().getResourceAsStream("test.json");
		Graph graphFromJsonRdf = provider.parse(jsonIn, "application/rdf+json", null);
		Assert.assertEquals(graphFromJsonRdf.size(), 6);
		Iterator<Triple> triples = graphFromJsonRdf.filter(new UriRef("http://base/child1"), null, null);
		while(triples.hasNext()) {
			UriRef uri = triples.next().getPredicate();
			Assert.assertEquals(uri.getUnicodeString(), "http://base/propertyB");
		}
	}
}
