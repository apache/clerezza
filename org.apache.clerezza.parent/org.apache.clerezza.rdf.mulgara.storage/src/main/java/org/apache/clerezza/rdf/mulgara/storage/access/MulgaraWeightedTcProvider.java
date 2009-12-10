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
package org.apache.clerezza.rdf.mulgara.storage.access;

import java.io.File;
import java.net.URI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mulgara.query.QueryException;
import org.mulgara.server.NonRemoteSessionException;
import org.mulgara.server.Session;
import org.mulgara.server.driver.SessionFactoryFinder;
import org.mulgara.server.driver.SessionFactoryFinderException;
import org.mulgara.server.local.LocalSessionFactory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.impl.SimpleGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedMGraphWrapper;
import org.apache.clerezza.rdf.mulgara.storage.MulgaraMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.TCPROVIDER;

/**
 * * 
 * @scr.component immediate=true
 * @scr.service interface="org.apache.clerezza.rdf.core.access.WeightedTcProvider"
 * @scr.property name="weight" type="Integer" value="101"
 * 
 * @author mir
 */
public class MulgaraWeightedTcProvider implements WeightedTcProvider {

	/**
	 * The host URI of the mulgara server
	 */
	private static final URI HOST = URI.create("local://localhost/server1");
	/**
	 *	weight of the graph
	 */
	private int weight = 101;
	/**
	 *	set of mgraphs
	 */
	private HashMap<UriRef, MGraph> mGraphs;
	/**
	 *	set of graphs
	 */
	private HashMap<UriRef, Graph> graphs;
	/**
	 *	logger
	 */
	private Logger logger = LoggerFactory.getLogger(MulgaraWeightedTcProvider.class);
	/**
	 *	directory where all graphs are stored
	 */
	private String defaultDirectory = System.getProperty("user.home") + "/.mulgara";
	/*
	 * The directory where the mulgara data will be stored pesistently
	 */ 
	private File dataDirectory;
	/**
	 * Uri of the graph containing information if a mulgara graphs are either
	 * MGraphs or Graphs.
	 */
	private URI mulgaraInfoGraphUri = URI.create("http://clerezza.org/mulgaraInfoGraph");

	/**
	 * The graph containing the information if a mulgara graphs are either
	 * MGraphs or Graphs.
	 */
	private MGraph mulgaraInfoGraph;

	private LocalSessionFactory lsf;
	
	private boolean isInitialized = false;

	/**
	 *	Creates a new MulgaraWeightedTcProvider.<br />
	 *  <br />
	 * Note that before the weighted provider can be used, the
	 * method <code>activate</code> has to be called.
	 */
	public MulgaraWeightedTcProvider() {
		logger.info("Created MulgaraWeightedTcProvider");
	}

	public MulgaraWeightedTcProvider(File directory) {
		logger.info("Created MulgaraWeightedTcProvider");
		//initialize(directory);
		dataDirectory = directory;
	}

	private void initialize() {
		mGraphs = new HashMap<UriRef, MGraph>();
		graphs = new HashMap<UriRef, Graph>();
		
		if (dataDirectory == null) {
			dataDirectory = new File(defaultDirectory);
		}
		
		if(!dataDirectory.exists()){
				dataDirectory.mkdir();
				logger.info("Mulgara data directory created at {}",
						dataDirectory.toString());
		}
		
		try {
			lsf = (LocalSessionFactory) SessionFactoryFinder.newSessionFactory(HOST);
			lsf.setDirectory(dataDirectory);
			lsf.setServerURI(HOST);
		} catch (SessionFactoryFinderException ex) {
			throw new RuntimeException(ex);
		} catch (NonRemoteSessionException ex) {
			throw new RuntimeException(ex);
		} catch (java.lang.IllegalStateException ex) {
		}
		loadExistingGraphs();
		isInitialized = true;
	}

	private void loadExistingGraphs() {
		mulgaraInfoGraph = 
				new PrivilegedMGraphWrapper(new MulgaraMGraph(mulgaraInfoGraphUri, lsf));

		if (!modelExists(mulgaraInfoGraphUri)) {
			createNewModel(mulgaraInfoGraphUri);
			return;
		}
		
		Iterator<Triple> graphsOfTypeGraph =
				mulgaraInfoGraph.filter(null, RDF.type, TCPROVIDER.Graph);
		while (graphsOfTypeGraph.hasNext()) {
			UriRef graph = (UriRef)graphsOfTypeGraph.next().getSubject();
			URI graphUri = URI.create(graph.getUnicodeString());
			graphs.put(graph, new SimpleGraph(new MulgaraMGraph(graphUri, lsf)));
		}

		Iterator<Triple> graphsOfTypeMGraph =
				mulgaraInfoGraph.filter(null, RDF.type, TCPROVIDER.MGraph);
		while (graphsOfTypeMGraph.hasNext()) {
			UriRef mGraph = (UriRef)graphsOfTypeMGraph.next().getSubject();
			URI graphUri = URI.create(mGraph.getUnicodeString());
			mGraphs.put(mGraph, new MulgaraMGraph(graphUri, lsf));
		}
	}

