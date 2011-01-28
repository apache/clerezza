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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 *
 * @author mir
 */
public class CallbackRendererImpl implements CallbackRenderer {
	private List<MediaType> mediaTypeList;
	RenderletRendererFactoryImpl manager;
	private final UriInfo uriInfo;
	private final MultivaluedMap<String, Object> httpHeaders;
	private final Map<String, Object> sharedRenderingValue;

	CallbackRendererImpl(RenderletRendererFactoryImpl manager, UriInfo uriInfo,
			MultivaluedMap<String, Object> httpHeaders, MediaType mediaType, Map<String, Object> sharedRenderingValue) {
		this.uriInfo = uriInfo;
		this.httpHeaders = httpHeaders;
		this.mediaTypeList = Collections.singletonList(mediaType);
		this.manager = manager;
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
		renderer.render(resource, context, uriInfo, httpHeaders,
				sharedRenderingValue, os);
	}

}
