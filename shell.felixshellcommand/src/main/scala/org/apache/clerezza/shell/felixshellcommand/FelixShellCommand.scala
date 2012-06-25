/*
 *  Copyright 2010 reto.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.shell.felixshellcommand

import java.io.OutputStream
import java.io.PrintStream
import java.io.Writer
import org.apache.clerezza.shell.ShellCommand
import org.apache.felix.shell.ShellService

class FelixShellCommand extends ShellCommand {
	private var felixShell: ShellService = null

	def command: String = "felix"
	def description: String = "execute a felix shell command"
	/**
	 * Extecutes the command an return (keepRunning,Option[lineToRecord])
	 */
	def execute(line: String, out: OutputStream): (Boolean, Option[String]) = {
		val printStream = new PrintStream(out)
		felixShell.executeCommand(line, printStream, printStream)
		printStream.flush()
		(true, None)
	}

	def bindFelixShell(felixShell: ShellService)  {
		this.felixShell = felixShell
	}

	def unbindFelixShell(felixShell: ShellService)  {
		this.felixShell = null
	}


}
