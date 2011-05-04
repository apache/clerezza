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

import org.apache.clerezza.foafssl.test.WebIDTester
import org.apache.clerezza.platform.typerendering.scala.{SRenderlet, XmlResult}
import org.apache.clerezza.platform.security.UserUtil
import org.apache.clerezza.foafssl.auth.X509Claim
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.asn1.ASN1ObjectIdentifier
/**
 * Assertion an X509 Certificate to see if it contains the correct fields
 *
 * @author hjs
 * @created: 11/04/2011
 */

class X509TestPg extends SRenderlet {
	def getRdfType() = WebIDTester.testCls

	override def renderedPage(arguments: XmlResult.Arguments) = new XhtmlX509TestPg(arguments)
}

class XhtmlX509TestPg(arguments: XmlResult.Arguments) extends XmlResult(arguments )  {
  val subj = UserUtil.getCurrentSubject();
  val creds: scala.collection.mutable.Set[X509Claim] = collection.JavaConversions.asScalaSet(subj.getPublicCredentials(classOf[X509Claim]));


  override def content = <span>
	  {for(x509claim<-creds) yield {
		  val x509: X509CertificateHolder = new X509CertificateHolder(x509claim.cert.getEncoded)
		  describeX509(x509)
	  }}
  </span>

	def describeX509(x509: X509CertificateHolder) = <span>
	   <h3>Extensions</h3>
		{describeExtensions(x509)}
		<p>Human Readable Details of certificate</p>
		<pre>{x509.toString}</pre>
		</span>

	def describeExtensions(x509: X509CertificateHolder) = <ul>{
//		for (extoid : ASN1ObjectIdentifier <- x509.getExtensionOIDs) yield <li>{
//			x509.getExtension(extoid) match {
//				case ns: NetscapeCertType => ns.
//			}
//		}</li>
	}</ul>
}

