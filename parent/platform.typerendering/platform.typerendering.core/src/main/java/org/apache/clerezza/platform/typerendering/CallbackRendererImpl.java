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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.platform.graphnodeprovider.GraphNodeProvider;

/**
 *
 * @author mir, reto
 */
public class CallbackRendererImpl implements CallbackRenderer {
	private List<MediaType> mediaTypeList;
	private final RendererFactory manager;
	private final GraphNodeProvider graphNodeProvider;
	private final UriInfo uriInfo;
	private final HttpHeaders requestHeaders;
	private final MultivaluedMap<String, Object> responseHeaders;
	private final Map<String, Object> sharedRenderingValue;

	CallbackRendererImpl(RendererFactory manager, GraphNodeProvider graphNodeProvider, UriInfo uriInfo,
						 HttpHeaders requestHeaders, MultivaluedMap<String, Object> responseHeaders, MediaType mediaType, Map<String, Object> sharedRenderingValue) {
		this.uriInfo = uriInfo;
		this.requestHeaders = requestHeaders;
		this.responseHeaders = responseHeaders;
		this.mediaTypeList = Collections.singletonList(mediaType);
		this.manager = manager;
		this.graphNodeProvider = graphNodeProvider;
		this.sharedRenderingValue = sharedRenderingValue;
	}
	
	@Override
	public void render(GraphNode resource, GraphNode context, String mode, 
			OutputStream os) throws IOException {
		Renderer renderer = manager.createRenderer(resource, mode, mediaTypeList);
		if (renderer == null) {
			throw new RuntimeException("no renderer could be created for "+
					resource+" (in "+resource.getNodeContext()+"), "+mode+","+mediaTypeList);
		}
		renderer.render(resource, context, mode, uriInfo, requestHeaders, responseHeaders,
				sharedRenderingValue, os);
	}

	@Override
	public void render(final UriRef resource, GraphNode context, String mode,
			OutputStream os) throws IOException {
		final GraphNode resourceNode = AccessController.doPrivileged( new PrivilegedAction<GraphNode>() {
					@Override
					public GraphNode run() {
						return graphNodeProvider.get(resource);
					}
				});
		render(resourceNode, context, mode, os);
	}

}
