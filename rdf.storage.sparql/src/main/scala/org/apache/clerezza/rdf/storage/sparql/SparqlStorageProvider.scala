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

package org.apache.clerezza.rdf.storage.sparql

import org.osgi.service.component.ComponentContext
import java.io.IOException
import java.net.{ HttpURLConnection, URL }
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat
import org.apache.clerezza.rdf.core.serializedform.Parser
import java.security.PrivilegedActionException
import org.slf4j.scala._
import org.apache.clerezza.rdf.core.access._
import org.apache.clerezza.rdf.core.impl.AbstractMGraph
import org.apache.clerezza.rdf.core._
import scala.collection.JavaConversions.asScalaSet
import java.util.Collection
import java.util.Collections
import org.apache.felix.scr.annotations.Component
import org.apache.felix.scr.annotations.Service
import org.apache.felix.scr.annotations.Property
import java.net.URLEncoder
import org.apache.clerezza.rdf.core.impl.TripleImpl
import org.apache.clerezza.rdf.ontologies.RDF

/**
 * The Web Proxy Service enables applications to request remote (and local) graphs.
 * It keeps cached version of the remote graphs in store for faster delivery.
 *
 */
@Component
@Service(Array(classOf[WeightedTcProvider]))
class SparqlStorageProvider extends WeightedTcProvider with Logging {

  final val queryEndpointKey = "query-endpoint"
  @Property(name = queryEndpointKey, value = Array("http://localhost:3030/ds/query"))
  private var queryEndpoint: String = _

  final val updateEndpointKey = "update-endpoint"
  @Property(name = updateEndpointKey, value = Array("http://localhost:3030/ds/update"))
  private var updateEndpoint: String = _

  final val metaGraphName = new UriRef("urn:x-internal:/meta")
  final val mGraphClass = new UriRef("http://clerezza.apache.org/ontologies/storage#MGraph")

  /**OSGI method, called on activation */
  protected def activate(context: ComponentContext) = {
    queryEndpoint = context.getProperties.get(queryEndpointKey).toString
    updateEndpoint = context.getProperties.get(updateEndpointKey).toString
  }

  private var parser: Parser = null

  protected def bindParser(p: Parser) = {
    parser = p
  }

  protected def unbindParser(p: Parser) = {
    parser = null
  }

  def getWeight: Int = {
    return 20000
  }

  def getMGraph(name: UriRef): MGraph = {
    checkMGraphExists(name)
    getMGraphNoExistenceCheck(name)
  }

  /**
   * throws NoSuchEntity if not existing
   */
  protected def checkMGraphExists(name: UriRef) {
    val metaGraph = getMGraphNoExistenceCheck(metaGraphName)
    if (!metaGraph.contains(new TripleImpl(name, RDF.`type`, mGraphClass))) {
      throw new NoSuchEntityException(name)
    }
  }

  protected def getMGraphNoExistenceCheck(name: UriRef): MGraph = {
    return new AbstractMGraph() {
      protected def performFilter(subject: NonLiteral, predicate: UriRef, obj: Resource): java.util.Iterator[Triple] = {
        def ask = {
          //TODO ask query
          true
        }
        
        val vars = new collection.mutable.ListBuffer[Char]
        if (subject == null) vars += 's'
        if (predicate == null) vars += 'p'
        if (obj == null) vars += 'o'
        if (vars.isEmpty() && ask()) {
          Collections.singletonList(new TripleImpl(subject, predicate, obj)).iterator() 
        } else {
          val vs = vars.foldLeft[String]("")(((s,v) => s+" ?"+v+" "))
        val query = "SELECT "+vs+" WHERE {" + SparqlStorageProvider.tpEncode(subject) +
          " " + SparqlStorageProvider.tpEncode(predicate) + " " + SparqlStorageProvider.tpEncode(obj) + " } "
        val url = new URL(queryEndpoint)
        val con = url.openConnection().asInstanceOf[HttpURLConnection]
        //not sure
        System.out.println("DEBUG: doing output...")
        con.setDoOutput(true)
        con.setRequestMethod("POST")
        val out = con.getOutputStream()
        System.out.println("update:  " + update)
        out.write(("update=" + URLEncoder.encode(update)).getBytes())
        val err = con.getErrorStream()
        if (err != null) {
          var che = err.read
          while (che != -1) {
            System.out.write(che)
            che = err.read
          }
        } else {
          val in = con.getInputStream()
          var ch = in.read
          while (ch != -1) {
            System.out.write(ch)
            ch = in.read
          }
        }
        System.out.flush
        Collections.emptyList[Triple]().iterator()
            }
      }
      override def add(triple: Triple) = {
        val update = "INSERT DATA { GRAPH " + name + " {" + SparqlStorageProvider.tpEncode(triple.getSubject) +
          " " + SparqlStorageProvider.tpEncode(triple.getPredicate) + " " + SparqlStorageProvider.tpEncode(triple.getObject) + " } }"
        performUpdate(update)
        true
      }

      def size = 0
    }
  }

