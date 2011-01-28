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

package org.apache.clerezza.platform.content;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.ontologies.HIERARCHY;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wymiwyg.wrhapi.HeaderName;

/**
 *
 * @author agron
 */
class WebDavUtils {

	static final String infinite = "infinite";
	static final String prop = "prop";
	static final String allprop = "allprop";
	static final String propname = "propname";
	static final String propfind = "propfind";
	static final String proppatch = "propertyupdate";
	static final String set = "set";
	static final String remove = "remove";
	private static final String davUri = "DAV:";
	private static final String multistat = "multistatus";
	private static final String response = "response";
	private static final String href = "href";
	private static final String propstat = "propstat";
	private static final String status = "status";

	//WebDAV properties
	private static final String creationdate = "creationdate";
	private static final String displayname = "displayname";
	private static final String getcontentlanguage = "getcontentlanguage";
	private static final String getcontentlength = "getcontentlength";
	private static final String getcontenttype = "getcontenttype";
	private static final String getetag = "getetag";
	private static final String getlastmodified = "getlastmodified";
	private static final String lockdiscovery = "lockdiscovery";
	private static final String resourcetype = "resourcetype";
	private static final String supportedlock = "supportedlock";

	private static final List<String> davProps = new ArrayList<String>(Arrays.asList(
			creationdate, displayname, getcontentlanguage, getcontentlength,
			getcontenttype, getetag, getlastmodified, lockdiscovery,
			resourcetype, supportedlock));
	
	private static final List<String> protectedProps = new ArrayList<String>(Arrays.asList(
			creationdate, getcontentlength, getetag, getlastmodified,
			lockdiscovery, resourcetype, supportedlock));


	/**
	 * Returns the {@link Node} from a given {@link Document}
	 * @param doc
	 * @param nodeName
	 * @return null if Node wasen't found
	 */
	static Node getNode(Document doc, String nodeName){
		NodeList nodes = doc.getElementsByTagNameNS(davUri, nodeName);
		if(nodes.getLength() == 1){
			return nodes.item(0);
		}
		return null;
	}

