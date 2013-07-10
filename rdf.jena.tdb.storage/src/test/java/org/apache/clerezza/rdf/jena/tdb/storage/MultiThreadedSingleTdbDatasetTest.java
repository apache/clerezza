package org.apache.clerezza.rdf.jena.tdb.storage;
/*
 *
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
 *
*/


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import junit.framework.Assert;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wymiwyg.commons.util.Util;


public class MultiThreadedSingleTdbDatasetTest {
    
    private Logger log = LoggerFactory.getLogger(MultiThreadedSingleTdbDatasetTest.class);
    
    private static final String TEST_GRAPH_URI_PREFIX = "http://www.example.org/multiThreadTest";
    private int[] graphNum = new int[]{0};
    /** 
     * how many threads to start
     */
    private static final int TEST_THREAD_COUNT = 25;
    private static final int VALIDATE_THREAD_COUNT = 2;
    /**
     * how many seconds to let them run
     */
    private static final int DELAY = 15;
    
    
    protected final List<MGraph> mGraphs = new ArrayList<MGraph>();
    protected final List<Set<Triple>> testTriplesList = new ArrayList<Set<Triple>>();
    private Random random = new Random();

    class TestThread extends Thread {

        private boolean stopRequested;
        private int addedTripleCount = 0;

        public TestThread(final int id) {
            setName("Test Thread "+id);
            start();
        }

        public void requestStop() {
            stopRequested = true;
        }

        @Override
        public void run() {
            while (!stopRequested) {
                float r;
                synchronized (random) {
                    r = random.nextFloat();
                }
                MGraph graph;
                Set<Triple> testTriples;
                if(r > 0.995){
                    int num;
                    synchronized (graphNum) {
                        num = graphNum[0];
                        graphNum[0]++;
                    }
                    graph = provider.createMGraph(new UriRef(TEST_GRAPH_URI_PREFIX+num));
                    log.info(" ... creating the {}. Grpah", num+1);
                    testTriples = new HashSet<Triple>();
                    synchronized (mGraphs) {
                        mGraphs.add(graph);
                        testTriplesList.add(testTriples);
                    }
                } else { //map the range [0..0.995] to the mGraphs
                    synchronized (mGraphs) {
                        int num = Math.round(r*(float)(mGraphs.size()-1)/0.995f);
                        graph = mGraphs.get(num);
                        testTriples = testTriplesList.get(num);
                    }
                }
                Literal randomLiteral = new PlainLiteralImpl(Util.createRandomString(22));
                Triple triple = new TripleImpl(new BNode(), new UriRef("http://example.com/property"), randomLiteral);
                graph.add(triple);
                addedTripleCount++;
                if ((addedTripleCount % 100) == 0) {
                    testTriples.add(triple);
                }
            }
        }

        public int getAddedTripleCount() {
            return addedTripleCount;
        }

    }
    /**
     * Iterates over max. the first 10 triples of a Graph
     * while acquiring a read lock on the graph.
     * @author westei
     *
     */
    class ValidatorThread extends Thread {
        
        boolean stopRequested = false;
        
        public ValidatorThread(int id) {
            setName("Validator Thread "+id);
            start();
        }
        
        public void requestStop() {
            stopRequested = true;
        }

        @Override
        public void run() {
            while (!stopRequested) {
                float r;
                synchronized (random) {
                    r = random.nextFloat();
                }
                int num = Math.round(r*(float)(mGraphs.size()-1));
                LockableMGraph graph;
                synchronized (mGraphs) {
                    graph = (LockableMGraph)mGraphs.get(num);
                }
                int elem = 0;
                graph.getLock().readLock().lock();
                try {
                    Iterator<Triple> it = graph.iterator();
                    while(it.hasNext() && elem < 10){
                        elem++;
                        it.next();
                    }
                } finally {
                    graph.getLock().readLock().unlock();
                }
                //iterate inly every 200ms
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
        
        
        
    }

    
    
    private static File tempFile;
    private static Dictionary<String,Object> config;
    private static SingleTdbDatasetTcProvider provider;
    @BeforeClass
    public static void setup() throws IOException, ConfigurationException {
        tempFile = File.createTempFile("tdbdatasettest", null);
        tempFile.delete();
        tempFile.mkdirs();
        config = new Hashtable<String,Object>();
        config.put(SingleTdbDatasetTcProvider.TDB_DIR, tempFile.getAbsolutePath());
        provider = new SingleTdbDatasetTcProvider(config);
    }
    @Before
    public void createGraphs(){
        mGraphs.add(provider.createMGraph(new UriRef(TEST_GRAPH_URI_PREFIX+graphNum[0])));
        testTriplesList.add(new HashSet<Triple>());
        graphNum[0]++;
        mGraphs.add(provider.createMGraph(new UriRef(TEST_GRAPH_URI_PREFIX+graphNum[0])));
        testTriplesList.add(new HashSet<Triple>());
        graphNum[0]++;
    }
    @Test
    public void perform() throws InterruptedException {
        TestThread[] threads =  new TestThread[TEST_THREAD_COUNT];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new TestThread(i);
        }
        ValidatorThread[] validators = new ValidatorThread[VALIDATE_THREAD_COUNT];
        for(int i = 0; i < validators.length; i++) {
            validators [i] = new ValidatorThread(i);
        }
        Thread.sleep(DELAY*1000);
        for (TestThread testThread : threads) {
            testThread.requestStop();
        }
        for (ValidatorThread validator : validators) {
            validator.requestStop();
        }
        for (TestThread testThread : threads) {
            testThread.join();
        }
        for (ValidatorThread validator : validators) {
            validator.join();
        }
        int addedTriples = 0;
        for (TestThread testThread : threads) {
            addedTriples += testThread.getAddedTripleCount();
        }
        int graphTriples = 0;
        log.info("Test created {} graphs with {} triples", mGraphs.size(), addedTriples);
        for(int i = 0;i < mGraphs.size(); i++){
            MGraph graph = mGraphs.get(i);
            graphTriples += graph.size();
            log.info("  > Grpah {}: {} triples",i,graph.size());
            for (Triple testTriple : testTriplesList.get(i)) {
                Assert.assertTrue(graph.contains(testTriple));
            }
        }
        Assert.assertEquals(addedTriples, graphTriples);
    }
    @AfterClass
    public static void cleanUpDirectory() throws IOException {
        provider.deactivate(null);
        TdbTcProvider.delete(tempFile);
    }

}
