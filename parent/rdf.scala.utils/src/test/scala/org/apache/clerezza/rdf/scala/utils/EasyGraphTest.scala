/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.clerezza.rdf.ontologies._
import org.junit._
import org.apache.clerezza.rdf.core._
import impl.{TypedLiteralImpl, PlainLiteralImpl, SimpleMGraph, TripleImpl}

class EasyGraphTest {

	val bblfishModulus = """
	9D ☮ 79 ☮ BF ☮ E2 ☮ F4 ☮ 98 ☮ BC ☮ 79 ☮ 6D ☮ AB ☮ 73 ☮ E2 ☮ 8B ☮ 39 ☮ 4D ☮ B5 26 ✜ 68 ✜ 49 ✜ EE ✜ 71 ✜ 87 ✜
	06 ✜ 32 ✜ C9 ✜ 9F ✜ 3F ✜ 94 ✜ E5 ✜ CB ✜ 4D ✜ B5 12 ☮ 35 ☮ 13 ☮ 69 ☮ 60 ☮ 81 ☮ 58 ☮ 79 ☮ 66 ☮ F3 ☮ 79 ☮ 20 ☮
	91 ☮ 6A ☮ 3F ☮ 42 5A ✜ F6 ✜ 54 ✜ 42 ✜ 88 ✜ B2 ✜ E9 ✜ 19 ✜ 4A ✜ 79 ✜ 87 ✜ 2E ✜ 62 ✜ 44 ✜ 2D ✜ 7C 06 ☽ 78 ☽ F8
	☽ FD ☽ 52 ☽ 92 ☽ 6D ☽ CD ☽ D6 ☽ F3 ☽ 28 ☽ 6B ☽ 1F ☽ DB ☽ CB ☽ D3 F2 ☮ 08 ☮ 34 ☮ 72 ☮ A2 ☮ 12 ☮ 75 ☮ AE ☮ D1
	☮ 09 ☮ 17 ☮ D0 ☮ 88 ☮ 4C ☮ 04 ☮ 8E 04 ☾ E5 ☾ BF ☾ D1 ☾ 41 ☾ 64 ☾ D1 ☾ F7 ☾ 89 ☾ 6D ☾ 8B ☾ B2 ☾ F2 ☾ 46 ☾ C0
	☾ 56 87 ☮ 8D ☮ B8 ☮ 7C ☮ C6 ☮ FE ☮ E9 ☮ 61 ☮ 88 ☮ 08 ☮ 61 ☮ DD ☮ E3 ☮ B8 ☮ B5 ☮ 47 ♥
	"""

	/** import some references in order to reduce dependencies */

	final val hex: UriRef = new UriRef("http://www.w3.org/ns/auth/cert#hex")
	final val identity: UriRef = new UriRef("http://www.w3.org/ns/auth/cert#identity")
	final val RSAPublicKey: UriRef = new UriRef("http://www.w3.org/ns/auth/rsa#RSAPublicKey")
	final val modulus: UriRef = new UriRef("http://www.w3.org/ns/auth/rsa#modulus")
	final val public_exponent: UriRef = new UriRef("http://www.w3.org/ns/auth/rsa#public_exponent")


	private val tinyGraph: Graph = {
		val gr = new SimpleMGraph
		val reto= new BNode()
		val danny = new BNode()
		val henry = new UriRef("http://bblfish.net/#hjs")

		gr.add(new TripleImpl(reto,RDF.`type`, FOAF.Person))
		gr.add(new TripleImpl(reto,FOAF.name, new PlainLiteralImpl("Reto Bachman-Gmür", new Language("rm"))))
		gr.add(new TripleImpl(reto,FOAF.title, new PlainLiteralImpl("Mr")))
		gr.add(new TripleImpl(reto,FOAF.currentProject, new UriRef("http://clerezza.org/")))
		gr.add(new TripleImpl(reto,FOAF.knows, henry))
		gr.add(new TripleImpl(reto,FOAF.knows, danny))

		gr.add(new TripleImpl(danny,FOAF.name,new PlainLiteralImpl("Danny Ayers", new Language("en"))))
		gr.add(new TripleImpl(danny,RDF.`type`, FOAF.Person))
		gr.add(new TripleImpl(danny,FOAF.knows, henry))
		gr.add(new TripleImpl(danny,FOAF.knows, reto))

		gr.add(new TripleImpl(henry,FOAF.name,new PlainLiteralImpl("Henry Story")))
		gr.add(new TripleImpl(henry,FOAF.currentProject,new UriRef("http://webid.info/")))
		gr.add(new TripleImpl(henry,RDF.`type`, FOAF.Person))
		gr.add(new TripleImpl(henry,FOAF.knows, danny))
		gr.add(new TripleImpl(henry,FOAF.knows, reto))

		val pk = new BNode()
		gr.add(new TripleImpl(pk,RDF.`type`,RSAPublicKey))
		gr.add(new TripleImpl(pk,identity,henry))
		gr.add(new TripleImpl(pk,modulus,LiteralFactory.getInstance().createTypedLiteral(65537)))
		gr.add(new TripleImpl(pk,public_exponent,new TypedLiteralImpl(bblfishModulus,hex)))
		gr.getGraph
	}

