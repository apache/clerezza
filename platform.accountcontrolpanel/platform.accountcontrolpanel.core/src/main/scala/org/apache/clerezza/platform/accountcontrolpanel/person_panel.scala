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
package org.apache.clerezza.platform.accountcontrolpanel

import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL
import org.apache.clerezza.rdf.utils.GraphNode
import xml.{NodeSeq, Text, Node}
import java.net.{URLEncoder, URL}
import org.apache.clerezza.rdf.ontologies.{RDF, FOAF, RDFS}
import org.apache.clerezza.rdf.web.proxy.WebProxy
import javax.ws.rs.core.MediaType

object person_panel {
	final val emptyText = new Text("")

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
	def personInABox(p: RichGraphNode): NodeSeq = {
		val pix = firstOf(p, FOAF.depiction, FOAF.logo).getNode match {
			case uri: UriRef => uri.getUnicodeString
			case _ => "http://upload.wikimedia.org/wikipedia/commons/0/0a/Gnome-stock_person.svg"
		}

		val pixml= { <a href={"people?uri="+encode(p*)}><img src={pix} width="70px" /></a> }
		return pixml ++ new Text(getName(p))
	}

	def encode(url: String): String =  URLEncoder.encode(url,"UTF8")

	/**
	 * get a useable name from the properties available including nick
	 */
	def getName(p: RichGraphNode): String =  {
		 val name = p/FOAF.name*;
		 if ("" != name ) { return name }
		 val firstNm: String = p/FOAF.firstName*;
		 val fmlyNm :String = firstOf(p, FOAF.family_name,FOAF.familyName)*;
  		 if ("" != firstNm || "" != fmlyNm) { return firstNm+" "+fmlyNm }
		 return p*

	}

	def personHtml(p: RichGraphNode): NodeSeq = {
		{<tr><td colspan="2">Person</td></tr>}++
		ifE(p!){case f:UriRef=>(<tr><td><input type="checkbox" name="webId" value={p*}/>Add as contact</td><td><a href={p*}>{p*}</a></td></tr>);
				  case _ => emptyText;}++
		ifE(p/FOAF.name){f=>(<tr><td>Name:</td><td>{f*}</td></tr>)}++
		 ifE(p/FOAF.firstName){f=>(<tr><td>First Name:</td><td>{f}</td></tr>)} ++
		 ifE(firstOf(p,FOAF.family_name,FOAF.familyName)){f=>(<tr><td>Family Name:</td><td>{f*}</td></tr>)} ++
		 ifE(p/FOAF.mbox){f =>(<tr><td>Mbox:</td><td><ul>{for (m<-f)yield {<li><a href={m*}>{m*}</a></li>}}</ul></td></tr>)}++
		 ifE(p/FOAF.homepage){f =>(<tr><td>Homepage:</td><td><ul>{for (x<-f)yield <li><a href={x*}>{x*}</a></li>}</ul></td></tr>)}++
		 ifE(p/FOAF.currentProject){f =>(<tr><td>Current Project(s):</td><td><ul>{for (x<-f) yield <li>{linkNlabel(x)}</li>}</ul></td></tr>)}++
		 ifE(p/FOAF.depiction){f=>(<tr><td>Depictions:</td><td><img src={f*} /></td></tr>)}++
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

	def linkNlabel(res: RichGraphNode): Node = {
		var label: String = res/RDFS.label! match {
			case uri: UriRef => uri.toString
			case _ => res*
		}
		return <a href={res*}>{label}</a>
	}

	def agentHtml(agent: RichGraphNode) = {<table>{
			ifE({agent/FOAF.name}){ case (f)=>(<tr><td>Name:</td><td>{f}</td></tr>)}++
			ifE(agent/FOAF.logo*){f=>(<tr><td>Logo:</td><td><img src={f} /></td></tr>)}
	}</table> }

}

class person_panel extends SRenderlet {
	def getRdfType() = CONTROLPANEL.ProfileViewerPage
  import person_panel._


	override def renderedPage(arguments: XmlResult.Arguments) = {
	  new XmlResult(arguments) {

	  override def content = {
		def cp(s: Any) =  new UriRef("http://clerezza.org/2009/03/controlpanel#" + s)
		def platform(s: Any) = new UriRef("http://clerezza.org/2009/08/platform#" + s)
		resultDocModifier.addStyleSheet("profile/style/profile.css");
		resultDocModifier.setTitle("Profile Viewer");
		resultDocModifier.addNodes2Elem("tx-module", <h1>Account Control Panel</h1>);
		resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li class="tx-active"><a href="#">Profile Viewer</a></li>);
		resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li><a href="control-panel">Settings</a></li>);
	   resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li><a href="profile">Profile</a></li>);

		val webIdUri= new UriRef(uriInfo.getQueryParameters(true).getFirst("uri"))
//	   val it: CollectedIter[RichGraphNode] = res / FOAF.primaryTopic
//	   val primeTpc: RichGraphNode = it.apply(0)
		val agent : RichGraphNode=  $[WebProxy].fetchSemantics(webIdUri) match { case Some(grph) => grph; case None => res};

//			primeTpc! match {
//			case uri : UriRef => $[WebProxy].fetchSemantics(uri) match { case Some(grph) => grph; case None => res};
//			case _ => res
//		}

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

		<div id="tx-content">
			 <h2>Profile Viewer</h2>
			<form action="profile/addContact" method="POST">
			<table>
			{ val typ: Resource = (agent/RDF.`type`).!
		     typ match {
  			     case FOAF.Person => personHtml(agent)
				  case FOAF.Group => groupHtml(agent)
				  case FOAF.Agent => agentHtml(agent)
				  case _ => allAgentsHtml(agent.getGraph)
			  }
			}
			</table>
				<input type="submit" value="add contacts"/>
		   </form>
			<code><pre>{
			  val s =org.apache.clerezza.rdf.core.serializedform.Serializer.getInstance();
			  import java.io._
			  val bout = new ByteArrayOutputStream()
			  s.serialize(bout,agent.getGraph(),"text/rdf+n3");
			  bout.toString("UTF-8")
			}</pre></code>
		</div>
    }

	}
  }
}


