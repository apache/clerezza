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

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.sparql.QueryEngine;

/**
 * This makes a set of WeightedTcProvider appear as one TcProvider. It delegates
 * requests to the WeightedTcProvider with the highest Weight
 *
 * @author reto
 */
public class TcProviderMultiplexer implements TcProvider {

    protected SortedSet<WeightedTcProvider> providerList = new TreeSet<WeightedTcProvider>(
            new WeightedProviderComparator());
    /**
     * Mapping to Graph's and ServiceRegistration using their URI's as key.
     * Makes sure that per URI only one instance of the Graph is used,
     * otherwise the locks in the <code>Graph</code>s would have no effect
     * between different instances and concurrency issues could occur.
     */
    private Map<IRI, MGraphHolder> mGraphCache = Collections.synchronizedMap(new HashMap<IRI, MGraphHolder>());

	/**
	 * Flag to indicate whether mgraphs should be cached for faster access. By
	 * default caching is enabled for backward compatibility.
	 */
	private boolean isCachingEnabled = true;

    /**
     * Registers a provider
     *
     * @param provider
     *            the provider to be registered
     */
    public void addWeightedTcProvider(WeightedTcProvider provider) {
        providerList.add(provider);
        updateGraphCache(provider, true);
    }

    /**
     * Unregister a provider
     *
     * @param provider
     *            the provider to be deregistered
     */
    public void removeWeightedTcProvider(
            WeightedTcProvider provider) {
        providerList.remove(provider);
        updateGraphCache(provider, false);
    }

    /**
     * subclasses overwrite this method to be notified when a new
     * ImmutableGraph is available (either because it has been created or being
     * provided by a newly added WeightedTcProvider). The default implementation
     * does nothing.
     *
     * @param name
     */
    protected void graphAppears(IRI name) {
    }

    /**
     * subclasses overwrite this method to be notified when a new
     * Graph is available (either because it has been created or being
     * provided by a newly added WeightedTcProvider). The default implementation
     * does nothing.
     *
     * @param name
     */
    protected void mGraphAppears(IRI name) {
    }

