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
package org.apache.clerezza.rdf.storage.externalizer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.TcProviderMultiplexer;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link org.apache.clerezza.rdf.core.access.WeightedTcProvider} storing
 * large literals to the file system and the triples (with references
 * instead of the literals) to another TcProvider.
 * <br/>
 * As this provider is not currently included in any clerezza launcher, the way
 * to install it is as follows:<br/>
 *
 * - start clerezza<br/>
 * - install the bundle and set its startlevel to 3<br/>
 * - shutdown clerezza<br/>
 * - delete the MGraphs that shall use the externalizer from the felix cache<br/>
 * - start clerezza again<br/>
 *
 * @author reto
 *
 */
@Component
@Service(WeightedTcProvider.class)
@Property(name = "weight", intValue = 500)
@Reference(name="weightedTcProvider", policy=ReferencePolicy.DYNAMIC,
		referenceInterface=WeightedTcProvider.class,
		cardinality=ReferenceCardinality.MANDATORY_MULTIPLE)
public class ExternalizingProvider implements WeightedTcProvider {

	private static final String EXTERNALIZEDLITERALS_SUFFIX = "-externalizedliterals";

	//@Reference(policy=ReferencePolicy.DYNAMIC, cardinality=ReferenceCardinality.OPTIONAL_UNARY)
	private TcProviderMultiplexer tcProvider = new TcProviderMultiplexer();
	/**
	 *	directory where all graphs are stored
	 */
	private static final String RELATIVE_DATA_PATH_NAME = "externalized-literals/";
	private File dataPath;
	private static final Logger log = LoggerFactory.getLogger(ExternalizingProvider.class);
	private int weight = 500;

	public ExternalizingProvider() {
	}

	ExternalizingProvider(File directory) {
		dataPath = directory;
		tcProvider = TcManager.getInstance();
	}

	protected void activate(ComponentContext cCtx) {
		log.info("Activating literal externalizing provider");
		if (cCtx != null) {
			weight = (Integer) cCtx.getProperties().get("weight");
			dataPath = cCtx.getBundleContext().getDataFile(RELATIVE_DATA_PATH_NAME);
		}
	}

