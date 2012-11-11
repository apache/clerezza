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




import org.osgi.service.component.ComponentContext
import java.io.InputStream
import java.io.OutputStream
import java.security.AccessController
import java.security.PrivilegedAction
import org.apache.clerezza.scala.scripting.InterpreterFactory
import scala.tools.jline.Terminal


class ShellFactory()  {



	private var interpreterFactory: InterpreterFactory = null
	private var componentContext: ComponentContext = null
	private var commands = Set[ShellCommand]()
	private var customizers = Set[ShellCustomizer]()
	
	def activate(componentContext: ComponentContext)= {
		this.componentContext = componentContext
	}

	def deactivate(componentContext: ComponentContext) = {
		this.componentContext = componentContext
	}

	/* 
	 * Using overloading instead of default, as default is not supported when calling from java
	 */
	def createShell(pIn: InputStream, pOut: OutputStream): Shell = {
	  createShell(pIn, pOut, None)
	}
	def createShell(pIn: InputStream, pOut: OutputStream, terminalOption: Option[Terminal]): Shell = {
    var security: SecurityManager = System.getSecurityManager
    if (security != null) {
      AccessController.checkPermission(new ShellPermission())
    }
		AccessController.doPrivileged(new PrivilegedAction[Shell] {
				override def run() = {
					val shell = new Shell(interpreterFactory, pIn, pOut, commands, terminalOption)
					//shell.bind("bundleContext", classOf[BundleContext].getName, componentContext.getBundleContext)
					//shell.bind("componentContext", classOf[ComponentContext].getName, componentContext)
					shell.bind("osgiDsl", classOf[OsgiDsl].getName, new OsgiDsl(componentContext, pOut))
					shell.addImport("org.apache.clerezza.{scala => zzscala, _ }")
					shell.addImport("osgiDsl._")
					val environment = new Shell.Environment {
						val componentContext: ComponentContext = ShellFactory.this.componentContext
						val in: InputStream = pIn;
						val out: OutputStream = pOut;
					}
					for (c <- customizers) {
						for(b <- c.bindings(environment)) {
							shell.bind(b._1, b._2, b._3)
						}
						for(i <- c.imports) {
							shell.addImport(i)
						}
					}
					shell
				}
			})
	}

	def bindInterpreterFactory(f: InterpreterFactory) = {
		interpreterFactory = f
	}

	def unbindInterpreterFactory(f: InterpreterFactory) = {
		interpreterFactory = null
	}

	def bindCommand(c: ShellCommand) = {
		commands += c
	}

	def unbindCommand(c: ShellCommand) = {
		commands -= c
	}

	def bindCustomizer(c: ShellCustomizer) = {
		customizers += c
	}

	def unbindCustomizer(c: ShellCustomizer) = {
		customizers -= c
	}
}