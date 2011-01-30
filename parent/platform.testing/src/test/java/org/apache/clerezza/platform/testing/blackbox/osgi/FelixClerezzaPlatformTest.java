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

import java.io.ByteArrayOutputStream;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.frameworks;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.mavenConfiguration;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.configProfile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.vmOption;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.webProfile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.dsProfile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.profile;
import static org.ops4j.pax.exam.junit.JUnitOptions.junitBundles;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;

import junit.framework.Assert;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.UriRef;

import org.apache.clerezza.rdf.core.access.TcManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.swissbox.tinybundles.core.TinyBundles;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.framework.Constants;



/**
 * 
 * @author mir, reto
 */
@RunWith(JUnit4TestRunner.class)
public class FelixClerezzaPlatformTest {

	private static final int REQUESTS_PER_THREAD = 50;
	private static final int THREADS_COUNT = 10;

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
				/*mavenBundle().groupId("org.apache.felix").artifactId(
				"org.apache.felix.scr").version("1.0.6"),
				mavenBundle().groupId("org.apache.felix").artifactId(
				"org.apache.felix.eventadmin").version("1.0.0"),
				mavenBundle().groupId("org.apache.felix").artifactId(
				"org.apache.felix.metatype").version("1.0.2"),*/
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
				"org.apache.clerezza.platform.security").startLevel(4).versionAsInProject(),
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.security.conditions").startLevel(4).versionAsInProject(),
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
				"org.apache.clerezza.platform.typerendering.scala").versionAsInProject(),
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
				mavenBundle().groupId("org.apache.clerezza").artifactId(
				"org.apache.clerezza.platform.language.ontologies").versionAsInProject(),
				mavenBundle().groupId("org.apache.httpcomponents").artifactId(
				"httpcore-osgi").versionAsInProject(),
				mavenBundle().groupId("org.mortbay.jetty").artifactId(
				"servlet-api-2.5").version("6.1.12"),
				dsProfile(),
				profile("felix.webconsole"),
				configProfile(),
				webProfile(),
				junitBundles(),
				vmOption("-XX:MaxPermSize=200m"),
				frameworks(felix()),
				systemProperty("org.osgi.service.http.port").value(
				Integer.toString(testHttpPort)),
				new Customizer() {

					@Override
					public InputStream customizeTestProbe(InputStream testProbe)
							throws IOException {
						return TinyBundles.modifyBundle(testProbe).
								set(Constants.EXPORT_PACKAGE, "org.apache.clerezza.platform.testing.blackbox.osgi").build();
					}
				});
	}
	protected final static int testHttpPort = 8976;
	@Inject
	private BundleContext bundleContext;
	private boolean webServerExist;

	@Before
	public void registerServices() throws Exception {
		webServerExist = waitForWebserver();
		Assert.assertTrue("webserver running on port " + testHttpPort,
				webServerExist);
		Thread.sleep(10000);
	}

	/**
	 * multiple tests that run within the lifetime of the same instance
	 * 
	 * @throws Exception
	 */
	@Test
	public void multi() throws Exception {
		checkTcManagerService();
		testJaxRsRegistration();
		testWithScalaServerPage();
	}

	private void testJaxRsRegistration() throws InterruptedException, IOException {
		final Dictionary<String, Object> jaxRsResourceProperty = new Hashtable<String, Object>();
		{
			jaxRsResourceProperty.put("javax.ws.rs", Boolean.TRUE);
			jaxRsResourceProperty.put("service.pid", SimpleRootResource.class.getName());
		}
		bundleContext.registerService(Object.class.getName(),
				new SimpleRootResource(), jaxRsResourceProperty);
		URL url = new URL("http://localhost:" + testHttpPort + "/foo");
		Thread.sleep(4000);
		requestUrl(url);//"/admin/users/list-users"));
		runRequestThreads(url);
	}

	private void testWithScalaServerPage() throws InterruptedException, IOException {
		registerRDFListRootResource();
		final Dictionary<String, Object> webRenderingServiceProperty = new Hashtable<String, Object>();
		{
			webRenderingServiceProperty.put("service.pid", SomeContentWebRenderingService.class.getName());
		}
		bundleContext.registerService(SomeContentWebRenderingService.class.getName(),
				new SomeContentWebRenderingService(), webRenderingServiceProperty);
		RenderletManager renderletManager = waitFor(RenderletManager.class, 20000);
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource("renderingServiceTest.ssp").toString()),
				RDFListRootResource.testType, null, MediaType.TEXT_PLAIN_TYPE, false);
		URL url = new URL("http://localhost:" + testHttpPort + "/list");
		Thread.sleep(4000);
		String returnedString = new String(requestUrl(url), "utf-8");
		Assert.assertEquals("some content", returnedString);

	}

	private void checkTcManagerService() throws Exception {
		Object service = waitFor(TcManager.class, 20000);
		Assert.assertTrue(service != null);

	}

	private <T> T waitFor(Class<T> aClass, long timeout)
			throws InterruptedException {
		ServiceTracker tracker = new ServiceTracker(bundleContext,
				aClass.getName(), null);
		tracker.open();
		T service = (T)tracker.waitForService(timeout);
		return service;
	}

	private boolean waitForWebserver() throws InterruptedException {
		int j = 0;
		try {
			URL serverURL = new URL("http://localhost:" + testHttpPort + "/");
			while (true) {
				try {
					serverURL.openConnection().getInputStream();
				} catch (FileNotFoundException ex) {
					break;
				} catch (IOException ex) {
					System.out.println("waiting (" + ex + ") - Iteration: "+j);
					Thread.sleep(3000);
					if (j++ < 100) {
						continue;
					}
				}
				break;
			}
		} catch (MalformedURLException ex) {
			Logger.getLogger(FelixClerezzaPlatformTest.class.getName()).log(
					Level.SEVERE, null, ex);
		}
		return j < 100;
	}

	private void runRequestThreads(final URL url) throws MalformedURLException,
			InterruptedException {
		RequestThread[] requestThread = new RequestThread[THREADS_COUNT];
		for (int i = 0; i < THREADS_COUNT; i++) {
			requestThread[i] = new RequestThread(url);
		}
		for (int i = 0; i < THREADS_COUNT; i++) {
			requestThread[i].start();
		}
		int successfulRequests = 0;
		for (int i = 0; i < THREADS_COUNT; i++) {
			requestThread[i].join();
			successfulRequests += requestThread[i].successfulRequests;
		}
		Assert.assertEquals(REQUESTS_PER_THREAD * THREADS_COUNT,
				successfulRequests);
	}

	private void registerRDFListRootResource() {
		final Dictionary<String, Object> jaxRsResourceProperty = new Hashtable<String, Object>();
		{
			jaxRsResourceProperty.put("javax.ws.rs", Boolean.TRUE);
			jaxRsResourceProperty.put("service.pid", RDFListRootResource.class.getName());
		}
		bundleContext.registerService(Object.class.getName(),
				new RDFListRootResource(), jaxRsResourceProperty);
	}

	static class RequestThread extends Thread {

		private URL url;
		int successfulRequests = 0;

		public RequestThread(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			try {
				for (int i = 0; i < REQUESTS_PER_THREAD; i++) {
					requestUrl(url);
					successfulRequests++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private static byte[] requestUrl(URL url) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		URLConnection urlConnection = url.openConnection();
		InputStream in = urlConnection.getInputStream();
		for (int ch = in.read(); ch != -1; ch = in.read()) {
			baos.write(ch);
		}
		return baos.toByteArray();
	}
}
