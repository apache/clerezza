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
package org.apache.clerezza.jaxrs.rdf.providers;

import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * By default this returns a serialization of the context of the GraphNode.
 *
 * The expansion can be widened by using the query parameters xPropObj and
 * xProSubj. These parameters specify property uris (both parameters might be
 * repeated). For the specified properties their objects respectively subjects
 * are expanded as if they were bnodes.
 *
 * 
 * @author reto
 */

@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Provider
@Produces({SupportedFormat.N3, SupportedFormat.N_TRIPLE,
    SupportedFormat.RDF_XML, SupportedFormat.TURTLE,
    SupportedFormat.X_TURTLE, SupportedFormat.RDF_JSON})
public class GraphNodeWriter implements MessageBodyWriter<GraphNode> {

    public static final String OBJ_EXP_PARAM = "xPropObj";
    public static final String SUBJ_EXP_PARAM = "xPropSubj";
    
    @Reference
    private Serializer serializer;
    private UriInfo uriInfo;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return GraphNode.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(GraphNode n, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(GraphNode node, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException, WebApplicationException {
        serializer.serialize(entityStream, getExpandedContext(node), mediaType.toString());
    }

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    private Graph getExpandedContext(GraphNode node) {
        final Graph result = new SimpleGraph(node.getNodeContext());
        final Set<RDFTerm> expandedResources = new HashSet<RDFTerm>();
        expandedResources.add(node.getNode());
        while (true) {
            Set<RDFTerm> additionalExpansionRes = getAdditionalExpansionResources(result);
            additionalExpansionRes.removeAll(expandedResources);
            if (additionalExpansionRes.size() == 0) {
                return result;
            }
            for (RDFTerm resource : additionalExpansionRes) {
                final GraphNode additionalNode = new GraphNode(resource, node.getGraph());
                result.addAll(additionalNode.getNodeContext());
                expandedResources.add(resource);
            }
        }
    }

    private Set<RDFTerm> getAdditionalExpansionResources(Graph tc) {
        final Set<IRI> subjectExpansionProperties = getSubjectExpansionProperties();
        final Set<IRI> objectExpansionProperties = getObjectExpansionProperties();
        final Set<RDFTerm> result = new HashSet<RDFTerm>();
        if ((subjectExpansionProperties.size() > 0)
                || (objectExpansionProperties.size() > 0)) {
            for (Triple triple : tc) {
                final IRI predicate = triple.getPredicate();
                if (subjectExpansionProperties.contains(predicate)) {
                    result.add(triple.getSubject());
                }
                if (objectExpansionProperties.contains(predicate)) {
                    result.add(triple.getObject());
                }
            }
        }
        return result;
    }

    private Set<IRI> getSubjectExpansionProperties() {
        final MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        final List<String> paramValues = queryParams.get(SUBJ_EXP_PARAM);
        if (paramValues == null) {
            return new HashSet<IRI>(0);
        }
        final Set<IRI> result = new HashSet<IRI>(paramValues.size());
        for (String uriString : paramValues) {
            result.add(new IRI(uriString));
        }
        return result;
    }

    private Set<IRI> getObjectExpansionProperties() {
        final MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        final List<String> paramValues = queryParams.get(OBJ_EXP_PARAM);
        if (paramValues == null) {
            return new HashSet<IRI>(0);
        }
        final Set<IRI> result = new HashSet<IRI>(paramValues.size());
        for (String uriString : paramValues) {
            result.add(new IRI(uriString));
        }
        return result;
    }
}
