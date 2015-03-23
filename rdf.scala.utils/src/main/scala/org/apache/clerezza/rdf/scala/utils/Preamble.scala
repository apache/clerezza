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


import org.apache.clerezza.rdf.ontologies.XSD
import org.apache.clerezza.rdf.utils.GraphNode
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.util.Date
import org.apache.clerezza.rdf.core._
import org.apache.commons.rdf.Graph
import org.apache.commons.rdf.Iri
import org.apache.commons.rdf.Literal
import org.apache.commons.rdf.RdfTerm
import org.apache.commons.rdf.impl.utils.PlainLiteralImpl
import org.apache.commons.rdf.impl.utils.simple.SimpleGraph
import org.apache.commons.rdf.impl.utils.simple.SimpleImmutableGraph

/**
* This object provides the implicit conversions. Typically this is used by
* adding
* {{{
* import org.apache.clerezza.rdf.scala.utils.Preamble._
* }}} near the top of the
* file using SCB Utilities for Scala
*
* @author bblfish, reto
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
* val preamble = new org.apache.clerezza.rdf.scala.utils.Preamble(myGraph)
* import preamble._
* }}}
* before the
* code section using the conversions
*
* @author bblfish, reto
*/
class Preamble(val baseTc: Graph) extends TcDependentConversions {
  
}
protected trait TcDependentConversions extends TcIndependentConversions {
  
  def baseTc: Graph
  
  implicit def toRichGraphNode(resource: RdfTerm) = {
    new RichGraphNode(new GraphNode(resource, baseTc))
  }
}

protected trait TcIndependentConversions extends EzLiteralImplicits {
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


  implicit def lit2String(lit: Literal) = lit.getLexicalForm

  implicit def date2lit(date: Date) = litFactory.createTypedLiteral(date)

  implicit def int2lit(int: Int) = litFactory.createTypedLiteral(int)

  implicit def bigint2lit(bint: BigInt) = litFactory.createTypedLiteral(bint.underlying())

  implicit def bigint2lit(bigInt: BigInteger) = litFactory.createTypedLiteral(bigInt)

  implicit def bool2lit(boolean: Boolean) = litFactory.createTypedLiteral(boolean)

  implicit def long2lit(long: Long) = litFactory.createTypedLiteral(long)

  implicit def double2lit(double: Double) = litFactory.createTypedLiteral(double)

  implicit def uriRef2Prefix(uriRef: Iri) = new NameSpace(uriRef.getUnicodeString)

  implicit def URItoIri(uri: URI) = new Iri(uri.toString)

  implicit def URLtoIri(url: URL) = new Iri(url.toExternalForm)
  
}
protected object TcIndependentConversions {
  val emptyGraph = new SimpleImmutableGraph(new SimpleGraph)
  val emptyLiteral = new RichGraphNode(new GraphNode(new PlainLiteralImpl(""), emptyGraph))

}

