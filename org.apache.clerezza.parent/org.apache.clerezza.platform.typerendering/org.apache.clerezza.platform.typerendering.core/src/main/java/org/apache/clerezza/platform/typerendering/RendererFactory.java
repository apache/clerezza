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

import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * Creates a <code>Renderer</code> which can used to render a <code>GraphNode</code>.
 *
 * @author mir
 */
public interface RendererFactory {

	/**
	 * Creates a <code>Renderer</code> for the specified mode, acceptable 
	 * media-types as well as the types of <code>GraphNode</code>.
	 * The <code>acceptableMediaTypes</code> list represent the media
	 * types that are acceptable for the rendered output. The list has a
	 * order where the most desirable media type is a the beginning of the list.
	 * The media type of the rendered output will be compatible to at least one
	 * media type in the list.
	 *
	 * @param resource The <code>GraphNode</code> to be rendered
	 * @param mode mode
	 * @param acceptableMediaTypes acceptable media types for the rendered output
	 * @return the Renderer or null if no renderer could be created for the specified parameters
	 */
	public Renderer createRenderer(GraphNode resource, String mode,
			List<MediaType> acceptableMediaTypes);
}
