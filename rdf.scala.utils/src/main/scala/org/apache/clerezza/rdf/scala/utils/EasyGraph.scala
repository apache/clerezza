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

import org.apache.clerezza.rdf.core._
import impl._
import org.apache.clerezza.rdf.ontologies.RDF
import java.math.BigInteger
import java.util.Date
import java.lang.Boolean
import java.net.{URL, URI}
import org.apache.clerezza.rdf.core._
import reflect.Apply
import org.apache.clerezza.rdf.utils.{UnionMGraph, GraphNode}

object EasyGraph {

	private val litFactory = LiteralFactory.getInstance

	implicit def string2lit(str: String) = new PlainLiteralScala(str)

	implicit def date2lit(date: Date) = litFactory.createTypedLiteral(date)

	implicit def int2lit(int: Int) = litFactory.createTypedLiteral(int)

	implicit def bigint2lit(bint: BigInt) = litFactory.createTypedLiteral(bint.underlying())

	implicit def bigint2lit(bigInt: BigInteger) = litFactory.createTypedLiteral(bigInt)

	implicit def bool2lit(boolean: Boolean) = litFactory.createTypedLiteral(boolean)

	implicit def scalaBool2lit(boolean: scala.Boolean) = litFactory.createTypedLiteral(boolean)

	implicit def long2lit(long: Long) = litFactory.createTypedLiteral(long)

	implicit def double2lit(double: Double) = litFactory.createTypedLiteral(double)

	implicit def uriRef2Prefix(uriRef: UriRef) = new NameSpace(uriRef.getUnicodeString)

	implicit def URItoUriRef(uri: URI) = new UriRef(uri.toString)

	implicit def URLtoUriRef(url: URL) = new UriRef(url.toExternalForm)


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
	//		      (FOAF.knows)(g.bnode(FOAF.name)("Danny Ayers"('en)))
	//	)

	// should work like http://programming-scala.labs.oreilly.com/ch11.html

}

/**
 * An implementation of PlainLiteral for Scala that allows some automatic conversions to happen
 * when combined with the implicit defs in the EasyGraph object
 */
class PlainLiteralScala(string: String) extends PlainLiteralImpl(string) {

	/**
	 * @return a plain literal with language specified by lang
	 */
	def apply(lang: String) = new PlainLiteralImpl(string, new Language(lang))

	/**
	 * @return a plain literal with language specified by lang
	 */
	def apply(lang: Symbol) = new PlainLiteralImpl(string, new Language(lang.name))

	def ^^(typ: UriRef) = new TypedLiteralImpl(string, typ)

	def uri = new UriRef(string)

}


/**
 * This is really a TripleCollection , should it just extend a TC? Or a MGraph?
 *
 * @param graph: a Triple collection - or should it be an MGraph since it is really meant to be modifiable
 * @author hjs
 * @created: 20/04/2011
 */

@deprecated("Don't use yet other than for trying out this class as it may be merged with another class or changed dramatically." +
	" Send feedback to CLEREZZA-510. ")
class EasyGraph(val graph: TripleCollection) extends SimpleMGraph(graph) {

	/*
	* because we can't jump straight to super constructor in Scala we need to
	* create the collection here
	**/
	def this() = this (new SimpleMGraph())

	def +=(other: Graph) = {
		if (graph ne other) graph.addAll(other)
	}

	def bnode: EasyGraphNode = {
		new EasyGraphNode(new BNode(), graph)
	}

	def u(url: String) = new EasyGraphNode(new UriRef(url), this)

	def apply(subj: NonLiteral) = new EasyGraphNode(subj, this)

	/**
	 * Add a a relation
	 * @param subj: subject of relation
	 * @param relation: relation
	 * @param: obj: the object of the statement
	 * @return this, to making method chaining easier
	 */
	def add(subj: NonLiteral, relation: UriRef, obj: Resource ) = {
		graph.add(new TripleImpl(subj,relation,obj))
		this
	}

	/**
	 * Add a type relation for the subject
	 * @param subj: the subject of the relation
	 * @param clazz: the rdfs:Class the subject is an instance of
	 * @return this, to making method chaining easier
	 */
	def addType(subj: NonLiteral, clazz: UriRef) = {
		graph.add(new TripleImpl(subj,RDF.`type`,clazz))
		this
	}

	//note one could have an apply for a Literal that would return a InversePredicate
	//but that would require restructuring EasyGraphNode so that one can have an EasyGraphNode
	//with a missing ref, or perhaps a sublcass of EasyGraphnode that only has the <- operator available
}


