package org.apache.clerezza.shell

import scala.tools.jline.Terminal
import scala.tools.jline.TerminalFactory
import scala.tools.jline.console.completer.CandidateListCompletionHandler
import scala.tools.jline.console.completer.Completer
import scala.tools.jline.console.completer.CompletionHandler
import scala.tools.jline.console.history.History
import scala.tools.jline.console.history.MemoryHistory
import scala.tools.jline.internal.Configuration
import scala.tools.jline.internal.Log
import org.fusesource.jansi.AnsiOutputStream
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.event.ActionListener
import scala.tools.jline.console.ConsoleReader
import scala.tools.jline.console.completer._
import scala.tools.nsc.interpreter.session._
import scala.collection.JavaConverters._
import scala.tools.nsc.interpreter.Completion._
import scala.tools.nsc.io.Streamable.slurp
import scala.tools.nsc.interpreter._
import java.io.OutputStream

//a modified version of scala.tools.nsc.interpreter.JLineReader
/**
 *  Reads from the console using JLine.
 */
class StreamJLineReader(_completion: => Completion, in: InputStream, out: OutputStream, terminal: Terminal) extends InteractiveReader {
  val interactive = false
  
  class StreamJLineConsoleReader extends ConsoleReader(in, out, null, terminal) with ConsoleReaderHelper {
    // working around protected/trait/java insufficiencies.
    def goBack(num: Int): Unit = back(num)
    def readOneKey(prompt: String) = {
      this.print(prompt)
      this.flush()
      this.readVirtualKey()
    }
    def eraseLine() = consoleReader.resetPromptLine("", "", 0)
    def redrawLineAndFlush(): Unit = { flush() ; drawLine() ; flush() }
    // override def readLine(prompt: String): String

    // A hook for running code after the repl is done initializing.
    lazy val postInit: Unit = {
      this setBellEnabled false
      if (history ne NoHistory)
        this setHistory history

      if (completion ne NoCompletion) {
        val argCompletor: ArgumentCompleter =
          new ArgumentCompleter(new JLineDelimiter, scalaToJline(completion.completer()))
        argCompletor setStrict false

        this addCompleter argCompletor
        this setAutoprintThreshold 400 // max completion candidates without warning
      }
    }
  }
  val consoleReader = new StreamJLineConsoleReader()
  
 lazy val completion = _completion
  lazy val history: JLineHistory = JLineHistory()

  private def term = consoleReader.getTerminal()
  def reset() = term.reset()
  def init() = term.init()

  def scalaToJline(tc: ScalaCompleter): Completer = new Completer {
    def complete(_buf: String, cursor: Int, candidates: JList[CharSequence]): Int = {
      val buf = if (_buf == null) "" else _buf
      val Candidates(newCursor, newCandidates) = tc.complete(buf, cursor)
      newCandidates foreach (candidates add _)
      newCursor
    }
  }

  class JLineConsoleReader extends ConsoleReader with ConsoleReaderHelper {
    if ((history: History) ne NoHistory)
      this setHistory history

    // working around protected/trait/java insufficiencies.
    def goBack(num: Int): Unit = back(num)
    def readOneKey(prompt: String) = {
      this.print(prompt)
      this.flush()
      this.readVirtualKey()
    }
    def eraseLine() = consoleReader.resetPromptLine("", "", 0)
    def redrawLineAndFlush(): Unit = { flush() ; drawLine() ; flush() }
    // override def readLine(prompt: String): String

    // A hook for running code after the repl is done initializing.
    lazy val postInit: Unit = {
      this setBellEnabled false

      if (completion ne NoCompletion) {
        val argCompletor: ArgumentCompleter =
          new ArgumentCompleter(new JLineDelimiter, scalaToJline(completion.completer()))
        argCompletor setStrict false

        this addCompleter argCompletor
        this setAutoprintThreshold 400 // max completion candidates without warning
      }
    }
  }

  def currentLine = consoleReader.getCursorBuffer.buffer.toString
  def redrawLine() = consoleReader.redrawLineAndFlush()
  def eraseLine() = consoleReader.eraseLine()
  // Alternate implementation, not sure if/when I need this.
  // def eraseLine() = while (consoleReader.delete()) { }
  def readOneLine(prompt: String) = consoleReader readLine prompt
  def readOneKey(prompt: String) = consoleReader readOneKey prompt
}