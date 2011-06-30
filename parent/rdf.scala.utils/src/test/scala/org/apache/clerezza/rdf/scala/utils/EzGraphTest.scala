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

import org.apache.clerezza.rdf.ontologies._
import org.junit._
import org.apache.clerezza.rdf.core._
import impl._

class EzGraphTest {

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

	val henryUri: String = "http://bblfish.net/#hjs"
	val retoUri: String = "http://farewellutopia.com/reto/#me"
	val danbriUri: String = "http://danbri.org/foaf.rdf#danbri"


	private val tinyGraph: Graph = {
		val gr = new SimpleMGraph
		val reto= new BNode()
		val danny = new BNode()
		val henry = new UriRef(henryUri)

		gr.add(new TripleImpl(reto,RDF.`type`, FOAF.Person))
		gr.add(new TripleImpl(reto,FOAF.name, new PlainLiteralImpl("Reto Bachman-Gmür", new Language("rm"))))
		//it is difficult to remember that one needs to put a string literal if one does not want to specify a language
		gr.add(new TripleImpl(reto,FOAF.title, new TypedLiteralImpl("Mr",XSD.string)))
		gr.add(new TripleImpl(reto,FOAF.currentProject, new UriRef("http://clerezza.org/")))
		gr.add(new TripleImpl(reto,FOAF.knows, henry))
		gr.add(new TripleImpl(reto,FOAF.knows, danny))

		gr.add(new TripleImpl(danny,FOAF.name,new PlainLiteralImpl("Danny Ayers", new Language("en"))))
		gr.add(new TripleImpl(danny,RDF.`type`, FOAF.Person))
		gr.add(new TripleImpl(danny,FOAF.knows, henry))
		gr.add(new TripleImpl(danny,FOAF.knows, reto))

		gr.add(new TripleImpl(henry,FOAF.name,new TypedLiteralImpl("Henry Story",XSD.string))) //It is tricky to remember that one needs this for pure strings
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
	def simpleGraphEquality {
		val gr = new SimpleMGraph

		val reto= new BNode()
		gr.add(new TripleImpl(reto,RDF.`type`, FOAF.Person))

		import EzStyleChoice.unicode
		val ez = EzGraph()
		ez.bnode ∈ FOAF.Person

		Assert.assertEquals("the two graphs should be of same size",gr.size(),ez.graph.size())
		Assert.assertEquals("the two graphs should be equals",gr.getGraph,new SimpleGraph(ez.graph)) //mutable graphs cannot be compared for equality

	}

	@Test
	def testList {
		val gr = new SimpleMGraph
		val reto= new UriRef(retoUri)
		val todoRef = new UriRef("http://clerezza.org/ex/ont/todo")
		val holiday = "http://dbpedia.org/resource/Holiday"
		val list = new BNode
		val list2 = new BNode
		val list3 = new BNode

		gr.add(new TripleImpl(reto, todoRef, list ))
		gr.add(new TripleImpl(list,RDF.`type`, RDF.List))
		gr.add(new TripleImpl(list,RDF.first, new PlainLiteralImpl("SPARQL update support",new Language("en"))))
		gr.add(new TripleImpl(list,RDF.rest,list2))
		gr.add(new TripleImpl(list2,RDF.first, new PlainLiteralImpl("XSPARQL support",new Language("en"))))
		gr.add(new TripleImpl(list2,RDF.rest,list3))
		gr.add(new TripleImpl(list3,RDF.first, new UriRef(holiday)))
		gr.add(new TripleImpl(list3,RDF.rest,RDF.nil))
		gr.add(new TripleImpl(reto,RDF.`type`, FOAF.Person))

		val ez = EzGraph()

		import org.apache.clerezza.rdf.scala.utils.EzGraph._
		import org.apache.clerezza.rdf.scala.utils.Lang._
		import org.apache.clerezza.rdf.scala.utils.EzStyleChoice.unicode
		( ez.u(retoUri) ∈ FOAF.Person
			    ⟝ todoRef ⟶ List[Resource]("SPARQL update support".lang(en),"XSPARQL support".lang(en),holiday.uri))

		Assert.assertEquals("the two graphs should be of same size",gr.size(),ez.graph.size())
		Assert.assertEquals("Both graphs should contain exactly the same triples",gr.getGraph,new SimpleGraph(ez.graph)) //mutable graphs cannot be compared for equality

	}

	@Test
	def oneToMany {
		val gr = new SimpleMGraph
		val reto= new UriRef(retoUri)
		gr.add(new TripleImpl(reto,FOAF.knows,new UriRef(henryUri)))
		gr.add(new TripleImpl(reto,FOAF.knows,new UriRef(danbriUri)))

		val ez = EzGraph()
		import org.apache.clerezza.rdf.scala.utils.EzGraph._
		//default style is now arrow
		(ez.u(retoUri) -- FOAF.knows -->> List(henryUri.uri,danbriUri.uri))

		Assert.assertEquals("the two graphs should be of same size",gr.size(),ez.graph.size())
		Assert.assertEquals("Both graphs should contain exactly the same triples",gr.getGraph,new SimpleGraph(ez.graph)) //mutable graphs cannot be compared for equality

		val ez2 = EzGraph()
		(ez2.u(retoUri)(EzStyleChoice.unicode) ⟝  FOAF.knows ⟶*  Set(danbriUri.uri,henryUri.uri))

		Assert.assertEquals("the two graphs should be of same size",gr.size(),ez2.graph.size())
		Assert.assertEquals("Both graphs should contain exactly the same triples",gr.getGraph,new SimpleGraph(ez2.graph)) //mutable graphs cannot be compared for equality

	}

	@Test
	def langEquals {
		import org.apache.clerezza.rdf.scala.utils.EzGraph._
		import org.apache.clerezza.rdf.scala.utils.Lang._

		 val lit = new PlainLiteralImpl("SPARQL update support",new Language("en"))
		 val lit2 =  "SPARQL update support".lang(en)

		Assert.assertEquals("the two literals should be equsl",lit.hashCode(),lit2.hashCode())
		Assert.assertEquals("the two literals should be equsl",lit,lit2)

		val lit3 = new PlainLiteralImpl("Reto Bachman-Gmür",new Language("rm"))
		val lit4 = "Reto Bachman-Gmür".lang(rm)

		Assert.assertEquals("the two lang literals should have same hash",lit3.hashCode(),lit4.hashCode())
		Assert.assertEquals("the two lang literals should be equal",lit3,lit4)


	}

	@Test
	def uriEquals {
		import org.apache.clerezza.rdf.scala.utils.EzGraph._
		val uc = new UriRef("http://clerezza.org/")
		val ec = "http://clerezza.org/".uri

		Assert.assertEquals("the two uris should have an equal hash",uc.hashCode(),ec.hashCode())
		Assert.assertEquals("the two uris should be equal",uc,ec)


	}

	@Test
	def literalTester1 {
		val n3 = """
		@prefix foaf: <http://xmlns.com/foaf/0.1/> .
		<http://bblfish.net/#hjs> a foaf:Person .
		"""
		val n3Lit : Literal = new TypedLiteralImpl(n3,new UriRef("http://example.com/turtle"))
	   val gr = new SimpleMGraph
		gr.add(new TripleImpl(new BNode,OWL.sameAs,n3Lit))

		import EzGraph._
		import EzStyleChoice.unicode
		val ez = EzGraph()

		(ez.bnode ⟝  OWL.sameAs ⟶  (n3^^"http://example.com/turtle".uri))

		Assert.assertEquals("Both graphs should contain exactly the same triples",gr.getGraph,new SimpleGraph(ez.graph)) //mutable graphs cannot be compared for equality

	}

	@Test
	def literalTester2 {
		val exp = LiteralFactory.getInstance().createTypedLiteral(65537)
		val mod= new TypedLiteralImpl(bblfishModulus,hex)

		import org.apache.clerezza.rdf.scala.utils.EzGraph._
		val modZ: TypedLiteral = bblfishModulus^^hex
		val expZ: TypedLiteral = 65537

		Assert.assertEquals("the two literals should have an equal hash",exp.hashCode(),expZ.hashCode())
		Assert.assertEquals("the two literals should be equal",exp,expZ)

		Assert.assertEquals("the two literals should have an equal hash",mod.hashCode(),modZ.hashCode())
		Assert.assertEquals("the two literals should be equal",mod,modZ)

	}


	@Test
	def usingSymbolicArrows {
		import org.apache.clerezza.rdf.scala.utils.EzGraph._
		import org.apache.clerezza.rdf.scala.utils.Lang._
		val ez = EzGraph()
		import EzStyleChoice.unicode //in IntelliJ this is needed for the moment to remove the red lines
		 // example using arrows
		 (
		   ez.b_("reto") ∈ FOAF.Person
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
			          ⟝ FOAF.knows ⟶* Set(ez.b_("reto").ref,ez.b_("danny").ref)
			 )
			 ⟝ FOAF.knows ⟶ (
			     ez.b_("danny") ∈ FOAF.Person
			          ⟝ FOAF.name ⟶ "Danny Ayers".lang(en)
		             ⟝ FOAF.knows ⟶ "http://bblfish.net/#hjs".uri //knows
					    ⟝ FOAF.knows ⟶ ez.b_("reto")
			 )
		 )
		Assert.assertEquals("the two graphs should be of same size",tinyGraph.size(),ez.graph.size())
		Assert.assertEquals("Both graphs should contain exactly the same triples",tinyGraph,new SimpleGraph(ez.graph)) //mutable graphs cannot be compared for equality
		ez.b_("danny") ⟝  FOAF.name ⟶  "George"
		Assert.assertNotSame("Added one more triple, so graphs should no longer be equal", tinyGraph,ez.graph)
	}