	/**
	 * Activates this component.
	 *
	 * Initializes all graphs provided by this provider according to
	 * the configuration properties. 
	 *
	 * @param cCtx  Execution context of this component. A value of null is
	 *			  acceptable when you set the property dataDirectory first.
	 * @throws IllegalArgumentException No component context given and data 
	 *			directory was not set.
	 */
	public void activate(ComponentContext context) {

		if (context != null) {
				weight = (Integer) context.getProperties().get("weight");
				String dataDirectoryName = context.getBundleContext().getDataFile("mulgara-data/").getAbsolutePath();
				dataDirectory = new File(dataDirectoryName);
				logger.debug("Setting data directory to {}", dataDirectory);
		}
		logger.info("Activated MulgaraNativeWeightedProvider.");
	}

	private boolean deleteModel(URI graphUri) {
		
		try {
			Session session = lsf.newJRDFSession();
			try {
				 session.removeModel(graphUri);
				 return true;
			} finally {
				session.close();
			}
		} catch (QueryException e) {
			logger.warn("QueryException: {}", e);
			return false;
		}
	}

	private boolean modelExists(URI graphUri) {
		
		try {
			Session session = lsf.newJRDFSession();
			try {
				return session.modelExists(graphUri);
			} finally {
				session.close();
			}
		} catch (QueryException e) {
			logger.warn("QueryException: {}", e);
			return false;
		}
	}

	private void createNewModel(URI graphUri) throws RuntimeException {
		try {
			Session session = lsf.newJRDFSession();
			try {
				session.createModel(graphUri, Session.MULGARA_GRAPH_URI);
			} finally {
				session.close();
			}
		} catch (QueryException e) {
			logger.warn("QueryException: {}", e);
			throw new RuntimeException(e);
		}
	}

	protected void clearAll() {
		Iterator<UriRef> mGraphUris = mGraphs.keySet().iterator();
		while(mGraphUris.hasNext()) {
			UriRef mGraphUri = mGraphUris.next();
			deleteModel(URI.create(mGraphUri.getUnicodeString()));
		}

		Iterator<UriRef> graphUris = graphs.keySet().iterator();
		while(graphUris.hasNext()) {
			UriRef graphUri = graphUris.next();
			deleteModel(URI.create(graphUri.getUnicodeString()));
		}

		deleteModel(mulgaraInfoGraphUri);
	}

	/**
	 *	Deactivates this component.
	 * 
	 *  This deactivates all MGraphs provided by this provider.
	 * 
	 * @param cCtx component context provided by OSGi
	 */
	protected void deactivate(ComponentContext cCtx) {
		try {
			lsf.close();
		} catch (QueryException ex) {
			throw new RuntimeException(ex);
		}
		logger.info("Shutdown complete.");
	}

	/**
	 *	Gets the current weight of this provider.
	 * 
	 * @return weight
	 */
	@Override
	public int getWeight() {
		if(!isInitialized) {
			initialize();
		}
		return weight;
	}

	/**
	 *	Gets a Graph by name.
	 * 
	 * @param name name of the Graph
	 * @throws NoSuchEntityException if there is no such Graph
	 */
	@Override
	public Graph getGraph(UriRef name) throws NoSuchEntityException {
		if(!isInitialized) {
			initialize();
		}
		if ((name == null) || (name.getUnicodeString() == null) || 
				(name.getUnicodeString().trim().length() == 0)) {
			throw new IllegalArgumentException("Name must not be null");
		}

		if (!graphs.containsKey(name)) {
			throw new NoSuchEntityException(name);
		}

		return graphs.get(name);
	}

	/**
	 *	Gets an MGraph by name.
	 * 
	 * @param name name of the graph
	 * @throws NoSuchEntityException if there is no such MGraph
	 */
	@Override
	public MGraph getMGraph(UriRef name) throws NoSuchEntityException {
		if(!isInitialized) {
			initialize();
		}
		if ((name == null) || (name.getUnicodeString() == null) ||
				(name.getUnicodeString().trim().length() == 0)) {
			throw new IllegalArgumentException("Name must not be null");
		}
		if (!mGraphs.containsKey(name)) {
			throw new NoSuchEntityException(name);
		}
		return mGraphs.get(name);
	}

	/**
	 *	Gets a TripleCollection from an MGraph or a Graph for a specific name.
	 * 
	 *  The method checks if there is an MGraph and returns it. If there is no
	 *  MGraph, it looks at the graphs. If there is no Graph, it throws a
	 *  NoSuchEntityException.
	 * 
	 */
	@Override
	public TripleCollection getTriples(UriRef name)
			throws NoSuchEntityException {
		if(!isInitialized) {
			initialize();
		}
		try {
			MGraph mGraph = getMGraph(name);
			return mGraph;
		} catch (NoSuchEntityException nsee) {
			return getGraph(name);
		}
	}

