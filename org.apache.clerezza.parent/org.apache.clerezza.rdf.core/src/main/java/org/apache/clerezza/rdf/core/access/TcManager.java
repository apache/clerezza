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
package org.apache.clerezza.rdf.core.access;

import org.apache.clerezza.rdf.core.access.security.TcPermission;
import org.apache.clerezza.rdf.core.impl.WriteBlockedMGraph;
import org.apache.clerezza.rdf.core.impl.WriteBlockedTripleCollection;

import java.lang.ref.WeakReference;
import java.security.AccessControlException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.sparql.query.AskQuery;
import org.apache.clerezza.rdf.core.sparql.query.ConstructQuery;
import org.apache.clerezza.rdf.core.sparql.query.DescribeQuery;
import org.apache.clerezza.rdf.core.sparql.NoQueryEngineException;
import org.apache.clerezza.rdf.core.sparql.query.Query;
import org.apache.clerezza.rdf.core.sparql.QueryEngine;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.query.SelectQuery;

/**
 * This class implements <code>TcManager</code>, delegating the actual
 * provision and creation of Graphs or MGraphs to registered <code>TcProvider</code>s. The class
 * attempts to satisfy the request using the register <code>WeightedTcProvider</code>
 * in decreasing order of weight. If multiple providers have the same weight the
 * lexicographical order of the fully qualified class name determines which one
 * is used, namely the one that occurs earlier. If a call to a registered provider
 * causes an <code>IllegalArgumentException</code>, <code>NoSuchEntityException</code>
 * or <code>UnsupportedOperationException</code> then the call is delegated to the
 * next provider.
 *
 * Only one instance of this class should exist in a system, the public no
 * argument constructor is meant for initialization by dependency injection systems
 * such as OSGi-DS. Applications should use the static <code>getInstance()</code>
 * method if they aren't using a framework that injects them the instance.
 *
 * This class returns <code>LockableMGraph</code>s a subtype of <code>MGraph</code>
 * that allows read/write locks.
 *
 *
 * @author reto, mir, hasan
 * 
 * @scr.component
 * @scr.service interface="org.apache.clerezza.rdf.core.access.TcManager"
 * @scr.reference name="weightedTcProvider" cardinality="1..n"
 *                policy="dynamic"
 *                interface="org.apache.clerezza.rdf.core.access.WeightedTcProvider"
 * 
 */
public class TcManager implements TcProvider {

	private SortedSet<WeightedTcProvider> providerList = new TreeSet<WeightedTcProvider>(
			new WeightedProviderComparator());
	private static volatile TcManager instance;

	/**
	 * Mapping to LockableMGraph's and ServiceRegistration using their URI's as key.
	 * Makes sure that per URI only one instance of the LockableMGraph is used,
	 * otherwise the locks in the <code>LockableMGraph</code>s would have no effect
	 * between different instances and concurrency issues could occur.
	 */
	private Map<UriRef, GraphHolder> synchronizedLockableMGraphCache = Collections
			.synchronizedMap(new HashMap<UriRef, GraphHolder>());

	/**
	 * @scr.reference cardinality="0..1"
	 */
	protected QueryEngine queryEngine;

	private ComponentContext componentContext;

	/*
	 * A store that keeps <code>WeightedTcProvider</code>S in case the
	 * componentContext is null. Used in activate().
	 */
	private Set<WeightedTcProvider> providerStore =
			Collections.synchronizedSet(new HashSet<WeightedTcProvider>());

	/**
	 * the constructor sets the singleton instance to allow instantiation by
	 * OSGi-DS. This constructor should not be called except by OSGi-DS,
	 * otherwise the static <code>getInstance</code> method should be used.
	 */
	public TcManager() {
		TcManager.instance = this;
	}

	/**
	 * This returns the singleton instance. If an instance has been previously
	 * created (e.g. by OSGi declarative services) this instance is returned,
	 * otherwise a new instance is created and providers are injected using the
	 * service provider interface (META-INF/services/)
	 * 
	 * @return the singleton instance
	 */
	public static TcManager getInstance() {
		if (instance == null) {
			synchronized (TcManager.class) {
				if (instance == null) {
					new TcManager();
					Iterator<WeightedTcProvider> weightedProviders = ServiceLoader
							.load(WeightedTcProvider.class).iterator();
					while (weightedProviders.hasNext()) {
						WeightedTcProvider weightedProvider = weightedProviders
								.next();
						instance
								.bindWeightedTcProvider(weightedProvider);
					}
					Iterator<QueryEngine> queryEngines = ServiceLoader.load(
							QueryEngine.class).iterator();
					System.out.println("looking for QE");
					if (queryEngines.hasNext()) {
						instance.queryEngine = queryEngines.next();
						System.out.println("QE: "
								+ instance.queryEngine.getClass());
					}
				}
			}
		}
		return instance;
	}

