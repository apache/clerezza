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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.maven.Maven;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.cli.BatchModeDownloadMonitor;
import org.apache.maven.cli.ConsoleDownloadMonitor;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.settings.DefaultMavenSettingsBuilder;
import org.apache.maven.settings.Settings;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.embed.Embedder;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author mir
 */
public class Util {


	private static Maven maven;
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

	static void setProjectVersion(File pomFile, String snapshotizedVersion) throws Exception {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(pomFile);
		Node projectNode = document.getChildNodes().item(0);
		Node versionNode = getSingleDiretChild(projectNode, "version");
		Node valueChild = versionNode.getFirstChild();
		valueChild.setNodeValue(snapshotizedVersion);

		writeXmlFile(document, pomFile);
	}

	static void setProjectParentRefVersion(File pomFile, String snapshotizedVersion) throws Exception {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(pomFile);
		Node projectNode = document.getChildNodes().item(0);
		Node parentNode = getSingleDiretChild(projectNode, "parent");
		Node versionNode = getSingleDiretChild(parentNode, "version");
		Node valueChild = versionNode.getFirstChild();
		valueChild.setNodeValue(snapshotizedVersion);

		writeXmlFile(document, pomFile);
	}


	private static ArtifactRepository getLocalRepository(Settings settings) {
		return new DefaultArtifactRepository(
				"local", new File(settings.getLocalRepository()).toURI().toString(),
				new DefaultRepositoryLayout());
	}

	static MavenProject getMavenProject(File pomFile) throws Exception {
		File userSettingFiles = new File(
				new File(System.getProperty("user.home")),
				".m2/settings.xml");
		Settings settings = new DefaultMavenSettingsBuilder().buildSettings(userSettingFiles);
		ArtifactRepository localRepository = getLocalRepository(settings);
		DefaultMavenProjectBuilder mpb = (DefaultMavenProjectBuilder) embedder.lookup(MavenProjectBuilder.ROLE);
		ProfileManager pm = new DefaultProfileManager(container, settings, new Properties());
		return mpb.build(pomFile, localRepository, pm);
	}

	public static void setDependencyManagerVersion(File pomFile, String groupId,
			String artifactId, String newVersion) throws Exception {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(pomFile);

		NodeList dependencyManagementNodes = document.getElementsByTagName("dependencyManagement");

		if (dependencyManagementNodes.getLength() != 1){
			throw new RuntimeException("no dependencyManagement element");
		}
		final Node depManageNode = dependencyManagementNodes.item(0);
		Node dependenciesElt = getSingleDiretChild(depManageNode, "dependencies");
		List<Node> dependecyList = getDirectChildren(dependenciesElt,"dependency");
		for(Node dependency : dependecyList) {
			Node groupIdElt = getSingleDiretChild(dependency, "groupId");
			String currentGroupId = groupIdElt.getFirstChild().getNodeValue().trim();
			if (!groupId.equals(currentGroupId)) {
				continue;
			}
			Node artifactIdElt = getSingleDiretChild(dependency, "artifactId");
			String currentArtifactId = artifactIdElt.getFirstChild().getNodeValue().trim();
			if (!artifactId.equals(currentArtifactId)) {
				continue;
			}
			Node versionElt = getSingleDiretChild(dependency, "version");
			Node valueChild = versionElt.getFirstChild();
			valueChild.setNodeValue(newVersion);
		}
		
		writeXmlFile(document, pomFile);
	}

	private static List<Node> getDirectChildren(Node node, String name) {
		final List<Node> result = new ArrayList<Node>();
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node child = nodeList.item(i);
			if (child.getNodeName().equals(name)) {
				result.add(child);
			}
		}
		return result;
	}

	private static Node getSingleDiretChild(Node node, String name) {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node child = nodeList.item(i);
			if (child.getNodeName().equals(name)) {
				return child;
			}
		}
		throw new RuntimeException("no child "+name);
	}

	private static void writeXmlFile(Document document, File file) throws Exception {
		// Use a Transformer for output
		FileOutputStream out = new FileOutputStream(file);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(out);
		transformer.transform(source, result);
	}
}
