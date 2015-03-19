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

import java.security.AccessControlException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.rdf.ImmutableGraph;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.security.TcAccessController;
import org.apache.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.core.impl.WriteBlockedGraph;
import org.apache.clerezza.rdf.core.sparql.NoQueryEngineException;
import org.apache.clerezza.rdf.core.sparql.ParseException;
import org.apache.clerezza.rdf.core.sparql.QueryEngine;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.SparqlPreParser;
import org.apache.clerezza.rdf.core.sparql.query.AskQuery;
import org.apache.clerezza.rdf.core.sparql.query.ConstructQuery;
import org.apache.clerezza.rdf.core.sparql.query.DescribeQuery;
import org.apache.clerezza.rdf.core.sparql.query.Query;
import org.apache.clerezza.rdf.core.sparql.query.SelectQuery;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * This class implements
 * <code>TcManager</code>, delegating the actual provision and creation of
 * Graphs or MGraphs to registered
 * <code>TcProvider</code>s. The class attempts to satisfy the request using the
 * register
 * <code>WeightedTcProvider</code> in decreasing order of weight. If multiple
 * providers have the same weight the lexicographical order of the fully
 * qualified class name determines which one is used, namely the one that occurs
 * earlier. If a call to a registered provider causes an
 * <code>IllegalArgumentException</code>,
 * <code>NoSuchEntityException</code> or
 * <code>UnsupportedOperationException</code> then the call is delegated to the
 * next provider.
 *
 * Only one instance of this class should exist in a system, the public no
 * argument constructor is meant for initialization by dependency injection
 * systems such as OSGi-DS. Applications should use the static
 * <code>getInstance()</code> method if they aren't using a framework that
 * injects them the instance.
 *
 * This class returns
 * <code>Graph</code>s a subtype of
 * <code>Graph</code> that allows read/write locks.
 *
 * This class also registers all Graphs as services with the property
 * 'name' indicating there name.
 *
 * Security checks are done when a Graph is retrieved. The returned
 * Graph will do no further security checks. Because of this it
 * should not be passed to a context where different access control applies. If
 * an Graph is retrieved without having write permission the returned graph
 * will be read-only.
 *
 * If a Graphs needs to passed around across different security
 * contexts the one retrieved from the OSGi service whiteboard should be used as
 * this performs access control on every access.
 *
 * @author reto, mir, hasan
 *
 */
//immedia is set to true as this should register the ImmutableGraph services (even if manager service is not required)
@Component(service = TcManager.class, immediate = true)
@Properties({
	@Property(name = TcManager.MGRAPH_CACHE_ENABLED, boolValue = true, description = "Enable caching mgraphs."),
	@Property(name = TcManager.Graph_SERVICES_ENABLED, boolValue = true, description = "Register triple collections as services.") })
public class TcManager extends TcProviderMultiplexer {

    public final static String GENERAL_PURPOSE_TC = "general.purpose.tc";
    public final static String Graph_SERVICES_ENABLED = "Graph.services.enabled";
    public final static String MGRAPH_CACHE_ENABLED = "graph.cache.enabled";

    private static volatile TcManager instance;
    private TcAccessController tcAccessController = new TcAccessController() {

        @Override
        protected TcManager getTcManager() {
            return TcManager.this;
        }
            
    };
    private Map<Iri, ServiceRegistration> serviceRegistrations = Collections
            .synchronizedMap(new HashMap<Iri, ServiceRegistration>());
    
    protected QueryEngine queryEngine;
    private boolean isActivated = false;
    private boolean isTcServicesEnabled = true;

    private ComponentContext componentContext;
    protected SortedSet<WeightedTcProvider> tempProviderList = new TreeSet<WeightedTcProvider>(
            new WeightedProviderComparator());

