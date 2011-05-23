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
package org.apache.clerezza.platform.launcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;
import org.wymiwyg.commons.util.arguments.AnnotatedInterfaceArguments;
import org.wymiwyg.commons.util.arguments.ArgumentHandler;
import org.wymiwyg.commons.util.arguments.ArgumentProcessor;
import org.wymiwyg.commons.util.arguments.InvalidArgumentsException;
import org.wymiwyg.commons.util.dirbrowser.PathNameFilter;
import org.wymiwyg.commons.util.dirbrowser.PathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNodeFactory;

/**
 * Clerezza Launcher Application.
 *
 *
 * @author daniel
 */
public class ClerezzaApp {

	private Felix felixInstance = null;
	private int exitCode = -1;
	private List<ShutdownListener> shutdownListeners =
			new ArrayList<ShutdownListener>(1);

	/**
	 * Install bundles after core framework launch.
	 *
	 * @param context	the bundle context.
	 * @param artDescs	the bundles to install.
	 * @param nextLevel	the start level to change to after the installation.
	 * 
	 * @return	the installed bundles.
	 *
	 */
	static Set<Bundle> installBundles(BundleContext context,
			Collection<MavenArtifactDesc> artDescs,
			int nextLevel) throws IOException, BundleException {

		Set<Bundle> installedBundles = new HashSet<Bundle>();
		for (MavenArtifactDesc artDesc : artDescs) {
			try {
				final Bundle installedBundle = context.installBundle(artDesc.toString(), artDesc.getInputStream());
				installedBundles.add(installedBundle);
			} catch (BundleException e) {
				System.out.println(e.toString());
			}
		}

		ServiceReference startLevelRef =
				context.getServiceReference(StartLevel.class.getName());
		StartLevel startLevel = (StartLevel) context.getService(startLevelRef);
		for (Bundle bundle : installedBundles) {
			startLevel.setBundleStartLevel(bundle, nextLevel);
		}
		return installedBundles;
	}

	/**
	 * Set Clerezza Instance system exit code.
	 *
	 * @param code	the code to set.
	 */
	public void setExitCode(int code) {
		exitCode = code;
	}

	/**
	 * Get Clerezza Instance system exit code.
	 *
	 * @return	the exit code.
	 */
	public int getExitCode() {
		return exitCode;
	}

	/**
	 * Add a shutdown listener.
	 * 
	 * @param shutdownListener	the shutdown listener.
	 */
	public void addShutdownListener(ShutdownListener shutdownListener) {
		shutdownListeners.add(shutdownListener);
	}

	/**
	 * Remove a shutdown Listener.
	 *
	 * @param shutdownListener	the shutdown listener.
	 */
	public void removeShutdownListener(ShutdownListener shutdownListener) {
		shutdownListeners.remove(shutdownListener);
	}

	/**
	 * Start this Clerezza Instance.
	 *
	 * Starts the Felix framework and installs CLerezza Bundles.
	 *
	 * This method exits after installing all bundles.
	 * It does not wait for the bundle installations to complete.
	 *
	 * @param args	Command line arguments.
	 */
	public void start(String... args) throws Throwable {
		LauncherArguments arguments;
		try {
			final ArgumentHandler argumentHandler = new ArgumentHandler(args);
			arguments = argumentHandler.getInstance(LauncherArguments.class);
			argumentHandler.processArguments(new ArgumentProcessor() {

				@Override
				public void process(List<String> remaining) throws InvalidArgumentsException {
					if (remaining.size() > 0) {
						throw new InvalidArgumentsException("The following arguments could not be understood: " + remaining);
					}
				}
			});
		} catch (InvalidArgumentsException e) {
			System.out.println(e.getMessage());
			showUsage();
			return;
		}
		if (arguments.getHelp()) {
			showUsage();
			return;
		}

		Properties configProps = getConfigProps(arguments);

		Policy.setPolicy(new Policy() {

			@Override
			public PermissionCollection getPermissions(ProtectionDomain domain) {
				PermissionCollection result = new Permissions();
				result.add(new AllPermission());
				return result;
			}
		});
		System.setSecurityManager(new SecurityManager());
		felixInstance = new Felix(configProps);
		System.out.println("starting felix");
		felixInstance.start();
		final String revertParam = arguments.getRevertParam();
		final PathNode bundlesRoot = PathNodeFactory.getPathNode(Main.class.getResource("/bundles"));
		final BundleContext bundleContext = felixInstance.getBundleContext();
		installBundlesForStartLevels(bundleContext, bundlesRoot, revertParam);
	}

	/**
	 * Stop this Clerezza instance.
	 *
	 * This method does not wait for the shutdown to complete before it exits.
	 */
	public void stop() throws Throwable {
		if (felixInstance != null) {
			felixInstance.stop();
		}
	}

