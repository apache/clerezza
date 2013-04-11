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

package org.apache.clerezza.platform.cris;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.cris.Condition;
import org.apache.clerezza.rdf.cris.FacetCollector;
import org.apache.clerezza.rdf.cris.GraphIndexer;
import org.apache.clerezza.rdf.cris.IndexDefinitionManager;
import org.apache.clerezza.rdf.cris.ResourceFinder;
import org.apache.clerezza.rdf.cris.SortSpecification;
import org.apache.clerezza.rdf.cris.VirtualProperty;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to index and find resources from the content graph.
 * 
 * @author tio
 */
@Component(metatype=true)
@Service(IndexService.class)
public class IndexService extends ResourceFinder {

    @Property(intValue=0, label="The delay in minutes until the first index optimization is invoked.", 
            description="This allows to set the time of the first invocation. 0 Is the lowest acceptable value and means run instantly.")
    static final String OPTIMIZE_DELAY = "org.apache.clerezza.platform.cris.optimizedelay";
    
    @Property(intValue=0, label="The period in minutes between index optimizations.", 
            description="When a new value is set, the first invocation will happen after the specified delay and the old schedule will be canceled instantly. The minimum acceptable value is 1 (min). A value of 0 turns off optimizations.")
    static final String OPTIMIZE_PERIOD = "org.apache.clerezza.platform.cris.optimizeperiod";
    
    private final Logger logger = LoggerFactory.getLogger(getClass());

    IndexDefinitionManager indexDefinitionManager = null;

    GraphIndexer graphIndexer  = null;

    @Reference
    ContentGraphProvider cgProvider;

    @Reference
    TcManager tcManager;

    UriRef definitionGraphUri = new UriRef("http://zz.localhost/cris.definitions.graph");

    LockableMGraph definitionGraph = null;

    Integer optimizePeriod = 0;
    Integer optimizeDelay = 0;
    
    protected void activate(ComponentContext context) {

        optimizePeriod = (Integer) context.getProperties().get(OPTIMIZE_PERIOD);
        optimizeDelay = (Integer) context.getProperties().get(OPTIMIZE_DELAY);
        if(optimizeDelay == null || optimizeDelay < 0) {
            optimizeDelay = 0;
        }
        
        try {
            definitionGraph = tcManager.getMGraph(definitionGraphUri);
        } catch (NoSuchEntityException ex) {
            definitionGraph = tcManager.createMGraph(definitionGraphUri);
        }
        File luceneIndexDir = context.getBundleContext().getDataFile("lucene-index");
        boolean createNewIndex = luceneIndexDir.exists();
        logger.info("Create new index: {}", !createNewIndex);

        indexDefinitionManager = new IndexDefinitionManager(definitionGraph);
        try {
            graphIndexer = new GraphIndexer(definitionGraph, cgProvider.getContentGraph(),
                    FSDirectory.open(luceneIndexDir), !createNewIndex);
            if(optimizePeriod != null && optimizePeriod >= 1) {
                long period = optimizePeriod * 60000;
                long delay = optimizeDelay * 60000;
                logger.info("Scheduling optimizations with delay {} min and period {} min", delay, period);
                graphIndexer.scheduleIndexOptimizations(delay, period);
            }
        } catch (IOException ex) {
            logger.error("Could not open lucene index directory.");
            throw new IllegalStateException(ex);
        }
    }

    protected void deactivate(ComponentContext context) {
        if(optimizePeriod >= 1) {
            graphIndexer.terminateIndexOptimizationSchedule();
        }
        optimizeDelay = 0;
        optimizePeriod = 0;
        graphIndexer.closeLuceneIndex();
        graphIndexer = null;

    }
    
    @Override
    public List<NonLiteral> findResources(List<? extends Condition> conditions, 
            SortSpecification sortSpecification, FacetCollector... facetCollectors) {
        try {
            return graphIndexer.findResources(conditions, sortSpecification, facetCollectors);
        } catch (ParseException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public List<NonLiteral> findResources(List<? extends Condition> conditions, 
            FacetCollector... facetCollectors) {
        try {
            return graphIndexer.findResources(conditions, facetCollectors);
        } catch (ParseException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public List<NonLiteral> findResources(List<? extends Condition> conditions) {
        try {
            return graphIndexer.findResources(conditions);
        } catch (ParseException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public List<NonLiteral> findResources(UriRef uriRef, String pattern) {
        try {
            return graphIndexer.findResources(uriRef, pattern);
        } catch (ParseException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public List<NonLiteral> findResources(UriRef uriRef, String pattern, 
            boolean escapePattern) {
        
        try {
            return graphIndexer.findResources(uriRef, pattern, escapePattern);
        } catch (ParseException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public List<NonLiteral> findResources(UriRef uriRef, String pattern, 
            boolean escapePattern, FacetCollector... facetCollectors) {
        
        try {
            return graphIndexer.findResources(uriRef, pattern, escapePattern, facetCollectors);
        } catch (ParseException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public List<NonLiteral> findResources(UriRef uriRef, String pattern, 
            boolean escapePattern, SortSpecification sortSpecification, 
            FacetCollector... facetCollectors) {
        
        try {
            return graphIndexer.findResources(uriRef, pattern, escapePattern, 
                    sortSpecification, facetCollectors);
        } catch (ParseException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public void reCreateIndex() {
        graphIndexer.reCreateIndex();
    }

    @Override
    public void optimizeIndex() {
        graphIndexer.optimizeIndex();
    }

    public void addDefintion(UriRef propertyType, List<UriRef> predicates) {
        Lock lock = definitionGraph.getLock().writeLock();
        lock.lock();
        try {
            indexDefinitionManager.addDefinition(propertyType, predicates);
        } finally {
            lock.unlock();
        }
    }

    public void addDefinitionVirtual(UriRef propertyType, List<VirtualProperty> predicates) {
        Lock lock = definitionGraph.getLock().writeLock();
        lock.lock();
        try {
            indexDefinitionManager.addDefinitionVirtual(propertyType, predicates);
        } finally {
            lock.unlock();
        }

    }

    public void deleteDefintion(UriRef propertyType) {
        Lock lock = definitionGraph.getLock().writeLock();
        lock.lock();
        try {
            indexDefinitionManager.deleteDefinition(propertyType);
        } finally {
            lock.unlock();
        }

    }
}
