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
package org.apache.clerezza.platform.typerendering.seedsnipe;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 *
 * @author rbn
 */
public class GenericTest {

	private final UriRef root = new UriRef("http://tests.clerezza.org/root#");
	private final SeedsnipeRenderlet renderlet = new SeedsnipeRenderlet();

	@Test
	public void verysimpleTest() throws Exception {
		testWithFiles("data-1.turtle", "template-1.seed", "result-1.txt");
		
	}

	@Test
	public void lexicalFormTest() throws Exception {
		testWithFiles("lexicalForm.turtle", "lexicalForm.seed", "lexicalForm.txt");
	}

	@Test
	public void ifTest() throws Exception {
		testWithFiles("ifTest.turtle", "ifTest.seed", "ifTest.txt");
	}

	private void testWithFiles(String triples, String template, String expected) 
			throws Exception {
		TripleCollection tc = Parser.getInstance().parse(
				getClass().getResourceAsStream(triples),
				"text/turtle");
		GraphNode res = new GraphNode(root, tc);
		ByteArrayOutputStream baosRendered = new ByteArrayOutputStream();
		renderlet.render(res, null, null, null,
				getClass().getResource(template).toURI(),
				null, null, null, baosRendered);
		ByteArrayOutputStream baosExpected = new ByteArrayOutputStream();
		InputStream expectedIn = getClass().getResourceAsStream(expected);
		for (int ch = expectedIn.read(); ch != -1; ch = expectedIn.read()) {
			baosExpected.write(ch);
		}
		//convertring byte[] to String for more readable output when failing
		Assert.assertEquals(new String(baosExpected.toByteArray(), "utf-8"),
				new String(baosRendered.toByteArray(), "utf-8"));
	}



}
