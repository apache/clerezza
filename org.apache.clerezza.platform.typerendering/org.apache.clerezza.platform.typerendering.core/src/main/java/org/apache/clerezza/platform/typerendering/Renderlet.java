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
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * A renderlet renders a <code>GraphNode</code> with the optionally specified
 * rendering specification (e.g. a template).
 * 
 * @author daniel, mir, reto
 */
public interface Renderlet {

	/**
	 * A class repressing properties of the http request within which the
	 * Renderlet is used
	 */
	static class RequestProperties {
		private UriInfo uriInfo;
		private MultivaluedMap<String, Object> httpHeaders;

		public RequestProperties(UriInfo uriInfo, 
				MultivaluedMap<String, Object> httpHeaders) {
			this.uriInfo = uriInfo;
			this.httpHeaders = httpHeaders;
		}

		public MultivaluedMap<String, Object>  getHttpHeaders() {
			return httpHeaders;
		}

		public UriInfo getUriInfo() {
			return uriInfo;
		}

	}
	
	/**
	 * Renders the data from <code>res</code> with a appropriate rendering
	 * engine.
	 *
	 * @param res  RDF resource to be rendered with the template.
	 * @param context  RDF resource providing a redering context.
	 * @param sharedRenderingValues	a map that can be used for sharing values
	 * across the different renderlets involved in a rendering process
	 * @param callbackRenderer  renderer for call backs.
	 * @param renderingSpecification  the rendering specification
	 * @param mediaType  the media type this media produces (a part of)
	 * @param mode  the mode this Renderlet was invoked with, this is mainly used
	 * so that the callbackeRenderer can be claeed inheriting the mode.
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
