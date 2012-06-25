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
package org.apache.clerezza.platform.content;


import java.net.URI;
import java.util.concurrent.locks.Lock;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * This Jax-rs root resource provides a method to post content to the content
 * graph
 *
 * @author mir
 */
@Component
@Service(Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Path("content")
public class ContentPostSupport {
	
	@Reference
	private DiscobitsHandler handler;

	@Reference
	private ContentGraphProvider cgProvider;

	/**
	 * Creates an InfoDiscoBt (aka Binary Content) in the content graph.<br/>
	 * This JAX-RS method is available under the path "content". It requires
	 * a multipart/form with two fields: "content", which is the content of the
	 * InfoDiscobit to be created and "uri" which is the uri of the new
	 * InfoDiscoBit.
	 *
	 * @param form
	 * @return Returns a Created (201) response, if the info bit was successfully
	 * uploaded. Returns Bad Request (400) response, if required form fields are
	 * missing. Returns a Conflict (409) response, if at the specified URI a
	 * resource already exists.
	 */
	@POST
	@Consumes("multipart/form")
	public Response postContent(MultiPartBody form) {
		FormFile formFile = form.getFormFileParameterValues("content")[0];
		String uri = form.getTextParameterValues("uri")[0];
		byte[] content = formFile.getContent();
		if (content == null || uri == null) {
			return Response.status(400).entity("Required form field is missing").
					type(MediaType.TEXT_PLAIN_TYPE).build();
		}
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		Lock readLock = contentGraph.getLock().readLock();
		readLock.lock();
		try {
			if (contentGraph.filter(new UriRef(uri), RDF.type, null).hasNext()) {
				return Response.status(Response.Status.CONFLICT).
						entity("A resource with the specified URI already exists").
						type(MediaType.TEXT_PLAIN_TYPE).build();
			}
		} finally {
			readLock.unlock();
		}
		handler.put(new UriRef(uri), formFile.getMediaType(), content);
		return Response.created(URI.create(uri)).build();
	}	
	
}