	/**
	 * @param node
	 * @return returns the first non Text node or null
	 */
	static Node getFirstChild(Node node){
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if(nodeList.item(i).getLocalName() != null){
				return nodeList.item(i);
			}
		}
		return null;
	}

	/**
	 * Converts a {@link Source} to a {@link Document}
	 *
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	static Document sourceToDocument(Source body)
			throws ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.newDocument();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		DOMResult result = new DOMResult(doc);
		transformer.transform(body, result);
		return (Document) result.getNode();
	}

	/**
	 * Returns the content of a requested header as a {@link String}
	 *
	 * @param headers
	 * @param header
	 *            header to be returned
	 * @return returns <code>null</code> if the requested header is empty
	 */
	static String getHeaderAsString(HttpHeaders headers, String header) {
		List<String> requestedHeader = headers.getRequestHeader(header);
		if(requestedHeader == null){
			return null;
		}
		Iterator<String> headerIterator = requestedHeader.iterator();
		if (headerIterator.hasNext()) {
			return headerIterator.next();
		} else {
			return null;
		}
	}

	/**
	 * @param clazz
	 * @return returns an ok Response with a DAV: and an ALLOW: header
	 */
	static Response options(Class<?> clazz){
		Response.ResponseBuilder builder = Response.ok();
		builder.header(HeaderName.DAV.toString(), "1");
		Set<String> allow = new HashSet<String>();
		Method[] methods = clazz.getMethods();
		for (Method method : methods){
			for (Annotation annotation : method.getAnnotations()){
				HttpMethod httpMethod = annotation.annotationType()
						.getAnnotation(HttpMethod.class);
				if(httpMethod != null){
					allow.add(httpMethod.value());
				}
			}
		}
		if (allow.isEmpty()) {
			builder.header(HeaderName.ALLOW.toString(), "");
		} else {
			final Iterator<String> iterator = allow.iterator();
			final StringBuffer buffer = new StringBuffer(iterator.next());
			while (iterator.hasNext()) {
				buffer.append(", ");
				buffer.append(iterator.next());
			}
			builder.header(HeaderName.ALLOW.toString(), buffer.toString());
		}
		return builder.build();
	}

	/*------------------------------------------*
	 * Putting the properties in a DOM Document *
	 *------------------------------------------*/

	static Document createResponseDoc(Map<UriRef, PropertyMap> resultMap)
			throws ParserConfigurationException {
		Document responseDoc = DocumentBuilderFactory.newInstance().
				newDocumentBuilder().newDocument();
		Set<UriRef> nodeNameSet = resultMap.keySet();

		Element multistatElement = responseDoc.createElementNS(davUri,"D:" + multistat);
		// add multistat element to response
		responseDoc.appendChild(multistatElement);
		for (UriRef nodeName : nodeNameSet) {
			Element responseElement = responseDoc.createElementNS(davUri, "D:" + response);
			// add response element to response Document
			multistatElement.appendChild(responseElement);
			PropertyMap propertyMap = resultMap.get(nodeName);
			addElementsToResponse(propertyMap, responseElement, responseDoc, nodeName);
		}
		return responseDoc;
	}

	private static void addElementsToResponse(PropertyMap propertyMap,
			Element responseElement, Document responseDoc, UriRef nodeName) {
		Element hrefElement = responseDoc.createElementNS(davUri, "D:" + href);
		hrefElement.setTextContent(nodeName.getUnicodeString());
		// add hrefElement element to responseElement
		responseElement.appendChild(hrefElement);
		addPropsToPropstat(responseElement, propertyMap, responseDoc);

	}

	private static void addPropsToPropstat(Element responseElement, PropertyMap propertyMap,
			Document responseDoc) {
		Set<Property> props = propertyMap.keySet();
		Element propFoundElement = responseDoc.createElementNS(davUri, "D:" + prop);
		Element propNotFoundElement = responseDoc.createElementNS(davUri, "D:" + prop);
		for(Property propVal : props){
			String propName = propVal.prop;
			String ns = propVal.ns;
			String prf = ns.equalsIgnoreCase(davUri) ? "D:":"R:";
			String value = propertyMap.get(propVal);
			Element resultElement = responseDoc.createElementNS(ns, prf + propName);
			if(value != null){
				if (!(value.isEmpty())){
					if (value.equals("collection")) {
						resultElement.appendChild(responseDoc.createElementNS(
								davUri, "D:collection"));
					} else {
						resultElement.setTextContent(value);
					}
				}
				propFoundElement.appendChild(resultElement);
			} else {
				propNotFoundElement.appendChild(resultElement);
			}
		}
		Element propstatFoundElement = responseDoc.createElementNS(davUri,
				"D:" + propstat);
		Element statusFoundElement = responseDoc.createElementNS(davUri,
				"D:" + status);
		propstatFoundElement.appendChild(propFoundElement);
		statusFoundElement.setTextContent("HTTP/1.1 200 OK");
		propstatFoundElement.appendChild(statusFoundElement);
		responseElement.appendChild(propstatFoundElement);
		if(propNotFoundElement.hasChildNodes()){
			Element propstatNotFoundElement = responseDoc.createElementNS(davUri,
					"D:" + propstat);
			Element statusNotFoundElement = responseDoc.createElementNS(davUri,
					"D:" + status);
			propstatNotFoundElement.appendChild(propNotFoundElement);
			statusNotFoundElement.setTextContent("HTTP/1.1 404 Not Found");
			propstatNotFoundElement.appendChild(statusNotFoundElement);
			responseElement.appendChild(propstatNotFoundElement);
		}

	}

	/*------------------------------------------------------------*
	 * Get the properties from the CollectionNode and its members *
	 *------------------------------------------------------------*/

	static Map<UriRef, PropertyMap> getPropsByName(NodeList children,
			GraphNode node, String depthHeader, boolean includeValues) {
		List<Property> requestedUserProps = new ArrayList<Property>();
		List<Property> requestedDavProps = new ArrayList<Property>();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String localName = child.getLocalName();
			if(localName == null){
				continue;
			}
			String nsUri = child.getNamespaceURI();
			if (nsUri.equals(davUri)) {
				requestedDavProps.add(new Property(nsUri, localName));
			} else {
				requestedUserProps.add(new Property(nsUri, localName));
			}
		}
		Map<UriRef, PropertyMap> allprops = new HashMap<UriRef, PropertyMap>();

		if (node.hasProperty(RDF.type, HIERARCHY.Collection)) {
			return getCollectionProps(allprops, requestedUserProps, requestedDavProps,
					node, depthHeader, includeValues);
		}else{
			addNodeProperties(allprops, requestedUserProps, requestedDavProps,
					node, includeValues);
			return allprops;
		}

	}
	
	static Map<UriRef, PropertyMap> getCollectionProps(Map<UriRef, PropertyMap> allprops,
			List<Property> requestedUserProps, List<Property> requestedDavProps,
			GraphNode collection, String depthHeader, boolean includeValues) {
		if(allprops == null){
			allprops = new HashMap<UriRef, PropertyMap>();
		}
		addNodeProperties(allprops, requestedUserProps, requestedDavProps, collection,
				includeValues);
		if (depthHeader.equals("1") || depthHeader.equals(infinite)) {
			Iterator<GraphNode> membersIter = collection.getSubjectNodes(HIERARCHY.parent);
			List<GraphNode> members = new ArrayList<GraphNode>();
			while (membersIter.hasNext()) {
				members.add(membersIter.next());
			}
			addMemberProps(allprops, requestedUserProps, requestedDavProps, members,
					depthHeader, includeValues);
		}
		return allprops;
	}

	private static void addMemberProps(Map<UriRef, PropertyMap> allprops,
			List<Property> requestedUserProps, List<Property> requestedDavProps,
			List<GraphNode> members, String depthHeader, boolean includeValues) {
		for (GraphNode member : members) {
			if (depthHeader.equals(infinite) && member.hasProperty(RDF.type, HIERARCHY.Collection)) {
				getCollectionProps(allprops, requestedUserProps, requestedDavProps,
						member, depthHeader, includeValues);
			} else {
				addNodeProperties(allprops, requestedUserProps, requestedDavProps,
						member,	includeValues);
			}
		}
	}

	static void addNodeProperties(Map<UriRef, PropertyMap> allprops,
			List<Property> requestedUserProps, List<Property> requestedDavProps,
			GraphNode node,	boolean includeValues) {

		if (requestedDavProps == null) {
			requestedDavProps = new ArrayList<Property>();
			for(String st : davProps){
				requestedDavProps.add(new Property(davUri, st));
			}
		}
		PropertyMap propertyMap = new PropertyMap();
		if (includeValues) {
			addDavProps(node, propertyMap, requestedDavProps);
			addUserProps(node, propertyMap, requestedUserProps);
		} else {
			addDavPropsWithoutValues(propertyMap);
			addUserPropsWithoutValues(node, propertyMap);
		}
		allprops.put((UriRef) node.getNode(), propertyMap);

	}

	private static void addUserProps(GraphNode node, PropertyMap propertyMap,
			List<Property> requestedProps) {
		Iterator<UriRef> userPropsIter;
		Lock readLock = node.readLock();
		readLock.lock(); 
		try {
			userPropsIter = node.getProperties();
		} finally {
			readLock.unlock();
		}
		Set<UriRef> userProps = new HashSet<UriRef>();
		while (userPropsIter.hasNext()) {
			userProps.add(userPropsIter.next());
		}
		userProps.remove(HIERARCHY.members);
		if (requestedProps != null) {
			for (Property requestedProp : requestedProps) {
				UriRef predicate = new UriRef(requestedProp.value());
				if (userProps.contains(predicate)) {
					readLock.lock();
					try {
						Iterator<Resource> value = node.getObjects(predicate);
						if (value.hasNext()) {
							propertyMap.put(requestedProp, getValue(value.next()));
						} else {
							propertyMap.put(requestedProp, "");
						}
					} finally {
						readLock.unlock();
					}
				} else {
					propertyMap.put(requestedProp, null);
				}
			}
		} else {
			for (UriRef uri : userProps) {
				String userProp = uri.getUnicodeString();
				int index = userProp.lastIndexOf("#");
				if (index == -1) {
					index = userProp.lastIndexOf("/");
				}
				Property property = new Property(userProp.substring(0, index + 1),
						userProp.substring(index + 1));

				Iterator<Resource> value = node.getObjects(uri);
				readLock.lock();
				try {
					if (value.hasNext()) {
						propertyMap.put(property, getValue(value.next()));
					} else {
						propertyMap.put(property, "");
					}
				} finally {
					readLock.unlock();
				}
			}
		}
	}

	private static void addUserPropsWithoutValues(GraphNode node,
			PropertyMap propertyMap) {
		Iterator<UriRef> userPropsIter;
		Lock readLock = node.readLock();
		readLock.lock();
		try {
			userPropsIter = node.getProperties();
		} finally {
			readLock.unlock();
		}
		Set<UriRef> userProps = new HashSet<UriRef>();
		while (userPropsIter.hasNext()) {
			userProps.add(userPropsIter.next());
		}
		userProps.remove(HIERARCHY.members);
		for (UriRef uri : userProps) {
			String userProp = uri.getUnicodeString();
			int index = userProp.lastIndexOf("#");
			if (index == -1) {
				index = userProp.lastIndexOf("/");
			}
			Property property = new Property(userProp.substring(0, index + 1),
					userProp.substring(index + 1));
			propertyMap.put(property, "");
		}
	}

	/**
	 * @param resource
	 * @return returns the unicode string of an UriRef or the lexical form of a
	 * Literal or the return value of a toString() on a BNode
	 */
	private static String getValue(Resource resource){
		if(resource instanceof UriRef){
			return ((UriRef)resource).getUnicodeString();
		}else if(resource instanceof Literal){
			return ((Literal)resource).getLexicalForm();
		}else {
			return resource.toString();
		}
	}

	/**
	 * FIXME find better implementation
	 * @param node
	 * @param propertyMap
	 * @param includeValues
	 * @param requestedProps
	 */
	private static void addDavProps(GraphNode node, PropertyMap propertyMap,
			List<Property> requestedProps) {
		for (Property property : requestedProps) {
			if (davProps.contains(property.prop)) {
				if (property.prop.equalsIgnoreCase(displayname)) {
					propertyMap.put(property, getLastSection(((UriRef)node.getNode()).getUnicodeString()));
				} else if (property.prop.equalsIgnoreCase(resourcetype)) {
					if (node.hasProperty(RDF.type, HIERARCHY.Collection)) {
						propertyMap.put(property, "collection");
					} else {
						propertyMap.put(property, "");
					}
				} else if(property.prop.equalsIgnoreCase(creationdate)){
					Lock readLock = node.readLock();
					readLock.lock();
					try {
						Iterator<Resource> date = node.getObjects(DCTERMS.dateSubmitted);
						if (date.hasNext()) {
							String st = getValue(date.next());
							propertyMap.put(property, st);
						} else {
							propertyMap.put(property, "");
						}
					} finally {
						readLock.unlock();
					}
				} else if(property.prop.equalsIgnoreCase(getlastmodified)){
					Lock readLock = node.readLock();
					readLock.lock();
					try {
						Iterator<Resource> date = node.getObjects(DCTERMS.modified);
						if (date.hasNext()) {
							String st = getValue(date.next());
							propertyMap.put(property, st);
						} else {
							propertyMap.put(property, "");
						}
					} finally {
						readLock.unlock();
					}
				} else if(property.prop.equalsIgnoreCase(getcontenttype)){
					Lock readLock = node.readLock();
					readLock.lock();
					try {
						Iterator<Resource> mediaType = node.getObjects(DCTERMS.MediaType);
						if (mediaType.hasNext()) {
							String st = getValue(mediaType.next());
							propertyMap.put(property, st);
						} else {
							propertyMap.put(property, "");
						}
					} finally {
						readLock.unlock();
					}
				} else {
					propertyMap.put(property, "");
				}
			} else {
				propertyMap.put(property, null);
			}
		}
	}

	private static void addDavPropsWithoutValues(PropertyMap propertyMap) {
		for (String property : davProps) {
				propertyMap.put(new Property(davUri, property), "");
		}
	}

	/*-------------------*
	 * Proppatch methods *
	 *-------------------*/

	static Document modifyProperties(GraphNode hierarchyNode, NodeList propsToSet,
			NodeList propsToRemove) throws ParserConfigurationException {
		Document responseDoc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
		UriRef subject = (UriRef) hierarchyNode.getNode();
		Element hrefElement = responseDoc.createElementNS(davUri, href);
		hrefElement.setTextContent(subject.getUnicodeString());
		Element multistatus = responseDoc.createElementNS(davUri, multistat);
		Element responseElement = responseDoc.createElementNS(davUri, response);
		Element propOk = responseDoc.createElementNS(davUri, prop);
		Element propForbidden = responseDoc.createElementNS(davUri, prop);

		responseDoc.appendChild(multistatus);
		multistatus.appendChild(responseElement);
		responseElement.appendChild(hrefElement);

		Map<Property, String> setMap = getNodeListAsMap(propsToSet);
		Map<Property, String> removeMap = getNodeListAsMap(propsToRemove);
		TripleCollection contentGraph = hierarchyNode.getGraph();
		for(Map.Entry<Property, String> entry : setMap.entrySet()){
			Property property = entry.getKey();
			if(property.ns.equalsIgnoreCase(davUri)){
				if(protectedProps.contains(property.prop)){
					propForbidden.appendChild(responseDoc
							.createElementNS(davUri, property.prop));
				} else {
					UriRef predicate = new UriRef(property.value());
					Lock writeLock = hierarchyNode.writeLock();
					writeLock.lock();
					try {
						Iterator<Resource> valIter = hierarchyNode.getObjects(predicate);
						replaceProp(subject, predicate, valIter, contentGraph, entry);
					} finally {
						writeLock.unlock();
					}
					propOk.appendChild(responseDoc.createElementNS(davUri, property.prop));
				}
			} else {
				UriRef predicate = new UriRef(property.value());
				Lock writeLock = hierarchyNode.writeLock();
				writeLock.lock();
				try {
					Iterator<Resource> valIter = hierarchyNode.getObjects(predicate);
					replaceProp(subject, predicate, valIter, contentGraph, entry);
				} finally {
					writeLock.unlock();
				}
				propOk.appendChild(responseDoc.createElementNS(property.ns, "R:" + property.prop));
			}
		}

		for(Map.Entry<Property, String> entry : removeMap.entrySet()){
			Property property = entry.getKey();
			if(davProps.contains(property.prop)){
				propForbidden.appendChild(responseDoc
							.createElementNS(davUri, property.prop));
			} else {
				UriRef predicate = new UriRef(property.value());
				Lock writeLock = hierarchyNode.writeLock();
				writeLock.lock();
				try {
					Iterator<Resource> valIter = hierarchyNode.getObjects(predicate);
					Set<Triple> triplesToBeRemoved = new HashSet<Triple>();
					while (valIter.hasNext()) {
						triplesToBeRemoved.add(new TripleImpl(subject, predicate, valIter.next()));
					}
					contentGraph.removeAll(triplesToBeRemoved);
				} finally {
					writeLock.unlock();
				}
				propOk.appendChild(responseDoc.createElementNS(property.ns, property.prop));

			}
		}

		if(propOk.hasChildNodes()){
			Element propstatOk = responseDoc.createElementNS(davUri, propstat);
			Element statusOk = responseDoc.createElementNS(davUri, status);
			responseElement.appendChild(propstatOk);
			propstatOk.appendChild(propOk);
			propstatOk.appendChild(statusOk);
			statusOk.setTextContent("HTTP/1.1 200 OK");
		}
		if(propForbidden.hasChildNodes()){
			Element propstatForbidden = responseDoc.createElementNS(davUri, propstat);
			Element statusForbidden = responseDoc.createElementNS(davUri, status);
			responseElement.appendChild(propstatForbidden);
			propstatForbidden.appendChild(propForbidden);
			propstatForbidden.appendChild(statusForbidden);
			statusForbidden.setTextContent("HTTP/1.1 403 Forbidden");
		}

		return responseDoc;
	}

	private static Map<Property, String> getNodeListAsMap(NodeList nodeList) {
		if (nodeList == null) {
			return new HashMap<Property, String>();
		}
		Map<Property, String> result = new HashMap<Property, String>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String propName = node.getLocalName();
			if (propName != null) {
				String nsUri = node.getNamespaceURI();
				result.put(new Property(nsUri, propName), node.getTextContent());
			}
		}
		return result;
	}

	private static void replaceProp(UriRef subject, UriRef predicate,
			Iterator<Resource> valIter, TripleCollection contentGraph,
			Map.Entry<Property, String> entry) {
		LiteralFactory fac = LiteralFactory.getInstance();
		Set<Triple> triplesToBeRemoved = new HashSet<Triple>();
		if (valIter.hasNext()) {
			triplesToBeRemoved.add(new TripleImpl(subject, predicate, valIter.next()));
		}
		contentGraph.removeAll(triplesToBeRemoved);
		contentGraph.add(new TripleImpl(subject, predicate,
				fac.createTypedLiteral(entry.getValue())));
	}

	private static String getLastSection(String s) {
		return s.substring(s.lastIndexOf('/', s.length()-2));
	}

	/**
	 * Helper class whicht is a {@link HashMap} that maps {@link Property} to a {@link String}
	 * @author ali
	 */
	@SuppressWarnings("serial")
	static class PropertyMap extends HashMap<Property, String> {
	}

	static class Property {
		final String ns;
		final String prop;

		public Property(String ns, String prop) {
			this.ns = ns;
			this.prop = prop;
		}

		public String value(){
			return ns+prop;
		}
	}

}
