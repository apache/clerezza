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

package org.apache.clerezza.foafssl.test.pages

import org.apache.clerezza.platform.typerendering.scala.{XmlResult, SRenderlet}
import org.apache.clerezza.platform.security.UserUtil
import org.apache.clerezza.foafssl.test.WebIDTester
import org.apache.clerezza.foafssl.ontologies.{RSA, CERT}
import org.apache.clerezza.rdf.scala.utils.Preamble._
import java.security.{PrivilegedAction, AccessController}
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.core.serializedform.Serializer
import org.apache.clerezza.foafssl.auth.{Verification, WebIDClaim, X509Claim}
import java.io.{PrintStream, ByteArrayOutputStream}
import xml.{Node, NodeSeq}
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils.{CollectedIter, RichGraphNode}
import org.apache.clerezza.rdf.ontologies.{XSD, RDF}
import org.apache.clerezza.platform.users.WebIdGraphsService

/**
 * @author hjs
 * @created: 01/04/2011
 */

class WebIDClaimPg extends SRenderlet {
	def getRdfType() = WebIDTester.testCls

	override def renderedPage(arguments: XmlResult.Arguments) = new XhtmlWebIDClaimPg(arguments, webIdGraphsService)

	//TODO a renderlet should not need services,

	private var webIdGraphsService: WebIdGraphsService = null

	protected def bindWebIdGraphsService(webIdGraphsService: WebIdGraphsService): Unit = {
		this.webIdGraphsService = webIdGraphsService
	}

	protected def unbindWebIdGraphsService(webIdGraphsService: WebIdGraphsService): Unit = {
		this.webIdGraphsService = null
	}



}

object XhtmlWebIDClaimPg {
   val emptyxml=new scala.xml.Text("")
}

class XhtmlWebIDClaimPg(arguments: XmlResult.Arguments, webIdGraphsService: WebIdGraphsService) extends XmlResult(arguments )  {
  import XhtmlWebIDClaimPg._

	resultDocModifier.setTitle("WebId Tests");
	resultDocModifier.addNodes2Elem("tx-module", <h1>Test Panel</h1>);
	resultDocModifier.addNodes2Elem("tx-module-tabs-ol", <li class="tx-active"><a href="#">WebId Tester</a></li>);

  override def content = <div id="tx-content"> <h2>WebID Login Test Page</h2>
    <p>The TLS connection was established. We do not test the basic TLS connection.</p>
      {
    val subj = UserUtil.getCurrentSubject();
    val creds: scala.collection.mutable.Set[X509Claim] = collection.JavaConversions.asScalaSet(subj.getPublicCredentials(classOf[X509Claim]));
    if (creds.size==0) <p>No X509 credentials available.</p>
    else for (cred <- creds) yield describeX509Claim(cred)
    }
	  <p>For very detailed test information to send to support <a href="WebId/n3">download this n3 file</a>.</p>
  </div>


  def describeX509Claim(claim: X509Claim) = {
     <p>The Certificate sent can be viewed <a href="WebId/x509">in detail here</a>. The public key is:
       <pre>{claim.cert.getPublicKey}</pre>
     </p>

     <p>The certificate sent contains {claim.webidclaims.size} WebId Claims. They are listed here, and it is shown if
       they have been verified.</p>
     <ul>{
        for (idClaim <- claim.webidclaims) yield <li>{describeWebIDClaim(idClaim)}</li>
     }</ul>
  }

  def getWebIDProfile(claim: WebIDClaim): Option[GraphNode] = {
    val som = AccessController.doPrivileged(new PrivilegedAction[Option[GraphNode]] {
      def run = Some(new GraphNode(claim.webId, webIdGraphsService.getWebIdInfo(claim.webId).publicProfile))
    })
    som
  }

  def describeWebIDClaim(claim: WebIDClaim) = {
      <span>
        Claimed ID: <a href={claim.webId.getUnicodeString}>{claim.webId.getUnicodeString}</a><br/>
        Claim was {claim.verified}<br/>
        {
        claim.verified match {
        case Verification.Verified => verifiedClaim(claim)
        case Verification.Failed => claimFailure(claim)
        case Verification.Unsupported => <p>WebId's with this protocol are currently unsupported</p>
        case Verification.Unverified => <p>Currently this is not possible, but in asynchronous situations it will be</p>
          }
        }
      </span>
  }

  def verifiedClaim(claim: WebIDClaim) = {
    <p>Supporting information from the WebID Profile
      {val som: Option[GraphNode] = getWebIDProfile(claim)
    som match {
      case Some(profile) => {
        val keys = profile /- CERT.identity
        for (key <- keys) yield displayKey(key)
      }
      case None => "No remote graph?"
    }}
    </p>
  }

