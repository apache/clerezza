package org.apache.clerezza.rdf.jena.tdb.storage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.jena.tdb.internals.ModelGraph;
import org.apache.clerezza.rdf.jena.tdb.internals.Symbols;
import org.apache.clerezza.rdf.jena.tdb.internals.UriRefSet;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * {@link WeightedTcProvider} implementation for Jena TDB that uses a single
 * {@link TDBFactory#createDataset(String) Dataset} to store all created
 * {@link Graph} and {@link MGraph} instances.<p>
 * The {@link #TDB_DIR} is uses to configure the directory on the disc. It
 * supports property substitution <code>'${property}'</code> based on properties defined
 * in the {@link BundleContext#getProperty(String)} and 
 * {@link System#getProperty(String)}. This is to easily allow configurations
 * such as <code>"${myHome}/myRdfStore"</code><p>
 * The {@link #DEFAULT_GRAPH_NAME} property can be used to define the
 * name of the Graph that exposes the {@link Dataset#getDefaultModel()} as
 * both {@link TcProvider#getGraph(UriRef)} and {@link TcProvider#getMGraph(UriRef)}.
 * This easily allows to access the union graph of the Jena TDB dataset.<p>
 * This {@link TcProvider} {@link ConfigurationPolicy#REQUIRE requires} an
 * configuration and uses the {@link Component#configurationFactory() 
 * configuration factory}. Therefore it will be bot active until a valid
 * configuration is parsed to the {@link ConfigurationAdmin} service. However
 * it supports multiple instances to be created.<p>
 * Users that want to use multiple instances will need to use special filters
 * to ensure that the correct instance is injected to components. As by default
 * the instance with the highest {@link #WEIGHT} will be used by Clerezza
 * to create instances. A good practice to filter for multiple instances is
 * to add an additional user defined key to the configuration that can later
 * be used for filtering. Such additional keys will be savely ignored by
 * this implementation.<p>
 * 
 * @author Rupert Westenthaler, MInto van der Sluis
 *
 */
@Component(metatype=true, immediate=true,
    configurationFactory=true, policy=ConfigurationPolicy.OPTIONAL)
@Service(WeightedTcProvider.class)
@Properties(value={
    @Property(name=ScalableSingleTdbDatasetTcProvider.TDB_DIR),
    @Property(name=ScalableSingleTdbDatasetTcProvider.DEFAULT_GRAPH_NAME),
    @Property(name=ScalableSingleTdbDatasetTcProvider.SYNC_INTERVAL, intValue=ScalableSingleTdbDatasetTcProvider.DEFAULT_SYNC_INTERVAL),
    @Property(name=ScalableSingleTdbDatasetTcProvider.WEIGHT, intValue=107)
})
public class ScalableSingleTdbDatasetTcProvider extends BaseTdbTcProvider implements WeightedTcProvider {

    public static final String TDB_DIR = "tdb-dir";
    public static final String DEFAULT_GRAPH_NAME = "default-graph-name";
    public static final String WEIGHT = "weight";
    public static final String SYNC_INTERVAL = "sync-interval";
    public static final String USE_GRAPH_NAME_SUFFIXES = "use-graph-name-suffixes";
    
    public static final int DEFAULT_SYNC_INTERVAL = 6;
    public static final int MIN_SYNC_INTERVAL = 3;
    
    private final Logger log = LoggerFactory.getLogger(ScalableSingleTdbDatasetTcProvider.class);

    private int weight;
    private ModelGraph graphNameIndex;
    private int syncInterval = DEFAULT_SYNC_INTERVAL;
    private SyncThread syncThread;

    private final ReadWriteLock datasetLock = new ReentrantReadWriteLock();;
    private UriRef defaultGraphName;

    // Ensure that models not yet garbage collected get properly synced.
    private final ConcurrentMap<UriRef, ModelGraph> syncModels = new MapMaker().weakValues().makeMap();
    
    /**
     * This background thread ensures that changes to {@link Model}s are
     * synchronized with the file system. Only {@link ModelGraph}s where
     * <code>{@link ModelGraph#isReadWrite()} == true</code> are synced.<p>
     * This is similar to the synchronize thread used by the {@link TdbTcProvider}.
     * This thread is started during the 
     * {@link ScalableSingleTdbDatasetTcProvider#activate(ComponentContext) activation}
     * ad the shutdown is requested during 
     * {@link ScalableSingleTdbDatasetTcProvider#deactivate(ComponentContext) deactivation}
     */
    class SyncThread extends Thread {
        private boolean stopRequested = false;

        @Override
        public void run() {
            while (!stopRequested) {
                try {
                    Thread.sleep(syncInterval*1000);
                } catch (InterruptedException ex) {
                    interrupt();
                }
                if (!stopRequested) {
                    datasetLock.writeLock().lock();
                    try {
                        for(ModelGraph mg : syncModels.values()){
                            if(mg.isReadWrite()){
                                mg.sync();
                            } //else we do not need to sync read-only models
                        }
                    } finally {
                        datasetLock.writeLock().unlock();
                    }
                }
            }
        }
        
        public void requestStop() {
            stopRequested = true;
        }
    }

    /**
     * Default constructor used by OSGI
     */
    public ScalableSingleTdbDatasetTcProvider(){}
    
    /**
     * Creates a TDB single dataset {@link TcProvider} based on the parsed
     * configuration.<p>
     * The following properties are supported:<ul>
     * <li> {@link #TDB_DIR} (required): The directory used by Jena TDB. Property
     * substitution "${property-name}" with {@link System#getProperties()} is
     * supported.
     * <li> {@link #DEFAULT_GRAPH_NAME}: The name ({@link UriRef}) of the
     * {@link Graph} that exports the union graph. This graph allows to query
     * triples in any named model managed by this {@link TcProvider}.
     * <li> {@link #SYNC_INTERVAL}: The sync interval that
     * is used to write changes in the graph to the file system. If missing
     * the {@link #DEFAULT_SYNC_INTERVAL} is used. Values lower than 
     * {@link #MIN_SYNC_INTERVAL} are ignored
     * <li>{@link #WEIGHT}: The weight of this {@link TcProvider}. If missing
     * <code>0</code> is used as weight.
     * </ul>
     * <b>NOTE</b> Users need to call {@link #close()} to free up system 
     * resources when they are no longer need this instance.
     * @param config The configuration
     * @throws IOException the 
     * @throws ConfigurationException 
     */
    public ScalableSingleTdbDatasetTcProvider(Dictionary<String,Object> config) throws ConfigurationException, IOException{
        activate(null,config);
    }
    /**
     * Activate method used by OSGI
     * @param ctx
     * @throws ConfigurationException
     * @throws IOException
     */
    @Activate
    @SuppressWarnings("unchecked")
    protected void activate(ComponentContext ctx) throws ConfigurationException, IOException {
        activate(ctx.getBundleContext(),ctx.getProperties());
    }

    /**
     * Internally used for activation to support  the instantiation via
     * {@link #ScalableSingleTdbDatasetTcProvider(Dictionary)} - to be used outside
     * an OSGI container.
     * @param bc the BundleContext or <code>null</code> if activating outside
     * an OSGI container. The BundleContext is just used to lookup properties
     * for {@link #substituteProperty(String, BundleContext)}.
     * @param config The configuration for this Instance. Note that {@link #TDB_DIR}
     * is required to be present.
     * @throws ConfigurationException if the parsed configuration is invalid
     * @throws IOException on any error while creating/accessing the Jena TDB
     * directory.
     */
    private void activate(BundleContext bc,Dictionary<String,Object> config) throws ConfigurationException, IOException {
        log.info("Activating scalable single Dataset TDB provider");
        Object value = config.get(WEIGHT);
        if(value instanceof Number){
            weight = ((Number)value).intValue();
        } else if(value != null){
            try {
                weight = new BigDecimal(value.toString()).intValueExact();
            } catch (RuntimeException e) {
                throw new ConfigurationException(WEIGHT, "Unable to parse integer weight!", e);
            }
        } else { //weight not defined
            weight = 0;
        }
        value = config.get(SYNC_INTERVAL);
        if(value instanceof Number){
            syncInterval = Math.max(((Number)value).intValue(),MIN_SYNC_INTERVAL);
        } else if(value != null){
            try {
                syncInterval = Math.max(new BigDecimal(value.toString()).intValueExact(),MIN_SYNC_INTERVAL);
            } catch (RuntimeException e) {
                throw new ConfigurationException(SYNC_INTERVAL, "Unable to parse integer weight!", e);
            }
        } else { //sync interval not defined
            syncInterval = DEFAULT_SYNC_INTERVAL;
        }
        value = config.get(TDB_DIR);
        File dataDir;
        if(value != null && !value.toString().isEmpty()){
            dataDir = new File(substituteProperty(value.toString(),bc)).getAbsoluteFile();
        } else {
            value = config.get(Constants.SERVICE_PID);
            if(value == null){
                throw new ConfigurationException(TDB_DIR, "No Data Directory for "
                    + "the Jena TDB store parsed. Also unable to use the "
                    + "'service.pid' property as default because this property "
                    + "is not present in the parsed configuration.");
            }
            dataDir = bc.getDataFile("singleTdb"+File.separatorChar+value.toString());
            log.info("No TDB directory parsed - use default '{}'",dataDir);
        }
        //parse the default graph name
        value = config.get(DEFAULT_GRAPH_NAME);
        if(value != null && !value.toString().isEmpty()){
            try {
                new URI(value.toString());
                defaultGraphName = new UriRef(value.toString());
            } catch (URISyntaxException e) {
                throw new ConfigurationException(DEFAULT_GRAPH_NAME, "The parsed name '"
                        + value + "'for the default graph (union over all "
                		+ "named graphs managed by this Jena TDB dataset) MUST BE "
                        + "an valid URI or NULL do deactivate this feature!",e);
            }
        } else {
            defaultGraphName = null; //deactivate the default graph name
        }
        
        //validate the parsed directory!
        if(!dataDir.exists()){
            if(dataDir.mkdirs()){
                log.info("Created Jena TDB data directory {}",dataDir);
            } else {
                throw new ConfigurationException(TDB_DIR, "Unable to create Jena TDB data directory '"+dataDir+"'!");
            }
        } else if(!dataDir.isDirectory()){
            throw new ConfigurationException("tdb.dir", "Configured jena TDB data directory '"
                    + dataDir+"' already exists, but is not a Directory!");
        } //else exists and is a directory ... nothing to do
        TDB.getContext().set(TDB.symUnionDefaultGraph, true);
        setDataset( TDBFactory.createDataset(dataDir.getAbsolutePath()) );
        graphNameIndex = new ModelGraph(datasetLock, getDataset().getDefaultModel(),true);

        // Remove existing default graph names from the index (if might have changed
        // in the mean time).
        removeDefaultGraphFromIndex();

        //finally ensure the the defaultGraphName is not also used as a graph/mgraph name
        if (defaultGraphName != null) {
          if (isExistingGraphName(defaultGraphName)) {
            throw new ConfigurationException(DEFAULT_GRAPH_NAME, "The configured default graph name '"
                +defaultGraphName+"' is already used as a Graph or MGraph name!");
          } else {
            addToIndex( defaultGraphName, Symbols.Default );
            addToIndex( defaultGraphName, Symbols.Graph );
          }
        }

        syncThread = new SyncThread();
        syncThread.setDaemon(true);
        syncThread.setName("SyncDaemon for Jena TDB "+dataDir.getAbsolutePath());
        syncThread.start();
    }
    
    /**
     * call close in finalisation
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Closes this {@link TcProvider} instance and frees up all system resources.
     * This method needs only to be called when using this TcProvider outside
     * an OSGI environment.
     */
    public void close(){
        deactivate(null);
    }

    /**
     * Deactivates this component. Called by the OSGI environment if this
     * component gets deactivated.
     * @param ctx the ComponentContext. May be <code>null</code>
     */
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        if(syncThread != null){
            syncThread.requestStop();
            syncThread = null;
        }
    	Dataset dataset = getDataset();
        if(dataset != null){ //avoid NPE on multiple calls
            datasetLock.writeLock().lock();
            try {
                for(ModelGraph mg : syncModels.values()){
                    mg.close(); //close also syncs!
                }
                syncModels.clear();

                graphNameIndex.close();
                graphNameIndex = null;

                TDB.sync(dataset);
                dataset.close();
                setDataset(null);
            } finally {
                datasetLock.writeLock().unlock();
            }
        }
    }
    
    /**
     * Internal method used to retrieve an existing Jena {@link ModelGraph} 
     * instance from {@link #syncModels} or initializes a new Jena TDB {@link Model}
     * and Clerezza {@link Graph}s/{@link MGraph}s.
     * @param name the name of the Graph to initialize/create
     * @param readWrite if <code>true</code> a {@link MGraph} is initialized.
     * Otherwise a {@link Graph} is created.
     * @param create if this method is allowed to create an new {@link Model} or
     * if an already existing model is initialized.
     * @return the initialized {@link Model} and @link Graph} or {@link MGraph}.
     * The returned instance will be also cached in {@link #syncModels}. 
     * @throws NoSuchEntityException If <code>create == false</code> and no
     * {@link Model} for the parsed <code>name</code> exists.
     */
    private ModelGraph getModelGraph(UriRef name, boolean readWrite,boolean create) throws NoSuchEntityException {
        ModelGraph modelGraph = null;
        datasetLock.readLock().lock();
        try {
            if(readWrite) {
                // Reuse existing model if not yet garbage collected.
                modelGraph = syncModels.get(name);
            }
            if((modelGraph != null || isExistingGraphName(name)) && create){
                throw new EntityAlreadyExistsException(name);
            } else if(modelGraph == null){
                String modelName = name.getUnicodeString();
                modelGraph = new ModelGraph(datasetLock, name.equals(defaultGraphName) ? 
                		getDataset().getNamedModel("urn:x-arq:UnionGraph") : 
                			getDataset().getNamedModel(modelName),readWrite);
                if(readWrite) {
                    // Keep track of readwrite model to be able to sync them.
                    this.syncModels.put(name, modelGraph);
                }
            }
        } finally {
            datasetLock.readLock().unlock();
        }
        return modelGraph;
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#getGraph(org.apache.clerezza.rdf.core.UriRef)
     */
    @Override
    public Graph getGraph(UriRef name) throws NoSuchEntityException {
        if(name == null){
            throw new IllegalArgumentException("The parsed Graph UriRef MUST NOT be NULL!");
        }
        datasetLock.readLock().lock();
        try {
            if (isExistingGraphName(name, Symbols.Graph) || name.equals(defaultGraphName)){
                return getModelGraph(name,false,false).getGraph();
            } else {
                throw new NoSuchEntityException(name);
            }
        } finally {
            datasetLock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#getMGraph(org.apache.clerezza.rdf.core.UriRef)
     */
    @Override
    public MGraph getMGraph(UriRef name) throws NoSuchEntityException {
        if(name == null){
            throw new IllegalArgumentException("The parsed Graph UriRef MUST NOT be NULL!");
        }
        datasetLock.readLock().lock();
        try {
            if(isExistingGraphName(name, Symbols.MGraph)){
                return getModelGraph(name,true,false).getMGraph();
            } else {
                throw new NoSuchEntityException(name);
            }
        } finally {
            datasetLock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#getTriples(org.apache.clerezza.rdf.core.UriRef)
     */
    @Override
    public TripleCollection getTriples(UriRef name) throws NoSuchEntityException {
        if(name == null){
            throw new IllegalArgumentException("The parsed Graph UriRef MUST NOT be NULL!");
        }
        datasetLock.readLock().lock();
        try {
            if(isExistingGraphName(name, Symbols.Graph) || name.equals(defaultGraphName)){
                return getGraph(name);
            } else if(isExistingGraphName(name, Symbols.MGraph)){
                return getMGraph(name);
            } else {
                throw new NoSuchEntityException(name);
            }
        } finally {
            datasetLock.readLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#listGraphs()
     */
    @Override
    public Set<UriRef> listGraphs() {
        return new UriRefSet( graphNameIndex, Symbols.Graph );
    }

    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#listMGraphs()
     */
    @Override
    public Set<UriRef> listMGraphs() {
        return new UriRefSet( graphNameIndex, Symbols.MGraph );
    }

    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#listTripleCollections()
     */
    @Override
    public Set<UriRef> listTripleCollections() {
        return new UriRefSet( graphNameIndex, null );
    }

    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#createMGraph(org.apache.clerezza.rdf.core.UriRef)
     */
    @Override
    public MGraph createMGraph(UriRef name) throws UnsupportedOperationException,
                                           EntityAlreadyExistsException {
        if(name == null){
            throw new IllegalArgumentException("The parsed MGrpah name MUST NOT be NULL!");
        }
        datasetLock.writeLock().lock();
        try {
            if(isExistingGraphName(name)){
                throw new EntityAlreadyExistsException(name);
            }
            MGraph graph = getModelGraph(name,true,true).getMGraph();
            addToIndex( name, Symbols.MGraph);
            return graph;
        } finally {
            datasetLock.writeLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#createGraph(org.apache.clerezza.rdf.core.UriRef, org.apache.clerezza.rdf.core.TripleCollection)
     */
    @Override
    public Graph createGraph(UriRef name, TripleCollection triples) throws UnsupportedOperationException,
                                                                   EntityAlreadyExistsException {
        if(name == null){
            throw new IllegalArgumentException("The parsed Grpah name MUST NOT be NULL!");
        }
        ModelGraph mg;
        datasetLock.writeLock().lock();
        try {
            if(isExistingGraphName(name)){
                throw new EntityAlreadyExistsException(name);
            }
            mg = getModelGraph(name,false,true);
            addToIndex( name, Symbols.Graph);
            
            //add the parsed data!
            if(triples != null) { //load the initial and final set of triples
                mg.getJenaAdapter().addAll(triples);
                mg.sync();
            }
        } finally {
            datasetLock.writeLock().unlock();
        }
        return mg.getGraph();
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#deleteTripleCollection(org.apache.clerezza.rdf.core.UriRef)
     */
    @Override
    public void deleteTripleCollection(UriRef name) throws UnsupportedOperationException,
                                                   NoSuchEntityException,
                                                   EntityUndeletableException {
        if(name == null){
            throw new IllegalArgumentException("The parsed MGrpah name MUST NOT be NULL!");
        }
        datasetLock.writeLock().lock();
        try {
            if(isExistingGraphName(name,Symbols.MGraph)){
                ModelGraph mg = getModelGraph(name, true, false);
                mg.delete();
                removeFromIndex( name, Symbols.MGraph );
            } else if(isExistingGraphName(name,Symbols.Graph)){
                ModelGraph mg = getModelGraph(name, false, false);
                mg.delete();
                removeFromIndex( name, Symbols.Graph );
            } else if (name.equals(defaultGraphName)){
                throw new EntityUndeletableException(defaultGraphName);
            }
            //delete the graph from the initModels list
            syncModels.remove(name);
        } finally {
            datasetLock.writeLock().unlock();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#getNames(org.apache.clerezza.rdf.core.Graph)
     */
    @Override
    public Set<UriRef> getNames(Graph graph) {
        //TODO: this method would require to compare the triples within the graph
        //      because an equals check will not work with BNodes. 
        Set<UriRef> graphNames = new HashSet<UriRef>();
        for( Iterator<Triple> iterator = graphNameIndex.getMGraph().iterator(); iterator.hasNext(); ) {
            Triple triple = iterator.next();
            UriRef graphName = new UriRef(triple.getSubject().toString());
            Graph currentGraph = getModelGraph(graphName, false, false).getGraph();
            if(graph.equals(currentGraph)){
                graphNames.add(graphName);
            }
        }
        return graphNames;
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.WeightedTcProvider#getWeight()
     */
    @Override
    public int getWeight() {
        return weight;
    }
    
    /**
     * Substitutes ${property.name} with the values retrieved via <ul>
     * <li> {@link BundleContext#getProperty(String)} or
     * <li> {@link System#getProperty(String, String)} if the parsed
     * {@link BundleContext} is <code>null</code>
     * </ul>
     * Substitutes with an empty string if the property is not present. If
     * the substitution does not end with {@link File#separatorChar}, than it is
     * appended to allow easily creating paths relative to root directory available
     * as property regardless if the property includes/excludes the final
     * separator char.
     * <p>
     * Nested substitutions are NOT supported. However multiple substitutions are supported.
     * <p>
     * If someone knows a default implementation feel free to replace!
     * 
     * @param value
     *            the value to substitute
     * @param bundleContext
     *            If not <code>null</code> the {@link BundleContext#getProperty(String)} is used instead of
     *            the {@link System#getProperty(String)}. By that it is possible to use OSGI only properties
     *            for substitution.
     * @return the substituted value
     */
    private static String substituteProperty(String value, BundleContext bundleContext) {
        int prevAt = 0;
        int foundAt = 0;
        StringBuilder substitution = new StringBuilder();
        while ((foundAt = value.indexOf("${", prevAt)) >= prevAt) {
            substitution.append(value.substring(prevAt, foundAt));
            String propertyName = value.substring(foundAt + 2, value.indexOf('}', foundAt));
            String propertyValue = bundleContext == null ? // if no bundleContext is available
            System.getProperty(propertyName) : // use the System properties
                    bundleContext.getProperty(propertyName);
            if(propertyValue != null) {
                substitution.append(propertyValue);
                if(propertyValue.charAt(propertyValue.length()-1) != File.separatorChar){
                    substitution.append(File.separatorChar);
                }
            } //else nothing to append
            prevAt = foundAt + propertyName.length() + 3; // +3 -> "${}".length
        }
        substitution.append(value.substring(prevAt, value.length()));
        return substitution.toString();
    }

    /**
     * Checks whether the given graph name already exists as the specified resource (either graph or mgraph).
     * @param graphName the graph name
     * @param graphType the resource type
     * @return true if a resource with the given name and type already exists, false otherwise.
     */
    private boolean isExistingGraphName(UriRef graphName, UriRef graphType) {
        return graphNameIndex.getMGraph().filter(graphName, RDF.type, graphType).hasNext();
    }

    /**
     * Checks whether the given graph name already exists as either a graph or mgraph.
     * @param graphName the graph name
     * @return true if a graph or mgraph with the given name already exists, false otherwise.
     */
    private boolean isExistingGraphName(UriRef graphName) {
        return isExistingGraphName(graphName, null);
    }
    
    /**
     * Adds a new graphname to the index of graphnames  
     * @param graphName name of the graph
     * @param graphType resourcetype for the graph to add.
     */
    private void addToIndex(UriRef graphName, UriRef graphType) {
        graphNameIndex.getMGraph().add(new TripleImpl(graphName, RDF.type, graphType));
        graphNameIndex.sync();
    }
    
    /**
     * Removes a graphanem from the index of graphnames
     * @param graphName name of the graph to remove
     * @param graphType resource type of the graph to remove.
     */
    private void removeFromIndex(UriRef graphName, UriRef graphType) {
        MGraph index = graphNameIndex.getMGraph();
        Iterator<Triple> triplesToRemove = index.filter(graphName, RDF.type, graphType);
        for( ; triplesToRemove.hasNext(); ) {
            triplesToRemove.next();
            triplesToRemove.remove();
        }
        graphNameIndex.sync();
    }
    
    private void removeDefaultGraphFromIndex() {
      MGraph index = graphNameIndex.getMGraph();
      Iterator<Triple> triplesToRemove = index.filter(null, RDF.type, Symbols.Default);
      for( ; triplesToRemove.hasNext(); ) {
          Triple triple = triplesToRemove.next();
          triplesToRemove.remove();
          removeFromIndex( UriRef.class.cast(triple.getSubject()), Symbols.Graph );
      }
      graphNameIndex.sync();
    }
} 
