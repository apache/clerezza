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

package org.apache.clerezza.platform.users

import org.apache.clerezza.platform.security.WebIdBasedPermissionProvider
import org.apache.clerezza.rdf.core.UriRef

class UserGraphAcessPermissionProvider extends WebIdBasedPermissionProvider {
	
	override def getPermissions(webId: UriRef) : java.util.Collection[String] = {
		import scala.collection.JavaConversions._
		val uriString = webId.getUnicodeString
		def uriStringWithoutFragment = {
			val hashPos = uriString.indexOf('#')
			if (hashPos != -1) {
				uriString.substring(0, hashPos)
			} else {
				uriString
			}
		}
		List("(org.apache.clerezza.rdf.core.access.security.TcPermission \""+uriStringWithoutFragment+"\" \"readwrite\")")
	}
}
