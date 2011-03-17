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
import java.io.{File, PrintWriter, Reader, StringWriter, BufferedReader, InputStreamReader, InputStream, Writer, OutputStream}
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
import scala.collection.immutable
import scala.tools.nsc._;
import scala.tools.nsc.interpreter._;
import scala.tools.nsc.io.{AbstractFile, PlainFile, VirtualDirectory}
import scala.tools.nsc.util._
import scala.tools.nsc.symtab.SymbolLoaders
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.reporters.Reporter
import scala.tools.util.PathResolver
import scala.tools.nsc.util.{ClassPath, JavaClassPath}
import scala.actors.Actor
import scala.actors.Actor._
import org.apache.clerezza.scala.scripting._
import java.io.File
import jline.{ ConsoleReader, ArgumentCompletor, History => JHistory }

class Shell(factory: InterpreterFactory, val inStream: InputStream, out: OutputStream, shellCommands: immutable.Set[ShellCommand])  {


	private var bundleContext: BundleContext = null

	private var bindings = Set[(String, String, Any)]()
	private var imports = Set[String]()
	private var terminationListeners = Set[Shell.TerminationListener]();


	val interpreterLoop = new InterpreterLoop(new BufferedReader(new InputStreamReader(inStream)), new PrintWriter(out, true)) {
		override def createInterpreter() {
			interpreter = factory.createInterpreter(out)
			interpreter.beQuietDuring {
				for (binding <- bindings) {
					interpreter.bind(binding._1, binding._2, binding._3)
				}
				for (v <- imports) {
					interpreter.interpret("import "+v)
				}
			}
		}

		override val prompt = "zz>"

		override val standardCommands: List[Command] = {
			import CommandImplicits._
			(for (shellCommand <- shellCommands) yield {
					LineArg(shellCommand.command, shellCommand.description, (line: String)=> {
							val (continue, linesToRecord) = shellCommand.execute(line, Shell.this.out)
							Result(continue, linesToRecord)
						})
				}).toList :::
			List(
				NoArgs("help", "print this help message", printHelp),
				VarArgs("history", "show the history (optional arg: lines to show)", printHistory),
				LineArg("h?", "search the history", searchHistory),
				OneArg("load", "load and interpret a Scala file", load),
				NoArgs("power", "enable power user mode", power),
				NoArgs("quit", "terminate the console shell (use shutdown to shut down clerezza)", () => Result(false, None)),
				NoArgs("replay", "reset execution and replay all previous commands", replay),
				LineArg("sh", "fork a shell and run a command", runShellCmd),
				NoArgs("silent", "disable/enable automatic printing of results", verbosity)
			)
		}

		override def printHelp() = {
			out println "This is a scala based console, it supports any Scala expression, as well as the command described below."
			out println "To access an OSGi service use $[interface]."
			out println ""
			out println "Initially the following variables are bound:"
			for ((name, boundType, value) <- bindings) {
				out println (name+": "+boundType+" = "+value)
			}
			out println ""
			out println "This are the initial imports: "
			for (v <- imports) {
				out println ("import "+v)
			}
			out println ""
			super.printHelp()
		}


		override def main(settings: Settings) {
			this.settings = settings
			createInterpreter()

			// sets in to some kind of reader depending on environmental cues
			in = new InteractiveReader() {

				override lazy val history = Some(History(consoleReader))
				override lazy val completion = Option(interpreter) map (x => new Completion(x))

				val consoleReader = {
					val r = new jline.ConsoleReader(inStream, out)
					r setHistory (History().jhistory)
					r setBellEnabled false
					completion foreach { c =>
						r addCompletor c.jline
						r setAutoprintThreshhold 250
					}

					r
				}

				def readOneLine(prompt: String) = consoleReader readLine prompt
				val interactive = true
			}

			loadFiles(settings)
			try {
				// it is broken on startup; go ahead and exit
				if (interpreter.reporter.hasErrors) return

				printWelcome()

				// this is about the illusion of snappiness.  We call initialize()
				// which spins off a separate thread, then print the prompt and try
				// our best to look ready.  Ideally the user will spend a
				// couple seconds saying "wow, it starts so fast!" and by the time
				// they type a command the compiler is ready to roll.
				interpreter.initialize()
				repl()
			}
			finally closeInterpreter()
		}

		override def printWelcome() {
			import Properties._
			val welcomeMsg =
				"""|Welcome to the Apache Clerezza Console
				|Console is based on Scala %s (%s, Java %s).
				|Type in expressions to have them evaluated.
				|Hint: To execute a Felix-Shell command prepend ":f "
				|Type :help for more information.""" .
			stripMargin.format(versionString, javaVmName, javaVersion)

			plushln(welcomeMsg)
		}
	}
	val console: Actor = new DaemonActor {
		def act() {
			try {
				interpreterLoop.main(Array[String]())
			} finally {
				for (l <- terminationListeners) {
					l.terminated
				}
				println("console terminated")
			}
		}
	}

	def start() {
		console.start
	}

	def stop() {
		interpreterLoop.command(":q")
		interpreterLoop.closeInterpreter()
	}

	def bind(name: String, boundType: String, value: Any) {
		bindings += ((name, boundType, value))
	}

	def addImport(importValue: String) {
		imports += importValue
	}

	def addTerminationListener(l: Shell.TerminationListener) {
		terminationListeners += l
	}

	def removeTerminationListener(l: Shell.TerminationListener) {
		terminationListeners -= l
	}

}
object Shell {
	trait TerminationListener {
		def terminated: Unit
	}
	
	trait Environment {
		val componentContext: ComponentContext;
		val in: InputStream;
		val out: OutputStream;
	}
}