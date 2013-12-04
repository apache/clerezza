package org.apache.clerezza.platform.accountcontrolpanel.html

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
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils._
import org.apache.clerezza.rdf.ontologies.FOAF
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import scala.xml.Text



/**
 * Metadata class for the person panel
 */
class PersonBox extends SRenderlet {
  def getRdfType() = FOAF.Person

  override def getModePattern = "box-naked"

  override def renderedPage(arguments: XmlResult.Arguments) = new XmlPerson(arguments)

  /**
   * Content class for the Person Panel
   */
  class XmlPerson(args: XmlResult.Arguments) extends XmlResult(args) {

    import RenderingUtility._
    
    //
    // the content itself.
    // This is the piece that is closest to a pure ssp, though there is still too much code in it
    //

    override def content = {
      val pixml= getAgentPix(res)
      <div class="personInABox">
        <table><tr><td>{pixml}</td></tr>
        <tr><td>{new Text(getName(res))}</td></tr>
        </table>
      </div>
    }

  }
}
