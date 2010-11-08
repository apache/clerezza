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

package org.apache.clerezza.platform.curieprefixmanager.impl;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.curieprefixmanager.CuriePrefixRecommender;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.platform.curieprefixmanager.ontologies.CURIE;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.service.component.ComponentContext;


/**
 * Allows to mana a set of CURIE prefix bindings. Multiple URI-Prefix can point
 * to the same prefix, but per uri-prefix only one suggested prefix is supported.
 * 
 * @author reto
 */
@Component
@Services({
	@Service(Object.class), 
	@Service(CuriePrefixRecommender.class)
})
@Property(name="javax.ws.rs", boolValue=true)
@Path("/admin/curie-prefix/manager")
public class CuriePrefixManager implements CuriePrefixRecommender {
	private static final LiteralFactory literalFactory = LiteralFactory.getInstance();

	@Reference
	private RenderletManager renderletManager;

	@Reference
	private ContentGraphProvider cgProvider;

	public void activate(ComponentContext context) throws URISyntaxException {
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
				"curie-prefix-naked.ssp").toURI().toString()),
				CURIE.CuriePrefixBinding, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
				"curie-prefix-list-naked.ssp").toURI().toString()),
				CURIE.CuriePrefixBindingList, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}

	@GET
	@Path("new")
	public GraphNode emptyPrefixBinding() {
		MGraph resultMGraph = new SimpleMGraph();
		GraphNode result = new GraphNode(new BNode(), resultMGraph);
		result.addProperty(RDF.type, CURIE.CuriePrefixBinding);
		result.addProperty(CURIE.prefix,
				literalFactory.createTypedLiteral("foaf"));
		result.addProperty(CURIE.binding,
				literalFactory.createTypedLiteral("http://xmlns.com/foaf/0.1/"));
		result.addProperty(RDF.type, PLATFORM.HeadedPage);
		return result;
	}

	/**
	 * Saves a PrefixBiding, replacing an existing binding to the same value and
	 * if oldBinding is not null then it is removed
	 */
	@POST
	@Path("save")
	public Response savePrefixBinding(@Context UriInfo uriInfo,
			@FormParam("prefix") String prefix,
			@FormParam("binding") String bindingValue,
			 @FormParam("oldBinding") String oldBindingValue) {
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		Lock l = contentGraph.getLock().writeLock();
		l.lock();
		
		try {
			NonLiteral binding = getBindingWithValue(bindingValue, contentGraph);
			if (binding == null) {
				binding = new BNode();
			}
			GraphNode bindingNode;
			if (oldBindingValue != null) {
				NonLiteral oldBinding = getBindingWithValue(oldBindingValue, contentGraph);
				if (oldBinding != null) {
					GraphNode oldBindingNode = new GraphNode(oldBinding, contentGraph);
					oldBindingNode.replaceWith(binding);
				}
			}
			bindingNode = new GraphNode(binding, contentGraph);
			bindingNode.addProperty(RDF.type, CURIE.CuriePrefixBinding);
			bindingNode.deleteProperties(CURIE.prefix);
			bindingNode.addProperty(CURIE.prefix, literalFactory.createTypedLiteral(prefix));
			bindingNode.deleteProperties(CURIE.binding);
			bindingNode.addProperty(CURIE.binding, literalFactory.createTypedLiteral(bindingValue));
		} finally {
			l.unlock();
		}
		return RedirectUtil.createSeeOtherResponse("./", uriInfo);
	}

	@POST
	@Path("delete")
	public Response delete(@Context UriInfo uriInfo,
			@FormParam("binding") String bindingValue) {
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		Lock l = contentGraph.getLock().writeLock();
		l.lock();
		try {
			NonLiteral binding = getBindingWithValue(bindingValue, contentGraph);
			GraphNode bindingNode = new GraphNode(binding, contentGraph);
			bindingNode.deleteProperty(RDF.type, CURIE.CuriePrefixBinding);
			bindingNode.deleteProperties(CURIE.prefix);
			bindingNode.deleteProperties(CURIE.binding);
		} finally {
			l.unlock();
		}
		return RedirectUtil.createSeeOtherResponse("./", uriInfo);
	}

	@GET
	public GraphNode list(@Context UriInfo uriInfo) {
		TrailingSlash.enforcePresent(uriInfo);
		TripleCollection resultGraph = new SimpleMGraph();
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		GraphNode result = new GraphNode(new BNode(), new UnionMGraph(resultGraph, contentGraph));
		RdfList list = new RdfList(result);		
		Lock l = contentGraph.getLock().readLock();
		l.lock();
		try {
			Iterator<Triple> greetings = contentGraph.filter(null, RDF.type, CURIE.CuriePrefixBinding);
			while (greetings.hasNext()) {
				list.add(greetings.next().getSubject());
			}
		} finally {
			l.unlock();
		}
		result.addProperty(RDF.type, CURIE.CuriePrefixBindingList);
		result.addProperty(RDF.type, PLATFORM.HeadedPage);
		return result;
	}

	@GET
	@Path("get")
	public GraphNode getSingle(@QueryParam("binding") String bindingValue) {
		TripleCollection resultGraph = new SimpleMGraph();
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		MGraph unionMGraph = new UnionMGraph(resultGraph, contentGraph);
		Lock l = contentGraph.getLock().readLock();
		l.lock();
		try {
			GraphNode result = new GraphNode(getBindingWithValue(bindingValue, contentGraph), unionMGraph);
			result.addProperty(RDF.type, PLATFORM.HeadedPage);
			return result;
		} finally {
			l.unlock();
		}
	}

	private static NonLiteral getBindingWithValue(String bindingValue, LockableMGraph graph) {
		Iterator<Triple> triples = graph.filter(null, CURIE.binding,
				literalFactory.createTypedLiteral(bindingValue));
		if (triples.hasNext()) {
			return triples.next().getSubject();
		} else {
			return null;
		}
	}

	@Override
	public String getRecommendedPrefix(String iriPrefix) {
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		Lock l = contentGraph.getLock().readLock();
		l.lock();
		try {
			NonLiteral binding = getBindingWithValue(iriPrefix, contentGraph);
			if (binding == null) {
				return null;
			}
			GraphNode graphNode = new GraphNode(binding, contentGraph);
			return ((Literal)graphNode.getObjects(CURIE.prefix).next()).getLexicalForm();
		} finally {
			l.unlock();
		}
	}

}