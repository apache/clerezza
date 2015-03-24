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

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.commons.rdf.Graph;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.Smusher;
import org.osgi.service.component.ComponentContext;

/**
 * Provides a method to remove duplicate noded from (aka smush) a ImmutableGraph
 * 
 * @author reto
 */
@Component
@Service(Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Path("/admin/graphs/smush")
public class Smush {
    private final Iri tBoxName = new Iri("urn:x-localinstance:/tbox.graph");

    @Reference
    private TcManager tcManager;

    @Reference
    private RenderletManager renderletManager;

    private Graph tBox;

    protected void activate(ComponentContext componentContext) {
        try {
            //getting the tBox here means no read right on Tbox is necessary
            //when smushing
            tBox = tcManager.getGraph(tBoxName);
        } catch (NoSuchEntityException e) {
            tBox = new SimpleGraph();
            tBox.add(new TripleImpl(FOAF.mbox, RDF.type, OWL.InverseFunctionalProperty));
            tBox.add(new TripleImpl(FOAF.mbox_sha1sum, RDF.type, OWL.InverseFunctionalProperty));
            tBox.add(new TripleImpl(PLATFORM.userName, RDF.type, OWL.InverseFunctionalProperty));
            tBox = tcManager.createImmutableGraph(tBoxName, tBox);
        }
    }


    
    @POST
    public Response smush(@Context UriInfo uriInfo, @FormParam("graphName") Iri graphName) {
        Graph mGraph = tcManager.getGraph(graphName);
        Smusher.smush(mGraph, tBox);
        return RedirectUtil.createSeeOtherResponse("./", uriInfo);
    }
}
