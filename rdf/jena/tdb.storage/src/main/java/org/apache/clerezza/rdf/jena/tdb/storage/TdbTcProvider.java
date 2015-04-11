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
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedGraphWrapper;
import org.apache.clerezza.rdf.jena.storage.JenaGraphAdaptor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

/**
 * A {@link org.apache.clerezza.rdf.core.access.WeightedTcProvider} based on
 * Jena TDB.
 *
 * @see <a href="http://jena.hpl.hp.com/wiki/TDB/JavaAPI">
 * TDB/JavaAPI</a>
 *
 * @author reto, hasan
 *
 *
 */
@Component(metatype = true, immediate = true)
@Service(WeightedTcProvider.class)
@Properties({
    @Property(name = "weight", intValue = 107),
    @Property(name = TcManager.GENERAL_PURPOSE_TC, boolValue = true)})
public class TdbTcProvider implements WeightedTcProvider {

    static {
        //not sure what the perfomance implication of this is
        //it is only needed so that on windows the files of a dataset can be deleteds
        SystemTDB.setFileMode(FileMode.direct);
    }
    @Property(intValue = 6, description = "Specifies the number of seconds to wait "
            + "between synchronizations of the TDB datasets to the filesystem")
    public static final String SYNC_INTERVAL = "sync-interval";
    private int syncInterval = 6;
    /**
     * directory where all graphs are stored
     */
    private static final String DATA_PATH_NAME = "tdb-data/";
    private String dataPathString = DATA_PATH_NAME;
    private Map<IRI, Graph> mGraphMap = new HashMap<IRI, Graph>();
    private Map<IRI, ImmutableGraph> graphMap = new HashMap<IRI, ImmutableGraph>();
    private Map<File, com.hp.hpl.jena.graph.Graph> dir2JenaGraphMap = new HashMap<File, com.hp.hpl.jena.graph.Graph>();
    private Map<File, Lock> dir2Lock = new HashMap<File, Lock>();
    private final Map<File, Dataset> dir2Dataset = new HashMap<File, Dataset>();
    private static final Logger log = LoggerFactory.getLogger(TdbTcProvider.class);
    private int weight = 107;

    class SyncThread extends Thread {

        private boolean stopRequested = false;

