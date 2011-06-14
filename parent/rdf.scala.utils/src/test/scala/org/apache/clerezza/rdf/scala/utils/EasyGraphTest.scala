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

class EasyGraphTest {


	private val tinyGraph: Graph = {
		val gr = new SimpleMGraph
		val subj= new BNode()
		gr.add(new TripleImpl(subj,RDF.`type`, FOAF.Person))
		gr.add(new TripleImpl(subj,FOAF.knows, new UriRef("http://bblfish.net/#hjs")))
		gr.add(new TripleImpl(subj,FOAF.name, new PlainLiteralImpl("Henry Story", new Language("en"))))
		val other = new BNode()
		gr.add(new TripleImpl(subj,FOAF.knows, other))
		gr.add(new TripleImpl(subj,FOAF.name,new PlainLiteralImpl("Danny Ayers")))
		gr.getGraph
	}

	@Test
	def plainChracter {
		/*val simpleMGraph = new SimpleMGraph
		val g = new EasyGraph(simpleMGraph)
		val sub = g.bnode
		( g.u("http://bblfish.net/#hjs") a FOAF.Person
		 has FOAF.name to {"Henry Story"}
		)
		
		Assert.assertEquals(tinyGraph, simpleMGraph.getGraph)*/
	}

	@Test
	def usingArrows {

		/*
		 // example using arrows
		 (
		 sub ∈ FOAF.Person
		 ⟝ FOAF.knows ⟶  "http://bblfish.net/#hjs".uri
		 ⟝ FOAF.name ⟶ "Henry Story"(en)
		 ⟝ FOAF.title ⟶ "Software"+" Architect"
		 ⟝ FOAF.knows ⟶ ( g.bnode ⟝ FOAF.name ⟶ "Danny Ayers" )
		 )
		 */
	}

	@Test
	def usingBrackets {
		/*
		 // example using just brackets ( the apply() method )
		 ( g.bnode(FOAF.knows)("http://bblfish.net/#hjs".uri,"http://farewellutopia.com/#me".uri)
		 (FOAF.knows)(g.bnode(FOAF.name)("Danny Ayers"('en)))
		 )
		 */
	}


}
