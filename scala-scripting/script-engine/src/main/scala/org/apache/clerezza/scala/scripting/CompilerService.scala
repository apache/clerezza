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

	def createCompiler(out: PrintWriter, outputSirectory: AbstractFile) : Global = {
		val settings = new Settings
		settings.outputDirs setSingleOutput outputSirectory
		AccessController.doPrivileged(new PrivilegedAction[BundleContextScalaCompiler]() {
			override def run() =  {
				new BundleContextScalaCompiler(bundleContext, settings,
					new ConsoleReporter(settings, null, out) {
					
						override def printMessage(msg: String) {
							out write msg
							out.flush()
						}
					}) 
			}
		})
	}

	def compile(sources: List[Array[Char]]): List[Class[_]] = {
		val virtualDirectory = new VirtualDirectory("(memory)", None)
		compile(sources, virtualDirectory)
	}

	def compile(sources: List[Array[Char]], rawOutputDirectory: AbstractFile): List[Class[_]] = {

		var writtenClasses: List[AbstractFile] = List[AbstractFile]()

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
				writtenClasses ::= this
				super.output
			}
		}
		val outputDirectory = wrap(rawOutputDirectory)
		val out = new ByteArrayOutputStream
		val printWriter = new PrintWriter(out)
		val compiler = createCompiler(printWriter, outputDirectory)
		val sourceFiles: List[SourceFile] = for(chars <- sources) yield new BatchSourceFile("<script>", chars)
		(new compiler.Run).compileSources(sourceFiles)
		printWriter.flush
		if (compiler.reporter.hasErrors) {
			compiler.reporter.reset
			throw new RuntimeException("compile errors: "+new String(out.toByteArray));
		} 
		val classLoader = new AbstractFileClassLoader(rawOutputDirectory, this.getClass.getClassLoader())
		val result: List[Class[_]] = for (classFile <- writtenClasses;
										if (!classFile.name.contains('$'))) yield {
											val path = classFile.path
											val relevantPath = path.substring(path.indexOf('/')+1,path.lastIndexOf('.'))
											val fqn = relevantPath.replace("/",".")
											classLoader.loadClass(fqn)
										}
		return result
	}

	
}
