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

import org.apache.clerezza.implementation.TripleImpl
import org.apache.clerezza.implementation.in_memory.SimpleGraph
import org.apache.clerezza.implementation.literal.{LiteralFactory, PlainLiteralImpl, TypedLiteralImpl}
import org.apache.clerezza.ontologies._
import org.apache.clerezza.scala.utils.Preamble._
import org.apache.clerezza.{BlankNode, IRI, ImmutableGraph, Language}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith;

/**
  * @author bblfish, reto
  */
@RunWith(classOf[JUnitPlatform])
class EzGraphTest {

    val bblfishModulus =
        """
    9D ☮ 79 ☮ BF ☮ E2 ☮ F4 ☮ 98 ☮ BC ☮ 79 ☮ 6D ☮ AB ☮ 73 ☮ E2 ☮ 8B ☮ 39 ☮ 4D ☮ B5 26 ✜ 68 ✜ 49 ✜ EE ✜ 71 ✜ 87 ✜
    06 ✜ 32 ✜ C9 ✜ 9F ✜ 3F ✜ 94 ✜ E5 ✜ CB ✜ 4D ✜ B5 12 ☮ 35 ☮ 13 ☮ 69 ☮ 60 ☮ 81 ☮ 58 ☮ 79 ☮ 66 ☮ F3 ☮ 79 ☮ 20 ☮
    91 ☮ 6A ☮ 3F ☮ 42 5A ✜ F6 ✜ 54 ✜ 42 ✜ 88 ✜ B2 ✜ E9 ✜ 19 ✜ 4A ✜ 79 ✜ 87 ✜ 2E ✜ 62 ✜ 44 ✜ 2D ✜ 7C 06 ☽ 78 ☽ F8
    ☽ FD ☽ 52 ☽ 92 ☽ 6D ☽ CD ☽ D6 ☽ F3 ☽ 28 ☽ 6B ☽ 1F ☽ DB ☽ CB ☽ D3 F2 ☮ 08 ☮ 34 ☮ 72 ☮ A2 ☮ 12 ☮ 75 ☮ AE ☮ D1
    ☮ 09 ☮ 17 ☮ D0 ☮ 88 ☮ 4C ☮ 04 ☮ 8E 04 ☾ E5 ☾ BF ☾ D1 ☾ 41 ☾ 64 ☾ D1 ☾ F7 ☾ 89 ☾ 6D ☾ 8B ☾ B2 ☾ F2 ☾ 46 ☾ C0
    ☾ 56 87 ☮ 8D ☮ B8 ☮ 7C ☮ C6 ☮ FE ☮ E9 ☮ 61 ☮ 88 ☮ 08 ☮ 61 ☮ DD ☮ E3 ☮ B8 ☮ B5 ☮ 47 ♥
    """

    /** import some references in order to reduce dependencies */

    final val hex: IRI = new IRI("http://www.w3.org/ns/auth/cert#hex")
    final val identity: IRI = new IRI("http://www.w3.org/ns/auth/cert#identity")
    final val RSAPublicKey: IRI = new IRI("http://www.w3.org/ns/auth/rsa#RSAPublicKey")
    final val modulus: IRI = new IRI("http://www.w3.org/ns/auth/rsa#modulus")
    final val public_exponent: IRI = new IRI("http://www.w3.org/ns/auth/rsa#public_exponent")

    val henryUri: String = "http://bblfish.net/#hjs"
    val retoUri: String = "http://farewellutopia.com/reto/#me"
    val danbriUri: String = "http://danbri.org/foaf.rdf#danbri"


