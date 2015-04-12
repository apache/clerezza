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
package org.apache.clerezza.platform.content.collections;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.ontologies.HIERARCHY;
import org.apache.clerezza.rdf.ontologies.RDF;


/**
 * @author mir, rbn
 */
public class CollectionsCreatorTest{

    private static IRI root = new IRI("http://localhost:8282/");
    private IRI foo = new IRI("http://localhost:8282/foo/");
    private IRI fooResource = new IRI("http://localhost:8282/foo/resource");
    private IRI fooTest = new IRI("http://localhost:8282/foo/test/");
    private IRI fooTestResource4 = new IRI("http://localhost:8282/foo/test/resource4");
        
    @Test
    public void listPositionTest() throws Exception {
        Graph mGraph = new SimpleGraph();
        CollectionCreator collectionCreator = new CollectionCreator(mGraph);
        collectionCreator.createContainingCollections(fooTestResource4);
        Assert.assertTrue(mGraph.contains(new TripleImpl(fooTest, RDF.type, HIERARCHY.Collection)));
        Assert.assertTrue(mGraph.contains(new TripleImpl(fooTestResource4, HIERARCHY.parent, fooTest)));
        Assert.assertTrue(mGraph.contains(new TripleImpl(foo, HIERARCHY.parent, root)));
        Assert.assertTrue(mGraph.contains(new TripleImpl(root, RDF.type, HIERARCHY.Collection)));
        collectionCreator.createContainingCollections(fooResource);
        Assert.assertTrue(mGraph.contains(new TripleImpl(fooResource, HIERARCHY.parent, foo)));
    }
}
