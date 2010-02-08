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
package org.apache.clerezza.platform.typerendering;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A class that "collects" elements to be added to the XHTML document resulting
 * from a rendering process.
 *
 * @author reto
 */
public class ResultDocModifier {

	private final static String XHTMLNS = "http://www.w3.org/1999/xhtml";
	private static final Logger logger = LoggerFactory.getLogger(ResultDocModifier.class);
	private static ThreadLocal<ResultDocModifier> threadLocal =
			new ThreadLocal<ResultDocModifier>();
	private DocumentBuilder documentBuilder;


	{
		try {
			final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setValidating(false);
			builderFactory.setNamespaceAware(true);
			builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			documentBuilder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Initialized a collector for the current thread
	 */
	static void init() {
		threadLocal.set(new ResultDocModifier());
	}

	static void dispose() {
		threadLocal.remove();
	}

	public static ResultDocModifier getInstance() {
		return threadLocal.get();
	}
	private final List<String> styleSheets = new ArrayList<String>();
	private final List<String> scriptRefs = new ArrayList<String>();
	private final Map<String, List<Node>> elemAdditions =
			new HashMap<String, List<Node>>();
	private String onloadScripts = "";
	private String scriptCodes = "";
	private String titleText = "";
	private String subTitleText = "";

	private boolean isModified = false;

	public boolean isModified() {
		return isModified;
	}

	public void addStyleSheet(String path) {
		if (!styleSheets.contains(path)) {
			styleSheets.add(path);
			isModified = true;
		}
	}

	public void addScriptReference(String path) {
		if (!scriptRefs.contains(path)) {
			scriptRefs.add(path);
			isModified = true;
		}
	}

	public void addOnLoad(String script) {
		if (!onloadScripts.isEmpty()) {
			onloadScripts += ";";
		}
		onloadScripts += script;
		isModified = true;
	}

	public void addScripts(String script) {
		if (!scriptCodes.isEmpty()) {
			scriptCodes += "\n";
		}
		scriptCodes += script;
		isModified = true;
	}

	public void setTitle(String title) {
		titleText = title;
		isModified = true;
	}

	public void setSubTitle(String subTitle) {
		subTitleText = subTitle;
		isModified = true;
	}

	/**
	 * Adds Nodes to the element with the given id.
	 *
	 * @param id
	 * @param elem
	 */
	public void addNodes2Elem(String id, NodeList nodes) {
		List<Node> nodeList = elemAdditions.get(id);
		if (nodeList == null) {
			nodeList = new ArrayList<Node>();
			elemAdditions.put(id, nodeList);
		}
		for (int i = 0; i < nodes.getLength(); i++) {
			nodeList.add(nodes.item(i));
		}
		isModified = true;
	}

	public void addNodes2Elem(String id, String nodes) {
		try {
			String miniDoc = "<doc xmlns=\"http://www.w3.org/1999/xhtml\">" + nodes + "</doc>";
			Document document = documentBuilder.parse(
					new InputSource(new StringReader(miniDoc)));
			addNodes2Elem(id, document.getDocumentElement().getChildNodes());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 *
	 * @param id the of the element to shich the nodes are to be added
	 * @param nodes an object of which the toSTring method returns a well
	 * balanced xhtml snipped
	 */
	public void addNodes2Elem(String id, Object nodes) {
		addNodes2Elem(id, nodes.toString());
	}

	void addToDocument(Document document) {
		Node head = getHead(document);
		if (!titleText.isEmpty()) {
			String pageTitle = titleText;
			if (!subTitleText.isEmpty()) {
				pageTitle += " - " + subTitleText;
			}
			Element title = (Element) getTitle(document);
			if (title == null) {
				title = document.createElementNS(XHTMLNS, "title");
			}
			title.setTextContent(pageTitle);
			head.appendChild(title);
		}
		for (String styleSheet : styleSheets) {
			Element link = document.createElementNS(XHTMLNS, "link");
			link.setAttribute("type", "text/css");
			link.setAttribute("rel", "stylesheet");
			link.setAttribute("href", styleSheet);
			head.appendChild(document.createTextNode("\t"));
			head.appendChild(link);
			head.appendChild(document.createTextNode("\n"));
		}
		for (String scriptRef : scriptRefs) {
			Element script = document.createElementNS(XHTMLNS, "script");
			script.setAttribute("type", "text/javascript");
			script.setAttribute("src", scriptRef);
			head.appendChild(document.createTextNode("\t"));
			head.appendChild(script);
			head.appendChild(document.createTextNode("\n"));
		}
		if (!scriptCodes.isEmpty()) {
			Element inlineScript = document.createElementNS(XHTMLNS, "script");
			inlineScript.setAttribute("type", "text/javascript");
			inlineScript.setTextContent(scriptCodes);
			head.appendChild(inlineScript);
		}
		if (!onloadScripts.isEmpty()) {
			Element body = (Element) getBody(document);
			String existingOnLoadScript = body.getAttribute("onload");
			if (!existingOnLoadScript.isEmpty()) {
				existingOnLoadScript += ";";
			}
			existingOnLoadScript += onloadScripts;
			body.setAttribute("onload", existingOnLoadScript);
		}
		for (Map.Entry<String, List<Node>> entry : elemAdditions.entrySet()) {
			addElems(document, entry.getKey(), entry.getValue());
		}
	}

	private Node getHead(Document document) {
		NodeList heads = document.getElementsByTagNameNS(XHTMLNS, "head");
		if (heads.getLength() > 1) {
			throw new RuntimeException("Expected at most one head in html document");
		}
		return heads.item(0);
	}

	private Node getBody(Document document) {
		NodeList bodies = document.getElementsByTagNameNS(XHTMLNS, "body");
		if (bodies.getLength() > 1) {
			throw new RuntimeException("Expected at most one body in html document");
		}
		return bodies.item(0);
	}

	private Node getTitle(Document document) {
		NodeList titleNodes = document.getElementsByTagNameNS(XHTMLNS, "title");
		if (titleNodes.getLength() == 0) {
			return null;
		}
		return titleNodes.item(0);
	}

	private void addElems(Document document, String id, List<Node> nodes) {
		Element element = getElementById(document.getDocumentElement(), id);
		if (element == null) {
			logger.error("No element found with id \"" + id + "\"");
			return;
		}
		for (Node node : nodes) {
			// if using adoptNode instead of importNode attributes of the node
			// are not correctly adopted.
			element.appendChild(document.importNode(node, true));
		}
	}

	private Element getElementById(Element element, String id) {
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element subElement = (Element) node;
				NamedNodeMap attrs = subElement.getAttributes();
				if (attrs != null) {
					Node idNode = attrs.getNamedItem("id");
					if ((idNode != null) && (id.equals(idNode.getTextContent()))) {
						return subElement;
					}
				}
				Element subResult = getElementById(subElement, id);
				if (subResult != null) {
					return subResult;
				}
			}
		}
		return null;
	}
}
