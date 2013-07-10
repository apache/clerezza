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
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.cris.ontologies.CRIS;
import org.junit.Assert;
import org.junit.Test;
/**
 *
 * @author tio
 */
public class IndexDefinitionManagerTest {


    private void createDefinition(UriRef rdfType, List<UriRef> properties, MGraph manuallyCreatedGraph) {
            GraphNode node = new GraphNode(new BNode(), manuallyCreatedGraph);
            node.addProperty(RDF.type, CRIS.IndexDefinition);
            node.addProperty(CRIS.indexedType, rdfType);
            for (UriRef p : properties) {
                node.addProperty(CRIS.indexedProperty, p);
            }
        }

    @Test
    public void createDefinitionGraph() {
   
    

    MGraph indexManagerGraph = new SimpleMGraph();
    IndexDefinitionManager indexDefinitionManager = new IndexDefinitionManager(indexManagerGraph);
    List<UriRef> properties = new java.util.ArrayList<UriRef>();
    properties.add(FOAF.firstName);
    properties.add(FOAF.lastName);
    indexDefinitionManager.addDefinition(FOAF.Person, properties);
    List<UriRef> list = new ArrayList<UriRef>();
    list.add(FOAF.firstName);
    list.add(FOAF.lastName);

     MGraph manuallyCreatedGraph = new SimpleMGraph();
    createDefinition(FOAF.Person, list, manuallyCreatedGraph);
    Assert.assertEquals(manuallyCreatedGraph.getGraph(), indexManagerGraph.getGraph());
    }

  @Test
    public void createJoinIndexProperty() {
    //import VirtualProperties._
    MGraph indexManagerGraph = new SimpleMGraph();
    IndexDefinitionManager indexDefinitionManager = new IndexDefinitionManager(indexManagerGraph);
    List<VirtualProperty> predicates = new java.util.ArrayList<VirtualProperty>();
    predicates.add(new PropertyHolder(FOAF.firstName));
    predicates.add(new PropertyHolder(FOAF.lastName));

    List<VirtualProperty>  properties = new java.util.ArrayList<VirtualProperty>();
    properties.add(new JoinVirtualProperty(predicates));
    indexDefinitionManager.addDefinitionVirtual(FOAF.Person, properties);
    Iterator<Triple> typeStmtIter = indexManagerGraph.filter(null, RDF.type, CRIS.JoinVirtualProperty);
    Assert.assertTrue(typeStmtIter.hasNext());
        //Assert.assertEquals(manuallyCreatedGraph.getGraph, indexManagerGraph.getGraph)
    }
}
