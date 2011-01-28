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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.SerializingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Credits:
 *
 * Blank node labeling algorithm by Jeremy J. Carroll (see "Signing RDF Graphs",
 * HP technical report 2003)
 *
 * Minimum Self-contained Graph (MSG) decomposition algorithm by
 * Giovanni Tummarello, Christian Morbidoni, Paolo Puliti, Francesco Piazza,
 * Università Politecnica delle Marche, Italy
 * (see "Signing individual fragments of an RDF graph", 14th International
 * World Wide Web Conference WWW2005, Poster track, May 2005, Chiba, Japan)
 */

/**
 * A {@link org.apache.clerezza.rdf.core.serializedform.SerializingProvider} that tries
 * to provide similar results when serializing graphs. Specifically it tries to
 * label blank nodes deterministically with reasonable complexity.
 *
 * This serializer does not guarantee a deterministic result but it may minimize
 * the amount of modified lines in serialized output.
 *
 * @author Daniel Spicar (daniel.spicar@access.uzh.ch)
 */
@Component
@Service(SerializingProvider.class)
@SupportedFormat({SupportedFormat.N_TRIPLE})
public class StableSerializerProvider implements SerializingProvider {

	@Property(description="Specifies maximum amount of blank nodes " +
	"labeling recursions, may increase performance at the expense of stability " +
			"(0 = no limit).", intValue=0)
	public static final String MAX_LABELING_ITERATIONS = "max_labeling_iterations";

	private int maxLabelingIterations = -1;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected void activate(ComponentContext cCtx) {
		maxLabelingIterations = (Integer) cCtx.getProperties().
			get(MAX_LABELING_ITERATIONS);
		logger.info("StableSerializerProvider activated");
	}

