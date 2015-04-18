/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.clerezza.platform.editor.renderlets

import java.io.ByteArrayOutputStream
import org.apache.clerezza.commons.rdf.Graph
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph
import org.apache.clerezza.platform.typerendering.scala.XmlResult
import org.apache.clerezza.rdf.core.serializedform.Serializer
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat
import scala.xml.NodeBuffer
import scala.xml.Unparsed

abstract class XmlResultWithTurtle(arguments: XmlResult.Arguments, serializer: Serializer) extends XmlResult(arguments) {
  override def content = {
    val shownGraph = new SimpleGraph
    addTriples(shownGraph)
    val serializedOut = new ByteArrayOutputStream;
    serializer.serialize(serializedOut, shownGraph, SupportedFormat.TURTLE)
    val turtle = new String(serializedOut.toByteArray, "UTF-8");
    new NodeBuffer() &+ <script type="text/turtle">
                          { Unparsed(turtle) }
                        </script> &+ specificContent

  }
  def addTriples(shownGraph: Graph): Unit;

  def specificContent: AnyRef;
}