	/**
	 * Wait for this Clerezza instance to shut down.
	 *
	 * After shut down all shutdown listeners are notified.
	 */
	public void waitForStop() throws Throwable {
		FrameworkEvent event = null;
		try {
			if (felixInstance != null) {
				event = felixInstance.waitForStop(0);
			}
			setExitCode(0);
		} catch (Throwable t) {
			setExitCode(-1);
			event = new FrameworkEvent(FrameworkEvent.ERROR, null, t);
			throw t;
		} finally {
			notifyShutdownListeners(event);
		}
	}

	private void notifyShutdownListeners(FrameworkEvent event) {
		for (ShutdownListener shutdownListener : shutdownListeners) {
			shutdownListener.notify(event);
		}
	}

	private void installBundlesForStartLevels(final BundleContext bundleContext,
			final PathNode bundlesRoot, final String revertParam) throws IOException, BundleException {
		final String startlevelpathprefix = "startlevel-";
		final String[] startLevelePaths = bundlesRoot.list(new PathNameFilter() {

			@Override
			public boolean accept(PathNode dir, String name) {
				return name.startsWith(startlevelpathprefix);
			}
		});
		Arrays.sort(startLevelePaths);
		final Bundle[] installedBundles = bundleContext.getBundles();
		final Set<Bundle> newlyInstalledBundles = new HashSet<Bundle>();
		byte startLevel = 0;
		for (String startLevelPath : startLevelePaths) {
			startLevel = Byte.parseByte(
					startLevelPath.substring(startlevelpathprefix.length(),
					startLevelPath.length() - 1));
			final PathNode startLevelPathNode = bundlesRoot.getSubPath(startLevelPath);
			Set<MavenArtifactDesc> artDescs = getArtDescsFrom(startLevelPathNode);
			if (revertParam != null) {
				artDescs = getRevertArtifacts(revertParam, artDescs, installedBundles);
			}
			if (!alreadyInstalled(artDescs, installedBundles) || revertParam != null) {
				newlyInstalledBundles.addAll(installBundles(bundleContext, new TreeSet(artDescs), startLevel));
				System.out.println("level " + startLevel + " bundles installed");
			}
		}

		for (Bundle bundle : newlyInstalledBundles) {
			try {
				bundle.start();
			} catch (BundleException e) {
				System.out.println("Exception installing Bundle " + bundle + ": " + e.toString());
			}
		}

		ServiceReference startLevelRef =
				bundleContext.getServiceReference(StartLevel.class.getName());
		StartLevel startLevelService = (StartLevel) bundleContext.getService(startLevelRef);
		startLevelService.setInitialBundleStartLevel(startLevel + 1);
		startLevelService.setStartLevel(startLevel + 1);
	}

	private Set<MavenArtifactDesc> getArtDescsFrom(PathNode pathNode) {
		Set<MavenArtifactDesc> result = new HashSet<MavenArtifactDesc>();
		List<PathNode> jarPaths = getJarPaths(pathNode);
		for (PathNode jarPath : jarPaths) {
			result.add(MavenArtifactDesc.parseFromPath(jarPath));
		}

		return result;
	}

	private List<PathNode> getJarPaths(PathNode pathNode) {
		List<PathNode> result = new ArrayList<PathNode>();
		for (String childName : pathNode.list()) {
			PathNode childNode = pathNode.getSubPath(childName);
			if ((!childNode.isDirectory()) && (childName.endsWith(".jar"))) {
				result.add(childNode);
			} else {
				for (PathNode subPath : getJarPaths(childNode)) {
					result.add(subPath);
				}
			}
		}
		return result;
	}

