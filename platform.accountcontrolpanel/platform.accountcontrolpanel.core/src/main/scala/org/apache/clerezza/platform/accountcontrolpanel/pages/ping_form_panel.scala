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

package org.apache.clerezza.platform.accountcontrolpanel.pages


import org.apache.clerezza.platform.accountcontrolpanel.ontologies.PINGBACK
import org.apache.clerezza.rdf.scala.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.core.UriRef
import xml.Text
import org.apache.clerezza.platform.accountcontrolpanel.PingBack
/**
 * static methods used by person panel and that could possibly be moved to a library
 */
object ping_form_panel {
	val emptyText = new Text("")

}

/**
 * Metadata class for the person panel
 */
class ping_form_panel extends SRenderlet {
	def getRdfType() = PingBack.ProxyForm

	override def renderedPage(arguments: XmlResult.Arguments) = new XmlPingForm(arguments)
}

/**
 * Content class for the Person Panel
 */
class XmlPingForm(args: XmlResult.Arguments) extends XmlResult(args) {
	//
	// Some initial constants
	//

	// either we use the rdf data on this as commented out here,
	//	    val it: CollectedIter[RichGraphNode] = res / FOAF.primaryTopic
	//	    val primeTpc: RichGraphNode = it.apply(0)
	// or we can get that information from URL, as shown here
	lazy val webIdStr = uriInfo.getQueryParameters(true).getFirst("uri")
	lazy val webIdUri= new UriRef(webIdStr)

	//
	// setting some header info
	//

	resultDocModifier.addStyleSheet("/account-control-panel/style/profile.css");
	resultDocModifier.setTitle("Ping Form");
	resultDocModifier.addNodes2Elem("tx-module", <h1>Account Control Panel</h1>);
	resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li class="tx-active"><a href="#">Profile Viewer</a></li>);
	resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li><a href="control-panel">Settings</a></li>);
	resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li><a href="profile">Profile</a></li>);

	//
	// the content itself.
	// This is the piece that is closest to a pure ssp, though there is still too much code in it
	//
	// note this ssp should only be called if 'to' is set. What should one do otherwise?
	override def content = {
		 val source = res/PINGBACK.source
		 val target = res/PINGBACK.target
		 var to = target/PINGBACK.to
       //todo: add code in case to is on the document, not the object
		<div id="tx-content">
			<h2>Ping</h2>
			<p>Ping {person_panel.getName(target)}
			</p>{person_panel.getAgentPix(target)}
			<p>About
				{source}
			</p>
			<p> Message:
				<div id="tx-content" xmlns:pingback="http://purl.org/net/pingback/" about=" " typeof="pingback:Container">
					<form method="POST" action="out">
							<input type="hidden" name="source" value={source *}/>
							<input type="hidden" name="target" value={target *}/>
							<input type="hidden" name="to" value={to *}/>
						<textarea rows="10" cols="80" name="comment"></textarea>
							<input type="submit" value="Send"/>
					</form>
				</div>
			</p>
		</div>
	}

}

