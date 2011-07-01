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
import java.util.Date
import collection.mutable.{ListBuffer, HashMap}
import org.apache.clerezza.rdf.utils.{GraphNode, UnionMGraph}
import org.apache.clerezza.rdf.core._
import impl._

object EzGraph {

	def apply(graph: TripleCollection) = new EzGraph(graph)
	def apply() = new EzGraph()

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

	//inspired from http://programming-scala.labs.oreilly.com/ch11.html

}

/**
 * An easy Literal implementations, contains functions for mapping literals to other literals, ie from String literals to
 * typed literals.
 */
class EzLiteral(lexicalForm: String) extends TypedLiteralImpl(lexicalForm,XSD.string) {

	/**
	 * @return a plain literal with language specified by lang
	 */
	def lang(lang: Lang) = new PlainLiteralImpl(lexicalForm, lang)
	def lang(lang: Symbol) = new PlainLiteralImpl(lexicalForm, new Language(lang.name)) //todo lookup in LangId instead

	/**
	 * Map to a Typed Literal of given type
	 */
	def ^^(typ: UriRef) = new TypedLiteralImpl(lexicalForm, typ)

	/**
	 * Map to a URI of given lexical form
	 */
	def uri = new UriRef(lexicalForm)

}

/**
 * A way to select one's preferred writing style
 */
abstract class EzStyle[T<:EzGraphNode]() {
	def preferred(ref: NonLiteral, tc: TripleCollection):T
}

/**
 * import your preferred writing styles into your code
 */
object EzStyleChoice {
	implicit val unicode = new EzStyle[EzGraphNodeU](){
		override def preferred(ref: NonLiteral, tc: TripleCollection): EzGraphNodeU = new EzGraphNodeU(ref,tc)
	}
	implicit val arrow = new EzStyle[EzGraphNodeA](){
		override def preferred(ref: NonLiteral, tc: TripleCollection): EzGraphNodeA = new EzGraphNodeA(ref,tc)
	}
   implicit val english = new EzStyle[EzGraphNodeEn](){
		override def preferred(ref: NonLiteral, tc: TripleCollection): EzGraphNodeEn = new EzGraphNodeEn(ref,tc)
	}


}
/**
 * EzGraph enhances graph writing. Used together with EzGraphNode, it can make writing rdf graphs in code a lot more
 * readable, as it avoids a lot of repetition.
 *
 * @param graph: a Triple collection - or should it be an MGraph since it is really meant to be modifiable
 * @author hjs
 * @created: 20/04/2011
 */
//todo: should this take a TripleCollection or a Set[Triple]
class EzGraph(val baseTc: TripleCollection) extends AbstractMGraph {

	def this() = this (new SimpleMGraph())

	def performFilter(subject: NonLiteral, predicate: UriRef,
			obj: Resource): java.util.Iterator[Triple] = baseTc.filter(subject, predicate, obj)

	override def size = baseTc.size

	override def add(t: Triple) = baseTc.add(t)

	/**
	 * Add all triples into the other graph to this one
	 */
	def +=(other: Graph) = {
		if (baseTc ne other) baseTc.addAll(other)
	}

	/**
	 * create a new bnode based EzGraphNode with the preferred writing style
	 */
	def bnode[T<: EzGraphNode](implicit writingStyle: EzStyle[T]=EzStyleChoice.arrow ): T = {
		node(new BNode)(writingStyle)
	}

	protected val namedBnodes = new HashMap[String,BNode]

	/**
	 * create a new named bnode based EzGraphNode with the preferred writing style
	 */
	def b_[T<: EzGraphNode](name: String)(implicit writingStyle: EzStyle[T]=EzStyleChoice.arrow): T = {
		namedBnodes.get(name) match {
			case Some(bnode) => writingStyle.preferred(bnode,baseTc)
			case None => {
				val bn = new BNode
				namedBnodes.put(name, bn);
				writingStyle.preferred(bn,baseTc)
			}
		}
	}

	/**
	 * create a new url based EzGraphNode with the preferred writing style
	 */
	def u[T<: EzGraphNode](url: String)(implicit writingStyle: EzStyle[T]=EzStyleChoice.arrow): T = {
		node(new UriRef(url))(writingStyle)
	}

	/**
	 * create a new Resource based EzGraphNode with the preferred writing style.
	 * The EzGraphNode will contain the graph that this EzGraph is built on and point to the given subj
	 */
	def node[T<: EzGraphNode](subj: NonLiteral)(implicit writingStyle: EzStyle[T]=EzStyleChoice.arrow ): T = {
	 	writingStyle.preferred(subj,baseTc)
	}

	/**
	 * Add a a relation
	 * @param subj: subject of relation
	 * @param relation: relation
	 * @param: obj: the object of the statement
	 * @return this, to making method chaining easier
	 */
	def add(subj: NonLiteral, relation: UriRef, obj: Resource ) = {
		baseTc.add(new TripleImpl(subj,relation,obj))
		baseTc
	}

