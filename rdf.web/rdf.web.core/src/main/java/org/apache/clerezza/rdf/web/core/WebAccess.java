/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.apache.clerezza.rdf.web.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.util.concurrent.locks.Lock;
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
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.web.fileserver.util.MediaTypeGuesser;

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
        AccessController.checkPermission(new WebAccessPermission());
        if (name == null) {
            Response r = Response.status(Response.Status.BAD_REQUEST)
                    .entity("must specify a graph name")
                    .type(MediaType.TEXT_PLAIN_TYPE).build();
            throw new WebApplicationException(r);
        }
        TripleCollection result = tcManager.getTriples(name);
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
        AccessController.checkPermission(new WebAccessPermission());
        LockableMGraph mGraph;
        try {
            mGraph = tcManager.getMGraph(name);
        } catch (NoSuchEntityException e) {
            mGraph = tcManager.createMGraph(name);
        }
        Lock writeLock = mGraph.getLock().writeLock();
        writeLock.lock();
        try {
            mGraph.clear();
            mGraph.addAll(triples);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Posts triples to be placed into an {@link MGraph} of the specified name.
     * If an {@link MGraph} with this name doesn't already exist, a new one
     * is created and filled with the triples posted.
     * @param form
     *        a multipart/form-data consisting of:
     *        - a {@link FormFile} labeled "graph" containing the triples and
     *            the mime-type.
     *        - a text field labeled "name" specifying the name of the MGraph.
     *        - an optional text field labeled "mode" specifying the mode.
     *            If the mode is "replace", existing triples of the MGraph will be
     *            deleted before new triples are added. If the mode is not
     *            specified or is "append", posted triples are added to the MGraph.
     *        - an optional text field labeled "redirection" specifying an URI
     *            which the client should be redirected to in case of success.
     * @return
     *        {@link Response}. A response with status code BAD REQUEST (400) is
     *        returned if the required data are missing. If the request can be
     *        satisfied, one of the following responses is returned:
     *        - SEE OTHER (303), if redirection is specified.
     *        - CREATED (201), if redirection is not specified and a new
     *            {@link MGraph} is created.
     *        - NO CONTENT (204), if redirection is not specified and no new
     *            {@link MGraph} is created.
     */
    @POST
    @Consumes("multipart/form-data")
    public Response postTriples(MultiPartBody form, @Context UriInfo uriInfo) {

        AccessController.checkPermission(new WebAccessPermission());
        FormFile[] formFiles = form.getFormFileParameterValues("graph");
        if (formFiles.length == 0) {
            responseWithBadRequest("form file parameter 'graph' is missing");
        }
        FormFile formFile = formFiles[0];
        byte[] graph = formFile.getContent();
        if (graph == null || (graph.length == 0)) {
            responseWithBadRequest("no triples uploaded");
        }
        MediaType mediaType = formFile.getMediaType();
        if (mediaType == null) {
            responseWithBadRequest("mime-type not specified");
        }
        if (mediaType.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
            MediaType guessedType = MediaTypeGuesser.getInstance().guessTypeForName(formFile.getFileName());
            if (guessedType != null) {
                mediaType = guessedType;
            }
        }
        String graphName = getFirstTextParameterValue(form, "name", true);
        if (graphName == null) {
            responseWithBadRequest("graph name not specified");
        }
        String mode = getFirstTextParameterValue(form, "mode", false);
        if (mode != null) {
            if (!(mode.equals("replace") || mode.equals("append"))) {
                responseWithBadRequest("unknown mode");
            }
        } else {
            mode = "append";
        }
        InputStream is = new ByteArrayInputStream(graph);
        Graph parsedGraph = parser.parse(is, mediaType.toString());
        UriRef graphUri = new UriRef(graphName);
        LockableMGraph mGraph;
        boolean newGraph = false;
        try {
            mGraph = tcManager.getMGraph(graphUri);
        } catch (NoSuchEntityException e) {
            mGraph = tcManager.createMGraph(graphUri);
            newGraph = true;
        }
        Lock writeLock = mGraph.getLock().writeLock();
        writeLock.lock();
        try {
            if (!newGraph && mode.equals("replace")) {
                mGraph.clear();
            }
            mGraph.addAll(parsedGraph);
        } finally {
            writeLock.unlock();
        }
        String redirection = getFirstTextParameterValue(form, "redirection", false);
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
        AccessController.checkPermission(new WebAccessPermission());
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
