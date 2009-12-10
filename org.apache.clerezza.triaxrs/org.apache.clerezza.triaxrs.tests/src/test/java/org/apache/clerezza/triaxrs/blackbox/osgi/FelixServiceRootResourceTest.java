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
package org.apache.clerezza.triaxrs.blackbox.osgi;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
import static org.ops4j.pax.exam.junit.JUnitOptions.*;

/**
 *
 * @author reto
 */
@RunWith(JUnit4TestRunner.class)
public class FelixServiceRootResourceTest
		extends AbstractServiceRootResourceTest {

	
	@Configuration
	public static Option[] configuration() {
		return options(
				mavenConfiguration(),
				//using old ds because of issues with 1.0.8
				mavenBundle().groupId("org.apache.felix").artifactId(
				"org.apache.felix.scr").version("1.0.6"),
				mavenBundle().groupId("org.apache.felix").artifactId(
				"org.apache.felix.eventadmin").version("1.0.0"),
				mavenBundle().groupId("org.apache.felix").artifactId(
				"org.apache.felix.metatype").version("1.0.2"),
				/*dsProfile(),*/
				configProfile(),
				webProfile(),
				junitBundles(),
				frameworks(
				felix()),
				systemProperty("org.osgi.service.http.port").value(
				Integer.toString(testHttpPort)));
	}

	
}
