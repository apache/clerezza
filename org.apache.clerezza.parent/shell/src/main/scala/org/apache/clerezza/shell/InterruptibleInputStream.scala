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
package org.apache.clerezza.shell

import java.io.InputStream
import java.nio.channels.ClosedByInterruptException
import scala.actors.Actor
import scala.actors.Actor._

class InterruptibleInputStream(base: InputStream) extends InputStream {
	private case object Stop
	private case object Read

	private var readingThread: Thread = null

	val readerActor = new Actor() {
		def act() {
			loop {
				react {
					case Stop => exit()
					case Read => {
							readingThread = Thread.currentThread
							val ch = try {
								 base.read()
							} catch {
								case e: ClosedByInterruptException => {
										-1
								}
							}
							readingThread = null
							sender ! ch
					}
				}
			}
		}
	}
	readerActor.start()

	def read() = {
		readerActor ! Read
		self.receive {
			case x: Int => x
		}
	}

	def terminate() {
		readerActor ! Stop
		val currentReadingThread = readingThread
		if (currentReadingThread != null) {
			currentReadingThread.interrupt()
		}
	}

}
