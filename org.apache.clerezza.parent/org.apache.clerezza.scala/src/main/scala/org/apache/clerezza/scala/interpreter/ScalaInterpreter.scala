/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This class was taken from Sling Scala module at
 * http://code.google.com/p/sling-scala/
 * Changes: Unused functions and variables were declared as protected 
 */
import java.net.URLClassLoader
import java.io.{File, InputStream, OutputStream}
import java.lang.reflect.Type
import javax.script.ScriptException
import java.security.{AccessController, PrivilegedAction}
import java.lang.reflect.Method
import scala.collection._
import scala.tools.nsc.{Settings, Global}
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.tools.nsc.io.AbstractFile
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.util.{SourceFile, BatchSourceFile}

import org.apache.clerezza.scala.interpreter.Utils.{option}

package org.apache.clerezza.scala.interpreter {

/**
 * An interpreter for Scala scripts. Interpretation of scripts proceeds in the following steps:
 * <ol>
 * <li>Pre-compilation: The source script is {@link #preProcess} wrapped into an object wrapper which
 *   contains variable definitions of the approproate types for the passed {@link Bindings bindings}.</li>
 * <li>Compilation: The resulting source code is {@link #compile compiled} by the Scala compiler. </li>
 * <li>Execution: The class file is {@link #execute loaded} and its main method called.</li>
 * </ol>
 * @param settings  compiler settings
 * @param reporter  reporter for compilation
 * @param classes  additional classes for the classpath
 * @param outDir  ourput directory for the compiler
 */
class ScalaInterpreter(settings: Settings, reporter: Reporter, classes: Array[AbstractFile],
                       var outDir: AbstractFile) {

  if (outDir == null) {
	  val compiler = new ScalaCompiler(settings, reporter, classes)
	  outDir = compiler.genJVM.outputDir
  }

  /**
   * Same as <code>ScalaInterpreter(settings, reporter, classes, null)</code>.
   * @param settings
   * @param reporter
   * @param classes
   * @return
   */
  def this(settings: Settings, reporter: Reporter, classes: Array[AbstractFile]) =
    this(settings, reporter, classes, null)

  /**
   * Same as <code>ScalaInterpreter(settings, reporter, null, outDir)</code>.
   * @param settings
   * @param reporter
   * @param outDir
   * @return
   */
  def this(settings: Settings, reporter: Reporter, outDir: AbstractFile) =
    this(settings, reporter, null, outDir)

  /**
   * Same as <code>ScalaInterpreter(settings, reporter, null, null)</code>.
   * @param settings
   * @param reporter
   * @return
   */
  def this(settings: Settings, reporter: Reporter) =
    this(settings, reporter, null, null)

  /**
   * Line separater used in the wrapper object
   */
  protected val NL: String = System.getProperty("line.separator");

  /**
   * The parent class loader used for execution
   */
  protected val parentClassLoader: ClassLoader = getClass.getClassLoader



  /**
   * Utility method for parsing a fully quallyfied class name into its packet compounds
   * @param name  Full qualified class name
   * @return  The compounds consisting of the (sub)-packates followed by the class name
   */
  protected def packetize(name: String): List[String] =
    name.split('.').toList

  /**
   * Pre processor for wrapping the script such that it becomes a valid Scala source entity
   * which can be passed the the Scala compiler.
   * @param name  name of the script. Used for generating the class name of the wrapper.
   * @param code  source code of the script
   * @param bindings  bindings to be passed to the script
   * @return  a valid Scala source and the lines of headers that have been added before the actual code
   */
  protected def preProcess(name: String, code: String, bindings: Map[String, Type]): (String, Int) = {
   
    val compounds = packetize(name)

    def packageDeclaration =
      if (compounds.size > 1) compounds.init.mkString("package ", ".", "") + NL
      else "" //to make sure the header always has the same length

    def className = compounds.last

    val header = packageDeclaration +
    "object " + className + " {" + NL +
    "  def main(bindings: Map[String, Any]," + NL +
    "           stdIn: java.io.InputStream," + NL +
    "           stdOut: java.io.OutputStream) : Any = {" + NL +
    "    def run() : Any = {" + NL +
           getScriptVariableInitialization(bindings) + NL ;
	val footer = "" + NL +
    "    }" + NL +
    "    Console.withIn(stdIn) {" + NL +
    "      Console.withOut(stdOut) {" + NL +
    "        val result = run" + NL +
    "        stdOut.flush" + NL + 
	"        result "+ NL +
    "      }" + NL +
    "    }" + NL +
    "  }" + NL +
    "}" + NL
	(header + code + footer, header.split(NL).size)
  }
  
  private def getScriptVariableInitialization(bindings: Map[String, Type]) = {
   (for ((key,value) <- bindings) yield {
     "val " + key + " =  bindings.get(\""+key+"\").get.asInstanceOf["+value.asInstanceOf[Class[AnyRef]].getName+"]"
   }).mkString(";"+NL)
  }
  
    

  /**
   * Compiles a list of source files. No pre-processing takes place.
   * @param sources  source files
   * @return  result of compilation
   */
  protected def compile(sources: List[SourceFile], lineDeduction : Int): Reporter = {
	reporter.reset
	val compiler = new ScalaCompiler(settings,
						new LineDeductingReporter(reporter, lineDeduction), classes)
    val run = new compiler.Run
    if (reporter.hasErrors)
      reporter
    else {
      run.compileSources(sources)
      reporter
    }
  }

  /**
   * Compiles a single source file. No pre-processing takes place.
   * @param name  name of the script
   * @param code  source code
   * @param lineDeduction the number of lines to be deducted in the line numbers given to Reporter
   * @return  result of compilation
   */
  def compile(name: String, code: String, lineDeduction : Int): Reporter = {
    compile(List(new BatchSourceFile(name, code.toCharArray)), lineDeduction)
	}
  /**
   * Pre-processes and compiles a single source file.
   * @param name  name of the script
   * @param code  source code
   * @param bindings  variable bindings to pass to the script
   * @return  result of compilation
   */
	def compile(name: String, code: String, bindings: Map[String, Type]) : Reporter = {
		val preprocessedScript = preProcess(name, code, bindings)
    	compile(name, preprocessedScript._1, preprocessedScript._2)
    }

  /**
   * Compiles a single source file. No pre-processing takes place.
   * @param sources  source file
   * @return  result of compilation
   */
  protected def compile(source: AbstractFile): Reporter = {
    compile(List(new BatchSourceFile(source)), 0)
  }

  /**
   * Pre-processes and compiles a single source file.
   * @param name  name of the script
   * @param source  source file
   * @param bindings  variable bindings to pass to the script
   * @return  result of compilation
   */
  /*protected def compile(name: String, source: AbstractFile, bindings: Map[String, Type]): Reporter = {
    val code = new String(source.toByteArray)
    compile(name, preProcess(name, code, bindings))
  }*/

  /**
   * Looks up the class file for a compiled script
   * @param  name  script name
   * @return  the class file or null if not found
   */
  def getClassFile(name: String): AbstractFile = {
    var file: AbstractFile = outDir
    val pathParts = name.split("[./]").toList
    for (dirPart <- pathParts.init) {
      file = file.lookupName(dirPart, true)
      if (file == null) {
        return null
      }
    }
    file.lookupName(pathParts.last + ".class", false)
  }

  /**
   * Executes a compiled script
   * @param name  name of the script
   * @param bindings  variable bindings to pass to the script
   * @param in  stdIn for the script execution
   * @param out  stdOut for the script execution
   * @return  result of execution
   * @throws  ScriptException when class files cannot be accessed or nor entry point is found
   */
  @throws(classOf[ScriptException])
  protected def execute(name: String, valueMap: Map[String, Any], in: Option[InputStream], out: Option[OutputStream]): Any = {
    try {
       val initMethod = AccessController.doPrivileged(new PrivilegedAction[Method] {
	    override def run(): Method = {
		  val classLoader = new AbstractFileClassLoader(outDir, parentClassLoader) {
			  def superImpl(name: String): Class[_] = super.findClass(name)
			  override def findClass(name : String) : Class[_] = {
				  AccessController.doPrivileged(new PrivilegedAction[Class[_]] {
					override def run(): Class[_] = {
					  superImpl(name)
					}
				  })
			  }
		  }
		  val script = Class.forName(name, true, classLoader)
		  (script
				.getDeclaredMethods
				.toList
				.find(method => method.getName == "main")
				.get)
		}
      })
	  initMethod.invoke(null, Array(valueMap, in.getOrElse(java.lang.System.in),
                                              out.getOrElse(java.lang.System.out)): _*)
    }
    catch {
      case e: java.lang.reflect.InvocationTargetException =>
        throw new ScriptException(e.getTargetException.asInstanceOf[Exception])
      case e: Exception =>
        throw new ScriptException(e)
    }
  }

  /**
   * Same as <code>execute(name, bindings, None, None)</code>.
   * @param name  name of the script
   * @param bindings  variable bindings to pass to the script
   * @return  result of execution
   * @throws  ScriptException when class files cannot be accessed or nor entry point is found
   */
  @throws(classOf[ScriptException])
  def execute(name: String, valueMap: Map[String, Any]): Any =
    execute(name, valueMap, None, None)

  /**
   * Same as <code>execute(name, bindings, Some(in), Some(out))</code>.
   * @param name  name of the script
   * @param bindings  variable bindings to pass to the script
   * @param in  stdIn for the script execution
   * @param out  stdOut for the script execution
   * @return  result of execution
   * @throws  ScriptException when class files cannot be accessed or nor entry point is found
   */
  @throws(classOf[ScriptException])
  def execute(name: String, valueMap: Map[String, Any], in: InputStream, out: OutputStream): Any =
    execute(name, valueMap, option(in), option(out))

}


}
