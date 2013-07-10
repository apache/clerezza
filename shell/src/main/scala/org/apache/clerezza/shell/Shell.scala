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

import org.apache.felix.scr.annotations.Component
import org.osgi.framework.BundleContext
import org.osgi.framework.BundleEvent
import org.osgi.framework.BundleListener
import org.osgi.service.component.ComponentContext
import org.osgi.framework.Bundle
import java.io.{ File, PrintWriter, Reader, StringWriter, BufferedReader, InputStreamReader, InputStream, Writer, OutputStream }
import java.lang.reflect.InvocationTargetException
import java.net._
import java.security.PrivilegedActionException
import java.security.AccessController
import java.security.PrivilegedAction
import javax.script.ScriptContext
import javax.script.{
  ScriptEngineFactory => JavaxEngineFactory,
  Compilable,
  CompiledScript,
  ScriptEngine,
  AbstractScriptEngine,
  Bindings,
  SimpleBindings,
  ScriptException
}
import java.util.{ ArrayList, Arrays }
import scala.actors.DaemonActor
import scala.collection.immutable
import scala.tools.nsc._
import scala.tools.nsc.interpreter._
import scala.tools.nsc.io.{ AbstractFile, PlainFile, VirtualDirectory }
import scala.tools.nsc.util._
import scala.tools.nsc.symtab.SymbolLoaders
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.reporters.Reporter
import scala.tools.util.PathResolver
import scala.tools.nsc.util.{ ClassPath, JavaClassPath }
import scala.actors.Actor
import scala.actors.Actor._
import org.apache.clerezza.scala.scripting._
import java.io.File
import org.slf4j.scala.Logging
import scala.tools.nsc.interpreter.JLineReader
import scala.tools.jline.Terminal
import scala.tools.jline.console.completer.CompletionHandler
import scala.tools.jline.console.ConsoleReader
import scala.tools.jline.console.completer.CandidateListCompletionHandler
import java.lang.CharSequence
import scala.tools.jline.TerminalFactory
import scala.tools.jline.UnixTerminal
import scala.tools.jline.console.history.History

class Shell(factory: InterpreterFactory, val inStream: InputStream,
  outStream: OutputStream, shellCommands: immutable.Set[ShellCommand], terminalOption: Option[Terminal] = None) extends Logging {

  private var bundleContext: BundleContext = null

  private var bindings = Set[(String, String, Any)]()
  private var imports = Set[String]()
  private var terminationListeners = Set[Shell.TerminationListener]();

  val terminal = terminalOption match {
    case Some(x) => x
    case None => TerminalFactory.create
  }

  val interpreterLoop = new ILoop(new BufferedReader(new InputStreamReader(inStream)), new PrintWriter(outStream, true)) {
    override def createInterpreter() {
      intp = factory.createInterpreter(out)
      intp.beQuietDuring {
        for (binding <- bindings) {
          intp.bind(binding._1, binding._2, binding._3)
        }
        for (v <- imports) {
          intp.interpret("import " + v)
        }
      }
    }

    override val prompt = "zz>"
    override def isAsync = false

    override lazy val standardCommands: List[LoopCommand] = {
      import LoopCommand._
      (for (shellCommand <- shellCommands) yield {
        new LineCmd(shellCommand.command, "<unknown>", shellCommand.description, (line: String) => {
          val (continue, linesToRecord) = shellCommand.execute(line, Shell.this.outStream)
          Result(continue, linesToRecord)
        })
      }).toList ::: List(
        cmd("help", "[command]", "print this summary or command-specific help", helpCommand),
        historyCommand,
        cmd("h?", "<string>", "search the history", searchHistory),
        cmd("load", "<path>", "load and interpret a Scala file", loadCommand),
        nullary("paste", "enter paste mode: all input up to ctrl-D compiled together", pasteCommand),
        nullary("power", "enable power user mode", powerCmd),
        nullary("quit", "terminate the console shell (use shutdown to shut down clerezza)", () => Result(false, None)),
        nullary("replay", "reset execution and replay all previous commands", replay),
        shCommand,
        nullary("silent", "disable/enable automatic printing of results", verbosity))
    }

    /** print a friendly help message */
  override def helpCommand(line: String): Result = {
    if (line == "") printAdditinalHelp();
      
    super.helpCommand(line);
  }

    def printAdditinalHelp() = {
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
		}

    override def process(settings: Settings): Boolean = {
      this.settings = settings
      createInterpreter()

      // sets in to some kind of reader depending on environmental cues
     //ignore settings.noCompletion.value)
      {
    	  val myIn = new StreamJLineReader(new JLineCompletion(intp), inStream, outStream, terminal) 
    	  in =  myIn
    	  //are we postinit already?
    	  addThunk(myIn.consoleReader.postInit)
      }
      loadFiles(settings)
    // it is broken on startup; go ahead and exit
    if (intp.reporter.hasErrors)
      return false

    // This is about the illusion of snappiness.  We call initialize()
    // which spins off a separate thread, then print the prompt and try
    // our best to look ready.  The interlocking lazy vals tend to
    // inter-deadlock, so we break the cycle with a single asynchronous
    // message to an actor.
    if (isAsync) {
      intp initialize initializedCallback()
      createAsyncListener() // listens for signal to run postInitialization
    }
    else {
      intp.initializeSynchronous()
      postInitialization()
    }
    printWelcome()

    try loop()
    catch AbstractOrMissingHandler()
    finally closeInterpreter()

    true
    }

    override def printWelcome() {
      import Properties._
      val welcomeMsg =
        """|Welcome to the Apache Clerezza Console
				|Console is based on Scala %s (%s, Java %s).
				|Type in expressions to have them evaluated.
				|Type :help for more information.""".
          stripMargin.format(versionString, javaVmName, javaVersion)

      echo(welcomeMsg)
    }
  }
  val console: Actor = new DaemonActor {
    def act() {
      try {
        interpreterLoop.process(Array[String]())
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