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
package org.apache.clerezza.platform.config;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.graphprovider.content.GraphNameTransitioner;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * This class provides a OSGi service for getting system properties from
 * the sytem graph.
 * 
 * @author mir
 */
@Component
@Service(PlatformConfig.class)
public class PlatformConfig {

	@Reference(target = SystemConfig.SYSTEM_GRAPH_FILTER)
	private LockableMGraph systemGraph;
	private BundleContext context;
	private static String DEFAULT_PORT = "8080";

	/**
	 * @deprecated use org.apache.clerezza.platform.Contants instead
	 */
	@Deprecated
	public static final UriRef CONFIG_GRAPH_URI = Constants.CONFIG_GRAPH_URI;

	/**
	 * A filter that can be used to get the config graph as OSGi service,
	 * that is provided by <code>org.apache.clerezza.rdf.core.access.TcManager</code>.
	 */
	public static final String CONFIG_GRAPH_FILTER =
			"(name="+ Constants.CONFIG_GRAPH_URI_STRING +")";

	@Reference
	private TcManager tcManager;

	@Reference
	private ContentGraphProvider cgProvider;


	/**
	 * Returns the default base URI of the Clerezza platform instance.
	 * @return the base URI of the Clerezza platform
	 */
	public UriRef getDefaultBaseUri() {
		return AccessController.doPrivileged(new PrivilegedAction<UriRef>() {

			@Override
			public UriRef run() {
				GraphNode platformInstance = getPlatformInstance();
				Lock l = platformInstance.readLock();
				l.lock();
				try {
					Iterator<Resource> triples = platformInstance.getObjects(PLATFORM.defaultBaseUri);
					if (triples.hasNext()) {
						return (UriRef) triples.next();
					} else {
						String port = context.getProperty("org.osgi.service.http.port");
						if (port == null) {
							port = DEFAULT_PORT;
						}
						if (port.equals("80")) {
							return new UriRef("http://localhost/");
						}
						return new UriRef("http://localhost:" + port + "/");
					}
				} finally {
					l.unlock();
				}
			}
		});
	}

	/**
	 * Returns the platforminstance as <code>GraphNode</code> of the system
	 * graph (a LockableMGraph). Access controls applies to the system graph
	 * instance underlying the <code>GraphNode</code>.
	 *
	 * @return
	 */
	public GraphNode getPlatformInstance() {
		return new GraphNode(getPlatformInstanceResource(), systemGraph);
	}

	private NonLiteral getPlatformInstanceResource() {
		Lock l = systemGraph.getLock().readLock();
		l.lock();
		try {
			Iterator<Triple> instances = systemGraph.filter(null, RDF.type, PLATFORM.Instance);
			if (!instances.hasNext()) {
				throw new RuntimeException("No Platform:Instance in system graph.");
			}
			return instances.next().getSubject();
		} finally {
			l.unlock();
		}
	}

	/**
	 * Returns the base URIs of the Clerezza platform instance.
	 * A base Uri is the shortest URI of a URI-Hierarhy the platform handles.
	 * @return the base URI of the Clerezza platform
	 */
	//todo: if this is the only class that sets and reads base uris then getBaseURIs should keep a cache
	public Set<UriRef> getBaseUris() {

		return AccessController.doPrivileged(new PrivilegedAction<Set<UriRef>>() {

			@Override
			public Set<UriRef> run() {
				Iterator<Resource> baseUrisIter = getPlatformInstance().
						getObjects(PLATFORM.baseUri);
				Set<UriRef> baseUris = new HashSet<UriRef>();
				while (baseUrisIter.hasNext()) {
					UriRef baseUri = (UriRef) baseUrisIter.next();
					baseUris.add(baseUri);
				}
				baseUris.add(getDefaultBaseUri());
				return baseUris;
			}
		});

	}

	/**
	 * Adds a base URI to the Clerezza platform instance.
	 *
	 * @param baseUri The base URI which will be added to the platform instance
	 */
	public void addBaseUri(UriRef baseUri) {
		systemGraph.add(new TripleImpl(getPlatformInstanceResource(), PLATFORM.baseUri, baseUri));
	}

	/**
	 * Removes a base URI from the Clerezza platform instance.
	 *
	 * @param baseUri The base URI which will be removed from the platform instance
	 */
	public void removeBaseUri(UriRef baseUri) {
		systemGraph.remove(new TripleImpl(getPlatformInstanceResource(), PLATFORM.baseUri, baseUri));
	}

	protected void activate(ComponentContext componentContext) {
		GraphNameTransitioner.renameGraphsWithOldNames(tcManager);
		this.context = componentContext.getBundleContext();
		try {
			tcManager.getMGraph(CONFIG_GRAPH_URI);
		} catch (NoSuchEntityException nsee) {
			tcManager.createMGraph(CONFIG_GRAPH_URI);			
		}
		cgProvider.addTemporaryAdditionGraph(CONFIG_GRAPH_URI);
	}
	
	protected void deactivate(ComponentContext componentContext) {
		this.context = null;
		cgProvider.removeTemporaryAdditionGraph(CONFIG_GRAPH_URI);
	}
}
