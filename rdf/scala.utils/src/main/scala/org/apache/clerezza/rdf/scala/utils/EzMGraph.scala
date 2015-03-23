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
import org.apache.clerezza.rdf.core.impl._
import org.apache.commons.rdf.BlankNode
import org.apache.commons.rdf.BlankNodeOrIri
import org.apache.commons.rdf.Graph
import org.apache.commons.rdf.Iri
import org.apache.commons.rdf.RdfTerm
import org.apache.commons.rdf.Triple
import org.apache.commons.rdf.impl.utils.AbstractGraph
import org.apache.commons.rdf.impl.utils.simple.SimpleGraph
import scala.collection.mutable.HashMap


/**
 * EzGraph enhances graph writing, it can make writing rdf graphs in code a lot more
 * readable, as it avoids a lot of repetition.
 *
 * @param graph: a Triple collection
 * @author hjs, reto
 */
class EzGraph(val baseTc: Graph) extends AbstractGraph with TcDependentConversions {


  def this() = this (new SimpleGraph())

  def performFilter(subject: BlankNodeOrIri, predicate: Iri,
      obj: RdfTerm): java.util.Iterator[Triple] = baseTc.filter(subject, predicate, obj)

  override def performSize = baseTc.size

  override def add(t: Triple) = baseTc.add(t)

  /**
   * Add all triples into the other graph to this one
   */
  def +=(other: Graph) = {
    if (baseTc ne other) baseTc.addAll(other)
  }

  /**
   * create a new bnode
   */
  def bnode = blankNode
 
   def blankNode: BlankNode = {
    new BlankNode
  }

  private val namedBnodes = new HashMap[String,BlankNode]

  /**
   * create a new named bnode based EzGraphNode with the preferred writing style
   */
  def b_(name: String): BlankNode = {
    namedBnodes.get(name) match {
      case Some(bnode) => bnode
      case None => {
        val bn = new BlankNode
        namedBnodes.put(name, bn);
        bn
      }
    }
  }

}


