/*
 * Copyright 2016 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.clerezza.commons.rdf.impl.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.Language;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.impl.utils.AbstractLiteral;
import org.apache.http.util.EntityUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author user
 */
public class SparqlResultParser {

    static Object parse(InputStream in) throws IOException {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            final SparqlsResultsHandler sparqlsResultsHandler = new SparqlsResultsHandler();
            xmlReader.setContentHandler(sparqlsResultsHandler);
            xmlReader.parse(new InputSource(in));
            return sparqlsResultsHandler.getResults();
        } catch (ParserConfigurationException | SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    final public static class SparqlsResultsHandler extends DefaultHandler {

        private String currentBindingName;
        private Map<String, RDFTerm> currentResult = null;
        private Object results = null;
        private boolean readingValue;
        private String lang; //the xml:lang attribute of a literal
        private StringWriter valueWriter;
        private Map<String, BlankNode> bNodeMap = new HashMap<>();
        private static final IRI XSD_STRING = new IRI("http://www.w3.org/2001/XMLSchema#string");
        private static final IRI RDF_LANG_STRING = new IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString");

        private RDFTerm getBNode(String value) {
            if (!bNodeMap.containsKey(value)) {
                bNodeMap.put(value, new BlankNode());
            }
            return bNodeMap.get(value);
        }

        private Object getResults() {
            return results;
        }

        private List<Map<String, RDFTerm>> getResultValueMaps() {
            return (List<Map<String, RDFTerm>>) results;
        }

        enum BindingType {

            uri, bnode, literal;
        }

        @Override
        public void startDocument() throws SAXException {

        }

        @Override
        public void startElement(String namespaceURI,
                String localName,
                String qName,
                Attributes atts)
                throws SAXException {
            if ("http://www.w3.org/2005/sparql-results#".equals(namespaceURI)) {
                if ("boolean".equals(localName)) {
                    if (results != null) {
                        throw new SAXException("unexpected tag <boolean>");
                    }
                    //results will have Boolean value assigned once value is read
                    readingValue = true;
                    valueWriter = new StringWriter();
                } else if ("results".equals(localName)) {
                    if (results != null) {
                        throw new SAXException("unexpected tag <result>");
                    }
                    results = new ArrayList<Map<String, RDFTerm>>();
                } else if ("result".equals(localName)) {
                    if (currentResult != null) {
                        throw new SAXException("unexpected tag <result>");
                    }
                    currentResult = new HashMap<String, RDFTerm>();
                } else if ("binding".equals(localName)) {
                    if (currentResult == null) {
                        throw new SAXException("unexpected tag <binding>");
                    }
                    currentBindingName = atts.getValue("name");
                } else if ("uri".equals(localName) || "bnode".equals(localName) || "literal".equals(localName)) {
                    if (readingValue) {
                        throw new SAXException("unexpected tag <" + localName + ">");
                    }
                    lang = atts.getValue("http://www.w3.org/XML/1998/namespace", "lang");
                    readingValue = true;
                    valueWriter = new StringWriter();
                }
            }

            //System.out.println(namespaceURI);
            //System.out.println(qName);
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            if (readingValue) {
                valueWriter.write(chars, start, length);
                //System.err.println(value + start + ", " + length);
            }
        }

        @Override
        public void endElement(String namespaceURI,
                String localName,
                String qName)
                throws SAXException {
            if ("http://www.w3.org/2005/sparql-results#".equals(namespaceURI)) {
                if ("result".equals(localName)) {
                    ((List<Map<String, RDFTerm>>) results).add(currentResult);
                    currentResult = null;
                } else if ("binding".equals(localName)) {
                    if (currentBindingName == null) {
                        throw new SAXException("unexpected tag </binding>");
                    }
                    currentBindingName = null;
                } else if ("boolean".equals(localName)) {
                    results = new Boolean(valueWriter.toString());
                    valueWriter = null;
                    readingValue = false;
                } else {
                    try {
                        BindingType b = BindingType.valueOf(localName);
                        RDFTerm rdfTerm = null;
                        final Language language = lang == null ? null : new Language(lang);;
                        switch (b) {
                            case uri:
                                rdfTerm = new IRI(valueWriter.toString());
                                valueWriter = null;
                                break;
                            case bnode:
                                rdfTerm = getBNode(valueWriter.toString());
                                valueWriter = null;
                                break;
                            case literal:
                                final String lf = valueWriter.toString();
                                rdfTerm = new AbstractLiteral() {

                                    @Override
                                    public String getLexicalForm() {
                                        return lf;
                                    }

                                    @Override
                                    public IRI getDataType() {
                                        if (language != null) {
                                            return RDF_LANG_STRING;
                                        }
                                        //TODO implement
                                        return XSD_STRING;
                                    }

                                    @Override
                                    public Language getLanguage() {
                                        return language;
                                    }

                                    @Override
                                    public String toString() {
                                        return "\"" + getLexicalForm() + "\"@" + getLanguage();
                                    }
                                };
                                break;
                        }
                        currentResult.put(currentBindingName, rdfTerm);
                        readingValue = false;
                    } catch (IllegalArgumentException e) {
                        //not uri|bnode|literal
                    }
                }
            }
        }

        public void endDocument() throws SAXException {
            //System.out.println("results: " + results.size());
        }

    }
}