    /**
     * subclasses overwrite this method to be notified whenGraph is 
     * no longer available (either because it has been deleted or bacause its
     * WeightedTcProvider was removed). The default implementation does nothing.
     *
     * for implementational reasons even for name of Graph not
     * previously registered.
     *
     * @param name
     */
    protected void tcDisappears(IRI name) {
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
    private void updateGraphCache(WeightedTcProvider provider,
            boolean providerAdded) {
        Set<IRI> uriSet = provider.listGraphs();
        if (!(uriSet == null || uriSet.isEmpty())) {
            if (providerAdded) {
                weightedProviderAdded(provider, uriSet);
            } else {
                weightedProviderRemoved(provider, uriSet);
            }
        }
    }

    private void weightedProviderAdded(WeightedTcProvider newProvider,
            Set<IRI> newProvidedUris) {
        Set<WeightedTcProvider> lowerWeightedProviderList = getLowerWeightedProvider(newProvider);
    	if (isCachingEnabled()) {
	        for (IRI name : newProvidedUris) {
	            final MGraphHolder holder = mGraphCache.get(name);
	            if ((holder != null) && (holder.getWeightedTcProvider() != null)) {
	                if (lowerWeightedProviderList.contains(holder.getWeightedTcProvider())) {
	                    tcDisappears(name);
	                    mGraphCache.remove(name);
	                } else {
	                    continue;
	                }
	            }
	            Graph triples = newProvider.getGraph(name);
	            if (triples instanceof Graph) {
	           		mGraphCache.put(name, new MGraphHolder(newProvider, ensureLockable((Graph)triples)));
	                mGraphAppears(name);
	            } else {
	                graphAppears(name);
	            }
	    	}
        }
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
            Set<IRI> oldProvidedUris) {
        for (IRI name : oldProvidedUris) {
            final MGraphHolder holder = mGraphCache.get(name);
            if ((holder != null) && (holder.getWeightedTcProvider() != null)
                    && holder.getWeightedTcProvider().equals(oldProvider)) {
                tcDisappears(name);
                mGraphCache.remove(name);

            	if (isCachingEnabled()) {
	                // check if another WeightedTcProvider has the Graph.
	                // And if so register as service.
	                for (WeightedTcProvider provider : providerList) {
	                    try {
	                        Graph triples = provider.getGraph(name);
	                        if (triples instanceof Graph) {
	                       		mGraphCache.put(name, new MGraphHolder(provider, ensureLockable((Graph)triples)));
	                            mGraphAppears(name);
	                        } else {
	                            graphAppears(name);
	                        }
	                        break;
	                    } catch (NoSuchEntityException e) {
	                        // continue;
	                    }
	                }
            	}
            }
        }
    }

    @Override
    public ImmutableGraph getImmutableGraph(IRI name) throws NoSuchEntityException {
        for (TcProvider provider : providerList) {
            try {
                return provider.getImmutableGraph(name);
            } catch (NoSuchEntityException e) {
                //we do nothing and try our luck with the next provider
            } catch (IllegalArgumentException e) {
                //we do nothing and try our luck with the next provider
            }
        }
        throw new NoSuchEntityException(name);
    }

    @Override
    public Graph getMGraph(IRI name)
            throws NoSuchEntityException {
        Graph result = getMGraphFromCache(name);
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

    private Graph getMGraphFromCache(IRI name) {
        MGraphHolder holder = mGraphCache.get(name);
        if (holder == null) {
            return null;
        }
        return holder.getMGraph();
    }

    private Graph getUnsecuredMGraphAndAddToCache(IRI name)
            throws NoSuchEntityException {
        for (WeightedTcProvider provider : providerList) {
            try {
                Graph providedMGraph = provider.getMGraph(name);
                Graph result = ensureLockable(providedMGraph);

                if (isCachingEnabled()) {
	                mGraphCache.put(name, new MGraphHolder(
	                        provider, result));
                }
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
    public Graph getGraph(IRI name)
            throws NoSuchEntityException {
        Graph result;
        for (WeightedTcProvider provider : providerList) {
            try {
                result = provider.getGraph(name);
                if (result instanceof ImmutableGraph) {
                    return result;
                } else {
                    // This is to ensure the Graph gets added to the cache
                    return getMGraph(name);
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
    public Graph createGraph(IRI name)
            throws UnsupportedOperationException {

        for (WeightedTcProvider provider : providerList) {
            try {
                Graph result = provider.createGraph(name);
                // unregisters a possible ImmutableGraph or Graph service under this name
                // provided by a WeightedTcProvider with a lower weight.
                tcDisappears(name);
                if (isCachingEnabled()) {
                	mGraphCache.put(name, new MGraphHolder(provider, null));
                }
                mGraphAppears(name);
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
    public ImmutableGraph createImmutableGraph(IRI name, Graph triples) {
        for (WeightedTcProvider provider : providerList) {
            try {
                ImmutableGraph result = provider.createImmutableGraph(name, triples);

                // unregisters a possible ImmutableGraph or Graph service under this name
                // provided by a WeightedTcProvider with a lower weight.
                tcDisappears(name);
                if (isCachingEnabled()) {
                	mGraphCache.put(name, new MGraphHolder(provider, null));
                }
                graphAppears(name);
                return result;
            } catch (UnsupportedOperationException e) {
                //we do nothing and try our luck with the next provider
            } catch (IllegalArgumentException e) {
                //we do nothing and try our luck with the next provider
            }
        }
        throw new UnsupportedOperationException(
                "No provider could create ImmutableGraph.");
    }

    @Override
    public void deleteGraph(IRI name) {
        for (TcProvider provider : providerList) {
            try {
                provider.deleteGraph(name);
                final MGraphHolder holder = mGraphCache.get(name);
                if ((holder != null)
                        && (holder.getWeightedTcProvider() != null)
                        && holder.getWeightedTcProvider().equals(provider)) {
                    tcDisappears(name);
                    mGraphCache.remove(name);
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
        // this throws a NoSuchEntityException if the ImmutableGraph doesn't exist
        getGraph(name);
        // the entity exists but cannot be deleted
        throw new UnsupportedOperationException(
                "No provider could delete the entity.");
    }

    @Override
    public Set<IRI> getNames(ImmutableGraph ImmutableGraph) {
        Set<IRI> result = new HashSet<IRI>();
        for (TcProvider provider : providerList) {
            result.addAll(provider.getNames(ImmutableGraph));
        }
        return result;
    }

    @Override
    public Set<IRI> listGraphs() {
        Set<IRI> result = new HashSet<IRI>();
        for (TcProvider provider : providerList) {
            result.addAll(provider.listGraphs());
        }
        return result;
    }

    @Override
    public Set<IRI> listMGraphs() {
        Set<IRI> result = new HashSet<IRI>();
        for (TcProvider provider : providerList) {
            result.addAll(provider.listMGraphs());
        }
        return result;
    }

    @Override
    public Set<IRI> listImmutableGraphs() {
        Set<IRI> result = new HashSet<IRI>();
        for (TcProvider provider : providerList) {
            result.addAll(provider.listImmutableGraphs());
        }
        return result;
    }

    private Graph ensureLockable(Graph providedMGraph) {
        //Graphs are alway locable now
        return providedMGraph;
    }

    /**
     * Contains an unsecured Graph, a ServiceRegistration and
     * the WeightedTcProvider that generated the ImmutableGraph
     */
    private static class MGraphHolder {

        private WeightedTcProvider tcProvider;
        private WeakReference<Graph> mGraphReference;

        MGraphHolder(WeightedTcProvider tcProvider, Graph graph) {
            this.tcProvider = tcProvider;
            this.mGraphReference = new WeakReference<Graph>(graph);
        }

        Graph getMGraph() {
            return this.mGraphReference.get();
        }

        WeightedTcProvider getWeightedTcProvider() {
            return this.tcProvider;
        }
    }

    //methods for debuging / monitoring
    public SortedSet<WeightedTcProvider> getProviderList() {
        return providerList;
    }

    public boolean isCachingEnabled() {
		return isCachingEnabled;
	}
    
    public void setCachingEnabled(boolean isCachingEnabled) {
		this.isCachingEnabled = isCachingEnabled;
		
		if (!isCachingEnabled()) {
			mGraphCache.clear();
		}
	}
}
