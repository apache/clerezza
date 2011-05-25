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
package org.apache.clerezza.platform.security;

import java.security.AccessController;
import java.security.AllPermission;
import java.security.Policy;
import java.security.Principal;
import java.security.PrivilegedAction;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.PackagePermission;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.condpermadmin.BundleLocationCondition;
import org.osgi.service.condpermadmin.ConditionInfo;
import org.osgi.service.condpermadmin.ConditionalPermissionAdmin;
import org.osgi.service.condpermadmin.ConditionalPermissionInfo;
import org.osgi.service.permissionadmin.PermissionInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.platform.security.conditions.NotBundleLocationCondition;
import org.apache.clerezza.rdf.core.MGraph;

/**
 * Manages permissions to be given to bundles and {@link Principal}s.
 * Permission information is obtained from the system graph.
 * The system graph is identified by the URI urn:x-localinstance:/system.graph.
 *
 * When this component is activated, it gives all permissions to itself, to
 * System Bundle, and to all bundles whose location starts with slinginstall.
 *
 * When a new bundle is installed, this component gets notified through
 * a listener and give this newly installed bundle its permissions as defined
 * in the system graph.
 *
 * @author: hasan, clemens
 */

@Component
public class BundlePermissionManager implements BundleListener {

	final Logger logger = LoggerFactory.getLogger(BundlePermissionManager.class);

	
	@Reference
	private ConditionalPermissionAdmin cpa;
	private static final String ALL_EXCEPT_USER_BUNDLES_CPINAME = "allExceptUserBundles";
	private static final String PACKAGE_EXPORT_CPINAME = "package export";
	private static final Collection<String> NON_USER_CPI_NAMES = new HashSet<String>();


	@Reference(target=SystemConfig.SYSTEM_GRAPH_FILTER)
	private MGraph systemGraph;

	static {
		NON_USER_CPI_NAMES.add(ALL_EXCEPT_USER_BUNDLES_CPINAME);
		NON_USER_CPI_NAMES.add(PACKAGE_EXPORT_CPINAME);
	}

	private PermissionDefinitions permissionDefinitions;
	

	/**
	 *
	 * @param cCtx
	 * @throws java.lang.Exception
	 */
	protected void activate(final ComponentContext cCtx) throws Exception {
		logger.debug("Activating PermissionManager");

		// give the bundle itself AllPermission
		// give the system bundle and the sling bundles AllPermission
		AccessController.doPrivileged(new PrivilegedAction() {

			@Override
			public Object run() {
				cpa.setConditionalPermissionInfo(ALL_EXCEPT_USER_BUNDLES_CPINAME,
						new ConditionInfo[]{
							new ConditionInfo(
							NotBundleLocationCondition.class.getName(),
							new String[]{"userbundle:*"})
						},
						new PermissionInfo[]{
							new PermissionInfo(
							AllPermission.class.getName(), "", "")
						});
				return null; // nothing to return
			}
		});

		assignAllBundlePermissions();

		cCtx.getBundleContext().addBundleListener(this);

		logger.debug("Permissions assigned");

		this.permissionDefinitions = new PermissionDefinitions(systemGraph);
		deleteUserBundlePermissions();
		for (int i = 0; i < cCtx.getBundleContext().getBundles().length; i++) {
			String bundleLocation = cCtx.getBundleContext().getBundles()[i].getLocation();

			if (bundleLocation.startsWith("userbundle:")) {
				updateFromSystemGraph(bundleLocation);
			}
		}
	}



	protected void deactivate(final ComponentContext cCtx) throws Exception {
		logger.debug("Permission manager being deactivated");
		cCtx.getBundleContext().removeBundleListener(this);
	}

	/**
	 * Give all bundles package permission
	 * may be extended with more permissions in the future
	 */
	private void assignAllBundlePermissions() {
		logger.debug("Give PackagePermission to all bundles");
		cpa.setConditionalPermissionInfo(PACKAGE_EXPORT_CPINAME,
				new ConditionInfo[]{
					null
				},
				new PermissionInfo[]{
					new PermissionInfo(
					PackagePermission.class.getName(), "*", "export")
				});
	}

	/**
	 * Update the permissions from the graph.
	 * @param bundleLocation
	 */
	private void updateFromSystemGraph(String bundleLocation) {
		logger.debug("Updating from system graph");
		logger.debug("location: {}, cpa: {}", bundleLocation, cpa);
		cpa.setConditionalPermissionInfo(bundleLocation,
				new ConditionInfo[]{
					new ConditionInfo(
					BundleLocationCondition.class.getName(),
					new String[]{bundleLocation})
				}, permissionDefinitions.retrievePermissions(bundleLocation));
	}

	/**
	 * Delete all permissions from locally installed bundles
	 */
	private void deleteUserBundlePermissions() {
		Enumeration<ConditionalPermissionInfo> cpis =
				cpa.getConditionalPermissionInfos();

		while (cpis.hasMoreElements()) {
			ConditionalPermissionInfo cpi = cpis.nextElement();
			if (!NON_USER_CPI_NAMES.contains(cpi.getName())) {
				cpi.delete();
			}

		}
	}

	/**
	 * The function is called whenever a bundle changes its state. According to
	 * the state, the bundle recieves permissions or loses all permissions.
	 * @param event	A specific <code>BundleEvent</code> according to the bundle state
	 */
	@Override
	public void bundleChanged(BundleEvent event) {
		logger.debug("Got bundle event {}", event.getType());
		final String bundleLocation = event.getBundle().getLocation();
		switch (event.getType()) {
			// give the bundle permissions according to the system graph
			case BundleEvent.INSTALLED:
				logger.debug("Bundle INSTALLED: {}", bundleLocation);
				if (bundleLocation.startsWith("userbundle:")) {
					updateFromSystemGraph(bundleLocation);
				}
				break;
			// delete all permissions of this bundle
			case BundleEvent.UNINSTALLED:
				logger.debug("Bundle UNINSTALLED: {}", bundleLocation);
				cpa.getConditionalPermissionInfo(bundleLocation).delete();
				break;
		}
	}
}
