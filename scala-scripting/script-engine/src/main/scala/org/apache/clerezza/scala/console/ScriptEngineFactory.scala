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
import java.io.{File, PrintWriter, Reader, StringWriter}
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
import javax.script.ScriptContext
import javax.script.{ScriptEngineFactory => JavaxEngineFactory, ScriptEngine, AbstractScriptEngine, Bindings, SimpleBindings}


class ScriptEngineFactory() extends  JavaxEngineFactory {

	var interpreter : Interpreter = null;


	//methods from ScriptEngineFactory
	override def getEngineName() = "Scala Scripting Engine for OSGi"
	override def getEngineVersion() = "0.2/scala 2.8.0.RC2"
	override def getExtensions() = java.util.Collections.singletonList("scala")
	override def getMimeTypes() = java.util.Collections.singletonList("application/x-scala")
	override def getNames() = java.util.Collections.singletonList("scala")
	override def getLanguageName() = "Scala"
	override def getLanguageVersion ="2.8.0.RC2"
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
	override def getScriptEngine : ScriptEngine = MyScriptEngine

	def activate(componentContext: ComponentContext)= {

	}

	def deactivate(componentContext: ComponentContext) = {

	}

	def bindInterpreterFactory(f: InterpreterFactory) = {
		interpreter = f.createInterpreter(new PrintWriter(System.out))
	}

	def unbindInterpreterFactory(f: InterpreterFactory) = {
		interpreter = null
	}
	/** Inner object as it accesse interpreter
	 */
	object MyScriptEngine extends AbstractScriptEngine() {
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
			//not yet threadsafe
			val jTypeMap : java.util.Map[String, java.lang.reflect.Type] =
				new java.util.HashMap[String, java.lang.reflect.Type]()
			val valueMap = new java.util.HashMap[String, Any]()
			import _root_.scala.collection.JavaConversions._
			for (scope <- context.getScopes;
					if (context.getBindings(scope.intValue) != null);
					entry <- context.getBindings(scope.intValue)) {
				interpreter.bind(entry._1,
								 getAccessibleClass(entry._2.getClass).getName, entry._2)
			}

			interpreter.eval[Object](script) match   {
				case Some(x) => x
				case None => null
			}
		}
		override def getFactory() = ScriptEngineFactory.this
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
	}
}


