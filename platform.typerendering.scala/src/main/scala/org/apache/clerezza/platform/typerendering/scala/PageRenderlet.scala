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
package org.apache.clerezza.platform.typerendering.scala

import java.io.IOException
import java.io.OutputStream
import java.net.URI
import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering.Renderlet.RequestProperties
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.utils.GraphNode
import org.osgi.service.component.ComponentContext
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._

/**
 * This extends AbstractRenderlet with rdfType (abstract) and mode (defaulting to
 * "naked") and provides an activate method registering the class as Renderlet
 * with the RenderletManager.
 */
abstract class PageRenderlet extends AbstractRenderlet {

	def renderedPage(renderingArguments: RenderedPage.Arguments): RenderedPage
	def rdfType: UriRef
	def mode = "naked"

	var renderletManager: RenderletManager = null;

	def activate(context: ComponentContext) = {
		renderletManager.registerRenderlet(this.getClass.getName,
				null,
				rdfType, mode,
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}

	def bindRenderletManager(m: RenderletManager)  = {
		renderletManager = m
	}

	def unbindRenderletManager(m: RenderletManager)  = {
		renderletManager = null
	}

}