	protected void activate(final ComponentContext componentContext) {
		this.componentContext = componentContext;
		Iterator<WeightedTcProvider> it = providerStore.iterator();
		while (it.hasNext()) {
			WeightedTcProvider provider = it.next();
			updateLockableMGraphCache(provider, true);			
		}
		providerStore.clear();
	}

	protected void deactivate(final ComponentContext componentContext) {
		this.componentContext = null;
	}

	/**
	 * Registers a provider
	 * 
	 * @param provider
	 *            the provider to be registered
	 */
	protected void bindWeightedTcProvider(WeightedTcProvider provider) {
		providerList.add(provider);
		if (componentContext != null) {
			updateLockableMGraphCache(provider, true);
		}  else {
			providerStore.add(provider);
		}
	}

	/**
	 * Unregister a provider
	 * 
	 * @param provider
	 *            the provider to be deregistered
	 */
	protected void unbindWeightedTcProvider(
			WeightedTcProvider provider) {
		providerList.remove(provider);
		providerStore.remove(provider);
		updateLockableMGraphCache(provider, false);
	}

	/**
	 * Updates the lockableMGraphCache AFTER a new <code>provider</code> was
	 * bound or unbound.
	 * This method also takes care of registering and unregistering
	 * provided triple collections as services based on the weight of
	 * all affected providers.
	 * 
	 * @param provider
	 *            the provider that was added or removed
	 * @param providerAdded
	 *            <code>boolean</code> that should be set as <code>true</code>
	 *            if <code>provider</code> was added to
	 *            <code>org.apache.clerezza.rdf.core.TcManager.providerList</code>
	 *            otherwise <code>false</code>
	 */
	private void updateLockableMGraphCache(WeightedTcProvider provider,
			boolean providerAdded) {
		Set<UriRef> uriSet = provider.listTripleCollections();
		if (!(uriSet == null || uriSet.isEmpty())) {
			if (providerAdded) {
				weightedProviderAdded(provider, uriSet);
			} else {
				weightedProviderRemoved(provider, uriSet);
			}
		}
	}

	private void weightedProviderAdded(WeightedTcProvider newProvider,
			Set<UriRef> newProvidedUris) {
		Set<WeightedTcProvider> lowerWeightedProviderList = getLowerWeightedProvider(newProvider);
		for (UriRef name : newProvidedUris) {
			final GraphHolder holder = synchronizedLockableMGraphCache
					.get(name);
			if ((holder != null) && (holder.getWeightedTcProvider() != null)) {
				if (lowerWeightedProviderList.contains(holder
							.getWeightedTcProvider())) {
					unregisterService(name);
					synchronizedLockableMGraphCache.remove(name);
				} else {
					continue;
				}
			}
			ServiceRegistration serviceReg = registerAsService(name,
					newProvider.getTriples(name));
			if (serviceReg != null) {
				synchronizedLockableMGraphCache.put(name,
						new GraphHolder(newProvider, null, serviceReg));
			}
		}
	}

	private void unregisterService(UriRef name) {
		GraphHolder entry = synchronizedLockableMGraphCache.get(name);
		if (entry != null) {
			ServiceRegistration reg = entry.getServiceRegistration();
			if (reg != null) {
				reg.unregister();
			}
		}
	}

	private ServiceRegistration registerAsService(UriRef name,
			TripleCollection triples) {
		if (componentContext == null) {
			return null;
		}
		Dictionary props = new Properties();
		props.put("name", name.getUnicodeString());
		String[] interfaceNames;
		Object service;
		if (triples instanceof MGraph) {
			interfaceNames = new String[]{
				MGraph.class.getName(),
				LockableMGraph.class.getName()
			};
			service = new MGraphServiceFactory(this, name);
		} else if (triples instanceof Graph) {
			interfaceNames = new String[]{Graph.class.getName()};
			service = new GraphServiceFactory(this, name);
		} else {
			return null;
		}
		return componentContext.getBundleContext().registerService(
				interfaceNames, service, props);
	}


	private Set<WeightedTcProvider> getLowerWeightedProvider(
			WeightedTcProvider newProvider) {
		boolean referenceProviderPassed = false;
		Set<WeightedTcProvider> lowerWeightedProviderList = new HashSet<WeightedTcProvider>();
		for (WeightedTcProvider weightedProvider : providerList) {
			if (referenceProviderPassed) {
				lowerWeightedProviderList.add(weightedProvider);
			} else if (newProvider.equals(weightedProvider)) {
				referenceProviderPassed = true;
			}
		}
		return lowerWeightedProviderList;
	}