    private val tinyGraph: ImmutableGraph = {
        val gr = new SimpleGraph()
        val reto = new BlankNode()
        val danny = new BlankNode()
        val henry = new IRI(henryUri)

        gr.add(new TripleImpl(reto, RDF.`type`, FOAF.Person))
        gr.add(new TripleImpl(reto, FOAF.name, new PlainLiteralImpl("Reto Bachman-Gmür", new Language("rm"))))
        //it is difficult to remember that one needs to put a string literal if one does not want to specify a language
        gr.add(new TripleImpl(reto, FOAF.title, new TypedLiteralImpl("Mr", XSD.string)))
        gr.add(new TripleImpl(reto, FOAF.currentProject, new IRI("http://clerezza.org/")))
        gr.add(new TripleImpl(reto, FOAF.knows, henry))
        gr.add(new TripleImpl(reto, FOAF.knows, danny))

        gr.add(new TripleImpl(danny, FOAF.name, new PlainLiteralImpl("Danny Ayers", new Language("en"))))
        gr.add(new TripleImpl(danny, RDF.`type`, FOAF.Person))
        gr.add(new TripleImpl(danny, FOAF.knows, henry))
        gr.add(new TripleImpl(danny, FOAF.knows, reto))

        gr.add(new TripleImpl(henry, FOAF.name, new TypedLiteralImpl("Henry Story", XSD.string))) //It is tricky to remember that one needs this for pure strings
        gr.add(new TripleImpl(henry, FOAF.currentProject, new IRI("http://webid.info/")))
        gr.add(new TripleImpl(henry, RDF.`type`, FOAF.Person))
        gr.add(new TripleImpl(henry, FOAF.knows, danny))
        gr.add(new TripleImpl(henry, FOAF.knows, reto))

        val pk = new BlankNode()
        gr.add(new TripleImpl(pk, RDF.`type`, RSAPublicKey))
        gr.add(new TripleImpl(pk, identity, henry))
        gr.add(new TripleImpl(pk, modulus, LiteralFactory.getInstance().createTypedLiteral(65537)))
        gr.add(new TripleImpl(pk, public_exponent, new TypedLiteralImpl(bblfishModulus, hex)))
        gr.getImmutableGraph
    }


    @Test
    def singleTriple {
        val expected = {
            val s = new SimpleGraph
            s.add(new TripleImpl(henryUri.iri, FOAF.knows, retoUri.iri))
            s.getImmutableGraph
        }
        val ez = new EzGraph() {
            henryUri.iri -- FOAF.knows --> retoUri.iri
        }
        assertEquals(expected, ez.getImmutableGraph, "The two graphs should be equals")
    }

    @Test
    def inverseTriple {
        val expected = {
            val s = new SimpleGraph
            s.add(new TripleImpl(retoUri.iri, FOAF.knows, henryUri.iri))
            s.getImmutableGraph
        }
        val ez = new EzGraph() {
            henryUri.iri <-- FOAF.knows -- retoUri.iri
        }
        assertEquals(expected, ez.getImmutableGraph, "The two graphs should be equals")
    }

    @Test
    def usingAsciiArrows {
        val ez = new EzGraph() {
            (
                    b_("reto").a(FOAF.Person) -- FOAF.name --> "Reto Bachman-Gmür".lang("rm")
                            -- FOAF.title --> "Mr"
                            -- FOAF.currentProject --> "http://clerezza.org/".iri
                            -- FOAF.knows --> (
                            "http://bblfish.net/#hjs".iri.a(FOAF.Person)
                                    -- FOAF.name --> "Henry Story"
                                    -- FOAF.currentProject --> "http://webid.info/".iri
                                    -- FOAF.knows -->> List(b_("reto"), b_("danny"))
                                    //one need to list properties before inverse properties, or use brackets
                                    <-- identity -- (
                                    bnode.a(RSAPublicKey) //. notation because of precedence of operators
                                            -- modulus --> 65537
                                            -- public_exponent --> (bblfishModulus ^^ hex) // brackets needed due to precedence
                                    )
                            )
                            -- FOAF.knows --> (
                            b_("danny").a(FOAF.Person)
                                    -- FOAF.name --> "Danny Ayers".lang("en")
                                    -- FOAF.knows --> "http://bblfish.net/#hjs".iri //knows
                                    -- FOAF.knows --> b_("reto")
                            )
                    )
        }
        assertEquals(tinyGraph.size, ez.size, "the two graphs should be of same size")
        assertEquals(tinyGraph, ez.getImmutableGraph, "Both graphs should contain exactly the same triples")
        //We can add triples by creating a new anonymous instance
        new EzGraph(ez) {
            (
                    "http://bblfish.net/#hjs".iri -- FOAF.name --> "William"
                            -- FOAF.name --> "Bill"
                    )
        }
        assertEquals(tinyGraph.size() + 2, ez.size, "the triple colletion has grown by one")
        //or by just importing it
        import ez._
        ez.b_("danny") -- FOAF.name --> "George"
        assertEquals(tinyGraph.size() + 3, ez.size, "the triple colletion has grown by one")
    }
}
