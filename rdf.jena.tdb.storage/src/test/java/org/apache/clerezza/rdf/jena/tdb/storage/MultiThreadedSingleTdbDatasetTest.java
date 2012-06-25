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
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import junit.framework.Assert;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.felix.scr.annotations.Activate;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;
import org.wymiwyg.commons.util.Util;


public class MultiThreadedSingleTdbDatasetTest {
	
	
	
	/** 
	 * how many threads to start
	 */
	private static final int THREAD_COUNT = 100;
	/**
	 * how many seconds to let them run
	 */
	private static final int DELAY = 30;
	
	
	private MGraph mGraph;
	private Set<Triple> testTriples = Collections.synchronizedSet(new HashSet<Triple>()); 

	class TestThread extends Thread {

		private final int id;
		private boolean stopRequested;
		private int addedTripleCount = 0;

		public TestThread(final int id) {
			this.id = id;
			start();
		}

		public void requestStop() {
			stopRequested = true;
		}

		@Override
		public void run() {
			while (!stopRequested) {
				Literal randomLiteral = new PlainLiteralImpl(Util.createRandomString(22));
				Triple triple = new TripleImpl(new BNode(), new UriRef("http://example.com/property"), randomLiteral);
				mGraph.add(triple);
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
    public void createGraph(){
        this.mGraph = provider.createMGraph(new UriRef("http://www.example.org/multiThreadTest"));
    }
	@Test
	public void perform() throws InterruptedException {
		TestThread[] threads =  new TestThread[THREAD_COUNT];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new TestThread(i);
		}
		Thread.sleep(DELAY*1000);
		for (TestThread testThread : threads) {
			testThread.requestStop();
		}
		for (TestThread testThread : threads) {
			testThread.join();
		}
		int addedTriples = 0;
		for (TestThread testThread : threads) {
			addedTriples += testThread.getAddedTripleCount();
		}
		Assert.assertEquals(addedTriples, mGraph.size());
		for (Triple testTriple : testTriples) {
			Assert.assertTrue(mGraph.contains(testTriple));
		}
	}
    @AfterClass
    public static void cleanUpDirectory() {
        provider.deactivate(null);
        TdbTcProvider.delete(tempFile);
    }

}
