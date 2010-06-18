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

package org.apache.clerezza.foafssl.auth

import org.apache.clerezza.foafssl.CertUtilities
import org.apache.clerezza.foafssl.WebDescriptionProvider
import org.apache.clerezza.platform.security.auth._
import org.apache.clerezza.rdf.core._
import org.wymiwyg.wrhapi.Request
import org.wymiwyg.wrhapi.Response
import net.java.dev.sommer.foafssl.claims._


/**
 * Here we no longer care about verifying the web-id claims as this should
 * already have been done by X509TrustManagerWrapperService
 */
class FoafSslAuthentication extends WeightedAuthenticationMethod {

	private var descriptionProvider: WebDescriptionProvider = null;

	protected def bindWebDescriptionProvider(descriptionProvider: WebDescriptionProvider) = {
		this.descriptionProvider = descriptionProvider
	}
	
	protected def unbindWebDescriptionProvider(descriptionProvider: WebDescriptionProvider) = {
		this.descriptionProvider = null
	}

	
	def authenticate(request: Request): String = {
		val certificates = request.getCertificates()
		println(certificates)
		if ((certificates == null) || (certificates.length == 0)) {
			return null
		} else {
			val webIdUriRefs = CertUtilities.getClaimedWebIds(certificates)
			/*val webIds = for(principal <- x509Claim.getPrincipals) yield {
				new UriRef(principal.getUri.toString)
			}*/
		 //descriptionProvider.getWebDescription
			webIdUriRefs(0).getUnicodeString
		}
	}

	def writeLoginResponse(request: Request, response: Response,
			cause: Throwable) = {
		false;
	}

	def getWeight() = 400
}
