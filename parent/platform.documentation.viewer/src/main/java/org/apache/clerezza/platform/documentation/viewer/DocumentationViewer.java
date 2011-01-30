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
package org.apache.clerezza.platform.documentation.viewer;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.documentation.DocumentationProvider;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.DOCUMENTATION;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.UnionMGraph;

/**
 *
 * The documentation viewer provides a webpage that shows the available
 * documentation.
 *
 * @scr.component
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true"
 *
 * @author mir
 */
@Path("/documentation")
public class DocumentationViewer {

	/**
	 * @scr.reference
	 */
	private TcManager tcManager;
		
	/**
	 * Redirects to the overview page
	 *
	 * @return {@link Response}
	 *
	 */
	@GET
	public GraphNode documentationPage(@Context UriInfo uriInfo) {
		TrailingSlash.enforcePresent(uriInfo);
		Graph documentations = tcManager.getGraph(
				DocumentationProvider.DOCUMENTATION_GRAPH_URI);		
		Collection<DocumentationItem> docItems = getDocItems(documentations);		
		List<DocumentationItem> sortedDocItems = sortDocItems(docItems);		
		MGraph mGraph = new SimpleMGraph();
		BNode orderedContent = createOrderedContent(sortedDocItems, mGraph);
		BNode titledContent = createTitledContent(orderedContent, mGraph);
		MGraph resultGraph = new UnionMGraph(mGraph, documentations);
		GraphNode resultNode = new GraphNode(titledContent, resultGraph);
		return resultNode;
	}

	private Collection<DocumentationItem> getDocItems(Graph documentations) {
		Iterator<Triple> docs = documentations.filter(null, 
				DOCUMENTATION.documentation, null);		

		Map<UriRef,DocumentationItem> uri2docItemObj = 
			new HashMap<UriRef,DocumentationItem>();
		
		while (docs.hasNext()) {
			Triple docc = docs.next();
			UriRef docItem = (UriRef) docc.getObject();
			Iterator<Triple> afterDocItemsIter = documentations.filter(docItem,
				DOCUMENTATION.after, null);
			Set<UriRef> afterDocItems = new HashSet<UriRef>();
			while (afterDocItemsIter.hasNext()) {
				afterDocItems.add((UriRef) afterDocItemsIter.next().getObject());
			}
			DocumentationItem docItemObj = new DocumentationItem(
					docItem, afterDocItems, uri2docItemObj);
			uri2docItemObj.put(docItem, docItemObj);
		}		
		return uri2docItemObj.values();
	}

	protected List<DocumentationItem> sortDocItems(
		Collection<DocumentationItem> docItems) {
		List<DocumentationItem> result = 
				new ArrayList<DocumentationItem>();
		Iterator<DocumentationItem> items = docItems.iterator();

		OUTER: while (items.hasNext()) {
			DocumentationItem item = items.next();
			for (int i = 0; i < result.size(); i++ ) {
				if (result.get(i).isAfer(item.documentationItem)) {
					result.add(i, item);
					continue OUTER;
				}
			}
			result.add(item);
		}
		return result;
	}

	private BNode createOrderedContent(List<DocumentationItem> sortedDocItems,
		MGraph mGraph) {
		BNode orderedContent = new BNode();
		mGraph.add(new TripleImpl(orderedContent, RDF.type, DISCOBITS.OrderedContent));
		Integer pos = 0;
		Iterator<DocumentationItem> docItemObjsIter = sortedDocItems.iterator();
		while (docItemObjsIter.hasNext()) {
			DocumentationItem docItemObj = docItemObjsIter.next();
			BNode containedDoc = new BNode();
			mGraph.add(new TripleImpl(orderedContent, DISCOBITS.contains,
					containedDoc));
			mGraph.add(new TripleImpl(containedDoc, DISCOBITS.pos,
					new PlainLiteralImpl(pos.toString())));
			mGraph.add(new TripleImpl(containedDoc, DISCOBITS.holds,
					docItemObj.documentationItem));
			pos++;
		}
		return orderedContent;
	}

	private BNode createTitledContent(BNode orderedContent, MGraph mGraph) {
		BNode titledContent = new BNode();
		mGraph.add(new TripleImpl(titledContent, RDF.type, DISCOBITS.TitledContent));
		BNode title = new BNode();
		mGraph.add(new TripleImpl(title, DISCOBITS.pos, new PlainLiteralImpl("0")));
		BNode titleXml = new BNode();
		mGraph.add(new TripleImpl(titleXml, RDF.type, DISCOBITS.XHTMLInfoDiscoBit));
		mGraph.add(new TripleImpl(titleXml, DISCOBITS.infoBit,
				LiteralFactory.getInstance().createTypedLiteral("Documentation")));
		mGraph.add(new TripleImpl(title, DISCOBITS.holds, titleXml));
		mGraph.add(new TripleImpl(title, RDF.type, DISCOBITS.Entry));
		mGraph.add(new TripleImpl(titledContent, DISCOBITS.contains, title));		
		BNode content = new BNode();
		mGraph.add(new TripleImpl(content, DISCOBITS.pos, new PlainLiteralImpl("1")));
		mGraph.add(new TripleImpl(content, DISCOBITS.holds, orderedContent));
		mGraph.add(new TripleImpl(content, RDF.type, DISCOBITS.Entry));
		mGraph.add(new TripleImpl(titledContent, DISCOBITS.contains, content));		
		return titledContent;
	}

	protected static class DocumentationItem {

		private UriRef documentationItem;
		private Set<UriRef> afterDocItems;
		
		private boolean transitiveAfterDocItemsAdded = false;
		private Map<UriRef, DocumentationItem> uri2docItemObj;

		DocumentationItem(UriRef doumentationItem, Set<UriRef> explicitAfterDocItems,
			Map<UriRef, DocumentationItem> uri2docItemObj) {
			this.documentationItem = doumentationItem;
			this.afterDocItems = explicitAfterDocItems;
			this.uri2docItemObj = uri2docItemObj;
		}

		public boolean isAfer(UriRef docItem) {
			return getAfterDocItems().contains(docItem);
		}
		
		private Set<UriRef> getAfterDocItems() {
			Stack<DocumentationItem> stack = new Stack<DocumentationItem>();
			stack.add(this);
			return getAfterDocItems(stack);
		}
		
		private Set<UriRef> getAfterDocItems(Stack<DocumentationItem> stack) {
			if (!transitiveAfterDocItemsAdded) {
				Iterator<UriRef> afterDocUrisIter = afterDocItems.iterator();
				while (afterDocUrisIter.hasNext()) {
					UriRef uriRef = afterDocUrisIter.next();
					DocumentationItem docItem = uri2docItemObj.get(uriRef);
					if (stack.contains(docItem)) {
						throw new RuntimeException("Documentation: cycle detected!\n"
							+ stack.toString());
					}
					stack.add(docItem);
					afterDocItems.addAll(docItem.getAfterDocItems(stack));
					
				}
				transitiveAfterDocItemsAdded = true;
			} 			
			return afterDocItems;
		}

		@Override
		public String toString() {
			StringWriter writer = new StringWriter();
			writer.append("[");
			writer.append(documentationItem.getUnicodeString());
			writer.append(" is after (");
			Iterator<UriRef> afterDocs = afterDocItems.iterator();
			while (afterDocs.hasNext()) {
				UriRef uriRef = afterDocs.next();
				writer.append(uriRef.getUnicodeString());
				if (afterDocs.hasNext()) {
					writer.append(",");
				}
			}
			writer.append(")]");
			return writer.toString();
		}
	}
}
