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


class InterpreterService() {
	
	protected val PATH_SEPARATOR = System.getProperty("path.separator")

	def activate(componentContext: ComponentContext) {
		System.out.println("activating");

		//val bundle = componentContext.getBundleContext.getBundle
		val bundles = componentContext.getBundleContext.getBundles
		val settings = new Settings();/*createSettings(
			bundles,
			componentContext)*/
		val origBootclasspath = settings.bootclasspath.value
		//settings.bootclasspath.value = (origBootclasspath :: pathList).mkString(java.io.File.separator)
		val interpreter =  new BundleContextScalaInterpreter(bundles)
		println(new java.util.Date)
		for (i <- 1 to 100) {
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
		println(new java.util.Date)

		
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