	/**
	 * Returns true if a bundle is found, whose location starts with a short URI
	 * ("mvn:[groupId]/[artifactId]") of a Maven Artifact specified.
	 *
	 * Returns false iff none of the specified Maven Artifacts is installed.
	 *
	 * @param artDescs MavenArtifacts to be checked if a corresponding bundle is
	 *		already installed
	 * @param installedBundles Bundles installed in the framework
	 * @return
	 */
	private boolean alreadyInstalled(Set<MavenArtifactDesc> artDescs,
			Bundle[] installedBundles) {
		for (int i = 0; i < installedBundles.length; i++) {
			String bundleLocation = installedBundles[i].getLocation();
			for (Iterator<MavenArtifactDesc> it = artDescs.iterator(); it.hasNext();) {
				MavenArtifactDesc mavenArtifactDesc = it.next();
				if (bundleLocation.matches(mavenArtifactDesc.getShortUri() + "/.*[0-9].*")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the Maven Artifacts that have to be reverted to default.
	 * If the revertParam equals "all", then all platform bundles already installed
	 * are uninstalled and the returned <code>Set</code> contains all
	 * Maven Artifacts.
	 * If the revertParam is a regular expression, then the returned <code>Set</code>
	 * contains all Maven Artifacts, whose short URI matches the expression. If
	 * a corresponding bundle is installed, then it is uninstalled.
	 * If the revertParam equals "missing" then the returned <code>Set</code>
	 * contains all Maven Artifacts that are not installed.
	 * @param revertParam
	 * @param artDescs
	 * @param installedBundles
	 * @return
	 * @throws org.osgi.framework.BundleException
	 */
	private Set<MavenArtifactDesc> getRevertArtifacts(String revertParam,
			Set<MavenArtifactDesc> artDescs,
			Bundle[] installedBundles) throws BundleException {
		boolean installMissing = revertParam.toLowerCase().equals("missing");
		if (revertParam.equals("all")) {
			revertParam = ".*";
		}
		Set<MavenArtifactDesc> artsToBeInstalled = new HashSet<MavenArtifactDesc>();
		for (Iterator<MavenArtifactDesc> it = artDescs.iterator(); it.hasNext();) {
			MavenArtifactDesc mavenArtifactDesc = it.next();
			boolean isInstalled = false;
			if (mavenArtifactDesc.getShortUri().matches(revertParam) || installMissing) {
				for (int i = 0; i < installedBundles.length; i++) {
					Bundle bundle = installedBundles[i];
					if (bundle.getLocation().matches(mavenArtifactDesc.getShortUri() + "/.*[0-9].*")) {
						if (installMissing) {
							isInstalled = true;
						} else {
							bundle.uninstall();
						}
					}
				}
				if (!installMissing || (installMissing && !isInstalled)) {
					artsToBeInstalled.add(mavenArtifactDesc);
				}
			}
		}
		return artsToBeInstalled;
	}

	private void showUsage() {
		System.out.print("Usage: LaunchBundle ");
		System.out.println(AnnotatedInterfaceArguments.getArgumentsSyntax(LauncherArguments.class));
		PrintWriter out = new PrintWriter(System.out, true);
		AnnotatedInterfaceArguments.printArgumentDescriptions(
				LauncherArguments.class, out);
		out.flush();
	}

	private Properties getConfigProps(LauncherArguments arguments) {

		Properties configProps = new Properties();
		configProps.putAll(System.getProperties());
		{
			String argLogLevel = arguments.getLogLevel();
			if (argLogLevel == null) {
				argLogLevel = "INFO";
			}
			System.out.println("setting log-level to: " + argLogLevel);
			configProps.put("org.ops4j.pax.logging.DefaultServiceLog.level",
					argLogLevel);
		}
		{
			final String port = arguments.getPort();
			if (port != null) {
				configProps.put("org.osgi.service.http.port", port);
			}
			configProps.put("org.ops4j.pax.url.mvn.repositories", getCommaSeparatedListOfMavenRepos());
		}
		{
			String extraPackages = (String) configProps.get("org.osgi.framework.system.packages.extra");
			if (extraPackages == null) {
				extraPackages = "";
			}
			//sun.reflect added because of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6265952 and loading of scala scripts
			configProps.put("org.osgi.framework.system.packages.extra",
					"sun.misc;sun.reflect;"
					+ extraPackages);
		}

		//public static final String CONTEXT_PROPERTY_HTTP_PORT_SECURE = "";
		boolean httpsEnabled = false;
		{

			final String httpsPort = arguments.getSecurePort();
			if (httpsPort != null && !"".equals(httpsPort)) {
				configProps.put("org.osgi.service.http.port.secure", httpsPort);
				httpsEnabled = true;
			}
		}
		{

			final String keyStorePath = arguments.getKeyStorePath();
			if (keyStorePath != null && !"".equals(keyStorePath)) {
				configProps.put("org.wymiwyg.jetty.httpservice.https.keystore.path", keyStorePath);
				httpsEnabled = true;
			}
		}
		{

			final String keyStorePassword = arguments.getKeyStorePassword();
			if (keyStorePassword != null && !"".equals(keyStorePassword)) {
				configProps.put("org.wymiwyg.jetty.httpservice.https.keystore.password", keyStorePassword);
				httpsEnabled = true;
			}
		}
		{

			final String keyStoreType = arguments.getKeyStoreType();
			if (keyStoreType != null && !"".equals(keyStoreType)) {
				configProps.put("org.wymiwyg.jetty.httpservice.https.keystore.type", keyStoreType);
				httpsEnabled = true;
			}
		}

		{

			final String clientAuth = arguments.getClientAuth();
			if (clientAuth != null && !"".equals(clientAuth)) {
				configProps.put("org.wymiwyg.jetty.httpservice.clientauth", clientAuth);
				httpsEnabled = true;
			}
		}

		if (httpsEnabled) {
			configProps.put("org.osgi.service.http.secure.enabled", "true");
		}
		return configProps;

	}

	private String getCommaSeparatedListOfMavenRepos() {
		return "http://repository.apache.org/content/groups/snapshots-group@snapshots@noreleases,"
				+ "http://repo1.maven.org/maven2/,"
				+ "http://repository.ops4j.org/mvn-snapshots/@snapshots@noreleases";
	}
}