    /**
     * the constructor sets the singleton instance to allow instantiation by
     * OSGi-DS. This constructor should not be called except by OSGi-DS,
     * otherwise the static
     * <code>getInstance</code> method should be used.
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
                    instance = new TcManager();
                    instance.isActivated = true;
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
        
        // Read configuration
		isTcServicesEnabled = true;
		Object configTcServicesEnabled = componentContext.getProperties().get(Graph_SERVICES_ENABLED);
		if ( configTcServicesEnabled != null && configTcServicesEnabled instanceof String ) {
			isTcServicesEnabled = Boolean.valueOf((String)configTcServicesEnabled);				
		}
		Object configCacheEnabled = componentContext.getProperties().get(MGRAPH_CACHE_ENABLED);
		if ( configCacheEnabled != null && configCacheEnabled instanceof String ) {
			setCachingEnabled(Boolean.valueOf((String)configCacheEnabled));				
		}
		isActivated = true;
		
        for (WeightedTcProvider provider : tempProviderList) {
        	addWeightedTcProvider(provider);
        }
        tempProviderList.clear();
    }

    protected void deactivate(final ComponentContext componentContext) {
        for (ServiceRegistration registration : serviceRegistrations.values()) {
            registration.unregister();
        }
        serviceRegistrations.clear();
        this.componentContext = null;
		isActivated = false;
    }

    @Override
    public ImmutableGraph getGraph(Iri name) throws NoSuchEntityException {
        tcAccessController.checkReadPermission(name);
        return super.getGraph(name);
    }

    @Override
    public Graph getMGraph(Iri name) {
        try {
            tcAccessController.checkReadWritePermission(name);
        } catch (AccessControlException e) {
            tcAccessController.checkReadPermission(name);
            return new WriteBlockedGraph(super.getMGraph(name));
        }
        return super.getMGraph(name);
    }

    @Override
    public Graph getTriples(Iri name) {
        try {
            tcAccessController.checkReadWritePermission(name);
        } catch (AccessControlException e) {
            tcAccessController.checkReadPermission(name);
            return new WriteBlockedGraph(
                    super.getTriples(name));
        }
        return super.getTriples(name);
    }

    @Override
    public Graph createMGraph(Iri name)
            throws UnsupportedOperationException {
        tcAccessController.checkReadWritePermission(name);
        return super.createMGraph(name);
    }

    @Override
    public ImmutableGraph createGraph(Iri name, Graph triples) {
        tcAccessController.checkReadWritePermission(name);
        return super.createGraph(name, triples);
    }

    @Override
    public void deleteGraph(Iri name) {
        tcAccessController.checkReadWritePermission(name);
        super.deleteGraph(name);
    }

    @Override
    public Set<Iri> getNames(ImmutableGraph ImmutableGraph) {
        return super.getNames(ImmutableGraph);
    }

    @Override
    public Set<Iri> listGraphs() {
        Set<Iri> result = super.listGraphs();
        return excludeNonReadable(result);
    }

    @Override
    public Set<Iri> listMGraphs() {
        Set<Iri> result = super.listMGraphs();
        return excludeNonReadable(result);
    }

    @Override
    public Set<Iri> listImmutableGraphs() {
        Set<Iri> result = super.listImmutableGraphs();
        return excludeNonReadable(result);
    }

    private Set<Iri> excludeNonReadable(Set<Iri> tcNames) {
        SecurityManager security = System.getSecurityManager();
        if (security == null) {
            return tcNames;
        }
        Set<Iri> result = new HashSet<Iri>();
        for (Iri name : tcNames) {
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
     * depending on the type of the query. If the defaultGraph is available
     * in this TcManages executeSparqlQuery(String, UriRef) should be used instead.
     *
     * @param query the sparql query to execute
     * @param defaultGraph the default ImmutableGraph against which to execute the query
     * if no FROM clause is present
     * @return the resulting ResultSet, ImmutableGraph or Boolean value
     */
    public Object executeSparqlQuery(String query, Graph defaultGraph) throws ParseException {
        TcProvider singleTargetTcProvider = null;

        final Iri defaultGraphName = new Iri("urn:x-temp:/kjsfadfhfasdffds");
        final SparqlPreParser sparqlPreParser = new SparqlPreParser(this);
        final Set<Iri> referencedGraphs = sparqlPreParser.getReferredGraphs(query, defaultGraphName);
        if ((referencedGraphs != null) && (!referencedGraphs.contains(defaultGraphName))) {
            singleTargetTcProvider = getSingleTargetTcProvider(referencedGraphs);
        }
        if ((singleTargetTcProvider != null) && (singleTargetTcProvider instanceof QueryableTcProvider)) {
            return ((QueryableTcProvider) singleTargetTcProvider).executeSparqlQuery(query, null);
        }
        final QueryEngine queryEngine = this.queryEngine;
        if (queryEngine != null) {
            return queryEngine.execute(this, defaultGraph, query);
        } else {
            throw new NoQueryEngineException();
        }
    }

    /**
     * Executes any sparql query. The type of the result object will vary
     * depending on the type of the query. Note that this method only works for
     * queries that do not need a default ImmutableGraph.
     *
     * @param query the sparql query to execute
     * @param forceFastlane indicate whether to force fastlane usage.
     * @return the resulting ResultSet, ImmutableGraph or Boolean value
     */
    public Object executeSparqlQuery(String query, boolean forceFastlane) throws ParseException {
        TcProvider singleTargetTcProvider = null;
    	if (forceFastlane) {
            singleTargetTcProvider = getSingleTargetTcProvider(Collections.EMPTY_SET);
    	} else {    	
	        final Iri defaultGraphName = new Iri("urn:x-temp:/kjsfadfhfasdffds");
	        SparqlPreParser sparqlPreParser = new SparqlPreParser(this);
	        final Set<Iri> referencedGraphs = sparqlPreParser.getReferredGraphs(query, defaultGraphName);
	        if ((referencedGraphs != null) && (!referencedGraphs.contains(defaultGraphName))) {
	            singleTargetTcProvider = getSingleTargetTcProvider(referencedGraphs);
	        }
    	}
    	
        if ((singleTargetTcProvider != null) && (singleTargetTcProvider instanceof QueryableTcProvider)) {
            return ((QueryableTcProvider)singleTargetTcProvider).executeSparqlQuery(query, null);
        }
        final QueryEngine queryEngine = this.queryEngine;
        if (queryEngine != null) {
            return queryEngine.execute(this, new SimpleGraph(), query);
        } else {
            throw new NoQueryEngineException();
        }
    }
    
