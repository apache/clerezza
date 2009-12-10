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
package org.apache.clerezza.internal.process;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;
import org.apache.maven.Maven;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.cli.BatchModeDownloadMonitor;
import org.apache.maven.cli.ConsoleDownloadMonitor;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.embed.Embedder;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.dag.DAG;

/**
 * A utility for verifying consistency of dependecy versions.
 *
 * @author reto
 */
public class CheckDepManConsistency {

	static Maven maven;
	static DAG dag = new DAG();
	static Map<String, Dependency> projectMap = new HashMap<String, Dependency>();
	private static Map<String, File> id2DirMap = new HashMap<String, File>();
	private static PlexusContainer container;
	private static Embedder embedder;


	static {
		try {
			embedder = new Embedder();
			embedder.start(new ClassWorld());
			container = embedder.getContainer();
			maven = (Maven) embedder.lookup(Maven.ROLE);
			LoggerManager loggerManager = null;
			loggerManager = (LoggerManager) embedder.lookup(LoggerManager.ROLE);
			loggerManager.setThreshold(Logger.LEVEL_INFO);
			WagonManager wagonManager = (WagonManager) embedder.lookup(WagonManager.ROLE);
			Logger logger = loggerManager.getLoggerForComponent(WagonManager.ROLE);
			boolean interactive = true;
			if (interactive) {
				wagonManager.setDownloadMonitor(new ConsoleDownloadMonitor(logger));
			} else {
				wagonManager.setDownloadMonitor(new BatchModeDownloadMonitor(logger));
			}
			wagonManager.setInteractive(interactive);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	static Map<ArtifactUid, String> depManVersionMap = new HashMap<ArtifactUid, String>();

	/**
	 *
	 * @param args the firt args point to the projects containing the dependency
	 *     management, the projects specified by the subsequent arguments are
	 *     checked that:
	 *     - they contain no dependency that is part of the depndencymanagement at
	 *       a different version
	 *     - if the projects itself is part of the dm that it has the same version
	 * @throws java.lang.Exception
	 */
	public static void main(String... args) throws Exception {
		File parentPomFile = new File(new File(args[0]), "pom.xml");
		processParent(parentPomFile);

		for (int i = 1; i < args.length; i++) {
			verifyProject(new File(new File(args[i]), "pom.xml"), new HashSet<File>());
		}

		/* EventMonitor eventMonitor = new DefaultEventMonitor(
				new PlexusLoggerAdapter(new MavenEmbedderConsoleLogger()));

		maven.execute(pom, Collections.singletonList("package"), eventMonitor,
				new ConsoleDownloadMonitor(), null, targetDirectory);*/
	}

	public static Set<File> getInconsistentProjects(File parentPomFile, File[] pomFiles) throws Exception {
		processParent(parentPomFile);
		Set<File> result = new HashSet<File>();
		for (File file : pomFiles) {
			verifyProject(file, result);
		}
		return result;
	}

	private static void processParent(File pomFile) throws Exception {

		MavenProject pom = Util.getMavenProject(pomFile);
		final List managedDependencies = pom.getDependencyManagement().getDependencies();
		for (Object manDepObj : managedDependencies) {
			Dependency manDep = (Dependency)manDepObj;
			ArtifactUid uid = new ArtifactUid(manDep.getGroupId(), manDep.getArtifactId());
			depManVersionMap.put(uid, manDep.getVersion());
		}
	}

	private static void verifyProject(File pomFile, Set<File> inconsistentPoms) throws Exception {
		MavenProject pom = Util.getMavenProject(pomFile);
		ArtifactUid uid = new ArtifactUid(pom.getGroupId(), pom.getArtifactId());
		if (depManVersionMap.containsKey(uid)) {
				String depManVersion = depManVersionMap.get(uid);
				String version = pom.getVersion();
				if (!depManVersion.equals(version)) {
					System.out.println("INCONSISTENCY: in "+pomFile+", the artifact "
							+"should have version "+depManVersion);
					inconsistentPoms.add(pomFile);
				}
			}
		final List dependencies = pom.getDependencies();
		for (Object depObj : dependencies) {
			Dependency dep = (Dependency)depObj;
			ArtifactUid depUid = new ArtifactUid(dep.getGroupId(), dep.getArtifactId());
			if (depManVersionMap.containsKey(depUid)) {
				String depManVersion = depManVersionMap.get(depUid);
				String version = dep.getVersion();
				if (!depManVersion.equals(version)) {
					System.out.println("INCONSISTENCY: in "+pomFile+", the dependency "+dep.getGroupId()+":"+dep.getArtifactId()
							+" should have version "+depManVersion);
					inconsistentPoms.add(pomFile);
				}
			}

		}
	}
}
