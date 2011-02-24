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
package org.apache.clerezza.platform.language;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.platform.config.PlatformConfig;

import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.platform.language.ontologies.LANGUAGE;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.UserContextProvider;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.apache.felix.scr.annotations.Services;
import org.osgi.framework.Bundle;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * Use the LanguageWidget service to store an RDF list of
 * languages in a triple collection and retrieve the root node of the list. The widget
 * can be rendered by using the render method. Via javascript function
 * LanguageList.getLanguage() the currently selected language can be retrieved.
 *
 * @author tio
 */
@Component(enabled=true, immediate=true)
@Services({
	@Service(Object.class),
	@Service(UserContextProvider.class)
})
@Property(name = "javax.ws.rs", boolValue = true)

@Path("/language-widget")
public class LanguageWidget implements UserContextProvider {

	private FileServer fileServer;

	@Reference
	private TcManager tcManager;

	@Reference
	private PlatformConfig platformConfig;

	@Reference
	private RenderletManager renderletManager;

	@Reference
	private LanguageService languageService;
	
	protected void activate(ComponentContext context) throws IOException, URISyntaxException {
		Bundle bundle = context.getBundleContext().getBundle();
		URL resourceDir = getClass().getResource("staticweb");
		PathNode pathNode = new BundlePathNode(bundle, resourceDir.getPath());

		fileServer = new FileServer(pathNode);

		URL template = getClass().getResource("language-list.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(template.toURI().toString()),
				LANGUAGE.LanguageList, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);

	}

	private LockableMGraph getConfigGraph() {
		return tcManager.getMGraph(Constants.CONFIG_GRAPH_URI);
	}

	/**
	 *
	 * @param node  The graph of the specified GraphNode will not be locked, neither for reading nor writing.
	 *		It is the responsibility of the calling function to set the write lock, if necessary.
	 * @return
	 */
	@Override
	public GraphNode addUserContext(final GraphNode node) {	
		final NonLiteral platformInstance = AccessController.doPrivileged(
			new PrivilegedAction<NonLiteral>() {
				@Override
				public NonLiteral run() {
					return (NonLiteral) platformConfig.getPlatformInstance().getNode();
				}
			});
		try {
			return addLanguages(node, platformInstance, languageService.getLanguages(), false);
			 
		} catch (AccessControlException ex) {
			return AccessController.doPrivileged(
				new PrivilegedAction<GraphNode>() {
					@Override
					public GraphNode run() {
						return addLanguages(node, platformInstance, languageService.getLanguages(), true);
					}
				});
		}		
	}

	private GraphNode addLanguages(GraphNode node, NonLiteral platformInstance, List<LanguageDescription> languages,
			boolean copyToNode) {
		TripleCollection graph = node.getGraph();
		BNode listNode = new BNode();		
		RdfList list = new RdfList(listNode, graph);
		LockableMGraph configGraph = getConfigGraph();
		Lock readLock = configGraph.getLock().readLock();
		for (LanguageDescription languageDescription : languages) {
			NonLiteral languageUri = (NonLiteral) languageDescription.getResource().getNode();
			list.add(languageUri);
			if (copyToNode) {
				readLock.lock();
				try {
					graph.addAll(new GraphNode(languageUri, configGraph).getNodeContext());
				} finally {
					readLock.unlock();
				}
			}
		}
		node.addProperty(PLATFORM.instance, platformInstance);
		graph.add(new TripleImpl(platformInstance, RDF.type, PLATFORM.Instance));
		graph.add(new TripleImpl(platformInstance, PLATFORM.languages, listNode));
		graph.add(new TripleImpl(listNode, RDF.type, LANGUAGE.LanguageList));
		if (!copyToNode) {
			node = new GraphNode(node.getNode(), new UnionMGraph(graph, configGraph));
		}
		return node;
	}

	/**
	 * Returns a PathNode of a static file from the staticweb folder.
	 *
	 * @param path specifies the path param of a URI
	 *
	 * @return {@link PathNode}
	 */
	@GET
	@Path("{path:.+}")
	public PathNode getStaticFile(@PathParam("path") String path) {
		return fileServer.getNode(path);
	}
}
