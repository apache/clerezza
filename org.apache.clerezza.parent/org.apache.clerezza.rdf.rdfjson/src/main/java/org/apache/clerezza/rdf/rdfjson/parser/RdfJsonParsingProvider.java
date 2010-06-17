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
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;

/**
 * A {@link org.apache.clerezza.rdf.core.serializedform.ParsingProvider} for rdf/json
 * 
 * @author tio *
 * @scr.component immediate="true"
 * @scr.service interface="org.apache.clerezza.rdf.core.serializedform.ParsingProvider"
 * 
 */
@SupportedFormat( SupportedFormat.RDF_JSON )
public class RdfJsonParsingProvider implements ParsingProvider {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Graph parse(InputStream serializedGraph, String formatIdentifier, UriRef baseUri) {

		MGraph mGraph = new SimpleMGraph();
		JSONParser parser = new JSONParser();
		InputStreamReader reader = new InputStreamReader(serializedGraph);
		try {
			JSONObject root = (JSONObject) parser.parse(reader);
			Map<String, NonLiteral> subjects = createSubjectsFromJSONObjects(root);
			for (String keyString : subjects.keySet()) {
				NonLiteral key = subjects.get(keyString);
				JSONObject predicates = (JSONObject) root.get(keyString);
				addValuesToGraph(key, subjects, predicates, mGraph);
			}
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			throw new RuntimeException(ioe.getMessage());
		} catch (ParseException pe) {
			logger.error(pe.getMessage());
			throw new RuntimeException(pe.getMessage());
		}
		return mGraph.getGraph();
	}

	private Map<String, NonLiteral> createSubjectsFromJSONObjects(
			JSONObject root) {
		Map<String, NonLiteral> subjectsAsJSONObjects = new HashMap<String, NonLiteral>();

		for (Object key : root.keySet()) {
			String keyString = (String) key;
			if (keyString.startsWith("_:")) {
				BNode bNode = new BNode();
				subjectsAsJSONObjects.put(keyString, bNode);
			} else {
				UriRef uri = new UriRef(keyString);
				subjectsAsJSONObjects.put(keyString, uri);
			}
		}
		return subjectsAsJSONObjects;
	}
	
	private void addValuesToGraph(NonLiteral key, Map<String, NonLiteral> subjects,
			JSONObject predicates, MGraph mGraph) {
		for (Object predicate : predicates.keySet()) {
			JSONArray objects = (JSONArray) predicates.get(predicate);
			for (Object object : objects) {
				JSONObject values = (JSONObject) object;
				String value = (String) values.get("value");
				if (values.get("type").equals("literal")) {
					if (values.containsKey("datatype")
							&& !values.get("datatype").equals("")
							&& values.get("datatype") != null) {
						mGraph.add(new TripleImpl(key, new UriRef(
								(String) predicate), LiteralFactory
								.getInstance()
								.createTypedLiteral(value)));
					} else if (values.containsKey("lang")
							&& !values.get("lang").equals("")
							&& values.get("lang") != null) {
						mGraph.add(new TripleImpl(key, new UriRef(
								(String) predicate),
								new PlainLiteralImpl(value,
										new Language((String) values
												.get("lang")))));
					} else {
						mGraph.add(new TripleImpl(key, new UriRef(
								(String) predicate),
								new PlainLiteralImpl(value)));
					}
				} else if (values.get("type").equals("uri")) {
					mGraph.add(new TripleImpl(key, new UriRef(
							(String) predicate), new UriRef(value)));
				} else if (values.get("type").equals("bnode")) {
					mGraph.add(new TripleImpl(key, new UriRef(
							(String) predicate), subjects.get(value)));
				}
			}
		}
	}
}
