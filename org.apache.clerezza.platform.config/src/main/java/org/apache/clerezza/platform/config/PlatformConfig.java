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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
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
	private MGraph systemGraph;
	private BundleContext context;
	private static String DEFAULT_PORT = "8080";


	/**
	 * Returns the default base URI of the Clerezza platform instance.
	 * @return the base URI of the Clerezza platform
	 */
	public UriRef getDefaultBaseUri() {
		Iterator<Resource> triples = new GraphNode(PLATFORM.Instance, systemGraph).
				getObjects(PLATFORM.defaultBaseUri);
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
	}

	/**
	 * Returns the base URIs of the Clerezza platform instance.
	 * A base Uri is the shortest URI of a URI-Hierarhy the platform handles.
	 * @return the base URI of the Clerezza platform
	 */
	public Set<UriRef> getBaseUris() {
		Iterator<Resource> baseUrisIter = new GraphNode(PLATFORM.Instance, systemGraph).
				getObjects(PLATFORM.baseUri);
		Set<UriRef> baseUris = new HashSet<UriRef>();
		while (baseUrisIter.hasNext()) {
			UriRef baseUri = (UriRef) baseUrisIter.next();
			baseUris.add(baseUri);
		}
		baseUris.add(getDefaultBaseUri());
		return baseUris;
	}

	/**
	 * The activate method is called when SCR activates the component configuration.
	 * 
	 * @param componentContext
	 */
	protected void activate(ComponentContext componentContext) {
		this.context = componentContext.getBundleContext();
	}
	
	protected void deactivate(ComponentContext componentContext) {
		this.context = null;
	}
}
