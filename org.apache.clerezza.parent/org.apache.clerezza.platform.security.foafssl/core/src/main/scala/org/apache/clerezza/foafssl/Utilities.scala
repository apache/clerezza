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

package org.apache.clerezza.foafssl

import java.security.cert.X509Certificate
import org.apache.clerezza.rdf.core.UriRef

/**
 * Utilitie functions to deal with certificates
 *
 * @author Reto Bachmann-Gmür, Henry Story
 */
object Utilities {
	
	/**
	 * same as getClaimedWebIds(chain(0))
	 */
	def getClaimedWebIds(chain: Array[X509Certificate]): List[UriRef] = {
		getClaimedWebIds(chain(0))
	}


	/**
	 * Extracts the URIs in the subject alternative name extension of an X.509
	 * certificate (perhaps others such as email addresses could also be
	 * useful).
	 *
	 * @param cert
	 *            X.509 certificate from which to extract the URIs.
	 * @return list of java.net.URIs built from the URIs in the subjectAltName
	 *         extension.
	 */
	def getClaimedWebIds(cert: X509Certificate): List[UriRef] = {
		//           throws CertificateParsingException {
		var result : List[UriRef] =  Nil
		if (cert == null) {
			return result;
		}

		val names = cert.getSubjectAlternativeNames()
		if (names == null) {
			return result;
		}

		//  val n = names(0)
		import scala.collection.mutable
		val it = names.iterator;
		while (it.hasNext)  {
			val altNme = it.next()
			val altTpe = altNme.get(0);
			val altObj = altNme.get(1);
			if ((altTpe.asInstanceOf[Integer] == 6) && altObj.isInstanceOf[String]) {
				result =  new UriRef(altObj.asInstanceOf[String]) :: result;
			}
		}
		return result
	}
	
	def cretateUsernameForWebId(webId: UriRef) = {
		webId.getUnicodeString.replace(":", "");
	} 

}