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

package org.apache.clerezza.scala.scripting

import org.apache.clerezza.scala.scripting.util.FileWrapper
import org.apache.clerezza.scala.scripting.util.GenericFileWrapperTrait
import org.apache.clerezza.scala.scripting.util.VirtualDirectoryWrapper
import org.osgi.framework.BundleContext
import scala.collection.mutable
import scala.tools.nsc._;
import scala.tools.nsc.interpreter._;
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.util._
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.net._


/** a compiler that keeps track of classes added to the directory
 */
class TrackingCompiler private (bundleContext : BundleContext,
		settings: Settings, reporter: Reporter, outputDirectory: AbstractFile,
		writtenClasses: mutable.ListBuffer[AbstractFile])
	extends  BundleContextScalaCompiler(bundleContext : BundleContext,
		settings: Settings, reporter: Reporter) {
	

	/**
	 * compiles a list of class sources returning a list of compiled classes
	 */
	@throws(classOf[CompileErrorsException])
	def compile(sources: List[Array[Char]]): List[Class[_]] = {
		writtenClasses.clear()
		val sourceFiles: List[SourceFile] = for(chars <- sources) yield new BatchSourceFile("<script>", chars)
		(new Run).compileSources(sourceFiles)
		if (reporter.hasErrors) {
			reporter.reset
			throw new CompileErrorsException;
		} 
		val classLoader = new AbstractFileClassLoader(outputDirectory, this.getClass.getClassLoader())
		val result: List[Class[_]] = for (classFile <- writtenClasses.toList;
										  if (!classFile.name.contains('$'))) yield {
			val path = classFile.path
			val relevantPath = path.substring(path.indexOf('/')+1,path.lastIndexOf('.'))
			val fqn = relevantPath.replace("/",".")
			classLoader.loadClass(fqn)
		}
		return result
	}
	
}

object TrackingCompiler {
	def apply(bundleContext : BundleContext, out: PrintWriter, outputDirectory: AbstractFile) = {
		val writtenClasses: mutable.ListBuffer[AbstractFile] = mutable.ListBuffer[AbstractFile]()
		val settings = {
				trait VirtualDirectoryFlavour extends VirtualDirectoryWrapper {
					abstract override def output = {
						println("unexpected call to output "+name)
						super.output
					}
				}

				def wrap(f: AbstractFile): AbstractFile = {
					f match {
						case d: VirtualDirectory => new VirtualDirectoryWrapper(d, wrap) with LoggingFileWrapper with VirtualDirectoryFlavour {
								override def output = d.output
							}
						case o => new FileWrapper(o, wrap) with LoggingFileWrapper
					}
				}

				trait LoggingFileWrapper extends GenericFileWrapperTrait {

					abstract override def output = {
						writtenClasses += this
						super.output
					}
				}
				val settings = new Settings
				settings.outputDirs setSingleOutput wrap(outputDirectory)
				settings
			}
		new TrackingCompiler(bundleContext,
			 settings,
			 new ConsoleReporter(settings, null, out) {
				override def printMessage(msg: String) {
					out write msg
					out.flush()
				}
			}, outputDirectory, writtenClasses)
	}
}


