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


class InterpreterFactory() {
	
	protected var bundles: Array[Bundle] = null

	def activate(componentContext: ComponentContext)= {
		bundles = componentContext.getBundleContext.getBundles
		//TODO register listener for bunle-changed events
	}

	def deactivate(componentContext: ComponentContext) = {
		bundles = null
	}

	def createInterpreter(out: PrintWriter) : Interpreter =
		new BundleContextScalaInterpreter(bundles, out)

	
}
