/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */
package org.apache.clerezza.jaxrs.sparql.providers;

import org.apache.clerezza.IRI;
import org.apache.clerezza.Language;
import org.apache.clerezza.Literal;
import org.apache.clerezza.RDFTerm;
import org.apache.clerezza.sparql.ResultSet;
import org.apache.clerezza.sparql.SolutionMapping;
import org.apache.clerezza.sparql.query.Variable;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * MessageBodyWirter for <code>ResultSet</code>.
 * Resulting output conforms to:
 * http://www.w3.org/TR/2008/REC-rdf-sparql-XMLres-20080115/
 *
 * @author mir, reto
 */
@Component(service = Object.class, property = {"javax.ws.rs=true"})
@Produces({"application/xml", "text/xml", "application/sparql-results+xml"})
@Provider
public class ResultSetXmlMessageBodyWriter implements MessageBodyWriter<ResultSet> {

    private Providers providers;

    final Logger logger = LoggerFactory.getLogger(ResultSetXmlMessageBodyWriter.class);

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
                               MediaType mediaType) {
        return ResultSet.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(ResultSet t, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(ResultSet resultSet, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType, MultivaluedMap<String,
            Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {

        Source source = toXmlSource(resultSet);
        MessageBodyWriter<Source> sourceMessageBodyWriter =
                providers.getMessageBodyWriter(Source.class, null, null, mediaType);
        sourceMessageBodyWriter.writeTo(source, Source.class, null, null, mediaType,
                httpHeaders, entityStream);
    }

    @Context
    public void setProviders(Providers providers) {
        this.providers = providers;
    }

    /**
     * Helper: transforms a {@link ResultSet} or a {@link Boolean} to a
     * {@link DOMSource}
     *
     * @param queryResult
     */
    private Source toXmlSource(ResultSet queryResult) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            Document doc = dbf.newDocumentBuilder().newDocument();
            // adding root element
            Element root = doc.createElement("sparql");
            root.setAttribute("xmlns", "http://www.w3.org/2005/sparql-results#");
            doc.appendChild(root);
            Element head = doc.createElement("head");
            createVariables(queryResult.getResultVars(), head, doc);
            root.appendChild(head);

            Element results = doc.createElement("results");
            while (queryResult.hasNext()) {
                createResultElement(queryResult.next(), results, doc);
            }
            root.appendChild(results);

            DOMSource source = new DOMSource(doc);
            return source;

        } catch (ParserConfigurationException e) {
            throw createWebApplicationException(e);
        }
    }

    /**
     * Creates a WebApplicationException and prints a logger entry
     */
    private WebApplicationException createWebApplicationException(Exception e) {
        return new WebApplicationException(Response.status(Status.BAD_REQUEST)
                .entity(e.getMessage().replace("<", "&lt;").replace("\n",
                        "<br/>")).build());
    }


    /**
     * Helper: creates value element from {@link RDFTerm} depending on its
     * class
     */
    private Element createValueElement(RDFTerm resource, Document doc) {
        Element value = null;
        if (resource instanceof IRI) {
            value = doc.createElement("uri");
            value.appendChild(doc.createTextNode(((IRI) resource)
                    .getUnicodeString()));
        } else if (resource instanceof Literal) {
            value = doc.createElement("literal");
            value.appendChild(doc.createTextNode(((Literal) resource)
                    .getLexicalForm()));
            value.setAttribute("datatype", (((Literal) resource)
                    .getDataType().getUnicodeString()));
            Language lang = ((Literal) resource).getLanguage();
            if (lang != null) {
                value.setAttribute("xml:lang", (lang.toString()));
            }
        } else {
            value = doc.createElement("bnode");
            value.appendChild(doc.createTextNode("/"));
        }
        return value;
    }

    /**
     * Helper: creates results element from ResultSet
     */
    private void createResultElement(SolutionMapping solutionMap, Element results, Document doc) {
        Set<Variable> keys = solutionMap.keySet();
        Element result = doc.createElement("result");
        results.appendChild(result);
        for (Variable key : keys) {
            Element bindingElement = doc.createElement("binding");
            bindingElement.setAttribute("name", key.getName());
            bindingElement.appendChild(createValueElement((RDFTerm) solutionMap.get(key), doc));
            result.appendChild(bindingElement);
        }
    }

    private void createVariables(List<String> variables, Element head, Document doc) {
        for (String variable : variables) {
            Element varElement = doc.createElement("variable");
            varElement.setAttribute("name", variable);
            head.appendChild(varElement);
        }
    }
}
