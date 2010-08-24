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
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.HIERARCHY;
import org.apache.clerezza.rdf.ontologies.RDF;


/**
 * @author mir, rbn
 */
public class CollectionsCreatorTest{

	private static UriRef root = new UriRef("http://localhost:8282/");
	private UriRef foo = new UriRef("http://localhost:8282/foo/");
	private UriRef fooResource = new UriRef("http://localhost:8282/foo/resource");
	private UriRef fooTest = new UriRef("http://localhost:8282/foo/test/");
	private UriRef fooTestResource4 = new UriRef("http://localhost:8282/foo/test/resource4");
		
	@Test
	public void listPositionTest() throws Exception {
		MGraph mGraph = new SimpleMGraph();
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
