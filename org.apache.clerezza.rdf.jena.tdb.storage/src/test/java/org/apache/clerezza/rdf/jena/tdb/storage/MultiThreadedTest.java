package org.apache.clerezza.rdf.jena.tdb.storage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
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
import org.junit.Before;
import org.junit.Test;
import org.wymiwyg.commons.util.Util;


public class MultiThreadedTest {
	
	
	
	/** 
	 * how many threads to start
	 */
	private static final int THREAD_COUNT = 100;
	/**
	 * how many seconds to let them run
	 */
	private static final int DELAY = 120;
	
	
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

	
	
	@Before
	public void setUp() throws IOException {
		File tempFile;
		UriRef MGRAPHNAME = new UriRef("http://text.example.org/");
		TdbTcProvider tdbTcProvider;
		tempFile = File.createTempFile("tdbtest", null);
		tempFile.delete();
		tempFile.mkdirs();
		tdbTcProvider = new TdbTcProvider(tempFile);
		tdbTcProvider.activate(null);
		mGraph = tdbTcProvider.createMGraph(MGRAPHNAME);
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

}
