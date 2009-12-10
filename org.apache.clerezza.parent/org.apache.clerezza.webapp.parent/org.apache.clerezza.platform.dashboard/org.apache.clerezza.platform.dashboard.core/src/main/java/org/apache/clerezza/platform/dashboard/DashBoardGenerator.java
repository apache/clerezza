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
package org.apache.clerezza.platform.dashboard;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.Path;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.jaxrs.extensions.prefixmanager.BundlePrefixManager;
import org.apache.clerezza.platform.dashboard.ontology.DASHBOARD;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;

/**
 * @deprecated obsoleted as the menu is now attached to the context node which
 *		is provided by the <code>ContextualMenuGenerator</code>.
 * @author tio
 */
@Component(enabled=true, immediate=true)
@References({
	@Reference(name="component",
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
		policy=ReferencePolicy.DYNAMIC,
		referenceInterface=Object.class,
		target="(&(javax.ws.rs=true)(org.apache.clerezza.platform.dashboard.visible=true))"),
	@Reference(name="bundlePrefixManager",
		cardinality=ReferenceCardinality.OPTIONAL_UNARY,
		policy=ReferencePolicy.DYNAMIC,
		referenceInterface=BundlePrefixManager.class)
})
@Deprecated
public class DashBoardGenerator {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	final static UriRef GRAPH_NAME = new UriRef(
			"http://tpf.localhost/menu.graph");
	final static UriRef DASHBOARD_URI = new UriRef(
			"http://clerezza.org/dashboard/");
	@Reference
	private TcManager tcMgr;
	private ComponentContext componentContext;
	private BundlePrefixManager prefixManager;

	/**
	 * Lock used when changing the configuration.
	 */
	private static ReentrantReadWriteLock configLock = new ReentrantReadWriteLock();

	/**
	 * stores all <code>ServiceReference</code>s of component objects (where
	 * javax.ws.rs=true) that were bound before the JaxRsHandler was activated.
	 */
	Set<ServiceReference> componentReferenceStore = new HashSet<ServiceReference>();

	protected void activate(ComponentContext cCtx) throws Exception {
		logger.debug("Activating DashBoard");
		configLock.writeLock().lock();
		MGraph mGraph = null;
		try {
			this.componentContext = cCtx;
			registerFromStores();
			mGraph = tcMgr.getMGraph(GRAPH_NAME);
			mGraph.clear();
		} catch (NoSuchEntityException exp) {
			mGraph = tcMgr.createMGraph(GRAPH_NAME);
		} finally {
			mGraph.add(new TripleImpl(DASHBOARD_URI, RDF.type,
					DASHBOARD.DashBoardMenu));
			mGraph.add(new TripleImpl(DASHBOARD_URI, DASHBOARD.hasLabel,
					new PlainLiteralImpl("Dashboard")));
			mGraph.add(new TripleImpl(DASHBOARD_URI, DASHBOARD.hasRelativeUrl,
					new PlainLiteralImpl("/dashboard")));
			configLock.writeLock().unlock();
		}
	}

	protected void deactivate(ComponentContext cCtx) throws Exception {
		tcMgr.getMGraph(GRAPH_NAME).clear();

	}

	private void registerFromStores() {
		if (componentContext != null) {
			for (ServiceReference compRef : componentReferenceStore) {
				this.addServiceToVirtualGraph(compRef);
			}
			componentReferenceStore.clear();
		}
	}

	private boolean jaxRsResourceHasLabel(ServiceReference reference) {
		return reference.getProperty("dashboardLabel") != null &&
			reference.getProperty("dashBoardGroupLabel") != null;
	}

