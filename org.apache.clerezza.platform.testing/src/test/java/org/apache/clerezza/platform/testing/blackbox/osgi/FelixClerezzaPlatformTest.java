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
package org.apache.clerezza.platform.testing.blackbox.osgi;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
import static org.ops4j.pax.exam.junit.JUnitOptions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.web.resources.jquery.JQuery;
import org.apache.clerezza.web.resources.scripts.Scripts;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Inject;
import org.osgi.framework.BundleContext;

import org.osgi.util.tracker.ServiceTracker;

/**
 *
 * @author mir, reto
 */
@RunWith(JUnit4TestRunner.class)
public class FelixClerezzaPlatformTest {

	@Configuration
	public static Option[] configuration() {
		return options(
				mavenConfiguration(),
				mavenBundle().groupId("org.osgi").artifactId(
				"org.osgi.core").versionAsInProject(),
				mavenBundle().groupId("org.osgi").artifactId(
				"org.osgi.compendium").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza.ext").artifactId(
				"com.hp.hpl.jena").versionAsInProject(),
				/*mavenBundle().groupId("org.apache.felix").artifactId(
				"org.apache.felix.log").versionAsInProject(),*/
				mavenBundle().groupId("org.ops4j.pax.logging").artifactId(
				"pax-logging-api").versionAsInProject(),
				mavenBundle().groupId("org.ops4j.pax.logging").artifactId(
				"pax-logging-service").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.core").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza.ext").artifactId(
				"org.openrdf.sesame").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.sesame.storage").versionAsInProject(),
				mavenBundle().groupId("org.wymiwyg").artifactId(
				"wrhapi").versionAsInProject(),
				mavenBundle().groupId("org.wymiwyg").artifactId(
				"wymiwyg-commons-core").versionAsInProject(),
				mavenBundle().groupId("org.wymiwyg").artifactId(
				"wrhapi-osgi").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza.ext").artifactId(
				"com.ibm.icu").versionAsInProject(),
				mavenBundle().groupId("org.apache.felix").artifactId(
				"org.apache.felix.scr").version("1.0.6"),
				mavenBundle().groupId("org.apache.felix").artifactId(
				"org.apache.felix.eventadmin").version("1.0.0"),
				mavenBundle().groupId("org.apache.felix").artifactId(
				"org.apache.felix.metatype").version("1.0.2"),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.triaxrs").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza.ext").artifactId(
				"javax.mail").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza.ext").artifactId(
				"org.json.simple").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.utils").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.jaxrs.rdf.providers").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.jaxrs.utils").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.ontologies").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.config").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.graphprovider.content").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.templating").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza.scala").artifactId(
				"scala-compiler-osgi").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza.scala").artifactId(
				"scala-library-osgi").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza.scala").artifactId(
				"script-engine").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.permissiondescriptions").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.typerendering.scalaserverpages").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.scala.utils").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.typerendering.ontologies").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.typerendering.core").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.templating.seedsnipe").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.typerendering.seedsnipe").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.mail").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.utils.customproperty").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.dashboard.core").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.dashboard.ontologies").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.web.fileserver").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.content").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.typehandlerspace").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.web.resources.jquery").startLevel(4).versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.usermanager").startLevel(4).versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.scripting").startLevel(4).versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.jena.sparql").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.jena.parser").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.jena.serializer").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.rdfjson").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.web.ontologies").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.rdf.web.core").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.typerendering.manager").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.scripting.scriptmanager").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.xhtml2html").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.web.resources.yui").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.permissiondescriptions").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.utils.imageprocessing").versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.language.core").versionAsInProject(),
				mavenBundle().groupId("org.apache.httpcomponents").artifactId(
				"httpcore-osgi").versionAsInProject(),
				//dsProfile(),
				configProfile(),
				webProfile(),
				junitBundles(),
				vmOption("-XX:MaxPermSize=200m"),
				frameworks(
				felix()),
				systemProperty("org.osgi.service.http.port").value(
				Integer.toString(testHttpPort)));
	}
	protected final static int testHttpPort = 8976;
	@Inject
	private BundleContext bundleContext;
	private boolean webServerExist;

	@Before
	public void registerServices()
			throws Exception {
		webServerExist = waitForWebserver();
		Assert.assertTrue("webserver running on port "+testHttpPort, webServerExist);
		Thread.sleep(10000);
	}

	@Test
	public void checkTcManagerService()
			throws Exception {
		Object service = waitFor(TcManager.class, 20000);
		Assert.assertTrue(service != null);

	}

	@Test
	public void checkJaxRsServices()
			throws Exception {
		Object service = waitFor(JQuery.class, 20000);
		Assert.assertTrue(service != null);		
		service = waitFor(Scripts.class, 20000);
		Assert.assertTrue(service != null);
	}


	private Object waitFor(Class<?> aClass, long timeout)
			throws InterruptedException {
		ServiceTracker tracker = new ServiceTracker(bundleContext,
				aClass.getName(), null);
		tracker.open();
		Object service = tracker.waitForService(timeout);
		return service;
	}

	private boolean waitForWebserver()
			throws InterruptedException {
		int j = 0;
		try {
			URL serverURL = new URL("http://localhost:" + testHttpPort + "/");
			while (true) {
				try {
					serverURL.openConnection().getInputStream();
				} catch (FileNotFoundException ex) {
					break;
				} catch (IOException ex) {
					System.out.println("waiting (" + ex + ")");
					Thread.sleep(3000);
					if (j++ < 100) {
						continue;
					}
				}
				break;
			}
		} catch (MalformedURLException ex) {
			Logger.getLogger(FelixClerezzaPlatformTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		return j < 100;
	}
}
