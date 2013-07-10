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
package org.apache.clerezza.rdf.cris;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.cris.ontologies.CRIS;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;

/**
 * Creates the definitions that specify which literals of a resource are
 * indexed by CRIS.
 * 
 * @author rbn, tio, daniel
 */
public class IndexDefinitionManager {

    private MGraph definitionGraph;

    /**
     * Creates a new IndexDefinitionManager.
     * 
     * @param definitionGraph    the graph into which the definitions are written. 
     */
    public IndexDefinitionManager(MGraph definitionGraph) {
        this.definitionGraph = definitionGraph;
    }

    /**
     * Defines an index for the specified types and properties, removing
     * previous index definitions for that type (java friendly version)
     * 
     * @param rdfType The RDF type for which to build an index.
     * @param properties A list of RDF properties to index.
     */
    public void addDefinition(UriRef rdfType, List<UriRef> properties) {

        List<VirtualProperty> list = new ArrayList<VirtualProperty>();
        for (UriRef uri : properties) {
            list.add(new PropertyHolder(uri));
        }
        addDefinitionVirtual(rdfType, list);
    }

    /**
     * Defines an index for the specified types and virtual properties, removing
     * previous index definitions for that type (java friendly version)
     * 
     * @param rdfType The RDF type for which to build an index.
     * @param properties A list of properties to index.
     */
    public void addDefinitionVirtual(UriRef rdfType, List<VirtualProperty> properties) {
        deleteDefinition(rdfType);
        GraphNode node = new GraphNode(new BNode(), definitionGraph);
        node.addProperty(RDF.type, CRIS.IndexDefinition);
        node.addProperty(CRIS.indexedType, rdfType);

        for (VirtualProperty p : properties) {
            node.addProperty(CRIS.indexedProperty, asResource(p));
        }
    }

    /**
     * Remove index definitions for the specified RDF type.
     * 
     * @param rdfType the RDF type
     */
    public void deleteDefinition(UriRef rdfType) {
        GraphNode node = new GraphNode(rdfType, definitionGraph);
        Iterator<GraphNode> iter = node.getSubjectNodes(CRIS.indexedType);
        while (iter.hasNext()) {
            iter.next().deleteNodeContext();
        }
    }

    private Resource asResource(VirtualProperty vp) {

        if (vp instanceof PropertyHolder) {
            return ((PropertyHolder) vp).property;
        } else if (vp instanceof JoinVirtualProperty) {
            JoinVirtualProperty joinVirtualProperty = (JoinVirtualProperty) vp;
            if (joinVirtualProperty.properties.isEmpty()) {
                throw new RuntimeException("vp " + vp + " conatins an empty list");
            }

            BNode virtualProperty = new BNode();
            definitionGraph.add(new TripleImpl(virtualProperty, RDF.type, CRIS.JoinVirtualProperty));
            BNode listBNode = new BNode();
            definitionGraph.add(new TripleImpl(virtualProperty, CRIS.propertyList, listBNode));
            List rdfList = new RdfList(listBNode, definitionGraph);
            for (VirtualProperty uri : joinVirtualProperty.properties) {
                rdfList.add(asResource(uri));
            }
            return virtualProperty;
        } else if (vp instanceof PathVirtualProperty) {
            PathVirtualProperty pathVirtualProperty = (PathVirtualProperty) vp;
            if (pathVirtualProperty.properties.isEmpty()) {
                throw new RuntimeException("vp " + vp + " conatins an empty list");
            }
            BNode virtualProperty = new BNode();
            definitionGraph.add(new TripleImpl(virtualProperty, RDF.type, CRIS.PathVirtualProperty));
            BNode listBNode = new BNode();
            definitionGraph.add(new TripleImpl(virtualProperty, CRIS.propertyList, listBNode));
            List rdfList = new RdfList(listBNode, definitionGraph);
            for (UriRef uri : pathVirtualProperty.properties) {
                rdfList.add(uri);
            }
            return virtualProperty;
        }

        throw new RuntimeException("Could not create resource.");

    }
}
