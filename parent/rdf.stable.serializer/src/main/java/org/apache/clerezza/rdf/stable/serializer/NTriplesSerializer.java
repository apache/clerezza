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
package org.apache.clerezza.rdf.stable.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A serializer that can serialze RDF Graphs into N-Triples format.
 *
 * NOTE: This is a special purpose serializer to be used with
 * {@link org.apache.clerezza.rdf.stable.serializer.StableSerializerProvider}.
 * Instances are assumed to be short-lived and not used concurrently.
 *
 * @author Daniel Spicar (daniel.spicar@access.uzh.ch)
 */
class NTriplesSerializer {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private long genSymCounter = 0;
	private HashMap<BNode, String> bNodeLabels;

	/**
	 * Resets the counter to zero that constitutes
	 * the numerical part of blank node labels.
	 */
	void resetGenSymCounter() {
		genSymCounter = 0;
	}

	/**
	 * Serializes a given TripleCollection using the N-Triples format.
	 *
	 * @param os
	 *				An outputstream.
	 * @param tc
	 *				the triples of the graph to be serialized.
	 */
	void serialize(OutputStream os, TripleCollection tc) {
		try {
			bNodeLabels = new HashMap<BNode, String>(tc.size() / 2);

			for (Triple t : tc) {
				if (t.getSubject() instanceof BNode) {
					os.write(serializeBNode((BNode) t.getSubject()).getBytes());
				} else {
					os.write(serializeUriRef(
							(UriRef) t.getSubject()).getBytes());
				}

				os.write((t.getPredicate().toString() + " ").getBytes());

				if (t.getObject() instanceof BNode) {
					os.write(serializeBNode((BNode) t.getObject()).getBytes());
					os.write(".\n".getBytes());
				} else {
					if (t.getObject() instanceof Literal) {
						os.write((serializeLiteral((Literal) t.getObject()) +
								".\n").getBytes());
					} else {
						os.write((serializeUriRef((UriRef) t.getObject()) +
								".\n").getBytes());
					}
				}
			}
		} catch (IOException ex) {
			logger.error("Exception while serializing graph: {}", ex);
		}
	}

	private String serializeUriRef(UriRef uriRef) {
		StringBuffer sb = new StringBuffer("<");
		escapeUtf8ToUsAscii(uriRef.getUnicodeString(), sb, true);
		sb.append("> ");

		return sb.toString() ;
	}

	private String serializeBNode(BNode bNode) {
		if (bNodeLabels.containsKey(bNode)) {
			return bNodeLabels.get(bNode) + " ";
		} else {
			String label = "_:b" + genSymCounter++;
			bNodeLabels.put(bNode, label);
			return label + " ";
		}
	}
	
	private String serializeLiteral(Literal literal) {
		StringBuffer sb = new StringBuffer("\"");
		escapeUtf8ToUsAscii(literal.getLexicalForm(), sb, false);
		sb = sb.append("\"");

		if(literal instanceof TypedLiteral) {
			TypedLiteral typedLiteral = (TypedLiteral) literal;
			sb.append("^^<");
			escapeUtf8ToUsAscii(
					typedLiteral.getDataType().getUnicodeString(), sb, false);
			sb.append(">");
		} else if(literal instanceof PlainLiteral) {
			PlainLiteral plainLiteral = (PlainLiteral) literal;
			if(plainLiteral.getLanguage() != null &&
					!plainLiteral.getLanguage().toString().equals("")) {

				sb.append("@");
				sb.append(plainLiteral.getLanguage().toString());
			}
		}

		sb.append(" ");

		return sb.toString() ;
	}

	private void escapeUtf8ToUsAscii(String input, StringBuffer sb, boolean uri) {

		for (int i = 0; i < input.length(); ++i) {
			char c = input.charAt(i);
			int val = (int) c;
			if (c == '\t') {
				sb.append("\\t");
			} else if (c == '\n') {
				sb.append("\\n");
			} else if (c == '\r') {
				sb.append("\\r");
			} else if (c == '"') {
				sb.append("\\\"");
			} else if (c == '\\') {
				sb.append("\\\\");
			} else if ((val >= 0x0 && val <= 0x8) || (val >= 0xB && val <= 0xC) 
					|| (val >= 0xE && val <= 0x1F)
					|| (val >= 0x7F && val <= 0xFFFF)) {
				sb.append("\\u");
				sb.append(getIntegerHashString(val, 4));
			} else if (val >= 0x10000 && val <= 0x10FFFF) {
				sb.append("\\U");
				sb.append(getIntegerHashString(val, 8));
			} else {
				if (uri && (c == '>' || c == '<')) {
					sb.append("\\u");
					sb.append(getIntegerHashString(val, 4));
				} else {
					sb.append(c);
				}
			}
		}
	}

	private String getIntegerHashString(int val, int length) {
		StringBuffer sb = new StringBuffer();
		String hex = Integer.toHexString(val);
		for(int i = 0; i < length - hex.length(); ++i) {
			sb.append("0");
		}
		sb.append(hex);

		return sb.toString().toUpperCase();
	}
}
