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
package org.apache.clerezza.uima.concept;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.uima.utils.InMemoryUIMAExecutor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.uima.util.XMLInputSource;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * REST service for concept tagging which accepts 'uri' parameter to tag concepts and create the graph for that resource
 */

@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/resourcetagger")
public class UIMARemoteResourceTaggerService {

  private static final String PATH = "/META-INF/AggregateResourceTaggerAE.xml";
  private static final String OUTPUTGRAPH = "outputgraph";
  private static final String ALCHEMYKEY = "alchemykey";

  @Reference
  private InMemoryUIMAExecutor executor;

  @GET
  @Path("tag")
  @Produces("application/rdf+xml")
  public Graph tagUri(@QueryParam("uri") String uri, @QueryParam("key") String key) {
    if (uri == null || uri.length() == 0)
      throw new WebApplicationException(Response.status(
              Response.Status.BAD_REQUEST).entity(new StringBuilder("No URI specified").toString()).build());

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(OUTPUTGRAPH, uri);
    parameters.put(ALCHEMYKEY, key);
    try {
      executor.analyzeDocument(uri, new XMLInputSource(getClass().getResource(PATH)), parameters);
    } catch (Exception e) {
      throw new WebApplicationException(Response.status(
              Response.Status.INTERNAL_SERVER_ERROR).entity(new StringBuilder("Failed UIMA execution on URI ").
              append(uri).append(" due to \n").append(e.getLocalizedMessage()).toString()).build());
    }
    return TcManager.getInstance().getMGraph(new UriRef(uri)).getGraph();
  }

}
