package org.apache.clerezza.platform.accountcontrolpanel.pages

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

import org.apache.clerezza.platform.accountcontrolpanel.ontologies.PINGBACK
import org.apache.clerezza.platform.typerendering.scala.{SRenderlet, XmlResult}
import org.apache.clerezza.rdf.ontologies.SIOC
import java.net.URLEncoder

/**
 * A panel to implement the Pingback "protocol" described at
 * http://www.w3.org/wiki/Pingback
 *
 * @author Henry Story
 */
class ping_back_collection_panel extends SRenderlet {
	def getRdfType() = PINGBACK.Container

   override def renderedPage(arguments: XmlResult.Arguments): XmlResult =  new XhtmlPingBackDoc(arguments)

}

class XhtmlPingBackDoc(args: XmlResult.Arguments) extends XmlResult(args) {
	import org.apache.clerezza.rdf.scala.utils.Preamble._

	resultDocModifier.addStyleSheet("profile/style/profile.css");
	resultDocModifier.setTitle("Account Control Panel");
	resultDocModifier.addNodes2Elem("tx-module", <h1>Account Control Panel</h1>);
	resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li><a href="profile">Profile</a></li>);
	resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li><a href="control-panel">Settings</a></li>);
	resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li class="tx-active"><a href="#">Ping</a></li>);

	override def content = {<div id="tx-content" xmlns:pingback="http://purl.org/net/pingback/" about="" typeof="pingback:Container">
		  <h3>Pingback Form</h3>
		  <form method="POST" action="">
			  { for (to <- res/PINGBACK.to) yield <input type="hidden" name="to" value={to*}/> }
			  <table>
				<tr><td><label for="source">Source</label></td><td><input type="text" size="80" name="source"/></td></tr>
				<tr><td><label for="target">Target</label></td><td><input type="text" size="80" name="target"/></td></tr>
				<tr><td><label for="comment">Comment</label></td><td><textarea rows="10" cols="80" name="comment"></textarea></td></tr>
				<tr><td colspan="2"><input type="submit" value="Send"/></td></tr>
			  </table>
        </form>
		  <p>
		  {showItems()}
		  </p>
	</div>}

	def showItems() = <form method="POST" action="delete">
		<input type="submit" value="Delete"/>
		{
			for(item <- res/SIOC.container_of ) yield
			<p><!-- todo: say who sent this information, and show more info about source or target -->
			  <a href={item*}>permalink</a> for this pingback</p>
			  <p>The resource {item/PINGBACK.source} is referring to {item/PINGBACK.target}</p>
			  <p>The sender says:
				  <blockquote>{item/SIOC.content*}</blockquote>
			  <input type="checkbox" name="item" value={item*}/>
			</p>
		}
		<input type="submit" value="Delete"/>
	</form>

}
