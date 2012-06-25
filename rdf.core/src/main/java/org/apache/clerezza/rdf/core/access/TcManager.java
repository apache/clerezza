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

import org.apache.clerezza.rdf.core.impl.WriteBlockedMGraph;
import org.apache.clerezza.rdf.core.impl.WriteBlockedTripleCollection;

import java.security.AccessControlException;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.security.TcAccessController;
import org.apache.clerezza.rdf.core.sparql.query.AskQuery;
import org.apache.clerezza.rdf.core.sparql.query.ConstructQuery;
import org.apache.clerezza.rdf.core.sparql.query.DescribeQuery;
import org.apache.clerezza.rdf.core.sparql.NoQueryEngineException;
import org.apache.clerezza.rdf.core.sparql.query.Query;
import org.apache.clerezza.rdf.core.sparql.QueryEngine;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.query.SelectQuery;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

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
 */
@Component
@Service(TcManager.class)
@Reference(name="weightedTcProvider", policy=ReferencePolicy.DYNAMIC,
		referenceInterface=WeightedTcProvider.class,
		cardinality=ReferenceCardinality.MANDATORY_MULTIPLE)
public class TcManager extends TcProviderMultiplexer {

	private static volatile TcManager instance;

	private TcAccessController tcAccessController = new TcAccessController(this);

	private Map<UriRef, ServiceRegistration> serviceRegistrations = Collections
			.synchronizedMap(new HashMap<UriRef, ServiceRegistration>());


	@Reference(policy=ReferencePolicy.DYNAMIC,
			cardinality=ReferenceCardinality.MANDATORY_UNARY)
	protected QueryEngine queryEngine;

	private ComponentContext componentContext;
	private Collection<UriRef> mGraphsToRegisterOnActivation = new HashSet<UriRef>();
	private Collection<UriRef> graphsToRegisterOnActivation = new HashSet<UriRef>();


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
		for (UriRef name : mGraphsToRegisterOnActivation) {
			registerTripleCollectionAsService(name, true);
		}
		for (UriRef name : graphsToRegisterOnActivation) {
			registerTripleCollectionAsService(name, false);
		}
	}

	protected void deactivate(final ComponentContext componentContext) {
		for (ServiceRegistration registration : serviceRegistrations.values()) {
			registration.unregister();
		}
		serviceRegistrations.clear();
		this.componentContext = null;
	}

	@Override
	public Graph getGraph(UriRef name) throws NoSuchEntityException {
		tcAccessController.checkReadPermission(name);
		return super.getGraph(name);
	}

	@Override
	public LockableMGraph getMGraph(UriRef name) {
		try {
			tcAccessController.checkReadWritePermission(name);
		} catch (AccessControlException e) {
			tcAccessController.checkReadPermission(name);
			return new WriteBlockedMGraph(super.getMGraph(name));
		}
		return super.getMGraph(name);
	}

	@Override
	public TripleCollection getTriples(UriRef name) {
		try {
			tcAccessController.checkReadWritePermission(name);
		} catch (AccessControlException e) {
			tcAccessController.checkReadPermission(name);
			return new WriteBlockedTripleCollection(
					super.getTriples(name));
		}
		return super.getTriples(name);
	}

	

	@Override
	public LockableMGraph createMGraph(UriRef name)
			throws UnsupportedOperationException {
		tcAccessController.checkReadWritePermission(name);
		return super.createMGraph(name);
	}

	@Override
	public Graph createGraph(UriRef name, TripleCollection triples) {
		tcAccessController.checkReadWritePermission(name);
		return super.createGraph(name, triples);
	}

	@Override
	public void deleteTripleCollection(UriRef name) {
		tcAccessController.checkReadWritePermission(name);
		super.deleteTripleCollection(name);
	}

	@Override
	public Set<UriRef> getNames(Graph graph) {
		return super.getNames(graph);
	}

	@Override
	public Set<UriRef> listGraphs() {
		Set<UriRef> result = super.listGraphs();
		return excludeNonReadable(result);
	}

	@Override
	public Set<UriRef> listMGraphs() {
		Set<UriRef> result = super.listMGraphs();
		return excludeNonReadable(result);
	}

	@Override
	public Set<UriRef> listTripleCollections() {
		Set<UriRef> result = super.listTripleCollections();
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
				tcAccessController.checkReadPermission(name);
			} catch (AccessControlException e) {
				continue;
			}
			result.add(name);
		}
		return result;
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
	 * @return the TcAccessController that can be used to set the permissions
	 * needed to access a Triple Collection
	 */
	public TcAccessController getTcAccessController() {
		return tcAccessController;
	}

	/**
	 * Registers a provider
	 *
	 * @param provider
	 *            the provider to be registered
	 */
	protected void bindWeightedTcProvider(WeightedTcProvider provider) {
		addWeightedTcProvider(provider);
	}

	/**
	 * Unregister a provider
	 *
	 * @param provider
	 *            the provider to be deregistered
	 */
	protected void unbindWeightedTcProvider(
			WeightedTcProvider provider) {
		removeWeightedTcProvider(provider);
	}

	@Override
	protected void mGraphAppears(UriRef name) {
		if (componentContext == null) {
			mGraphsToRegisterOnActivation.add(name);
		} else {
			registerTripleCollectionAsService(name, true);
		}
	}

	@Override
	protected void graphAppears(UriRef name) {
		if (componentContext == null) {
			graphsToRegisterOnActivation.add(name);
		} else {
			registerTripleCollectionAsService(name, false);
		}
	}

	private void registerTripleCollectionAsService(UriRef name, boolean isMGraph) {
		Dictionary props = new Properties();
		props.put("name", name.getUnicodeString());
		String[] interfaceNames;
		Object service;
		if (isMGraph) {
			interfaceNames = new String[] {
				MGraph.class.getName(),
				LockableMGraph.class.getName()
			};
			service = new MGraphServiceFactory(this, name, tcAccessController);
		} else {
			interfaceNames = new String[] {Graph.class.getName()};
			service = new GraphServiceFactory(this, name, tcAccessController);
		}
		final int bundleState = componentContext.getBundleContext().getBundle().getState();
		if ((bundleState == Bundle.ACTIVE) || (bundleState == Bundle.STARTING)) {
			ServiceRegistration serviceReg = componentContext.getBundleContext()
					.registerService(interfaceNames, service, props);
			serviceRegistrations.put(name, serviceReg);
		}
	}

	@Override
	protected void tcDisappears(UriRef name) {
		mGraphsToRegisterOnActivation.remove(name);
		graphsToRegisterOnActivation.remove(name);
		ServiceRegistration reg = serviceRegistrations.get(name);
		if (reg != null) {
			reg.unregister();
			serviceRegistrations.remove(name);
		}
	}
}
