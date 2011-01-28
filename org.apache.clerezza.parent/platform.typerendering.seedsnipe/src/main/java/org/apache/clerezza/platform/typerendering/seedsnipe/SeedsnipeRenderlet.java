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
package org.apache.clerezza.platform.typerendering.seedsnipe;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.platform.typerendering.CallbackRenderer;
import org.apache.clerezza.platform.typerendering.Renderlet;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.templating.RenderingFunctions;
import org.apache.clerezza.templating.seedsnipe.simpleparser.SeedsnipeTemplatingEngine;

/**
 * Renderlet for Seedsnipe
 *
 * @author mir
 *
 * @scr.component
 * @scr.service interface="org.apache.clerezza.platform.typerendering.Renderlet"
 */
public class SeedsnipeRenderlet implements Renderlet{

	private SeedsnipeTemplatingEngine seedsnipeEngine;

	public SeedsnipeRenderlet() {
		seedsnipeEngine = new SeedsnipeTemplatingEngine();
	}

	@Override
	public void render(GraphNode res, GraphNode context, Map<String, Object> sharedRenderingValues,
			CallbackRenderer callbackRenderer,
			URI renderingSpecification,
			String mode,
			MediaType mediaType, RequestProperties requestProperties,
			OutputStream os) throws IOException {
		try {
			RenderingFunctions renderingFunctions = new WebRenderingFunctions(
					res.getGraph(), context, callbackRenderer, mode);
			seedsnipeEngine.process(res, context, renderingFunctions, renderingSpecification.toURL(), os);
		} catch (MalformedURLException ex) {
			throw new WebApplicationException(ex);
		}
	}
}
