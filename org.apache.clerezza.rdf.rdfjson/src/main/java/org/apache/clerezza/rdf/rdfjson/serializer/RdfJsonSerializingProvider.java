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
package org.apache.clerezza.rdf.rdfjson.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.SerializingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;

/**
 * A {@link org.apache.clerezza.rdf.core.serializedform.SerializingProvider} for
 * rdf/json
 * 
 * @author tio
 * 
 * @scr.component immediate="true"
 * @scr.service 
 *              interface="org.apache.clerezza.rdf.core.serializedform.SerializingProvider"
 * 
 */
@SupportedFormat( SupportedFormat.RDF_JSON )
public class RdfJsonSerializingProvider implements SerializingProvider {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void serialize(OutputStream serializedGraph, TripleCollection tc,
			String formatIdentifier) {
		JSONObject root = new JSONObject();

		Map<NonLiteral, String> subjectsAsJSONObjects = createSubjectsAsJSONObjects(tc);

		for (NonLiteral subject : subjectsAsJSONObjects.keySet()) {
			String key = subjectsAsJSONObjects.get(subject);

			JSONObject predicatesAsJSONObjects = new JSONObject();

			Iterator<Triple> triplesFromSubject = tc
					.filter(subject, null, null);
			while (triplesFromSubject.hasNext()) {
				UriRef predicate = triplesFromSubject.next()
						.getPredicate();
				JSONArray jsonValues = addValuesToJSONArray(tc, subject,
						predicate, subjectsAsJSONObjects);
				predicatesAsJSONObjects.put(predicate.getUnicodeString(),
						jsonValues);
			}
			root.put(key, predicatesAsJSONObjects);
		}
		try {
			serializedGraph.write(root.toJSONString().getBytes());
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			throw new RuntimeException(ioe.getMessage());
		}
	}

	private Map<NonLiteral, String> createSubjectsAsJSONObjects(
			TripleCollection tc) {
		Map<NonLiteral, String> subjectsAsJSONObjects = new HashMap<NonLiteral, String>();
		Iterator<Triple> triples = tc.iterator();
		int bNodeCounter = 1;
		while (triples.hasNext()) {
			NonLiteral subject = triples.next().getSubject();
			if (!subjectsAsJSONObjects.containsKey(subject)) {
				if (subject instanceof UriRef) {
					subjectsAsJSONObjects.put(subject, ((UriRef) subject)
							.getUnicodeString());
				} else if (subject instanceof BNode) {
					subjectsAsJSONObjects.put(subject, "_:" + bNodeCounter++);
				}
			}
		}
		return subjectsAsJSONObjects;
	}

	private JSONArray addValuesToJSONArray(TripleCollection tc,
			NonLiteral subject, UriRef predicate,
			Map<NonLiteral, String> subjectsAsJSONObjects) {

		JSONArray jsonValues = new JSONArray();

		Iterator<Triple> objectsFromPredicate = tc.filter(subject, predicate,
				null);
		while (objectsFromPredicate.hasNext()) {
			Resource object = objectsFromPredicate.next()
					.getObject();
			JSONObject objectAsJSONObject = new JSONObject();
			if (object instanceof PlainLiteral) {
				PlainLiteral plainLiteral = (PlainLiteral) object;
				objectAsJSONObject.put("value", plainLiteral.getLexicalForm());
				objectAsJSONObject.put("type", "literal");
				if (plainLiteral.getLanguage() != null) {
					objectAsJSONObject.put("lang", plainLiteral.getLanguage().toString());
				}
			} else if (object instanceof TypedLiteral) {
				TypedLiteral literal = (TypedLiteral) object;
				objectAsJSONObject.put("value", literal.getLexicalForm());
				objectAsJSONObject.put("type", "literal");
				objectAsJSONObject.put("datatype", literal.getDataType()
						.getUnicodeString());
			} else if (object instanceof UriRef) {
				UriRef uri = (UriRef) object;
				objectAsJSONObject.put("value", uri.getUnicodeString());
				objectAsJSONObject.put("type", "uri");
			} else if (object instanceof BNode) {
				objectAsJSONObject.put("value", subjectsAsJSONObjects
						.get(object));
				objectAsJSONObject.put("type", "bnode");
			}
			jsonValues.add(objectAsJSONObject);
		}
		return jsonValues;
	}
}
