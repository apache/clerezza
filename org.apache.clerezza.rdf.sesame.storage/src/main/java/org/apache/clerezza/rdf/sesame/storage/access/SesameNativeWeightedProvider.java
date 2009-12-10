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
package org.apache.clerezza.rdf.sesame.storage.access;

import java.io.File;
import java.io.FileFilter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.repository.RepositoryException;

import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedGraphWrapper;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedMGraphWrapper;
import org.apache.clerezza.rdf.sesame.storage.SesameGraph;
import org.apache.clerezza.rdf.sesame.storage.SesameMGraph;

/**
 * This is a weighted provider for the sesame native graph implementation.
 * <p>
 * <b>Description</b><br />
 *  
 * This provider uses the file system to store and read MGraphs and Graphs. 
 * At startup, the data directory is scanned for subdirectories "mgraph" and 
 * "graph". For each subdirectory of these two directories, the provider
 * tries to load a Graph or an MGraph (according to the subdirectories).
 * The provider continues loading if there was a directory not containing a
 * sesame repository.
 * </p>
 * <p>
 * <b>Running in OSGi Environments</b><br />
 * SesameNativeWeightedProvider registers itself as a 
 * WeightedTcProvider.
 * </p>
 * <p>
 * <b>Configuration</b><br />
 * There is 1 property to configure: weight, it has an initial value of 100.
 * </p>
 * <p>
 * <b>Data Directory</b><br />
 * The default value of the "dataDirectory" property is "graphdata" in a
 * non-OSGi environment, in a OSGi environment it is set to the data directory
 * has a default value of 
 * <code>getBundleContext().getDataFile("sesame-data/")</code>.
 * </p>
 * 
 * @scr.component
 * @scr.service interface="org.apache.clerezza.rdf.core.access.WeightedTcProvider"
 * @scr.property name="weight" value="100"
 * 
 * @author msy
 */
public class SesameNativeWeightedProvider implements WeightedTcProvider {

	private static final String ENCODING = "utf-8";
	private static final String MGRAPH_SUFFIX = "/mgraph/";
	private static final String GRAPH_SUFFIX = "/graph/";
	/**
	 *	weight of the graph
	 */
	private int weight = 100;
	/**
	 *	set of mgraphs
	 */
	private HashMap<UriRef, SesameMGraph> mGraphs;
	/**
	 *	set of graphs
	 */
	private HashMap<UriRef, SesameGraph> graphs;
	/**
	 *	logger
	 */
	private Logger logger = LoggerFactory.getLogger(SesameNativeWeightedProvider.class);
	/**
	 *	directory where all graphs are stored
	 */
	private String dataDirectory = "graphdata";

	/**
	 *	Creates a new SesameNativeWeightedProvider.<br />
	 *  <br />
	 * Note that before the weighted provider can be used, the
	 * method <code>activate</code> has to be called.
	 */
	public SesameNativeWeightedProvider() {
		mGraphs = null;
		graphs = null;
		logger.info("Created SesameNativeWeightedProvider");
	}

