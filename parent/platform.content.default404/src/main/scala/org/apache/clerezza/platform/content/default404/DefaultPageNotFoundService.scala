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

package org.apache.clerezza.platform.content.default404

import org.apache.clerezza.platform.content.PageNotFoundService
import javax.ws.rs.core.UriInfo
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import java.security.AccessController
import java.security.PrivilegedAction
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

/**
 * This returns the content of the resource /page-not-found dereferenced as uri
 * with the same authority section as the request.
 */
class DefaultPageNotFoundService extends PageNotFoundService {

	private val notFoundPagePath = "/page-not-found"

	override def createResponse(uriInfo: UriInfo) = {
		if (uriInfo.getPath == notFoundPagePath) {
			Response.status(Status.NOT_FOUND).build();
		} else {
			val pageNotFoundUrl = new URL(uriInfo.getBaseUri.toURL, notFoundPagePath)
			AccessController.doPrivileged(
				new PrivilegedAction[Option[(String, InputStream)]] {
					def run() = {
						val connection = pageNotFoundUrl.openConnection()
						try {
							Some(connection.getContentType, connection.getInputStream)
						} catch {
							case _: FileNotFoundException => None
						}
					}
				}
			) match {
				case Some((mediaTypeString, in)) => Response.status(Status.NOT_FOUND).`type`(mediaTypeString).entity(in).build();
				case None => Response.status(Status.NOT_FOUND).build();
			}

		}
	}
	
}
