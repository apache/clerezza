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
package org.apache.clerezza.rdf.utils;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class IfpSmushTest {

	private MGraph ontology = new SimpleMGraph();
	{
		ontology.add(new TripleImpl(FOAF.mbox, RDF.type, OWL.InverseFunctionalProperty));
	}

	@Test
	public void simpleBNode()  {
		MGraph mGraph = new SimpleMGraph();
		UriRef mbox1 = new UriRef("mailto:foo@example.org");
		final BNode bNode1 = new BNode();
		mGraph.add(new TripleImpl(bNode1, FOAF.mbox, mbox1));
		mGraph.add(new TripleImpl(bNode1, RDFS.comment, 
				new PlainLiteralImpl("a comment")));
		final BNode bNode2 = new BNode();
		mGraph.add(new TripleImpl(bNode2, FOAF.mbox, mbox1));
		mGraph.add(new TripleImpl(bNode2, RDFS.comment, 
				new PlainLiteralImpl("another comment")));
		Smusher.smush(mGraph, ontology);
		Assert.assertEquals(3, mGraph.size());
	}

	@Test
	public void overlappingEquivalenceClasses()  {
		MGraph mGraph = new SimpleMGraph();
		UriRef mbox1 = new UriRef("mailto:foo@example.org");
		final BNode bNode1 = new BNode();
		mGraph.add(new TripleImpl(bNode1, FOAF.mbox, mbox1));
		mGraph.add(new TripleImpl(bNode1, RDFS.comment,
				new PlainLiteralImpl("a comment")));
		final BNode bNode2 = new BNode();
		UriRef mbox2 = new UriRef("mailto:bar@example.org");
		mGraph.add(new TripleImpl(bNode2, FOAF.mbox, mbox1));
		mGraph.add(new TripleImpl(bNode2, FOAF.mbox, mbox2));
		mGraph.add(new TripleImpl(bNode2, RDFS.comment,
				new PlainLiteralImpl("another comment")));
		final BNode bNode3 = new BNode();
		mGraph.add(new TripleImpl(bNode3, FOAF.mbox, mbox2));
		mGraph.add(new TripleImpl(bNode3, RDFS.comment,
				new PlainLiteralImpl("yet another comment")));
		Smusher.smush(mGraph, ontology);
		Assert.assertEquals(5, mGraph.size());
	}

	@Test
	public void oneUriRef()  {
		MGraph mGraph = new SimpleMGraph();
		UriRef mbox1 = new UriRef("mailto:foo@example.org");
		final UriRef resource = new UriRef("http://example.org/");
		mGraph.add(new TripleImpl(resource, FOAF.mbox, mbox1));
		mGraph.add(new TripleImpl(resource, RDFS.comment,
				new PlainLiteralImpl("a comment")));
		final BNode bNode2 = new BNode();
		mGraph.add(new TripleImpl(bNode2, FOAF.mbox, mbox1));
		mGraph.add(new TripleImpl(bNode2, RDFS.comment,
				new PlainLiteralImpl("another comment")));
		Smusher.smush(mGraph, ontology);
		Assert.assertEquals(3, mGraph.size());
	}

	@Test
	public void twoUriRefs()  {
		MGraph mGraph = new SimpleMGraph();
		UriRef mbox1 = new UriRef("mailto:foo@example.org");
		final UriRef resource1 = new UriRef("http://example.org/");
		mGraph.add(new TripleImpl(resource1, FOAF.mbox, mbox1));
		mGraph.add(new TripleImpl(resource1, RDFS.comment,
				new PlainLiteralImpl("a comment")));
		final UriRef resource2 = new UriRef("http://2.example.org/");
		mGraph.add(new TripleImpl(resource2, FOAF.mbox, mbox1));
		mGraph.add(new TripleImpl(resource2, RDFS.comment,
				new PlainLiteralImpl("another comment")));
		Smusher.smush(mGraph, ontology);
		Assert.assertEquals(4, mGraph.size());
	}

}
