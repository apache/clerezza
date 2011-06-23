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
import org.apache.clerezza.rdf.utils.UnionMGraph
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.ontologies.{XSD, RDF}
import org.apache.clerezza.rdf.core._
import impl._
import java.util.{HashSet, Date}
import collection.mutable.{ListBuffer, HashMap}

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


/**
 * This is really a TripleCollection , should it just extend a TC? Or a MGraph?
 *
 * @param graph: a Triple collection - or should it be an MGraph since it is really meant to be modifiable
 * @author hjs
 * @created: 20/04/2011
 */

class EasyGraph(val graph: HashSet[Triple]) extends SimpleMGraph(graph) {
	val namedBnodes = new HashMap[String,EasyGraphNode]

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

	def bnode: EasyGraphNode = {
		new EasyGraphNode(new BNode(), this)
	}

	def bnode(name: String): EasyGraphNode = {
		namedBnodes.get(name) match {
			case Some(ezGraphNode) => ezGraphNode
			case None => {
				val ezgn = bnode;
				namedBnodes.put(name, ezgn);
				ezgn
			}
		}
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

//	lazy val easyGraph = graph match {
//		case eg: EasyGraph => eg
//		case other: TripleCollection => new EasyGraph(graph)
//	}

	/*
	 * create an EasyGraphNode from this one where the backing graph is protected from writes by a new
	 * SimpleGraph.
	 */
	def protect(): EasyGraphNode = new EasyGraphNode(ref, new UnionMGraph(new SimpleMGraph(), graph))

	def this(s: NonLiteral) = this (s, new SimpleMGraph())

	def this() = this (new BNode)

	def --(rel: UriRef): Predicate = new Predicate(rel)

	def --(rel: String): Predicate = new Predicate(new UriRef(rel))

	/**
	 * we Can't have <-- as that messes up the balance of precedence
	 */
	def -<-(rel: UriRef) = new InversePredicate(rel)

	// does not worked as hoped, and does not look that good either
	//	def hasQ(yes: Boolean, rel: UriRef )(func: Predicate => EasyGraphNode): EasyGraphNode =
	//		if (yes) func(has(rel))
	//		else this


	def ⟝(rel: UriRef): Predicate = --(rel)

	def ⟝(rel: String): Predicate = --(rel)

	/* For inverse relationships */
	def ⟵(rel: UriRef) = -<-(rel)

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

		def ⟞(sub: EasyGraphNode):  EasyGraphNode = {
			EasyGraphNode.this + sub
			add(sub.ref)
		}

		def --(subj: NonLiteral) = ⟞(subj)
		def --(subj: String) = ⟞(subj)
		def --(subj: EasyGraphNode) = ⟞(subj)
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
		def -->(obj: Resource): EasyGraphNode = add(obj)

		def -->(lit: String): EasyGraphNode = add(new EzLiteral(lit))

		/**
		 * Adds a relation to a real linked list.
		 * If you want one relation to each entry use -->> or ⟶*
		 */
		def -->(list: List[Resource]) = addList(list)

		def -->(sub: EasyGraphNode): EasyGraphNode = {
			EasyGraphNode.this + sub
			add(sub.ref)
		}

		/**
		 * Add one relation for each member of the iterable collection
		 */
		def -->>[T <: Resource](uris: Iterable[T]): EasyGraphNode = {
			for (u <- uris) addTriple(u)
			EasyGraphNode.this
		}


		//
		// arrow notation
		//
		// todo: a relation to a list

		def ⟶(obj: String) = -->(obj)

		def ⟶(obj: Resource): EasyGraphNode = -->(obj)

		def ⟶(list: List[Resource]): EasyGraphNode = addList(list)

		/**
		 * Add one relation for each member of the iterable collection
		 */
		def ⟶*[T <: Resource](objs: Iterable[T]) = -->>(objs)

		def ⟶(sub: EasyGraphNode) = -->(sub)

		protected def add(obj: Resource) = {
			addTriple(obj)
			EasyGraphNode.this
		}

		protected def addTriple(obj: Resource) {
			graph.add(new TripleImpl(ref, rel, obj))
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
			EasyGraphNode.this
		}



	}

}

