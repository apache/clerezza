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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Inject;
import org.osgi.framework.BundleContext;

import org.osgi.util.tracker.ServiceTracker;
import org.wymiwyg.wrhapi.Handler;

/**
 *
 * @author reto
 */
public abstract class AbstractServiceRootResourceTest {

	protected final static int testHttpPort = 8976;
	@Inject
	private BundleContext bundleContext;
	private Dictionary<String, Object> jaxRsResourceProperty = new Hashtable<String, Object>();

	{
		jaxRsResourceProperty.put("javax.ws.rs", Boolean.TRUE);
		jaxRsResourceProperty.put("service.pid", SimpleRootResource.class.getName());
	}


	@Before
	public void registerRootResource() throws Exception {
		Thread.sleep(20 * 1000);
		waitForWebserver();
		waitFor(Handler.class);
		bundleContext.registerService(Object.class.getName(),
				new SimpleRootResource(), jaxRsResourceProperty);
		Thread.sleep(3000);
	}

	@Test
	public void getHello() throws Exception {
		URL serverURL = new URL("http://localhost:" + testHttpPort + "/foo");
		HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
		Assert.assertEquals("text/plain", connection.getHeaderField("Content-Type"));
		Reader reader = new InputStreamReader(connection.getInputStream());
		StringWriter stringWriter = new StringWriter();

		for (int ch = reader.read(); ch != -1; ch = reader.read()) {
			stringWriter.write(ch);
		}
		Assert.assertEquals(SimpleRootResource.greeting, stringWriter.toString());
	}

	private void waitFor(Class<?> aClass) throws InterruptedException {
		System.out.println("waiting for a " + aClass);
		ServiceTracker tracker = new ServiceTracker(bundleContext,
				aClass.getName(), null);
		tracker.open();
		Object service = tracker.waitForService(10000);
	}

	private void waitForWebserver() {
		try {
			URL serverURL = new URL("http://localhost:" + testHttpPort + "/");
			boolean succeded = false;
			while (!succeded) {
				try {
					serverURL.openConnection().getInputStream();
				} catch (FileNotFoundException ex) {
					break;
				} catch (IOException ex) {
					System.out.println("waiting ("+ex+")");
					continue;
				}
				succeded = true;
			}
		} catch (MalformedURLException ex) {
			Logger.getLogger(AbstractServiceRootResourceTest.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
}
