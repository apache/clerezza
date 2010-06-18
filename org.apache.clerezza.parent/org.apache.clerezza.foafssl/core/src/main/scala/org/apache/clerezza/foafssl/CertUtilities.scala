/*
 *  Copyright 2010 reto.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.foafssl

import java.security.cert.X509Certificate
import net.java.dev.sommer.foafssl.claims.X509Claim
import org.apache.clerezza.rdf.core.UriRef

object CertUtilities {
	def getClaimedWebIds(certificates: Array[X509Certificate]) = {
		val x509Claim = new X509Claim(certificates(0))
		import scala.collection.JavaConversions._
		val webIdUriRefs = for (uri <- X509Claim.getAlternativeURIName(certificates(0))) yield {
				new UriRef(uri.toString)
		}
		webIdUriRefs
	}
}
