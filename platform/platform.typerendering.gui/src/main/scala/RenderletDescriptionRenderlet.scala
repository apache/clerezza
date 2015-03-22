/*
 *
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
 *
*/

package org.apache.clerezza.platform.typerendering.gui

import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.ontologies.DC

/**
 * A Renderlet for the descriptions of RenderletDescription
 */
class RenderletDescriptionRenderlet extends SRenderlet {

  val getRdfType = Ontology.Renderlet

  override def getModePattern = "naked"

  override def renderedPage(arguments: XmlResult.Arguments) = {
    new XmlResult(arguments) {
      override def content = {
        resultDocModifier.setTitle("Renderlet Overview")
        resultDocModifier.addStyleSheet("/styles/renderlets/style.css")
        <div class="renderlet">
          <div>Renderlet: <span class="value">{res/Ontology.stringRepresentation*}</span></div>
          <div>For type: <span class="value">{res/Ontology.rdfType*}</span></div>
          <div>Producing: <span class="value">{res/Ontology.mediaType*}</span></div>
          {if ((res/Ontology.modePattern).size > 0) <div>Mode pattern: <span class="value">{res/Ontology.modePattern*}</span></div>}
          <div>Provided by: <span class="value">{res/Ontology.providingBundle*}</span></div>
        </div>
      }
    }
  }

}
