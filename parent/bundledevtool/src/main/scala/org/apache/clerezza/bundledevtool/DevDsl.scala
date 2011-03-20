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

package org.apache.clerezza.bundledevtool

import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import org.osgi.framework.BundleContext
import org.apache.clerezza.osgi.services.ServicesDsl

class DevDsl(outputStream: OutputStream, bundleContext: BundleContext) {

	object Dev {
		private val serviceDsl = new ServicesDsl(bundleContext)
		import serviceDsl._
		private lazy val out = new PrintWriter(new OutputStreamWriter(outputStream, "utf-8"), true)

		def listArchetypes() {
			out println "The following archetypes are available"
			for (a <- $[BundleRoot].availableSkeletons) {
				out println "  - "+a
			}
		}
		
		def create(archetype: Symbol) = new Object() {
			def in(location: String): Unit = try {
				$[BundleRoot].createSourceBundle(archetype, new File(location))
			} catch {
				case u: UnavailableSkeletonException => {
					out println "FAILURE: no archetype "+archetype+" is available"
					listArchetypes()
				}
			}
		}

		def load(location: String) {
			val dir = new File(location)
			if (!dir.isDirectory) {
				out println "No directory "+location+" found"
			} else {
				$[BundleRoot].addSourceBundle(dir)
			}
		}
	}

}
