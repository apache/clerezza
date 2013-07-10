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
package org.apache.clerezza.rdf.jena.tdb.storage;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.block.FileMode;
import com.hp.hpl.jena.tdb.sys.SystemTDB;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.LockableMGraphWrapper;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedMGraphWrapper;
import org.apache.clerezza.rdf.jena.storage.JenaGraphAdaptor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

/**
 * A {@link org.apache.clerezza.rdf.core.access.WeightedTcProvider} based on Jena TDB.
 *
 * @see <a href="http://jena.hpl.hp.com/wiki/TDB/JavaAPI">
 * TDB/JavaAPI</a>
 *
 * @author reto, hasan
 *
 * @scr.component immediate="true"
 * @scr.service interface="org.apache.clerezza.rdf.core.access.WeightedTcProvider"
 * @scr.property name="weight" type="Integer" value="105"
 *
 */
@Component(metatype=true, immediate=true)
@Service(WeightedTcProvider.class)
@Property(name="weight", intValue=105)
public class TdbTcProvider implements WeightedTcProvider {

    static {
        //not sure what the perfomance implication of this is
        //it is only needed so that on windows the files of a dataset can be deleteds
        SystemTDB.setFileMode(FileMode.direct);
    }
    
    @Property(intValue=6, description="Specifies the number of seconds to wait "
    + "between synchronizations of the TDB datasets to the filesystem")
    public static final String SYNC_INTERVAL = "sync-interval";
    private int syncInterval = 6;