	protected void deactivate(ComponentContext cCtx) {
		dataPath = null;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	/**
	 * we don't do graphs
	 * 
	 * @param name
	 * @return
	 * @throws NoSuchEntityException
	 */
	@Override
	public Graph getGraph(UriRef name) throws NoSuchEntityException {
		throw new NoSuchEntityException(name);
	}

	@Override
	public synchronized MGraph getMGraph(UriRef name) throws NoSuchEntityException {
		if (name.getUnicodeString().endsWith(EXTERNALIZEDLITERALS_SUFFIX)) {
			throw new IllegalArgumentException();
		}
		try {
			final UriRef baseGraphName = new UriRef(name.getUnicodeString() + EXTERNALIZEDLITERALS_SUFFIX);
			if (tcProvider == null) {
				throw new RuntimeException("MGraph retrieval currently not possible: tcManager unavailable");
			}
			final MGraph baseGraph = AccessController.doPrivileged(new PrivilegedExceptionAction<MGraph>() {

				@Override
				public MGraph run() {
					return tcProvider.getMGraph(baseGraphName);
				}
			});
			return new ExternalizingMGraph(baseGraph, getHashStoreDir(name));
		} catch (PrivilegedActionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException)cause;
			}
			if (cause instanceof EntityAlreadyExistsException) {
				throw (EntityAlreadyExistsException)cause;
			}
			if (cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			}
			throw new RuntimeException(cause);
		}
	}

	@Override
	public TripleCollection getTriples(UriRef name) throws NoSuchEntityException {
		return getMGraph(name);

	}

	@Override
	public synchronized MGraph createMGraph(UriRef name)
			throws UnsupportedOperationException, EntityAlreadyExistsException {
		try {
			if (name.getUnicodeString().endsWith(EXTERNALIZEDLITERALS_SUFFIX)) {
				throw new IllegalArgumentException();
			}
			final UriRef baseGraphName = new UriRef(name.getUnicodeString() + EXTERNALIZEDLITERALS_SUFFIX);
			if (tcProvider == null) {
				throw new RuntimeException("MGraph creation currently not possible: tcManager unavailable");
			}
			final MGraph baseGraph = AccessController.doPrivileged(new PrivilegedExceptionAction<MGraph>() {

				@Override
				public MGraph run() {
					return tcProvider.createMGraph(baseGraphName);
				}
			});
			File hashStoreDir = getHashStoreDir(name);
			hashStoreDir.mkdirs();
			return new ExternalizingMGraph(baseGraph, hashStoreDir);
		} catch (PrivilegedActionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException)cause;
			}
			if (cause instanceof EntityAlreadyExistsException) {
				throw (EntityAlreadyExistsException)cause;
			}
			if (cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			}
			throw new RuntimeException(cause);
		}
	}

	@Override
	public Graph createGraph(UriRef name, TripleCollection triples)
			throws UnsupportedOperationException, EntityAlreadyExistsException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteTripleCollection(UriRef name)
			throws UnsupportedOperationException, NoSuchEntityException,
			EntityUndeletableException {
		try {
			final UriRef baseGraphName = new UriRef(name.getUnicodeString() + EXTERNALIZEDLITERALS_SUFFIX);
			AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

				@Override
				public Object run() {
					tcProvider.deleteTripleCollection(baseGraphName);
					return null;
				}
			});
			delete(getHashStoreDir(name));
		} catch (PrivilegedActionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof UnsupportedOperationException) {
				throw (UnsupportedOperationException)cause;
			}
			if (cause instanceof EntityAlreadyExistsException) {
				throw (EntityAlreadyExistsException)cause;
			}
			if (cause instanceof RuntimeException) {
				throw (RuntimeException)cause;
			}
			throw new RuntimeException(cause);
		}
	}

	/**
	 * Cleans the content of the specified directory recursively.
	 * @param dir  Abstract path denoting the directory to clean.
	 */
	private static void cleanDirectory(File dir) {
		File[] files = dir.listFiles();
		if (files != null && files.length > 0) {
			for (File file : files) {
				delete(file);
			}
		}
	}

	/**
	 * Deletes the specified file or directory.
	 * @param file  Abstract path denoting the file or directory to clean.
	 */
	protected static void delete(File file) {
		if (file.isDirectory()) {
			cleanDirectory(file);
		}
		file.delete();
	}

	@Override
	public Set<UriRef> getNames(Graph graph) {
		//this could be done more efficiently with an index, could be done with
		//a MultiBidiMap (BidiMap allowing multiple keys for the same value)
		Set<UriRef> result = new HashSet<UriRef>();
		for (UriRef name : listGraphs()) {
			if (getGraph(name).equals(graph)) {
				result.add(name);
			}
		}
		return result;
	}

	@Override
	public Set<UriRef> listTripleCollections() {
		Set<UriRef> result = new HashSet<UriRef>();
		result.addAll(listGraphs());
		result.addAll(listMGraphs());
		return result;
	}

	@Override
	public Set<UriRef> listGraphs() {
		return new HashSet<UriRef>();
	}

	@Override
	public Set<UriRef> listMGraphs() {
		Set<UriRef> result = getHashStoreUris();
		for (Iterator<UriRef> it = result.iterator(); it.hasNext();) {
			UriRef graphName = it.next();
			final UriRef baseGraphName = new UriRef(graphName.getUnicodeString() + EXTERNALIZEDLITERALS_SUFFIX);
			try {
				tcProvider.getMGraph(baseGraphName);
			} catch (NoSuchEntityException e) {
				log.warn("Store for externalized literals but no base graph found for {}.", graphName);
				it.remove();
			}

		}
		return result;
	}

	private File getHashStoreDir(UriRef name) {
		try {
			String subDirName = URLEncoder.encode(name.getUnicodeString(), "utf-8");
			File result = new File(dataPath, subDirName);
			return result;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("utf-8 not supported", ex);
		}
	}

	private Set<UriRef> getHashStoreUris() {
		Set<UriRef> result = new HashSet<UriRef>();
		if (dataPath.exists()) {
			for (String mGraphDirName : dataPath.list()) {
				try {
					UriRef uri = new UriRef(URLDecoder.decode(mGraphDirName, "utf-8"));
					result.add(uri);
				} catch (UnsupportedEncodingException ex) {
					throw new RuntimeException("utf-8 not supported", ex);
				}
			}
		}
		return result;
	}

	/**
	 * Register a provider
	 *
	 * @param provider
	 *            the provider to be registered
	 */
	protected void bindWeightedTcProvider(WeightedTcProvider provider) {
		tcProvider.addWeightedTcProvider(provider);
	}

	/**
	 * Deregister a provider
	 *
	 * @param provider
	 *            the provider to be deregistered
	 */
	protected void unbindWeightedTcProvider(WeightedTcProvider provider) {
		tcProvider.removeWeightedTcProvider(provider);
	}
}
