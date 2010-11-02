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

package org.apache.clerezza.skeleton;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.skeleton.ontologies.GREETINGS;
import org.apache.clerezza.web.fileserver.FileServer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;



@Component
@Service(value=Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Path("/greeting/list")
public class GreetingsList {

	@Reference
	private RenderletManager renderletManager;

	@Reference
	private ContentGraphProvider cgProvider;

	public void activate(ComponentContext context) throws URISyntaxException {
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
				"greeting-list-naked.ssp").toURI().toString()),
				GREETINGS.GreetingList, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}

	@GET
	public GraphNode list() {
		TripleCollection resultGraph = new SimpleMGraph();
		GraphNode result = new GraphNode(new BNode(), resultGraph);
		RdfList list = new RdfList(result);
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		Lock l = contentGraph.getLock().readLock();
		l.lock();
		try {
			Iterator<Triple> greetings = contentGraph.filter(null, RDF.type, GREETINGS.Greeting);
			while (greetings.hasNext()) {
				list.add(greetings.next().getSubject());
			}
		} finally {
			l.unlock();
		}
		result.addProperty(RDF.type, GREETINGS.GreetingList);
		return result;
	}


}