    /**
     * Executes any sparql query. The type of the result object will vary
     * depending on the type of the query. If the defaultGraph is available
     * in this TcManages executeSparqlQuery(String, UriRef) should be used instead.
     *
     * @param query the sparql query to execute
     * @param defaultGraphName the ImmutableGraph to be used as default ImmutableGraph in the Sparql ImmutableGraph Store
     * @return the resulting ResultSet, ImmutableGraph or Boolean value
     */
    public Object executeSparqlQuery(String query, Iri defaultGraphName) throws ParseException {
      return executeSparqlQuery(query, defaultGraphName, false);
    }

    /**
     * Executes any sparql query. The type of the result object will vary
     * depending on the type of the query. If the defaultGraph is available
     * in this TcManages executeSparqlQuery(String, UriRef) should be used instead.
     *
     * @param query the sparql query to execute
     * @param defaultGraph the ImmutableGraph to be used as default ImmutableGraph in the Sparql ImmutableGraph Store
     * @param forceFastlane indicate whether to force fastlane usage.
     * @return the resulting ResultSet, ImmutableGraph or Boolean value
     */
    public Object executeSparqlQuery(String query, Iri defaultGraphName, boolean forceFastlane) throws ParseException {
        TcProvider singleTargetTcProvider = null;
    	if (forceFastlane) {
            singleTargetTcProvider = getSingleTargetTcProvider(Collections.singleton(defaultGraphName));
    	} else {    	
	        SparqlPreParser sparqlPreParser = new SparqlPreParser(this);
	        final Set<Iri> referencedGraphs = sparqlPreParser.getReferredGraphs(query, defaultGraphName);
	        if ((referencedGraphs != null)) {
	            singleTargetTcProvider = getSingleTargetTcProvider(referencedGraphs);
	        }
    	}
        if ((singleTargetTcProvider != null) && (singleTargetTcProvider instanceof QueryableTcProvider)) {
            return ((QueryableTcProvider)singleTargetTcProvider).executeSparqlQuery(query, defaultGraphName);
        }
        final QueryEngine queryEngine = this.queryEngine;
        if (queryEngine != null) {
            return queryEngine.execute(this, this.getTriples(defaultGraphName), query);
        } else {
            throw new NoQueryEngineException();
        }
    }

