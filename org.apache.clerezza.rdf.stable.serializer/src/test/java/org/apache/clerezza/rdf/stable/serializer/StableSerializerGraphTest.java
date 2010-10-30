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
package org.apache.clerezza.rdf.stable.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author daniel
 */
@RunWith(Parameterized.class)
public class StableSerializerGraphTest {
	private String inputFileName;
	private String format;

	public StableSerializerGraphTest(String inputFileName, String format) {
		this.inputFileName = inputFileName;
		this.format = format;
	}

	@Parameterized.Parameters
	public static Collection<String[]> inputFileNames() {
		return Arrays.asList(new String[][]{
					{"amp-in-url-test001.rdf", "application/rdf+xml"},
					{"datatypes-test001.rdf", "application/rdf+xml"},
					{"datatypes-test002.rdf", "application/rdf+xml"},
					{"rdf-charmod-literals-test001.rdf", "application/rdf+xml"},
					{"rdf-charmod-uris-test001.rdf", "application/rdf+xml"},
					{"rdf-charmod-uris-test002.rdf", "application/rdf+xml"},
					{"xml-canon-test001.rdf", "application/rdf+xml"},
					{"css3deps.rdf", "application/rdf+xml"},
					{"agenda_62.rdf", "application/rdf+xml"},
					{"Talks.rdf", "application/rdf+xml"},
					{"elvisimp.rdf", "application/rdf+xml"}, 
					//{"images.xrdf", "application/rdf+xml"}, //large
					{"libby.foaf", "application/rdf+xml"}
				});
	}

	@Test
	public void RDFTestCases() {
		StableSerializerProvider ssp = new StableSerializerProvider();

		Parser parser = Parser.getInstance();
		Graph deserializedGraphOld = parser.parse(
				getClass().getResourceAsStream(inputFileName), format);

		TripleCollection tc = new SimpleMGraph();
		tc.addAll(deserializedGraphOld);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ssp.serialize(baos, tc, "text/rdf+nt");

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		Graph deserializedGraphNew = parser.parse(bais, "text/rdf+nt");

		Assert.assertEquals(deserializedGraphOld, deserializedGraphNew);

	}
}
