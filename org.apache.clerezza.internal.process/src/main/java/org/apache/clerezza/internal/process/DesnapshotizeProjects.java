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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A utility for desnapshotizing clerezza.org projects.
 * 
 * @author mir
 */
public class DesnapshotizeProjects {

	public static final String SNAPSHOT = "-SNAPSHOT";

	/**
	 *
	 * @param args the project directories to be desnapshotized.
	 * @throws java.lang.Exception
	 */
	public static void main(String... args) throws Exception {

		processParent(new File(args[0]));

		for (int i = 0; i < args.length; i++) {
			File projDirectory = new File(args[i]);

			if (!projDirectory.isDirectory()) {
				continue;
			}
			
			if (args[i].contains("sandbox")) {
				continue;
			}

			processProject(projDirectory);
		}

	}

	private static void desnapshotizeVersionNode(Node version) throws DOMException {
		Node valueChild = version.getFirstChild();
		String value = valueChild.getNodeValue();
		if (value.endsWith(SNAPSHOT)) {
			valueChild.setNodeValue(value.substring(0, value.length() - SNAPSHOT.length()));
		}
	}

	private static void processParent(File targetDirectory) throws Exception {
		System.out.println("Desnapshotize Parent: " + targetDirectory.getPath());
		File pomFile = new File(targetDirectory, "pom.xml");
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(pomFile);

		NodeList versionNodes = document.getElementsByTagName("version");
		for (int i = 0; i < versionNodes.getLength(); i++) {
			Node version = versionNodes.item(i);
			Node parent = version.getParentNode();
			if (parent.getNodeName().equals("dependency")) {
				if (!parent.getParentNode().getNodeName().equals("dependencies")) {
					continue;
				}
				if (!parent.getParentNode().getParentNode().
						getNodeName().equals("dependencyManagement")) {
					continue;
				}

				NodeList depChildren = parent.getChildNodes();
				for (int j = 0; j < depChildren.getLength(); j++) {
					Node child = depChildren.item(j);
					if (child.getNodeName().equals("groupId")) {
						if (child.getFirstChild().getNodeValue().startsWith("org.apache.clerezza")) {
							desnapshotizeVersionNode(version);
						}
					}
				}	
			}
		}
		
		writeXmlFile(document, pomFile);
	}

	private static void processProject(File targetDirectory) throws Exception{
		File pomFile = new File(targetDirectory, "pom.xml");
		if (!pomFile.exists()) {
			return;
		}
		System.out.println("Desnapshotize Project: " + targetDirectory.getPath());
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.parse(pomFile);

		NodeList children = document.getChildNodes().item(0).getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals("parent")) {
				NodeList parentNodes = child.getChildNodes();
				for (int j = 0; j < parentNodes.getLength(); j++) {
					Node parentNode = parentNodes.item(j);
					if (parentNode.getNodeName().equals("version")) {
						desnapshotizeVersionNode(parentNode);
					}
				}
			}

			if (child.getNodeName().equals("version")) {
				desnapshotizeVersionNode(child);
			}

		}

		writeXmlFile(document, pomFile);
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
