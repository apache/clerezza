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
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;

/**
 * This class provides a OSGi service for managing languages in the Clerezza
 * platform.
 * 
 * @author mir
 */
@Component(immediate=true, enabled= true)
@Service(LanguageService.class)
public class LanguageService {	

	@Reference(target = SystemConfig.SYSTEM_GRAPH_FILTER)
	private MGraph systemGraph;

	@Reference
	private ContentGraphProvider cgProvider;

	/** this is linked to the system-graph, accessing requires respective
	 * permission
	 */
	private List<Resource> languageList;
	/**
	 * no permission on the system graph required to access this
	 */
	private List<Resource> languageListCache;

	private static final String PARSER_FILTER =
			"(supportedFormat=" + SupportedFormat.RDF_XML +")";

	@Reference(target=PARSER_FILTER)
	private ParsingProvider parser;
	
	private SoftReference<Graph> softLingvojGraph = new SoftReference<Graph>(null);

	/**
	 * Returns a <code>List</code> of <code>LanguageDescription</code>s which
	 * describe the languages which are supported by the platform. The first
	 * entry describes the default language af the platform.
	 * @return a list containing all language descriptions.
	 */
	public List<LanguageDescription> getLanguages() {
		List<LanguageDescription> langList = new ArrayList<LanguageDescription>();
		Iterator<Resource> languages = languageListCache.iterator();
		while (languages.hasNext()) {
			UriRef language = (UriRef) languages.next();
			langList.add(
					new LanguageDescription(new GraphNode(language, 
					cgProvider.getContentGraph())));
		}
		return langList;
	}

	/**
	 * Returns the <code>LanguageDescription</code> of the default language
	 * of the platform.
	 * @return the language description of the default language.
	 */
	public LanguageDescription getDefaultLanguage() {
		return new LanguageDescription(
				new GraphNode(languageListCache.get(0), cgProvider.getContentGraph()));
	}

	/**
	 * Returns a set containg all language uris which are in the
	 * <http://www.lingvoj.org/lingvoj> graph which is included in this bundle.
	 * @return a set containing all language uris
	 */
	public Set<UriRef> getAllLanguages() {
		Set<UriRef> result = new HashSet<UriRef>();
		Graph lingvojGraph = getLingvojGraph();
		Iterator<Triple> languages = lingvojGraph.filter(null, RDFS.isDefinedBy,
				null);
		while (languages.hasNext()) {
			UriRef languageUri = (UriRef) languages.next().getSubject();
			result.add(languageUri);
		}
		return result;
	}

	/**
	 * Adds the language specified through languageUri to the Clerezza
	 * platform. The languageUri has to be a <http://www.lingvoj.org/ontology#Lingvo>
	 * according to the graph <http://www.lingvoj.org/lingvoj> included in this
	 * bundle., e.g. "http://www.lingvoj.org/lang/de" adds German.
	 * The uri is added to the system graph and its context to the conent graph.
	 * The context added is the context provided by lingvoj.rdf.
	 * @param languageUri The language uri which specifies the language to be
	 *		added to the platform.
	 */
	public void addLanguage(UriRef languageUri) {
		if (!languageListCache.contains(languageUri)) {
			if(languageList.add(languageUri)) {
				cgProvider.getContentGraph().
						addAll(getLanguageContext(languageUri));
			}
			languageListCache.add(languageUri);
		}
	}

	private Graph getLanguageContext(UriRef langUri) {
		Graph lingvojRdf = getLingvojGraph();
		GraphNode languageNode = new GraphNode(langUri, lingvojRdf);
		return languageNode.getNodeContext();
	}
	
	private Graph getLingvojGraph() {
		Graph lingvojGraph = softLingvojGraph.get();
		if (lingvojGraph != null) {
			return lingvojGraph;
		}
		URL config = getClass().getResource("lingvoj.rdf");
		if (config == null) {
			throw new RuntimeException("no file found");
		}
		try {
			lingvojGraph = parser.parse(config.openStream(),
					SupportedFormat.RDF_XML);
			softLingvojGraph = new SoftReference<Graph>(lingvojGraph);
			return lingvojGraph;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * The activate method is called when SCR activates the component configuration.
	 * 
	 * @param componentContext
	 */
	protected void activate(ComponentContext componentContext) {
		final RdfList rdfList = new RdfList(getListNode(), systemGraph);
		languageList = Collections.synchronizedList(rdfList);
		//access to languages should not require access to system graph,
		//so copying the resources to an ArrayList
		languageListCache = Collections.synchronizedList(
				new ArrayList<Resource>(rdfList));
		if (languageListCache.size() == 0) {
			addLanguage(new UriRef("http://www.lingvoj.org/lang/en"));
		}
	}

	private NonLiteral getListNode() {
		Iterator<Triple> instances = systemGraph.filter(null, RDF.type, PLATFORM.Instance);
		if (!instances.hasNext()) {
			throw new RuntimeException("No Platform:Instance in system graph.");
		}
		NonLiteral instance = instances.next().getSubject();
		Iterator<Triple> langListIter = systemGraph.filter(instance,
				PLATFORM.languages, null);
		if (langListIter.hasNext()) {
			return (NonLiteral) langListIter.next().getObject();
		}
		BNode listNode = new BNode();
		systemGraph.add(new TripleImpl(instance, PLATFORM.languages,
				listNode));
		return listNode;
	}
}
