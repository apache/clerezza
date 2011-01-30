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
package org.apache.clerezza.rdf.rdfa;

import java.io.IOException;
import java.io.InputStream;

import net.rootdev.javardfa.Parser;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author Henry Story <henry.story@bblfish.net>
 */
public abstract class ClerezzaRDFaParser implements ParsingProvider {

	private static Logger log = LoggerFactory.getLogger(ClerezzaRDFaParser.class);


	@Override
	public void parse(MGraph target, InputStream in, String formatIdentifier, UriRef baseUri) {
		try {
			parse(target, new InputSource(in), baseUri);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void parse(MGraph target, InputSource in, UriRef baseURI) throws IOException {
		Parser parser = new Parser(new ClerezzaStatementSink(target));
		if (baseURI != null) {
			parser.setBase(baseURI.getUnicodeString());
		} else {
			parser.setBase("urn:x-relative:root");
		}
		initParser(parser);
		try {
			XMLReader xreader = getReader();
			xreader.setContentHandler(parser);
			xreader.parse(in);
		} catch (SAXException ex) {
			throw new RuntimeException("SAX Error when parsing", ex);
		}
	}

	protected abstract XMLReader getReader() throws SAXException;

	/**
	 * subclasses may override this method to do some specific initialisation of parser
	 * @param parser
	 */
	protected void initParser(Parser parser) {
	}
}
