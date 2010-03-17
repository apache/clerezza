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
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.platform.content.WebDavUtils.PropertyMap;
import org.apache.clerezza.platform.content.hierarchy.CollectionNode;
import org.apache.clerezza.platform.content.hierarchy.HierarchyNode;
import org.apache.clerezza.platform.content.hierarchy.HierarchyService;
import org.apache.clerezza.platform.content.hierarchy.HierarchyUtils;
import org.apache.clerezza.platform.content.hierarchy.IllegalMoveException;
import org.apache.clerezza.platform.content.hierarchy.NodeAlreadyExistsException;
import org.apache.clerezza.platform.content.hierarchy.NodeDoesNotExistException;
import org.apache.clerezza.platform.content.hierarchy.UnknownRootExcetpion;
import org.apache.clerezza.platform.content.webdav.MKCOL;
import org.apache.clerezza.platform.content.webdav.MOVE;
import org.apache.clerezza.platform.content.webdav.PROPFIND;
import org.apache.clerezza.platform.content.webdav.PROPPATCH;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typehandlerspace.OPTIONS;
import org.apache.clerezza.platform.typehandlerspace.SupportedTypes;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.web.fileserver.util.MediaTypeGuesser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wymiwyg.wrhapi.HeaderName;

/**
 * This Class allows getting and putting content structured using the
 * Discobits ontology.
 *
 * Is an implementation of DiscobitsHandler and additionally registers as
 * TypeHanlder to allow HTTP GET and PUT.
 *
 * @author reto, tho, agron, mir
 */
