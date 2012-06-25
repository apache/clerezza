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
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.MGraph;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.SecuredMGraph;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.ontologies.LINGVOJ;
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

	@Reference
	private TcManager tcManager;

	/**
	 * this is linked to the system-graph, accessing requires respective
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

	@Reference(target = SystemConfig.SYSTEM_GRAPH_FILTER)
	private MGraph securedSystemGraph;

	@Reference(target = PlatformConfig.CONFIG_GRAPH_FILTER)
	private MGraph securedConfigGraph;
	
	private SoftReference<Graph> softLingvojGraph = new SoftReference<Graph>(null);


	private LockableMGraph getSystemGraph() {
		return ((SecuredMGraph) securedSystemGraph).getUnsecuredMGraph();
	}

	private LockableMGraph getConfigGraph() {
		return ((SecuredMGraph) securedConfigGraph).getUnsecuredMGraph();
	}

	/**
	 * Returns a <code>List</code> of <code>LanguageDescription</code>s which
	 * describe the languages which are supported by the platform. The first
	 * entry describes the default language of the platform.
	 * @return a list containing all language descriptions.
	 */
	public List<LanguageDescription> getLanguages() {
		List<LanguageDescription> langList = new ArrayList<LanguageDescription>();
		Iterator<Resource> languages = languageListCache.iterator();
		while (languages.hasNext()) {
			UriRef language = (UriRef) languages.next();
			langList.add(new LanguageDescription(new GraphNode(language, getConfigGraph())));
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
				new GraphNode(languageListCache.get(0), getConfigGraph()));
	}

	/**
	 * Returns a set containg all language uris which are in the
	 * <http://www.lingvoj.org/lingvoj> graph which is included in this bundle.
	 * @return a set containing all language uris. This uris can be used to 
	 * add the language to Clerezza over the addLanguage()-method in this class.
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
	 * Returns a language uri of a language which has a label containing the
	 * specified languageName. The label itself is in the language specified through
	 * inLanguage. If inLanguage is null, then all labels of a language of searched.
	 * If no language was found in the <http://www.lingvoj.org/lingvoj>
	 * graph, then null is returned. The returned uri can be used to
	 * add the language to Clerezza over the addLanguage()-method in this class.
	 * @return a language uris
	 */
	public UriRef getLanguage(String languageName, Language inLanguage) {
		Graph lingvojGraph = getLingvojGraph();
		Iterator<Triple> languages = lingvojGraph.filter(null, RDFS.isDefinedBy, null);
		while (languages.hasNext()) {
			GraphNode languageNode = new GraphNode((UriRef) languages.next().getSubject(), lingvojGraph);
			Iterator<Resource> labels = languageNode.getObjects(RDFS.label);
			while (labels.hasNext()) {
				PlainLiteral label = (PlainLiteral) labels.next();
				if (label.getLanguage().equals(inLanguage) || inLanguage == null) {
					if (label.getLexicalForm().contains(languageName)) {
						return (UriRef) languageNode.getNode();
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Get the language as <code>GraphNode</code> specified through languageUri. 
	 * The languageUri has to be a <http://www.lingvoj.org/ontology#Lingvo>
	 * according to the graph <http://www.lingvoj.org/lingvoj> included in this
	 * bundle., e.g. "http://www.lingvoj.org/lang/de".
	 *
	 * @param languageUri The language uri which specifies the language 
	 */
	public GraphNode getLanguageNode(NonLiteral langUri) {
		Graph lingvojRdf = getLingvojGraph();
		return new GraphNode(langUri, lingvojRdf);
	}

	/**
	 * Adds the language specified through languageUri to the Clerezza
	 * platform. The languageUri has to be a <http://www.lingvoj.org/ontology#Lingvo>
	 * according to the graph <http://www.lingvoj.org/lingvoj> included in this
	 * bundle., e.g. "http://www.lingvoj.org/lang/de" adds German.
	 * The uri is added to the system graph and its context to the config graph.
	 * The context added is the context provided by lingvoj.rdf.
	 * @param languageUri The language uri which specifies the language to be
	 *		added to the platform.
	 */
	public void addLanguage(UriRef languageUri) {
		if (!languageListCache.contains(languageUri)) {
			LockableMGraph systemGraph = getSystemGraph();
			Lock writeLock = systemGraph.getLock().writeLock();
			writeLock.lock();
			try {
				if (languageList.add(languageUri)) {
					addToLanguageConfigGraph(languageUri);
				}
			} finally {
				writeLock.unlock();
			}
			languageListCache.add(languageUri);
		}
	}

	private void synchronizeContentGraph() {
		for (Resource resource : languageListCache) {
			addToLanguageConfigGraph((UriRef)resource);
		}
	}
	/**
	 * Adds the language information of the language specified through
	 * languageUri to the config graph. The languageUri has to be of type 
	 * <http://www.lingvoj.org/ontology#Lingvo> according to the graph 
	 * <http://www.lingvoj.org/lingvoj> included in this
	 * bundle., e.g. "http://www.lingvoj.org/lang/de" adds German.
	 * 
	 * The added language will not be a platform language.
	 *
	 * @param languageUri
	 */
	public void addToLanguageConfigGraph(NonLiteral languageUri) {
		LockableMGraph configGraph = getConfigGraph();
		Lock writeLock = configGraph.getLock().writeLock();
		writeLock.lock();
		try {
			if (!configGraph.filter(languageUri, LINGVOJ.iso1, null).hasNext()) {
				configGraph.addAll(getLanguageContext(languageUri));
			}
		} finally {
			writeLock.unlock();
		}
	}

	private Graph getLanguageContext(NonLiteral langUri) {
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
			MGraph lingvojMGraph = new SimpleMGraph();
			parser.parse(lingvojMGraph, config.openStream(), SupportedFormat.RDF_XML, null);
			lingvojGraph = lingvojMGraph.getGraph();
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
		LockableMGraph systemGraph = getSystemGraph();
		NonLiteral listNode = getListNode(systemGraph);

		Lock writeLock = systemGraph.getLock().writeLock();
		writeLock.lock();
		try {
			// the constructor of RdfList might write to the graph! => requires a write lock
			final RdfList rdfList = new RdfList(listNode, systemGraph);
			languageList = Collections.synchronizedList(rdfList);
			//access to languages should not require access to system graph,
			//so copying the resources to an ArrayList
			languageListCache = Collections.synchronizedList(
					new ArrayList<Resource>(rdfList));
			if (languageListCache.size() == 0) {
				addLanguage(new UriRef("http://www.lingvoj.org/lang/en"));
			}
			//this is to make sure the content graph contains the relevant data
			synchronizeContentGraph();
		} finally {
			writeLock.unlock();
		}
	}

	private NonLiteral getListNode(LockableMGraph systemGraph) {
		NonLiteral instance = null;
		Lock readLock = systemGraph.getLock().readLock();
		readLock.lock();
		try {
			Iterator<Triple> instances = systemGraph.filter(null, RDF.type, PLATFORM.Instance);
			if (!instances.hasNext()) {
				throw new RuntimeException("No Platform:Instance in system graph.");
			}
			instance = instances.next().getSubject();
			Iterator<Triple> langListIter = systemGraph.filter(instance,
					PLATFORM.languages, null);
			if (langListIter.hasNext()) {
				return (NonLiteral) langListIter.next().getObject();
			}
		} finally {
			readLock.unlock();
		}
		BNode listNode = new BNode();
		Lock writeLock = systemGraph.getLock().writeLock();
		writeLock.lock();
		try {
			systemGraph.add(new TripleImpl(instance, PLATFORM.languages, listNode));
		} finally {
			writeLock.unlock();
		}
		return listNode;
	}
}
