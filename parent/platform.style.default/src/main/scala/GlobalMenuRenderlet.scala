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

package org.apache.clerezza.platform.style.default

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
 * A Renderlet for the menu
 */
class GlobalMenuRenderlet extends SRenderlet {

	val getRdfType = RDFS.Resource

	override def getModePattern = "menu"

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			def menuLink(href: String, label: String) =
			if ((res*).endsWith(href) || (res*).endsWith(href+"index")) {
				 <a href={href} class="active">{label}</a>
			} else {
				 <a href={href}>{label}</a>
			}
			override def content = {
				def menu(s: Any) = new UriRef("http://clerezza.org/2009/11/global-menu#"+s)
def rdfs(s: Any) = new UriRef("http://www.w3.org/2000/01/rdf-schema#"+s)
def platform(s: Any) = new UriRef("http://clerezza.org/2009/08/platform#"+s)
def dct(s: Any) = new UriRef("http://purl.org/dc/terms/"+s)

<div class="column nav">
							<ul>
								

	{for (menuItem <- res/menu("globalMenu")!!) yield
		<li class="top-nav-entry"><div class="title">
			{
				if ((menuItem/menu("path")).length > 0) {
					<a href={menuItem/menu("path")*}>{(menuItem/rdfs("label")*)}</a>
				} else {
					<a href="#" onclick="return false">{(menuItem/rdfs("label")*)}</a>
				}
			}
		 </div>
			{
				ifx ((menuItem/menu("children")).length > 0) {
				<div>
					<ul class="nav-entries">
						 {
							for (childMenuItem <- menuItem/menu("children")!!) yield {
							<li><a href={childMenuItem/menu("path")*}>{childMenuItem/rdfs("label")*}</a><span>{childMenuItem/dct("description")*}</span></li>
							}
						 }
					</ul>
				</div>
				}
			}
		</li>
	}
	</ul>
</div>
			}
		}
	}

}
