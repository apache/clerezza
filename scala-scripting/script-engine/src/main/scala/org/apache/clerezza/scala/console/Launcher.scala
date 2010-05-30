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
package org.apache.clerezza.scala.console;



import org.apache.felix.scr.annotations.Component;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.Bundle
import java.io.{File, PrintWriter}
import java.util.{ArrayList, Arrays};
import scala.tools.nsc._;
import scala.tools.nsc.interpreter._;
import scala.tools.nsc.io.{AbstractFile, PlainFile}
import scala.tools.nsc.util._
import scala.tools.nsc.symtab.SymbolLoaders
import java.net._
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.reporters.Reporter
import scala.tools.util.PathResolver
import scala.tools.nsc.util.{ClassPath, JavaClassPath}


class Launcher() {
	
	protected val PATH_SEPARATOR = System.getProperty("path.separator")

	def activate(componentContext: ComponentContext) {
		System.out.println("activating");
		def jarPathOfClass(className: String) = {
			Class.forName(className).getProtectionDomain.getCodeSource.getLocation
		}
		//val bundle = componentContext.getBundleContext.getBundle
		val bundles = componentContext.getBundleContext.getBundles
		val libPath = jarPathOfClass("scala.ScalaObject")
		//this works: val stream = new URL(libPath.toString).openStream()
		val compilerPath = jarPathOfClass("scala.tools.nsc.Interpreter")
		println("jar: "+libPath)
		val settings = new Settings();/*createSettings(
			bundles,
			componentContext)*/
		val origBootclasspath = settings.bootclasspath.value
		val pathList = List(compilerPath,
							libPath)
		//settings.bootclasspath.value = (origBootclasspath :: pathList).mkString(java.io.File.separator)
		
		val parentClassLoader: ClassLoader = this.getClass.getClassLoader()
		for (c <- classOf[Interpreter].getConstructors) {
			println(c);
			
		}

		val interpreter =  new Interpreter(settings, new PrintWriter(System.out)) { // new Interpreter(settings){
			/*override object reporter extends ConsoleReporter(settings, null, new PrintWriter(System.out)) {
				override def printMessage(msg: String) {
				  println(msg)
				}
			  }*/

			override lazy val compilerClasspath: List[URL] = {
				println("requested path list")
				pathList
			}
			override def classLoader: AbstractFileClassLoader = {
				println("requested classLoader")
				new AbstractFileClassLoader(virtualDirectory, parentClassLoader) {
					override def tryToInitializeClass[T <: AnyRef](path: String): Option[Class[T]] = {
						println("initializing "+path)
						super.tryToInitializeClass(path)
					}
				}
		  }
			override protected def newCompiler(settings: Settings, reporter: Reporter) = {
				println("requested compiler for "+settings)
				reporter.info(scala.tools.nsc.util.NoPosition /* {
					override def source = null}*/, "new Compiler", true)
				//super.newCompiler(settings, reporter)
				settings.outputDirs setSingleOutput virtualDirectory
				new Global(settings, reporter) {
					println("constructing compiler")

					private lazy val _classPath: ClassPath[AbstractFile] = {
						//println("getting ClassPath!")
						//throw new RuntimeException
						//super.classPath
						//of classpath.packages is a seq of ClassPath[AbstractFile]
						val classPathOrig: ClassPath[AbstractFile]  = new PathResolver(settings).result
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

					   val classPath = new MergedClassPath[AbstractFile](classPathOrig :: classPaths,
									classPathOrig.context)
						classPath
					}
					override lazy val classPath: ClassPath[_] = {
						_classPath
					}

					override def rootLoader: LazyType = {
						println("getting rootLoader:"+platform.rootLoader+"("+platform.rootLoader.getClass+")")
						//platform.rootLoader
						//new PathResolver(settings).result is a ClassPath[AbstractFile]
						//of classpath.packages is a seq of ClassPath[AbstractFile]
						/*val classPathOrig: ClassPath[AbstractFile]  = new PathResolver(settings).result
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

					   val classPath = new MergedClassPath[AbstractFile](classPathOrig :: classPaths,
									classPathOrig.context)*/
						//new DirectoryClassPath(BundleFS.create(bundle), classPathOrig.context)
						//class JavaClassPath(containers: List[ClassPath[AbstractFile]], context: JavaContext)
						class MyPackageLoader(loaderClassPath: ClassPath[AbstractFile]) 
						extends loaders.JavaPackageLoader(loaderClassPath) /*{
							override protected def newClassLoader(bin: AbstractFile) = {
								println("getting newClassLoader "+bin+"------")
								super.newClassLoader(bin)
							}

							override protected def newPackageLoader(pkg: ClassPath[AbstractFile]) = {
								println("getting newPackageLoader "+pkg+"------*")
								super.newPackageLoader(pkg)
								//throw new RuntimeException
								new MyPackageLoader(pkg)
							}
						}*/
						val result = new MyPackageLoader(_classPath)
						println("result: "+result)
						result
					}
				}
			}
		}
		
		for (i <- 1 to 1000) {
			val script = """
			println("hello"""+Math.random+"""");
			"good bye"
			"""
			println(new java.util.Date()+"evaluating "+script)
			try {
				println("evaluated to "+interpreter.eval[Unit](script))
			} catch {
				case e:Exception => e.printStackTrace()
			}
		}

		
		//scala.tools.nsc.MainGenericRunner.main(new String[0]);
		//System.out.println("activated");
	}
	private def createSettings(bundles : Array[Bundle], componentContext : ComponentContext) : Settings = {
		val settings = new Settings()
		val bootUrls = getBootUrls(bundles(0), componentContext)
		val bootPath = new StringBuilder()//settings.classpath.v());
		for (bootUrl <- bootUrls) {
			// bootUrls are sometimes null, at least when running integration
			// tests with cargo-maven2-plugin
			if(bootUrl != null) {
				bootPath.append(PATH_SEPARATOR).append(bootUrl.getPath())
			}
		}
		settings.classpath.value = bootPath.toString()
		val dataFile = new File(componentContext.getBundleContext.getDataFile(""),"sclalaclasses")
		dataFile.mkdirs();
		settings.outdir.value = dataFile.getAbsolutePath
		return settings;
	}
	private def getBootUrls(bundle : Bundle, componentContext : ComponentContext) : Array[URL] = {
		val urls = new ArrayList[URL]()
		val dataFile = componentContext.getBundleContext.getDataFile("")
		var classLoader = bundle.getClass().getClassLoader()
		while (classLoader != null) {
			if (classLoader.isInstanceOf[URLClassLoader]) {
				//urls.add((componentContext.getBundleContext.getDataFile("")).toURL())
				urls.addAll(Arrays.asList((classLoader.asInstanceOf[URLClassLoader]).getURLs():_*))
			}
			classLoader = classLoader.getParent()
		}
		return urls.toArray(new Array[URL](urls.size))
	}
}
