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
package org.apache.clerezza.platform.dashboard.blackbox.osgi;

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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.clerezza.platform.dashboard.ontologies.GLOBALMENU;
import org.apache.clerezza.platform.typerendering.UserContextProvider;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Inject;
import org.osgi.framework.BundleContext;

import org.osgi.util.tracker.ServiceTracker;
import org.wymiwyg.wrhapi.Handler;

/**
 *
 * @author mir, reto
 */
@RunWith(JUnit4TestRunner.class)
public class FelixContextualMenuGeneratorTest {
	
	@Configuration
	public static Option[] configuration() {
		return options(
				mavenConfiguration(),
				dsProfile(),
				configProfile(),
				webProfile(),
				junitBundles(),
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
	public void registerServices() throws Exception {
		System.out.println("@Before");
		webServerExist = waitForWebserver();
		if (webServerExist) {
			waitFor(Handler.class, 10000);
			bundleContext.registerService(GlobalMenuItemsProvider.class.getName(),
					new GlobalMenuItemsProviderA(), null);
			Thread.sleep(10000);
		}
	}

	@Test
	public void checkMenu() throws Exception {
		Assert.assertTrue(webServerExist);
		UserContextProvider contextProvider = (UserContextProvider)
				waitFor(UserContextProvider.class, 300000);
		Assert.assertTrue(contextProvider != null);
		GraphNode node = new GraphNode(new BNode(), new SimpleMGraph());
		contextProvider.addUserContext(node);
		Iterator<Resource> iter = node.getObjects(GLOBALMENU.globalMenu);
		Assert.assertTrue(iter.hasNext());
		TripleCollection graph = node.getGraph();
		RdfList list = new RdfList((NonLiteral)iter.next(), graph);
		Assert.assertEquals(3, list.size());
		Assert.assertEquals(GlobalMenuItemsProviderA.groupALabel,
				getLabel(graph, list.get(0)));
		Assert.assertEquals(GlobalMenuItemsProviderA.groupCLabel,
				getLabel(graph, list.get(1)));
		Assert.assertEquals(GlobalMenuItemsProviderA.implicitGroupBLabel,
				getLabel(graph, list.get(2)));		
		Assert.assertEquals(GLOBALMENU.Menu, getRdfType(graph, list.get(0)));
		Assert.assertEquals(GLOBALMENU.MenuItem, getRdfType(graph, list.get(1)));		
		Assert.assertEquals(GlobalMenuItemsProviderA.groupAPath,
				getPath(graph, list.get(0)));
		RdfList children = getChildren(graph, list.get(0));
		Assert.assertEquals(2, children.size());
		Assert.assertEquals(GlobalMenuItemsProviderA.itemA2Label,
				getLabel(graph, children.get(0)));
		Assert.assertEquals(GlobalMenuItemsProviderA.itemA1Label,
				getLabel(graph, children.get(1)));
		Assert.assertEquals(GlobalMenuItemsProviderA.itemA2Path,
				getPath(graph, children.get(0)));
	}

	private String getLabel(TripleCollection graph, Resource res) {
		Iterator<Triple> labels = graph.filter((NonLiteral) res, RDFS.label, null);
		if (labels.hasNext()) {
			return ((Literal) labels.next().getObject()).getLexicalForm();
		} else {
			return null;
		}
	}

	private String getPath(TripleCollection graph, Resource res) {
		Resource path = graph.filter((NonLiteral) res,
				GLOBALMENU.path, null).next().getObject();
		return ((Literal) path).getLexicalForm();
	}

	private RdfList getChildren(TripleCollection graph, Resource res) {
		NonLiteral children = (NonLiteral) graph.filter((NonLiteral) res,
				GLOBALMENU.children, null).next().getObject();
		return new RdfList(children, graph);
	}

	private UriRef getRdfType(TripleCollection graph, Resource res) {
		return (UriRef) graph.filter((NonLiteral) res, RDF.type, null).next().
				getObject();
	}

	private Object waitFor(Class<?> aClass, long timeout)
			throws InterruptedException {
		System.out.println("waiting for a " + aClass);
		ServiceTracker tracker = new ServiceTracker(bundleContext,
				aClass.getName(), null);
		tracker.open();
		Object service = tracker.waitForService(timeout);
		return service;
	}

	private boolean waitForWebserver() throws InterruptedException {
		int j = 0;
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
					Thread.sleep(3000);
					if (j++ < 100) {
						continue;
					}
				}
				succeded = true;
			}
		} catch (MalformedURLException ex) {
			Logger.getLogger(FelixContextualMenuGeneratorTest.class.getName())
					.log(Level.SEVERE, null, ex);
		}
		return j < 100;
	}
}