	@Test
	def usingAsciiArrows {
		import org.apache.clerezza.rdf.scala.utils.EzGraph._
		import org.apache.clerezza.rdf.scala.utils.Lang._
		import EzStyleChoice.arrow
		 val ez = EzGraph()
		 // example using arrows
		 (
		   ez.b_("reto").a(FOAF.Person)
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
			          -- FOAF.knows -->> List(ez.b_("reto").ref,ez.b_("danny").ref)
			 )
			 -- FOAF.knows --> (ez.b_("danny").a(FOAF.Person)
			          -- FOAF.name --> "Danny Ayers".lang(en)
		             -- FOAF.knows --> "http://bblfish.net/#hjs".uri //knows
					    -- FOAF.knows --> ez.b_("reto")
			 )
		 )
		Assert.assertEquals("the two graphs should be of same size",tinyGraph.size(),ez.graph.size())
		Assert.assertEquals("Both graphs should contain exactly the same triples",tinyGraph,new SimpleGraph(ez.graph)) //mutable graphs cannot be compared for equality
		ez.b_("danny") -- FOAF.name --> "George"
		Assert.assertNotSame("Added one more triple, so graphs should no longer be equal",tinyGraph,ez.graph)

	}


	@Test
	def usingWordOperators {
		import org.apache.clerezza.rdf.scala.utils.EzGraph._
		import org.apache.clerezza.rdf.scala.utils.Lang._
		import EzStyleChoice.english

		 val ez = EzGraph()
		 // example using arrows
		 (
		   ez.b_("reto").asInstanceOf[EzGraphNodeEn] a FOAF.Person
			 has FOAF.name to "Reto Bachman-Gmür".lang(rm)
			 has FOAF.title to "Mr"
			 has FOAF.currentProject to "http://clerezza.org/".uri
			 has FOAF.knows to (
			     ez.u("http://bblfish.net/#hjs") a FOAF.Person
			          has FOAF.name to "Henry Story"
			          has FOAF.currentProject to "http://webid.info/".uri
 			          is identity of (
			                   ez.bnode a RSAPublicKey //. notation because of precedence of operators
			                       has modulus to 65537
			                       has public_exponent to bblfishModulus^^hex // brackets needed due to precedence
			                   )
			          has FOAF.knows toEach List(ez.b_("reto").ref,ez.b_("danny").ref)
			 )
			 has FOAF.knows to ( ez.b_("danny") a FOAF.Person
			          has FOAF.name to "Danny Ayers".lang(en)
		             has FOAF.knows to "http://bblfish.net/#hjs".uri //knows
					    has FOAF.knows to ez.b_("reto")
			 )
		 )
		Assert.assertEquals("the two graphs should be of same size",tinyGraph.size(),ez.graph.size())
		Assert.assertEquals("Both graphs should contain exactly the same triples",tinyGraph,new SimpleGraph(ez.graph)) //mutable graphs cannot be compared for equality
		ez.b_("danny") has FOAF.name to "George"
		Assert.assertNotSame("Added one more triple, so graphs should no longer be equal",tinyGraph,new SimpleGraph(ez.graph))

	}

}