	@Test
	def testEquality {
		val gr = new SimpleMGraph

		val reto= new BNode()
		gr.add(new TripleImpl(reto,RDF.`type`, FOAF.Person))

		val ez = new EasyGraph()
		ez.bnode ∈ FOAF.Person

		Assert.assertEquals("the two graphs should be of same size",gr.size(),ez.size())
		Assert.assertTrue("the two graphs should be equals",gr.getGraph.equals(ez.getGraph)) //mutable graphs cannot be compared for equality

	}


	@Test
	def usingSymbolicArrows {
		import org.apache.clerezza.rdf.scala.utils.EasyGraph._
		import org.apache.clerezza.rdf.scala.utils.Lang._
		 val ez = new EasyGraph()
		 // example using arrows
		 (
		   ez.bnode("reto") ∈ FOAF.Person
			 ⟝ FOAF.name ⟶ "Reto Bachman-Gmür".lang(rm)
			 ⟝ FOAF.title ⟶ "Mr"
			 ⟝ FOAF.currentProject ⟶ "http://clerezza.org/".uri
			 ⟝ FOAF.knows ⟶ (
			     ez.u("http://bblfish.net/#hjs") ∈ FOAF.Person
			          ⟝ FOAF.name ⟶ "Henry Story"
			          ⟝ FOAF.currentProject ⟶ "http://webid.info/".uri
			          ⟵ identity ⟞ (
			              ez.bnode ∈ RSAPublicKey
			                 ⟝ modulus ⟶ 65537
			                 ⟝ public_exponent ⟶ (bblfishModulus^^hex) // brackets needed due to precedence
			          )
			          ⟝ FOAF.knows ⟶ ez.bnode("reto")
		 			    ⟝ FOAF.knows ⟶ ez.bnode("danny")
			 )
			 ⟝ FOAF.knows ⟶ (
			     ez.bnode("danny") ∈ FOAF.Person
			          ⟝ FOAF.name ⟶ "Danny Ayers".lang(en)
		             ⟝ FOAF.knows ⟶ "http://bblfish.net/#hjs".uri //knows
					    ⟝ FOAF.knows ⟶ ez.bnode("reto")
			 )
		 )
		Assert.assertEquals("the two graphs should be of same size",tinyGraph.size(),ez.size())
		Assert.assertEquals("Both graphs should contain exactly the same triples",tinyGraph,ez.getGraph)
		ez.bnode("danny") ⟝  FOAF.name ⟶  "George"
		Assert.assertNotSame("Added one more triple, so graphs should no longer be equal", tinyGraph,ez.getGraph)
	}

	@Test
	def usingAsciiArrows {
		import org.apache.clerezza.rdf.scala.utils.EasyGraph._
		import org.apache.clerezza.rdf.scala.utils.Lang._
		 val ez = new EasyGraph()
		 // example using arrows
		 (
		   ez.bnode("reto").a(FOAF.Person)
			 -- FOAF.name --> "Reto Bachman-Gmür".lang(rm)
			 -- FOAF.title --> "Mr"
			 -- FOAF.currentProject --> "http://clerezza.org/".uri
			 -- FOAF.knows --> (
			     ez.u("http://bblfish.net/#hjs").a(FOAF.Person)
			          -- FOAF.name --> "Henry Story"
			          -- FOAF.currentProject --> "http://webid.info/".uri
 			          -<- identity -- (
			                   ez.bnode.a(RSAPublicKey) //. notation because of precedence of operators
			                       -- modulus --> 65537
			                       -- public_exponent --> (bblfishModulus^^hex) // brackets needed due to precedence
			                   )
			          -- FOAF.knows --> ez.bnode("reto")
		 			    -- FOAF.knows --> ez.bnode("danny")
			 )
			 -- FOAF.knows --> (ez.bnode("danny").a(FOAF.Person)
			          -- FOAF.name --> "Danny Ayers".lang(en)
		             -- FOAF.knows --> "http://bblfish.net/#hjs".uri //knows
					    -- FOAF.knows --> ez.bnode("reto")
			 )
		 )
		Assert.assertEquals("the two graphs should be of same size",tinyGraph.size(),ez.size())
		Assert.assertEquals("Both graphs should contain exactly the same triples",tinyGraph,ez.getGraph)
		ez.bnode("danny") -- FOAF.name --> "George"
		Assert.assertNotSame("Added one more triple, so graphs should no longer be equal",tinyGraph,ez.getGraph)

	}


}
