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
package org.apache.clerezza.rdf.cris;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphEvent;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.cris.ontologies.CRIS;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates an index of RDF resources and provides an interface to 
 * search for indexed resources.
 *
 * @author reto, tio, daniel
 */
public class GraphIndexer extends ResourceFinder {
    
    /**
     * Default value for {@code maxhits}.
     */
    public static final int DEFAULT_MAXHITS = 100000;
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Prefix for stored Lucene fields.
     */
    static final String SORT_PREFIX = "_STORED_";
    
    /**
     * Field name for the resource field in Lucene.
     */
    static final String URI_FIELD_NAME = "resource-uri";
    
    /**
     * Handler for asynchronous indexing.
     */
    ReindexThread reindexer;
    
    private TripleCollection definitionGraph;
    private TripleCollection baseGraph;
    private int maxHits;
    private Analyzer analyzer;
    private LuceneTools luceneTools;
    private Map<UriRef, Set<VirtualProperty>> type2IndexedProperties = null;
    private Map<VirtualProperty, Set<UriRef>> property2TypeMap = new HashMap<VirtualProperty, Set<UriRef>>();
    private Map<UriRef, Set<VirtualProperty>> property2IncludingVProperty = new HashMap<UriRef, Set<VirtualProperty>>();
    private final GraphListener typeChangeListener;
    private final GraphListener indexedPropertyChangeListener;
    private Map<SortFieldArrayWrapper, Sort> sortCache = new HashMap<SortFieldArrayWrapper, Sort>();
    private Timer timer = new Timer();
    private final OptimizationTask optimizationTask = new OptimizationTask();
    
    /**
     * Allows to schedule optimizations using a Timer.
     * 
     * NOTE: not for public access as this functionality is likely to be moved
     * into a stand-alone service.
     */
    private class OptimizationTask extends TimerTask {
        @Override
        public void run() {
            optimizeIndex();
        }
    }
    
    /**
     * When resources are (re)-indexed, 
     * this thread updates the Lucene index asynchronously. 
     */
    class ReindexThread extends Thread {

        private final long resourceCacheCapacity;
        private final long stableThreshold;
        private final String name;
        private final Set<Resource> resourcesToProcess;
        private final Lock lock = new ReentrantLock(true);
        private final java.util.concurrent.locks.Condition indexResources =
                lock.newCondition();
        private long counter;
        private boolean stop;
        private boolean resourcesClean;

        /**
         * Constructs a new thread with specified name and indexing threshold. 
         * Setting the name may be useful for distinguishing logging output when 
         * multiple instances of GraphIndexer are running.
         * 
         * {@code stableThreshold} specifies a waiting period before the 
         * indexing starts. The timer is restarted if more resources are added 
         * within {@code stableThreshold} nanoseconds. A high value means the 
         * thread will wait a long time before indexing resources added using 
         * {@link addResource(Resource resource)}. A short value means new 
         * resources are added to the index quickly. Configure this value such 
         * that when adding many new resources in a short time these are 
         * gathered and indexed at once.
         * 
         * @param name    the thread name (used in logging output).
         * @param stableThreshold
         *        If no new resource has been added for {@code stableThreshold} 
         *        nanoseconds and there are cached unindexed resources, then 
         *        indexing starts.
         * @param resourceCacheCapacity  
         *        How many resources will be cached maximally before indexing.
         *        A negative number means infinite.
         */
        ReindexThread(String name, long stableThreshold, long resourceCacheCapacity) {
            this.resourceCacheCapacity = resourceCacheCapacity;
            this.stableThreshold = stableThreshold;
            this.name = name;
            this.resourcesToProcess = new HashSet<Resource>();
            this.resourcesClean = true;
        }
        