	/**
	 * Add a type relation for the subject
	 * @param subj: the subject of the relation
	 * @param clazz: the rdfs:Class the subject is an instance of
	 * @return this, to making method chaining easier
	 */
	def addType(subj: NonLiteral, clazz: UriRef) = {
		baseTc.add(new TripleImpl(subj,RDF.`type`,clazz))
		baseTc
	}


}

/**
 * Unicode arrow notation for EzGraphNode. Very clean, short and efficient, but unicode values may not
 * be available everywhere yet.
 *
 * Because of operator binding rules all the mathematical operators  bind the strongest. This means that
 * anything outside of these operators should be put into brackets or use dot notation if you wish them to
 * bind more tightly.
 *
 *
 * @prefix graph: should this be an MGraph, since the EzGraphNode is really designed for editing
 *
 */
class EzGraphNodeU(ref: NonLiteral, graph: TripleCollection) extends EzGraphNode(ref, graph) {
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

	/**
	 * relate the subject via the given relation to....
	 */
	def ⟝(rel: UriRef) = predicate(rel)

	/**
	 * relate the subject via the inverse of the given relation to....
	 */
	def ⟵(rel: UriRef) = inverse(rel)

	/**
	 * the subject is a member of the given class
	 */
	def ∈(rdfclass: UriRef) = a(rdfclass)


	class InversePredicateU(rel: UriRef) extends InversePredicate(rel) {
		/**
		 * ...to the following non literal
		 */
		def ⟞(subj: NonLiteral) = add(subj)

		/**
		 * ...to the following EzGraphNode
		 * (useful for opening a new parenthesis and specifying other things in more detail
		 */
		def ⟞(sub: EzGraphNodeU)  = addGN(sub)
	}

	class PredicateU(rel: UriRef) extends Predicate(rel) {


		/**
		 * ...to the following resource
		 */
		def ⟶(obj: Resource) = add(obj)

		/**
		 * ...to the following list as an RDF List
		 */
		def ⟶(list: List[Resource]) = addList(list)

		/**
		 * Add one relation for each member of the iterable collection
		 */
		def ⟶*[T <: Resource](objs: Iterable[T]) = addMany(objs)

		/**
		 * ...to the EzGraphNode, which is useful for opening a parenthesis.
		 */
		def ⟶(sub: EzGraphNode) = addEG(sub)

	}


}

/**
 * Ascii Arrow notation for EzGraphNode. This is easy to write using an ascii keyboard but because
 * of operator precedence rules, some operators will have higher and some lower precedence to these
 * meaning that one has to keep in mind these rules:
 *
 * <blockquote>
      The precedence of an infix operator is determined by the operator’s first character.
      Characters are listed below in increasing order of precedence, with characters on
      the same line having the same precedence.
              (all letters)
               |
               ^
              &
              < >
              = !
              :
             + -
             * / %
           (all other special characters)

    That is, operators starting with a letter have lowest precedence, followed by operators
    starting with ‘|’, etc.
    There’s one exception to this rule, which concerns assignment operators(§6.12.4).
    The precedence of an assigment operator is the same as the one of simple assignment
    (=). That is, it is lower than the precedence of any other operator.
    The associativity of an operator is determined by the operator’s last character. Operators
    ending in a colon ‘:’ are right-associative. All other operators are leftassociative.
 </blockquote>
 */
class EzGraphNodeA(ref: NonLiteral, graph: TripleCollection) extends EzGraphNode(ref, graph) {

	type T_Pred = PredicateA
	type T_InvP = InversePredicateA
	type T_EzGN = EzGraphNodeA

	override protected def  make(ref: NonLiteral, graph: TripleCollection) = new EzGraphNodeA(ref,graph)
	override protected def  predicate(rel: UriRef) = new PredicateA(rel)
	override protected def  inverse(rel: UriRef) = new InversePredicateA(rel)

	/**
	 * relate the subject via the given relation to....
	 */
	def --(rel: UriRef) = predicate(rel)

	/**
	 * relate the subject via the inverse of the given relation to....
	 * note: we can't have <-- as that messes up the balance of precedence
	 */
	def -<-(rel: UriRef) = inverse(rel)

	class PredicateA(rel: UriRef) extends Predicate(rel) {
		/**
		 * ...to the following non resource
		 */
		def -->(obj: Resource) = add(obj)

		/**
		 * Adds a relation to a real linked list.
		 * If you want one relation to each entry use -->> or ⟶*
		 */
		def -->(list: List[Resource]) = addList(list)

		/**
		 * ...to the EzGraphNode, which is useful for opening a parenthesis.
		 */
		def -->(sub: EzGraphNode) = addEG(sub)

		/**
		 * Add one relation for each member of the iterable collection
		 */
		def -->>[T <: Resource](uris: Iterable[T]) = addMany(uris)


	}

	class InversePredicateA(ref: UriRef) extends InversePredicate(ref) {
		/**
		 * ...to the following non literal
		 */
		def --(subj: NonLiteral) = add(subj)

		/**
		 * ...to the following EzGraphNode
		 * (useful for opening a new parenthesis and specifying other things in more detail
		 */
		def --(subj: EzGraphNode) = addGN(subj)
		// since we can only have inverses from non literals (howto deal with bndoes?)
	}

}

