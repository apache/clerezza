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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.templating.RenderingFunction;

/**
 * Unit tests for WebRenderingFunctions
 * 
 * @author daniel
 * 
 */
public class WebRenderingFunctionsTest {
	@Test
	public void dateTest() throws IOException {

		Date date = new Date();
		TypedLiteral dateLiteral = LiteralFactory.getInstance()
				.createTypedLiteral(date);

		WebRenderingFunctions webRenderingFunctions = new WebRenderingFunctions(
				null, null, null, null);
		RenderingFunction<Object, String> dateFunction = webRenderingFunctions
				.getNamedFunctions().get("date");

		final String formatPattern = "EEE MMM dd HH:mm:ss z yyyy";
		final DateFormat dateFormat = new SimpleDateFormat(formatPattern);
		Assert.assertEquals(dateFormat.format(date), dateFunction.process(dateLiteral,
				formatPattern));

	}

	@Test
	public void substringTest() throws IOException {

		WebRenderingFunctions webRenderingFunctions = new WebRenderingFunctions(
				null, null, null, null);
		RenderingFunction<Object, String> function = webRenderingFunctions
				.getNamedFunctions().get("substring");

		//limits
		Assert.assertEquals("test", function.process("test", 0, 4));
		Assert.assertEquals("", function.process("test", 0, 0));
		Assert.assertEquals("", function.process("test", 4, 4));

		//normal use
		Assert.assertEquals("te", function.process("test", 0, 2));
		Assert.assertEquals("st", function.process("test", 2, 4));
		Assert.assertEquals("es", function.process("test", 1, 3));

		//wrong indices
		Assert.assertEquals("test", function.process("test", -2, 4));
		Assert.assertEquals("test", function.process("test", 0, 6));
		Assert.assertEquals("test", function.process("test", -2, 6));

		//missing indices
		Assert.assertEquals("test", function.process("test", 0));
		Assert.assertEquals("st", function.process("test", 2));
		Assert.assertEquals("test", function.process("test"));

		//non standard input
		Assert.assertEquals("50", function.process(5005, "0", "2"));
	}
	
	@Test
	public void lexicalFormFunctionTest() throws IOException {
		WebRenderingFunctions webRenderingFunctions = new WebRenderingFunctions(
				null, null, null, null);
		RenderingFunction<Literal, String> function = webRenderingFunctions
				.getNamedFunctions().get("lexicalForm");
		
		Assert.assertEquals("test", function.process(
				LiteralFactory.getInstance().createTypedLiteral("test")));
	}
	
	@Test
	public void containsTest() throws IOException {
		WebRenderingFunctions webRenderingFunctions = new WebRenderingFunctions(
				null, null, null, null);
		RenderingFunction<Object, Boolean> function = webRenderingFunctions
				.getNamedFunctions().get("contains");
		UriRef testObject = new UriRef("http://example.org/bla#fooBar");
		Assert.assertTrue(function.process(testObject, "bla"));
		Assert.assertFalse(function.process(testObject, "hello"));
	}
}
