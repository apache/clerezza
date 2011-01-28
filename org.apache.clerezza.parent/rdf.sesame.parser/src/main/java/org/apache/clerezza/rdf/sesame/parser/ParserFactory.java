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

import java.util.HashMap;
import java.util.Map;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.n3.N3ParserFactory;
import org.openrdf.rio.ntriples.NTriplesParserFactory;
import org.openrdf.rio.rdfxml.RDFXMLParserFactory;
import org.openrdf.rio.turtle.TurtleParserFactory;

/**
 *
 * @author hasan
 */
public class ParserFactory {
	private static final Map<String, RDFParserFactory> factories = new HashMap<String, RDFParserFactory>();
	static {
		factories.put(SupportedFormat.RDF_XML, new RDFXMLParserFactory());
		factories.put(SupportedFormat.N_TRIPLE, new NTriplesParserFactory());
		factories.put(SupportedFormat.X_TURTLE, new TurtleParserFactory());
		factories.put(SupportedFormat.TURTLE, new TurtleParserFactory());
		factories.put(SupportedFormat.N3, new N3ParserFactory());
		// other parser factories can be added in future if needed:
		// TriXParserFactory, TriGParserFactory, and RDFaHtmlParserFactory
	}

	private ParserFactory() {
	}

	public static RDFParser createRdfParser(String format) {
		RDFParserFactory factory = factories.get(format);
		if (factory == null) {
			return null;
		}
		return factory.getParser();
	}
}
