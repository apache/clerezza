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
 * Copyright 2008, 2009  Mark Harrah
 */
package sbt

	import java.io.File

trait Mapper
{
	type PathMap = File => Option[String]
	type FileMap = File => Option[File]

	val basic: PathMap = f => Some(f.getPath)
	def relativeTo(base: File): PathMap = IO.relativize(base, _)
	def rebase(oldBase: File, newBase0: String): PathMap =
	{
		val newBase = normalizeBase(newBase0)
		(file: File) =>
			if(file == oldBase)
				Some( if(newBase.isEmpty) "." else newBase )
			else
				IO.relativize(oldBase, file).map(newBase + _)
	}
	def fail: Any => Nothing = f => error("No mapping for " + f)
	val flat: PathMap = f => Some(f.getName)
	def flatRebase(newBase0: String): PathMap =
	{
		val newBase = normalizeBase(newBase0)
		f => Some(newBase + f.getName)
	}
	def some[A,B](f: A => B): A => Some[B] = x => Some(f(x))

	def normalizeBase(base: String) = if(!base.isEmpty && !base.endsWith("/"))  base + "/" else base

	def abs: FileMap = f => Some(f.getAbsoluteFile)
	def resolve(newDirectory: File): FileMap = file => Some(new File(newDirectory, file.getPath))
	def rebase(oldBase: File, newBase: File): FileMap =
		file =>
			if(file == oldBase)
				Some(newBase)
			else
				IO.relativize(oldBase, file) map { r => new File(newBase, r) }

	def flat(newDirectory: File): FileMap = file => Some(new File(newDirectory, file.getName))
}

trait Alternative[A,B] { def | (g: A => Option[B]): A => Option[B] }
trait Alternatives
{
	implicit def alternative[A,B](f:A => Option[B]): Alternative[A,B] =
		new Alternative[A,B] { def | (g: A => Option[B]) =
			(a: A) => f(a) orElse g(a)
		}
	final def alternatives[A,B](alts: Seq[A => Option[B]]): A => Option[B] =
		alts match
		{
			case Seq(f, fs @ _*) => f | alternatives(fs)
			case Seq() => a => None
		}
}
