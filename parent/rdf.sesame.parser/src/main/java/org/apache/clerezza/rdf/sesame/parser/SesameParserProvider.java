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

import java.io.IOException;
import java.io.InputStream;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.core.serializedform.UnsupportedParsingFormatException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;

/**
 * A {@link org.apache.clerezza.rdf.core.serializedform.ParsingProvider} based on Sesame
 *
 * @author hasan
 */

@Component(immediate=true)
@Service(ParsingProvider.class)
@Property(name="supportedFormat", value={SupportedFormat.RDF_XML,
	SupportedFormat.TURTLE,	SupportedFormat.X_TURTLE,
	SupportedFormat.N_TRIPLE, SupportedFormat.N3})
@SupportedFormat({SupportedFormat.RDF_XML,
	SupportedFormat.TURTLE,	SupportedFormat.X_TURTLE,
	SupportedFormat.N_TRIPLE, SupportedFormat.N3})
public class SesameParserProvider implements ParsingProvider {

	@Override
	public void parse(MGraph target, InputStream serializedGraph, String formatIdentifier, UriRef baseUri) {

		RDFParser rdfParser = ParserFactory.createRdfParser(formatIdentifier);
		if (rdfParser == null) {
			throw new UnsupportedParsingFormatException(formatIdentifier);
		}
		SesameRdfHandler sesameRdfHandler = new SesameRdfHandler(target);
		rdfParser.setRDFHandler(sesameRdfHandler);
		String base = (baseUri == null) ? "http://relative-uri.fake/" : baseUri.getUnicodeString();
		try {
			rdfParser.parse(serializedGraph, base);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (RDFParseException ex) {
			throw new RuntimeException(ex);
		} catch (RDFHandlerException ex) {
			throw new RuntimeException(ex);
		}
	}
}
