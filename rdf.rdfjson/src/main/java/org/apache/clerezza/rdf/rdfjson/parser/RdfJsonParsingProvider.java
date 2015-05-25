/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.clerezza.rdf.rdfjson.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.Language;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.LiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.commons.rdf.impl.utils.TypedLiteralImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * A {@link org.apache.clerezza.rdf.core.serializedform.ParsingProvider} for
 * rdf/json
 *
 * @author tio, hasan
 *
 */
@Component(immediate = true)
@Service(ParsingProvider.class)
@SupportedFormat(SupportedFormat.RDF_JSON)
public class RdfJsonParsingProvider implements ParsingProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final IRI XSD_STRING = new IRI("http://www.w3.org/2001/XMLSchema#string");

    @Override
    public void parse(Graph target, InputStream serializedGraph, String formatIdentifier, IRI baseUri) {

        BlankNodeManager bNodeMgr = new BlankNodeManager();

        JSONParser parser = new JSONParser();
        InputStreamReader reader;
        try {
            reader = new InputStreamReader(serializedGraph, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String msg = "Encoding 'UTF-8' is not supported by this System";
            logger.error("{} (message: {})", msg, e.getMessage());
            throw new IllegalStateException(msg, e);
        }

        try {
            JSONObject root = (JSONObject) parser.parse(reader);

            BlankNodeOrIRI nonLiteral = null;
            for (Object key : root.keySet()) {
                String keyString = (String) key;
                if (keyString.startsWith("_:")) {
                    nonLiteral = bNodeMgr.getBlankNode(keyString);
                } else {
                    nonLiteral = new IRI(keyString);
                }
                JSONObject predicates = (JSONObject) root.get(keyString);
                addTriplesToGraph(nonLiteral, bNodeMgr, predicates, target);
            }
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
            throw new RuntimeException(ioe.getMessage());
        } catch (ParseException pe) {
            logger.error(pe.getMessage());
            throw new RuntimeException(pe.getMessage());
        }
    }

    private class BlankNodeManager {

        private Map<String, BlankNode> bNodeMap = new HashMap<String, BlankNode>();

        public BlankNode getBlankNode(String id) {
            BlankNode bNode = bNodeMap.get(id);
            if (bNode == null) {
                bNode = new BlankNode();
                bNodeMap.put(id, bNode);
            }
            return bNode;
        }
    }

    private void addTriplesToGraph(BlankNodeOrIRI subject, BlankNodeManager bNodeMgr, JSONObject predicates, Graph mGraph) {
        for (Object predicate : predicates.keySet()) {
            JSONArray objects = (JSONArray) predicates.get(predicate);
            for (Object object : objects) {
                JSONObject values = (JSONObject) object;
                String value = (String) values.get("value");
                if (values.get("type").equals("literal")) {
                    IRI dataType;
                    Object dataTypeValue = values.get("datatype");
                    if (dataTypeValue == null
                            || dataTypeValue.toString().isEmpty()) {
                        dataType = XSD_STRING;
                    } else {
                        dataType = new IRI(dataTypeValue.toString());
                    }
                    Language language = null;
                    if (values.containsKey("lang")
                            && !values.get("lang").equals("")
                            && values.get("lang") != null) {
                        language = new Language((String) values.get("lang"));
                    }
                    mGraph.add(new TripleImpl(subject, new IRI((String) predicate),
                            new LiteralImpl(value.toString(), dataType, language)));
                } else if (values.get("type").equals("uri")) {
                    mGraph.add(new TripleImpl(subject, new IRI((String) predicate), new IRI(value)));
                } else if (values.get("type").equals("bnode")) {
                    BlankNodeOrIRI bNode = bNodeMgr.getBlankNode(value);
                    mGraph.add(new TripleImpl(subject, new IRI((String) predicate), bNode));
                }
            }
        }
    }
}
