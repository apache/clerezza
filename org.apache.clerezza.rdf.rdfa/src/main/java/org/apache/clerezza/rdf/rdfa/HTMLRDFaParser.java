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

import net.rootdev.javardfa.Parser;
import net.rootdev.javardfa.Setting;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.xml.sax.XMLReader;

/**
 * @author Henry Story <henry.story@bblfish.net>
 */
@Component()
@Service(ParsingProvider.class)
@Property(name = "supportedFormat", value = {"text/html"})
@SupportedFormat("text/html")
public class HTMLRDFaParser extends ClerezzaRDFaParser {

	@Override
	public XMLReader getReader() {
		HtmlParser reader = new HtmlParser();
		reader.setXmlPolicy(XmlViolationPolicy.ALLOW);
		reader.setXmlnsPolicy(XmlViolationPolicy.ALLOW);
		reader.setMappingLangToXmlLang(false);
		return reader;
	}

	@Override
	public void initParser(Parser parser) {
		parser.enable(Setting.ManualNamespaces);
	}
}