	private void weightedProviderRemoved(WeightedTcProvider oldProvider,
			Set<UriRef> oldProvidedUris) {
		for (UriRef name : oldProvidedUris) {
			final GraphHolder holder = synchronizedLockableMGraphCache
					.get(name);
			if ((holder != null) && (holder.getWeightedTcProvider() != null)
					&& holder.getWeightedTcProvider().equals(oldProvider)) {
				unregisterService(name);
				synchronizedLockableMGraphCache.remove(name);
				
				// check if another WeightedTcProvider has the TripleCollection.
				// And if so register as service.
				for (WeightedTcProvider provider : providerList) {
					try {
						TripleCollection triples = provider.getTriples(name);
						synchronizedLockableMGraphCache.put(name, new GraphHolder(
								provider, null, registerAsService(name, triples)));
						break;
					} catch (NoSuchEntityException e) {
						// continue;
					}
				}

			}
		}
	}

	@Override
	public Graph getGraph(UriRef name) throws NoSuchEntityException {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(new TcPermission(name.getUnicodeString(),
					"read"));
		}
		for (TcProvider provider : providerList) {
			try {
				return provider.getGraph(name);
			} catch (NoSuchEntityException e) {
				//we do nothing and try our luck with the next provider
			} catch (IllegalArgumentException e) {
				//we do nothing and try our luck with the next provider
			}
		}
		throw new NoSuchEntityException(name);
	}

	@Override
	public LockableMGraph getMGraph(UriRef name) {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			try {
				security.checkPermission(new TcPermission(name
						.getUnicodeString(), "readwrite"));
			} catch (AccessControlException e) {
				security.checkPermission(new TcPermission(name
						.getUnicodeString(), "read"));
				return new WriteBlockedMGraph(getUnsecuredMGraph(name));
			}
		}
		return getUnsecuredMGraph(name);
	}

	private LockableMGraph getUnsecuredMGraph(UriRef name)
			throws NoSuchEntityException {
		LockableMGraph result = getMGraphFromCache(name);
		if (result == null) {
			synchronized (this) {
				result = getMGraphFromCache(name);
				if (result == null) {
					result = getUnsecuredMGraphAndAddToCache(name);
				}
			}
		}
		return result;
	}

	private LockableMGraph getMGraphFromCache(UriRef name) {
		GraphHolder holder = synchronizedLockableMGraphCache.get(name);
		if (holder == null) {
			return null;
		}
		return holder.getMGraph();
	}

	private LockableMGraph getUnsecuredMGraphAndAddToCache(UriRef name)
			throws NoSuchEntityException {
		for (WeightedTcProvider provider : providerList) {
			try {
				MGraph providedMGraph = provider.getMGraph(name);
				LockableMGraph result;
				if (providedMGraph instanceof LockableMGraph) {
					result = (LockableMGraph) providedMGraph;
				} else {
					result = new LockableMGraphWrapper(providedMGraph);
				}
				
				GraphHolder holder = synchronizedLockableMGraphCache.get(name);
				ServiceRegistration serviceReg = null;
				if (holder != null) {
					serviceReg = holder.getServiceRegistration();
				}
				synchronizedLockableMGraphCache.put(name, new GraphHolder(
						provider, result, serviceReg));
				return result;
			} catch (NoSuchEntityException e) {
				//we do nothing and try our luck with the next provider
			} catch (IllegalArgumentException e) {
				//we do nothing and try our luck with the next provider
			}
		}
		throw new NoSuchEntityException(name);
	}

	@Override
	public TripleCollection getTriples(UriRef name) {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			try {
				security.checkPermission(new TcPermission(name
						.getUnicodeString(), "readwrite"));
			} catch (AccessControlException e) {
				security.checkPermission(new TcPermission(name
						.getUnicodeString(), "read"));
				return new WriteBlockedTripleCollection(
						getUnsecuredTriples(name));
			}
		}
		return getUnsecuredTriples(name);
	}

	private TripleCollection getUnsecuredTriples(UriRef name)
			throws NoSuchEntityException {
		TripleCollection result;
		for (WeightedTcProvider provider : providerList) {
			try {
				result = provider.getTriples(name);
				if (!(result instanceof MGraph)) {
					return result;
				} else {
					// This is to ensure the MGraph gets added to the cache
					return getUnsecuredMGraph(name);
				}
			} catch (NoSuchEntityException e) {
				//we do nothing and try our luck with the next provider
			} catch (IllegalArgumentException e) {
				//we do nothing and try our luck with the next provider
			}
		}
		throw new NoSuchEntityException(name);
	}

	@Override
	public LockableMGraph createMGraph(UriRef name)
			throws UnsupportedOperationException {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(new TcPermission(name.getUnicodeString(),
					"readwrite"));
		}
		for (WeightedTcProvider provider : providerList) {
			try {
				MGraph providedMGraph = provider.createMGraph(name);
				LockableMGraph result;
				if (providedMGraph instanceof LockableMGraph) {
					result = (LockableMGraph) providedMGraph;
				} else {
					result = new LockableMGraphWrapper(providedMGraph);
				}

				// unregisters a possible Graph or MGraph service under this name
				// provided by a WeightedTcProvider with a lower weight.
				unregisterService(name);
			    
				ServiceRegistration newReg = registerAsService(name, result);
				synchronizedLockableMGraphCache.put(name, new GraphHolder(
						provider, result, newReg));
				return result;
			} catch (UnsupportedOperationException e) {
				//we do nothing and try our luck with the next provider
			} catch (IllegalArgumentException e) {
				//we do nothing and try our luck with the next provider
			}
		}
		throw new UnsupportedOperationException(
				"No provider could create MGraph.");
	}

	@Override
	public Graph createGraph(UriRef name, TripleCollection triples) {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(new TcPermission(name.getUnicodeString(),
					"readwrite"));
		}
		for (WeightedTcProvider provider : providerList) {
			try {
				Graph result = provider.createGraph(name, triples);

				// unregisters a possible Graph or MGraph service under this name
				// provided by a WeightedTcProvider with a lower weight.
				unregisterService(name);
			    
				ServiceRegistration newReg = registerAsService(name, result);
				synchronizedLockableMGraphCache.put(name, new GraphHolder(
						provider, null, newReg));
				return result;
			} catch (UnsupportedOperationException e) {
				//we do nothing and try our luck with the next provider
			} catch (IllegalArgumentException e) {
				//we do nothing and try our luck with the next provider
			}
		}
		throw new UnsupportedOperationException(
				"No provider could create Graph.");
	}

	@Override
	public void deleteTripleCollection(UriRef name) {
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkPermission(new TcPermission(name.getUnicodeString(),
					"readwrite"));
		}
		for (TcProvider provider : providerList) {
			try {
				provider.deleteTripleCollection(name);
				final GraphHolder holder = synchronizedLockableMGraphCache
						.get(name);
				if ((holder != null)
						&& (holder.getWeightedTcProvider() != null)
						&& holder.getWeightedTcProvider().equals(provider)) {
					unregisterService(name);
					synchronizedLockableMGraphCache.remove(name);
				}
				return;
			} catch (UnsupportedOperationException e) {
				// we do nothing and try our luck with the next provider
			} catch (NoSuchEntityException e) {
				//we do nothing and try our luck with the next provider
			} catch (IllegalArgumentException e) {
				//we do nothing and try our luck with the next provider
			}
		}
		// this throws a NoSuchEntityException if the graph doesn't exist
		getTriples(name);
		// the entity exists but cannot be deleted
		throw new UnsupportedOperationException(
				"No provider could delete the entity.");
	}

	@Override
	public Set<UriRef> getNames(Graph graph) {
		Set<UriRef> result = new HashSet<UriRef>();
		for (TcProvider provider : providerList) {
			result.addAll(provider.getNames(graph));
		}
		return result;
	}

	@Override
	public Set<UriRef> listGraphs() {
		Set<UriRef> result = new HashSet<UriRef>();
		for (TcProvider provider : providerList) {
			result.addAll(provider.listGraphs());
		}
		return excludeNonReadable(result);
	}

	@Override
	public Set<UriRef> listMGraphs() {
		Set<UriRef> result = new HashSet<UriRef>();
		for (TcProvider provider : providerList) {
			result.addAll(provider.listMGraphs());
		}
		return excludeNonReadable(result);
	}

	@Override
	public Set<UriRef> listTripleCollections() {
		Set<UriRef> result = new HashSet<UriRef>();
		for (TcProvider provider : providerList) {
			result.addAll(provider.listTripleCollections());
		}
		return excludeNonReadable(result);
	}

	private Set<UriRef> excludeNonReadable(Set<UriRef> tcNames) {
		SecurityManager security = System.getSecurityManager();
		if (security == null) {
			return tcNames;
		}
		Set<UriRef> result = new HashSet<UriRef>();
		for (UriRef name : tcNames) {
			try {
				security.checkPermission(new TcPermission(name
						.getUnicodeString(), "read"));
			} catch (AccessControlException e) {
				continue;
			}
			result.add(name);
		}
		return result;
	}

	/**
	 * Compares the WeightedTcManagementProviders, descending for weight and
	 * ascending by name
	 */
	static class WeightedProviderComparator implements
			Comparator<WeightedTcProvider> {

		@Override
		public int compare(WeightedTcProvider o1, WeightedTcProvider o2) {
			int o1Weight = o1.getWeight();
			int o2Weight = o2.getWeight();
			if (o1Weight != o2Weight) {
				return o2Weight - o1Weight;
			}
			return o1.getClass().toString().compareTo(o2.getClass().toString());
		}
	}

	/**
	 * Executes any sparql query. The type of the result object will vary
	 * depending on the type of the query.
	 * 
	 * @param query
	 *            the sparql query to execute
	 * @param defaultGraph
	 *            the default graph against which to execute the query if not
	 *            FROM clause is present
	 * @return the resulting ResultSet, Graph or Boolean value
	 */
	public Object executeSparqlQuery(Query query, TripleCollection defaultGraph) {
		final QueryEngine queryEngine = this.queryEngine;
		if (queryEngine != null) {
			return queryEngine.execute(this, defaultGraph, query);
		} else {
			throw new NoQueryEngineException();
		}
	}

	/**
	 * Executes a sparql SELECT query.
	 * 
	 * @param query
	 *            the sparql SELECT query to execute
	 * @param defaultGraph
	 *            the default graph against which to execute the query if not
	 *            FROM clause is present
	 * @return the resulting ResultSet
	 */
	public ResultSet executeSparqlQuery(SelectQuery query,
			TripleCollection defaultGraph) {
		final QueryEngine queryEngine = this.queryEngine;
		if (queryEngine != null) {
			return (ResultSet) queryEngine.execute(this, defaultGraph, query);
		} else {
			throw new NoQueryEngineException();
		}
	}

	/**
	 * Executes a sparql ASK query.
	 * 
	 * @param query
	 *            the sparql ASK query to execute
	 * @param defaultGraph
	 *            the default graph against which to execute the query if not
	 *            FROM clause is present
	 * @return the boolean value this query evaluates to
	 */
	public boolean executeSparqlQuery(AskQuery query,
			TripleCollection defaultGraph) {
		final QueryEngine queryEngine = this.queryEngine;
		if (queryEngine != null) {
			return (Boolean) queryEngine.execute(this, defaultGraph, query);
		} else {
			throw new NoQueryEngineException();
		}
	}

	/**
	 * Executes a sparql DESCRIBE query.
	 * 
	 * @param query
	 *            the sparql DESCRIBE query to execute
	 * @param defaultGraph
	 *            the default graph against which to execute the query if not
	 *            FROM clause is present
	 * @return the resulting Graph
	 */
	public Graph executeSparqlQuery(DescribeQuery query,
			TripleCollection defaultGraph) {
		final QueryEngine queryEngine = this.queryEngine;
		if (queryEngine != null) {
			return (Graph) queryEngine.execute(this, defaultGraph, query);
		} else {
			throw new NoQueryEngineException();
		}
	}

	/**
	 * Executes a sparql CONSTRUCT query.
	 * 
	 * @param query
	 *            the sparql CONSTRUCT query to execute
	 * @param defaultGraph
	 *            the default graph against which to execute the query if not
	 *            FROM clause is present
	 * @return the resulting Graph
	 */
	public Graph executeSparqlQuery(ConstructQuery query,
			TripleCollection defaultGraph) {
		final QueryEngine queryEngine = this.queryEngine;
		if (queryEngine != null) {
			return (Graph) queryEngine.execute(this, defaultGraph, query);
		} else {
			throw new NoQueryEngineException();
		}
	}

	/**
	 * Contains an unsecured LockableMGraph, a ServiceRegistration and
	 * the WeightedTcProvider that generated the graph
	 */
	private static class GraphHolder {
		private WeightedTcProvider tcProvider;
		private WeakReference<LockableMGraph> mGraphReference;
		private ServiceRegistration serviceReg;

		private GraphHolder(WeightedTcProvider tcProvider, LockableMGraph mGraph,
				ServiceRegistration serviceReg) {
			this.tcProvider = tcProvider;
			this.mGraphReference = new WeakReference<LockableMGraph>(mGraph);
			this.serviceReg = serviceReg;
		}

		private LockableMGraph getMGraph() {
			return this.mGraphReference.get();
		}

		private WeightedTcProvider getWeightedTcProvider() {
			return this.tcProvider;
		}

		private ServiceRegistration getServiceRegistration() {
			return serviceReg;
		}

	}

}
