package org.apache.clerezza.rdf.jena.tdb.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.LockableMGraphWrapper;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.core.impl.SimpleGraph;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedGraphWrapper;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedMGraphWrapper;
import org.apache.clerezza.rdf.jena.storage.JenaGraphAdaptor;
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

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
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
 * @author Rupert Westenthaler
 *
 */
@Component(metatype=true, immediate=true,
    configurationFactory=true, policy=ConfigurationPolicy.OPTIONAL)
@Service(WeightedTcProvider.class)
@Properties(value={
    @Property(name=SingleTdbDatasetTcProvider.TDB_DIR),
    @Property(name=SingleTdbDatasetTcProvider.DEFAULT_GRAPH_NAME),
    @Property(name=SingleTdbDatasetTcProvider.SYNC_INTERVAL, intValue=SingleTdbDatasetTcProvider.DEFAULT_SYNC_INTERVAL),
    @Property(name=SingleTdbDatasetTcProvider.WEIGHT, intValue=106)
})
public class SingleTdbDatasetTcProvider implements WeightedTcProvider {

    public static final String TDB_DIR = "tdb-dir";
    public static final String DEFAULT_GRAPH_NAME = "default-graph-name";
    public static final String WEIGHT = "weight";
    public static final String SYNC_INTERVAL = "sync-interval";
    public static final String USE_GRAPH_NAME_SUFFIXES = "use-graph-name-suffixes";
    
    public static final int DEFAULT_SYNC_INTERVAL = 6;
    public static final int MIN_SYNC_INTERVAL = 3;
    
    private final Logger log = LoggerFactory.getLogger(SingleTdbDatasetTcProvider.class);
    
    private int weight;
    private int syncInterval = DEFAULT_SYNC_INTERVAL;
    private SyncThread syncThread;

    private Dataset dataset;
    private final ReadWriteLock datasetLock = new ReentrantReadWriteLock();;
    
    private File graphConfigFile;
    private File mGraphConfigFile;
    
    private HashMap<UriRef,ModelGraph> initModels = new HashMap<UriRef,ModelGraph>();
    /**
     * the {@link UriRef}s of the graphs (read-only)
     */
    private Set<UriRef> graphNames;
    private Set<UriRef> mGraphNames;
    private UriRef defaultGraphName;
    /**
     * Represents the Jena {@link Model} and the Clerezza {@link Graph} or
     * {@link MGraph}. It also provide access to the {@link JenaGraphAdaptor}
     * so that this component can add parsed data to {@link Graph}s created
     * by calls to {@link SingleTdbDatasetTcProvider#createGraph(UriRef, TripleCollection)}.
     * @author Rupert Westenthaler
     *
     */
    private class ModelGraph {
        /**
         * The Jena Model
         */
        private final Model model;
        /**
         * The JenaGraphAdapter. Note that in case of read-only in anonymous
         * subclass is used that prevents the creation of an in-memory copy
         * of the data when calling {@link JenaGraphAdaptor#getGraph()}.
         */
        private JenaGraphAdaptor jenaAdapter;
        /**
         * The {@link Graph}(in case of read-only) or {@link MGraph} (if read/write)
         * that can be shared with other components. The instance stored by this
         * variable will use all the required Wrappers such as such as 
         * {@link LockableMGraphWrapper lockable} and {@link PrivilegedMGraphWrapper
         * privileged}.
         */
        private TripleCollection graph;
        /**
         * keeps the state if this represents an {@link Graph} (read-only) or
         * {@link MGraph}(read/write) ModelGraph.
         */
        private final boolean readWrite;
        
