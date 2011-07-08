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

import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl
import org.apache.clerezza.rdf.ontologies.XSD
import org.apache.clerezza.rdf.utils.GraphNode
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.util.Date
import org.apache.clerezza.rdf.core._

/**
* This object provides the implicit conversions. Typically this is used by
* adding
* {{{
* import org.apache.clerezza.rdf.scala.utils.Preamble._
* }}} near the top of the
* file using SCB Utilities for Scala
*
* @author hjs, reto
*/
object Preamble extends TcIndependentConversions {

}

/**
* This class provides the implicit conversions of its companion Object and
* additional conversions that require an evaluation graph, i.e. the conversion
* from a resource to a RichGraphNode.
*
* Typically this is used by
* adding
* {{{
* val preamble = new org.apache.clerezza.rdf.scala.utils.Preamble(myMGraph)
* import preamble._
* }}}
* before the
* code section using the conversions
*
* @author hjs, reto
*/
class Preamble(val baseTc: TripleCollection) extends TcDependentConversions {
	
}
protected trait TcDependentConversions extends TcIndependentConversions {
	
	def baseTc: TripleCollection
	
	implicit def toRichGraphNode(resource: Resource) = {
		new RichGraphNode(new GraphNode(resource, baseTc))
	}
}

protected trait TcIndependentConversions {
	implicit def toRichGraphNode(node: GraphNode) = {
		new RichGraphNode(node)
	}

	implicit def toFirstElement(c: CollectedIter[RichGraphNode])  = {
		if (c.length(1) > 0) {
			c(0)
		} else {
			TcIndependentConversions.emptyLiteral
		}
	}

	private val litFactory = LiteralFactory.getInstance

	/*a strig is dynamically converted to 3 types of objects, a Literal, a
	  LiteralBuilder or a UriRefBuilder*/

	implicit def string2litBuilder(str: String) = new TcIndependentConversions.LiteralBuilder(str)

	implicit def string2uriRefBuilder(str: String) = new TcIndependentConversions.UriRefBuilder(str)

	implicit def string2lit(str: String) = litFactory.createTypedLiteral(str)

	implicit def lit2String(lit: Literal) = lit.getLexicalForm

	implicit def litBuilder2lit(litBuilder: TcIndependentConversions.LiteralBuilder) = litFactory.createTypedLiteral(litBuilder.lexicalForm)

	implicit def date2lit(date: Date) = litFactory.createTypedLiteral(date)

	implicit def int2lit(int: Int) = litFactory.createTypedLiteral(int)

	implicit def bigint2lit(bint: BigInt) = litFactory.createTypedLiteral(bint.underlying())

	implicit def bigint2lit(bigInt: BigInteger) = litFactory.createTypedLiteral(bigInt)

	implicit def bool2lit(boolean: Boolean) = litFactory.createTypedLiteral(boolean)

	implicit def long2lit(long: Long) = litFactory.createTypedLiteral(long)

	implicit def double2lit(double: Double) = litFactory.createTypedLiteral(double)

	implicit def uriRef2Prefix(uriRef: UriRef) = new NameSpace(uriRef.getUnicodeString)

	implicit def URItoUriRef(uri: URI) = new UriRef(uri.toString)

	implicit def URLtoUriRef(url: URL) = new UriRef(url.toExternalForm)
	
}
protected object TcIndependentConversions {
	val emptyGraph = new impl.SimpleGraph(new impl.SimpleMGraph)
	val emptyLiteral = new RichGraphNode(new GraphNode(new impl.PlainLiteralImpl(""), emptyGraph))

	/**
	 * A Literal Builder enriches a String with methods to create a literal
	 */
	class LiteralBuilder(val lexicalForm: String) {

		/**
		 * Produces a PlainLiteral with the wrapped String as lexical form
		 * and a given language
		 *
		 * @param lang the language tag of the literal to be created
		 * @return a plain literal with the specified language
		 */
		def lang(lang: Language) = new PlainLiteralImpl(lexicalForm, lang)

		/**
		 * Produces a PlainLiteral with the wrapped String as lexical form
		 * and a given language
		 *
		 * @param lang a sthe language tag of the literal to be created as String
		 * @return a plain literal with the specified language
		 */
		def lang(lang: String) = new PlainLiteralImpl(lexicalForm, new Language(lang))

		/**
		 * Produces a PlainLiteral with the wrapped String as lexical form
		 * and a given language
		 *
		 * @param lang a symbol of which the name is the language tag of the literal to be created
		 * @return a plain literal with the specified language
		 */
		def lang(lang: Symbol) = new PlainLiteralImpl(lexicalForm, new Language(lang.name))

		/**
		 * Produces a TypedLiteral with the wrapped String as lexical form
		 * and a data type
		 *
		 * @param dataType the data type
		 * @return the TypedLiteral of the specified type
		 */
		def ^^(dataType: UriRef) = new TypedLiteralImpl(lexicalForm, dataType)

	}

	/**
	 * A UriRef Builder enriches a String with methods to create a UriRef
	 */
	class UriRefBuilder(string: String) {

		/**
		 * returns a UriRef with the wrapped string as unicode representation
		 */
		def uri = new UriRef(string)

	}
}