        /**
         * Constructs a new thread with specified indexing threshold.
         * 
         * @code stableThreshold} specifies a waiting period before the 
         * indexing starts. The timer is restarted if more resources are added 
         * within {@code stableThreshold} nanoseconds. A high value means the 
         * thread will wait a long time before indexing resources added using 
         * {@link addResource(Resource resource)}. A short value means new 
         * resources are added to the index quickly. Configure this value such 
         * that when adding many new resources in a short time these are 
         * gathered and indexed at once.
         * 
         * @param stableThreshold 
         *        If no new resource has been added for {@code stableThreshold} 
         *        nanoseconds and there are cached unindexed resources, then 
         *        indexing starts.
         * @param resourceCacheCapacity  
         *        How many resources will be cached maximally before indexing.
         *        A negative number means infinite.
         */
        ReindexThread(long stableThreshold, long resourceCacheCapacity) {
            this(null, stableThreshold, resourceCacheCapacity);
        }
        

        /**
         * Request the termination of this thread. The thread will finish its 
         * current operations before it terminates.
         */
        void stopThread() {
            stop = true;
            lock.lock();
            try {
                indexResources.signal();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void run() {
            if(name == null) {
                setName("CRIS Reindex Thread[" + getId() + "]");
            } else {
                setName(name);
            }
            stop = false;
            counter = 0;
            Set<Resource> set;
            logger.info("{} started.", getName());
            while (true) {
                try {
                    lock.lock();
                    try {
                        waitForDirty();
                        if (stop) {
                            break;
                        }
                        logger.debug("{}: registered write - waiting for more writes to follow.", getName());
                        waitUntilStable();
                        set = new HashSet<Resource>(resourcesToProcess);
                        resourcesToProcess.clear();
                        counter = 0;
                        resourcesClean = true;
                    } finally {
                        lock.unlock();
                    }
                    logger.info("{}: cache full or writes have ceased. Indexing...", getName());
                    for (Resource resource : set) {
                        indexResource(resource, luceneTools.getIndexWriter());
                    }
                    luceneTools.commitChanges();
                } catch (InterruptedException ex) {
                    logger.warn("{}: interrupted: {}.", getName(), ex);
                }
            }
            logger.info("{} stopped.", getName());
        }
        
        private void waitUntilStable() throws InterruptedException {
            while (!resourcesClean) {
                resourcesClean = true;
                indexResources.awaitNanos(stableThreshold);
                if(resourceCacheCapacity >= 0 && ++counter > resourceCacheCapacity) {
                    break;
                }
            }
        }

        private void waitForDirty() throws InterruptedException {
            while (resourcesClean && !stop) {
                indexResources.await();
            }
        }

        /**
         * Add a new resource for indexing.
         * 
         * @param resource the resource.
         */
        public void addResource(Resource resource) {
            lock.lock();
            try {
                resourcesToProcess.add(resource);
                resourcesClean = false;
                indexResources.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * Creates a new index.
     * 
     * The {@code GraphIndexer} looks for specifications of what properties on 
     * what resources to index in the {@code definitionGraph}.
     * 
     * The {@code baseGraph} specifies the graph on which the index is built.
     * 
     * <p>Notes: 
     * 
     * <p>
     * This is an expensive operation and it is advisable to call 
     * {@link #closeLuceneIndex()} when this instance is no longer needed.
     * </p><p>
     * The GraphIndexer must have write-access to the index directory specified.
     * </p>
     * 
     * @param definitionGraph
     *        where index definitions are stored
     * @param baseGraph
     *        where the resources to index are stored
     * @param indexDirectory
     *        The directory where the index is stored.
     * @param createNewIndex
     *        Whether to create a new index or reuse an existing one. 
     *        The constructor does not check if there is a valid exiting index. 
     *        The user is responsible for setting this value correctly.
     * @param maxHits
     *        How many results the indexer returns. All entries in the index are 
     *        searched, but only @code{maxHits} resources are resolved and 
     *        returned in the result.
     * 
     * @see IndexDefinitionManager
     */
    public GraphIndexer(TripleCollection definitionGraph,
            TripleCollection baseGraph, Directory indexDirectory,
            boolean createNewIndex, int maxHits) {
        this.definitionGraph = definitionGraph;
        this.baseGraph = baseGraph;
        this.maxHits = maxHits;
        analyzer = new StandardAnalyzer(Version.LUCENE_41);

        luceneTools = new LuceneTools(indexDirectory, analyzer);
        processDefinitions();

        this.reindexer = new ReindexThread(100000000L, 500000L);

        typeChangeListener = new GraphListener() {

            @Override
            public void graphChanged(List<GraphEvent> events) {
                for (GraphEvent e : events) {
                    Triple triple = e.getTriple();
                    logger.debug("processing addition of type " + triple.getObject());
                    if (type2IndexedProperties.containsKey((UriRef) triple.getObject())) {
                        scheduleForReindex(triple.getSubject());
                    }

                }
            }
        };

        baseGraph.addGraphListener(typeChangeListener, new FilterTriple(null, RDF.type, null));

        indexedPropertyChangeListener = new GraphListener() {

            @Override
            public void graphChanged(List<GraphEvent> events) {
                for (GraphEvent e : events) {
                    logger.debug("Triple: " + e.getTriple());
                    Triple triple = e.getTriple();

                    UriRef predicate = triple.getPredicate();
                    Set<VirtualProperty> vProperties = property2IncludingVProperty.get(predicate);

                    List<Resource> indexedResources = new ArrayList<Resource>();
                    logger.debug("Predicate: " + predicate);
                    for (VirtualProperty vProperty : vProperties) {
                        logger.debug("Subject: " + " " + triple.getSubject());
                        followInversePaths(triple.getSubject(), 
                                vProperty.pathToIndexedResource(predicate), indexedResources);

                    }
                    for (Resource resource : indexedResources) {
                        GraphNode node = new GraphNode(resource, e.getGraph());
                        List<UriRef> types = new ArrayList<UriRef>();
                        Lock lock = node.readLock();
                        lock.lock();
                        try {
                            Iterator<Resource> resources = node.getObjects(RDF.type);
                            while (resources.hasNext()) {
                                Resource res = resources.next();
                                if (res instanceof UriRef) {
                                    types.add((UriRef) res);
                                }
                            }
                        } finally {
                            lock.unlock();
                        }
                        for (UriRef type : types) {
                            if (type2IndexedProperties.containsKey(type)) {
                                scheduleForReindex(resource);
                            }
                        }
                    }

                }
            }
        };

        baseGraph.addGraphListener(indexedPropertyChangeListener, 
                new FilterTriple(null, null, null) {

            @Override
            public boolean match(Triple triple) {
                UriRef predicate = triple.getPredicate();
                //check indirectly involved properties
                Set<VirtualProperty> vProperties = property2IncludingVProperty.get(predicate);
                if (vProperties != null) {
                    for (VirtualProperty vProperty : vProperties) {
                        if (property2TypeMap.containsKey(vProperty)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        reindexer.start();

        if (createNewIndex) {
            reCreateIndex();
        }
    }
    
    /**
     * Creates a new index with default {@code maxHits}.
     * 
     * The {@code GraphIndexer} looks for specifications of what properties on 
     * what resources to index in the {@code definitionGraph}.
     * 
     * The {@code baseGraph} specifies the graph on which the index is built.
     * 
     * <p>Notes: 
     * 
     * <p>
     * This is an expensive operation and it is advisable to call 
     * {@link #closeLuceneIndex()} when this instance is no longer needed.
     * </p><p>
     * The GraphIndexer must have write-access to the index directory specified.
     * </p>
     * 
     * @param definitionGraph
     *        where index definitions are stored
     * @param baseGraph
     *        where the resources to index are stored
     * @param indexDirectory
     *        The directory where the index is stored.
     * @param createNewIndex
     *        Whether to create a new index or reuse an existing one. 
     *        The constructor does not check if there is a valid exiting index. 
     *        The user is responsible for setting this value correctly.
     */
    public GraphIndexer(TripleCollection definitionGraph,
            TripleCollection baseGraph, Directory indexDirectory,
            boolean createNewIndex) {
        this(definitionGraph, baseGraph, indexDirectory, createNewIndex, 
                DEFAULT_MAXHITS);
    }

    /**
     * Creates a new in-memory index with default {@code maxHits}.
     * 
     * The {@code GraphIndexer} looks for specifications of what properties on 
     * what resources to index in the {@code definitionGraph}.
     * 
     * The {@code baseGraph} specifies the graph on which the index is built.
     * 
     * <p>Notes: 
     * 
     * <p>
     * This is an expensive operation and it is advisable to call 
     * {@link #closeLuceneIndex()} when this instance is no longer needed.
     * </p><p>
     * The GraphIndexer must have write-access to the index directory specified.
     * </p>
     * 
     * @param definitionGraph
     *        where index definitions are stored
     * @param baseGraph
     *        where the resources to index are stored
     */
    public GraphIndexer(TripleCollection definitionGraph, TripleCollection baseGraph) {
        this(definitionGraph, baseGraph, new RAMDirectory(), true);
    }

    /**
     * Releases resources held by GraphIndexer. After the call to this method, 
     * this GraphIndexer instance must not be used anymore.
     */
    public void closeLuceneIndex() {
        this.baseGraph.removeGraphListener(typeChangeListener);
        this.baseGraph.removeGraphListener(indexedPropertyChangeListener);
        this.reindexer.stopThread();
        this.luceneTools.closeIndexSearcher();
        this.luceneTools.closeIndexWriter();
        this.sortCache.clear();
    }
    
    /**
     * Returns the Analyzer used by this GraphIndexer instance.
     * 
     * @return the Analyzer
     */
    public Analyzer getAnalyzer() {
        return analyzer;
    }

    /**
     * Returns the graph that this GraphIndexer builds an index on.
     * 
     * @return    The graph containing the indexed resources.
     */
    public TripleCollection getBaseGraph() {
        return baseGraph;
    }
    
    /**
     * Returns the graph where the index definitions are stored.
     * 
     * @return The graph with the index definitions.
     */
    public TripleCollection getDefinitionGraph() {
        return definitionGraph;
    }
    
    /**
     * How many results a search on the index returns maximally.
     * 
     * @return    the maximum number of results.
     */
    public int getMaxHits() {
        return maxHits;
    }

    /**
     * Set how many results a search on the index returns maximally.
     * 
     * @param maxHits    the maximum number of results.
     */
    public void setMaxHits(int maxHits) {
        this.maxHits = maxHits;
    }

    @Override
    public void optimizeIndex() {
        luceneTools.optimizeIndex();
    }
    
    /**
     * Schedule optimizations for repeated executions.
     * 
     * @param delay 
     *        The delay before the first execution in milliseconds.
     * @param period 
     *        Time between successive executions (execution rate) in milliseconds.
     */
    public void scheduleIndexOptimizations(long delay, long period) {
        if(timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(optimizationTask, delay, period);
    }
    
    /**
     * Cancel scheduled optimizations. This call does not have any effect on
     * optimizations that are being executed while the method is called.
     */
    public void terminateIndexOptimizationSchedule() {
        timer.cancel();
        timer = null;
    } 

    @Override
    public void reCreateIndex() {
        processDefinitions();

        List<NonLiteral> instances = new ArrayList<NonLiteral>();

        for (UriRef indexedType : type2IndexedProperties.keySet()) {
            //lock necessary?
            Lock lock =  new GraphNode(indexedType, this.baseGraph).readLock();
            lock.lock();
            try {
                Iterator<Triple> iter  = this.baseGraph.filter(null, RDF.type, indexedType);
                while (iter.hasNext()) {
                    instances.add(iter.next().getSubject());
                }
            } finally {
                lock.unlock();
            }
        }
        logger.debug("instances " + instances.size());
        IndexWriter writer = luceneTools.getIndexWriter(true);
        try {
            writer.deleteAll();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        for (NonLiteral instance : instances) {
            indexResource(instance, writer);
        }
        luceneTools.commitChanges();
    }

    @Override
    public List<NonLiteral> findResources(List<? extends Condition> conditions, 
            SortSpecification sortSpecification, 
            FacetCollector... facetCollectors) throws ParseException {
        return findResources(conditions, sortSpecification, 
                Arrays.asList(facetCollectors), 0, maxHits + 1);
    }
    
    /**
     * Find resources using conditions and collect facets and specify a sort order. 
     * 
     * This method allows to specify the indices of the query results to return
     * (e.g. for pagination).
     * 
     * @param conditions
     *        a list of conditions to construct a query from.
     * @param facetCollectors
     *        Facet collectors to apply to the query result. 
     *        Can be {@link Collections#EMPTY_LIST}, if not used.
     * @param sortSpecification 
     *        Specifies the sort order. Can be null, if not used.
     * @param from
     *        return results starting from this index (inclusive).
     * @param to
     *        return results until this index (exclusive).
     * @return    
     *        a list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(List<? extends Condition> conditions, 
            SortSpecification sortSpecification, 
            List<FacetCollector> facetCollectors, int from, int to)
            throws ParseException {

        if(from < 0) {
            from = 0;
        }
        
        if(to < from) {
            to = from + 1;
        }
        
        if(facetCollectors == null) {
            facetCollectors = Collections.EMPTY_LIST;
        }
        
        BooleanQuery booleanQuery = new BooleanQuery();
        for (Condition c : conditions) {
            booleanQuery.add(c.query(), BooleanClause.Occur.MUST);
        }
        
        IndexSearcher searcher = luceneTools.getIndexSearcher();
        ScoreDoc[] hits = null;
        try {
            if(sortSpecification != null) {
                SortFieldArrayWrapper fieldKey = 
                        new SortFieldArrayWrapper(sortSpecification.getSortFields());
                Sort sort = sortCache.get(fieldKey);
                if(sort == null) {
                    sort = new Sort(sortSpecification.getSortFields());
                    sortCache.put(fieldKey, sort);
                }
                //searcher.setDefaultFieldSortScoring(true, true);
                TopFieldDocs topFieldDocs = searcher.search(booleanQuery, null, to, sort);
                hits = topFieldDocs.scoreDocs;
            } else {
                TopScoreDocCollector collector = TopScoreDocCollector.create(to, true);
                searcher.search(booleanQuery, collector);
                hits = collector.topDocs().scoreDocs;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        List<NonLiteral> result = new ArrayList<NonLiteral>();

        for (int i = from; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d;
            try {
                d = searcher.doc(docId);
                collectFacets(facetCollectors, d);
                result.add(getResource(d));
            } catch (IOException ex) {
                logger.error("CRIS Error: ", ex);
            }
        }
        
        for(FacetCollector facetCollector : facetCollectors) {
            facetCollector.postProcess();
        }
        
        return result;
    }
    
    @Override
    public void finalize()
            throws Throwable {
        super.finalize();
        closeLuceneIndex();

    }
    
    /**
     * Schedule an update or creation of an index for a resource.
     * 
     * @param resource    the resource to index.
     */
    protected void scheduleForReindex(Resource resource) {
        logger.debug("Scheduling for reindex: " + resource);
        reindexer.addResource(resource);
    }

    /**
     * Read the index definitions and initialize the GraphIndexer with them.
     */
    protected void processDefinitions() {

        Iterator<Triple> indexDefinitionResources = 
                this.definitionGraph.filter(null, RDF.type, CRIS.IndexDefinition);

        Map<UriRef, Set<VirtualProperty>> type2IndexedPropertiesTuples = 
                new HashMap<UriRef, Set<VirtualProperty>>();

        while (indexDefinitionResources.hasNext()) {
            GraphNode node = new GraphNode(indexDefinitionResources.next().getSubject(), 
                    this.definitionGraph);
            Iterator<GraphNode> types = node.getObjectNodes(CRIS.indexedType);
            while (types.hasNext()) {
                UriRef tUri = (UriRef) types.next().getNode();
                Iterator<GraphNode> properties = node.getObjectNodes(CRIS.indexedProperty);
                Set<VirtualProperty> props = new HashSet<VirtualProperty>();
                while (properties.hasNext()) {
                    VirtualProperty vProp = asVirtualProperty(properties.next(), null);
                    if (property2TypeMap.containsKey(vProp)) {
                        property2TypeMap.get(vProp).add(tUri);
                    } else {
                        Set<UriRef> set = new HashSet<UriRef>();
                        set.add(tUri);
                        property2TypeMap.put(vProp, set);
                    }

                    for (UriRef baseProperty : vProp.baseProperties) {
                        if (property2IncludingVProperty.containsKey(baseProperty)) {
                            property2IncludingVProperty.get(baseProperty).add(vProp);
                        } else {
                            Set<VirtualProperty> set = new HashSet<VirtualProperty>();
                            set.add(vProp);
                            property2IncludingVProperty.put(baseProperty, set);
                        }
                    }
                    props.add(vProp);

                }
                type2IndexedPropertiesTuples.put(tUri, props);

            }
        }
        type2IndexedProperties = new HashMap(type2IndexedPropertiesTuples);

    }
    
    /**
     * Index a resource.
     * 
     * @param resource    the resource to index.
     * @param writer    the index writer.
     */
    protected void indexResource(Resource resource, IndexWriter writer) {
        if (resource instanceof UriRef) {
            try {
                indexNamedResource((UriRef) resource, writer);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            indexAnonymousResource(resource);
        }
    }
    
    private NonLiteral getResource(Document d) {
        return new UriRef(d.get(URI_FIELD_NAME));
    }

    private void followInversePaths(Resource resource, List<UriRef> pathToIndexedResource, List<Resource> list) {
        if (pathToIndexedResource.isEmpty()) {
            list.add(resource);
        } else {
            GraphNode node = new GraphNode(resource, this.baseGraph);
            Lock lock = node.readLock();
            lock.lock();
            try {
                Iterator<NonLiteral> predecessors = node.getSubjects(pathToIndexedResource.get(0));

                List<UriRef> tail = pathToIndexedResource.subList(1, pathToIndexedResource.size());
                while (predecessors.hasNext()) {
                    followInversePaths(predecessors.next(), tail, list);
                }
            } finally {
                lock.unlock();
            }
            //throw new RuntimeException("modification of indirect properties not yet supported")
        }
    }

    private VirtualProperty asVirtualProperty(GraphNode r, List<VirtualProperty> vPropertyList) {
        if (r.hasProperty(RDF.type, CRIS.JoinVirtualProperty)) {
            if (vPropertyList == null) {
                vPropertyList = new ArrayList<VirtualProperty>();
            }
            return new JoinVirtualProperty(getVirtualPropertyList(r, vPropertyList));
        } else {
            if (r.hasProperty(RDF.type, CRIS.PathVirtualProperty)) {
                return new PathVirtualProperty(getUriPropertyList(r));
            } else {
                if ((r.getNode()) instanceof UriRef) {
                    return new PropertyHolder((UriRef) r.getNode());
                } else {
                    throw new RuntimeException(r + " is not of a knows VirtualProperty type and its not a UriRef  (it's a " + (r.getNode()).getClass() + ")");
                }
            }
        }
    }

    private List<VirtualProperty> getVirtualPropertyList(GraphNode r, List<VirtualProperty> vPropertyList) {
        List<Resource> rdfList = getPropertyList(r);
        for (Resource childPropertyResource : rdfList) {
            vPropertyList.add(asVirtualProperty(new GraphNode(childPropertyResource, r.getGraph()), vPropertyList));
        }

        return vPropertyList;
    }

    private List<UriRef> getUriPropertyList(GraphNode r) {
        List<UriRef> uriPropertyList = new ArrayList<UriRef>();

        List<Resource> rdfList = getPropertyList(r);
        for (Resource childPropertyResource : rdfList) {
            uriPropertyList.add((UriRef) childPropertyResource);
        }
        return uriPropertyList;
    }

    private List<Resource> getPropertyList(GraphNode r) {
        Iterator<GraphNode> propertyLists = r.getObjectNodes(CRIS.propertyList);
        if (propertyLists.hasNext()) {
            List<Resource> rdfList = new RdfList(propertyLists.next());
            return rdfList;
        }
        throw new RuntimeException("There is no propertyList on this definition.");
    }
    
    private void collectFacets(List<FacetCollector> facetCollectors, Document d) {
        if(facetCollectors.size() > 0) {
            for(FacetCollector facetCollector : facetCollectors) {
                Map<VirtualProperty, Map<String, Object>> facetMap = 
                        facetCollector.getFacetMap();
                for(VirtualProperty property : facetMap.keySet()) {
                    String[] values = d.getValues(SORT_PREFIX + property.getStringKey());
                    if(values != null) {
                        for(String value : values) {
                            facetCollector.addFacetValue(property, value);
                        }
                    }
                }
            }
        }
    }

    private void indexNamedResource(UriRef uriRef, IndexWriter writer)
            throws IOException {

        Term term = new Term(URI_FIELD_NAME, uriRef.getUnicodeString());
        writer.deleteDocuments(term);
        //the reindexing might be caused by the removal of a type statement

        GraphNode node = new GraphNode(uriRef, this.baseGraph);
        List<UriRef> types = new ArrayList<UriRef>();
        Lock lock = node.readLock();
        lock.lock();
        try {
            Iterator<Resource> resources = node.getObjects(RDF.type);
            while (resources.hasNext()) {
                Resource res = resources.next();
                if (res instanceof UriRef) {
                    types.add((UriRef) res);
                }
            }
        } finally {
            lock.unlock();
        }
        for (UriRef type : types) {
            if (type2IndexedProperties.containsKey(type)) {
                Document doc = resourceToDocument(uriRef, type);
                doc.add(new Field(URI_FIELD_NAME, uriRef.getUnicodeString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                writer.addDocument(doc);
            }
        }
    }

    private Document resourceToDocument(UriRef resource, UriRef resourceType) {
        Document doc = new Document();
        Set<VirtualProperty> indexedProperties = type2IndexedProperties.get(resourceType);
        logger.info("indexing " + resource + " considering " + indexedProperties.size() + " properties (" + indexedProperties + ")");
        for (VirtualProperty vProperty : indexedProperties) {
            logger.info("indexing " + vProperty + " with values " + (vProperty.value(
                    new GraphNode(resource, this.baseGraph))).size());
            for (String propertyValue : vProperty.value(new GraphNode(resource, this.baseGraph))) {
                logger.info("indexing " + vProperty + "(" + vProperty.stringKey + ") with value " + (propertyValue));
                //for sorting
                doc.add(new Field(SORT_PREFIX + vProperty.stringKey,
                        propertyValue,
                        Field.Store.YES,
                        Field.Index.NOT_ANALYZED_NO_NORMS));
                //for searching (the extra field doesn't cost much time)
                doc.add(new Field(vProperty.stringKey,
                        propertyValue,
                        Field.Store.NO,
                        Field.Index.NOT_ANALYZED));
                doc.add(new Field(vProperty.stringKey,
                        propertyValue,
                        Field.Store.NO,
                        Field.Index.ANALYZED));
            }
        }
        return doc;
    }

    private void indexAnonymousResource(Resource resource) {
        logger.warn("Currently only indexing named resources is supported");
        /*val doc = resourceToDocument(resource)
        doc.add(new Field(URI_FIELD_NAME, getIdentifier(resource), Field.Store.YES, Field.Index.ANALYZED))
        writer.addDocument(doc)*/
    }
}
