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
package org.apache.clerezza.shell;



import org.apache.felix.scr.annotations.Component;
import org.osgi.framework.BundleContext
import org.osgi.framework.BundleEvent
import org.osgi.framework.BundleListener
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.Bundle
import java.io.{File, PrintWriter, Reader, StringWriter, FileDescriptor, OutputStreamWriter}
import java.lang.reflect.InvocationTargetException
import java.net._
import java.security.PrivilegedActionException
import java.security.AccessController
import java.security.PrivilegedAction
import java.util.{ArrayList, Arrays};
import javax.script.ScriptContext
import javax.script.{ScriptEngineFactory => JavaxEngineFactory, Compilable, 
					 CompiledScript, ScriptEngine, AbstractScriptEngine, Bindings,
					 SimpleBindings, ScriptException}
//import scala.collection.immutable.Map
import scala.actors.DaemonActor
import scala.tools.nsc._;
import scala.tools.nsc.interpreter._;
import scala.tools.nsc.io.{AbstractFile, PlainFile, VirtualDirectory}
import scala.tools.nsc.util._
import scala.tools.nsc.symtab.SymbolLoaders
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.reporters.Reporter
import scala.tools.util.PathResolver
import scala.tools.nsc.util.{ClassPath, JavaClassPath}
import scala.actors.Actor
import scala.actors.Actor._
import org.apache.clerezza.scala.scripting._

class ShellFactory()  {



	var factory: InterpreterFactory = null
	
	
	def activate(componentContext: ComponentContext)= {
		//bundleContext = componentContext.getBundleContext
	}

	def deactivate(componentContext: ComponentContext) = {
		//bundleContext = componentContext.getBundleContext
	}

	def createShell() = {
		new Shell(factory, System.in, new OutputStreamWriter(System.out))
	}

	def bindInterpreterFactory(f: InterpreterFactory) = {
		factory = f
	}

	def unbindInterpreterFactory(f: InterpreterFactory) = {
		factory = null
	}
}