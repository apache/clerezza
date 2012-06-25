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

import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * An implementation of the <code>CallbackRenderer</code> is passed to a
 * <code>Renderlet</code>. The renderlet uses the callback renderer to render
 * <code>GraphNode</code>s. The media type of the rendered GraphNode is the same
 * as the media type of the output of the calling renderlet.
 *
 * @author mir, reto
 */
public interface CallbackRenderer {

	/**
	 * Renders the specified resource and context in the specified mode to an outputstream
	 *
	 * @param resource
	 * @param context
	 * @param mode
	 * @param os
	 * @throws IOException
	 */
	public void render(GraphNode resource, GraphNode context, String mode, 
			OutputStream os) throws IOException;

	/**
	 * Renders a specified named resource using the GraphNode returned by
	 * <code>org.apache.clerezza.platform.graphnodeprovider.GraphNodeProvider#get(org.apache.clerezza.rdf.core.UriRef)</code>.
	 *
	 * Otherwise same as render(GraphNode, .GraphNode, String, OutputStream)
	 *
	 * @param resource
	 * @param context
	 * @param mode
	 * @param os
	 * @throws IOException
	 */
	public void render(UriRef resource, GraphNode context, String mode,
			OutputStream os) throws IOException;

}
