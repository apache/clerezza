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
package org.apache.clerezza.scala.utils

import org.apache.clerezza._
import org.apache.clerezza.implementation.TripleImpl
import org.apache.clerezza.implementation.in_memory.SimpleGraph
import org.apache.clerezza.implementation.literal.{PlainLiteralImpl, TypedLiteralImpl}
import org.apache.clerezza.ontologies._
import org.apache.clerezza.scala.utils.Preamble._
import org.apache.clerezza.utils.{GraphNode, RdfList}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.{BeforeEach, Test}
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

import _root_.scala.language.postfixOps

@RunWith(classOf[JUnitPlatform])
class RichGraphNodeTest {

    private val johnUri = new IRI("http://example.org/john")
    private val susanneUri = new IRI("http://example.org/susanne")
    private val listUri = new IRI("http://example.org/list")
    private val greetingsUri = new IRI("http://example.org/greetings")
    private val billBlankNode = new BlankNode()
    private var node: RichGraphNode = null;
    private var mGraph = new SimpleGraph()

    @BeforeEach
    def prepare() = {
        mGraph.add(new TripleImpl(johnUri, FOAF.name, new PlainLiteralImpl("John")));
        mGraph.add(new TripleImpl(johnUri, FOAF.nick, new PlainLiteralImpl("johny")));
        mGraph.add(new TripleImpl(johnUri, FOAF.name, new PlainLiteralImpl("Johnathan Guller")));
        mGraph.add(new TripleImpl(johnUri, FOAF.knows, billBlankNode))
        mGraph.add(new TripleImpl(johnUri, RDF.`type`, FOAF.Person));
        mGraph.add(new TripleImpl(billBlankNode, FOAF.nick, new PlainLiteralImpl("Bill")));
        mGraph.add(new TripleImpl(billBlankNode, FOAF.name, new PlainLiteralImpl("William")));
        mGraph.add(new TripleImpl(billBlankNode, RDF.`type`, FOAF.Person));
        mGraph.add(new TripleImpl(susanneUri, FOAF.knows, johnUri));
        mGraph.add(new TripleImpl(susanneUri, FOAF.name, new PlainLiteralImpl("Susanne")));
        mGraph.add(new TripleImpl(susanneUri, RDF.`type`, FOAF.Person));
        val rdfList = new RdfList(listUri, mGraph);
        rdfList.add(johnUri)
        rdfList.add(new PlainLiteralImpl("foo"))
        rdfList.add(new PlainLiteralImpl("bar"))
        mGraph.add(new TripleImpl(johnUri, SKOS04.related, listUri))
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
        def asGn(gn: GraphNode) = gn

        val johnUriNode = asGn(johnUri)
        assertEquals(johnUriNode, node)
    }

    @Test
    def testSlash = {
        val rNode = new RichGraphNode(node)
        assertEquals(new PlainLiteralImpl("johny"), (rNode / FOAF.nick) (0).getNode)
        assertEquals(2, (rNode / FOAF.name).length(20))
        val stringNames = (for (name <- (rNode / FOAF.name).iterator) yield {
            name.toString
        }).toList
        assertTrue(stringNames.contains("\"Johnathan Guller\""))
        assertTrue(stringNames.contains("\"John\""))
    }

    @Test
    def testIterate = {
        val simple: Graph = new SimpleGraph();
        val node = new GraphNode(new BlankNode(), simple);
        node.addProperty(DCTERMS.provenance, new IRI("http://example.org/foo"));
        node.addProperty(DCTERMS.language, new IRI("http://www.bluewin.ch/"));
        simple.add(new TripleImpl(new IRI("http://www.bluewin.ch/"), RDF.`type`, RDFS.Container));
        node.addProperty(RDF.`type`, PLATFORM.HeadedPage);
        node.addProperty(RDF.`type`, RDFS.Class);
        val test: CollectedIter[RichGraphNode] = node / DCTERMS.language / RDF.`type`;
        assertEquals(1, test.length)
        var counter = 0;
        for (k <- test) {
            counter = counter + 1
        }
        assertEquals(1, counter)
    }

    @Test
    def testInverse = {
        val rNode = new RichGraphNode(node)
        assertEquals(1, (rNode /- FOAF.knows).length)
    }

    @Test
    def testMissingProperty = {
        val rNode = new RichGraphNode(node)
        assertEquals(0, (rNode / FOAF.thumbnail).length)
        assertEquals("", rNode / FOAF.thumbnail *)

    }

    @Test
    def testInverseImplicit = {
        assertEquals(1, (node /- FOAF.knows).length)
    }

    @Test
    def testPath = {
        assertEquals(1, (node /- FOAF.knows).length)
        assertEquals(new PlainLiteralImpl("Susanne"), node /- FOAF.knows % 0 / FOAF.name % 0 !)
        assertEquals(new PlainLiteralImpl("Susanne"), ((node /- FOAF.knows) (0) / FOAF.name) (0) !)
        assertEquals(new PlainLiteralImpl("Susanne"), node /- FOAF.knows / FOAF.name !)
        assertEquals(new PlainLiteralImpl("Bill"), node / FOAF.knows / FOAF.nick !)
        assertEquals("Bill", (node / FOAF.knows / FOAF.nick) (0) *)
        assertEquals("Bill", node / FOAF.knows / FOAF.nick *)
    }

    @Test
    def testLists = {
        assertEquals(new PlainLiteralImpl("foo"), (node / SKOS04.related).asList().get(1))
        assertEquals(new PlainLiteralImpl("foo"), (node / SKOS04.related % 0 !!) (1) !)
        assertEquals(new PlainLiteralImpl("foo"),
            (for (value <- node / SKOS04.related % 0 !!) yield value !).toList(1))
        assertEquals(new PlainLiteralImpl("bar"),
            (for (value <- node / SKOS04.related % 0 !!) yield value !).toList(2))
        assertEquals(new PlainLiteralImpl("foo"), node / SKOS04.related % 0 %!! 1 !)
    }

    @Test
    def sortProperties = {
        assertEquals(new PlainLiteralImpl("bar"), (node / SKOS04.related % 0 !!).sortWith((a, b) => ((a *) < (b *)))(0) !)
        assertEquals(johnUri, (node / SKOS04.related % 0 !!).sortWith((a, b) => ((a *) > (b *)))(0) !)
    }

    @Test
    def literalAsObject = {
        val dateLiteral = new TypedLiteralImpl("2009-01-01T01:33:58Z",
            new IRI("http://www.w3.org/2001/XMLSchema#dateTime"))
        val node = new GraphNode(dateLiteral, new SimpleGraph())
        assertNotNull(node.as[java.util.Date])
    }

    @Test
    def literalLanguage = {
        node = new GraphNode(greetingsUri, mGraph)
        val lang = new Language("en")
        val enValue = (node / RDF.value).find(l => (l !).asInstanceOf[Literal].getLanguage == lang).get
        assertEquals("hello", enValue *)
    }
}
