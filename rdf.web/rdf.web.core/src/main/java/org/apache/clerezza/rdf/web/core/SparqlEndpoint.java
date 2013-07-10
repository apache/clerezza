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
package org.apache.clerezza.rdf.web.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.sparql.ParseException;
import org.apache.clerezza.rdf.core.sparql.QueryParser;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;
import org.apache.clerezza.rdf.core.sparql.query.Query;
import org.apache.clerezza.rdf.core.sparql.query.SelectQuery;
import org.apache.clerezza.rdf.core.sparql.query.Variable;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.web.ontologies.SPARQLENDPOINT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

/**
 * Provides methods to query a graph over the web.
 * 
 * @author ali, hasan
 * 
 */
@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/sparql")
public class SparqlEndpoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private static final String CONTENT_GRAPH_FILTER =
            "(name="+ Constants.CONTENT_GRAPH_URI_STRING +")";
    
    @Reference
    TcManager tcManager;
    @Reference(target = CONTENT_GRAPH_FILTER)
    MGraph contentGraph;
    @Reference
    private RenderletManager renderletManager;

    /**
     * The activate method is called when SCR activates the component configuration.
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {
        URL templateURL = getClass().getResource("sparql-endpoint.ssp");
        renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
                new UriRef(templateURL.toString()), SPARQLENDPOINT.SparqlEndpoint,
                null, MediaType.APPLICATION_XHTML_XML_TYPE, true);
    }

    @GET
    @Path("form")
    public GraphNode getAvailableTripleCollectionUris(@Context UriInfo uriInfo) {
        AccessController.checkPermission(new SparqlEndpointAccessPermission());
        TrailingSlash.enforceNotPresent(uriInfo);
        GraphNode graphNode = new GraphNode(new BNode(), new SimpleMGraph());
        Set<UriRef> tripleCollections = tcManager.listTripleCollections();
        for (UriRef uriRef : tripleCollections) {
            graphNode.addProperty(SPARQLENDPOINT.tripleCollection, uriRef);
        }
        graphNode.addProperty(RDF.type, SPARQLENDPOINT.SparqlEndpoint);
        return graphNode;
    }

    /**
     * Returns either a {@link Graph} or a {@link DOMSource}. A {@link Graph} is
     * returned if a CONSTRUCT or a DESCRIBE sparql query was submitted and
     * successfully carried out. A {@link DOMSource} is returned if an ASK or a
     * SELECT sparql query was submitted and successfully carried out. The query
     * is performed against a specified graph with the URI
     * <code>defaultGrapfUri</code>
     * 
     * @param queryString
     *            URL encoded sparql query
     * @param defaultGraphUri
     *            URI of the default graph, an {@link UriRef} is expected
     * @param applyStyleSheet
     * @param serverSide
     * @param styleSheetUri 
     * @return either a {@link Graph} or a {@link DOMSource}
     */
    @POST
    public Object runFormQuery(@FormParam("query") String queryString,
            @FormParam("default-graph-uri") UriRef defaultGraphUri,
            @FormParam("apply-style-sheet") String applyStyleSheet,
            @FormParam("server-side") String serverSide,
            @FormParam("style-sheet-uri") String styleSheetUri) {
        AccessController.checkPermission(new SparqlEndpointAccessPermission());
        logger.info("Executing SPARQL Query: " + queryString);
        boolean applyStyle;
        if (applyStyleSheet != null && applyStyleSheet.equals("on")) {
            applyStyle = true;
        } else {
            applyStyle = false;
        }
        boolean applyServerSide;
        if (serverSide != null && serverSide.equals("on")){
            applyServerSide = true;
        } else {
            applyServerSide = false;
        }
        TripleCollection defaultGraph = null;
        Object result = null;
        try {
            if (defaultGraphUri == null
                    || defaultGraphUri.getUnicodeString().equals("")) {
                defaultGraph = contentGraph;
            } else {
                defaultGraph = tcManager.getTriples(defaultGraphUri);
            }
            Query query = QueryParser.getInstance().parse(queryString);
            result = tcManager.executeSparqlQuery(query, defaultGraph);
            if (result instanceof Graph) {
                return (Graph) result;
            } else if ((result instanceof ResultSet)
                    || (result instanceof Boolean)) {
                Source source = toXmlSource(result, query, applyStyle,
                        applyServerSide, styleSheetUri);
                if (applyStyle && applyServerSide) {
                    Response.ResponseBuilder rb = Response.ok(source).type(
                            MediaType.APPLICATION_XHTML_XML_TYPE);
                    return rb.build();
                }
                return source;
            } else {
                throw new WebApplicationException(
                        Response.status(Status.BAD_REQUEST).entity("Only " +
                        "queries yielding to a Graph, a ResultSet or " +
                        "Boolean are supported").build());
            }
        } catch (ParseException e) {
            throw createWebApplicationException(e);
        } catch (NoSuchEntityException e) {
            throw createWebApplicationException(e);
        }
    }

    /**
     * Returns either a {@link Graph} or a {@link DOMSource}. A {@link Graph} is
     * returned if a CONSTRUCT or a DESCRIBE sparql query was submitted and
     * successfully carried out. A {@link DOMSource} is returned if an ASK or a
     * SELECT sparql query was submitted and successfully carried out. The query
     * is performed against a specified graph with the URI
     * <code>defaultGrapfUri</code>
     * 
     * @param queryString
     * @param defaultGraphUri
     * @param styleSheetUri
     * @param serverSide
     * @return
     */
    @GET
    public Object runGetQuery(@QueryParam("query") String queryString,
            @QueryParam("default-graph-uri") UriRef defaultGraphUri,
            @QueryParam("style-sheet-uri") String styleSheetUri,
            @QueryParam("server-side") String serverSide) {
        AccessController.checkPermission(new SparqlEndpointAccessPermission());
        String applyStyleSheet = null;
        if(styleSheetUri != null){
            applyStyleSheet = "on";
        }
        if(serverSide != null && serverSide.equals("true")){
            serverSide = "on";
        }
        return runFormQuery(queryString, defaultGraphUri, applyStyleSheet,
                serverSide,    styleSheetUri);
    }

    /**
     * Helper: returns the variables of a sparql {@link SelectQuery}
     * 
     * @param queryString
     * @return
     */
    private List<Variable> getVariables(Query query) {
        if (query instanceof SelectQuery) {
            return ((SelectQuery) query).getSelection();
        } else {
            return new ArrayList<Variable>();
        }
    }

    /**
     * Helper: transforms a {@link ResultSet} or a {@link Boolean} to a
     * {@link DOMSource}
     * 
     * @param queryResult
     * @param query
     * @param applyStyle 
     */
    private Source toXmlSource(Object queryResult, Query query,
            boolean applyStyle, boolean applyServerSide, String styleSheetUri) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            Document doc = dbf.newDocumentBuilder().newDocument();
            // adding root element
            Element root = doc.createElement("sparql");
            root.setAttribute("xmlns", "http://www.w3.org/2005/sparql-results#");
            doc.appendChild(root);
            Element head = doc.createElement("head");
            root.appendChild(head);
            if (queryResult instanceof Boolean) {
                Element booleanElement = doc.createElement("boolean");
                booleanElement.appendChild(doc.createTextNode(queryResult
                        .toString()));
                root.appendChild(booleanElement);
            } else {
                List<Variable> variables = getVariables(query);
                for (Variable var : variables) {
                    Element varElement = doc.createElement("variable");
                    varElement.setAttribute("name", var.getName());
                    head.appendChild(varElement);
                }
                root.appendChild(createResultsElement((ResultSet) queryResult,
                        doc));
            }
            DOMSource source = new DOMSource(doc);
            if (applyStyle) {
                ProcessingInstruction instruction = doc
                        .createProcessingInstruction("xml-stylesheet",
                                "type=\"text/xsl\" href=\"" + styleSheetUri + "\"");
                doc.insertBefore(instruction, root);
                if (applyServerSide) {
                    return applyStyleServerSide(source, styleSheetUri);
                }
            }
            return source;

        } catch (ParserConfigurationException e) {
            throw createWebApplicationException(e);
        }
    }

    /**
     * Applies a style sheet to a XML on server side
     * @param source
     *            XML result
     * @param styleSheetUri
     *            URI of the style sheet
     * @return
     * @throws TransformerException
     * @throws IOException
     */
    private Source applyStyleServerSide(final DOMSource source,
            final String styleSheetUri) {
        return AccessController.doPrivileged(new PrivilegedAction<DOMSource>() {
            @Override
            public DOMSource run() {
                try {
                    StreamResult streamResult = new StreamResult(
                            new ByteArrayOutputStream());
                    final TransformerFactory tf = TransformerFactory
                            .newInstance();
                    Transformer transformer = tf.newTransformer();
                    transformer.transform(source, streamResult);
                    final URL stylesheet = new URL(styleSheetUri);
                    Source streamSource = new StreamSource(
                            new ByteArrayInputStream(
                            ((ByteArrayOutputStream) streamResult
                                    .getOutputStream()).toByteArray()));
                    DOMResult domResult = new DOMResult();
                    StreamSource xslFileSource = new StreamSource(stylesheet
                            .openStream());
                    Transformer xslTransformer = tf.newTransformer(xslFileSource);
                    xslTransformer.transform(streamSource, domResult);
                    return new DOMSource(domResult.getNode());
                } catch (TransformerConfigurationException ex) {
                    throw createWebApplicationException(ex);
                } catch (TransformerException ex) {
                    throw createWebApplicationException(ex);
                } catch (IOException ex) {
                    throw createWebApplicationException(ex);
                }
            }
        });
    }

    /**
     * Creates a WebApplicationexception and prints a logger entry
     */
    private WebApplicationException createWebApplicationException(Exception e) {
        logger.info(e.getClass().getSimpleName() + ": {}", e.getMessage());
        return new WebApplicationException(Response.status(Status.BAD_REQUEST)
                .entity(e.getMessage().replace("<", "&lt;").replace("\n",
                        "<br/>")).build());
    }

    /**
     * Helper: creates results element from ResultSet
     * 
     */
    private Element createResultsElement(ResultSet resultSet, Document doc) {
        Element results = doc.createElement("results");
        while (resultSet.hasNext()) {
            SolutionMapping solutionMap = resultSet.next();
            Set<Variable> keys = solutionMap.keySet();
            Element result = doc.createElement("result");
            results.appendChild(result);

            for (Variable key : keys) {
                Element bindingElement = doc.createElement("binding");
                bindingElement.setAttribute("name", key.getName());
                bindingElement.appendChild(createValueElement(
                        (Resource) solutionMap.get(key), doc));
                result.appendChild(bindingElement);
            }
        }
        return results;
    }

    /**
     * Helper: creates value element from {@link Resource} depending on its
     * class
     * 
     */
    private Element createValueElement(Resource resource, Document doc) {
        Element value = null;
        if (resource instanceof UriRef) {
            value = doc.createElement("uri");
            value.appendChild(doc.createTextNode(((UriRef) resource)
                    .getUnicodeString()));
        } else if (resource instanceof TypedLiteral) {
            value = doc.createElement("literal");
            value.appendChild(doc.createTextNode(((TypedLiteral) resource)
                    .getLexicalForm()));
            value.setAttribute("datatype", (((TypedLiteral) resource)
                    .getDataType().getUnicodeString()));
        } else if (resource instanceof PlainLiteral) {
            value = doc.createElement("literal");
            value.appendChild(doc.createTextNode(((PlainLiteral) resource)
                    .getLexicalForm()));
            Language lang = ((PlainLiteral) resource).getLanguage();
            if (lang != null) {
                value.setAttribute("xml:lang", (lang.toString()));
            }
        } else {
            value = doc.createElement("bnode");
            value.appendChild(doc.createTextNode(((BNode) resource).toString()));
        }
        return value;
    }
}
