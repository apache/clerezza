package org.apache.clerezza.platform.shellcustomizer

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
import org.apache.clerezza.shell.Shell
import org.apache.clerezza.shell.ShellCustomizer
import org.apache.clerezza.rdf.scala.utils.{EasyGraph, Preamble}
import org.apache.clerezza.rdf.core.MGraph
import org.apache.clerezza.platform.graphprovider.content._

class PlatformShellCustomizer extends ShellCustomizer {

	private var contentGraphProvider: ContentGraphProvider = null

	def bindings(e: Shell.Environment): List[(String, String, Any)] = {
		val bundleContext = e.componentContext.getBundleContext
		List(("contentGraphPreamble", classOf[Preamble].getName, new Preamble(contentGraphProvider.getContentGraph)),
				("platformDsl", classOf[PlatformDsl].getName, new PlatformDsl(e.out, bundleContext)))
	}
	def imports: List[String] = List("org.apache.clerezza.rdf.scala.utils.EasyGraph._", "contentGraphPreamble._",
									"platformDsl._", "org.apache.clerezza.rdf.core._",
									"org.apache.clerezza.rdf.core.access.TcManager",
									"org.apache.clerezza.rdf.ontologies._",
									"org.apache.clerezza.rdf.scala.utils._")

	protected def bindContentGraphProvider(p: ContentGraphProvider) {
		contentGraphProvider = p
	}

	protected def unbindContentGraphProvider(p: ContentGraphProvider) {
		contentGraphProvider = null
	}
}
