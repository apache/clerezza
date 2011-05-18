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

package org.apache.clerezza.platform.accountcontrolpanel.pages

import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL
import org.apache.clerezza.platform.typerendering.scala._


class settings_panel extends PageRenderlet {

	val rdfType = CONTROLPANEL.SettingsPage
	override def mode = "naked"

  	override def renderedPage(arguments: RenderedPage.Arguments): RenderedPage = {
 		new RenderedPage(arguments) {

			override def content = {
import org.apache.clerezza.rdf.core.UriRef
import scala.xml.NodeBuffer
import scala.collection.mutable.ListBuffer
def cp(s: Any) = new UriRef("http://clerezza.org/2009/03/controlpanel#"+s)
def osgi(s: Any) = new UriRef("http://clerezza.org/2008/11/osgi#"+s)
def platform(s: Any) = new UriRef("http://clerezza.org/2009/08/platform#" + s)
val nodeBuff = new ListBuffer[NodeBuffer]
resultDocModifier.setTitle("Account Control Panel");
resultDocModifier.addNodes2Elem("tx-module", <h1>Account Control Panel</h1>);
resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li><a href="profile">Profile</a></li>);
resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li class="tx-active"><a href="#">Settings</a></li>);

if ((res/cp("userBundlePermission")*) == "true") {
	nodeBuff +=(<h2>Bundle Control Panel</h2>
	<h3>Install Bundle</h3>

	<form method="post" action="control-panel/install-bundle" enctype="multipart/form-data">
		<input type="file" class="FieldText" name="bundle" size="30" title="bundle path"/><br/><br/>
		<input style="width: 5em;" value="Install" type="submit"/><p />
	</form>
	<br/><br/>
	<h3>User Bundles</h3>
	<table border="1">
	<tbody>
		<tr>
			<th>Location</th>
			<th>Status</th>
			<th colspan="4">Action</th>
		</tr>

		{for (renderlet <- (res/-osgi("owner")).sort((a,b) => ((a*)<(b*)))) yield
		<tr>
			<td>{renderlet*}</td>
			<td>{renderlet/osgi("status")* match {
				case "2" => "Installed"
				case "4" => "Resolved"
				case "8" => "Starting"
				case "16" => "Stopping"
				case "32" => "Active"
				}}
			</td>
			{if (((renderlet/osgi("bundle_id")).length) > 0)
			<td>
			<form method="post" action="control-panel/start-bundle">
			<input name="bundleId" value={(renderlet/osgi("bundle_id"))*} type="hidden"/>
			<input value="start" type="submit"/>
			</form>
			</td>
			<td>
			<form method="post" action="control-panel/stop-bundle">
			<input name="bundleId" value={(renderlet/osgi("bundle_id"))*} type="hidden"/>
			<input value="stop" type="submit"/>
			</form>
			</td>
			<td>
			<form method="post" action="control-panel/uninstall-bundle">
			<input name="bundleId" value={(renderlet/osgi("bundle_id"))*} type="hidden"/>
			<input value="uninstall" type="submit"/>
			</form>
			</td>
			else
			<td colspan="3">Not registered as bundle</td>}
			</tr>
			}
		</tbody>
	</table>
	<br/>)
}

if((res/cp("changePasswordPermission")*) == "true") {
	nodeBuff +=(<h2>Change Password</h2>


	<form action="control-panel/change-password" method="post">
		<fieldset>
			<ol style="display: block;">
				<li class="tx-line" style="background-image: none;">
					<label>Current Password:</label>
					<span class="tx-item">
						<input type="password" name="oldPW"/>
					</span>
				</li>
				<li class="tx-line" style="background-image: none;">
					<label>New Password:</label>
					<span class="tx-item">
						<input type="password" name="newPW"/>
					</span>
				</li>
				<li class="tx-line" style="background-image: none;">
					<label>Confirm new Password:</label>
					<span class="tx-item">
						<input type="password" name="confirmNewPW"/>
					</span>
				</li>
				<br />
				<input style="width: 5em;" type="submit" name="submit" value="Submit"/>
			</ol>
			<br/>
		</fieldset>
	</form>)


}

if((res/cp("changedPassword")).length > 0) {
	nodeBuff +=(<br /><span>Password has not changed, either wrong current password or the
				new password and the confirmation didn't match!<br /><br /></span>)
}


nodeBuff +=(<h2>Change user's default language</h2>
	<form method="post" action="control-panel/change-language">
		{render(context/platform("instance")/platform("languages"), "naked")}
		<br/><br/>
		<script type="text/javascript">$("#availablelanguages").val({"'" + (context/platform("user")/platform("preferredLangInISOCode")*) + "'"})</script>
		<input style="width: 5em;" type="submit" name="submit" value="Submit"/>
		<br/><br/>
	</form>)

<div id="tx-content">
	<div class="tx-edit" style="margin-left: 0.5em;">
	{if(nodeBuff.isEmpty)
		<span>There are no settings you can configure for this account!</span>
	else
		nodeBuff
	}
	</div>
</div>
      }
    }
  }
}