    /**
     * Executes any sparql query. The type of the result object will vary
     * depending on the type of the query.
     *
     * @param query the sparql query to execute
     * @param defaultGraph the default ImmutableGraph against which to execute the query
     * if no FROM clause is present
     * @return the resulting ResultSet, ImmutableGraph or Boolean value
     *
     * @deprecated Query is discontinued
     */
    @Deprecated
    public Object executeSparqlQuery(Query query, Graph defaultGraph) {
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
     * @param query the sparql SELECT query to execute
     * @param defaultGraph the default ImmutableGraph against which to execute the query
     * if not FROM clause is present
     * @return the resulting ResultSet
     * @deprecated Query is discontinued
     */
    @Deprecated
    public ResultSet executeSparqlQuery(SelectQuery query,
            Graph defaultGraph) {
        return (ResultSet) executeSparqlQuery((Query) query, defaultGraph);
    }

    /**
     * Executes a sparql ASK query.
     *
     * @param query the sparql ASK query to execute
     * @param defaultGraph the default ImmutableGraph against which to execute the query
     * if not FROM clause is present
     * @return the boolean value this query evaluates to
     * @deprecated Query is discontinued
     */
    @Deprecated
    public boolean executeSparqlQuery(AskQuery query,
            Graph defaultGraph) {
        return (Boolean) executeSparqlQuery((Query) query, defaultGraph);
    }

    /**
     * Executes a sparql DESCRIBE query.
     *
     * @param query the sparql DESCRIBE query to execute
     * @param defaultGraph the default ImmutableGraph against which to execute the query
     * if not FROM clause is present
     * @return the resulting ImmutableGraph
     * @deprecated Query is discontinued
     */
    @Deprecated
    public ImmutableGraph executeSparqlQuery(DescribeQuery query,
            Graph defaultGraph) {
        return (ImmutableGraph) executeSparqlQuery((Query) query, defaultGraph);
    }

    /**
     * Executes a sparql CONSTRUCT query.
     *
     * @param query the sparql CONSTRUCT query to execute
     * @param defaultGraph the default ImmutableGraph against which to execute the query
     * if not FROM clause is present
     * @return the resulting ImmutableGraph
     * @deprecated Query is discontinued
     */
    @Deprecated
    public ImmutableGraph executeSparqlQuery(ConstructQuery query,
            Graph defaultGraph) {
        return (ImmutableGraph) executeSparqlQuery((Query) query, defaultGraph);
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
     * @param provider the provider to be registered
     */
    @Reference(policy = ReferencePolicy.DYNAMIC, 
            cardinality = ReferenceCardinality.MULTIPLE)
    protected void bindWeightedTcProvider(WeightedTcProvider provider) {
    	if (isActivated) {
    		addWeightedTcProvider(provider);
    	} else {
    		tempProviderList.add(provider);  		
    	}
    }

    /**
     * Unregister a provider
     *
     * @param provider the provider to be deregistered
     */
    protected void unbindWeightedTcProvider(
            WeightedTcProvider provider) {
        removeWeightedTcProvider(provider);
    }
    
    /**
     * Registers a provider
     *
     * @param provider the provider to be registered
     */
    @Reference(policy = ReferencePolicy.DYNAMIC, 
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            target = "("+TcManager.GENERAL_PURPOSE_TC+"=true)")
    protected void bindGpWeightedTcProvider(WeightedTcProvider provider) {
    	if (isActivated) {
    		addWeightedTcProvider(provider);
    	} else {
    		tempProviderList.add(provider);  		
    	}
    }

    /**
     * Unregister a provider
     *
     * @param provider the provider to be deregistered
     */
    protected void unbindGpWeightedTcProvider(
            WeightedTcProvider provider) {
        removeWeightedTcProvider(provider);
    }
    
    @Reference(policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.OPTIONAL)
    protected void bindQueryEngine(QueryEngine queryEngine) {
        this.queryEngine = queryEngine;
    }
    
    protected void unbindQueryEngine(QueryEngine queryEngine) {
        this.queryEngine = null;
    }

    @Override
    protected void mGraphAppears(Iri name) {
    	if (isTcServicesEnabled()) {
    		// Only create the service when activated. When not activated
    		// creating will be delayed till after activation.
	        if (componentContext != null) {
	            registerGraphAsService(name, true);
	        }
    	}
    }

    @Override
    protected void graphAppears(Iri name) {
    	if (isTcServicesEnabled()) {
    		// Only create the service when activated. When not activated
    		// creating will be delayed till after activation.
	        if (componentContext != null) {
	            registerGraphAsService(name, false);
	        }
    	}
    }

    private void registerGraphAsService(Iri name, boolean isMGraph) {
        Dictionary<String,Object> props = new Hashtable<String, Object>();
        props.put("name", name.getUnicodeString());
        String[] interfaceNames;
        Object service;
        if (isMGraph) {
            interfaceNames = new String[]{
                Graph.class.getName(),
                Graph.class.getName()
            };
            service = new MGraphServiceFactory(this, name, tcAccessController);
        } else {
            interfaceNames = new String[]{ImmutableGraph.class.getName()};
            service = new ImmutableGraphServiceFactory(this, name, tcAccessController);
        }
        final int bundleState = componentContext.getBundleContext().getBundle().getState();
        if ((bundleState == Bundle.ACTIVE) || (bundleState == Bundle.STARTING)) {
            ServiceRegistration serviceReg = componentContext.getBundleContext()
                    .registerService(interfaceNames, service, props);
            serviceRegistrations.put(name, serviceReg);
        }
    }

    @Override
    protected void tcDisappears(Iri name) {
        ServiceRegistration reg = serviceRegistrations.get(name);
        if (reg != null) {
            reg.unregister();
            serviceRegistrations.remove(name);
        }
    }

    private TcProvider getSingleTargetTcProvider(final Set<Iri> referencedGraphs) {
        TcProvider singleTargetTcProvider = null;
        for (WeightedTcProvider provider : providerList) {
            final Set<Iri> providerGraphs = provider.listGraphs();
            if (providerGraphs.containsAll(referencedGraphs)) {
               singleTargetTcProvider = provider;
               break; //success
            }
            for (Iri graphName : referencedGraphs) {
                if (providerGraphs.contains(graphName)) {
                    break; //failure
                }
            }      
        }
        return singleTargetTcProvider;
    }

    public boolean isTcServicesEnabled() {
		return isTcServicesEnabled;
	}
}
