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
package org.apache.clerezza.platform.content;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.platform.typerendering.CallbackRenderer;
import org.apache.clerezza.platform.typerendering.Renderlet;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * A renderlet for rendering TitledContent. The contained elements are rendered
 * calling back to the specified <code>Renderer</code>.
 *
 * This renderlet uses a thread-local variable to set the appropriate heading
 * level (h1 - h6)
 *
 * @author rbn, mir
 *
 * @scr.component
 * @scr.service interface="org.apache.clerezza.platform.typerendering.Renderlet"
 */
public class TitledContentRenderlet implements Renderlet {

	private static ThreadLocal<Integer> headingLevel  = new ThreadLocal<Integer>() {

		@Override
		protected Integer initialValue() {
			return 1;
		}

	};
	@Override
	public void render(GraphNode res, GraphNode context, Map<String, Object> sharedRenderingValues,
			CallbackRenderer callbackRenderer,
			URI renderingSpecification,
			String mode,
			MediaType mediaType, RequestProperties requestProperties,
			OutputStream os) throws IOException {
		PrintWriter writer = new PrintWriter(os);
		List<GraphNode> containedNodes = getContainedNodes(res);
		if (containedNodes.size() < 2) {
			String nodeLabel = res.getNode() instanceof UriRef ?
				((UriRef)res.getNode()).getUnicodeString() : " Bnode";
			writer.print(nodeLabel+": titled and/or content could not be found");
			writer.flush();
			return;
		}
		writer.print(getHeaderOpen());
		writer.flush();
		callbackRenderer.render(
				containedNodes.get(0),
				context, mode, os);
		writer.println(getHeaderClose());
		headingLevel.set(headingLevel.get()+1);
		writer.print("<div class='tx-content'>");
		writer.flush();
		callbackRenderer.render(
				containedNodes.get(1),
				context, mode, os);
		headingLevel.set(headingLevel.get()-1);
		writer.println("</div>");
		writer.flush();
	}


	private List<GraphNode> getContainedNodes(GraphNode titledContent) {
		final SortedSet<GraphNode> entries = new TreeSet<GraphNode>(new Comparator<GraphNode>() {

			@Override
			public int compare(GraphNode o1, GraphNode o2) {
					int pos1 = getPos(o1);
					int pos2 = getPos(o2);
					return pos1 - pos2;
			}
			private int getPos(GraphNode o) {
				try {
					return Integer.parseInt(o.getLiterals(DISCOBITS.pos).next().getLexicalForm());
				} catch (NullPointerException e) {
					return -1;
				}
			}

		});
		final Iterator<Resource> entriesIter = titledContent.getObjects(DISCOBITS.contains);
		while (entriesIter.hasNext()) {
			Resource resource = entriesIter.next();
			entries.add(new GraphNode((NonLiteral) resource,titledContent.getGraph()));
		}
		final List<GraphNode> result = new ArrayList<GraphNode>();
		for (GraphNode graphNode : entries) {
			Iterator<Resource> holded = graphNode.getObjects(DISCOBITS.holds);
			if (!holded.hasNext()) {
				throw new RuntimeException(
						"Titled Content must contain a first element: "+graphNode.getNodeContext());
			}
			result.add(new GraphNode(holded.next(),
					titledContent.getGraph()));
		}
		return result;
	}

	private String getHeaderOpen() {
		final Integer level = headingLevel.get();
		return level < 7 ? "<h"+level+">" : "<div class = \"heading\">";
	}

	private String getHeaderClose() {
		final Integer level = headingLevel.get();
		return level < 7 ? "</h"+level+">" : "</div>";
	}
}
