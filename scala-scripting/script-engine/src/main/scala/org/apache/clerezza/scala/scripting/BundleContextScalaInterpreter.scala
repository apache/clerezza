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

import org.osgi.framework.BundleContext
import org.osgi.framework.Bundle
import java.io.{File, PrintWriter}
import scala.tools.nsc._;
import scala.tools.nsc.interpreter._;
import scala.tools.nsc.io.{AbstractFile, PlainFile}
import scala.tools.nsc.util._
import java.io.PrintWriter
import java.net._
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.reporters.Reporter
import scala.tools.util.PathResolver



class BundleContextScalaInterpreter(bundleContext : BundleContext, out: PrintWriter)
		extends Interpreter(new Settings, out) {

	def this(bundleContext : BundleContext) = {
		this(bundleContext, new PrintWriter(System.out))
	}

	override lazy val classLoader: AbstractFileClassLoader = {
		new AbstractFileClassLoader(virtualDirectory, this.getClass.getClassLoader())
	}
	override protected def newCompiler(settings: Settings, reporter: Reporter) = {
		settings.outputDirs setSingleOutput virtualDirectory
		new BundleContextScalaCompiler(bundleContext, settings, reporter)
	}
}

/*
 * unfortunately there seems to be no way to change the classpath, so this doesn't
 * listen to BunldeEvents
 */
class BundleContextScalaCompiler(bundleContext : BundleContext,
		settings: Settings, reporter: Reporter)
		extends Global(settings, reporter) {
	

	override lazy val classPath: ClassPath[AbstractFile] = {

		val classPathOrig: ClassPath[AbstractFile]  = new PathResolver(settings).result
		var bundles: Array[Bundle] = bundleContext.getBundles
		val classPathAbstractFiles = for (bundle <- bundles;
										  val url = bundle.getResource("/");
										  if url != null) yield {
			if ("file".equals(url.getProtocol())) {
				new PlainFile(new File(url.toURI()))
			}
			else {
				BundleFS.create(bundle);
			}
		}
		val classPaths: List[ClassPath[AbstractFile]] = (for (abstractFile <- classPathAbstractFiles)
			yield {
					new DirectoryClassPath(abstractFile, classPathOrig.context)
				}) toList

		new MergedClassPath[AbstractFile](classPathOrig :: classPaths,
			   classPathOrig.context)

	}

	override def rootLoader: LazyType = {
		new loaders.JavaPackageLoader(classPath)
	}
}

