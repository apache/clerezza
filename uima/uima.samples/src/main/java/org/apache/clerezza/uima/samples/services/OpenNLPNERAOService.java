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
package org.apache.clerezza.uima.samples.services;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.uima.utils.InMemoryUIMAExecutor;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.uima.util.XMLInputSource;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample REST service which uses {@link opennlp.uima.namefind.NameFinder} to extract named entities
 * from the text of a given URI
 */
@Component
@Service(OpenNLPService.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/uima")
public class OpenNLPNERAOService implements OpenNLPService {

  private static final String PATH = "/META-INF/OpenNLPPersonAOAE.xml";
  private static final String OUTPUTGRAPH = "outputgraph";

  @Reference
  private InMemoryUIMAExecutor executor;

  @POST
  @Path("opennlp/person")
  @Produces("application/rdf+xml")
  public Graph extractPersons(@FormParam("uri") String uriString) {
    if (uriString == null || uriString.length() == 0)
      throw new WebApplicationException(Response.status(
              Response.Status.BAD_REQUEST).entity(new StringBuilder("No URI specified").toString()).build());

    Map<String, Object> parameters = new HashMap<String, Object>(1);
    parameters.put(OUTPUTGRAPH, uriString);
    try {
      URL url = URI.create(uriString).toURL();
      String text = IOUtils.toString(url.openConnection().getInputStream());
      executor.analyzeDocument(text, new XMLInputSource(getClass().getResource(PATH)), parameters);
    } catch (Exception e) {
      throw new WebApplicationException(Response.status(
              Response.Status.INTERNAL_SERVER_ERROR).entity(new StringBuilder("Failed UIMA execution on URI ").
              append(uriString).append(" due to \n").append(e.getLocalizedMessage()).toString()).build());
    }
    return TcManager.getInstance().getMGraph(new UriRef(uriString)).getGraph();
  }


}