	private void addServiceToVirtualGraph(ServiceReference reference) {
		if (jaxRsResourceHasLabel(reference)) {
			MGraph mGraph = tcMgr.getMGraph(GRAPH_NAME);
			NonLiteral dashBoardGroup = retrieveDashBoardGroup(
					new PlainLiteralImpl((String) reference.getProperty("dashBoardGroupLabel")),
					mGraph);
			List<Resource> list = new RdfList(dashBoardGroup, mGraph);
			Bundle bundle = reference.getBundle();
			Object component = componentContext.locateService("component",
					reference);
			final Class<?> clazz = component.getClass();
			final Path path = clazz.getAnnotation(Path.class);
			String label = (String) reference.getProperty("dashBoardLabel");
			UriRef bundleLocation = new UriRef("http://clerezza.org/"
					+ label.trim().replace(" ", "-"));
			mGraph.add(new TripleImpl(bundleLocation, DASHBOARD.hasLabel,
					new PlainLiteralImpl(label)));
			mGraph.add(new TripleImpl(bundleLocation, DASHBOARD.hasRelativeUrl,
					new PlainLiteralImpl(prefixManager.getPrefix(bundle)
							+ path.value())));
			Integer order = (Integer) reference
					.getProperty("dashBoardMenuOrder");
			if (order != null && order < list.size()) {
				list.add(order, bundleLocation);
			} else {
				list.add(bundleLocation);
			}
		}
	}

	protected void bindBundlePrefixManager(BundlePrefixManager prefixManager) {
		logger.info("Binding bundle prefix manager");
		configLock.writeLock().lock();
		try {
			this.prefixManager = prefixManager;
			registerFromStores();
		} finally {
			configLock.writeLock().unlock();
		}
	}

	protected void unbindBundlePrefixManager(BundlePrefixManager prefixManager) {
		logger.info("Unbinding bundle prefix manager");
		configLock.writeLock().lock();
		try {
			this.prefixManager = null;
		} finally {
			configLock.writeLock().unlock();
		}
	}

	/**
	 * Binds the specified JAX-RS root-resource or provider.
	 *
	 * @param component
	 *            The new JAX-RS component to bind.
	 */
	protected void bindComponent(ServiceReference serviceReference) {
		logger.info("Bind component of bundle {}", serviceReference.getBundle()
				.getSymbolicName());
		configLock.writeLock().lock();
		try {
			if (componentContext != null && prefixManager != null) {
				addServiceToVirtualGraph(serviceReference);
			} else {
				componentReferenceStore.add(serviceReference);
			}
		} finally {
			configLock.writeLock().unlock();
		}
	}

	protected void unbindComponent(ServiceReference serviceReference) {
		configLock.writeLock().lock();
		try {
			if (!componentReferenceStore.remove(serviceReference)
					&& tcMgr != null) {

				removeServiceFromVirtualGraph(serviceReference);
			}
		} finally {
			configLock.writeLock().unlock();
		}
	}

	private void removeServiceFromVirtualGraph(ServiceReference reference) {
		MGraph mGraph = tcMgr.getMGraph(GRAPH_NAME);
		Literal dashBoardGroupLabel = new PlainLiteralImpl((String) reference.getProperty("dashBoardGroupLabel"));
		NonLiteral dashBoardGroup = retrieveDashBoardGroup(dashBoardGroupLabel, mGraph);
		List<Resource> list = new RdfList(dashBoardGroup, mGraph);
		String label = (String) reference.getProperty("dashBoardLabel");
		UriRef bundleLocation = new UriRef("http://clerezza.org/"
				+ label.trim().replace(" ", "-"));
		list.remove(bundleLocation);
		GraphNode node = new GraphNode(bundleLocation, mGraph);
		node.deleteProperties(DASHBOARD.hasLabel);
		node.deleteProperties(DASHBOARD.hasRelativeUrl);
	}

	private NonLiteral retrieveDashBoardGroup(Literal dashBoardGroupLabel,
			MGraph mGraph) {
		Iterator<Triple> iter = mGraph.filter(null,
				DASHBOARD.hasDashBoardGroupLabel, dashBoardGroupLabel);
		NonLiteral dashBoardGroup;
		if (!iter.hasNext()) {
			dashBoardGroup = new BNode();
			mGraph.add(new TripleImpl(dashBoardGroup,
					DASHBOARD.hasDashBoardGroupLabel, dashBoardGroupLabel));
			mGraph.add(new TripleImpl(dashBoardGroup, RDF.type, RDF.List));
			List<Resource> list = new RdfList(DASHBOARD_URI, mGraph);
			list.add(dashBoardGroup);
		} else {
			dashBoardGroup = iter.next().getSubject();
		}
		return dashBoardGroup;
	}
}