  def claimFailure(claim: WebIDClaim) = <p>
     The Claim failed because { import collection.JavaConversions._
       for (e: Throwable <- claim.errors) yield <pre>{
         val str = new ByteArrayOutputStream(1024)
         val out = new PrintStream(str)
         e.printStackTrace(out)
         str.toString("UTF-8")
         }</pre>
    }
    </p>

  def displayKey(key: RichGraphNode) = <span>
	  {val errs = verifyKeyClosely(key)
	  if (errs.size > 0) <ul>{errs}</ul>
	  else scala.xml.Null
	  }
	  <pre>
		  {val graph: Graph = key.getNodeContext
	  val sout = Serializer.getInstance()
	  val out = new ByteArrayOutputStream(1024)
	  sout.serialize(out, graph, "text/rdf+n3")
	  out.toString("UTF-8")}
	  </pre>
  </span>

	/**
	 * test numbers written in the old style with non literals
	 *
	 * @param The node that is the number. Should be a non literal
	 * @param The relation name to the string that identifies the number
	 * @param The role of the number in the public key (modulus, exponent, ...)
	 *
	*/
	def nonLiteralNumTest(number: RichGraphNode, relToString: UriRef, numberRole: String ): NodeSeq = {
		val typeTest = number! match {
			case uri: UriRef => <li>Warning: your {numberRole} is a uri. The new spec requires this to be a literal.
					 Uri found: {uri.getUnicodeString}</li>
			case _: BNode => <li>Warning: your {numberRole} is a blank node. The newer spec requires this to be a literal</li>
		}
		typeTest++
		 {
			val hexs = number / relToString
			if (hexs.size > 1) {
				<li>WARNING: Your {numberRole} has more than one relation to a hexadecimal string. Unless both of those strings
					map to the same number, your identification experience will be very random</li>
			} else if (hexs.size == 0) {
				<li>WARNING: your {numberRole} has no decimal literal attached by the {relToString} relation</li>
			} else hexs! match {
				case bnode: NonLiteral => <li>Warning: Your {numberRole} has a relation to a non literal, where it should be a
					relation to a literal. It is possible that logically it all works out, but don't even expect a few engines to
					bother reflecting deeply on this. We don't check past here.</li>
				case lit: Literal => emptyxml
			}
		}

	}

	def literalNumTest(number: Literal): NodeSeq = {
		number match {
			case tl: TypedLiteral => tl.getDataType match {
		   	case CERT.int_ => emptyxml
			   case CERT.hex => emptyxml
			   case CERT.decimal => emptyxml
			   case XSD.base64Binary => <li>Base 64 binary is not a number. If you wish to have a base64 integer notation
				   let the WebId Group know</li>
				case XSD.hexBinary => <li>Base hex binary is not a number. If you wish to have a hex integer notation
				   use the {CERT.hex} relation. It is easier for people to write out</li>
				case XSD.nonNegativeInteger => emptyxml
				case XSD.integer => emptyxml
				case XSD.positiveInteger => emptyxml
				case XSD.int_ => emptyxml
				case littype => <li>We don't know how to interpret numbers of {littype}.
					It would be better to use either
					<a href={CERT.hex.getUnicodeString}>cert:hex</a> or <a href={CERT.int_.getUnicodeString}>cert:int</a></li>
			}
			case _: Literal => <li>Warning: you need to put a literal type, so we can know how to interpret the string.
		</li>
		}
	}

	def verifyKeyClosely(key: RichGraphNode) : NodeSeq= {
		val moduli = key/RSA.modulus
		val modWarn = if (moduli.size>1) {
			<li>Warning: you have written the modulus out in more than one way. If they a are not equal your
				connections might be alternatively successful</li>
		} else if (moduli.size==0) {
			<li>Warning: you have no modulus here. RSA keys must have one modulus</li>
		} else  moduli! match {
				 case _: NonLiteral => nonLiteralNumTest(moduli(0),CERT.hex,"modulus")
				 case lit: Literal => literalNumTest(lit)
		}
		val expts = key/RSA.public_exponent
		val expWarn = if (expts.size>1) {
			<li>Warning: you have more than one exponent. They must be equal, or your connections will be unpredicable</li>

		} else if (expts.size==0) {
			<li>Warning: you have no exponent here. RSA keys must have one public exponent</li>
		} else expts! match {
				 case _: NonLiteral => nonLiteralNumTest(expts(0),CERT.decimal,"exponent")
				 case lit: Literal => literalNumTest(lit)
		}

		return modWarn ++ expWarn
	}

}