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
package org.apache.clerezza.rdf.jena.storage;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.jena.facade.JenaGraph;
import org.apache.clerezza.rdf.ontologies.SKOS;

/**
 *
 * @author rbn
 */
public class RoundTripTest {

    @Test
    public void addAndCount() {
        MGraph mGraph = new SimpleMGraph();
        Graph jenaGraph = new JenaGraph(mGraph);
        Model model = ModelFactory.createModelForGraph(jenaGraph);
        model.add(DC.title, RDFS.label, "title");
        MGraph rewrappedMGraph = new JenaGraphAdaptor(jenaGraph);
        Assert.assertEquals(1, rewrappedMGraph.size());
        rewrappedMGraph.add(new TripleImpl(new BNode(), SKOS.prefLabel,
                LiteralFactory.getInstance().createTypedLiteral("foo")));
        Assert.assertEquals(2, rewrappedMGraph.size());
        Assert.assertEquals(2, mGraph.size());
        rewrappedMGraph.clear();
        Assert.assertEquals(0, rewrappedMGraph.size());
    }
}
