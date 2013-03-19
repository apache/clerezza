package org.example.clerezza.combined.tutorial;
/*
 *
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
 *
*/


import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.ComponentContext;

import org.apache.clerezza.platform.typerendering.seedsnipe.SeedsnipeRenderlet;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.serializedform.Parser;

/**
 * Get Persons by their email
 *
 * @scr.component
 * @scr.service interface="java.lang.Object"
 * @scr.property name="javax.ws.rs" type="Boolean" value="true"
 */

@Path("/foaf")
public class TutorialApp {
    
    /**
     * @scr.reference
     */
    TcManager tcManager;
    
    /**
     * @scr.reference
     */
    private RenderletManager renderletManager;
    
    private UriRef graphName = new UriRef("http://localhost.mygraph");

    @GET
    @Path("find")
    @Produces("application/rdf+xml")
    public Graph getPersonRdf(@QueryParam("mbox") String mboxString) {
        MGraph mGraph = tcManager.getMGraph(graphName);
        NonLiteral person = getPersonByMbox(mboxString, mGraph);
        return new GraphNode(person, mGraph).getNodeContext();
    }

    @GET
    @Path("find")
    @Produces("application/xhtml+xml")
    public GraphNode getPersonHtml(@QueryParam("mbox") String mboxString) {
        MGraph mGraph = tcManager.getMGraph(graphName);
        NonLiteral person = getPersonByMbox(mboxString, mGraph);
        return new GraphNode(person, mGraph);
    }

    private NonLiteral getPersonByMbox(String mboxString, MGraph mGraph) {
        Iterator<Triple> iter = mGraph.filter(null, FOAF.mbox, new UriRef(mboxString));
        NonLiteral person = null;
        while(iter.hasNext()) {
            person = iter.next().getSubject();
        }
        return person;
    }
    
    /**
     * The activate method is called when SCR activates the component configuration.
     * This method gets the system graph or create a new one if it doesn't exist.
     * 
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {
        
        URL templateURL = getClass().getResource("tutorial.xhtml");    
        try {
            renderletManager.registerRenderlet(SeedsnipeRenderlet.class
                    .getName(), new UriRef(templateURL.toURI().toString()),
                    FOAF.Person, null, MediaType.APPLICATION_XHTML_XML_TYPE,
                    true);
        } catch (URISyntaxException ex) {
            throw new WebApplicationException(ex);
        }
        TripleCollection tc;
        try {
            tcManager.getMGraph(graphName);
        } catch (NoSuchEntityException nsee) {
            tc = tcManager.createMGraph(graphName);
            InputStream fin = null;
            fin = getClass().getResourceAsStream("data.turtle");
            Parser parser = Parser.getInstance();
            tc.addAll(parser.parse(fin, "text/turtle"));
        }
    }
}

