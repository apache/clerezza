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
import java.net.URISyntaxException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 *
 * @author mir
 */
class RendererImpl implements Renderer, Comparable {

	private URI renderSpecUri = null;
	private Renderlet renderlet = null;
	private MediaType mediaType = null;
	private int prio;
	private CallbackRenderer callbackRenderer;
	private boolean builtIn;
	private String mode;

	RendererImpl(UriRef renderingSpecification,
			Renderlet renderlet, String mode, MediaType mediaType, int prio,
			CallbackRenderer callbackRenderer, boolean builtIn) {
		this.renderlet = renderlet;
		this.mediaType = mediaType;
		this.mode = mode;
		this.prio = prio;
		this.callbackRenderer = callbackRenderer;
		this.builtIn = builtIn;
		if (renderingSpecification != null) {
			try {
				renderSpecUri = new URI(renderingSpecification.getUnicodeString());
			} catch (URISyntaxException ex) {
				throw new WebApplicationException(ex);
			}
		}

	}

	@Override
	public URI getRenderingSpecificationUri() {
		return renderSpecUri;
	}

	@Override
	public Renderlet getRenderlet() {
		return renderlet;
	}

	@Override
	public MediaType getMediaType() {
		return mediaType;
	}

	protected int getPrio() {
		return prio;
	}

	@Override
	public void render(GraphNode resource, GraphNode context, OutputStream os)
		throws IOException {
			renderlet.render(resource, context, callbackRenderer,
				renderSpecUri, mode, mediaType, os);
	}

	@Override
	public int compareTo(Object o) {
		RendererImpl otherConf = (RendererImpl) o;
		if (this.prio == otherConf.getPrio()) {
			if (this.builtIn == otherConf.builtIn) {
				return 0;
			}
			if (this.builtIn) {
				return 1;
			} else {
				return -1;
			}

		}
		if (this.getPrio() < otherConf.getPrio()) {
			return -1;
		} else {
			return 1;
		}
	}
}