        /**
         * Constructs and initializes the ModelGraph
         * @param model the Jena Model
         * @param readWrite if the Clerezza counterpart should be read- and 
         * write-able or read-only.
         */
        protected ModelGraph(Model model, boolean readWrite){
            if(model == null){
                throw new IllegalArgumentException("The parsed Model MUST NOT be NULL");
            }
            this.model = model;
            this.readWrite = readWrite;
            if(!readWrite){ //construct an graph
                jenaAdapter = new JenaGraphAdaptor(model.getGraph()){
                    /**
                     * Ensure that no in-memory copies are created for read only
                     * Jena Graphs
                     * @return
                     */
                    @Override
                    public Graph getGraph() {
                        return new SimpleGraph(this,true);
                    }
                };
                graph = new PrivilegedGraphWrapper(jenaAdapter.getGraph());
            } else { //construct an MGraph
                jenaAdapter = new JenaGraphAdaptor(model.getGraph());
                this.graph =  new DatasetLockedMGraph(
                    new PrivilegedMGraphWrapper(jenaAdapter));
            }
        }
        /**
         * The {@link JenaGraphAdaptor}. For internal use only! Do not pass
         * this instance to other components. Use {@link #getGraph()} and
         * {@link #getMGraph()} instead!
         * @return the plain {@link JenaGraphAdaptor}
         */
        protected JenaGraphAdaptor getJenaAdapter(){
            return jenaAdapter;
        }
//        public boolean isReadonly(){
//            return !readWrite;
//        }
        public boolean isReadWrite(){
            return readWrite;
        }
        /**
         * Getter for the {@link MGraph}
         * @return the {@link MGraph}
         * @throws IllegalStateException if this {@link ModelGraph} is NOT
         * {@link #readWrite}
         */
        public MGraph getMGraph(){
            if(!readWrite){
                throw new IllegalStateException("Unable to return MGraph for read-only models");
            }
            return (MGraph)graph;
        }
        /**
         * Getter for the {@link Graph}
         * @return the {@link Graph}
         * @throws IllegalStateException if this {@link ModelGraph} is 
         * {@link #readWrite}
         */
        public Graph getGraph() {
            if(readWrite){
                throw new IllegalStateException("Unable to return Graph for read/write models.");
            }
            return (Graph)graph;
        }
        /**
         * closes this ModelGraph and frees up all Jena TDB related resources.
         */
        public void close(){
            this.graph = null;
            this.jenaAdapter = null;
            sync();
            this.model.close();
        }
        /**
         * Synchronize the Jena Model with the field system by calling
         * {@link TDB#sync(Model)}
         */
        public void sync(){
            TDB.sync(model);
        }
        /**
         * Removes all triples from the Jena Model and than calls {@link #close()}
         * to free remaining resources. Note that in Jena TDB a named model is 
         * deleted if no more triples with the given context are present within
         * the {@link Quad} store of the Jena TDB {@link DatasetGraph}.
         */
        public void delete(){
            this.model.removeAll();
            close();
        }
        
    }
    /**
     * This background thread ensures that changes to {@link Model}s are
     * synchronized with the file system. Only {@link ModelGraph}s where
     * <code>{@link ModelGraph#isReadWrite()} == true</code> are synced.<p>
     * This is similar to the synchronize thread used by the {@link TdbTcProvider}.
     * This thread is started during the 
     * {@link SingleTdbDatasetTcProvider#activate(ComponentContext) activation}
     * ad the shutdown is requested during 
     * {@link SingleTdbDatasetTcProvider#deactivate(ComponentContext) deactivation}
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
                        for(ModelGraph mg : initModels.values()){
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
    public SingleTdbDatasetTcProvider(){}
    
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
    public SingleTdbDatasetTcProvider(Dictionary<String,Object> config) throws ConfigurationException, IOException{
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
     * {@link #SingleTdbDatasetTcProvider(Dictionary)} - to be used outside
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
        log.info("Activating singe Dataset TDB provider");
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
        } else { //weight not defined
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
        dataset = TDBFactory.createDataset(dataDir.getAbsolutePath());
        //init the read/write lock
        
        //init the graph config (stores the graph and mgraph names in a config file)
        initGraphConfigs(dataDir,config);
        
        //finally ensure the the defualtGraphName is not also used as a graph/mgraph name
        if(graphNames.contains(defaultGraphName)){
            throw new ConfigurationException(DEFAULT_GRAPH_NAME, "The configured default graph name '"
                +defaultGraphName+"' is also used as a Graph name!");
        }
        if(mGraphNames.contains(defaultGraphName)){
            throw new ConfigurationException(DEFAULT_GRAPH_NAME, "The configured default graph name '"
                +defaultGraphName+"' is also used as a MGraph name!");
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
        if(dataset != null){ //avoid NPE on multiple calls
            datasetLock.writeLock().lock();
            try {
                for(ModelGraph mg : initModels.values()){
                    mg.close(); //close also syncs!
                }
                TDB.sync(dataset);
                dataset.close();
                dataset = null;
            } finally {
            	datasetLock.writeLock().unlock();
            }
        }
        if(syncThread != null){
            syncThread.requestStop();
            syncThread = null;
        }
        initModels = null;
        graphConfigFile = null;
        graphNames = null;
        mGraphConfigFile = null;
        mGraphNames = null;
    }
    
    /**
     * Internal method used to retrieve an existing Jena {@link ModelGraph} 
     * instance from {@link #initModels} or initializes a new Jena TDB {@link Model}
     * and Clerezza {@link Graph}s/{@link MGraph}s.
     * @param name the name of the Graph to initialize/create
     * @param readWrite if <code>true</code> a {@link MGraph} is initialized.
     * Otherwise a {@link Graph} is created.
     * @param create if this method is allowed to create an new {@link Model} or
     * if an already existing model is initialized.
     * @return the initialized {@link Model} and @link Graph} or {@link MGraph}.
     * The returned instance will be also cached in {@link #initModels}. 
     * @throws NoSuchEntityException If <code>create == false</code> and no
     * {@link Model} for the parsed <code>name</code> exists.
     */
    private ModelGraph getModelGraph(UriRef name, boolean readWrite,boolean create) throws NoSuchEntityException {
        ModelGraph modelGraph;
        datasetLock.readLock().lock();
        try {
            modelGraph = initModels.get(name);
            if(modelGraph != null && create){
                throw new EntityAlreadyExistsException(name);
            } else if(modelGraph == null){
                String modelName = name.getUnicodeString();
                modelGraph = new ModelGraph(name.equals(defaultGraphName) ? 
                        dataset.getNamedModel("urn:x-arq:UnionGraph") : 
                            dataset.getNamedModel(modelName),readWrite);
                this.initModels.put(name, modelGraph);
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
            if(graphNames.contains(name) || name.equals(defaultGraphName)){
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
            if(mGraphNames.contains(name)){
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
            if(graphNames.contains(name) || name.equals(defaultGraphName)){
                return getGraph(name);
            } else if(mGraphNames.contains(name)){
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
        HashSet<UriRef> names = new HashSet<UriRef>(graphNames);
        if(defaultGraphName != null){
            names.add(defaultGraphName);
        }
        return names;
    }
    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#listMGraphs()
     */
    @Override
    public Set<UriRef> listMGraphs() {
        Set<UriRef> tcNames = listTripleCollections();
        tcNames.removeAll(graphNames);
        if(defaultGraphName != null){
            tcNames.remove(defaultGraphName);
        }
        return tcNames;
    }
    /*
     * (non-Javadoc)
     * @see org.apache.clerezza.rdf.core.access.TcProvider#listTripleCollections()
     */
    @Override
    public Set<UriRef> listTripleCollections() {
        Set<UriRef> graphNames = new HashSet<UriRef>();
        datasetLock.readLock().lock();
        try {
            for(Iterator<String> names = dataset.listNames(); 
                names.hasNext();
                    graphNames.add(new UriRef(names.next())));
        } finally {
        	datasetLock.readLock().unlock();
        }
        if(defaultGraphName != null){
            graphNames.add(defaultGraphName);
        }
        return graphNames;
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
            if(graphNames.contains(name) || mGraphNames.contains(name) || name.equals(defaultGraphName)){
                throw new EntityAlreadyExistsException(name);
            }
            MGraph graph = getModelGraph(name,true,true).getMGraph();
            mGraphNames.add(name);
            try {
                writeMGraphConfig();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to wirte MGraphName config file '"
                        + mGraphConfigFile+"'!",e);
            }
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
            if(graphNames.contains(name) || mGraphNames.contains(name) || name.equals(defaultGraphName)){
                throw new EntityAlreadyExistsException(name);
            }
            mg = getModelGraph(name,false,true);
            graphNames.add(name);
            try {
                writeGraphConfig();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to wirte GraphName config file '"
                        + graphConfigFile+"'!",e);
            }
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
            if(mGraphNames.remove(name)){
                try {
                    writeMGraphConfig();
                } catch (IOException e){
                    mGraphNames.add(name);//make it consistent with the file
                    throw new IllegalStateException("Unable to wirte MGraphName config file '"
                            + graphConfigFile+"'!",e);
                }
                ModelGraph mg = getModelGraph(name, true, false);
                mg.delete();
            } else if(graphNames.remove(name)){
                try {
                    writeGraphConfig();
                } catch (IOException e){
                    graphNames.add(name); //make it consistent with the file
                    throw new IllegalStateException("Unable to wirte GraphName config file '"
                            + graphConfigFile+"'!",e);
                }
                ModelGraph mg = getModelGraph(name, false, false);
                mg.delete();
            } else if (name.equals(defaultGraphName)){
                throw new EntityUndeletableException(defaultGraphName);
            }
            //delete the graph from the initModels list
            initModels.remove(name);
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
        for(Entry<UriRef,ModelGraph> entry : initModels.entrySet()){
            if(graphNames.contains(entry.getKey()) && //avoid lazy initialisation of grpahs for MGraphs
                    graph.equals(entry.getValue().getGraph())){
                graphNames.add(entry.getKey());
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
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final byte[] lineSep = "\n".getBytes(UTF8);
    /**
     * Writes the configuration file storing the initialized {@link Graph} names
     * @throws IOException
     */
    private void writeGraphConfig() throws IOException {
        writeConfig(graphConfigFile,graphNames);
    }
    /**
     * Writes the configuration file storing the initialized {@link MGraph} names
     * @throws IOException
     */
    private void writeMGraphConfig() throws IOException {
        writeConfig(mGraphConfigFile,mGraphNames);
    }
    /**
     * Writes the parsed names to the parsed file. One name in each line.
     * TODO: This might not scale to millions of graphs. If this would be
     * necessary one should store {@link MGraph} and {@link Graph} names in the
     * {@link Dataset#getDefaultModel() default model} of the TDB 
     * {@link Dataset}.<br>
     * However for now I have decided against that, because this way makes it
     * more easy to manually play around with the configuration (such as
     * manually adding the name of an Graph/MGraph that already exists in
     * an pre-existing Dataset.
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void writeConfig(File file,Set<UriRef> names) throws IOException, FileNotFoundException {
        if(file.exists()){
            if(!file.delete()){
                throw new IOException("Unable to delete GraphConfigFile '"
                        + file.getAbsolutePath()+"'!");
            }
        }
        OutputStream out = new FileOutputStream(file);
        for (UriRef name : names) {
            out.write(name.getUnicodeString().getBytes(UTF8));
            out.write(lineSep);
        }
        out.close();
    }
    private void initGraphConfigs(File tdbDir,Dictionary<String,Object> config) throws IOException {
        
        graphConfigFile = new File(tdbDir,"tcprovider.graphnames");
        graphNames = new HashSet<UriRef>();
        boolean configPresent = readGraphConfig(graphConfigFile, graphNames);
        log.info("Present named Models");
        datasetLock.readLock().lock();
        try {
	        for(Iterator<String> it = dataset.listNames();it.hasNext();){
	            log.info(" > {}",it.next());
	        }
        } finally {
        	datasetLock.readLock().unlock();
        }
        if(configPresent) {
            //validate that all Graphs and MGraphs in the configFile also are 
            //also present in the Jena TDB dataset

            //NOTE: validation of graph model MUST BE deactivated, because
            //      Clerezza TcProvider needs to support the preservation of
            //      empty graphs!
//            synchronized (dataset) {
//                boolean modified = false;
//                Iterator<UriRef> it = graphNames.iterator();
//                while(it.hasNext()){
//                    String name = it.next().getUnicodeString();
//                    if(!(dataset.containsNamedModel(name) ||
//                            dataset.containsNamedModel(name+GRAPH_NAME_SUFFIX))){
//                        log.info("Remove GraphName {} form GrpahNameConfig because " +
//                                "it is not part of the TDB dataset",name);
//                        it.remove();
//                        modified = true;
//                    }
//                }
//                if(modified){
//                    writeGraphConfig();
//                }
//            }
        } else {
            writeGraphConfig();
        }
        mGraphConfigFile = new File(tdbDir,"tcprovider.mgraphnames");
        mGraphNames = new HashSet<UriRef>();
        configPresent = readGraphConfig(mGraphConfigFile, mGraphNames);
        if(configPresent) {
            //validate that all Graphs and MGraphs in the configFile also are 
            //also present in the Jena TDB dataset

            //NOTE: validation of graph model MUST BE deactivated, because
            //      Clerezza TcProvider needs to support the preservation of
            //      empty graphs!
//            synchronized (dataset) {
//                boolean modified = false;
//                Iterator<UriRef> it = mGraphNames.iterator();
//                while(it.hasNext()){
//                    String name = it.next().getUnicodeString();
//                    if(!(dataset.containsNamedModel(name) ||
//                            dataset.containsNamedModel(name+MGRAPH_NAME_SUFFIX))){
//                        log.info("Remove MGraphName {} form GrpahNameConfig because " +
//                                "it is not part of the TDB dataset",name);
//                        it.remove();
//                        modified = true;
//                    }
//                }
//                if(modified){
//                    writeMGraphConfig();
//                }
//            }
        } else { //read pre-existing models in the dataset
            datasetLock.readLock().lock();
            try {
                for(Iterator<String> it = dataset.listNames();it.hasNext();){
                    mGraphNames.add(new UriRef(it.next()));
                }
                writeMGraphConfig();
            } finally {
            	datasetLock.readLock().unlock();
            }
        }
    }
    /**
     * Read a graph configuration file.
     * @throws FileNotFoundException
     * @throws IOException
     * @see #writeGraphConfig()
     */
    private static boolean readGraphConfig(File file,Set<UriRef> names) throws FileNotFoundException, IOException {
        if(file.exists()){ //read existing
            if(!file.isFile()){
                throw new IllegalStateException("Graph name configuration file '"
                    + file.getAbsolutePath()+"' exsits but is not of type File!");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file),UTF8));
            String line = reader.readLine();
            while (line != null) {
                names.add(new UriRef(line));
                line = reader.readLine();
            }
            reader.close();
            return true;
        } else {// no config file indicates first initialisation
            return false;
        }
    }
    /**
     * {@link LockableMGraph} wrapper that uses a single {@link ReadWriteLock} for
     * the Jena TDB {@link SingleTdbDatasetTcProvider#dataset}
     * @author Rupert Westenthaler
     *
     */
    private class DatasetLockedMGraph implements LockableMGraph {

    	private final MGraph wrapped;

    	/**
    	 * Constructs a LocalbleMGraph for an MGraph.
    	 *
    	 * @param providedMGraph a non-lockable mgraph
    	 */
    	public DatasetLockedMGraph(final MGraph providedMGraph) {
    		this.wrapped = providedMGraph;
    	}

    	@Override
    	public ReadWriteLock getLock() {
    		return datasetLock;
    	}

    	@Override
    	public Graph getGraph() {
    		datasetLock.readLock().lock();
    		try {
    			return wrapped.getGraph();
    		} finally {
    			datasetLock.readLock().unlock();
    		}
    	}

    	@Override
    	public Iterator<Triple> filter(NonLiteral subject, UriRef predicate, Resource object) {
			//users will need to aquire a readlock while iterating
			return wrapped.filter(subject, predicate, object);
    	}

    	@Override
    	public int size() {
    		datasetLock.readLock().lock();
    		try {
    			return wrapped.size();
    		} finally {
    			datasetLock.readLock().unlock();
    		}
    	}

    	@Override
    	public boolean isEmpty() {
    		datasetLock.readLock().lock();
    		try {
    			return wrapped.isEmpty();
    		} finally {
    			datasetLock.readLock().unlock();
    		}
    	}

    	@Override
    	public boolean contains(Object o) {
    		datasetLock.readLock().lock();
    		try {
    			return wrapped.contains(o);
    		} finally {
    			datasetLock.readLock().unlock();
    		}
    	}

    	@Override
    	public Iterator<Triple> iterator() {
    		//users will need it acquire a read lock while iterating!
			return wrapped.iterator();
    	}

    	@Override
    	public Object[] toArray() {
    		datasetLock.readLock().lock();
    		try {
    			return wrapped.toArray();
    		} finally {
    			datasetLock.readLock().unlock();
    		}
    	}

    	@Override
    	public <T> T[] toArray(T[] a) {
    		datasetLock.readLock().lock();
    		try {
    			return wrapped.toArray(a);
    		} finally {
    			datasetLock.readLock().unlock();
    		}
    	}

    	@Override
    	public boolean containsAll(Collection<?> c) {
    		datasetLock.readLock().lock();
    		try {
    			return wrapped.containsAll(c);
    		} finally {
    			datasetLock.readLock().unlock();
    		}
    	}

    	@Override
    	public boolean add(Triple e) {
    		datasetLock.writeLock().lock();
    		try {
    			return wrapped.add(e);
    		} finally {
    			datasetLock.writeLock().unlock();
    		}
    	}

    	@Override
    	public boolean remove(Object o) {
    		datasetLock.writeLock().lock();
    		try {
    			return wrapped.remove(o);
    		} finally {
    			datasetLock.writeLock().unlock();
    		}
    	}

    	@Override
    	public boolean addAll(Collection<? extends Triple> c) {
    		datasetLock.writeLock().lock();
    		try {
    			return wrapped.addAll(c);
    		} finally {
    			datasetLock.writeLock().unlock();
    		}
    	}

    	@Override
    	public boolean removeAll(Collection<?> c) {
    		datasetLock.writeLock().lock();
    		try {
    			return wrapped.removeAll(c);
    		} finally {
    			datasetLock.writeLock().unlock();
    		}
    	}

    	@Override
    	public boolean retainAll(Collection<?> c) {
    		datasetLock.writeLock().lock();
    		try {
    			return wrapped.retainAll(c);
    		} finally {
    			datasetLock.writeLock().unlock();
    		}
    	}

    	@Override
    	public void clear() {
    		datasetLock.writeLock().lock();
    		try {
    			wrapped.clear();
    		} finally {
    			datasetLock.writeLock().unlock();
    		}
    	}

    	@Override
    	public void addGraphListener(GraphListener listener, FilterTriple filter, long delay) {
    		wrapped.addGraphListener(listener, filter, delay);
    	}

    	@Override
    	public void addGraphListener(GraphListener listener, FilterTriple filter) {
    		wrapped.addGraphListener(listener, filter);
    	}

    	@Override
    	public void removeGraphListener(GraphListener listener) {
    		wrapped.removeGraphListener(listener);
    	}

    	@Override
    	public int hashCode() {
    		return wrapped.hashCode();
    	}

    	@Override
    	public boolean equals(Object obj) {
    		if(obj instanceof DatasetLockedMGraph){
    			DatasetLockedMGraph other = (DatasetLockedMGraph) obj;
    			return wrapped.equals(other.wrapped);
    		} else {
    			return false;
    		}
    	}

    	@Override
    	public String toString() {
    		return wrapped.toString();
    	}    	
    }

}
