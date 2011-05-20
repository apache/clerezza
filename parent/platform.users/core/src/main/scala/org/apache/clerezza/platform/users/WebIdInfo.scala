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

package org.apache.clerezza.platform.users

import org.apache.clerezza.rdf.core.{MGraph, TripleCollection, UriRef}
import org.apache.clerezza.rdf.core.access.LockableMGraph


/*
 * This functionality is currently limited to users with webid, it could be
 * extended for being available also for users without a URI. But in general
 * many things are easier for user with URIs.
 *
 */
trait WebIdInfo {

	/**
	 * The WebID this instance is about
	 */
	def webId: UriRef

	/**
	 * The WebId profile graph, for remote users this is immutable
	 */
	def publicProfile: TripleCollection

	/**
	 * An MGraph used to store public information about the user. For local
	 * users this is the same as publicProfile. In any case this contains the
	 * triples of the publicProfile.
	 */
	def localPublicUserData: LockableMGraph

	/**
	 * indicates if this WebId is local.
	 */
	def isLocal: Boolean

	/**
	 * forces an update of cached graphs associated with thie WebID (if any)
	 */
	def forceCacheUpdate(): Unit

}