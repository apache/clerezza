/*
 *
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
 *
*/

/* sbt -- Simple Build Tool
 * Copyright 2009  Mark Harrah
 */
package sbt

object ErrorHandling
{
	def translate[T](msg: => String)(f: => T) =
		try { f }
		catch { case e: Exception => throw new TranslatedException(msg + e.toString, e) }

	def wideConvert[T](f: => T): Either[Throwable, T] =
		try { Right(f) }
		catch
		{
			case ex @ (_: Exception | _: StackOverflowError) => Left(ex)
			case err @ (_: ThreadDeath | _: VirtualMachineError) => throw err
			case x => Left(x)
		}

	def convert[T](f: => T): Either[Exception, T] =
		try { Right(f) }
		catch { case e: Exception => Left(e) }
}
final class TranslatedException private[sbt](msg: String, cause: Throwable) extends RuntimeException(msg, cause)
{
	override def toString = msg
}