        @Override
        public void run() {
            while (!stopRequested) {
                try {
                    Thread.sleep(syncInterval * 1000);
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
        loadGraphs();
        loadImmutableGraphs();
    }

    public void activate(ComponentContext cCtx) {
        log.info("Activating TDB provider");
        if (cCtx != null) {
            weight = (Integer) cCtx.getProperties().get("weight");
            dataPathString = cCtx.getBundleContext().
                    getDataFile(DATA_PATH_NAME).getAbsolutePath();
            syncInterval = Integer.parseInt(cCtx.getProperties().get(SYNC_INTERVAL).toString());
        }
        loadGraphs();
        loadImmutableGraphs();
        syncThread = new SyncThread();
        syncThread.start();
    }

    public void deactivate(ComponentContext cCtx) {
        syncThread.requestStop();
        syncThread = null;
        for (com.hp.hpl.jena.graph.Graph jenaGraph : dir2JenaGraphMap.values()) {
            jenaGraph.close();
        }
        synchronized (dir2Dataset) {
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
    public ImmutableGraph getImmutableGraph(IRI name) throws NoSuchEntityException {
        if (!graphMap.containsKey(name)) {
            throw new NoSuchEntityException(name);
        }
        return graphMap.get(name);
    }

    @Override
    public synchronized Graph getMGraph(IRI name) throws NoSuchEntityException {
        if (!mGraphMap.containsKey(name)) {
            throw new NoSuchEntityException(name);
        }
        return mGraphMap.get(name);
    }

    @Override
    public Graph getGraph(IRI name) throws NoSuchEntityException {
        try {
            return getMGraph(name);
        } catch (NoSuchEntityException e) {
            return getImmutableGraph(name);
        }
    }

    @Override
    public synchronized Graph createGraph(IRI name)
            throws UnsupportedOperationException, EntityAlreadyExistsException {
        File tcDir = getGraphDir(name);
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
        Graph result = getGraph(tcDir);
        dir2Lock.put(tcDir, result.getLock().writeLock());
        mGraphMap.put(name, result);
        return result;
    }

    @Override
    public ImmutableGraph createImmutableGraph(IRI name, Graph triples)
            throws UnsupportedOperationException, EntityAlreadyExistsException {
        File tcDir = getImmutableGraphDir(name);
        if (tcDir.exists()) {
            throw new EntityAlreadyExistsException(name);
        }

        if (triples == null) {
            triples = new SimpleGraph();
        }
        tcDir.mkdirs();
        File otimizationIndicator = new File(tcDir, "fixed.opt");
        try {
            otimizationIndicator.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        Graph mGraph = getGraph(tcDir);
        mGraph.addAll(triples);
        ImmutableGraph result = mGraph.getImmutableGraph();
        TDB.sync(dir2Dataset.get(tcDir));
        graphMap.put(name, result);
        return result;
    }

    @Override
    public void deleteGraph(IRI name)
            throws UnsupportedOperationException, NoSuchEntityException,
            EntityUndeletableException {
        syncWithFileSystem();
        if (deleteTcDir(getImmutableGraphDir(name))) {
            graphMap.remove(name);
            return;
        }
        if (deleteTcDir(getGraphDir(name))) {
            mGraphMap.remove(name);
            return;
        }
        throw new NoSuchEntityException(name);
    }

    private boolean deleteTcDir(File tcDir) {
        if (tcDir.exists()) {
            dir2JenaGraphMap.get(tcDir).close();
            dir2JenaGraphMap.remove(tcDir);
            synchronized (dir2Dataset) {
                dir2Dataset.get(tcDir).close();
                dir2Dataset.remove(tcDir);
            }
            try {
                delete(tcDir);
            } catch (IOException ex) {
                for (int i = 0; i < 10; i++) {
                    try {
                        System.gc();
                        delete(tcDir);
                    } catch (IOException ex1) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex2) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                    return true;
                }
                throw new RuntimeException(ex);
            }
            return true;
        }
        return false;
    }

    /**
     * Cleans the content of the specified directory recursively.
     *
     * @param dir Abstract path denoting the directory to clean.
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
     *
     * @param file Abstract path denoting the file or directory to clean.
     */
    protected static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            cleanDirectory(file);
        }
        //better but only in java 7
        //java.nio.file.Files.delete(file.toPath());
        if (!file.delete()) {
            throw new IOException("couldn't delete " + file.getAbsolutePath());
        }
    }

    @Override
    public Set<IRI> getNames(ImmutableGraph graph) {
        //this could be done more efficiently with an index, could be done with
        //a MultiBidiMap (BidiMap allowing multiple keys for the same value)
        Set<IRI> result = new HashSet<IRI>();
        for (IRI name : listGraphs()) {
            if (getGraph(name).equals(graph)) {
                result.add(name);
            }
        }
        return result;
    }

    @Override
    public Set<IRI> listGraphs() {
        Set<IRI> result = new HashSet<IRI>();
        result.addAll(listMGraphs());
        result.addAll(listImmutableGraphs());
        return result;
    }

    @Override
    public Set<IRI> listMGraphs() {
        return graphMap.keySet();
    }

    @Override
    public Set<IRI> listImmutableGraphs() {
        return mGraphMap.keySet();
    }

    private ImmutableGraph getImmutableGraph(File tcDir) {
        return getGraph(tcDir).getImmutableGraph();
    }

    private File getImmutableGraphDir(IRI name) {
        File base = new File(dataPathString);
        return getTcDir(new File(base, "graph"), name);
    }

    private Graph getGraph(File tcDir) {
        Dataset dataset = TDBFactory.createDataset(tcDir.getAbsolutePath());
        Model model = dataset.getDefaultModel();
        //Model model = TDBFactory.createModel(tcDir.getAbsolutePath());
        final com.hp.hpl.jena.graph.Graph jenaGraph = model.getGraph();
        dir2JenaGraphMap.put(tcDir, jenaGraph);
        //dataset.
        synchronized (dir2Dataset) {
            dir2Dataset.put(tcDir, dataset);
        }
        return new PrivilegedGraphWrapper(new JenaGraphAdaptor(jenaGraph));
    }

    private File getGraphDir(IRI name) {
        File base = new File(dataPathString);
        return getTcDir(new File(base, "mgraph"), name);
    }

    private File getTcDir(File directory, IRI name) {
        try {
            String subDirName = URLEncoder.encode(name.getUnicodeString(), "utf-8");
            return new File(directory, subDirName);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("utf-8 not supported", ex);
        }
    }

    private void loadImmutableGraphs() {
        File graphsDir = new File(new File(dataPathString), "graph");
        if (graphsDir.exists()) {
            for (String graphDirName : graphsDir.list()) {
                try {
                    IRI uri = new IRI(URLDecoder.decode(graphDirName, "utf-8"));
                    log.info("loading: " + graphDirName);
                    graphMap.put(uri, getImmutableGraph(new File(graphsDir, graphDirName)));
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("utf-8 not supported", ex);
                } catch (Exception e) {
                    log.error("Could not load tdb graph in " + graphDirName, e);
                }
            }
        }
    }

    private void loadGraphs() {
        File mGraphsDir = new File(new File(dataPathString), "mgraph");
        if (mGraphsDir.exists()) {
            for (String mGraphDirName : mGraphsDir.list()) {
                try {
                    IRI uri = new IRI(URLDecoder.decode(mGraphDirName, "utf-8"));
                    log.info("loading: " + mGraphDirName);
                    final File tcDir = new File(mGraphsDir, mGraphDirName);
                    final Graph lockableGraph = getGraph(tcDir);
                    mGraphMap.put(uri, lockableGraph);
                    dir2Lock.put(tcDir, lockableGraph.getLock().writeLock());
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("utf-8 not supported", ex);
                } catch (Exception e) {
                    log.error("Could not load tdb graph in " + mGraphDirName, e);
                }
            }
        }
    }

    public void syncWithFileSystem() {
        synchronized (dir2Dataset) {
            for (Map.Entry<File, Dataset> entry : dir2Dataset.entrySet()) {
                Lock l = dir2Lock.get(entry.getKey());
                if (l == null) {
                    return;
                }
                l.lock();
                try {
                    TDB.sync(entry.getValue());
                } finally {
                    l.unlock();
                }
            }
        }
    }
}
