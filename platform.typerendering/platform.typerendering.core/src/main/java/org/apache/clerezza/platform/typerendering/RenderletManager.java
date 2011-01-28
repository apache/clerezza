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

import javax.ws.rs.core.MediaType;
import org.apache.clerezza.rdf.core.UriRef;

/**
 * Over the RenderletManager renderlets can be registered.
 *
 * @author mir
 */
public interface RenderletManager {

	/**
	 * Registeres a renderlet.
	 * 
	 * For the same rdfType, mediaType and Mode at motst one built-in and one 
	 * non-built-in renderlet can be registered. An attempt to register a second 
	 * renderlet results in the unregistration of the previously registered one 
	 * 
	 * @param renderingSpecification the argument that is passed to the
	 * 	renderlet.
	 * @param rdfType defines the RDF-type to be rendered with specified renderlet
	 * 	and renderingSpecification and mode.
	 * @param mode defines the mode in which the renderlet and rendering
	 *		specification has to be used.
	 *		mode may be null, that indicates that it is only used when no mode is
	 *		required.
	 * @param mediaType The media type of the rendered
	 * @param builtIn Marks a renderlet as built-in, by default non-built-in renderlets
	 *      are preferred
	 */
	public void registerRenderlet(String renderletServiceName,
			UriRef renderingSpecification,
			UriRef rdfType,
			String mode,
			MediaType mediaType, boolean builtIn);
	
}