/**
 * English language looking Notation for EzGraphNode. This feels gives somewhat awkward
 * english, but the operator binding priorities for ascii named operators is the weakest, which
 * means that one needs very few parenthesis when writing out the code as all other operators bind
 * more tightly.
 */
class EzGraphNodeEn(ref: NonLiteral, graph: TripleCollection) extends EzGraphNode(ref, graph) {

	type T_Pred = PredicateEn
	type T_InvP = InversePredicateEn
	type T_EzGN = EzGraphNodeEn

	override protected def make(ref: NonLiteral, graph: TripleCollection) = new EzGraphNodeEn(ref,graph)
	override protected def predicate(rel: UriRef) = new PredicateEn(rel)
	override protected def inverse(rel: UriRef) = new InversePredicateEn(rel)

	/**
	 * the subject has the given relation to....
	 */
	def has(rel: UriRef) = predicate(rel)

	/**
	 * the subject is the inverse relation of ...
	 */
	def is(rel: UriRef) = inverse(rel)

	class InversePredicateEn(rel: UriRef) extends InversePredicate(rel) {

		/**
		  * ...the following non literal
		  */
		def of(subj: NonLiteral) = add(subj)

		/**
		  * ...the following EzGraphNode - useful for opening a new bracket
		  */
		def of(subj: EzGraphNode) = addGN(subj)
	}

	class PredicateEn(rel: UriRef) extends Predicate(rel) {

		/**
		 * ...to the following resource
		 */
		def to(obj: Resource) = add(obj)

		/**
		 * ...to the following RDF list
		 */
		def to(list: List[Resource]) = addList(list)

		/**
		 * ... each of the members of the iterable collection
		 */
		def toEach[T <: Resource](objs: Iterable[T]) = addMany(objs)

		/**
		 * ...to the EzGraphNode, which is useful for opening a parenthesis.
		 */
		def to(sub: EzGraphNode) = addEG(sub)

	}

}

object EzGraphNode {
	/**
	 * create a new EzGraphNode in the preferred writing style
	 */
	def apply[T<:EzGraphNode](ref: NonLiteral, graph: TripleCollection)(implicit writingStyle: EzStyle[T]=EzStyleChoice.arrow ): T = {
	 	writingStyle.preferred(ref,graph)
	}
}

/**
 * EzGraphNode. Create instances from an EzGraph object. Differnt notations implementations can be used.
 */
abstract class EzGraphNode(val ref: NonLiteral, val graph: TripleCollection) extends RichGraphNode(ref, graph) {

//	lazy val easyGraph = graph match {
//		case eg: EzGraph => eg
//		case other: TripleCollection => new EzGraph(graph)
//	}

	def +(sub: EzGraphNode) = {
		if (graph ne sub.graph) graph.addAll(sub.graph)
		this
	}

	type T_Pred <: Predicate
	type T_InvP <: InversePredicate
	type T_EzGN <: EzGraphNode

	protected def predicate(rel: UriRef): T_Pred
	protected def inverse(rel: UriRef): T_InvP
	protected def make(ref: NonLiteral, graph: TripleCollection): T_EzGN

	/** is an instance of the given class */
	def a(rdfclass: UriRef): T_EzGN = {
		graph.add(new TripleImpl(ref, RDF.`type`, rdfclass))
		return this.asInstanceOf[T_EzGN]
	}

	/*
	 * create an EzGraphNode from this one where the backing graph is protected from writes by a new
	 * SimpleGraph.
	 */
	def protect(): T_EzGN = make(ref, new UnionMGraph(new SimpleMGraph(), graph))

	def this(s: NonLiteral) = this (s, new SimpleMGraph())

	def this() = this (new BNode)

	/** class for Inverse relations with the current EzGraphNode.ref as object */
	abstract class InversePredicate(rel: UriRef) {

		protected def addGN(subj: EzGraphNode) = {
			EzGraphNode.this + subj
			add(subj.ref)
		}

		protected def add(subj: NonLiteral) = {
			graph.add(new TripleImpl(subj, rel, ref))
			EzGraphNode.this.asInstanceOf[T_EzGN]
		}
	}

	/**
	 *  class for relations with the current EzGraphNode.ref as subject
	 */
	abstract class Predicate(rel: UriRef) {

		protected def add(obj: Resource): T_EzGN = {
			addTriple(obj)
			EzGraphNode.this.asInstanceOf[T_EzGN]
		}

		protected def addTriple(obj: Resource) {
			graph.add(new TripleImpl(ref, rel, obj))
		}

		protected def addMany[T<:Resource](uris: Iterable[T]): T_EzGN = {
			for (u <- uris) addTriple(u)
			EzGraphNode.this.asInstanceOf[T_EzGN]
		}

		protected def addEG(sub: EzGraphNode): T_EzGN = {
			EzGraphNode.this + sub
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
			EzGraphNode.this.asInstanceOf[T_EzGN]
		}

	}

}

