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

package org.apache.clerezza.foafssl.ssl

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager;
import org.jsslutils.sslcontext.X509TrustManagerWrapper
import org.jsslutils.sslcontext.trustmanagers.TrustAllClientsWrappingTrustManager
import org.slf4j.LoggerFactory
import org.osgi.service.component.ComponentContext
import org.apache.clerezza.foafssl.auth.X509Claim


object X509TrustManagerWrapperService {
	private val logger = LoggerFactory.getLogger(classOf[X509TrustManagerWrapperService])
}


class X509TrustManagerWrapperService() extends X509TrustManagerWrapper {

	import X509TrustManagerWrapperService._

	override def wrapTrustManager(trustManager: X509TrustManager): X509TrustManager = {

		new TrustAllClientsWrappingTrustManager(trustManager) {

			//At this level we just check if there are webids
			override def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {
				try {
					val webIdUriRefs = X509Claim.getClaimedWebIds(chain(0))
					if (webIdUriRefs.isEmpty) {
						trustManager.checkClientTrusted(chain, authType)
					}
					return

				} catch {
					//todo: this should be more clever, only displaying full stack trace if requested
					//todo: currently could be a denial of service attack - by filling up your hard drive
					case ex: Throwable => {
						logger.info("can't check client", ex)
						throw new CertificateException("cannot check client" + ex.getMessage);
					}
				}
			}
		}
	}

	protected def activate(context: ComponentContext) = { }


}
	 