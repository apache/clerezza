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
package org.apache.clerezza.scala.scripting;


import java.security.AccessController
import java.security.PrivilegedAction
import org.apache.clerezza.scala.scripting.util.FileWrapper
import org.apache.clerezza.scala.scripting.util.GenericFileWrapperTrait
import org.apache.clerezza.scala.scripting.util.VirtualDirectoryWrapper
import org.osgi.framework.BundleContext
import org.osgi.service.component.ComponentContext;
import scala.tools.nsc._;
import scala.tools.nsc.interpreter._;
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.util._
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import java.io.Reader
import java.net._


class CompilerService() {
	
	protected var bundleContext : BundleContext = null;

	def activate(componentContext: ComponentContext)= {
		bundleContext = componentContext.getBundleContext
	}

	def deactivate(componentContext: ComponentContext) = {
		bundleContext = null
	}

	def createCompiler(out: PrintWriter, outputSirectory: AbstractFile) : TrackingCompiler = {
		TrackingCompiler(bundleContext, out, outputSirectory)
	}

	def compile(sources: List[Array[Char]]): List[Class[_]] = {
		val virtualDirectory = new VirtualDirectory("(memory)", None)
		compile(sources, virtualDirectory)
	}

	def compile(sources: List[Array[Char]], outputDirectory: AbstractFile): List[Class[_]] = {
		val out = new ByteArrayOutputStream
		val printWriter = new PrintWriter(out)
		val compiler = createCompiler(printWriter, outputDirectory)
		compiler.compile(sources)
	}

	
}
