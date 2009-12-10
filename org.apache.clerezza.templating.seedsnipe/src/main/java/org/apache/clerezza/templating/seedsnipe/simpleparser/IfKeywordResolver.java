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
package org.apache.clerezza.templating.seedsnipe.simpleparser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.clerezza.templating.seedsnipe.datastructure.DataFieldResolver;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldDoesNotHaveDimensionException;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldIndexOutOfBoundsException;
import org.apache.clerezza.templating.seedsnipe.datastructure.InvalidElementException;

/**
 * The if-keyword handler for {@link DefaultParser}.
 * 
 * <p>
 * Resolves if the if condition is true or false. Only the comparison for
 * equality is supported.
 * </p>
 * 
 * @author reto, daniel
 */
class IfKeywordResolver implements KeywordResolver {
	
	DefaultParser parser = null;
	
	@Override
	public void resolve(DefaultParser parser, String parameters, Reader in,
			Writer out, DataFieldResolver dataFieldResolver,
			int[] arrayPositioner) throws IOException {
		this.parser = parser;
		String[] endMarkers = { "/if", "else" };

		StringWriter outBuffer;
		if (out != null) {
			outBuffer = new StringWriter();
		} else {
			outBuffer = null;
		}
		
		//unefficiently this code parses both the if and else part!

		boolean evaluation = ((out == null) || isIfTrue(parameters,
				dataFieldResolver, arrayPositioner));
		final String reachedEndMarker;
		if (evaluation == true) {
			//if the condition is true parse the content within the if tags.
			reachedEndMarker = parser.perform(outBuffer, endMarkers, arrayPositioner, dataFieldResolver);
			if (out != null) {
				out.write(outBuffer.toString());
			}
		} else {
			//parse for the if end-tag, skipping everything in-between.
			reachedEndMarker = parser.perform(null, endMarkers, arrayPositioner, dataFieldResolver);
		}

		if (reachedEndMarker == null) { //no end marker found
			PrintWriter printOut = new PrintWriter(out);
			printOut.println("SYNTAX ERROR: if not terminated with /if");
			return;
		}
		
		if (reachedEndMarker.equals("else")) {
			String[] endMarkers2 = { "/if" };
			outBuffer = new StringWriter();
			if (!evaluation) {
				//parse then content within the else tag.
				parser.perform(outBuffer, endMarkers2, arrayPositioner, dataFieldResolver);
				if (out != null) {
					out.write(outBuffer.toString());
				}
			} else {
				//parse for the if end-tag, skipping everything in-between.
				parser.perform(null, endMarkers2, arrayPositioner, dataFieldResolver);
			}
		}
		
		
		return;
	}

	/**
	 * Evaluates if the condition is true.
	 * 
	 * @param parameters  the condition (e.g. <code>number == "5"</code>).
	 * @param dataFieldResolver  the resolver for the type of data fields used.
	 * @param arrayPositioner  the current indices for the data fields.
	 * 
	 * @return  <code>true</code> if the condition is fulfilled, 
	 * 			<code>false</code> otherwise.
	 * 
	 * @throws IOException When the parameters cannot be read.
	 */
	private boolean isIfTrue(String parameters,
			DataFieldResolver dataFieldResolver, int[] arrayPositioner)
			throws IOException {
		try {
			return new Expression(parameters).evaluate(dataFieldResolver, arrayPositioner);
		} catch (FieldDoesNotHaveDimensionException ex) {
			return false;
		} catch (FieldIndexOutOfBoundsException ex) {
			return false;
		} catch (InvalidElementException ex) {
			return false;
		}
	}
}