	/**
	 * Activates this component.<br />
	 * <p>
	 * Initializes all graphs provided by this provider according to
	 * the configuration properties. 
	 * <p>
	 * 
	 * @param cCtx  Execution context of this component. A value of null is
	 *              acceptable when you set the property dataDirectory first.
	 * @throws IllegalArgumentException No component context given and data 
	 *			directory was not set.
	 */
	public void activate(ComponentContext cCtx) {

		if (cCtx != null) {
			try {
				String weightStr =
						(String) cCtx.getProperties().get("weight");
				weight = Integer.parseInt(weightStr);
			} catch (NumberFormatException nfe) {
				logger.warn(nfe.toString());
			}

			dataDirectory = cCtx.getBundleContext().
					getDataFile("sesame-data/").
					getAbsolutePath();

			logger.debug("Setting data directory to {}", dataDirectory);
		}
		loadAllGraphs();
		logger.info("Activated SesameNativeWeightedProvider.");
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
			//shutdown all mgraphs
			Iterator<SesameMGraph> mGraphIterator = mGraphs.values().iterator();
			while (mGraphIterator.hasNext()) {
				SesameMGraph g = mGraphIterator.next();
				if (g != null) {
					g.shutdown();
				}
			}

			//shutdown graphs
			Iterator<SesameGraph> graphIterator = graphs.values().iterator();
			while (graphIterator.hasNext()) {
				SesameGraph g = graphIterator.next();
				if (g != null) {
					g.shutdown();
				}
			}
		} catch (RepositoryException re) {
			logger.warn(re.toString(), re);
			throw new RuntimeException(re);
		}
		logger.info("Shutdown complete.");
	}

	private void loadAllGraphs() throws RuntimeException {

		try {
			synchronized (SesameNativeWeightedProvider.class) {
				if (mGraphs == null || graphs == null) {
					mGraphs = new HashMap<UriRef, SesameMGraph>();
					graphs = new HashMap<UriRef, SesameGraph>();
				} else {
					return;
				}
			}

			//load mgraphs
			File dataDir = new File(dataDirectory);
			File mgraphDir = new File(dataDir, MGRAPH_SUFFIX);

			if (!mgraphDir.exists()) {
				mgraphDir.mkdirs();
			}
			loadMGraphs(mgraphDir);

			//load graphs
			File graphDir = new File(dataDir, GRAPH_SUFFIX);

			if (!graphDir.exists()) {
				graphDir.mkdirs();
			}
			loadGraphs(graphDir);
		} catch (RepositoryException re) {
			logger.warn(re.toString(), re);
			throw new RuntimeException(re);
		}
	}

	/**
	 *	Loads all MGraphs
	 */
	private void loadMGraphs(File mgraphDir) throws RepositoryException {

		logger.debug("Scan directory for mgraph: {}",
				mgraphDir.getAbsolutePath());

		//for each subdirectory, create an mgraph
		File[] subDirs = getSubDirectories(mgraphDir);
		if (subDirs != null) {
			for (File subDirectory : subDirs) {
				String subDirName = subDirectory.getName();
				UriRef graphName = getGraphNameForDirectory(subDirName);
				SesameMGraph mGraph = new SesameMGraph();
				mGraph.initialize(subDirectory);
				mGraphs.put(graphName, mGraph);
				logger.debug("MGraph {} created.", graphName);
			}
		}
		logger.debug("mGraphs loaded");
	}

	/**
	 *	Loads all Graphs
	 */
	private void loadGraphs(File graphDir) throws RepositoryException {
		logger.debug("Scan directory for graph: {}",
				graphDir.getAbsolutePath());

		//for each subdirectory, create an mgraph
		File[] subDirs = getSubDirectories(graphDir);
		if (subDirs != null) {
			for (File subDirectory : subDirs) {

				String subDirName = subDirectory.getName();
				UriRef graphName = getGraphNameForDirectory(subDirName);
				SesameGraph graph = new SesameGraph();
				graph.initialize(subDirectory);
				graphs.put(graphName, graph);
				logger.debug("Graph {} created.", graphName);
			}
		}
		logger.debug("graphs loaded");
	}

	/**
	 *	helper method to get all subdirectories
	 */
	private File[] getSubDirectories(File parent) {
		return parent.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
	}

	/**
	 *	helper method to get graph name for directory
	 */
	private UriRef getGraphNameForDirectory(String directory) {
		try {
			UriRef name = new UriRef(URLDecoder.decode(directory, ENCODING));
			return name;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Your JVM doesn't support UTF-8", ex);
		}
	}

	/**
	 *	Gets the current weight of this provider.
	 * 
	 * @return weight
	 */
	@Override
	public int getWeight() {
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
		if ((name == null) || (name.getUnicodeString() == null) || (name.getUnicodeString().trim().length() == 0)) {
			throw new IllegalArgumentException("Name must not be null");
		}

		loadAllGraphs();

		if (graphs.containsKey(name)) {
			return new PrivilegedGraphWrapper(graphs.get(name));
		} else {
			throw new NoSuchEntityException(name);
		}
	}

	/**
	 *	Gets an MGraph by name.
	 * 
	 * @param name name of the graph
	 * @throws NoSuchEntityException if there is no such MGraph
	 */
	@Override
	public MGraph getMGraph(UriRef name) throws NoSuchEntityException {

		if ((name == null) || (name.getUnicodeString() == null) || (name.getUnicodeString().trim().length() == 0)) {
			throw new IllegalArgumentException("Name must not be null");
		}

		loadAllGraphs();

		if (mGraphs.containsKey(name)) {
			return new PrivilegedMGraphWrapper(mGraphs.get(name));
		} else {
			throw new NoSuchEntityException(name);
		}
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

		try {
			MGraph mGraph = getMGraph(name);
			return new PrivilegedMGraphWrapper(mGraph);
		} catch (NoSuchEntityException nsee) {
			return new PrivilegedGraphWrapper(getGraph(name));
		}
	}

	/**
	 *	Creates a new MGraph. 
	 * 
	 *  Creates a new data directory, initializes a new sesame repository there
	 *  and returns the MGraph.
	 * 
	 */
	@Override
	public MGraph createMGraph(UriRef name)
			throws EntityAlreadyExistsException {

		if ((name == null) || (name.getUnicodeString() == null) || (name.getUnicodeString().trim().length() == 0)) {
			throw new IllegalArgumentException("Name must not be null");
		}

		loadAllGraphs();

		// Checks that an MGraph with this name does not already exist
		if (mGraphs.containsKey(name)) {
			throw new EntityAlreadyExistsException(name);
		}

		// Creates a new directory
		String newDirectoryPath = getDirectoryForMGraphName(name);

		File newDir = new File(newDirectoryPath);
		newDir.mkdirs();

		logger.debug("Created directory: {}", newDir.getAbsolutePath());

		// Creates and initializes a new MGraph
		SesameMGraph graph = new SesameMGraph();

		try {
			graph.initialize(newDir);
			mGraphs.put(name, graph);
			logger.info("Created new MGraph: {}", name);
			return new PrivilegedMGraphWrapper(graph);
		} catch (RepositoryException re) {
			logger.warn("Error loading MGraph: {}", re);
			throw new RuntimeException(re);
		}
	}

	/**
	 *	Creates a new <code>Graph</code>.
	 * 
	 */
	@Override
	public Graph createGraph(UriRef name, TripleCollection triples) {

		if ((name == null) || (name.getUnicodeString() == null) || (name.getUnicodeString().trim().length() == 0)) {
			throw new IllegalArgumentException("Name must not be null");
		}

		loadAllGraphs();

		// Checks that a Graph with this name does not already exist
		if (graphs.containsKey(name)) {
			throw new EntityAlreadyExistsException(name);
		}

		// Creates a new directory
		String newDirectoryPath = getDirectoryForGraphName(name);

		File newDir = new File(newDirectoryPath);
		newDir.mkdirs();

		logger.debug("Created directory: {}", newDir.getAbsolutePath());

		// Creates and initializes a new Graph
		SesameGraph graph = new SesameGraph();

		try {
			graph.initialize(newDir, triples);
			graphs.put(name, graph);
			logger.info("Created new Graph: " + name);
			return new PrivilegedGraphWrapper(graph);

		} catch (RepositoryException re) {
			logger.warn("Error loading Graph: {}", re);
			throw new RuntimeException(re);
		}
	}

	/*
	 *	Deletes an MGraph or a Graph.
	 * 
	 *  <p>
	 *	If the graph does not exist in this provider, a NoSuchEntityException is
	 *  thrown. If there was an error deleting the graph, an 
	 *  EntityUndeletableException is thrown.
	 *  <br />
	 */
	@Override
	public void deleteTripleCollection(UriRef name)
			throws UnsupportedOperationException,
			NoSuchEntityException,
			EntityUndeletableException {

		loadAllGraphs();

		try {
			if (mGraphs.containsKey(name)) {
				deleteMGraph(name);
			} else if (graphs.containsKey(name)) {
				deleteGraph(name);
			} else {
				throw new NoSuchEntityException(name);
			}
		} catch (RepositoryException re) {
			logger.warn("Error deleting TripleCollection: {}", re);
			throw new RuntimeException(re);
		}
	}

	/**
	 *	Deletes an MGraph
	 */
	private void deleteMGraph(UriRef name)
			throws EntityUndeletableException, RepositoryException {

		loadAllGraphs();

		try {
			String dir = getDirectoryForMGraphName(name);
			File path = new File(dir);
			SesameMGraph mGraph = mGraphs.get(name);
			shutdown(mGraph, path);
			mGraphs.remove(name);
		} catch (Exception ex) {
			logger.warn("Error deleting an MGraph: {}", ex);
			throw new EntityUndeletableException(name);
		}
	}

	/**
	 *	helper to compose directory path for an MGraph name
	 * 
	 */
	private String getDirectoryForMGraphName(UriRef name) {
		return getDirectoryForName(name, MGRAPH_SUFFIX);
	}

	/**
	 *	helper method to create directory name for a graph name
	 */
	private String getDirectoryForName(UriRef name, String suffix) {
		try {
			StringWriter writer = new StringWriter();
			writer.append(dataDirectory);
			writer.append(suffix);
			writer.append(URLEncoder.encode(name.getUnicodeString(), ENCODING));
			return writer.toString();
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("Your JVM doesn't support UTF-8");
		}
	}

	/**
	 *	shutdown graph and destroy directory
	 */
	private void shutdown(SesameMGraph graph, File path)
			throws RepositoryException {

		graph.shutdown();
		deleteDirectory(path);
	}

	/**
	 *	helper method to REALLY delete a directoy even when it's not empty...
	 */
	private boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (File f : files) {
				if (f.isDirectory()) {
					deleteDirectory(f);
				} else {
					f.delete();
				}
			}
		}
		return (path.delete());
	}

	/**
	 *	Deletes a Graph
	 */
	private void deleteGraph(UriRef name)
			throws EntityUndeletableException, RepositoryException {

		loadAllGraphs();

		try {
			String dir = getDirectoryForGraphName(name);
			File path = new File(dir);
			SesameGraph graph = graphs.get(name);
			shutdown(graph, path);
			graphs.remove(name);
		} catch (Exception ex) {
			logger.warn("Error deleting a Graph: {}", ex);
			throw new EntityUndeletableException(name);
		}
	}

	/**
	 *	helper to compose directory path for graph
	 */
	private String getDirectoryForGraphName(UriRef name) {
		return getDirectoryForName(name, GRAPH_SUFFIX);
	}

	/**
	 *	Gets all names of a Graph
	 * 
	 */
	@Override
	public Set<UriRef> getNames(Graph graph) {

		if (graphs == null) {
			loadAllGraphs();
		}

		Set<UriRef> result = new HashSet<UriRef>();
		Iterator<UriRef> keys = graphs.keySet().iterator();
		while (keys.hasNext()) {
			UriRef key = keys.next();
			SesameGraph g = graphs.get(key);
			if (g.equals(graph)) {
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

	/**
	 *	set the data directory. 
	 * 
	 * @param d data directory where all graphs are stored
	 */
	public void setDataDirectory(String d) {
		dataDirectory = d;
		mGraphs = null;
		graphs = null;
	}

	@Override
	public Set<UriRef> listGraphs() {
		return graphs.keySet();
	}

	@Override
	public Set<UriRef> listMGraphs() {
		return mGraphs.keySet();
	}

	@Override
	public Set<UriRef> listTripleCollections() {
		Set<UriRef> result = new HashSet<UriRef>();
		result.addAll(listGraphs());
		result.addAll(listMGraphs());
		return result;
	}
}
