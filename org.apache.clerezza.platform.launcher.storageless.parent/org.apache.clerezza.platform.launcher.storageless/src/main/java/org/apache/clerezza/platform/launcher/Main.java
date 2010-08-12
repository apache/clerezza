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
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;
import org.wymiwyg.commons.util.dirbrowser.PathNode;
import org.wymiwyg.commons.util.dirbrowser.PathNodeFactory;
import java.security.Policy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;
import org.wymiwyg.commons.util.arguments.AnnotatedInterfaceArguments;
import org.wymiwyg.commons.util.arguments.ArgumentHandler;
import org.wymiwyg.commons.util.arguments.ArgumentProcessor;
import org.wymiwyg.commons.util.arguments.InvalidArgumentsException;
import org.wymiwyg.commons.util.dirbrowser.PathNameFilter;

public class Main implements BundleActivator {

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
		installBundles(context, artDescs, nextLevel);

		final int newStartLevel =
				originalStartLevel > nextLevel + 1 ? originalStartLevel : nextLevel + 1;
		startLevel.setStartLevel(newStartLevel);
		if (startLevel.getInitialBundleStartLevel() < nextLevel + 1) {
			startLevel.setInitialBundleStartLevel(nextLevel + 1);
		}
		System.out.println("uninstalling platform launcher");
		context.getBundle().uninstall();
	}

	private static Set<Bundle> installBundles(BundleContext context,
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

	@Override
	public void stop(BundleContext context) throws Exception {
	}

	private static class MavenArtifactDesc implements Comparable<MavenArtifactDesc> {

		private static MavenArtifactDesc parseFromURL(URL bundleUrl) {
			String string = bundleUrl.toString();
			int posSlashM1 = string.lastIndexOf('/');
			int posSlashM2 = string.lastIndexOf('/', posSlashM1 - 1);
			int posSlashM3 = string.lastIndexOf('/', posSlashM2 - 1);
			String version = string.substring(posSlashM2 + 1, posSlashM1);
			String artifactId = string.substring(posSlashM3 + 1, posSlashM2);
			String groupId = getGroupId(string.substring(0, posSlashM3));
			return new MavenArtifactDesc(groupId, artifactId, version, bundleUrl);
		}

		private static MavenArtifactDesc parseFromPath(PathNode pathNode) {
			String string = pathNode.getPath();
			int posSlashM1 = string.lastIndexOf('/');
			int posSlashM2 = string.lastIndexOf('/', posSlashM1 - 1);
			int posSlashM3 = string.lastIndexOf('/', posSlashM2 - 1);
			String version = string.substring(posSlashM2 + 1, posSlashM1);
			String artifactId = string.substring(posSlashM3 + 1, posSlashM2);
			String groupId = getGroupId(string.substring(0, posSlashM3));
			return new MavenArtifactDesc(groupId, artifactId, version, pathNode);
		}

		/**
		 * assembles group-id from the diretories after "bundles/"
		 */
		private static String getGroupId(String string) {
			int startPos = string.indexOf("bundles/") + 8;
			startPos = string.indexOf('/', startPos) + 1;
			return string.substring(startPos).replace('/', '.');
		}
		//one of these is null
		URL bundleUrl;
		PathNode pathNode;
		String groupId, artifactId, version;

		private MavenArtifactDesc(String groupId, String artifactId,
				String version, URL bundleUrl) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
			this.bundleUrl = bundleUrl;
		}

		private MavenArtifactDesc(String groupId, String artifactId,
				String version, PathNode pathNode) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
			this.pathNode = pathNode;
		}

		public String getShortUri() {
			return "mvn:" + groupId + "/" + artifactId;
		}

		@Override
		public String toString() {
			return "mvn:" + groupId + "/" + artifactId + "/" + version;
		}

		private InputStream getInputStream() throws IOException {
			if (bundleUrl == null) {
				return pathNode.getInputStream();
			}
			return bundleUrl.openStream();
		}

		@Override
		public int compareTo(MavenArtifactDesc o) {
			return toString().compareTo(o.toString());
		}
	}

	public static void main(String... args) throws IOException {
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
		Policy.setPolicy(new Policy() {

			@Override
			public PermissionCollection getPermissions(ProtectionDomain domain) {
				PermissionCollection result = new Permissions();
				result.add(new AllPermission());
				return result;
			}
		});
		System.setSecurityManager(new SecurityManager());
		Properties configProps = getConfigProps(arguments);
		try {
			Felix m_felix = new Felix(configProps);
			System.out.println("starting felix");
			m_felix.start();
			final PathNode bundlesRoot = PathNodeFactory.getPathNode(Main.class.getResource("/bundles"));
			final BundleContext bundleContext = m_felix.getBundleContext();
			final String revertParam = arguments.getRevertParam();
			installBundlesForStartLevels(bundleContext, bundlesRoot, revertParam);
		} catch (Exception ex) {
			System.err.println("Could not create framework: " + ex);
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	private static Properties getConfigProps(LauncherArguments arguments) {

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
			configProps.put("org.osgi.framework.system.packages.extra",
					"sun.misc;"
					+ extraPackages);
		}

	//public static final String CONTEXT_PROPERTY_HTTP_PORT_SECURE = "";
		boolean httpsEnabled = false;
		{
			
			final String httpsPort = arguments.getSecurePort();
			if (httpsPort != null) {
				configProps.put("org.osgi.service.http.port.secure", httpsPort);
				httpsEnabled = true;
			}
		}
		{
			
			final String keyStorePath = arguments.getKeyStorePath();
			if (keyStorePath != null) {
				configProps.put("org.wymiwyg.jetty.httpservice.https.keystore.path", keyStorePath);
				httpsEnabled = true;
			}
		}
		{
			
			final String keyStorePassword = arguments.getKeyStorePassword();
			if (keyStorePassword != null) {
				configProps.put("org.wymiwyg.jetty.httpservice.https.keystore.password", keyStorePassword);
				httpsEnabled = true;
			}
		}
		{
			
			final String keyStoreType = arguments.getKeyStoreType();
			if (keyStoreType != null) {
				configProps.put("org.wymiwyg.jetty.httpservice.https.keystore.type", keyStoreType);
				httpsEnabled = true;
			}
		}
		
		{
			
			final String clientAuth = arguments.getClientAuth();
			if (clientAuth != null) {
				configProps.put("org.wymiwyg.jetty.httpservice.clientauth", clientAuth);
				httpsEnabled = true;
			}
		}
		
		if (httpsEnabled) {
			configProps.put("org.osgi.service.http.secure.enabled", "true");
		}
		
		return configProps;

	}

	private static String getCommaSeparatedListOfMavenRepos() {
		return "http://repository.apache.org/content/groups/snapshots-group@snapshots@noreleases,"+
				"http://repo1.maven.org/maven2/,"+
				"http://repository.ops4j.org/mvn-snapshots/@snapshots@noreleases";
	}

	private static void installBundlesForStartLevels(final BundleContext bundleContext,
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

	private static void showUsage() {
		System.out.print("Usage: LaunchBundle ");
		System.out.println(AnnotatedInterfaceArguments.getArgumentsSyntax(LauncherArguments.class));
		PrintWriter out = new PrintWriter(System.out, true);
		AnnotatedInterfaceArguments.printArgumentDescriptions(
				LauncherArguments.class, out);
		out.flush();
	}

	private static Set<MavenArtifactDesc> getArtDescsFrom(PathNode pathNode) {
		Set<MavenArtifactDesc> result = new HashSet<MavenArtifactDesc>();
		List<PathNode> jarPaths = getJarPaths(pathNode);
		for (PathNode jarPath : jarPaths) {
			result.add(MavenArtifactDesc.parseFromPath(jarPath));
		}

		return result;
	}

	private static List<PathNode> getJarPaths(PathNode pathNode) {
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
	private static boolean alreadyInstalled(Set<MavenArtifactDesc> artDescs,
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
	private static Set<MavenArtifactDesc> getRevertArtifacts(String revertParam,
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
}
