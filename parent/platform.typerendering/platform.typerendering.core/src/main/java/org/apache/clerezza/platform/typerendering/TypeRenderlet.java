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

import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import javax.ws.rs.core.*;

import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A typerenderlet creates a representation of resource described in a graph.
 *
 * @author reto
 */
public interface TypeRenderlet {

	/**
	 * A class repressing properties of the rendering request within which the
	 * TypeRenderlet is used. It also allows access to contextual rendering services.
	 */
	static class RequestProperties {

		private UriInfo uriInfo;
		private MultivaluedMap<String, Object> responseHeaders;
		private HttpHeaders requestHeaders;
		private final BundleContext bundleContext;
		private final String mode;
		private final MediaType mediaType;

		public RequestProperties(UriInfo uriInfo,
				HttpHeaders requestHeaders,
				MultivaluedMap<String, Object> responseHeaders,
				String mode,
				MediaType mediaType,
				BundleContext bundleContext) {
			this.uriInfo = uriInfo;
			this.requestHeaders = requestHeaders;
			this.responseHeaders = responseHeaders;
			this.mode = mode;
			this.mediaType = mediaType;
			this.bundleContext = bundleContext;
		}

		public HttpHeaders getRequestHeaders() {
			return requestHeaders;
		}

		public MultivaluedMap<String, Object> getResponseHeaders() {
			return responseHeaders;
		}

		public UriInfo getUriInfo() {
			return uriInfo;
		}

		/**
		 * the media type of the representation to be produced.
		 *
		 * @return the concrete MediaType
		 */
		public MediaType getMediaType() {
			return mediaType;
		}

		/**
		 * the mode the TypeRenderlet is invoked with, this is mainly used
		 * so that the callbackRenderer can be called inheriting the mode.
		 *
		 * @return
		 */
		public String getMode() {
			return mode;
		}

		/**
		 * Rendering services
		 *
		 * @param type
		 * @return a instance of the requested rendering services
		 */
		public <T> T getRenderingService(final Class<T> type) {
			return AccessController.doPrivileged(
					new PrivilegedAction<T>() {

						@Override
						public T run() {
							ServiceReference serviceReference = RequestProperties.this.bundleContext.getServiceReference(type.getName());
							if (serviceReference != null) {
								T resultCandidate = (T) bundleContext.getService(serviceReference);
								if (resultCandidate.getClass().getAnnotation(WebRenderingService.class) != null) {
									return resultCandidate;
								} else {
									return null;
								}
							} else {
								return null;
							}
						}
					});
		}
	}

	/**
	 * @return the rdf type rendered by this renderlet
	 */
	UriRef getRdfType();

	/**
	 * The renderer may render resources in different modes. Such a mode is
	 * typically set when a renderlet calls back the renderer to delegate a
	 * referenced resource.
	 *
	 * @return a regex the mode must match
	 */
	String getModePattern();

	/**
	 *
	 * @return the Media Type pattern this TypeRenderlet supports
	 */
	MediaType getMediaType();

	/**
	 * Renders a node, possibly considering the context of the rendering request
	 * (e.g. for which user the resource is rendered for) to a stream.
	 *
	 * @param node  RDF resource to be rendered with the template.
	 * @param context  RDF resource providing a rendering context, typically this
	 *		  contains a description of the user for which the resource is to be rendered
	 * @param sharedRenderingValues	a map that can be used for sharing values
	 * across the different Renderlets involved in a rendering process
	 * @param callbackRenderer  renderer for call backs.
	 * @param requestProperties properties of this rendering request
	 * @param os  where the output will be written to.
	 */
	public void render(GraphNode node,
			GraphNode context,
			Map<String, Object> sharedRenderingValues,
			CallbackRenderer callbackRenderer,
			RequestProperties requestProperties,
			OutputStream os) throws IOException;
}
