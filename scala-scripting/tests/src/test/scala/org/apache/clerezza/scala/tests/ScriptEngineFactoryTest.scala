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
import javax.script.ScriptEngineFactory
import org.osgi.util.tracker.ServiceTracker;


/**
 *
 * @author reto
 */
@RunWith(classOf[JUnit4TestRunner])
class ScriptEngineFactoryTest {
	

	
	@Inject
	private var bundleContext: BundleContext = null;
	
	private var webServerExist = false;

	private var scriptEngineFactory: ScriptEngineFactory = null;

	@Before
	def getService() : Unit = {
		/*webServerExist = waitForWebserver();*/
		scriptEngineFactory = waitFor(classOf[ScriptEngineFactory], 300000);
		println("Got: "+scriptEngineFactory)
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
		Assert.assertNotNull(scriptEngineFactory)
	}

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