    /**
     *    directory where all graphs are stored
     */
    private static final String DATA_PATH_NAME = "tdb-data/";
    private String dataPathString = DATA_PATH_NAME;
    private Map<UriRef, LockableMGraph> mGraphMap = new HashMap<UriRef, LockableMGraph>();
    private Map<UriRef, Graph> graphMap = new HashMap<UriRef, Graph>();
    private Map<File, com.hp.hpl.jena.graph.Graph> dir2JenaGraphMap =
            new HashMap<File, com.hp.hpl.jena.graph.Graph>();
    private final Map<File, Dataset> dir2Dataset = new HashMap<File, Dataset>();
    private static final Logger log = LoggerFactory.getLogger(TdbTcProvider.class);
    private int weight = 105;
    
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
                    syncWithFileSystem();
                }
            }
        }
        
        public void requestStop() {
            stopRequested = true;
        }
    }

    private SyncThread syncThread;

    public TdbTcProvider() {
    }

    TdbTcProvider(File directory) {
        dataPathString = directory.getAbsolutePath();
        loadMGraphs();
        loadGraphs();
    }

    public void activate(ComponentContext cCtx) {
        log.info("Activating TDB provider");
        if (cCtx != null) {
            weight = (Integer) cCtx.getProperties().get("weight");
            dataPathString = cCtx.getBundleContext().
                    getDataFile(DATA_PATH_NAME).getAbsolutePath();
            syncInterval = Integer.parseInt(cCtx.getProperties().get(SYNC_INTERVAL).toString());
        }
        loadMGraphs();
        loadGraphs();
        syncThread = new SyncThread();
        syncThread.start();
    }

    public void deactivate(ComponentContext cCtx) {
        syncThread.requestStop();
        syncThread = null;
        for (com.hp.hpl.jena.graph.Graph jenaGraph : dir2JenaGraphMap.values()) {
            jenaGraph.close();
        }
        synchronized(dir2Dataset) {
            for (Dataset dataset : dir2Dataset.values()) {
                dataset.close();
            }
        }
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public Graph getGraph(UriRef name) throws NoSuchEntityException {
        if (!graphMap.containsKey(name)) {
            throw new NoSuchEntityException(name);
        }
        return graphMap.get(name);
    }

    @Override
    public synchronized MGraph getMGraph(UriRef name) throws NoSuchEntityException {
        if (!mGraphMap.containsKey(name)) {
            throw new NoSuchEntityException(name);
        }
        return mGraphMap.get(name);
    }

    @Override
    public TripleCollection getTriples(UriRef name) throws NoSuchEntityException {
        try {
            return getMGraph(name);
        } catch (NoSuchEntityException e) {
            return getGraph(name);
        }
    }

    @Override
    public synchronized MGraph createMGraph(UriRef name)
            throws UnsupportedOperationException, EntityAlreadyExistsException {
        File tcDir = getMGraphDir(name);
        if (tcDir.exists()) {
            throw new EntityAlreadyExistsException(name);
        }
        tcDir.mkdirs();
        File otimizationIndicator = new File(tcDir, "fixed.opt");
        try {
            otimizationIndicator.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        LockableMGraph result = new LockableMGraphWrapper(getMGraph(tcDir));
        mGraphMap.put(name, result);
        return result;
    }

    @Override
    public Graph createGraph(UriRef name, TripleCollection triples)
            throws UnsupportedOperationException, EntityAlreadyExistsException {
        File tcDir = getGraphDir(name);
        if (tcDir.exists()) {
            throw new EntityAlreadyExistsException(name);
        }

        if (triples == null) {
            triples = new SimpleMGraph();
        }
        tcDir.mkdirs();
        File otimizationIndicator = new File(tcDir, "fixed.opt");
        try {
            otimizationIndicator.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        MGraph mGraph = getMGraph(tcDir);
        mGraph.addAll(triples);
        Graph result = mGraph.getGraph();
        
        graphMap.put(name, result);
        return result;
    }

    @Override
    public void deleteTripleCollection(UriRef name)
            throws UnsupportedOperationException, NoSuchEntityException,
            EntityUndeletableException {
        syncWithFileSystem();
        if (deleteTcDir(getGraphDir(name))) {
            graphMap.remove(name);
            return;
        }
        if (deleteTcDir(getMGraphDir(name))) {
            mGraphMap.remove(name);
            return;
        }
        throw new NoSuchEntityException(name);
    }

    private boolean deleteTcDir(File tcDir) {
        if (tcDir.exists()) {
            dir2JenaGraphMap.get(tcDir).close();
            dir2JenaGraphMap.remove(tcDir);
            synchronized(dir2Dataset) {
                dir2Dataset.get(tcDir).close();
                dir2Dataset.remove(tcDir);
            }
            try {
                delete(tcDir);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Cleans the content of the specified directory recursively.
     * @param dir  Abstract path denoting the directory to clean.
     */
    private static void cleanDirectory(File dir) throws IOException {
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
    protected static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            cleanDirectory(file);
        }
        //better but only in java 7
        //Files.delete(file.toPath());
        if (!file.delete()) {
            throw new IOException("couldn't delete "+file.getAbsolutePath());
        }
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
        return graphMap.keySet();
    }

    @Override
    public Set<UriRef> listMGraphs() {
        return mGraphMap.keySet();
    }

    private Graph getGraph(File tcDir) {
        return getMGraph(tcDir).getGraph();
    }

    private File getGraphDir(UriRef name) {
        File base = new File(dataPathString);
        return getTcDir(new File(base, "graph"), name);
    }

    private MGraph getMGraph(File tcDir) {
        Dataset dataset = TDBFactory.createDataset(tcDir.getAbsolutePath());
        Model model = dataset.getDefaultModel();
        //Model model = TDBFactory.createModel(tcDir.getAbsolutePath());
        final com.hp.hpl.jena.graph.Graph jenaGraph = model.getGraph();
        dir2JenaGraphMap.put(tcDir, jenaGraph);
        synchronized(dir2Dataset) {
            dir2Dataset.put(tcDir, dataset);
        }
        return new PrivilegedMGraphWrapper(new JenaGraphAdaptor(jenaGraph));
    }

    private File getMGraphDir(UriRef name) {
        File base = new File(dataPathString);
        return getTcDir(new File(base, "mgraph"), name);
    }

    private File getTcDir(File directory, UriRef name) {
        try {
            String subDirName = URLEncoder.encode(name.getUnicodeString(), "utf-8");
            return new File(directory, subDirName);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("utf-8 not supported", ex);
        }
    }


    private void loadGraphs() {
        File graphsDir = new File(new File(dataPathString), "graph");
        if (graphsDir.exists()) {
            for (String graphDirName : graphsDir.list()) {
                try {
                    UriRef uri = new UriRef(URLDecoder.decode(graphDirName, "utf-8"));
                    log.info("loading: "+graphDirName);
                    graphMap.put(uri, getGraph(new File(graphsDir, graphDirName)));
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("utf-8 not supported", ex);
                } catch (Exception e) {
                    log.error("Could not load tdb graph in "+graphDirName, e);
                }
            }
        }
    }

    private void loadMGraphs() {
        File mGraphsDir = new File(new File(dataPathString), "mgraph");
        if (mGraphsDir.exists()) {
            for (String mGraphDirName : mGraphsDir.list()) {
                try {
                    UriRef uri = new UriRef(URLDecoder.decode(mGraphDirName, "utf-8"));
                    log.info("loading: "+mGraphDirName);
                    mGraphMap.put(uri, new LockableMGraphWrapper(getMGraph(new File(mGraphsDir, mGraphDirName))));
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("utf-8 not supported", ex);
                } catch (Exception e) {
                    log.error("Could not load tdb graph in "+mGraphDirName, e);
                }
            }
        }
    }
    
    public void syncWithFileSystem() {
        synchronized(dir2Dataset) {
            for (Dataset dataset : dir2Dataset.values()) {
                TDB.sync(dataset);
            }
        }
    }
}
