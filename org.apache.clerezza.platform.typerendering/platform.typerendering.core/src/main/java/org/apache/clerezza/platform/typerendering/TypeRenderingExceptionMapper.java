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

import java.net.URL;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.platform.typerendering.ontologies.TYPERENDERING;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An <code>ExceptionMapper</code> that maps
 * <code>RenderingspecificationException</code> to an <code>GraphNode</code>
 * that will be renderer to a page containing the information provided by the
 * exception.
 *
 * @author mir
 */
@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Provider
public class TypeRenderingExceptionMapper implements ExceptionMapper<TypeRenderingException> {


	private Logger logger = LoggerFactory.getLogger(
			TypeRenderingExceptionMapper.class);

	@Reference
	RenderletManager renderletManager;

	@Override
	public Response toResponse(TypeRenderingException exception) {
		ResponseBuilder rb = Response.serverError();
		logger.info(exception.getMessage());
		if (exception.getRenderNode().hasProperty(RDF.type, TYPERENDERING.Exception)) {
			logger.error("Exception in template used for rendering exceptions ", exception);
			rb.entity("There is an error in the template used for rendering" +
					" exceptions. Please check the console output for further" +
					" information. Thanks!").type(MediaType.TEXT_PLAIN_TYPE);
		} else {
			rb.entity(exception.getExceptionGraphNode());
		}
		return rb.build();
	}

	/**
	 * The activate method is called when SCR activates the component
	 * configuration
	 *
	 * @param componentContext
	 */
	protected void activate(ComponentContext componentContext) throws Exception {
		URL template = getClass().getResource("exception-template.ssp");
		renderletManager.registerRenderlet(
				"org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet",
				new UriRef(template.toURI().toString()),
				TYPERENDERING.Exception, ".*",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}
}
