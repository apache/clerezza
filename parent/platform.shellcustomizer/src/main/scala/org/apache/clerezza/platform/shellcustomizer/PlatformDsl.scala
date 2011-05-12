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

import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import org.osgi.framework.BundleContext
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza
import clerezza.platform.graphprovider.content.ContentGraphProvider
import clerezza.rdf.core.serializedform.Parser

/**
 * A DSL to acces various services of the platform, mainly designed for usaged
 * on the console.
 */
class PlatformDsl(outputStream: OutputStream, bundleContext: BundleContext) {

	private lazy val out = new PrintWriter(new OutputStreamWriter(outputStream, "utf-8"), true)
	private val serviceDsl = new ServicesDsl(bundleContext)
	import serviceDsl._

	lazy val contentGraph = $[ContentGraphProvider].getContentGraph
	def parser = $[Parser]

	def uname {
		out println ("Clerezza on "+System.getProperty("java.vm.name"))
	}


	//here we could add a command to set the debug output to the console for some packages
}
