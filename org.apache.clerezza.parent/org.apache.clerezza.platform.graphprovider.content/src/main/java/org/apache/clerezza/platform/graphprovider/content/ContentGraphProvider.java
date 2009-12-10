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
package org.apache.clerezza.platform.graphprovider.content;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.utils.UnionMGraph;

/**
 * A service providing a method to retrieve a <code>UnionGraph</code> containing
 * the ContentGraph and additional graphs.
 * 
 * Over the Configuration Admin Service additional graphs can be added
 * permanently. With the methods <code>addTemporaryAdditionGraph</code> and
 * <code>removeTemporaryAdditionGraph</code> graphs can be added temporarily.
 * Temporarily means that after restarting the org.apache.clerezza.platform.graphprovider.content
 * bundle, these graphs will no longer be returned in the <code>UnionGraph</code>.
 * 
 * @scr.component
 * @scr.service interface="org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider"
 * 
 * @author rbn, mir
 */
public class ContentGraphProvider {

	/**
	 * Service property header which specifies the URI of the graph used by
	 * Discobits
	 * 
	 * @scr.property type="String" value="http://tpf.localhost/content.graph"
	 *               description
	 *               ="Specifies the URI of the graph used by Discobits."
	 */
	public static final String DISCOBITS_GRAPH_URI_PROPERTY = "Discobits_graph_URI";

	/**
	 * Service property header, which contains the URIs of additional
	 * <code>TripleCollection</code>s that are united as read-only to the Graph
	 * returned by <code>getContentGraph</code>
	 * 
	 * @scr.property name="additions" values.name="" description="Contains
	 *               additional TripleCollections that are added to the content
	 *               graph for reading"
	 */
	public static final String CONTENT_ADDITIONS = "additions";

	/**
	 * @scr.reference
	 */
	private TcManager tcManager;

	/**
	 * The URI which is specified in the service property "Discobits_graph_URI"
	 */
	private UriRef graphUri;

	/**
	 * The URIs of the read-only addition-<code>TripleCollection</code>s
	 */
	private UriRef[] additions;

	private ReentrantReadWriteLock configLock = new ReentrantReadWriteLock();

	protected void activate(ComponentContext context) {
		Dictionary<?, ?> properties = context.getProperties();
		graphUri = new UriRef((String) properties
				.get(DISCOBITS_GRAPH_URI_PROPERTY));
		try {
			tcManager.getMGraph(graphUri);
		} catch (NoSuchEntityException nsee) {
			tcManager.createMGraph(graphUri);
		}
		String[] additionUriStrings = (String[]) context.getProperties().get(
				CONTENT_ADDITIONS);
		additions = new UriRef[additionUriStrings.length];
		for (int i = 0; i < additionUriStrings.length; i++) {
			additions[i] = new UriRef(additionUriStrings[i]);

		}
	}

	public MGraph getContentGraph() {
		configLock.readLock().lock();
		try {
			TripleCollection[] united = new TripleCollection[additions.length + 1];
			int i = 0;
			united[i++] = tcManager.getMGraph(graphUri);
			for (UriRef uriRef : additions) {
				united[i++] = tcManager.getTriples(uriRef);
			}
			return new UnionMGraph(united);
		} finally {
			configLock.readLock().unlock();
		}
	}

	public void addTemporaryAdditionGraph(UriRef graphName) {
		configLock.writeLock().lock();
		try {
			Set<UriRef> additionsSet = new HashSet<UriRef>(Arrays
					.asList(additions));
			additionsSet.add(graphName);
			additions = additionsSet.toArray(new UriRef[additionsSet.size()]);
		} finally {
			configLock.writeLock().unlock();
		}
	}

	public void removeTemporaryAdditionGraph(UriRef graphName) {
		configLock.writeLock().lock();
		try {
			Set<UriRef> additionsSet = new HashSet<UriRef>(Arrays
					.asList(additions));
			additionsSet.remove(graphName);
			additions = additionsSet.toArray(new UriRef[additionsSet.size()]);
		} finally {
			configLock.writeLock().unlock();
		}
	}
}
