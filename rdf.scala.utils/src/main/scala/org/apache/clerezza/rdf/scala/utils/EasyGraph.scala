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

import java.math.BigInteger
import java.lang.Boolean
import java.net.{URL, URI}
import org.apache.clerezza.rdf.ontologies.{XSD, RDF}
import java.util.{HashSet, Date}
import collection.mutable.{ListBuffer, HashMap}
import org.apache.clerezza.rdf.utils.{GraphNode, UnionMGraph}
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.core.impl._
import sun.security.krb5.internal.EncASRepPart

object EasyGraph {

	private val litFactory = LiteralFactory.getInstance

	implicit def string2lit(str: String) = new EzLiteral(str)

	implicit def lit2String(lit: Literal) = lit.getLexicalForm

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

	import EzStyleChoice.unicode

	//inspired from http://programming-scala.labs.oreilly.com/ch11.html

}

/**
 * An Easy Literal, contains functions for mapping literals to other literals, ie from String literals to
 * typed literals.
 */
class EzLiteral(lexicalForm: String) extends TypedLiteral {

	def unapply(lexical: String) = lexical

	/**
	 * @return a plain literal with language specified by lang
	 */
	def lang(lang: Lang) = new PlainLiteralImpl(lexicalForm, lang)
	def lang(lang: Symbol) = new PlainLiteralImpl(lexicalForm, new Language(lang.name)) //todo lookup in LangId instead

	def ^^(typ: UriRef) = new TypedLiteralImpl(lexicalForm, typ)

	def uri = new UriRef(lexicalForm)

	def getLexicalForm = lexicalForm

	override def equals(other: Any) = {
      other match {
			case olit: TypedLiteral => (olit eq this) || (olit.getLexicalForm == lexicalForm && olit.getDataType == this.getDataType)
			case _ => false
		}
	}

	override def hashCode() = XSD.string.hashCode() + lexicalForm.hashCode()

	def getDataType = XSD.string

	override def toString() = lexicalForm
}

abstract class EzStyle[T<:EasyGraphNode]() {
	def preferred(ref: NonLiteral, tc: TripleCollection):T
}

object EzStyleChoice {
	implicit val unicode = new EzStyle[EzGraphNodeU](){
		override def preferred(ref: NonLiteral, tc: TripleCollection): EzGraphNodeU = new EzGraphNodeU(ref,tc)
	}
	implicit val ascii = new EzStyle[EzGraphNodeA](){
		override def preferred(ref: NonLiteral, tc: TripleCollection): EzGraphNodeA = new EzGraphNodeA(ref,tc)
	}
   implicit val english = new EzStyle[EzGraphNodeEn](){
		override def preferred(ref: NonLiteral, tc: TripleCollection): EzGraphNodeEn = new EzGraphNodeEn(ref,tc)
	}


}
/**
 * This is really a TripleCollection , should it just extend a TC? Or a MGraph?
 *
 * @param graph: a Triple collection - or should it be an MGraph since it is really meant to be modifiable
 * @author hjs
 * @created: 20/04/2011
 */

class EasyGraph(val graph: HashSet[Triple]) extends SimpleMGraph(graph) {

	/*
	* because we can't jump straight to super constructor in Scala we need to
	* create the collection here
	**/
	def this() = this (new HashSet[Triple])


	/**
	 * Constructor for collection
	 * Because superclasses map copy information to a new HashSet, we do this now, so that this class can keep
	 * track of the container. If super class changes this may become unnecessary
	 */
	def this(tripleColl: java.util.Collection[Triple]) = this(new HashSet[Triple](tripleColl))

	def +=(other: Graph) = {
		if (graph ne other) graph.addAll(other)
	}

	def bnode[T<: EasyGraphNode](implicit writingStyle: EzStyle[T]=EzStyleChoice.unicode ): T = {
		apply(new BNode)(writingStyle)
	}

	val namedBnodes = new HashMap[String,BNode]
	def b_[T<: EasyGraphNode](name: String)(implicit writingStyle: EzStyle[T]=EzStyleChoice.unicode): T = {
		namedBnodes.get(name) match {
			case Some(bnode) => writingStyle.preferred(bnode,this)
			case None => {
				val bn = new BNode
				namedBnodes.put(name, bn);
				writingStyle.preferred(bn,this)
			}
		}
	}

	def u[T<: EasyGraphNode](url: String)(implicit writingStyle: EzStyle[T]=EzStyleChoice.unicode): T = {
		apply(new UriRef(url))(writingStyle)
	}

	def apply[T<: EasyGraphNode](subj: NonLiteral)(implicit writingStyle: EzStyle[T]=EzStyleChoice.unicode ): T = {
	 	writingStyle.preferred(subj,this)
//		new EasyGraphNode(subj, this)
	}