	@Override
	public void serialize(OutputStream os, TripleCollection tc,
			String formatIdentifier) {

		try {
			List<String> lines = new LinkedList<String>();
			List<MSG> msgs = decomposeGraphToMSGs(tc);
			NTriplesSerializer serializer = new NTriplesSerializer();

			computeMsgHashes(msgs, "MD5");

			for (MSG msg : msgs) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				serializer.serialize(baos, msg.tc);
				BufferedReader serializedGraph =
						new BufferedReader(new StringReader(baos.toString()));
				lines.addAll(labelBlankNodes(serializedGraph, msg.hash));
			}

			Collections.sort(lines);
			for (String l : lines) {
				os.write((l + "\n").getBytes());
			}
		} catch (IOException ex) {
			logger.error("Exception while parsing serialized graph: {}", ex);
		} catch (NoSuchAlgorithmException ex) {
			logger.error("Exception while trying to generate graph hash: {}", ex);
		}
	}

	private List<MSG> decomposeGraphToMSGs(TripleCollection tc) {

		TripleCollection tmp = new SimpleMGraph();
		tmp.addAll(tc);

		List<MSG> msgSet = new LinkedList<MSG>();

		while (tmp.size() > 0) {
			Triple triple = tmp.iterator().next();
			TripleCollection msgTc = new SimpleMGraph();

			boolean containsBNode = fillMSG(triple, tmp, msgTc);
			MSG msg = new MSG(msgTc);
			msg.containsBlankNodes = containsBNode;
			msgSet.add(msg);
		}

		return msgSet;
	}

	private boolean fillMSG(Triple triple, TripleCollection tc,
			TripleCollection msg) {

		boolean containsBNode = false;

		Resource resource = triple.getSubject();
		if (resource instanceof BNode) {
			containsBNode = true;
		} else {
			resource = triple.getObject();
			if (resource instanceof BNode) {
				containsBNode = true;
			}
		}
		if (containsBNode) {
			GraphNode gn = new GraphNode(resource, tc);
			Graph context = gn.getNodeContext();
			msg.addAll(context);
			tc.removeAll(context);
		} else {
			msg.add(triple);
			tc.remove(triple);
		}
		return containsBNode;
	}

	private List<String> labelBlankNodes(BufferedReader serializedGraph,
			String prefix) throws IOException {

		String line = null;
		List<String> lines = new LinkedList<String>();

		long commentedIdentifiers = 0;
		while ((line = serializedGraph.readLine()) != null) {
			try {
				commentedIdentifiers = commentBlankNodeLabels(line,
						commentedIdentifiers, lines);
			} catch (IOException ex) {
				logger.error("Exception while trying to parse line: "
						+ line + "\n{}", ex);
			}
		}

		Collections.sort(lines);

		Map<String, Long> labels = new HashMap<String, Long>();
		long[] counters = {1, commentedIdentifiers}; //counter[0] = genSymCounter
		long commentedIdentifierBefore;
		int ctr = 0;
		do {
			commentedIdentifierBefore = counters[1];
			counters = generateBlankNodeLabels(lines, labels, counters[0],
					counters[1], prefix);
			applyLabels(lines, labels, prefix);
			Collections.sort(lines);
			if(++ctr == maxLabelingIterations) {
				break;
			}
		} while (counters[1] > 0 && commentedIdentifierBefore != counters[1]);

		if (counters[1] > 0) {
			labelBlankNodesNonDeterministically(lines, counters, labels, prefix);
		}

		return lines;
	}


	private long[] generateBlankNodeLabels(List<String> lines,
			Map<String, Long> labels, long genSymCounter, long comments,
			String prefix) {

		for (int i = 1; i <= lines.size(); ++i) {
			StringBuilder previousLine = i > 1 ?new StringBuilder(lines.get(i - 2)) : new StringBuilder();
			StringBuilder currentLine = new StringBuilder(lines.get(i - 1));
			StringBuilder nextLine = new StringBuilder();
			if (i < lines.size()) {
				nextLine.append(lines.get(i));
			}

			String currentLineWithoutComments = stripComments(currentLine);
			if (stripComments(previousLine).equals(currentLineWithoutComments) ||
					stripComments(nextLine).equals(currentLineWithoutComments)) {
				continue;
			}

			int indexOfObject = checkObject(currentLineWithoutComments);
			if (indexOfObject != -1) {
				genSymCounter = applyGenSymIdentifier(labels, genSymCounter,
						currentLine, indexOfObject, prefix);
				--comments;
			}

			int indexOfSubject = checkSubject(currentLineWithoutComments);
			if (indexOfSubject != -1) {
				genSymCounter = applyGenSymIdentifier(labels, genSymCounter,
						currentLine, indexOfSubject, prefix);
				--comments;
			}

			lines.set(i - 1, currentLine.toString());
		}

		long[] result = {genSymCounter, comments};
		return result;
	}

	private void applyLabels(List<String> lines, Map<String, Long> labels,
			String prefix) {

		for (int i = 0; i < lines.size(); ++i) {
			StringBuilder line = new StringBuilder(lines.get(i));

			int indexOfObject = checkObject(stripComments(line));
			if (indexOfObject != -1) {
				int indexOfComment = line.lastIndexOf("#_:");
				String identifier =
						line.substring(indexOfComment + 1, line.length());

				if (labels.containsKey(identifier)) {
					line.delete(indexOfComment, line.length());
					line.delete(indexOfObject, indexOfObject + 1);
					line.insert(indexOfObject, "_:" + prefix +
							labels.get(identifier));
				}
			}

			int indexOfSubject = checkSubject(stripComments(line));
			if (indexOfSubject != -1) {
				int indexOfComment = line.lastIndexOf("#_:");
				String identifier =
						line.substring(indexOfComment + 1, line.length());

				if (labels.containsKey(identifier)) {
					line.delete(indexOfComment, line.length());
					line.delete(indexOfSubject, indexOfSubject + 1);
					line.insert(indexOfSubject, "_:" + prefix +
							labels.get(identifier));
				}
			}

			lines.set(i, line.toString());
		}
	}

	private long commentBlankNodeLabels(String line, long commentedIdentifiers,
			List<String> lines) throws IOException {

		StringReader lineReader = new StringReader(line);
		int data = lineReader.read();
		while (data != -1) {
			if (data == '<') {
				//skip until end tag
				while ((data = lineReader.read()) != '>') {
					checkForEndOfStream(data);
				}
			} else if (data == '"') {
				break;
			} else if (data == '_') {
				if ((data = lineReader.read()) == ':') {
					String identifier = "_:";
					while ((data = lineReader.read()) != ' ') {
						checkForEndOfStream(data);
						identifier = identifier.concat(
								Character.toString((char) data));
					}
					line = line.replaceFirst(identifier, "~");
					line = line.concat(" #" + identifier);
					++commentedIdentifiers;
					checkForEndOfStream(data);
				}
			}
			data = lineReader.read();
		}
		lines.add(line);
		return commentedIdentifiers;
	}

	private long applyGenSymIdentifier(Map<String, Long> labels,
			long genSymCounter, StringBuilder currentLine, int where,
			String prefix) {

		int index = currentLine.lastIndexOf("#_:");
		String identifier =
				currentLine.substring(index + 1, currentLine.length()).trim();
		currentLine.delete(index, currentLine.length());
		if (!labels.containsKey(identifier)) {
			labels.put(identifier, genSymCounter++);
		}
		currentLine.delete(where, where + 1);
		currentLine.insert(where, "_:" + prefix + labels.get(identifier));

		return genSymCounter;
	}

	private void labelBlankNodesNonDeterministically(List<String> lines,
			long[] counters, Map<String, Long> labels, String prefix) {

		for (int i = 0; i < lines.size(); ++i) {
			StringBuilder currentLine = new StringBuilder(lines.get(i));
			String currentLineWithoutComments = stripComments(currentLine);
			int indexOfObject = checkObject(currentLineWithoutComments);
			if (indexOfObject != -1) {
				counters[0] = applyGenSymIdentifier(labels, counters[0],
						currentLine, indexOfObject, prefix);
				--(counters[1]);
			}
			int indexOfSubject = checkSubject(currentLineWithoutComments);
			if (indexOfSubject != -1) {
				counters[0] = applyGenSymIdentifier(labels, counters[0],
						currentLine, indexOfSubject, prefix);
				--(counters[1]);
			}
			lines.set(i, currentLine.toString());
		}
		Collections.sort(lines);
	}

	private void checkForEndOfStream(int data) throws IOException {
		if (data == -1) {
			throw new IOException("Parsing Error!");
		}
	}

	private int checkObject(String line) {
		int index = -1;
		if (line.charAt((index = line.length() - 3)) == '~') {
			return index;
		}
		return -1;
	}

	private int checkSubject(String line) {
		if (line.charAt(0) == '~') {
			return 0;
		}
		return -1;
	}


	private String stripComments(StringBuilder line) {
		if (line.length() < 3) {
			return "";
		}
		return line.substring(0, line.lastIndexOf(" .") + 2);
	}

	private void computeMsgHashes(List<MSG> msgs, String algorithm)
			throws NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance(algorithm);
		HashSet<String> computedHashes = new HashSet<String>(msgs.size());

		for(MSG msg : msgs) {
			if(!msg.containsBlankNodes) {
				//hash is needed only for b-node labelling
				continue;
			}
			List<String> tripleHashes = new ArrayList<String>(msg.tc.size());
			for (Triple t : msg.tc) {
				StringBuilder tripleHash = new StringBuilder();
				if (!(t.getSubject() instanceof BNode)) {
					tripleHash.append(((UriRef) t.getSubject()).hashCode());
				}
				tripleHash.append(t.getPredicate().hashCode());
				if (!(t.getObject() instanceof BNode)) {
					if (t.getObject() instanceof Literal) {
						tripleHash.append(((Literal) t.getObject()).
								toString().hashCode());
					} else {
						tripleHash.append(((UriRef) t.getObject()).hashCode());
					}
				}
				tripleHashes.add(tripleHash.toString());
			}
			Collections.sort(tripleHashes);
			StringBuilder msgHash = new StringBuilder();
			for(String tripleHash : tripleHashes) {
				msgHash.append(tripleHash);
			}

			md.update(msgHash.toString().getBytes());

			String hexString;
			if(computedHashes.add((hexString = getHashHexString(md.digest())))){
				msg.hash = hexString;
			} else {
				md.update(String.valueOf(
						System.currentTimeMillis()).getBytes());
				while(!computedHashes.add(
						(hexString = getHashHexString(md.digest())))) {
					md.update(String.valueOf(
							System.currentTimeMillis()).getBytes());
				}
				msg.hash = hexString;
			}
		}
	}

	private String getHashHexString(byte[] hash) {
		StringBuilder hexString = new StringBuilder();
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xFF & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	private static class MSG {

		final TripleCollection tc;
		String hash = null;
		boolean containsBlankNodes = false;

		MSG(TripleCollection tc) {
			this.tc = tc;
		}
	}
}