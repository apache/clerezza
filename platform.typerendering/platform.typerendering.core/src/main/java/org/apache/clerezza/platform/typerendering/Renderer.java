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
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * A instance of this class is created by a <code>RendererFactory</code>
 * implementation. <code>Renderer</code> is used to render <code>GraphNode</code>s.
 *
 * The difference between this and a TYpeRenderer is that this renders
 * a node without doing CallBacks, while a TypeRenderlet might delegate parts of
 * the rendering by doing callbacks.
 *  
 * @author mir, reto
 */
public interface Renderer  {


	/**
	 * Returns a <code>MediaType</code> Object representing the media type
	 * that will be produced <code>GraphNode</code>.
	 * @return
	 */
	public MediaType getMediaType();

	/**
	 * Renders the data from <code>resource</code> with the renderlet and
	 * rendering specification.
	 *
	 * @param res  RDF resource to be rendered with the template.
	 * @param context RDF resource providing a rendering context.
	 * @param uriInfo the uriInfo of the request, the renderlet may use
	 * @param httpHeaders the http-headers of the request
	 * @param os  where the output will be written to.
	 */
	public void render(GraphNode node, GraphNode userContext, String mode, UriInfo uriInfo,
			HttpHeaders requestHeaders,
			MultivaluedMap<String, Object> responseHeaders,
			Map<String, Object> sharedRenderingValues, OutputStream entityStream)
			throws IOException;
}