	/**
	 * Add a a relation
	 * @param subj: subject of relation
	 * @param relation: relation
	 * @param: obj: the object of the statement
	 * @return this, to making method chaining easier
	 */
	def add(subj: NonLiteral, relation: UriRef, obj: Resource ) = {
		graph.add(new TripleImpl(subj,relation,obj))
		graph
	}

	/**
	 * Add a type relation for the subject
	 * @param subj: the subject of the relation
	 * @param clazz: the rdfs:Class the subject is an instance of
	 * @return this, to making method chaining easier
	 */
	def addType(subj: NonLiteral, clazz: UriRef) = {
		graph.add(new TripleImpl(subj,RDF.`type`,clazz))
		graph
	}

	//note one could have an apply for a Literal that would return a InversePredicate
	//but that would require restructuring EasyGraphNode so that one can have an EasyGraphNode
	//with a missing ref, or perhaps a sublcass of EasyGraphnode that only has the <- operator available
}

class EzGraphNodeU(ref: NonLiteral, graph: TripleCollection) extends EasyGraphNode(ref, graph) {
	//
	// symbolic notation
	//
	// (shorter and more predictable precedence rules, but unicode issues)

	type T_Pred = PredicateU
	type T_InvP = InversePredicateU
	type T_EzGN = EzGraphNodeU

	override def make(ref: NonLiteral, graph: TripleCollection) = new EzGraphNodeU(ref,graph)
	override def predicate(rel: UriRef) = new PredicateU(rel)
	override def inverse(rel: UriRef) = new InversePredicateU(rel)


	def ⟝(rel: UriRef) = predicate(rel)

	def ⟝(rel: String) = predicate(new UriRef(rel))

	/* For inverse relationships */
	def ⟵(rel: UriRef) = inverse(rel)

	//
	// symbolic notation
	//
	// (shorter and more predictable precedence rules - they are always the weakest, and so very few brakets are need
	// when symbolic operators are used. But sometimes this notation is grammatically awkward)

	def ∈(rdfclass: UriRef) = a(rdfclass)

	class InversePredicateU(rel: UriRef) extends InversePredicate(rel) {
		def ⟞(subj: NonLiteral) = add(subj)

		def ⟞(subj: String) = add(new UriRef(subj))

		def ⟞(sub: EzGraphNodeU)  = addGN(sub)
	}

	class PredicateU(rel: UriRef) extends Predicate(rel) {


		def ⟶(obj: String) = add(new EzLiteral(obj))

		def ⟶(obj: Resource) = add(obj)

		def ⟶(list: List[Resource]) = addList(list)

		/**
		 * Add one relation for each member of the iterable collection
		 */
		def ⟶*[T <: Resource](objs: Iterable[T]) = addMany(objs)

		def ⟶(sub: EasyGraphNode) = addEG(sub)

	}


}


class EzGraphNodeA(ref: NonLiteral, graph: TripleCollection) extends EasyGraphNode(ref, graph) {

	type T_Pred = PredicateA
	type T_InvP = InversePredicateA
	type T_EzGN = EzGraphNodeA

	override def make(ref: NonLiteral, graph: TripleCollection) = new EzGraphNodeA(ref,graph)
	override def predicate(rel: UriRef) = new PredicateA(rel)
	override def inverse(rel: UriRef) = new InversePredicateA(rel)

	def --(rel: UriRef) = predicate(rel)

	def --(rel: String) = predicate(new UriRef(rel))

	/**
	 * we Can't have <-- as that messes up the balance of precedence
	 */
	def -<-(rel: UriRef) = inverse(rel)

	class PredicateA(rel: UriRef) extends Predicate(rel) {
		//
		// methods that do the work
		//
		def -->(obj: Resource) = add(obj)

		def -->(lit: String) = add(new EzLiteral(lit))

		/**
		 * Adds a relation to a real linked list.
		 * If you want one relation to each entry use -->> or ⟶*
		 */
		def -->(list: List[Resource]) = addList(list)

		def -->(sub: EasyGraphNode) = addEG(sub)

		/**
		 * Add one relation for each member of the iterable collection
		 */
		def -->>[T <: Resource](uris: Iterable[T]) = addMany(uris)


	}

	class InversePredicateA(ref: UriRef) extends InversePredicate(ref) {
		def --(subj: NonLiteral) = add(subj)
		def --(subj: String) = add(new UriRef(subj))
		def --(subj: EasyGraphNode) = addGN(subj)
		// since we can only have inverses from non literals (howto deal with bndoes?)
	}

}

class EzGraphNodeEn(ref: NonLiteral, graph: TripleCollection) extends EasyGraphNode(ref, graph) {

	type T_Pred = PredicateEn
	type T_InvP = InversePredicateEn
	type T_EzGN = EzGraphNodeEn

