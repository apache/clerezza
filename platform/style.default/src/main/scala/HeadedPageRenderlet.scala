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

import java.net.URLEncoder
import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.scala.utils.RichGraphNode
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
class HeadedPageRenderlet extends SRenderlet {

  val getRdfType = PLATFORM.HeadedPage

  override def getModePattern = "(?!.*naked).*"

  protected def defaultTitle(res: RichGraphNode) = "An incomplete titled content "+(res/DISCOBITS.contains*)

  override def renderedPage(arguments: XmlResult.Arguments) = {
    new XmlResult(arguments) {
      def menuLink(href: String, label: String) =
      if ((res*).endsWith(href) || (res*).endsWith(href+"index")) {
         <a href={href} class="active">{label}</a>
      } else {
         <a href={href}>{label}</a>
      }
      override def content = {
        resultDocModifier.addStyleSheet("/style/style.css");
resultDocModifier.addScriptReference("/jquery/jquery-1.3.2.min.js");
/*resultDocModifier.addScriptReference("/jquery/jquery.menu.js");
resultDocModifier.addScriptReference("/jquery/jquery.panel.js");
resultDocModifier.addScriptReference("/style/scripts/panel.js");*/
resultDocModifier.addScriptReference("/scripts/modification-status.js");
resultDocModifier.addScriptReference("/scripts/status-message.js");
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    {(res/DISCOBITS.contains).find(e => ((e/DISCOBITS.pos*) == "0")) match {
        case Some(e) => <title>{render(e/DISCOBITS.holds, "naked")}</title>
        case None => <title>{defaultTitle(res)}</title>
      }
    }
  </head>
  <body>
    <div class="zz-header">
      <div class="bar"></div>
      <div class="logo">
        <a href="http://clerezza.apache.org/" style=""><img src="/images/logo.png" alt="logo" /></a>
      </div>
      <div class="module-info">
        <span id="tx-module">
          <div id="tx-page-actions">
            <ol id="tx-page-actions-ol">
            </ol>
          </div>
        </span>
        <div id="tx-module-tabs">
          <ol id="tx-module-tabs-ol">
          </ol>
          
        </div>
      </div>
      
      <div class="zz-control">
        <div class="login">
            {
              def platform(s: Any) = new UriRef("http://clerezza.org/2009/08/platform#"+s)
              val userName = context/platform("user")/platform("userName")*
              val displayName = if ((context/platform("user")/FOAF.name).length == 0) {
                    userName
                  } else {
                    context/platform("user")/FOAF.name*
                  }
              if((userName).equals("anonymous")) {
                <span>
                  <a href= {"/login?referer="+URLEncoder.encode(uriInfo.getAbsolutePath.toString, "utf-8")}
                        id="tx-login-button">login</a>
                </span>
              } else {
                <span><a href={"/user/" + userName + "/control-panel"}>{displayName}</a>|<a href="/logout">logout</a></span>
              }
            }
        </div>
        <div class="actions" id="tx-contextual-buttons">
            <ol id="tx-contextual-buttons-ol">
            </ol>
          </div>
        <div id="tx-module-options">
          <ol id="tx-module-options-ol">
          </ol>
        </div>
        
        
        <div class="tx-panel" id="tx-panel">
          <div id="tx-panel-title"><h3></h3></div>
          <div class="tx-panel-window">
            <div class="tx-panel-tab-buttons" id="tx-panel-tab-buttons">
              <ol id="tx-panel-tab-buttons-ol">
              </ol>
            </div>
            <div class="tx-panel-tabs" id="tx-panel-tabs"></div>
          </div>
        </div>
        
      </div>
    </div>
    {render(context,"menu")}
    <div class="zz-content">
    {
      if (mode == null) {
        render(res, "naked")
      } else {
        render(res, mode + "-naked")
      }
    }
    </div>
            <div class="footer">
              <div class="logos"><img src="/images/feather.png" /><img src="/images/sw-vert-w3c.png" /><img src="/images/footer-logo.png" /></div>

              <div class="divider"></div>
              <div class="dark">
                <div class="copyright">Apache Clerezza, Clerezza, Apache, the Apache feather logo, and the Apache Clerezza project logo are trademarks of The Apache Software Foundation. <br></br>© 2011 The Apache Software Foundation.</div>
              </div>
            </div>
  </body>
</html>
      
      }
    }
  }

}
