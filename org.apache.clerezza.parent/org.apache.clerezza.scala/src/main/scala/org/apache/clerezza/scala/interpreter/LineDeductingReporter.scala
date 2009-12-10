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
import scala.tools.nsc.util.Position
import javax.script.ScriptException
import java.io.{ByteArrayOutputStream, PrintWriter}
import scala.tools.nsc.reporters.Reporter


package org.apache.clerezza.scala.interpreter {

	/**
	 * This class implements a <code>Reporter</code> wrapping another reported
	 * deducting a specified Number of line from the position of message.
	 *
	 * @author rbn
	 */
	class LineDeductingReporter(base: Reporter, lineDeduction: Int) extends Reporter {

		private def deductLines(pos : Position) = {
			new Position {
				override def column : Option[Int] = pos.column
				override def dbgString = pos.dbgString
				override def line : Option[Int] = Some(pos.line.get - lineDeduction)
				override def lineContent = pos.lineContent
				override def offset = pos.offset
				override def source = pos.source
			}
		}

		override def info(pos: Position, msg: String, force: Boolean): Unit =
			base.info(deductLines(pos), msg, force)
		override def warning(pos: Position, msg: String): Unit =
			base.warning(deductLines(pos), msg)
		override def error(pos: Position, msg: String): Unit =
			base.error(deductLines(pos), msg)

		override protected def info0(pos : Position, msg : String, severity : Severity, force : Boolean) = {
			throw new RuntimeException("I think that's design bug of scala.tools.nsc.reporters.Reporter")

		}

	}
}