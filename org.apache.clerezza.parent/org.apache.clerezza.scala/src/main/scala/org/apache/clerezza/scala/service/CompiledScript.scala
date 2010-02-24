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
import java.lang.reflect.{Method, Type}
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import scala.collection._
import org.apache.clerezza.scala.interpreter._
import org.osgi.framework.BundleContext

package org.apache.clerezza.scala.service{

	/**
	 * Scala class which initially compiles the assigned scala script. The script then can be executed by
	 * calling the execute(valueMap) function.
	 *
	 * @param script
	 * 			the script which has to be compiled and executed
	 * @param jTypeMap
	 * 			a Java Map which contains the Types of the parameters
	 * @param interpreter
	 * 			the ScalaInterpreter which compiles and executes the script
	 * @param className
	 *			the name of the class of the compiled script.
	 * @param lineOffset
	 *			the offset used for adjusting the line numbers.
	 * @author rbn, mkn, pmg
	 */
  
	class CompiledScript (script : String , jTypeMap : Map[String, Type] , interpreter : ScalaInterpreter,
		  className : String, lineOffset : Int){
		val name = if (className == null) {
			createClassName("CompiledScalaScript");
		} else {
			createClassName(className.replaceAll("-", "_"));
		}
		
		if (interpreter.getClassFile(name) == null) {
			//prevents synchroneous compilation (even for different scripts)
			CompiledScript.synchronized {
				if (interpreter.getClassFile(name) == null) {
					interpreter.compile(name, script, jTypeMap, lineOffset)
				}
			}
		}

		/**
		 * Executes the Creates and stores a concept with the specified prefLabel into the
		 * content graph if a concept with this prefLabel does not already exist in
		 * the graph.
		 *
		 * @param jValueMap
		 * 			a Java Map with the types of the parameters
		 * @return Any
		 * 			result of execution
		 */
		def execute(jValueMap : java.util.Map[String, Any]): Any = {
		
			val jHashMap = new java.util.HashMap[String, Any]();
			jHashMap.putAll(jValueMap)
			val valueMap : Map[String, Any] = new immutable.HashMap() ++ new jcl.HashMap[String, Any](jHashMap)
			interpreter.execute(name,valueMap);
		}

		/**
		 * 	Creates a name based on the hash value of the script
		 * 	e.g. 54ec400cde5e65a27320d6c71e2a334d.class
		 */
		protected def createClassName(className : String) = {
			val encryptMsg : Array[Byte] =
			try {
				val md = MessageDigest.getInstance("MD5")
				md.digest(script.getBytes())
				//solving the MD5-Hash
			} catch {
				case ex: NoSuchAlgorithmException => throw new RuntimeException("No Such Algorithm Exception!")
			}
			val strBuf = new StringBuffer()
			for (msg <- encryptMsg) {
				val byteStr = Integer.toHexString(msg)
				//swap-string for current hex-value of byte
				val swap = byteStr.length match {
					case 1 => "0" + Integer.toHexString(msg)
					case 2 => Integer.toHexString(msg)
					case _ => Integer.toHexString(msg).substring(6, 8)
				}
				strBuf.append(swap); // appending swap to get complete hash-key
			}
			className + strBuf.toString()
		}
	}

	object CompiledScript {
	}
}

