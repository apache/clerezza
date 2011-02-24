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
package org.apache.clerezza.rdf.rdfjson.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * A {@link org.apache.clerezza.rdf.core.serializedform.SerializingProvider} for rdf/json
 * 
 * @author tio, hasan
 */
@Component(immediate=true)
@Service(SerializingProvider.class)
@SupportedFormat(SupportedFormat.RDF_JSON)
public class RdfJsonSerializingProvider implements SerializingProvider {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void serialize(OutputStream serializedGraph, TripleCollection tc, String formatIdentifier) {
		JSONObject root = new JSONObject();

		Set<NonLiteral> processedSubject = new HashSet<NonLiteral>();
		BNodeManager bNodeMgr = new BNodeManager();
		NonLiteral subject = null;
		String subjectStr = null;
		Iterator<Triple> triples = tc.iterator();
		while (triples.hasNext()) {
			subject = triples.next().getSubject();
			if (!processedSubject.contains(subject)) {
				if (subject instanceof BNode) {
					subjectStr = bNodeMgr.getBNodeId((BNode)subject);
				} else { // if (subject instanceof UriRef)
					subjectStr = ((UriRef)subject).getUnicodeString();
				}
				JSONObject predicatesAsJSONObjects = new JSONObject();
				Iterator<Triple> triplesOfSubject = tc.filter(subject, null, null);
				while (triplesOfSubject.hasNext()) {
					UriRef predicate = triplesOfSubject.next().getPredicate();
					JSONArray jsonValues = addValuesToJSONArray(tc, subject, predicate, bNodeMgr);
					predicatesAsJSONObjects.put(predicate.getUnicodeString(), jsonValues);
				}
				root.put(subjectStr, predicatesAsJSONObjects);

				processedSubject.add(subject);
			}
		}
		try {
			serializedGraph.write(root.toJSONString().getBytes());
		} catch (IOException ioe) {
			logger.error(ioe.getMessage());
			throw new RuntimeException(ioe.getMessage());
		}
	}

	private class BNodeManager {
		private Map<BNode, String> bNodeMap = new HashMap<BNode, String>();
		private int counter = 0;

		public String getBNodeId(BNode node) {
			String bNodeId = bNodeMap.get(node);
			if (bNodeId == null) {
				bNodeId = "_:b" + ++counter;
				bNodeMap.put((BNode)node, bNodeId);
			}
			return bNodeId;
		}
	}

	private JSONArray addValuesToJSONArray(TripleCollection tc, NonLiteral subject, UriRef predicate,
			BNodeManager bNodeMgr) {

		JSONArray jsonValues = new JSONArray();

		Iterator<Triple> objectsOfPredicate = tc.filter(subject, predicate, null);
		while (objectsOfPredicate.hasNext()) {
			Resource object = objectsOfPredicate.next().getObject();
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
				objectAsJSONObject.put("datatype", literal.getDataType().getUnicodeString());
			} else if (object instanceof UriRef) {
				UriRef uriRef = (UriRef) object;
				objectAsJSONObject.put("value", uriRef.getUnicodeString());
				objectAsJSONObject.put("type", "uri");
			} else if (object instanceof BNode) {
				String bNodeId = bNodeMgr.getBNodeId((BNode)object);
				objectAsJSONObject.put("value", bNodeId);
				objectAsJSONObject.put("type", "bnode");
			}
			jsonValues.add(objectAsJSONObject);
		}
		return jsonValues;
	}
}
