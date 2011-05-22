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
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.foafssl.ontologies.CERT
import org.apache.clerezza.foafssl.ontologies.RSA
import org.apache.clerezza.platform.typerendering.scala._
import java.math.BigInteger
import java.util.Date
import java.text._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL
import org.apache.clerezza.rdf.ontologies.{RDFS, DC, FOAF}

class profile_panel extends SRenderlet {
  override def getRdfType() = CONTROLPANEL.ProfilePage

  override def renderedPage(arguments: XmlResult.Arguments) = new ProfilePanelXHTML(arguments)
}

class ProfilePanelXHTML(arguments: XmlResult.Arguments) extends XmlResult(arguments ) {

	//set header properties

 	resultDocModifier.addStyleSheet("/account-control-panel/style/profile.css");
	resultDocModifier.addScriptReference("/account-control-panel/scripts/profile.js");
	resultDocModifier.addScriptReference("/account-control-panel/scripts/IEKeygen.js");
	resultDocModifier.setTitle("Account Control Panel");
	resultDocModifier.addNodes2Elem("tx-module", <h1>Account Control Panel</h1>);
	resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li class="tx-active"><a href="#">Profile</a></li>);
	resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li><a href="control-panel">Settings</a></li>);

	//constants and variables

	lazy val agent: RichGraphNode = res / FOAF.primaryTopic


	// the content itself
	// clearly in this case there is so much more in the the methods, that one could hesitate to call this an ssp

	override def content = {
	  <div id="tx-content">
		  <h2>Personal Profile</h2>{agent ! match {
				case _: BNode => createWebId()
				case _: UriRef => existingWebId()
			 }}
	  </div>

	}

	//methods used to create content

	def cp(s: Any) =  new UriRef("http://clerezza.org/2009/03/controlpanel#" + s)
	def platform(s: Any) = new UriRef("http://clerezza.org/2009/08/platform#" + s)


	def createWebId() = {
	  <h3>Associate Profile to WebID</h3>
	  <div id="newOrExistingSelection">
		<p>Your profile is not currently associated to a WebID. A WebID allows you
		  to link your friends as well as to log-in to many sites (supporting foaf+ssl
		  or open-id).
		</p>
		<p>You may either create a new WebID or associate your account to an
		  existing WebID. Only creating a WebID here will allow you to manage your
		  profile here.
		</p>
		<form action="#" id="associateSelection">
		  <button type="button" id="newWebIdButton">Create a new Web-Id</button>
		  <button type="button" id="existingWebIdButton">I already have a Web-ID and want to use it</button>
		</form>
	  </div>
	  <div id="createNewWebId">
		<p>You have chosen to create a new Web-Id.</p>
		<p>The Web-ID will be created as follows:
		  <br/>
		  <ol>
			<li>Web-Id:
			  {var webId = res / cp("suggestedPPDUri") *;
				webId += "#me";
				webId}
			</li>
			<li>Personal-Profile Document: {res/cp("suggestedPPDUri")*}</li>
		  </ol>
		</p>
		<form method="post" action="profile/create-new-web-id">
		  <input value="Create it!" type="submit"/>
		</form>
	  </div>
	  <div id="setExistingWebId">
		<p>Please enter your Web-Id, if your Web-Id supports Foaf+SSL you will
		  be able to use it to log in to this site.</p>
		<form method="post" action="profile/set-existing-webid">
		  <label for="webid">WebID</label> <input type="text" name="webid" size="80" title="Web-ID"/>
		  <br/>
		  <input value="Associate Profile to Web-Id" type="submit"/>
		  <p/>
		</form>
	  </div>

	}

	def existingWebId() = {
	  if ((res / cp("isLocalProfile")).as[Boolean]) {
		existingLocalWebId()
	  } else {
		roamingUser()
	  }
	}


	def existingLocalWebId() = {
	  <h3>Manage your profile</h3>
		 <p>Here you can change your public profile.</p>

	  <form method="post" action="profile/modify">
		<input type="hidden" name="webId" value={agent *}/>
		<table>
		  <tr><td class="formlabel">Name:</td>
			<td><input type="text" name="name" value={agent / FOAF.name *}/></td>
		  </tr>
		  <tr><td class="formlabel multiline">Description:</td>
			<td><textarea name="description" rows="3" cols="80">{agent / DC.description *}</textarea></td>
		  </tr>
		  <tr><td class="formlabel"><input value="Modify" type="submit"/></td><td/></tr>
		</table>

		<p/>
	  </form>

	  <h3>Contacts</h3>
	  <form id="addContact" method="get" action="/browse/person">
	  <table>{ var i =0
		  val friends = for (friend <- agent/FOAF.knows) yield {
		  import person_panel._
		  <td class="personInABox">{personInABox(friend)}</td>
		 }
		 for (row <- friends.grouped(5)) yield <tr>{row}</tr>
	  }</table>
	  <input type="text" name="uri" size="80"/><input type="submit" value="add contact" />
	  </form>

	  <h3>Key and Certificate Creation</h3>

	  <script type="text/javascript"> <![CDATA[$(document).ready(  function() { configurePage(); }   ); ]]> </script>

	  <div id="iehelptext" style="display: none;">
		<p>Using Internet Explorer under Windows Vista or above or Windows
		  Server 2008, you need to configure the following for this to work:</p>
		<ul>
		  <li>Add this site to the <i>Trusted Sites</i> list: in Internet
			Options -&gt; Security -&gt; Trusted Sites -&gt; Sites -&gt; Add ...</li>
		  <li>You may need to configure the trust level (in this tab), using
			<i>Custom Level...</i>: enable <i>Initialize and script ActiveX
			  controls not marked as safe for scripting</i>.</li>
		  <li>If you are using Windows Vista without SP1 or above, you will
			probably need to install <a href="cacert.crt">this certificate</a> as a
			Trusted Root Certification Authority Certificate for your own
			certificate installation to succeed. You should probably remove that
			trusted root CA certificate afterwards.</li>
		</ul>
	  </div>
	  <form id="keygenform" method="post" action="profile/keygen">
		<input name="webId" id="webId" type="hidden" value={agent*} />
		<table>
		  <colgroup><col width="1*"/><col/></colgroup>
		  <tr>
			<td class="formlabel">Certificate Name:</td>
			<td>
			  <input alt="create a certificate name that will help you distinguish it from others you may have" name="cn"
						size="35" id="cn" type="text" value={ ((agent/FOAF.name*)+"@clerezza")}/>
			</td>
		  </tr>
		  <tr>
			<td class="formlabel">Key strength:</td>
			<td id="keystrenghtd">
			  <keygen id="spkac" name="spkac" challenge="TheChallenge1"/>
			</td>
		  </tr>
		  <tr>
			<td class="formlabel">Valid for:</td>
			<td>
			  <input type="text" name="days" value="365" size="4"/>
			  days <input type="text" name="hours" value="0.0" size="4"/> hours</td>
		  </tr>
		  <tr>
			<td class="formlabel">Comment:</td>
			<td><input type="text" name="comment" value="" size="80"/></td>
		  </tr>
		  <tr><td class="formlabel"><input id="keygensubmit" type="submit" value="create certificate" /></td><td/></tr>
		</table>
	  </form>
	  <h3>Existing Certificates</h3>
	  <form method="post" action="profile/deletekey">
		<table>
		  <tr><th>Delete</th><th>Certificate Details</th></tr>
		  <input name="webId" id="webId" type="hidden" value={agent*} />
		  <tbody>{
			  for (key <- agent/-CERT.identity )
				yield { val modulus = (key/RSA.modulus).as[BigInteger]
						if (modulus == null)  <span/> //todo: broken public key, should delete it
						else <tr><td><input type="checkbox" name="keyhash" value={modulus.hashCode().toString()}/></td>
					<td><table>
						<tr><td class="propvalue">Created:</td><td>{beautifyDate(key/DC.date )}</td></tr>
						<tr><td class="propvalue">Comment:</td><td>{ key/RDFS.comment* }</td></tr>
						<tr><td class="propvalue multiline">Modulus:</td><td><code>{ beautifyHex(key/RSA.modulus) }</code></td></tr>
						<tr><td class="propvalue">Exponent:</td><td><code>{ beautifyInt(key/RSA.public_exponent) }</code></td></tr>
						</table>
					</td>
							</tr>}
			}</tbody>
		</table>
		<input type="submit" value="Disable Keys"/>
	  </form>
	  <p></p>

	}

	def roamingUser() = {
	  <h3>Using remote profile</h3>
		 <p>
		{agent / FOAF.nick *}, you have accessed this site using your WebID
			{"<" + (agent *) + ">"}
			which has not been
			created on this site.To edit your profile you should visit the site issuing the
			profile.</p>
	}




  def beautifyDate(dtIt: CollectedIter[RichGraphNode]) {
	  if (0 == dtIt.size) return "_"
	  DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.FULL).format(dtIt.as[Date])
  }


  def beautifyHex(dtIt: CollectedIter[RichGraphNode]): String = {
	  if (0 == dtIt.size) return "warning! missing. Key invalid"
	  //this is a problem, it should always be here or it is invalid, and key should be removed
	  val bigint: BigInteger = dtIt.as[BigInteger]
	  val bstr = bigint.toString(16).toUpperCase;
	  val sbuf = new StringBuffer(bstr.size + (bstr.size/2)+10)
	  var cnt = 0
	  for (c <- bstr.toCharArray) {
		if ((cnt % 2) == 0) sbuf.append(' ')
		sbuf.append(c)
		cnt += 1
	  }
	  sbuf.toString
	}



  def beautifyInt(dtIt: CollectedIter[RichGraphNode] ) :String = {
	  if (0 == dtIt.size) return "warning! missing. Key invalid"
	  else return dtIt.as[BigInteger].toString
  }
}


