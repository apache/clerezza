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
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.language.ontologies.LANGUAGE;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.UserContextProvider;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
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
	protected ContentGraphProvider cgProvider;
	@Reference
	private RenderletManager renderletManager;
	@Reference
	private LanguageService languageService;
	
	protected void activate(ComponentContext context)
			throws IOException,
			URISyntaxException {

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

	@Override
	public GraphNode addUserContext(GraphNode node) {
		BNode instance = new BNode();
		node.addProperty(PLATFORM.instance, instance);
		node.getGraph().add(new TripleImpl(instance, RDF.type, PLATFORM.Instance));
		BNode lang = getLanguagesList(node.getGraph());
		node.getGraph().add(new TripleImpl(instance, PLATFORM.languages, lang));
		node = new GraphNode(node.getNode(), new UnionMGraph(node.getGraph(),
				cgProvider.getContentGraph()));
		return node;
	}

	private BNode getLanguagesList(TripleCollection graph) {
		BNode listNode = new BNode();
		RdfList list = new RdfList(listNode, graph);
		List<LanguageDescription> languages = languageService.getLanguages();
		for (LanguageDescription languageDescription : languages) {
			list.add(languageDescription.getResource().getNode());
		}
		graph.add(new TripleImpl(listNode, RDF.type, LANGUAGE.LanguageList));
		return listNode;
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
