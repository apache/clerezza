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
package org.apache.clerezza.scala.tests;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import org.ops4j.pax.exam.CoreOptions._;
import org.ops4j.pax.exam.container.`def`.PaxRunnerOptions._;
import org.ops4j.pax.exam.junit.JUnitOptions._;

import java.security.AccessController
import java.security.PrivilegedActionException
import java.security.PrivilegedExceptionAction
import org.apache.clerezza.scala.scripting.CompilerService
import org.junit.Assert
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Inject;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker
import scala.actors.Actor
import scala.math.random


/**
 *
 * @author reto
 */
@RunWith(classOf[JUnit4TestRunner])
class CompilerServiceTest {


	
	@Inject
	private var bundleContext: BundleContext = null;
	
	private var webServerExist = false;

	private var service: CompilerService = null;

	@Before
	def getService() : Unit = {
		/*webServerExist = waitForWebserver();*/
		service = waitFor(classOf[CompilerService], 300000);
	}

	private def waitFor[T](aClass: Class[T], timeout: Long): T = {
		System.out.println("waiting for a " + aClass);
		val tracker = new ServiceTracker(bundleContext,
				aClass.getName(), null);
		tracker.open();
		val service = tracker.waitForService(timeout);
		return service.asInstanceOf[T];
	}

	@Test
	def checkEngine(): Unit =  {
		Assert.assertNotNull(service);
		//do it once
		val testClassClass1 = {
			val s = """
			package foo {
				class TestClass() {
					println("constructing TestClass");
				}
				object TestClass {
					println("constructing TestClass Object");
					val msg = "Hello"
				}
			}
			"""
			val compileResult = priv(service.compile(List(s.toCharArray)))
			println("finished compiling")
			Assert.assertEquals(1, compileResult.size)
			val testClassClass: Class[_] = compileResult(0)
			//Assert.assertEquals("foo.TestClass", testClassClass.getName)
			testClassClass
		}
		//compile different class with same name
		{
			val s = """
			package foo {
				class TestClass() {
					println("constructing a different TestClass");
				}
				object TestClass {
					println("constructing TestClass Object");
					val msg = "Hello2"
				}
			}
			"""
			//and another class
			val s2 = """
			package foo {
				class TestClass2() {
					println("constructing TestClass2");
				}
				object TestClass2 {
					println("constructing TestClass2 Object");
					val msg = "Hello2b"
				}
			}
			"""
			val compileResult = priv(service.compile(List(s.toCharArray, s2.toCharArray)))
			Assert.assertEquals(2, compileResult.size)
			val nameMsg = for (cr <- compileResult) yield
				(cr.getName, {
						val method = cr.getMethod("msg")
						method.invoke(null)
					})
			val nameMsgMap = Map(nameMsg: _*)
			Assert.assertTrue(nameMsgMap.contains("foo.TestClass"))
			Assert.assertTrue(nameMsgMap.contains("foo.TestClass2"))

			Assert.assertEquals("Hello2", nameMsgMap("foo.TestClass"))
			Assert.assertEquals("Hello2b", nameMsgMap("foo.TestClass2"))
		}
		val methodFrom1Again = testClassClass1.getMethod("msg")
		Assert.assertEquals("Hello", methodFrom1Again.invoke(null))
	}

	@Test
	def testConcurrency : Unit = {
		val startTime = System.currentTimeMillis
		import scala.actors.Actor._
		val actorsCount = 5
		val iterationsCount = 9
		val testRunner = self
		for (i <- 1 to actorsCount) {
			object ValueVerifier extends Actor {
				def act() {
					try {
						for (i <- 1 to iterationsCount) {
							val uniqueToken = (for (i <-1 to 12) yield ((random*('z'-'a'+1))+'a').asInstanceOf[Char]).mkString
							val objectName = "MyClass"
							val message = "Hello from "+uniqueToken

							val source = """
object """+objectName+""" {
	println("constructing TestClass Object");
	val msg = """"+message+""""
}"""

							//println("compiling: "+source)
							val sources = List(source.toCharArray)
							val compiled = priv(service.compile(sources))
							val clazz = compiled(0)
							val className = clazz.getName
							testRunner ! (objectName, className)
							val method = clazz.getMethod("msg")
							val receivedMessage = method.invoke(null)
							testRunner ! (message, receivedMessage)
						}
					} catch {
						case t => testRunner ! t
					}
				}
			}
			ValueVerifier.start()
		}
		for (i <- 1 to (actorsCount*iterationsCount*2)) {
			self.receive {
				case (expected, got) => {
						Assert.assertEquals(expected, got)
				}
				case t : Throwable => throw t
			}
		}
		val duration = System.currentTimeMillis - startTime
		println("running the tests took "+duration)

	}

	def priv[T](action: => T): T = {
		try {
			AccessController.doPrivileged(
			new PrivilegedExceptionAction[T]() {
				def run = action
			})
		} catch {
			case e: PrivilegedActionException => throw e.getCause
			case e => throw e
		}
	}

}

object CompilerServiceTest {

	protected val testHttpPort = 8976;

	@Configuration
	def configuration() : Array[Option] = {
		return options(
				mavenConfiguration(),
				dsProfile(),
				configProfile(),
				//webProfile(),
				junitBundles(),
				frameworks(
					felix()),
				systemProperty("org.osgi.service.http.port").value(
				Integer.toString(testHttpPort)));
	}
}