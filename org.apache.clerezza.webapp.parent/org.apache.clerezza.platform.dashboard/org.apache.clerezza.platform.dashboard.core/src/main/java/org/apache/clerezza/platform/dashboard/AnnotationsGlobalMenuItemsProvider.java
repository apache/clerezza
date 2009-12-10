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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.ws.rs.Path;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.jaxrs.extensions.prefixmanager.BundlePrefixManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>GlobalMenuItemsProvider</code> providing menu items from annotated
 * JAX-RS resources. This is an adaptor for menu-resources implemented before
 * the introduction of GlobalMenuItemsProvider, the service property approach is
 * to be considered as LEGACY.<p/>
 * This generates one menu item per JAX-RS root resource with a service property
 * "org.apache.clerezza.platform.dashboard.visible" set to true to its path annotation
 * and the label with value of the service property "dashBoardLabel" the menu item
 * will be added to the group specified by the service property "dashBoardGroupLabel".
 * The priority of an menu-item is defined by a service-property "dashBoardMenuOrder".
 *
 * 
 * @author mir
 */
@Component(enabled=true, immediate=true)
@Service(GlobalMenuItemsProvider.class)
@Reference(name="component",
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	policy=ReferencePolicy.DYNAMIC,
	referenceInterface=Object.class,
	target="(&(javax.ws.rs=true)(org.apache.clerezza.platform.dashboard.visible=true))")

public class AnnotationsGlobalMenuItemsProvider implements GlobalMenuItemsProvider {
	
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Reference
	private BundlePrefixManager prefixManager;

	private Map<ServiceReference, GlobalMenuItem> componentMenuItemMap =
			new HashMap<ServiceReference, GlobalMenuItem>();
	
	/**
	 * Lock used when changing the configuration.
	 */
	private static ReentrantReadWriteLock configLock = new ReentrantReadWriteLock();

	/**
	 * stores all <code>ServiceReference</code>s of component objects (where
	 * javax.ws.rs=true) that were bound before the JaxRsHandler was activated.
	 */
	Set<ServiceReference> componentReferenceStore = new HashSet<ServiceReference>();
	private ComponentContext componentContext;
	private Set<GlobalMenuItem> globalMenuItems = new HashSet<GlobalMenuItem>();

	@Override
	public Set<GlobalMenuItem> getMenuItems() {
		return globalMenuItems;
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
			if (componentContext != null) {
				addMenuItem(serviceReference);
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
			if (!componentReferenceStore.remove(serviceReference)) {
				removeMenuItem(serviceReference);
			}
		} finally {
			configLock.writeLock().unlock();
		}
	}
	
	
	protected void activate(ComponentContext cCtx) throws Exception {
		configLock.readLock().lock();
		try {
			this.componentContext = cCtx;
			for (ServiceReference serviceReference : componentReferenceStore) {
				addMenuItem(serviceReference);
			}
			componentReferenceStore.clear();
		} finally {
			configLock.readLock().unlock();
		}
	}

	protected void deactivate(ComponentContext cCtx) throws Exception {
		this.componentContext = null;
	}
	
	private void addMenuItem(ServiceReference reference) {
		if (jaxRsResourceHasLabel(reference)) {
			GlobalMenuItem item = getItemFromReference(reference);			
			globalMenuItems.add(item);
			componentMenuItemMap.put(reference, item);
		}
	}

	private void removeMenuItem(ServiceReference reference) {
		if (jaxRsResourceHasLabel(reference)) {
			globalMenuItems.remove(componentMenuItemMap.get(reference));
		}
	}

	private GlobalMenuItem getItemFromReference(ServiceReference reference) {
		String groupLabel = (String) reference.getProperty("dashBoardGroupLabel");
		Bundle bundle = reference.getBundle();
		Object component = componentContext.locateService("component", reference);
		final Class<?> clazz = component.getClass();
		final Path path = clazz.getAnnotation(Path.class);
		String combinedPath = prefixManager.getPrefix(bundle) + path.value();
		String label = (String) reference.getProperty("dashBoardLabel");
		int order = (Integer) reference
					.getProperty("dashBoardMenuOrder");
		int priority = Integer.MAX_VALUE - order;
		GlobalMenuItem item = new GlobalMenuItem(combinedPath,
				generateIdFromLabel(label), label, priority,
				generateIdFromLabel(groupLabel));
		return item;
	}
	
	private boolean jaxRsResourceHasLabel(ServiceReference reference) {
		return reference.getProperty("dashboardLabel") != null &&
			reference.getProperty("dashBoardGroupLabel") != null;
	}

	private String generateIdFromLabel(String label) {
		return label.replace(' ', '_');
	}
}