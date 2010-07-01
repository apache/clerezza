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

package org.apache.clerezza.permissiondescriptions;

import java.net.URL;
import java.security.Permission;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.clerezza.utils.IteratorMerger;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This binds all implementations of <code>PermissionDescriptionsProvider</code>,
 * and gathers their <code>PermissionDescription</code>s. Furthermore the gatherer
 * scans all activaded bundles for <code>Permission</code>s which are annonated with the 
 * <code>PermissionInfo</code> annotation and generates <code>PermissionDescription</code>s
 * for them. This service provides methods to retrieve the gathered
 * <code>PermissionDescription</code>s and also methods to retrieve all unannotated
 * <code>Permission</code> found in the activated bundles. If new bundles are
 * started then they are also scanned.
 *
 * @author mir
 */
@Component(immediate=true)
@Service(PermissionGatherer.class)
@Reference(name="permissionProvider", policy=ReferencePolicy.DYNAMIC,
	referenceInterface=PermissionDescriptionsProvider.class, cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE)
public class PermissionGatherer implements BundleListener {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<Bundle, Collection<Class<? extends Permission>>> bundle2PermissionClassesMap =
			Collections.synchronizedMap(new HashMap<Bundle, Collection<Class<? extends Permission>>>());
	private Map<Bundle, Collection<PermissionDescripton>> bundle2PermissionDescriptorsMap =
			Collections.synchronizedMap(new HashMap<Bundle, Collection<PermissionDescripton>>());
	
	private ComponentContext componentContext;
	
	/**
	 * stores all <code>ServiceReference</code>s of <code>PermissionDescriptionsProvider</code>s
	 * that were bound before the PermissionGatherer was activated.
	 */
	private Set<ServiceReference> serviceReferenceStore = new HashSet<ServiceReference>();


	public Map<Bundle, Collection<PermissionDescripton>> getPermissionDescriptorsPerBundles() {
		return Collections.unmodifiableMap(bundle2PermissionDescriptorsMap);
	}

	public Iterator<PermissionDescripton> getAllPermissionDescriptors() {
		return new IteratorMerger<PermissionDescripton>(bundle2PermissionDescriptorsMap.values());
	}

	public Map<Bundle, Collection<Class<? extends Permission>>> getPermissionsPerBundles() {
		return Collections.unmodifiableMap(bundle2PermissionClassesMap);
	}

	public Iterator<Class<? extends Permission>> getAllPermissions() {
		return new IteratorMerger<Class<? extends Permission>>(
				bundle2PermissionClassesMap.values());
	}

	protected void activate(final ComponentContext componentContext) {
		this.componentContext = componentContext;
		registerFromServiceReferenceStore();
		componentContext.getBundleContext().addBundleListener(this);
		logger.debug("Start registering permissions from activated bundles");
		registerPermissionsFromActivatedBundles(componentContext);
		logger.debug("Registered permissions from activated bundles");
	}

	synchronized private void registerFromServiceReferenceStore() {
		if (componentContext != null) {

			for (ServiceReference ref : serviceReferenceStore) {
				this.registerPermissionDescriptorsProvider(ref);
			}
			serviceReferenceStore.clear();
		}
	}

	synchronized protected void bindPermissionProvider(ServiceReference serviceReference) {
		if (componentContext != null) {
			registerPermissionDescriptorsProvider(serviceReference);
		} else {
			serviceReferenceStore.add(serviceReference);
		}

	}

	synchronized protected void unbindPermissionProvider(ServiceReference serviceReference) {

		if (!serviceReferenceStore.remove(serviceReference)) {
			PermissionDescriptionsProvider permissionProvider = (PermissionDescriptionsProvider)
				componentContext.locateService("permissionProvider", serviceReference);
			Bundle bundle = serviceReference.getBundle();

			Collection<PermissionDescripton> permissionDescriptiors =
					bundle2PermissionDescriptorsMap.get(bundle);
			if (permissionDescriptiors != null) {
				permissionDescriptiors.removeAll(permissionProvider.getPermissionDescriptors());
				if (permissionDescriptiors.isEmpty()) {
					bundle2PermissionDescriptorsMap.remove(bundle);
				}
			}
		}

	}

