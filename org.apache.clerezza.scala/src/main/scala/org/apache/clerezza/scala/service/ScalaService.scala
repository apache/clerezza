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
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.{Arrays, HashMap};
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.{File, StringWriter};
import java.security.{AccessController, PrivilegedExceptionAction, PrivilegedActionException}

import org.apache.clerezza.scala.interpreter._
import org.slf4j.LoggerFactory;

import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.io.{AbstractFile, PlainFile};
import org.osgi.service.component.ComponentContext;
import scala.collection._;
import org.osgi.framework.Bundle
import scala.tools.nsc.{Settings, Global};
import scala.tools.nsc.io.{AbstractFile, PlainFile};
import javax.script.{ScriptEngineFactory, ScriptEngine, AbstractScriptEngine,
					 Bindings, SimpleBindings}


package org.apache.clerezza.scala.service {

	/**
	 * The <codee>ScalaService</code> interprets a ScalaScript.
	 *
	 * @author rbn, mkn, pmg
	 */
	class ScalaService extends AbstractScriptEngine with ScriptEngineFactory {
		protected val PATH_SEPARATOR = System.getProperty("path.separator")
		protected var interpreter : ScalaInterpreter = null;
		/**
		 * Assigns a instance of the <code>ComponentContext</code>.
		 *
		 * @param componentContext
		 * 			The componentContext
		 */
		def activate(componentContext : ComponentContext) {
			val settings = createSettings(
				componentContext.getBundleContext.getBundles,
				componentContext)
			interpreter = new ScalaInterpreter(settings,
												   new ExceptionReporter(),
												   createClassPath(componentContext.getBundleContext.getBundles))

		}
		//methods from ScriptEngineFactory
		override def getEngineName() = "Scala Scripting Engine for OSGi"
		override def getEngineVersion() = "0.1/scala 2.7.5"
		override def getExtensions() = java.util.Collections.singletonList("scala")
		override def getMimeTypes() = java.util.Collections.singletonList("application/x-scala")
		override def getNames() = java.util.Collections.singletonList("scala")
		override def getLanguageName() = "Scala"
		override def getLanguageVersion ="2.7.5"
		override def getParameter(key : String) = {
			key match {
				case ScriptEngine.ENGINE => getEngineName
				case ScriptEngine.ENGINE_VERSION => getEngineVersion
				case ScriptEngine.NAME => getNames.get(0)
				case ScriptEngine.LANGUAGE => getLanguageName
				case ScriptEngine.LANGUAGE_VERSION => getLanguageVersion
				case _ => null
			}
		}
		override def getMethodCallSyntax(obj : String,
										 m : String,
										 args : String*) = {
			obj+"."+m+"("+args.mkString(",")+")"
		}
		override def getOutputStatement(toDisplay : String) = "println(\""+toDisplay+"\")"
		override def getProgram(statements : String*) = statements.mkString("\n")
		override def getScriptEngine : ScriptEngine = this

		//methods from AbstractScriptEngine
		override def eval(script : Reader, context : ScriptContext) : Object = {
			val scriptStringWriter = new StringWriter()
			var ch = script.read
			while (ch != -1) {
				scriptStringWriter.write(ch)
				ch = script.read
			}
			eval(scriptStringWriter.toString, context)
		}
		override def eval(script : String, context : ScriptContext) : Object = {
			val jTypeMap : java.util.Map[String, java.lang.reflect.Type] =
			new java.util.HashMap[String, java.lang.reflect.Type]()
			val valueMap = new java.util.HashMap[String, Any]()
			import _root_.scala.collection.jcl.Conversions._
			for (scope <- context.getScopes;
				 if (context.getBindings(scope.intValue) != null);
				 entry <- context.getBindings(scope.intValue)) {
				jTypeMap.put(entry._1, getAccessibleClass(entry._2.getClass))
				valueMap.put(entry._1, entry._2)
			}
			interpretScalaScript(script, jTypeMap).execute(valueMap).asInstanceOf[Object]
		}
		override def getFactory() = this
		override def createBindings() : Bindings = new SimpleBindings

		/**
		 * returns an accessible class or interface that is implemented by class,
		 * is doesn't look for superinterfaces of implement interfaces
		 */
		private def getAccessibleClass(clazz : Class[_]) : Class[_] = {
			if(isAccessible(clazz)) {
				return clazz
			} else {
				val foo : Class[_] = clazz.getInterfaces()(0)
				for (implementedInterface <- clazz.getInterfaces()) {
					if (isAccessible(implementedInterface)) return implementedInterface
				}
			}
			return getAccessibleSuperClass(clazz)
		}

		private def getAccessibleSuperClass(clazz : Class[_]) : Class[_] = {
			val superClass = clazz.getSuperclass
			if (superClass == null) {
				throw new RuntimeException("No upper class to be checked for accessibility for "+clazz)
			}
			if (isAccessible(superClass)) {
				superClass
			} else {
				getAccessibleSuperClass(superClass)
			}
		}

		private def isAccessible(clazz : Class[_])  = {
			try {
				Class.forName(clazz.getName)
				true
			} catch {
				case e: Exception => false
			}
		}
		/**
		 * Creates and return a <code>CompiledScript</code> of the scala script.
		 *
		 * @param script
		 * 			the scala script
		 * @param jTypeMap
		 * 			the map with the parameter types
		 * @return CompiledScript
		 *			the compiled scala script
		 */
		@throws(classOf[ScriptException])
		def interpretScalaScript(script : String , jTypeMap : java.util.Map[String, java.lang.reflect.Type]) : CompiledScript = {
			val jHashMap = new java.util.HashMap[String, java.lang.reflect.Type]()
			jHashMap.putAll(jTypeMap)
			val map : immutable.Map[String, java.lang.reflect.Type] =
			new immutable.HashMap() ++ new jcl.HashMap[String, java.lang.reflect.Type](jHashMap)
			try {
				val compileScript = AccessController.doPrivileged(new PrivilegedExceptionAction[CompiledScript] {
						override def run(): CompiledScript = {
							new CompiledScript(script, map, interpreter)
						}
				 })
				return compileScript
			} catch {
				case e: PrivilegedActionException =>
					var cause = e.getCause
					if (cause.isInstanceOf[ScriptException]) {
						throw cause.asInstanceOf[ScriptException]
					} else {
						throw cause.asInstanceOf[RuntimeException]
					}
			}
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

		private def createClassPath(bundles : Array[Bundle]) : Array[AbstractFile] = {
			val bundleFs : Array[AbstractFile] = for (bundle <- bundles;
													  val url = bundle.getResource("/");
													  if url != null) yield {
				if ("file".equals(url.getProtocol())) {
					new PlainFile(new File(url.toURI()))
				}
				else {
					BundleFS.create(bundle);
				}
			};
			bundleFs
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
}
