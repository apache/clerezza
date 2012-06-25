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
package org.apache.clerezza.shell

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import org.apache.clerezza.osgi.services.ServicesDsl
import org.osgi.framework.Bundle
import org.osgi.service.component.ComponentContext
import scala.collection.JavaConversions._

class OsgiDsl(context: ComponentContext, outputStream: OutputStream) 
		extends ServicesDsl(context.getBundleContext) {

	lazy val out = new PrintWriter(new OutputStreamWriter(outputStream, "utf-8"), true)
	val bundleContext = context.getBundleContext

	def ps = {
		for (b <- bundleContext.getBundles) {out.println(b.getBundleId+" - "+b.getSymbolicName+" "+b.getLocation)}
	}

	def install(uri: String) = {
		bundleContext.installBundle(uri)
	}

	def start(uri: String) = {
		val b = install(uri)
		b.start()
		b
	}

	def update(pattern: String) = {
		for (b <- bundleContext.getBundles; if (b.getLocation.matches(pattern))) {
			out.println("updating "+b.getLocation)
			b.update()
		}
	}

	def headers(bundleId: Int) {
		headers(bundleContext.getBundle(bundleId))
	}

	def headers(bundle: Bundle) {
		for ((k,v) <- bundle.getHeaders) {out.println(k+" = "+v) }
	}

	def shutdown {
		bundleContext.getBundle(0).stop()
	}
}
