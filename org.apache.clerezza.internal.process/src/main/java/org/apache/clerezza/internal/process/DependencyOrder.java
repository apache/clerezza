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
import java.util.ArrayList;
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
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.embed.Embedder;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;

/**
 *
 * @author rbn
 */
public class DependencyOrder {

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

	public static class ProcessedDepenedency {
		private String groupId, artifactId;
		private boolean compiled;
		private File directory;

		ProcessedDepenedency(String groupId, String artifactId, File directory,
				boolean compiled) {
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.directory = directory;
			this.compiled = compiled;
		}

		public String getArtifactId() {
			return artifactId;
		}

		public boolean isCompiled() {
			return compiled;
		}

		public String getGroupId() {
			return groupId;
		}

		public File getDirectory() {
			return directory;
		}

		

		@Override
		public String toString() {
			return groupId+":"+artifactId+" (directory: "+directory+") compiled: "+compiled;
		}


	}

	/**
	 *
	 * @param args a list of project-folders to compile
	 * @throws java.lang.Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length == 0) {
			System.out.println("Usage: order <project-directory> [<project-directory> ...]");
			System.exit(1);
		}
		final File[] directories = new File[args.length];
		for (int i = 0; i < args.length; i++) {
			directories[i] = new File(args[i]);

		}
		for (ProcessedDepenedency dep : getTopologicalOrder(directories)) {
			System.out.println(dep);
		}
	}

	public static List<ProcessedDepenedency> getTopologicalOrder(File[] directories) throws Exception {
		List<File> succesfullyPreprocessed = new ArrayList<File>();
		Set<File> succesfullyCompiled = new HashSet<File>();
		Map<File, Integer> retries = new HashMap<File, Integer>();
		for (int i = 0; i < directories.length; i++) {
			final File dir = directories[i];
			try {
				preprocess(dir);
				System.out.println("preprocessed: " + dir);
				succesfullyPreprocessed.add(dir);
			} catch (ProjectBuildingException e) {
				retries.put(dir, 0);
			}
		}

		while (retries.size() > 0) {
			for (Object o : TopologicalSorter.sort(dag)) {
				File dir = id2DirMap.get(o);
				System.out.println("compile?: " + dir);
				if (succesfullyPreprocessed.contains(dir) && !succesfullyCompiled.contains(dir)) {
					MultiCompile.compile(dir);
					succesfullyCompiled.add(dir);
					break;
				}
			}
			Map<File, Integer> currentRetries = new HashMap<File, Integer>(retries);
			for (File dir : currentRetries.keySet()) {
				try {
					preprocess(dir);
					succesfullyPreprocessed.add(dir);
					retries.remove(dir);
				} catch (ProjectBuildingException e) {
					final int retryCount = retries.get(dir).intValue() + 1;
					System.out.println("exception retrying (count: " + retryCount + "): " + e);
					if (e.getCause() != null) {
						System.out.println(e.getCause().getMessage());
					}

					if (retryCount > 5) {
						System.err.println("Giving up on " + dir);
						retries.remove(dir);
					} else {
						retries.put(dir, retryCount);
					}
					continue;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}


		List<ProcessedDepenedency> result = new ArrayList<ProcessedDepenedency>();
		for (Object o : TopologicalSorter.sort(dag)) {
			String idOfProject = (String) o;
			ArtifactUid artifactUid = new ArtifactUid(idOfProject);
			//System.out.println(idOfProject);
			File dir = id2DirMap.get(idOfProject);
			if (dir != null) {
				//System.out.println(dir + " compiled: " + succesfullyCompiled.contains(dir));
				result.add(new ProcessedDepenedency(artifactUid.getGroupId(), 
						artifactUid.getArtifactId(), dir,
						succesfullyCompiled.contains(dir)));
			}
		}
		return result;
	}



	

	private static void preprocess(File projectDir) throws Exception {

		File pomFile = new File(projectDir, "pom.xml");
		
		
		
		ProjectBuilderConfiguration config = new DefaultProjectBuilderConfiguration();
		DefaultProjectBuilderConfiguration pbc = new DefaultProjectBuilderConfiguration();
		//MavenProjectBuilder mpb = new DefaultMavenProjectBuilder();
		
		
		MavenProject pom = Util.getMavenProject(pomFile);
		ArtifactUid uid = new ArtifactUid(pom.getGroupId(), pom.getArtifactId());

		String id = uid.toString();
		id2DirMap.put(id, projectDir);

		MavenProject parent = pom.getParent();
		//System.out.print("parent: "+parent);
		if (parent != null) {
			addDependency(uid, new ArtifactUid(parent.getGroupId(), parent.getArtifactId()));
		}
		final List dependencies = pom.getDependencies();
		for (Object depObj : dependencies) {
			Dependency dep = (Dependency) depObj;
			ArtifactUid depUid = new ArtifactUid(dep.getGroupId(), dep.getArtifactId());
			addDependency(uid, depUid);

		}
	}
	/**
	 * used for dependencies as well as child-parent relationship
	 * @param uid
	 * @param depUid
	 */
	private static void addDependency(ArtifactUid uid, ArtifactUid depUid) {
			String dependencyId = depUid.toString();
			//if (dag.getVertex(dependencyId) != null) {
			try {
				dag.addEdge(uid.toString(), dependencyId);
				//System.out.println(uid + " is dependend from " + dependencyId);
			} catch (CycleDetectedException e) {
				System.out.println("Ignore cycle detected in project dependencies: " + e.getMessage());

			}
	}
}