@Component
@Services({
	@Service(Object.class),
	@Service(DiscobitsHandler.class)
})
@Property(name="org.apache.clerezza.platform.typehandler", boolValue=true)
@Reference(name="metaDataGenerator",
	policy=ReferencePolicy.DYNAMIC,
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	referenceInterface=MetaDataGenerator.class
)
@SupportedTypes(types = { "http://www.w3.org/2000/01/rdf-schema#Resource" }, prioritize = false)
public class DiscobitsTypeHandler extends AbstractDiscobitsHandler
		implements DiscobitsHandler {

	@Reference
	protected ContentGraphProvider cgProvider;

	@Reference
	protected HierarchyService hierarchyService;
	
	private static final Logger logger = LoggerFactory.getLogger(DiscobitsTypeHandler.class);

	private Set<MetaDataGenerator> metaDataGenerators =
			Collections.synchronizedSet(new HashSet<MetaDataGenerator>());

	/**
	 * TypeHandle method for rdf types "TitledContext", "InfoDiscoBit",
	 * "OrderedContent" and "XHTMLInfoDiscoBit".
	 * 
	 * @param uriInfo
	 * @return
	 */
	@GET
	@Produces({"*/*"})
	public Object getResource(@Context UriInfo uriInfo) {
		final MGraph mGraph = cgProvider.getContentGraph();
		final UriRef uri = new UriRef(uriInfo.getAbsolutePath().toString());
		final GraphNode graphNode = new GraphNode(uri, mGraph);
		InfoDiscobit infoDiscobit = InfoDiscobit.createInstance(graphNode);
		if (infoDiscobit != null) {
			return infoDiscobit;
		} else {
			if (mGraph.filter(uri, null, null).hasNext() ||
					mGraph.filter(null, null, uri).hasNext()) {
				return graphNode;
			} else {
				UriRef slashedUri = new UriRef(uri.getUnicodeString()+"/");
				if (mGraph.filter(slashedUri, null, null).hasNext() ||
					mGraph.filter(null, null, slashedUri).hasNext()) {
					RedirectUtil.createSeeOtherResponse(
							slashedUri.getUnicodeString(), uriInfo);
				}
			}
		}
		throw new WebApplicationException(RuntimeDelegate.getInstance()
				.createResponseBuilder().status(Status.NOT_FOUND)
				.entity("Sorry, we know nothing about this resource.").build());
	}

	
	/**
	 * Creates an <code>InfoDiscoBit</code> at the specified location
	 *
	 * @param uriInfo the uri of the InforDiscoBit to be created
	 * @param data the content of the upload
	 */
	@PUT
	public Response putInfoDiscobit(@Context UriInfo uriInfo, @Context HttpHeaders headers, byte[] data) {
		final String contentType;
		{
			final List<String> contentTypeHeaders = headers.getRequestHeader(HttpHeaders.CONTENT_TYPE);
			if (contentTypeHeaders == null) {
				logger.warn("Content-Type not specified");
				final MediaType guessTypeForName = MediaTypeGuesser.getInstance().
						guessTypeForName(uriInfo.getAbsolutePath().toString());
				contentType = guessTypeForName == null ?
					MediaType.APPLICATION_OCTET_STREAM : guessTypeForName.toString();
			} else {
				contentType = contentTypeHeaders.get(0);
			}
			
		}
	
		final UriRef infoDiscoBitUri = new UriRef(uriInfo.getAbsolutePath().toString());
		put(infoDiscoBitUri, MediaType.valueOf(contentType), data);
		return Response.status(Status.CREATED).build();
	}

	/**
	 * Creates a new collection at the specified uri
	 *
	 * @param uriInfo
	 * @return
	 * <ul>
	 *	<li>201 "Created" response if method succeeded
	 *	<li>405 "Method Not Allowed" response if collection already exists
	 * </ul>
	 */
	@MKCOL
	public Object mkcol(@Context UriInfo uriInfo) {
		UriRef nodeUri = new UriRef(uriInfo.getAbsolutePath().toString());
		if (!nodeUri.getUnicodeString().endsWith("/")) {
			nodeUri = new UriRef(nodeUri.getUnicodeString() + "/");
		}
		try {
			hierarchyService.createCollectionNode(nodeUri);
		} catch (NodeAlreadyExistsException e) {
			logger.debug("Collection \""
					+ nodeUri.getUnicodeString() + "\" already exists.");
			return Response.status(405) // Method Not Allowed
					.entity("Collection \"" + nodeUri.getUnicodeString()
					+ "\" already exists.").build();
		}
		return Response.created(uriInfo.getAbsolutePath()).build();
	}

	/**
	 * Finds all properties of a hierarchy node and returns them in a
	 * {@link DOMSource}
	 *
	 * @param uriInfo
	 * @param headers {@link HttpHeaders}
	 * @param body {@link DOMSource} containing the requested properties, can be null
	 * @return
	 * <ul>
	 *	<li>207 "Multistatus" response if method succeeded
	 *	<li>404 "Not Found" response if the hierarchy node was not found
	 *	<li>400 "Bad Request" response if the body is malformed
	 * </ul>
	 */
	@PROPFIND
	@Consumes({"application/xml", "text/xml", "*/*"})
	@Produces({"application/xml", "text/xml", "*/*"})
	public Response propfind(@Context UriInfo uriInfo,
			@Context HttpHeaders headers, DOMSource body) {
		final UriRef nodeUri = new UriRef(uriInfo.getAbsolutePath().toString());
		if (!nodeAtUriExists(nodeUri)) {
			return checkIfCollectionExistsAndRedirectIfSo(nodeUri, uriInfo);
		}
			Map<UriRef, PropertyMap> result;
			try {
				String depthHeader = WebDavUtils.getHeaderAsString(headers, "Depth");
				if (depthHeader == null) {
					depthHeader = WebDavUtils.infinite;
				}
				HierarchyNode node = hierarchyService.getHierarchyNode(nodeUri);
				if (body != null) {
					Document requestDoc = WebDavUtils.sourceToDocument(body);
					Node propfindNode = WebDavUtils.getNode(requestDoc, WebDavUtils.propfind);
					Node requestNode = WebDavUtils.getFirstChild(propfindNode);
					String requestType = requestNode.getLocalName();
					if (requestType.equalsIgnoreCase(WebDavUtils.allprop)) {
						result = getAllProps(node, depthHeader);
					} else if (requestType.equalsIgnoreCase(WebDavUtils.prop)) {
						result = getPropsByName(requestNode, node, depthHeader);
					} else if (requestType.equalsIgnoreCase(WebDavUtils.propname)) {
						result = getPropNames(node, depthHeader);
					} else {
						return Response.status(Status.BAD_REQUEST).build();
					}
				} else {
					// returns all properties
					result = getAllProps(node, depthHeader);
				}
				Document responseDoc = WebDavUtils.createResponseDoc(result);
				return Response.status(207).entity(new DOMSource(responseDoc)).type(
						MediaType.APPLICATION_XML_TYPE).build();
			} catch (NodeDoesNotExistException e) {
				return Response.status(Status.NOT_FOUND).entity(
						e.getMessage()).build();
			} catch (TransformerFactoryConfigurationError e) {
				return Response.status(Status.BAD_REQUEST).build();
			} catch (TransformerException e) {
				return Response.status(Status.BAD_REQUEST).build();
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			} catch(UnknownRootExcetpion ex) {
				return Response.status(Status.BAD_REQUEST).build();
			}
	}

	Map<UriRef, PropertyMap> getPropNames(HierarchyNode node, String depthHeader) {
		Map<UriRef, PropertyMap> result = new HashMap<UriRef, PropertyMap>();
		WebDavUtils.addNodeProperties(result, null, null, node, false);
		return result;
	}

	Map<UriRef, PropertyMap> getPropsByName(Node requestNode, HierarchyNode node,
			String depthHeader) {
		Map<UriRef, PropertyMap> result;
		NodeList children = requestNode.getChildNodes();
		result = WebDavUtils.getPropsByName(children, node, "0", true);
		return result;
	}

	Map<UriRef, PropertyMap> getAllProps(HierarchyNode node, String depthHeader) {
		HashMap<UriRef, PropertyMap> result = new HashMap<UriRef, PropertyMap>();
		WebDavUtils.addNodeProperties(result, null, null, node, true);
		return result;
	}

	/**
	 * @param uriInfo
	 * @param body {@link DOMSource} containing properties which should be set
	 * or deleted
	 * @return
	 * <ul>
	 *	<li>207 "Multistatus" response if method succeeded
	 *	<li>404 "Not Found" response if the hierarchy node was not found
	 *	<li>400 "Bad Request" response if the body is malformed
	 * </ul>
	 */
	@PROPPATCH
	@Consumes({"application/xml", "text/xml", "*/*"})
	@Produces({"application/xml", "text/xml", "*/*"})
	public Response proppatch(@Context UriInfo uriInfo, DOMSource body) {
		UriRef nodeUri = new UriRef(uriInfo.getAbsolutePath().toString());
		if (!nodeAtUriExists(nodeUri)) {
			return checkIfCollectionExistsAndRedirectIfSo(nodeUri, uriInfo);
		}
		try {
			Document requestDoc = WebDavUtils.sourceToDocument(body);
			HierarchyNode node = hierarchyService.getHierarchyNode(nodeUri);
			NodeList propsToSet = null;
			NodeList propsToRemove = null;
			Node proppatchNode = WebDavUtils.getNode(requestDoc, WebDavUtils.proppatch);
			NodeList childNodes = proppatchNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				String localName = child.getLocalName();
				if (localName != null) {
					if (localName.equals(WebDavUtils.set)) {
						propsToSet = child.getFirstChild().getChildNodes();
					} else if (localName.equals(WebDavUtils.remove)) {
						propsToRemove = child.getFirstChild().getChildNodes();
					}
				}
			}
			Document responseDoc = WebDavUtils.modifyProperties(node, propsToSet, propsToRemove);
			return Response.status(207).entity(new DOMSource(responseDoc)).type(
					MediaType.APPLICATION_XML_TYPE).build();
		} catch (NodeDoesNotExistException e) {
			return Response.status(Status.NOT_FOUND).entity(
					e.getMessage()).build();
		}catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		} catch (TransformerFactoryConfigurationError ex) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (TransformerException ex) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch(UnknownRootExcetpion ex) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	/**
	 * Moves or renames a hierarchy node
	 *
	 * @param uriInfo
	 * @param headers
	 * @return
	 * <ul>
	 *  <li>201 "Created" response if method succeeded
	 *  <li>412 "Precondition Failed" response if the destination URL is already
	 *	mapped to a resource
	 *  <li>404 "Not Found" response if the hierarchy node was not found
	 *  <li>403 "Forbidden" response if the source and destination resources are the same
	 *  <li>400 "Bad Request" if no "Destination" header was found
	 * </ul>
	 */
	@MOVE
	public Response move(@Context UriInfo uriInfo, @Context HttpHeaders headers) {
		final UriRef nodeUri = new UriRef(uriInfo.getAbsolutePath().toString());
		if (!nodeAtUriExists(nodeUri)) {
			return checkIfCollectionExistsAndRedirectIfSo(nodeUri, uriInfo);
		}
		HierarchyNode targetNode;
		String overwriteHeader = null;
		CollectionNode newParentCollection = null;
		try {
			targetNode = hierarchyService.getHierarchyNode(nodeUri);
			/* ignored at the moment */
			overwriteHeader = WebDavUtils.getHeaderAsString(headers, "Overwrite");
			if (overwriteHeader == null) {
				overwriteHeader = "F";
			}
			String newCollectionString = WebDavUtils.getHeaderAsString(headers,
					"Destination");
			if (newCollectionString != null) {				
				UriRef newParentUri = HierarchyUtils.extractParentCollectionUri(
						new UriRef(newCollectionString));
				newParentCollection = hierarchyService
						.getCollectionNode(newParentUri);
				targetNode.move(newParentCollection, HierarchyUtils.getName(
						new UriRef(newCollectionString)), newParentCollection
						.getMembers().size());
				return Response.created(new java.net.URI(newCollectionString))
						.build();
			} else {
				logger.error("empty Destination header!");
				return Response.status(Status.BAD_REQUEST).build();
			}
		} catch (URISyntaxException e) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (NodeDoesNotExistException e) {
			return Response.status(Status.NOT_FOUND).build();
		} catch (NodeAlreadyExistsException e) {
			if (overwriteHeader.equals("F")) {
				return Response.status(Status.PRECONDITION_FAILED).build();
			} else if (overwriteHeader.equals("T")) {
				try {
					String name = HierarchyUtils.getName(nodeUri);
					hierarchyService.getHierarchyNode(
							new UriRef(newParentCollection.getNode().
							getUnicodeString() + name)).delete();
					return this.move(uriInfo, headers);
				} catch (NodeDoesNotExistException ex) {
					throw new RuntimeException(e);
				} catch (UnknownRootExcetpion ex) {
					throw new RuntimeException(ex);
				}
			} else {
				return Response.status(Status.BAD_REQUEST).build();
			}
		} catch (IllegalMoveException e) {
			return Response.status(Status.FORBIDDEN).build();
		} catch (UnknownRootExcetpion ex) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	/**
	 * Deletes a hierarchy node
	 *
	 * @param uriInfo
	 * @return
	 * <ul>
	 *	<li>200 "OK" response if method succeeded
	 *	<li>404 "Not Found" response if the hierarchy node was not found
	 * </ul>
	 */
	@DELETE
	public Response delete(@Context UriInfo uriInfo) {
		final UriRef nodeUri = new UriRef(uriInfo.getAbsolutePath().toString());
		if (!nodeAtUriExists(nodeUri)) {
			return checkIfCollectionExistsAndRedirectIfSo(nodeUri, uriInfo);
		}
		UriRef hierarchyNodeUri = new UriRef(uriInfo.getAbsolutePath().toString());
		HierarchyNode hierarchyNode;
		try {
			hierarchyNode = hierarchyService.getHierarchyNode(hierarchyNodeUri);
		} catch (NodeDoesNotExistException e) {
			return Response.status(Status.NOT_FOUND).entity(
					uriInfo.getAbsolutePath()).build();
		} catch (UnknownRootExcetpion ex) {
			return Response.status(Status.NOT_FOUND).entity(
					uriInfo.getAbsolutePath()).build();
		}
		hierarchyNode.delete();
		return Response.ok().build();
	}

	/**
	 * @param uriInfo
	 * @return
	 * <ul>
	 *	<li>200 "OK" response with an "Allow" and a "DAV" header. The "Allow"
	 * header contains all the possible HTTP methods that can be executed on the
	 * resource and the "DAV" header shows if the resource is WebDAV enabled
	 *	<li>404 "Not Found" response if the resource was not found
	 * </ul>
	 */
	@OPTIONS
	public Response options(@Context UriInfo uriInfo) {
		final UriRef nodeUri = new UriRef(uriInfo.getAbsolutePath().toString());
		if (!nodeAtUriExists(nodeUri)) {
			return checkIfCollectionExistsAndRedirectIfSo(nodeUri, uriInfo);
		}
			Response.ResponseBuilder builder = Response.ok();
			builder.header(HeaderName.DAV.toString(), "1");
			Set<String> allow = new HashSet<String>();
			Method[] methods = this.getClass().getMethods();
			for (Method method : methods) {
				for (Annotation annotation : method.getAnnotations()) {
					HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
					if (httpMethod != null) {
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

	protected void bindMetaDataGenerator(MetaDataGenerator generator) {
		metaDataGenerators.add(generator);
	}

	protected void unbindMetaDataGenerator(MetaDataGenerator generator) {
		metaDataGenerators.remove(generator);
	}

	@Override
	protected MGraph getMGraph() {
		return cgProvider.getContentGraph();
	}

	@Override
	protected Set<MetaDataGenerator> getMetaDataGenerators() {
		return metaDataGenerators;
	}

	@Override
	protected HierarchyService getHierarchyService() {
		return hierarchyService;
	}	

	private boolean nodeAtUriExists(UriRef nodeUri) {
		MGraph mGraph = getMGraph();
		return mGraph.filter(nodeUri, null, null).hasNext()
				|| mGraph.filter(null, null, nodeUri).hasNext();
	}

	private Response checkIfCollectionExistsAndRedirectIfSo(UriRef nodeUri,
			UriInfo uriInfo) {
		UriRef slashedUri = new UriRef(nodeUri.getUnicodeString() + "/");
		if (collectionExists(slashedUri)) {
			return RedirectUtil.createSeeOtherResponse(
					slashedUri.getUnicodeString(), uriInfo);
		}
		return Response.status(Status.NOT_FOUND).build();
	}
	
	private boolean collectionExists(UriRef slashedUri) {
		MGraph mGraph = getMGraph();
		return mGraph.filter(slashedUri, null, null).hasNext() ||
				mGraph.filter(null, null, slashedUri).hasNext();
	}
}