	override def make(ref: NonLiteral, graph: TripleCollection) = new EzGraphNodeEn(ref,graph)
	override def predicate(rel: UriRef) = new PredicateEn(rel)
	override def inverse(rel: UriRef) = new InversePredicateEn(rel)



	// does not worked as hoped, and does not look that good either
	//	def hasQ(yes: Boolean, rel: UriRef )(func: Predicate => EasyGraphNode): EasyGraphNode =
	//		if (yes) func(has(rel))
	//		else this



	def has(rel: UriRef) = predicate(rel)

	def has(rel: String) = predicate(new UriRef(rel))

	/* For inverse relationships */
	def is(rel: UriRef) = inverse(rel)

	class InversePredicateEn(rel: UriRef) extends InversePredicate(rel) {



		def of(subj: NonLiteral) = add(subj)
		def of(subj: String) = add(new UriRef(subj))
		def of(subj: EasyGraphNode) = addGN(subj)
	}

	class PredicateEn(rel: UriRef) extends Predicate(rel) {


		//
		// text notation
		//

		def to(lit: String) = add(new EzLiteral(lit))

		def to(obj: Resource) = add(obj)

		def to(list: List[Resource]) = addList(list)

		/**
		 * Add one relation for each member of the iterable collection
		 */
		def toEach[T <: Resource](objs: Iterable[T]) = addMany(objs)

		def to(sub: EasyGraphNode) = addEG(sub)

	}

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
abstract class EasyGraphNode(val ref: NonLiteral, val graph: TripleCollection) extends GraphNode(ref, graph) {

//	lazy val easyGraph = graph match {
//		case eg: EasyGraph => eg
//		case other: TripleCollection => new EasyGraph(graph)
//	}

	def +(sub: EasyGraphNode) = {
		if (graph ne sub.graph) graph.addAll(sub.graph)
		this
	}

	type T_Pred <: Predicate
	type T_InvP <: InversePredicate
	type T_EzGN <: EasyGraphNode

	protected def predicate(rel: UriRef): T_Pred
	protected def inverse(rel: UriRef): T_InvP

	def a(rdfclass: UriRef): T_EzGN = {
		graph.add(new TripleImpl(ref, RDF.`type`, rdfclass))
		return this.asInstanceOf[T_EzGN]
	}

	def make(ref: NonLiteral, graph: TripleCollection): T_EzGN

	/*
	 * create an EasyGraphNode from this one where the backing graph is protected from writes by a new
	 * SimpleGraph.
	 */
	def protect(): T_EzGN = make(ref, new UnionMGraph(new SimpleMGraph(), graph))

	def this(s: NonLiteral) = this (s, new SimpleMGraph())

	def this() = this (new BNode)

	abstract class InversePredicate(rel: UriRef) {

		protected def addGN(subj: EasyGraphNode) = {
			EasyGraphNode.this + subj
			add(subj.ref)
		}

		protected def add(subj: NonLiteral) = {
			graph.add(new TripleImpl(subj, rel, ref))
			EasyGraphNode.this.asInstanceOf[T_EzGN]
		}
	}

	abstract class Predicate(rel: UriRef) {


		protected def add(obj: Resource): T_EzGN = {
			addTriple(obj)
			EasyGraphNode.this.asInstanceOf[T_EzGN]
		}

		protected def addTriple(obj: Resource) {
			graph.add(new TripleImpl(ref, rel, obj))
		}

		protected def addMany[T<:Resource](uris: Iterable[T]): T_EzGN = {
			for (u <- uris) addTriple(u)
			EasyGraphNode.this.asInstanceOf[T_EzGN]
		}

		protected def addEG(sub: EasyGraphNode): T_EzGN = {
			EasyGraphNode.this + sub
			add(sub.ref)
		}

		private def toTriples[T <: Resource](head: NonLiteral,list : List[T]): List[Triple] = {
			val answer = new ListBuffer[Triple]
			var varList = list
			var headRef = head
			while (varList != Nil) {
				varList = varList match {
					case head :: next :: rest => {
						val nextRef = new BNode
						answer.append(new TripleImpl(headRef, RDF.first, head))
						answer.append(new TripleImpl(headRef, RDF.rest, nextRef))
						headRef = nextRef
						next :: rest
					}
					case head :: Nil => {
						answer.append(new TripleImpl(headRef, RDF.first, head))
						answer.append(new TripleImpl(headRef, RDF.rest, RDF.nil))
						Nil
					}
					case Nil => Nil
				}
			}
			answer.toList
		}


		protected def addList[T <: Resource](list: List[T]) = {
			val headNode = new BNode
			addTriple(headNode)
		   val tripleLst = toTriples(headNode,list);
			graph.add(new TripleImpl(headNode,RDF.`type`,RDF.List))
			graph.addAll(collection.JavaConversions.asJavaCollection(tripleLst))
			EasyGraphNode.this.asInstanceOf[T_EzGN]
		}

	}

}

