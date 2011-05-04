package org.apache.clerezza.rdf.scala.utils

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

import org.apache.clerezza.rdf.core._
import collection.mutable.Queue
import impl._
import org.apache.clerezza.rdf.ontologies.{RDF, RDFS, FOAF}
import java.math.BigInteger
import java.util.Date

object EasyGraph {
	final val en = "en"
	final val de = "de"
	final val fr = "fr"
	val litFactory = new SimpleLiteralFactory()

	implicit def string2lit(str: String) = new PlainLiteralScala(str)
	implicit def date2lit(date: Date) = litFactory.createTypedLiteral(date)
	implicit def int2lit(date: Int) = litFactory.createTypedLiteral(date)



//	val g = new EasyGraph(new SimpleMGraph)
//	val sub = g.bnode

	// example using old graph notation
	// of course the add method could be overloaded to take triples, but it is still very repetitive

//	val gr = new SimpleMGraph
//	val subj= new BNode()
//	gr.add(new TripleImpl(subj,RDF.`type`, FOAF.Person))
//	gr.add(new TripleImpl(subj,FOAF.knows, new UriRef("http://bblfish.net/#hjs")))
//	gr.add(new TripleImpl(subj,FOAF.name, new PlainLiteralImpl("Henry Story","en")))
//	val other = new BNode()
//	gr.add(new TripleImpl(subj,FOAF.knows, other))
//	gr.add(new TripleImpl(subj,FOAF.name,new PlainLiteralImpl("Danny Ayers")))
//
//	//Example using english easy to type non unicode chars and simple object typing
//	( g.u("http://bblfish.net/#hjs") a FOAF.Person
//		 has FOAF.knows toUris Seq("http://www.w3.org/People/Connolly/#me", "http://farewellutopia.com/#me")
//		 has FOAF.name to {"Henry "+ " Story"}
//		 hasQ (true, FOAF.depiction){ p =>  p.to(new UriRef("hello")) }
//		)
//
//
//	   // example using arrows
//		(
//			sub ∈ FOAF.Person
//				⟝ FOAF.knows ⟶  "http://bblfish.net/#hjs".uri
//			   ⟝ FOAF.name ⟶ "Henry Story"(en)
//				⟝ FOAF.title ⟶ "Software"+" Architect"
//			   ⟝ FOAF.knows ⟶ ( g.bnode ⟝ FOAF.name ⟶ "Danny Ayers" )
//		)
//
//	// example using just brackets ( the apply() method )
//	( g.bnode(FOAF.knows)("http://bblfish.net/#hjs".uri,"http://farewellutopia.com/#me".uri)
//		      (FOAF.knows)(g.bnode(FOAF.name)("Danny Ayers"(en)))
//	)

// should work like http://programming-scala.labs.oreilly.com/ch11.html

}

class PlainLiteralScala(string: String) extends PlainLiteralImpl(string) {

	def apply(lang: String) = new PlainLiteralImpl(string, new Language(lang) )
	def ^^(typ: UriRef) = new TypedLiteralImpl(string, typ)
	def uri = new UriRef(string)

}


/**
 *  This is really a TripleCollection
 *
 * @author hjs
 * @created: 20/04/2011
 */

class EasyGraph(val graph: TripleCollection) {

	def +=(sub: SubjectGraph) = {
		  if (graph ne  sub.graph) graph.addAll(sub.graph)
	}

	 def bnode : SubjectGraph = {
		new SubjectGraph(new BNode(), graph)
	 }

	def u(url: String) = new SubjectGraph(new UriRef(url),graph)

}


/**
 * This is really a GraphNode
 *
 * Because of operator binding rules all the mathamatical operators should
 * be used together as they bind at the same strenght. Since they bind strongest
 * other operators will need to be strengthened with parenthesis, such as when addding strings
 *
 */
class SubjectGraph(val ref: NonLiteral, val graph: TripleCollection) {

	def this(s: NonLiteral)  = this(s,new SimpleMGraph())
	def this() = this(new BNode)

	def apply(rel: UriRef): Predicate = has(rel)
	def apply(rel: String): Predicate = has(rel)

	def has(rel: UriRef): Predicate = new Predicate(rel)
	def has(rel: String): Predicate = new Predicate(new UriRef(rel))

// does not worked as hoped, and does not look that good either
//	def hasQ(yes: Boolean, rel: UriRef )(func: Predicate => SubjectGraph): SubjectGraph =
//		if (yes) func(has(rel))
//		else this


	def ⟝(rel: UriRef): Predicate = apply(rel)
	def ⟝(rel: String): Predicate = apply(rel)

// does not work as hoped
//	def ⟝?(yes: Boolean, uri: UriRef)(func: Predicate => SubjectGraph): SubjectGraph = hasQ(yes,uri)(func)

	def +(sub: SubjectGraph) = {
		  if (graph ne sub.graph) graph.addAll(sub.graph)
		  this
	}

	def a(rdfclass: UriRef) = ∈(rdfclass)
	def ∈(rdfclass: UriRef) : SubjectGraph = {
		graph.add(new TripleImpl(ref,RDF.`type`,rdfclass))
		return SubjectGraph.this
	}


	class Predicate(rel: UriRef) {

		//
		// methods that do the work
		//
		def to(obj: Resource): SubjectGraph = add(obj)

		/* add a relation to each object in the argument list */
		def to(objs: Resource*): SubjectGraph = {
			for (o <- objs) add(o)
			SubjectGraph.this
		}
		def to[T<:Resource](objs: Iterable[T]): SubjectGraph = {
			for (o <- objs) add(o)
			SubjectGraph.this
		}
		def to(uri: String) : SubjectGraph = add(new PlainLiteralImpl(uri))
		def to(sub: SubjectGraph): SubjectGraph = {
			SubjectGraph.this + sub
			add(sub.ref)
		}

		def toUri(uri: String) = add(new UriRef(uri))
		def toUris(uris: Seq[String]) = {
			for (u <- uris) add(new UriRef(u))
			SubjectGraph.this
		}

		//
		//apply method allows turns brackets () into an equivalent of <rel>
		//
		def apply(obj: Resource) = to(obj)
		def apply(objs: Resource*) = to(objs: _*)
		def apply(uri: String) = to(uri)
		def apply(sub: SubjectGraph) = to(sub)

		//
		// arrow notation
		//
		// todo: a relation to a list

		def ⟶ (obj: String) = to(obj)
		def ⟶ (obj: Resource) = to(obj)
		def ⟶* [T<:Resource](objs: Iterable[T]) = to(objs)
		def ⟶ (sub: SubjectGraph) = to(sub)

		protected def add(obj: Resource) =  {
			graph.add(new TripleImpl(ref,rel,obj))
			SubjectGraph.this
		}
	}
}