/**
 *
 * Because of operator binding rules all the mathematical operators should
 * be used together as they bind at the same strength. Since they bind strongest
 * other operators will need to be strengthened with parenthesis, such as when adding strings
 *
 * @prefix graph: should this be an MGraph, since the EasyGraphNode is really designed for editing
 *
 */
class EasyGraphNode(val ref: NonLiteral, val graph: TripleCollection) extends GraphNode(ref, graph) {

	lazy val easyGraph = graph match {
		case eg: EasyGraph => eg
		case other: TripleCollection => new EasyGraph(graph)
	}

	/*
	 * create an EasyGraphNode from this one where the backing graph is protected from writes by a new
	 * SimpleGraph.
	 */
	def protect(): EasyGraphNode = new EasyGraphNode(ref, new UnionMGraph(new SimpleMGraph(), graph))

	def this(s: NonLiteral) = this (s, new SimpleMGraph())

	def this() = this (new BNode)

	def apply(rel: UriRef): Predicate = has(rel)

	def apply(rel: String): Predicate = has(rel)

	def has(rel: UriRef): Predicate = new Predicate(rel)

	def has(rel: String): Predicate = new Predicate(new UriRef(rel))

	// does not worked as hoped, and does not look that good either
	//	def hasQ(yes: Boolean, rel: UriRef )(func: Predicate => EasyGraphNode): EasyGraphNode =
	//		if (yes) func(has(rel))
	//		else this


	def ⟝(rel: UriRef): Predicate = has(rel)

	def ⟝(rel: String): Predicate = has(rel)

	/* For inverse relationships */
	def ⟵(rel: UriRef) = new InversePredicate(rel)

	// does not work as hoped
	//	def ⟝?(yes: Boolean, uri: UriRef)(func: Predicate => EasyGraphNode): EasyGraphNode = hasQ(yes,uri)(func)

	def +(sub: EasyGraphNode) = {
		if (graph ne sub.graph) graph.addAll(sub.graph)
		this
	}

	def a(rdfclass: UriRef) = ∈(rdfclass)

	def ∈(rdfclass: UriRef): EasyGraphNode = {
		graph.add(new TripleImpl(ref, RDF.`type`, rdfclass))
		return EasyGraphNode.this
	}

	class InversePredicate(rel: UriRef) {
		def ⟞(subj: NonLiteral) = add(subj)

		def ⟞(subj: String) = add(new UriRef(subj))

		// since we can only have inverses from non literals (howto deal with bndoes?)

		protected def add(subj: NonLiteral) = {
			graph.add(new TripleImpl(subj, rel, ref))
			EasyGraphNode.this
		}
	}

	class Predicate(rel: UriRef) {

		//
		// methods that do the work
		//
		def to(obj: Resource): EasyGraphNode = add(obj)

		/* add a relation to each object in the argument list */
		def to(objs: Resource*): EasyGraphNode = {
			for (o <- objs) add(o)
			EasyGraphNode.this
		}

		def to[T <: Resource](objs: Iterable[T]): EasyGraphNode = {
			for (o <- objs) add(o)
			EasyGraphNode.this
		}

		def to(uri: String): EasyGraphNode = add(new PlainLiteralImpl(uri))

		def to(sub: EasyGraphNode): EasyGraphNode = {
			EasyGraphNode.this + sub
			add(sub.ref)
		}

		def toUri(uri: String) = add(new UriRef(uri))

		def toUris(uris: Seq[String]) = {
			for (u <- uris) add(new UriRef(u))
			EasyGraphNode.this
		}

		//
		//apply method allows turns brackets () into an equivalent of <rel>
		//
		def apply(obj: Resource) = to(obj)

		def apply(objs: Resource*) = to(objs: _*)

		def apply(uri: String) = to(uri)

		def apply(sub: EasyGraphNode) = to(sub)

		//
		// arrow notation
		//
		// todo: a relation to a list

		def ⟶(obj: String) = to(obj)

		def ⟶(obj: Resource) = to(obj)

		def ⟶*[T <: Resource](objs: Iterable[T]) = to(objs)

		def ⟶(sub: EasyGraphNode) = to(sub)

		protected def add(obj: Resource) = {
			graph.add(new TripleImpl(ref, rel, obj))
			EasyGraphNode.this
		}
	}

}

/**
 * A IRI-namespace prefix
 */
//Or should this be called EasyNameSpace ?
class NameSpace(prefix: String) {

	/**
	 * returns a UriRef applying this namespace prefix to the given symbol
	 */
	def +(s: Symbol) = new UriRef(prefix + s.name)

	/**
	 * returns a UriRef applying this prefix to the given string
	 */
	def +(s: String) = new UriRef(prefix + s)
}