  protected def performUpdate(update: String) {
    val url = new URL(updateEndpoint)
    val con = url.openConnection().asInstanceOf[HttpURLConnection]
    //not sure
    System.out.println("DEBUG: doing output...")
    con.setDoOutput(true)
    con.setRequestMethod("POST")
    val out = con.getOutputStream()
    System.out.println("update:  " + update)
    out.write(("update=" + URLEncoder.encode(update)).getBytes())
    val err = con.getErrorStream()
    if (err != null) {
      var che = err.read
      while (che != -1) {
        System.out.write(che)
        che = err.read
      }
    } else {
      val in = con.getInputStream()
      var ch = in.read
      while (ch != -1) {
        System.out.write(ch)
        ch = in.read
      }
    }
    System.out.flush
  }

  def getGraph(name: UriRef): Graph = {
    throw new NoSuchEntityException(name)
  }

  def getTriples(name: UriRef): TripleCollection = {
    return getMGraph(name)
  }

  def createMGraph(name: UriRef): MGraph = {
    performUpdate("CREATE GRAPH " + name)
    val metaGraph = getMGraphNoExistenceCheck(metaGraphName)
    metaGraph.add(new TripleImpl(name, RDF.`type`, mGraphClass))
    getMGraph(name)
  }

  def createGraph(name: UriRef, triples: TripleCollection): Graph = {
    throw new UnsupportedOperationException
  }

  def deleteTripleCollection(name: UriRef): Unit = {
    throw new UnsupportedOperationException
  }

  def getNames(graph: Graph): java.util.Set[UriRef] = {
    return java.util.Collections.emptySet[UriRef]
  }

  def listTripleCollections: java.util.Set[UriRef] = {
    var result: java.util.Set[UriRef] = new java.util.HashSet[UriRef]
    result.addAll(listGraphs)
    result.addAll(listMGraphs)
    return result
  }

  def listGraphs: java.util.Set[UriRef] = {
    //or should we list graphs for which we have a cached version?
    return java.util.Collections.emptySet[UriRef]
  }

  def listMGraphs: java.util.Set[UriRef] = {
    var result: java.util.Set[UriRef] = new java.util.HashSet[UriRef]
    result.add(new UriRef("http://fake/sparql"))
    result.add(new UriRef(queryEndpoint))
    result.add(new UriRef(updateEndpoint))
    return result

  }
}
object SparqlStorageProvider {
  private def tpEncode(res: Resource): String =
    res match {
      case l: Literal => tpEncode(l)
      case u: UriRef => u.toString()
      case b: BNode => tpEncode(b)
    }

  private def tpEncode(lit: Literal): String =
    lit match {
      case l: PlainLiteral if (l.getLanguage() == null) => "\"\"\"" + l.getLexicalForm() + "\"\"\""
      case l: PlainLiteral if (l.getLanguage() != null) => "\"\"\"" + l.getLexicalForm() + "\"\"\"@" + l.getLanguage()
      case l: TypedLiteral => "\"\"\"" + l.getLexicalForm() + "\"\"\"^^" + l.getDataType()
    }

  private def tpEncode(bNode: BNode): String =
    //if the bNode has previously been retuned by SPARQL we should add 
    //TriplePatterns so that the right one is identified
    //If this is one that is newly added we must make sure the subsequentyl added
    //statements get added, as long as a newly added bNode doesn't add
    //new information (would cause a non-lean graph) it is not added to the graph
    "_:"
}
