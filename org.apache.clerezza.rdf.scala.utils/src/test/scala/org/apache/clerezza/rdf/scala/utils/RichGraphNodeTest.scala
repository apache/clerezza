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
package org.apache.clerezza.rdf.scala.utils

import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.core.impl._
import org.apache.clerezza.rdf.ontologies._
import org.junit._
import Preamble._

class RichGraphNodeTest {

	private val johnUri = new UriRef("http://example.org/john")
	private val susanneUri = new UriRef("http://example.org/susanne")
	private val listUri = new UriRef("http://example.org/list")
	private val greetingsUri = new UriRef("http://example.org/greetings")
	private val billBNode = new BNode()
	private var node : RichGraphNode = null;
	private var mGraph = new SimpleMGraph()

	@Before
	def prepare() = {
		mGraph.add(new TripleImpl(johnUri, FOAF.name, new PlainLiteralImpl("John")));
		mGraph.add(new TripleImpl(johnUri, FOAF.nick, new PlainLiteralImpl("johny")));
		mGraph.add(new TripleImpl(johnUri, FOAF.name, new PlainLiteralImpl("Johnathan Guller")));
		mGraph.add(new TripleImpl(johnUri, FOAF.knows, billBNode))
		mGraph.add(new TripleImpl(billBNode, FOAF.nick, new PlainLiteralImpl("Bill")));
		mGraph.add(new TripleImpl(billBNode, FOAF.name, new PlainLiteralImpl("William")));
		mGraph.add(new TripleImpl(billBNode, RDF.`type`, FOAF.Person));
		mGraph.add(new TripleImpl(susanneUri, FOAF.knows, johnUri));
		mGraph.add(new TripleImpl(susanneUri, FOAF.name, new PlainLiteralImpl("Susanne")));
		val rdfList = new RdfList(listUri, mGraph);
		rdfList.add(johnUri)
		rdfList.add(new PlainLiteralImpl("foo"))
		rdfList.add(new PlainLiteralImpl("bar"))
		mGraph.add(new TripleImpl(johnUri, SKOS.related, listUri))
		val litEn = new PlainLiteralImpl("hello",
					new Language("en"))
		val litFr = new PlainLiteralImpl("satul",
					new Language("fr"))
		mGraph.add(new TripleImpl(greetingsUri, RDF.value, litEn))
		mGraph.add(new TripleImpl(greetingsUri, RDF.value, litFr))
		node = new GraphNode(johnUri, mGraph)
	}

	@Test
	def testBaseGraph {
		val preamble = new Preamble(mGraph)
		import preamble._
		def asGn(gn: GraphNode)  = gn
		val johnUriNode = asGn(johnUri)
		Assert.assertEquals(johnUriNode, node)
	}

	@Test
	def testSlash = {
		val rNode = new RichGraphNode(node)
		Assert.assertEquals(new PlainLiteralImpl("johny"), (rNode/FOAF.nick)(0).getNode)
		Assert.assertEquals(2, (rNode/FOAF.name).length(20))
		val stringNames = (for(name <- (rNode/FOAF.name).iterator) yield {
			name.toString
		}).toList
		Assert.assertTrue(stringNames.contains("\"Johnathan Guller\""))
		Assert.assertTrue(stringNames.contains("\"John\""))
	}

	@Test
	def testIterate = {
		val simple: MGraph = new SimpleMGraph();
		val node = new GraphNode(new BNode(), simple);
		node.addProperty(DCTERMS.provenance, new UriRef("http://example.org/foo"));
		node.addProperty(DCTERMS.language, new UriRef("http://www.bluewin.ch/"));
		simple.add(new TripleImpl(new UriRef("http://www.bluewin.ch/"),RDF.`type`, RDFS.Container));
		node.addProperty(RDF.`type`, PLATFORM.HeadedPage);
		node.addProperty(RDF.`type`, RDFS.Class);
		val test: CollectedIter[RichGraphNode] = node/DCTERMS.language/RDF.`type`;
		Assert.assertEquals(1, test.length)
		var counter = 0;
		for(k <- test) { counter = counter + 1 }
		Assert.assertEquals(1, counter)
	}

	@Test
	def testInverse = {
		val rNode = new RichGraphNode(node)
		Assert.assertEquals(1, (rNode/-FOAF.knows).length)
	}

	@Test
	def testMissingProperty = {
		val rNode = new RichGraphNode(node)
		Assert.assertEquals(0, (rNode/FOAF.thumbnail).length)
		Assert.assertEquals("", rNode/FOAF.thumbnail*)

	}

	@Test
	def testInverseImplicit = {
		Assert.assertEquals(1, (node/-FOAF.knows).length)
	}

	@Test
	def testPath = {
		Assert.assertEquals(1, (node/-FOAF.knows).length)
		Assert.assertEquals(new PlainLiteralImpl("Susanne"), node/-FOAF.knows%0/FOAF.name%0!)
		Assert.assertEquals(new PlainLiteralImpl("Susanne"), ((node/-FOAF.knows)(0)/FOAF.name)(0)!)
		Assert.assertEquals(new PlainLiteralImpl("Susanne"), node/-FOAF.knows/FOAF.name!)
		Assert.assertEquals(new PlainLiteralImpl("Bill"), node/FOAF.knows/FOAF.nick!)
		Assert.assertEquals("Bill", (node/FOAF.knows/FOAF.nick)(0)*)
		Assert.assertEquals("Bill", node/FOAF.knows/FOAF.nick*)
	}

	@Test
	def testLists = {
		Assert.assertEquals(new PlainLiteralImpl("foo"),(node/SKOS.related).asList().get(1))
		Assert.assertEquals(new PlainLiteralImpl("foo"), (node/SKOS.related%0!!)(1)!)
		Assert.assertEquals(new PlainLiteralImpl("foo"),
							(for (value <- node/SKOS.related%0!!) yield value!).toList(1))
		Assert.assertEquals(new PlainLiteralImpl("bar"),
							(for (value <- node/SKOS.related%0!!) yield value!).toList(2))
		Assert.assertEquals(new PlainLiteralImpl("foo"), node/SKOS.related%0%!!1!)
	}

	@Test
	def sortProperties = {
		Assert.assertEquals(new PlainLiteralImpl("bar"), (node/SKOS.related%0!!).sortWith((a,b) => ((a*) < (b*)))(0)!)
		Assert.assertEquals(johnUri, (node/SKOS.related%0!!).sortWith((a,b) => ((a*) > (b*)))(0)!)
	}

	@Test
	def literalAsObject = {
		val dateLiteral = new TypedLiteralImpl("2009-01-01T01:33:58Z",
					new UriRef("http://www.w3.org/2001/XMLSchema#dateTime"))
		val node = new GraphNode(dateLiteral, new SimpleMGraph())
		Assert.assertNotNull(node.as[java.util.Date])
	}

	@Test
	def literalLanguage = {
		node = new GraphNode(greetingsUri, mGraph)
		val lang = new Language("en")
		val enValue = (node/RDF.value).find(l=>(l!).asInstanceOf[PlainLiteral].getLanguage == lang).get
		Assert.assertEquals("hello", enValue*)
	}

}
