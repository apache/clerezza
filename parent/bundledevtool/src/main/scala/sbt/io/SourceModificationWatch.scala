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
 * Copyright 2009, 2010  Mikko Peltonen, Stuart Roebuck, Mark Harrah
 */
package sbt

	import annotation.tailrec

object SourceModificationWatch
{
	@tailrec def watch(sourcesFinder: PathFinder, pollDelaySec: Int, state: WatchState)(terminationCondition: => Boolean): (Boolean, WatchState) =
	{
			import state._

		def sourceFiles: Iterable[java.io.File] = sourcesFinder.getFiles
		val (lastModifiedTime, fileCount) =
			( (0L, 0) /: sourceFiles) {(acc, file) => /*println("processing "+file);*/ (math.max(acc._1, file.lastModified), acc._2 + 1)}

		//println("lastModifiedTime:"+new java.util.Date(lastModifiedTime))
		//println("lastModifiedTime - lastCallbackCallTime"+(lastModifiedTime - lastCallbackCallTime))
		val sourcesModified =
			lastModifiedTime > lastCallbackCallTime ||
			previousFileCount != fileCount

		val (triggered, newCallbackCallTime) =
			if (sourcesModified) {
				(false, System.currentTimeMillis)
			}
			else
				(awaitingQuietPeriod, lastCallbackCallTime)

		val newState = new WatchState(newCallbackCallTime, fileCount, sourcesModified, if(triggered) count + 1 else count)
		if(triggered)
			(true, newState)
		else
		{
			Thread.sleep(pollDelaySec * 1000)
			if(terminationCondition)
				(false, newState)
			else
				watch(sourcesFinder, pollDelaySec, newState)(terminationCondition)
		}
	}
}
final class WatchState(val lastCallbackCallTime: Long, val previousFileCount: Int, val awaitingQuietPeriod:Boolean, val count: Int)
object WatchState
{
	def empty = new WatchState(0L, 0, false, 0)
}
