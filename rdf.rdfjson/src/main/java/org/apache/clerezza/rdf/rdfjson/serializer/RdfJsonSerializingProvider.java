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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * A {@link org.apache.clerezza.rdf.core.serializedform.SerializingProvider} for
 * rdf/json.
 * 
 * This implementation is based on first sorting the triples within the parsed
 * {@link TripleCollection} based on the {@link #SUBJECT_COMPARATOR subject}.
 * <p>
 * The serialization is done on a subject scope. Meaning that all triples for a
 * subject are serialized and instantly written to the provided
 * {@link OutputStream}.
 * <p>
 * 'UFT-8' is used as encoding to write the data.
 * 
 * @author tio, hasan, rwesten
 */
@Component(immediate = true)
@Service(SerializingProvider.class)
@SupportedFormat(SupportedFormat.RDF_JSON)
public class RdfJsonSerializingProvider implements SerializingProvider {

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(OutputStream serializedGraph, TripleCollection tc,
            String formatIdentifier) {
        if (tc.isEmpty()) { // ensure writing an empty element in case of an
                            // empty collection
            try {
                serializedGraph.write(new JSONObject().toJSONString().getBytes(
                        "UTF-8"));
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Exception while writing to parsed OutputStream", e);
            }
            return;
        }
        BNodeManager bNodeMgr = new BNodeManager();
        BufferedWriter out;
        try {
            out = new BufferedWriter(new OutputStreamWriter(serializedGraph,
                    "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(
                    "Encoding 'UTF-8' is not supported by this System", e);
        }
        Triple[] sortedTriples = tc.toArray(new Triple[tc.size()]);
        Arrays.sort(sortedTriples, SUBJECT_COMPARATOR);
        Triple triple;
        NonLiteral subject = null;
        String subjectStr = null;
        UriRef predicate = null;
        Map<UriRef, JSONArray> predicateValues = new HashMap<UriRef, JSONArray>();
        JSONObject jSubject = new JSONObject();
        try {
            out.write("{"); // start the root object
            for (int i = 0; i < sortedTriples.length; i++) {
                triple = sortedTriples[i];
                boolean subjectChange = !triple.getSubject().equals(subject);
                if (subjectChange) {
                    if (subject != null) {
                        // write the predicate values
                        for (Entry<UriRef, JSONArray> predicates : predicateValues
                                .entrySet()) {
                            jSubject.put(
                                    predicates.getKey().getUnicodeString(),
                                    predicates.getValue());
                        }
                        // write subject
                        out.write(JSONObject.toString(subjectStr, jSubject));
                        out.write(",");
                        jSubject.clear(); // just clear
                        predicateValues.clear();
                    }
                    // init next subject
                    subject = triple.getSubject();
                    if (subject instanceof BNode) {
                        subjectStr = bNodeMgr.getBNodeId((BNode) subject);
                    } else { // if (subject instanceof UriRef)
                        subjectStr = ((UriRef) subject).getUnicodeString();
                    }
                }
                predicate = triple.getPredicate();
                JSONArray values = predicateValues.get(predicate);
                if (values == null) {
                    values = new JSONArray();
                    predicateValues.put(predicate, values);
                }
                values.add(writeObject(bNodeMgr, triple.getObject()));
            }
            if (subjectStr != null) {
                for (Entry<UriRef, JSONArray> predicates : predicateValues
                        .entrySet()) {
                    jSubject.put(predicates.getKey().getUnicodeString(),
                            predicates.getValue());
                }
                out.write(JSONObject.toString(subjectStr, jSubject));
            }
            out.write("}");// end the root object
            out.flush();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Exception while writing on the parsed OutputStream", e);
        }
    }

    private class BNodeManager {
        private Map<BNode, String> bNodeMap = new HashMap<BNode, String>();
        private int counter = 0;

        public String getBNodeId(BNode node) {
            String bNodeId = bNodeMap.get(node);
            if (bNodeId == null) {
                bNodeId = "_:b" + ++counter;
                bNodeMap.put((BNode) node, bNodeId);
            }
            return bNodeId;
        }
    }

    /**
     * Converts the {@link Resource object} of an triple to JSON
     * 
     * @param bNodeMgr
     *            used to lookup {@link BNode} instances
     * @param object
     *            the object of the triple
     * @return the JSON representation of parsed object
     */
    @SuppressWarnings("unchecked")
    private JSONObject writeObject(BNodeManager bNodeMgr, Resource object) {
        JSONObject jObject = new JSONObject();
        if (object instanceof PlainLiteral) {
            PlainLiteral plainLiteral = (PlainLiteral) object;
            jObject.put("value", plainLiteral.getLexicalForm());
            jObject.put("type", "literal");
            if (plainLiteral.getLanguage() != null) {
                jObject.put("lang", plainLiteral.getLanguage().toString());
            }
        } else if (object instanceof TypedLiteral) {
            TypedLiteral literal = (TypedLiteral) object;
            jObject.put("value", literal.getLexicalForm());
            jObject.put("type", "literal");
            jObject.put("datatype", literal.getDataType().getUnicodeString());
        } else if (object instanceof UriRef) {
            UriRef uriRef = (UriRef) object;
            jObject.put("value", uriRef.getUnicodeString());
            jObject.put("type", "uri");
        } else if (object instanceof BNode) {
            String bNodeId = bNodeMgr.getBNodeId((BNode) object);
            jObject.put("value", bNodeId);
            jObject.put("type", "bnode");
        }
        return jObject;
    }

    /**
     * Compares only the subjects of the triples. If they are equals
     * <code>0</code> is returned. This will ensure that all triples with the
     * same subjects are sorted correctly. However it does not sort predicates
     * and objects!
     */
    public static final Comparator<Triple> SUBJECT_COMPARATOR = new Comparator<Triple>() {

        @Override
        public int compare(Triple a, Triple b) {
            return compare(a.getSubject(), b.getSubject());
        }

        private int compare(NonLiteral a, NonLiteral b) {
            int hashA = a.hashCode();
            int hashB = b.hashCode();
            if (hashA != hashB) {
                return hashA > hashB ? 1 : -1;
            }
            return a.toString().compareTo(b.toString());
        }

    };
}
