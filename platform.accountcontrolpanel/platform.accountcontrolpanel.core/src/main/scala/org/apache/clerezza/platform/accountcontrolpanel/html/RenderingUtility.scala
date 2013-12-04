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

package org.apache.clerezza.platform.accountcontrolpanel.html


import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.Text
import java.net.URLEncoder
import org.apache.clerezza._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.ontologies.FOAF
import org.apache.clerezza.rdf.ontologies.RDFS
import rdf.scala.utils.CollectedIter
import rdf.scala.utils.RichGraphNode
import rdf.scala.utils.Preamble._
/**
 * Some utility methods for the renderlets
 */
object RenderingUtility {
  val emptyText = new Text("")

  def ifE[T](arg:T)(template: T=>Node ):NodeSeq = {
    def isEmpty(arg: Any): Boolean = {
      arg match {
        case prod: Product => prod.productIterator.forall(isEmpty(_))
        case str: String => (str.size == 0)
        case it: CollectedIter[RichGraphNode] => (it.size == 0)
        case node: RichGraphNode => (null == node)
        case other: AnyRef => (null == other)
        case _ => false //literals can't be empty
      }
    }
    if (isEmpty(arg)) return emptyText else template(arg)
  }

  def firstOf(node: RichGraphNode, uris: UriRef*):CollectedIter[RichGraphNode] = {
    for (uri <- uris) {
      val res : CollectedIter[RichGraphNode] = node/uri
      if (res.size>0) return res
    }
    return new CollectedIter[RichGraphNode]()
  }



  /**
   * Show a person: a picture, a link to their local profile and their name
   * Different default icons should be shown if the agent is a person, company, group, robot...
   *
   * assumes the p is WebID node (can change later)
   */
  def getAgentPix(p: RichGraphNode) = {
    val pix = firstOf(p, FOAF.depiction, FOAF.logo, FOAF.img).getNode match {
      case uri: UriRef => uri.getUnicodeString
      case _ => "http://upload.wikimedia.org/wikipedia/commons/0/0a/Gnome-stock_person.svg"
    }
    <a href={"/browse/person?uri="+encode(p*)}><img class="mugshot" src={pix}/></a>
  }

  private def encode(url: String): String =  URLEncoder.encode(url,"UTF8")

  /**
   * get a usable name from the properties available including nick
   */
  def getName(p: RichGraphNode): String =  {
     val name = p/FOAF.name*;
     if ("" != name ) { return name }
     val firstNm: String = p/FOAF.firstName*;
     val fmlyNm :String = firstOf(p, FOAF.family_name,FOAF.familyName)*;
       if ("" != firstNm || "" != fmlyNm) { return firstNm+" "+fmlyNm }
     return p*

  }

}

