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
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.DOCUMENTATION;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.UnionGraph;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 *
 * The documentation viewer provides a webpage that shows the available
 * documentation.
 *
 *
 * @author mir
 */
@Component()
@Service(Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Path("/documentation")
public class DocumentationViewer {

    @Reference
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
        ImmutableGraph documentations = tcManager.getImmutableGraph(
                DocumentationProvider.DOCUMENTATION_GRAPH_URI);        
        Collection<DocumentationItem> docItems = getDocItems(documentations);        
        List<DocumentationItem> sortedDocItems = sortDocItems(docItems);        
        Graph mGraph = new SimpleGraph();
        BlankNode orderedContent = createOrderedContent(sortedDocItems, mGraph);
        BlankNode titledContent = createTitledContent(orderedContent, mGraph);
        Graph resultGraph = new UnionGraph(mGraph, documentations);
        GraphNode resultNode = new GraphNode(titledContent, resultGraph);
        return resultNode;
    }

    private Collection<DocumentationItem> getDocItems(ImmutableGraph documentations) {
        Iterator<Triple> docs = documentations.filter(null, 
                DOCUMENTATION.documentation, null);        

        Map<IRI,DocumentationItem> uri2docItemObj = 
            new HashMap<IRI,DocumentationItem>();
        
        while (docs.hasNext()) {
            Triple docc = docs.next();
            IRI docItem = (IRI) docc.getObject();
            Iterator<Triple> afterDocItemsIter = documentations.filter(docItem,
                DOCUMENTATION.after, null);
            Set<IRI> afterDocItems = new HashSet<IRI>();
            while (afterDocItemsIter.hasNext()) {
                afterDocItems.add((IRI) afterDocItemsIter.next().getObject());
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

    private BlankNode createOrderedContent(List<DocumentationItem> sortedDocItems,
        Graph mGraph) {
        BlankNode orderedContent = new BlankNode();
        mGraph.add(new TripleImpl(orderedContent, RDF.type, DISCOBITS.OrderedContent));
        Integer pos = 0;
        Iterator<DocumentationItem> docItemObjsIter = sortedDocItems.iterator();
        while (docItemObjsIter.hasNext()) {
            DocumentationItem docItemObj = docItemObjsIter.next();
            BlankNode containedDoc = new BlankNode();
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

    private BlankNode createTitledContent(BlankNode orderedContent, Graph mGraph) {
        BlankNode titledContent = new BlankNode();
        mGraph.add(new TripleImpl(titledContent, RDF.type, DISCOBITS.TitledContent));
        BlankNode title = new BlankNode();
        mGraph.add(new TripleImpl(title, DISCOBITS.pos, new PlainLiteralImpl("0")));
        BlankNode titleXml = new BlankNode();
        mGraph.add(new TripleImpl(titleXml, RDF.type, DISCOBITS.XHTMLInfoDiscoBit));
        mGraph.add(new TripleImpl(titleXml, DISCOBITS.infoBit,
                LiteralFactory.getInstance().createTypedLiteral("Documentation")));
        mGraph.add(new TripleImpl(title, DISCOBITS.holds, titleXml));
        mGraph.add(new TripleImpl(title, RDF.type, DISCOBITS.Entry));
        mGraph.add(new TripleImpl(titledContent, DISCOBITS.contains, title));        
        BlankNode content = new BlankNode();
        mGraph.add(new TripleImpl(content, DISCOBITS.pos, new PlainLiteralImpl("1")));
        mGraph.add(new TripleImpl(content, DISCOBITS.holds, orderedContent));
        mGraph.add(new TripleImpl(content, RDF.type, DISCOBITS.Entry));
        mGraph.add(new TripleImpl(titledContent, DISCOBITS.contains, content));        
        return titledContent;
    }

    protected static class DocumentationItem {

        private IRI documentationItem;
        private Set<IRI> afterDocItems;
        
        private boolean transitiveAfterDocItemsAdded = false;
        private Map<IRI, DocumentationItem> uri2docItemObj;

        DocumentationItem(IRI doumentationItem, Set<IRI> explicitAfterDocItems,
            Map<IRI, DocumentationItem> uri2docItemObj) {
            this.documentationItem = doumentationItem;
            this.afterDocItems = explicitAfterDocItems;
            this.uri2docItemObj = uri2docItemObj;
        }

        public boolean isAfer(IRI docItem) {
            return getAfterDocItems().contains(docItem);
        }
        
        private Set<IRI> getAfterDocItems() {
            Stack<DocumentationItem> stack = new Stack<DocumentationItem>();
            stack.add(this);
            return getAfterDocItems(stack);
        }
        
        private Set<IRI> getAfterDocItems(Stack<DocumentationItem> stack) {
            if (!transitiveAfterDocItemsAdded) {
                Set<IRI> afterDocItemsClone = new HashSet<>(afterDocItems);
                Iterator<IRI> afterDocUrisIter = afterDocItemsClone.iterator();
                while (afterDocUrisIter.hasNext()) {
                    IRI uriRef = afterDocUrisIter.next();
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
            Iterator<IRI> afterDocs = afterDocItems.iterator();
            while (afterDocs.hasNext()) {
                IRI uriRef = afterDocs.next();
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
