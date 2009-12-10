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
import java.util.Map;
import java.util.Set;
import org.apache.maven.project.MavenProject;
import org.apache.clerezza.internal.process.DependencyOrder.ProcessedDepenedency;

/**
 * Snapshotizes a project and the projects depending on it.
 *
 * @author mir
 */
public class SnapshotizeProject {

	private static Map<ArtifactUid, File> artifact2PomMap;
	private static File parentPom;

	public static void main(String... args) throws Exception {

		if (args.length < 3) {
			System.out.println("Usage: order <parent> <projects> <target-project>");
			System.out.println("Snapshotizes a project in the dependency " +
					"management in the parent and makes sure none of projects " +
					"implements or depends on an older version by snapshotizing " +
					"dependant projects recursively");
			System.exit(1);
		}
		parentPom = new File(new File(args[0]), "pom.xml");
		final File[] projectPoms = new File[args.length-2];
		for (int i = 1; i < args.length-1; i++) {
			projectPoms[i-1] = new File(new File(args[i]),"pom.xml");
			if (!projectPoms[i-1].exists()) {
				throw new RuntimeException(projectPoms[i-1]+" doesn't exist");
			}

		}

		createArtifact2DirMap(projectPoms);
		final File targetProjectDir = new File(args[args.length-1]);
		final File targetProjectPom = new File(targetProjectDir,"pom.xml");
		snapshotize(targetProjectPom);
		int lastNumberOfInconsistentProjects = Integer.MAX_VALUE;
		while (true) {
			Set<File> inconsistentProjects = CheckDepManConsistency.getInconsistentProjects(parentPom, projectPoms);
			if (inconsistentProjects.size() < lastNumberOfInconsistentProjects) {
				lastNumberOfInconsistentProjects = inconsistentProjects.size();
				for (File pomFile : inconsistentProjects) {
					snapshotize(pomFile);
				}
				continue;
			}
			break;
		}
		
	}

	private static String createSnapshotVersion(String version) {
		if (version.endsWith("-SNAPSHOT")) {
			return version;
		} else {
			return increase(version)+"-SNAPSHOT";
		}
	}

	private static void createArtifact2DirMap(File[] projectPoms) throws Exception {
		artifact2PomMap = new HashMap<ArtifactUid, File>();
		for (File projectPom : projectPoms) {
			MavenProject mavenProject = Util.getMavenProject(projectPom);
			artifact2PomMap.put(new ArtifactUid(mavenProject.getGroupId(),
					mavenProject.getArtifactId()), projectPom);
		}
	}

	private static String increase(String version) {
		int lastDotPos = version.lastIndexOf('.');
		String subVersion = version.substring(lastDotPos+1);
		String trunk = version.substring(0, lastDotPos+1);
		return trunk+(Integer.parseInt(subVersion)+1);
	}

	private static boolean isSnapshot(String version) {
		return version.endsWith("-SNAPSHOT");
	}

	private static void snapshotize(File targetProjectPom) throws Exception {
		System.out.println("Snapshotizing "+targetProjectPom);
		MavenProject mavenProject = Util.getMavenProject(targetProjectPom);
		snapshotizeInDependencyManagement(mavenProject);
		snapshotizePom(targetProjectPom);

	}

	private static void snapshotizeInDependencyManagement(MavenProject mavenProject) throws Exception {
		String artifactId = mavenProject.getArtifactId();
		String groupId = mavenProject.getGroupId();
		Util.setDependencyManagerVersion(parentPom,groupId, artifactId, createSnapshotVersion(mavenProject.getVersion()));
		snapshotizePomWithoutParents(parentPom);

	}

	/** recursively snapshotizes parents and changes the ref to the newest version 
	 *
	 * @param mavenProject
	 */
	private static void snapshotizeParent(MavenProject mavenProject) throws Exception {
		MavenProject parentProject = mavenProject.getParent();
		String parentVersion = parentProject.getVersion();
		File projectPomFile = artifact2PomMap.get(new ArtifactUid(
				mavenProject.getGroupId(), mavenProject.getArtifactId()));
		if (!isSnapshot(parentVersion)) {
			final ArtifactUid parentArtifactUid = new ArtifactUid(parentProject.getGroupId(), parentProject.getArtifactId());
			File parentProjectPom = artifact2PomMap.get(parentArtifactUid);
			if (parentProjectPom == null) {
				throw new RuntimeException("no project found for "+parentArtifactUid);
			}
			MavenProject currentParentProject = Util.getMavenProject(parentProjectPom);
			final String currentParentVersion = currentParentProject.getVersion();
			final String snapshotizedParentVersion = createSnapshotVersion(currentParentVersion);
			if (!isSnapshot(currentParentVersion)) {
				snapshotizePom(parentProjectPom);
			}
			Util.setProjectParentRefVersion(projectPomFile, snapshotizedParentVersion);
		}
		
	}

	private static void snapshotizePom(File projectPom) throws Exception {
		final MavenProject mavenProject = Util.getMavenProject(projectPom);
		final String version = mavenProject.getVersion();
		final String snapshotizedVersion = createSnapshotVersion(version);
		if (!version.equals(snapshotizedVersion)) {
			snapshotizeParent(mavenProject);
			Util.setProjectVersion(projectPom, snapshotizedVersion);
		}
	}

	private static void snapshotizePomWithoutParents(File projectPom) throws Exception {
		final MavenProject mavenProject = Util.getMavenProject(projectPom);
		final String version = mavenProject.getVersion();
		final String snapshotizedVersion = createSnapshotVersion(version);
		if (!version.equals(snapshotizedVersion)) {
			Util.setProjectVersion(projectPom, snapshotizedVersion);
		}
	}
}
