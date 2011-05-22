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
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.{PINGBACK, CONTROLPANEL}
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.utils.GraphNode
import xml.{NodeSeq, Text, Node}
import java.net.{URLEncoder, URL}
import javax.ws.rs.core.MediaType
import org.apache.clerezza.rdf.ontologies.{FOAF, RDF, RDFS}
import javax.swing.UIDefaults.LazyInputMap
import org.apache.clerezza.platform.accountcontrolpanel.FoafBrowser

/**
 * static methods used by person panel and that could possibly be moved to a library
 */
object person_panel {
	val emptyText = new Text("")

	def ifE[T](arg:T)(template: T=>Node ):NodeSeq = {
		def isEmpty(arg: Any): Boolean = {
			arg match {
				case prod: Product => prod.productIterator.forall(isEmpty(_))
				case str: String => (str.size == 0)
				case it: CollectedIter[RichGraphNode] => (it.size == 0)
				case node: RichGraphNode => (null == node)
				case other: AnyRef => (null == other)
				case _ => false //literals can't be empty
			}
		}
		if (isEmpty(arg)) return emptyText else template(arg)
	}

	def firstOf(node: RichGraphNode, uris: UriRef*):CollectedIter[RichGraphNode] = {
		for (uri <- uris) {
			val res : CollectedIter[RichGraphNode] = node/uri
			if (res.size>0) return res
		}
		return new CollectedIter[RichGraphNode]()
	}



	/**
	 * Show a person: a picture, a link to their local profile and their name
	 * Different default icons should be shown if the agent is a person, company, group, robot...
	 *
	 * assumes the p is WebID node (can change later)
	 */
	def getAgentPix(p: RichGraphNode) = {
		val pix = firstOf(p, FOAF.depiction, FOAF.logo, FOAF.img).getNode match {
			case uri: UriRef => uri.getUnicodeString
			case _ => "http://upload.wikimedia.org/wikipedia/commons/0/0a/Gnome-stock_person.svg"
		}
		<a href={"/browse/person?uri="+encode(p*)}><img class="mugshot" src={pix}/></a>
	}

	def personInABox(p: RichGraphNode): NodeSeq = {
		val pixml= getAgentPix(p)
		val pingXML = if ((p / PINGBACK.to).size > 0) p.getNode match {
			case uri: UriRef => {
				val ref = "ping/new?target=" + URLEncoder.encode(uri.getUnicodeString, "UTF-8")
				<a href={ref}>ping me</a>
			}
			case _ => emptyText
		} else emptyText
		return <table><tr><td>{pixml}</td></tr>
			<tr><td>{new Text(getName(p))}<br/>{pingXML}</td></tr>
			</table>
	}

	def encode(url: String): String =  URLEncoder.encode(url,"UTF8")

	/**
	 * get a usable name from the properties available including nick
	 */
	def getName(p: RichGraphNode): String =  {
		 val name = p/FOAF.name*;
		 if ("" != name ) { return name }
		 val firstNm: String = p/FOAF.firstName*;
		 val fmlyNm :String = firstOf(p, FOAF.family_name,FOAF.familyName)*;
  		 if ("" != firstNm || "" != fmlyNm) { return firstNm+" "+fmlyNm }
		 return p*

	}

	def agentHtml(agent: RichGraphNode) = {<table>{
			ifE({agent/FOAF.name}){ case (f)=>(<tr><td>Name:</td><td>{f}</td></tr>)}++
			ifE(agent/FOAF.logo*){f=>(<tr><td>Logo:</td><td><img src={f} /></td></tr>)}
	}</table> }


	def linkNlabel(res: RichGraphNode): Node = {
		var label: String = res/RDFS.label! match {
			case uri: UriRef => uri.toString
			case _ => res*
		}
		return <a href={res*}>{label}</a>
	}

	def platform(s: Any) = new UriRef("http://clerezza.org/2009/08/platform#"+s)

}

/**
 * Metadata class for the person panel
 */
class person_panel extends SRenderlet {
	def getRdfType() = CONTROLPANEL.ProfileViewerPage

	override def renderedPage(arguments: XmlResult.Arguments) = new XmlPerson(arguments)
}

/**
 * Content class for the Person Panel
 */
class XmlPerson(args: XmlResult.Arguments) extends XmlResult(args) {
	import person_panel._

	//
	// Some initial constants
	//

	// either we use the rdf data on this as commented out here,
	//	    val it: CollectedIter[RichGraphNode] = res / FOAF.primaryTopic
	//	    val primeTpc: RichGraphNode = it.apply(0)
	// or we can get that information from URL, as shown here
	//lazy val webIdStr = uriInfo.getQueryParameters(true).getFirst("uri")
	//lazy val webIdUri= new UriRef(webIdStr)

	//lazy val webIdInfo =  $[WebProxy].getResourceInfo(webIdUri)
	//lazy val agent : RichGraphNode=  $[WebProxy].fetchSemantics(webIdUri) match { case Some(grph) => grph; case None => res};
	lazy val agent : RichGraphNode = res / FOAF.primaryTopic
	lazy val agentDoc = FoafBrowser.removeHash(agent.getNode.asInstanceOf[UriRef]).getUnicodeString
	lazy val user= context/platform("user")
	lazy val username = user/platform("userName")*
	lazy val local = username != "" && username != "anonymous"
	//
	// setting some header info
	//

