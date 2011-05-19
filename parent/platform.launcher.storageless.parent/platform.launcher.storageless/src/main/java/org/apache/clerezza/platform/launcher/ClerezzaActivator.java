/*
 *  Copyright (c) 2011 trialox.org (trialox AG, Switzerland).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.platform.launcher;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;

/**
 * OSGi Clerezza Launcher Bundle Activator.
 *
 * @author daniel
 */
public class ClerezzaActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("starting platform launcher");
		ServiceReference startLevelRef =
				context.getServiceReference(StartLevel.class.getName());
		StartLevel startLevel = (StartLevel) context.getService(startLevelRef);
		final int currentBundleStartLevel =
				startLevel.getBundleStartLevel(context.getBundle());
		startLevel.setStartLevel(currentBundleStartLevel);
		final int originalStartLevel = startLevel.getStartLevel();
		int nextLevel = currentBundleStartLevel + 1;
		Enumeration<URL> bundleJars =
				context.getBundle().findEntries("platform-bundles", "*.jar", true);
		Set<MavenArtifactDesc> artDescs = new HashSet<MavenArtifactDesc>();

		while (bundleJars.hasMoreElements()) {
			MavenArtifactDesc artDesc =
					MavenArtifactDesc.parseFromURL(bundleJars.nextElement());
			artDescs.add(artDesc);
		}
		ClerezzaApp.installBundles(context, artDescs, nextLevel);

		final int newStartLevel =
				originalStartLevel > nextLevel + 1 ? originalStartLevel : nextLevel + 1;
		startLevel.setStartLevel(newStartLevel);
		if (startLevel.getInitialBundleStartLevel() < nextLevel + 1) {
			startLevel.setInitialBundleStartLevel(nextLevel + 1);
		}
		System.out.println("uninstalling platform launcher");
		context.getBundle().uninstall();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}
}
