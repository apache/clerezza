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

import org.junit.Assert
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Inject;
import org.osgi.framework.BundleContext;
import javax.script.{ScriptEngineFactory, Bindings, ScriptException, Compilable}
import org.osgi.util.tracker.ServiceTracker
import scala.actors.Actor
import scala.math.random


/**
 *
 * @author reto
 */
@RunWith(classOf[JUnit4TestRunner])
class ScriptEngineFactoryTest {
	

	
	@Inject
	private var bundleContext: BundleContext = null;
	
	private var webServerExist = false;

	private var factory: ScriptEngineFactory = null;

	@Before
	def getService() : Unit = {
		/*webServerExist = waitForWebserver();*/
		factory = waitFor(classOf[ScriptEngineFactory], 300000);
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
		Assert.assertNotNull(factory)
		Assert.assertEquals("Scala Scripting Engine for OSGi", factory.getEngineName);
		val s = "hello"
		val engine = factory.getScriptEngine
		Assert.assertEquals(s, engine.eval("\""+s+"\""))
		val bindings = engine.createBindings
		bindings.put("s",s)
		Assert.assertEquals(s, engine.eval("s", bindings))
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
							val s = "r: "+random.toString
							val engine = factory.getScriptEngine
							val bindings = engine.createBindings
							bindings.put("s",s)
							val script = """
import scala.math.random
Thread.sleep((random*10).toInt)
s"""
							testRunner ! (s, engine.eval(script, bindings))
						}
					} catch {
						case t => testRunner ! t
					}
				}
			}
			ValueVerifier.start()
		}
		for (i <- 1 to (actorsCount*iterationsCount)) {
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

	@Test
	def classFromNewlyAddedBundle(): Unit =  {
		val s = "hello"
		val engine = factory.getScriptEngine
		val bindings = engine.createBindings
		bindings.put("s",s)
		Assert.assertEquals(s, engine.eval("s", bindings))
		bundleContext.installBundle("http://repo2.maven.org/maven2/org/wymiwyg/wrhapi/0.8.2/wrhapi-0.8.2.jar");
		Thread.sleep(100)
		val script = """
		|import org.wymiwyg.wrhapi._
		|val h : Handler = null
		|s""".stripMargin
		Assert.assertEquals(s, engine.eval(script, bindings))
	}

	@Test
	def compiledScript(): Unit = {
		val string = "hello"
		val script = "\""+string+"\""
		val engine = factory.getScriptEngine.asInstanceOf[Compilable]
		val compiledScript = engine.compile(script)
		Assert.assertEquals(string, compiledScript.eval())
	}
	
	@Test(expected=classOf[ScriptException])
	def compileErrorScript(): Unit = {
		val script = "this is not real scala !"
		val engine = factory.getScriptEngine.asInstanceOf[Compilable]
		val compiledScript = engine.compile(script)
	}
	
	@Test(expected=classOf[ScriptException])
	def compileUnfinishedScript(): Unit = {
		val script = "if (true) {"
		val engine = factory.getScriptEngine.asInstanceOf[Compilable]
		val compiledScript = engine.compile(script)
	}

	
	def compileNormalAfterErrorScript(): Unit = {
		val script = "this is not real scala !"
		val engine = factory.getScriptEngine.asInstanceOf[Compilable]
		try {
			val compiledScript = engine.compile(script)
		} catch {
			case e => Assert.assertEquals(classOf[ScriptException], e.getClass)
		}
		val string = "hello"
		val script2 = "\""+string+"\""
		val compiledScript2 = engine.compile(script2)
		Assert.assertEquals(string, compiledScript2.eval())
	}
	//This seems hard to realize before https://lampsvn.epfl.ch/trac/scala/ticket/3513 is fixed
	/*@Test
	def checkException(): Unit =  {
		val s = """val s="hello"
		illegal.do"""
		val engine = factory.getScriptEngine
		try {
			Assert.assertEquals("should have exception",engine.eval(s))
		} catch {
			case e : ScriptException => Assert.assertEquals(2, e.getLineNumber)
		}
	}*/
}

object ScriptEngineFactoryTest {

	protected val testHttpPort = 8976;

	@Configuration
	def configuration() : Array[Option] = {
		return options(
				mavenConfiguration(),
				//using old ds because of issues with 1.0.8
				dsProfile(),
				configProfile(),
				webProfile(),
				junitBundles(),
				frameworks(
					felix()),
				systemProperty("org.osgi.service.http.port").value(
				Integer.toString(testHttpPort)));
	}
}