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
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * A {@link org.apache.clerezza.rdf.core.serializedform.ParsingProvider} for rdf/json
 * 
 * @author tio, hasan
 * 
 */
@Component(immediate=true)
@Service(ParsingProvider.class)
@SupportedFormat(SupportedFormat.RDF_JSON)
public class RdfJsonParsingProvider implements ParsingProvider {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void parse(MGraph target, InputStream serializedGraph, String formatIdentifier, UriRef baseUri) {

		BNodeManager bNodeMgr = new BNodeManager();

		JSONParser parser = new JSONParser();
		InputStreamReader reader = new InputStreamReader(serializedGraph);

		try {
			JSONObject root = (JSONObject) parser.parse(reader);

			NonLiteral nonLiteral = null;
			for (Object key : root.keySet()) {
				String keyString = (String) key;
				if (keyString.startsWith("_:")) {
					nonLiteral = bNodeMgr.getBNode(keyString);
				} else {
					nonLiteral = new UriRef(keyString);
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

	private class BNodeManager {
		private Map<String, BNode> bNodeMap = new HashMap<String, BNode>();

		public BNode getBNode(String id) {
			BNode bNode = bNodeMap.get(id);
			if (bNode == null) {
				bNode = new BNode();
				bNodeMap.put(id, bNode);
			}
			return bNode;
		}
	}

	private void addTriplesToGraph(NonLiteral subject, BNodeManager bNodeMgr, JSONObject predicates, MGraph mGraph) {
		for (Object predicate : predicates.keySet()) {
			JSONArray objects = (JSONArray) predicates.get(predicate);
			for (Object object : objects) {
				JSONObject values = (JSONObject) object;
				String value = (String) values.get("value");
				if (values.get("type").equals("literal")) {
					if (values.containsKey("datatype")
							&& !values.get("datatype").equals("")
							&& values.get("datatype") != null) {
						mGraph.add(new TripleImpl(subject, new UriRef((String) predicate),
								LiteralFactory.getInstance().createTypedLiteral(value)));
					} else if (values.containsKey("lang")
							&& !values.get("lang").equals("")
							&& values.get("lang") != null) {
						mGraph.add(new TripleImpl(subject, new UriRef((String) predicate),
								new PlainLiteralImpl(value, new Language((String) values.get("lang")))));
					} else {
						mGraph.add(new TripleImpl(subject, new UriRef((String) predicate), new PlainLiteralImpl(value)));
					}
				} else if (values.get("type").equals("uri")) {
					mGraph.add(new TripleImpl(subject, new UriRef((String) predicate), new UriRef(value)));
				} else if (values.get("type").equals("bnode")) {
					NonLiteral bNode = bNodeMgr.getBNode(value);
					mGraph.add(new TripleImpl(subject, new UriRef((String) predicate), bNode));
				}
			}
		}
	}
}
