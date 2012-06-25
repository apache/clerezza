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

import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A renderlet renders a <code>GraphNode</code> with the optionally specified
 * rendering specification (e.g. a template).
 *
 * @deprecated used {@TypeRenderlet} instead
 * @author daniel, mir, reto
 */
public interface Renderlet {

	/**
	 * A class repressing properties of the http request within which the
	 * Renderlet is used, it also allows access to contextual rendering services.
	 */
	static class RequestProperties {
		private UriInfo uriInfo;
		private MultivaluedMap<String, Object> responseHeaders;
		private HttpHeaders requestHeaders;
		public final BundleContext bundleContext;      //public only to test an idea

		public RequestProperties(UriInfo uriInfo, 
				HttpHeaders requestHeaders,
				MultivaluedMap<String, Object> responseHeaders,
				BundleContext bundleContext) {
			this.uriInfo = uriInfo;
			this.requestHeaders = requestHeaders;
			this.responseHeaders = responseHeaders;
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
							ServiceReference serviceReference = bundleContext.getServiceReference(type.getName());
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
	 * Renders the data from <code>res</code> with a appropriate rendering
	 * engine.
	 *
	 * @param res  RDF resource to be rendered with the template.
	 * @param context  RDF resource providing a rendering context.
	 * @param sharedRenderingValues	a map that can be used for sharing values
	 * across the different Renderlets involved in a rendering process
	 * @param callbackRenderer  renderer for call backs.
	 * @param renderingSpecification  the rendering specification
	 * @param mediaType  the media type this media produces (a part of)
	 * @param mode  the mode this Renderlet was invoked with, this is mainly used
	 * so that the callbackRenderer can be claeed inheriting the mode.
	 * @param requestProperties properties of the http request, may be null
	 * @param os  where the output will be written to.
	 */
	public void render(GraphNode res,
			GraphNode context,
			Map<String, Object> sharedRenderingValues,
			CallbackRenderer callbackRenderer,
			URI renderingSpecification,
			String mode,
			MediaType mediaType,
			RequestProperties requestProperties,
			OutputStream os) throws IOException;
}
