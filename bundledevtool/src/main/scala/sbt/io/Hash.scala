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
 * Copyright 2009 Mark Harrah
 */
package sbt

import java.io.{ByteArrayInputStream, File, InputStream}

object Hash
{
	private val BufferSize = 8192
	def toHex(bytes: Array[Byte]): String =
	{
		val buffer = new StringBuilder(bytes.length * 2)
		for(i <- 0 until bytes.length)
		{
			val b = bytes(i)
			val bi: Int = if(b < 0) b + 256 else b
			buffer append toHex((bi >>> 4).asInstanceOf[Byte])
			buffer append toHex((bi & 0x0F).asInstanceOf[Byte])
		}
		buffer.toString
	}
	def fromHex(hex: String): Array[Byte] =
	{
		require((hex.length & 1) == 0, "Hex string must have length 2n.")
		val array = new Array[Byte](hex.length >> 1)
		for(i <- 0 until hex.length by 2)
		{
			val c1 = hex.charAt(i)
			val c2 = hex.charAt(i+1)
			array(i >> 1) = ((fromHex(c1) << 4) | fromHex(c2)).asInstanceOf[Byte]
		}
		array
	}
	/** Calculates the SHA-1 hash of the given String.*/
	def apply(s: String): Array[Byte] = apply(new ByteArrayInputStream(s.getBytes("UTF-8")))
	/** Calculates the SHA-1 hash of the given file.*/
	def apply(file: File): Array[Byte] = Using.fileInputStream(file)(apply)
	/** Calculates the SHA-1 hash of the given stream, closing it when finished.*/
	def apply(stream: InputStream): Array[Byte] =
	{
		import java.security.{MessageDigest, DigestInputStream}
		val digest = MessageDigest.getInstance("SHA")
		try
		{
			val dis = new DigestInputStream(stream, digest)
			val buffer = new Array[Byte](BufferSize)
			while(dis.read(buffer) >= 0) {}
			dis.close()
			digest.digest
		}
		finally { stream.close() }
	}

	private def toHex(b: Byte): Char =
	{
		require(b >= 0 && b <= 15, "Byte " + b + " was not between 0 and 15")
		if(b < 10)
			('0'.asInstanceOf[Int] + b).asInstanceOf[Char]
		else
			('a'.asInstanceOf[Int] + (b-10)).asInstanceOf[Char]
	}
	private def fromHex(c: Char): Int =
	{
		val b =
			if(c >= '0' && c <= '9')
				(c - '0')
			else if(c >= 'a' && c <= 'f')
				(c - 'a') + 10
			else if(c >= 'A' && c <= 'F')
				(c - 'A') + 10
			else
				throw new RuntimeException("Invalid hex character: '" + c + "'.")
		b
	}
}