	resultDocModifier.addStyleSheet("/account-control-panel/style/profile.css");
	resultDocModifier.setTitle("Profile Viewer");
	resultDocModifier.addNodes2Elem("tx-module", <h1>Profile Viewer</h1>);
	resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li class="tx-active"><a href="#">Profile Viewer</a></li>);

	//
	// the content itself.
	// This is the piece that is closest to a pure ssp, though there is still too much code in it
	//

	override def content = <div id="tx-content">
		<h2>Profile Viewer</h2>
		{if (local) <form action={"/user/"+username+"/profile/addContact"} method="POST">
			<input type="submit" value="Add Contacts"/>{maintable}<input type="submit" value="Add Contacts"/>
		</form>
		 else maintable }
		<code>
			<pre>{val s = org.apache.clerezza.rdf.core.serializedform.Serializer.getInstance();
			import java.io._
			val bout = new ByteArrayOutputStream()
			s.serialize(bout, agent.getGraph(), "text/rdf+n3");
			bout.toString("UTF-8")
			}</pre>
		</code>
	</div>

	 def maintable = <table>
		 {val typ: Resource = (agent / RDF.`type`).!
		 typ match {
			 case FOAF.Person => personHtml(agent)
			 case FOAF.Group => groupHtml(agent)
			 case FOAF.Agent => agentHtml(agent)
			 case _ => allAgentsHtml(agent.getGraph)
		 }}
	 </table>



	//
	// Methods called by the content
	//

	def relations() = {
		<table>{for (friend <- agent/FOAF.knows) {
			<tr><td>{friend*}</td></tr>
			}
			<tr><td><form id="addContact" method="post" action="profile/people">
			<input type="text" name="webId" size="80"/>
			<input type="submit" value="add contact" />
		</form></td></tr>
		}</table>
	}


	def allAgentsHtml(tc: TripleCollection): Node = {<span>
		<th><tr colspan="2">All agents found</tr></th>
		{ import collection.JavaConversions._
		//todo: change
		  val base = new URL(agent.getNode.asInstanceOf[UriRef].getUnicodeString());
		  val lclPrson = for (tr: Triple <- tc.filter(null, RDF.`type`, FOAF.Person);
				 subjUrl = try { new URL(tr.getSubject.asInstanceOf[UriRef].getUnicodeString) } catch  { case _ => null }
				 if (subjUrl != null && base.sameFile(subjUrl))
		  ) yield tr.getSubject
		  for (p <- lclPrson) yield
				<tbody>{personHtml(new GraphNode(p,tc))}</tbody>
		}
	</span>}

	/**
	 * Is the uri defined here on the page from which the WebID comes?
	 * The point of this function is not to give the user a choice of adding a person to his list
	 * of friends, until that page has been at least once properly de-referenced. (People can link
	 * to non existent foaf files, things that are not persons, etc...)
	 *
	 * For pages with multiple #uris
	 * This will always return false for URIs that are redirected (as the foaf vocab is)
	 *
	 * If that is the reasoning then should we also extend this method to return true for pages already
	 * in the cache?
	 *
	 */
	def definedHere(uri: UriRef):Boolean = uri.getUnicodeString.startsWith(agentDoc)


	def personHtml(p: RichGraphNode): NodeSeq = {
		//note: img is a sub-relation of depiction, so an inference engine would add both, and one would end up with repetition
		//todo: only first image is shown
		{<tr><td colspan="2">Person</td></tr>}++
		ifE(p!){   case u:UriRef=> if (definedHere(u))
			<tr>{if (local) <td><input type="checkbox" name="webId" value={p*}/>Add as contact</td> else <td>WebID</td>}<td><a href={p*}>{p*}</a></td></tr>
			else
			<tr><td><a href=""/>Explore</td><td><a href={"person?uri="+encode(u.getUnicodeString)}>{p*}</a></td></tr>
					  case _ => emptyText;
		}++
		ifE(p/FOAF.name){f=>(<tr><td>Name:</td><td>{f*}</td></tr>)}++
		 ifE(p/FOAF.firstName){f=>(<tr><td>First Name:</td><td>{f}</td></tr>)} ++
		 ifE(firstOf(p,FOAF.family_name,FOAF.familyName)){f=>(<tr><td>Family Name:</td><td>{f*}</td></tr>)} ++
		 ifE(p/FOAF.mbox){f =>(<tr><td>Mbox:</td><td><ul>{for (m<-f)yield {<li><a href={m*}>{m*}</a></li>}}</ul></td></tr>)}++
		 ifE(p/FOAF.homepage){f =>(<tr><td>Homepage:</td><td><ul>{for (x<-f)yield <li><a href={x*}>{x*}</a></li>}</ul></td></tr>)}++
		 ifE(p/FOAF.currentProject){f =>(<tr><td>Current Project(s):</td><td><ul>{for (x<-f) yield <li>{linkNlabel(x)}</li>}</ul></td></tr>)}++
		 ifE(p/FOAF.depiction){f=>(<tr><td>Depictions:</td><td><img src={f*} /></td></tr>)}++
		 ifE(p/FOAF.img){f=>(<tr><td>Depictions:</td><td><img src={f*} /></td></tr>)}++
		 ifE(p/FOAF.logo){f=>(<tr><td>Logo:</td><td><img src={f*} /></td></tr>)}++
		 ifE(p/FOAF.knows){k=>(<tr><td>claims to know</td><td><table>{for (fr<-k) yield displayAgent(fr)}</table></td></tr>)}
	}


	def groupHtml(grp: RichGraphNode): NodeSeq = {
		ifE(grp/FOAF.name){f=>(<tr><td>Name:</td><td>{f}</td></tr>)}++
		ifE(grp/FOAF.logo*){f=>(<tr><td>Logo:</td><td><img src={f} /></td></tr>)}
	}


	def displayAgent(agent: RichGraphNode): NodeSeq = {
	  val typ: Resource = (agent/RDF.`type`).!
	  return typ match {
			case FOAF.Person => personHtml(agent)
			case FOAF.Group => groupHtml(agent)
			case FOAF.Agent => agentHtml(agent)
			case _ => emptyText
	  }
	}


}

