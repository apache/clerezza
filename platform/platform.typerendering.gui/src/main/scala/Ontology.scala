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

import org.apache.clerezza.rdf.core.UriRef

object Ontology {

  private def p(s: String) = new UriRef("http://clerezza.org/2011/25/renderletgui#"+s)

  val RenderletOverviewPage = p("RenderletOverviewPage")
  val Renderlet = p("Renderlet")
  val renderlet = p("renderlet")
  val modePattern = p("modePattern")
  val mediaType = p("mediaType")
  val rdfType = p("renderedType")
  val providingBundle = p("providingBundle")
  val stringRepresentation = p("stringRepresentation")

}