	private void registerPermissionDescriptorsProvider(ServiceReference serviceReference) {
		PermissionDescriptionsProvider permissionProvider = (PermissionDescriptionsProvider)
				componentContext.locateService("permissionProvider", serviceReference);
		if (permissionProvider == null) {
			return;
		}
		Bundle bundle = serviceReference.getBundle();
		Collection<PermissionDescripton> permissionDescriptiors =
				bundle2PermissionDescriptorsMap.get(bundle);
		if (permissionDescriptiors == null) {
			permissionDescriptiors = new HashSet<PermissionDescripton>();
		}
		permissionDescriptiors.addAll(permissionProvider.getPermissionDescriptors());
		bundle2PermissionDescriptorsMap.put(bundle, permissionDescriptiors);

	}

	protected void deactivate(final ComponentContext componentContext) {
		componentContext.getBundleContext().removeBundleListener(this);
		bundle2PermissionClassesMap.clear();
		bundle2PermissionDescriptorsMap.clear();
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		Bundle bundle = event.getBundle();
		switch (event.getType()) {
			case BundleEvent.STARTED:
				registerPermissions(bundle);
				break;
			case BundleEvent.STOPPED:
				unregisterPermissions(bundle);
				break;
		}
	}

	private void registerPermissionsFromActivatedBundles(ComponentContext componentContext) {
		Bundle[] bundles = componentContext.getBundleContext().getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getState() == Bundle.ACTIVE) {
				registerPermissions(bundle);
			}
		}
	}

	private void registerPermissions(Bundle bundle) {
		Enumeration<URL> classUrls = bundle.findEntries("/", "*.class", true);
		if (classUrls == null) {
			return;
		}
		Set<Class<? extends Permission>> permissionClassesSet = new HashSet<Class<? extends Permission>>();
		Set<PermissionDescripton> newPermissionDescriptors =
				new HashSet<PermissionDescripton>();
		while (classUrls.hasMoreElements()) {
			URL url = classUrls.nextElement();			
			try {
				String className = url.getPath();
				int indexOfLastDot = className.lastIndexOf(".");
				className = className.replaceAll("/", ".").substring(1, indexOfLastDot);
				Class<?> clazz = bundle.loadClass(className);
				if (Permission.class.isAssignableFrom(clazz)) {
					PermissionInfo permissionInfo = clazz.getAnnotation(PermissionInfo.class);
					if (permissionInfo != null) {
						newPermissionDescriptors.add(new PermissionDescripton(permissionInfo.value(),
								permissionInfo.description(), clazz.getResource(permissionInfo.icon()),
								(Class<? extends Permission>) clazz,
								getJavaPermissionString(clazz)));
					} else {
						permissionClassesSet.add((Class<? extends Permission>) clazz);
					}
				}
			} catch (Exception ex) {
			} catch (NoClassDefFoundError err) {}
			
		}
		if (!permissionClassesSet.isEmpty()) {
			bundle2PermissionClassesMap.put(bundle, permissionClassesSet);
		}
		if (!newPermissionDescriptors.isEmpty()) {

			Collection<PermissionDescripton> permissionDescriptiors =
					bundle2PermissionDescriptorsMap.get(bundle);
			if (permissionDescriptiors == null) {
				permissionDescriptiors = newPermissionDescriptors;
			} else {
				permissionDescriptiors.addAll(newPermissionDescriptors);
			}
			bundle2PermissionDescriptorsMap.put(bundle, permissionDescriptiors);
		}
	}

	private void unregisterPermissions(Bundle bundle) {
		bundle2PermissionClassesMap.remove(bundle);
		bundle2PermissionDescriptorsMap.remove(bundle);
	}

	private String getJavaPermissionString(Class clazz) {
		return "(" + clazz.getName() + " \"\" \"\")";
	}
}
