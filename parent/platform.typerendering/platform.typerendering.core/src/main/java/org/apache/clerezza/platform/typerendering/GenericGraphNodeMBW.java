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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Generates output based on the rendering mode passed as query parameter ("mode")
 * and the rdf type of the given GraphNode.
 * 
 * @author daniel, mir, rbn
 */
@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Reference(name = "contextProvider", referenceInterface = UserContextProvider.class,
cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
policy = ReferencePolicy.DYNAMIC)
@Provider
@Produces({"application/xhtml+xml", "*/*"})
public class GenericGraphNodeMBW implements MessageBodyWriter<GraphNode> {

	public static final String MODE = "mode";
	
	@Reference
	RendererFactory rendererFactory;

	private static final Logger logger = LoggerFactory.getLogger(GenericGraphNodeMBW.class);
	private UriInfo uriInfo = null;
	private HttpHeaders headers = null;
	private final Set<UserContextProvider> contextProviders =
			Collections.synchronizedSet(new HashSet<UserContextProvider>());
	private final DocumentBuilder documentBuilder;
	private TransformerFactory transformerFactory = TransformerFactory.newInstance();

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

	@Context
	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}


	@Context
	public void setHttpHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	@Override
	public long getSize(GraphNode node, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return GraphNode.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(GraphNode node, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		String mode = getRenderingMode();

		Renderer renderer = rendererFactory.createRenderer(node, mode,
				headers.getAcceptableMediaTypes());

		if (renderer == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No suitable renderer found").build());
		}
		final MediaType rendererMediaType = renderer.getMediaType();
		Map<String, Object> sharedRenderingValues = new HashMap<String, Object>();
		ResultDocModifier.init();
		if (!(rendererMediaType.getType().equals("application") && rendererMediaType.getSubtype().equals("xhtml+xml"))) {
			httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, rendererMediaType);
			renderer.render(node, getUserContext(), mode, uriInfo, headers, httpHeaders, sharedRenderingValues, entityStream);
		} else {
			final MediaType mediaTypeWithCharset = MediaType.valueOf(MediaType.APPLICATION_XHTML_XML+";charset=UTF-8");
			httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, mediaTypeWithCharset);
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				renderer.render(node, getUserContext(), mode, uriInfo, headers, httpHeaders, sharedRenderingValues, baos);
				final byte[] bytes = baos.toByteArray();
				if (!ResultDocModifier.getInstance().isModified()) {
					entityStream.write(bytes);
					return;
				}
				Document document;
				try {
					synchronized(documentBuilder) {
						document = documentBuilder.parse(new ByteArrayInputStream(bytes));
					}
				} catch (SAXException ex) {
					logger.error("Error parsing XHTML", ex);
					entityStream.write(bytes);
					return;
				}
				ResultDocModifier.getInstance().addToDocument(document);
				Transformer transformer;
				try {
					transformer = transformerFactory.newTransformer();
				} catch (TransformerConfigurationException ex) {
					throw new RuntimeException(ex);
				}
				DOMSource source = new DOMSource(document);
				StreamResult result = new StreamResult(entityStream);
				try {
					transformer.setOutputProperty("omit-xml-declaration","yes");
					transformer.transform(source, result);
				} catch (TransformerException ex) {
					throw new RuntimeException(ex);
				}
			} finally {
				ResultDocModifier.dispose();
			}
		}
	}

	private String getRenderingMode() {
		if (uriInfo == null) {
			return null;
		}
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		List<String> modes = queryParams.get(MODE);
		if ((modes == null) || (modes.size() == 0)) {
			return null;
		}
		return modes.get(0);
	}

	protected void bindContextProvider(UserContextProvider provider) {
		contextProviders.add(provider);
	}

	protected void unbindContextProvider(UserContextProvider provider) {
		contextProviders.remove(provider);
	}

	private GraphNode getUserContext() {
		GraphNode contextNode = new GraphNode(new BNode(), new SimpleMGraph());
		synchronized(contextProviders) {
			Iterator<UserContextProvider> providersIter = contextProviders.iterator();
			while (providersIter.hasNext()) {
				UserContextProvider userContextProvider = providersIter.next();
				contextNode = userContextProvider.addUserContext(contextNode);
			}
		}
		return contextNode;
	}
}
