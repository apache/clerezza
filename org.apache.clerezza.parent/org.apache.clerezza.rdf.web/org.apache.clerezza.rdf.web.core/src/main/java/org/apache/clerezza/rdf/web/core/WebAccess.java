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
package org.apache.clerezza.rdf.web.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.serializedform.Parser;

/**
 * Provides methods to GET, PUT, and POST an SCB graph over the web.
 * To be deployed in a JAX-RS runtime.
 * 
 * @author hasan
 */
@Component
@Service(Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Path("/graph")
public class WebAccess {

	@Reference
	TcManager tcManager;

	@Reference
	private Parser parser;

	final Logger logger = LoggerFactory.getLogger(WebAccess.class);

	/**
	 * Gets the TripleCollection with specified name
	 *
	 * @param name
	 * @return
	 */
	@GET
	public TripleCollection getTriples(@QueryParam("name") UriRef name) {
		if (name == null) {
			Response r = Response.status(Response.Status.BAD_REQUEST)
					.entity("must specify a graph name")
					.type(MediaType.TEXT_PLAIN_TYPE).build();
			throw new WebApplicationException(r);
		}
		TripleCollection result =  tcManager.getTriples(name);
		logger.debug("Got graph of size {} ", result.size());
		int i = 1;
		if (logger.isDebugEnabled()) {
			for (Triple triple : result) {
				logger.debug("({}) with triples {}", i++, triple.toString());
			}
			logger.debug("returning");
		}
		return result;
	}

	/**
	 * Puts the triples replacing the triples of the existing MGraph with the
	 * specified name. If the graph doesn't exist creates a new <code>MGraph</code> 
	 * with the specified name and puts the triples
	 * 
	 *
	 * @param name
	 * @param triples
	 */
	@PUT
	public void putTriples(@QueryParam("name") UriRef name, TripleCollection triples) {
		TripleCollection tc;
		try {
			tc = tcManager.getTriples(name);
			tc.clear();
		} catch (NoSuchEntityException e) {
			tc = tcManager.createMGraph(name);
		}
		tc.addAll(triples);
	}

	/**
	 * Posts triples to be placed into an {@link MGraph} of the specified name.
	 * If an {@link MGraph} with this name doesn't already exist, a new one
	 * is created and filled with the triples posted.
	 * @param form
	 *		a multipart/form-data consisting of:
	 *		- a {@link FormFile} labeled "graph" containing the triples and
	 *			the mime-type.
	 *		- a text field labeled "name" specifying the name of the MGraph.
	 *		- an optional text field labeled "mode" specifying the mode.
	 *			If the mode is "replace", existing triples of the MGraph will be
	 *			deleted before new triples are added. If the mode is not
	 *			specified or is "append", posted triples are added to the MGraph.
	 *		- an optional text field labeled "redirection" specifying an URI
	 *			which the client should be redirected to in case of success.
	 * @return
	 *		{@link Response}. A response with status code BAD REQUEST (400) is
	 *		returned if the required data are missing. If the request can be
	 *		satisfied, one of the following responses is returned:
	 *		- SEE OTHER (303), if redirection is specified.
	 *		- CREATED (201), if redirection is not specified and a new
	 *			{@link MGraph} is created.
	 *		- NO CONTENT (204), if redirection is not specified and no new
	 *			{@link MGraph} is created.
	 */
	@POST
	@Consumes("multipart/form")
	public Response postTriples(MultiPartBody form, @Context UriInfo uriInfo) {

		FormFile[] formFiles = form.getFormFileParameterValues("graph");
		if (formFiles.length == 0) {
			responseWithBadRequest("form file parameter 'graph' is missing");
		}
		FormFile formFile = formFiles[0];
		byte[] graph = formFile.getContent();
		if (graph == null || (graph.length == 0)) {
			responseWithBadRequest("no triples uploaded");
		}
		InputStream is = new ByteArrayInputStream(graph);

		MediaType mediaType = formFile.getMediaType();
		if (mediaType == null) {
			responseWithBadRequest("mime-type not specified");
		}
		Graph parsedGraph = parser.parse(is, mediaType.toString());

		String graphName = getFirstTextParameterValue(form, "name", true);
		if (graphName == null) {
			responseWithBadRequest("graph name not specified");
		}
		UriRef graphUri = new UriRef(graphName);
		TripleCollection tc;
		boolean newGraph = false;
		try {
			tc = tcManager.getTriples(graphUri);
			String mode = getFirstTextParameterValue(form, "mode", false);
			if (mode != null) {
				if (mode.equals("replace")) {
					tc.clear();
				} else if (!mode.equals("append")) {
					responseWithBadRequest("unknown mode");
				}
			}
		} catch (NoSuchEntityException e) {
			tc = tcManager.createMGraph(graphUri);
			newGraph = true;
		}
		tc.addAll(parsedGraph);
		String redirection = getFirstTextParameterValue(form, "redirection",
				false);
		if (redirection == null) {
			if (newGraph) {
				return Response.status(Status.CREATED).build();
			} else {
				return Response.status(Status.NO_CONTENT).build();
			}
		}
		return RedirectUtil.createSeeOtherResponse(redirection, uriInfo);
	}

	@GET
	@Path("upload-form")
	@Produces("application/xhtml+xml")
	public InputStream getUploadForm() {
		return getClass().getResourceAsStream("upload-form.xhtml");
	}

	private void responseWithBadRequest(String message) {
		logger.warn(message);
		throw new WebApplicationException(Response.status(Status.BAD_REQUEST)
				.entity(message).build());
	}

	private String getFirstTextParameterValue(MultiPartBody form,
			String parameterName, boolean mandatory) {
		String[] paramValues = form.getTextParameterValues(parameterName);
		if (paramValues.length == 0) {
			if (mandatory) {
				responseWithBadRequest("text parameter '" + parameterName +
						"' is missing");
			}
			return null;
		}
		return paramValues[0];
	}
}
