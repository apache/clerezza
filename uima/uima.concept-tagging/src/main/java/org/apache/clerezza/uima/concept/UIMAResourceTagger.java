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

import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.platform.concepts.core.ResourceTagger;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.utils.ExternalServicesFacade;
import org.apache.clerezza.uima.utils.UIMAServicesFacade;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.osgi.service.component.ComponentContext;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.List;


/**
 * UIMA enabled version of {@link ResourceTagger} providing automatic concepts tagging
 *
 */
@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/concepts/tagger/")
public class UIMAResourceTagger extends ResourceTagger {

  private UIMAServicesFacade uimaServicesFacade;

  @Override
  protected void activate(ComponentContext context) throws URISyntaxException {
    uimaServicesFacade = new ExternalServicesFacade();
  }

  /**
   * Update Resource with automatic concept recognition
   *
   * @param uri     Resource to enrich
   * @param uriInfo Context for the Resource
   * @return Resource page eventually containing extracted concepts
   */
  @POST
  @Path("set")
  public Response updateConcepts(@FormParam("uri") UriRef uri, @Context UriInfo uriInfo, @FormParam("override") boolean override) {
    if (uri != null) {
      GraphNode node = new GraphNode(uri, cgProvider.getContentGraph());
      List<FeatureStructure> conceptsFS = null;
      try {
        /* extract concepts */
        conceptsFS = uimaServicesFacade.getConcepts(node.toString());

        if (override)
          node.deleteProperties(DCTERMS.subject);

        /* if concepts have a good relevance, then add them as concept tags */
        if (conceptsFS != null && !conceptsFS.isEmpty()) {
          for (FeatureStructure conceptFS : conceptsFS) {
            Type conceptType = conceptFS.getType();
            Double relevance = Double.valueOf(conceptFS.getStringValue(conceptType.getFeatureByBaseName("relevance")));
            if (relevance > 0.6) {
              node.addProperty(DCTERMS.subject, new UriRef(conceptFS.getStringValue(conceptType.getFeatureByBaseName("text"))));
            }
          }
        }
      } catch (UIMAException e) {
        throw new WebApplicationException(Response.status(
                Response.Status.NOT_MODIFIED).entity("Could not automatically recognize concepts for the specified URI.").build());
      }
      catch (Exception e) {
        throw new WebApplicationException(Response.status(
                Response.Status.INTERNAL_SERVER_ERROR).entity(
                "Some error occurred while trying to add automatically extracted concept tags.").build());
      }
    } else {
      throw new WebApplicationException(Response.status(
              Response.Status.BAD_REQUEST).entity("No resource uri defined.").build());
    }
    return RedirectUtil.createSeeOtherResponse("/concepts/generic-resource?uri=" +
            uri.getUnicodeString(), uriInfo);
  }
}
