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
import scala.tools.nsc.reporters.ConsoleReporter
import scala.tools.nsc.util.Position
import javax.script.ScriptException
import java.io.{ByteArrayOutputStream, PrintWriter}
import scala.tools.nsc.reporters.Reporter


package org.apache.clerezza.scala.interpreter {
	/**
	 * This class implements a <code>Reporter</code> that throws
	 * <code>ScriptException<code>s when an error occurred.
	 *
	 * @author mir
	 */
	class ExceptionReporter() extends Reporter {

		override protected def info0(pos : Position, msg : String, severity : Severity, force : Boolean) = {
			if (severity.equals(ERROR)) {
				val out:ByteArrayOutputStream = new ByteArrayOutputStream
				val consoleReporter:ConsoleReporter = new ConsoleReporter(null, null, new PrintWriter(out, true))
				consoleReporter.printSourceLine(pos)
				val sb:StringBuilder = new StringBuilder;
				sb.append(msg + "\n")
				sb.append(new String(out.toByteArray));
				throw new ScriptException(sb.toString, null, pos.line.get, pos.column.get)
			}
		}
	}
}