	/**
	 *	Creates a new MGraph. 
	 */
	@Override
	public MGraph createMGraph(UriRef name)
			throws EntityAlreadyExistsException {
		if(!isInitialized) {
			initialize();
		}
		if ((name == null) || (name.getUnicodeString() == null) 
				|| (name.getUnicodeString().trim().length() == 0)) {
			throw new IllegalArgumentException("Name must not be null");
		}
		// Checks that an MGraph with this name does not already exist
		if (mGraphs.containsKey(name)) {
			throw new EntityAlreadyExistsException(name);
		}
		URI graphUri = URI.create(name.getUnicodeString());
		createNewModel(graphUri);
		MGraph mGraph = new PrivilegedMGraphWrapper(new MulgaraMGraph(graphUri,lsf));
		mGraphs.put(name, mGraph);
		// save mapping persistent
		Triple triple = new TripleImpl(name, RDF.type, TCPROVIDER.MGraph);
		mulgaraInfoGraph.add(triple);
		return mGraph;
	}

	/**
	 *	Creates a new <code>Graph</code>.
	 * 
	 */
	@Override
	public Graph createGraph(UriRef name, TripleCollection triples) {
		if(!isInitialized) {
			initialize();
		}
		if ((name == null) || (name.getUnicodeString() == null)
				|| (name.getUnicodeString().trim().length() == 0)) {
			throw new IllegalArgumentException("Name must not be null");
		}
		// Checks that an MGraph with this name does not already exist
		if (graphs.containsKey(name)) {
			throw new EntityAlreadyExistsException(name);
		}

		URI graphUri = URI.create(name.getUnicodeString());
		createNewModel(graphUri);
		final MGraph mGraph = new PrivilegedMGraphWrapper(new MulgaraMGraph(graphUri, lsf));
		mGraph.addAll(triples);
		Graph graph = new SimpleGraph(mGraph);
		graphs.put(name, graph);
		// save mapping persistent
		Triple triple = new TripleImpl(name, RDF.type, TCPROVIDER.Graph);
		mulgaraInfoGraph.add(triple);
		return graph;
	}

	/*
	 *	Deletes an MGraph or a Graph.
	 * 
	 *	If the graph does not exist in this provider, a NoSuchEntityException is
	 *  thrown. If there was an error deleting the graph, an 
	 *  EntityUndeletableException is thrown.
	 */
	@Override
	public void deleteTripleCollection(UriRef name)
			throws UnsupportedOperationException,
			NoSuchEntityException,
			EntityUndeletableException {
		if(!isInitialized) {
			initialize();
		}
		if (mGraphs.containsKey(name)) {
			deleteMGraph(name);
		} else if (graphs.containsKey(name)) {
			deleteGraph(name);
		} else {
			throw new NoSuchEntityException(name);
		}
	}

	/**
	 *	Deletes an MGraph
	 */
	private void deleteMGraph(UriRef name)
		throws EntityUndeletableException {
		if(!isInitialized) {
			initialize();
		}
		if (deleteModel(URI.create(name.getUnicodeString()))){
			mGraphs.remove(name);
			// remove from persistent mapping
			Triple triple = new TripleImpl(name, RDF.type, TCPROVIDER.MGraph);
			mulgaraInfoGraph.remove(triple);
		} else {
			throw new EntityUndeletableException(name);
		}	
	}

	/**
	 *	Deletes a Graph
	 */
	private void deleteGraph(UriRef name)
			throws EntityUndeletableException {
		if(!isInitialized) {
			initialize();
		}
		if (deleteModel(URI.create(name.getUnicodeString()))){
			graphs.remove(name);
			Triple triple = new TripleImpl(name, RDF.type, TCPROVIDER.Graph);
			mulgaraInfoGraph.remove(triple);
		} else {
			throw new EntityUndeletableException(name);
		}
	}

	/**
	 *	Gets all names of a Graph
	 * 
	 */
	@Override
	public Set<UriRef> getNames(Graph graph) {
		if(!isInitialized) {
			initialize();
		}
		Set<UriRef> result = new HashSet<UriRef>();
		Iterator<UriRef> graphUris = graphs.keySet().iterator();
		while (graphUris.hasNext()) {
			UriRef key = graphUris.next();
			Graph storedGraph = graphs.get(key);
			if (storedGraph.equals(graph)) {
				result.add(key);
			}
		}
		return result;
	}

	/**
	 *	set the current weight. 
	 *
	 * @param w weight for this provider
	 */
	public void setWeight(int w) {
		weight = w;
	}

	@Override
	public Set<UriRef> listGraphs() {
		if(!isInitialized) {
			initialize();
		}
		return graphs.keySet();
	}

	@Override
	public Set<UriRef> listMGraphs() {
		if(!isInitialized) {
			initialize();
		}
		return mGraphs.keySet();
	}

	@Override
	public Set<UriRef> listTripleCollections() {
		if(!isInitialized) {
			initialize();
		}
		Set<UriRef> result = new HashSet<UriRef>(graphs.keySet());
		result.addAll(mGraphs.keySet());
		return result;
	}
}
