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
package org.apache.clerezza.integrationtest.web.framework;

import java.util.HashSet;
import java.util.Set;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of this class is responsible for running all {@link WebTestCase}S
 * which implement the service as referred below.
 * 
 * @scr.component
 * @scr.reference name="WebTestCase"
 *		cardinality="1..n" policy="static"
 *		interface="org.apache.clerezza.integrationtest.web.framework.WebTestCase"
 * 
 * @author hasan, reto
 * 
 * @since version 0.1
 */
public class WebIntegrationTestFramework {

	private Set<ServiceReference> boundTestCases = new HashSet<ServiceReference>();
	private Set<TestThread> allTestThreads = new HashSet<TestThread>();
	private Set<TestThread> runningTestThreads = new HashSet<TestThread>();
	private ResultsLogger resultsLogger;
	/**
	 * Service property
	 * 
	 * @scr.property value="http://localhost:8080"
	 *	description="Specifies the URI prefix of the subject under test."
	 */
	public static final String SUBJECT_UNDER_TEST = "subjectUnderTest";
	/**
	 * Service property
	 * 
	 * @scr.property value="testresults"
	 *	description="Specifies the directory to place result log files."
	 */
	public static final String TEST_RESULTS_DIR = "testResultsDirectory";
	/**
	 * Service property
	 * 
	 * @scr.property type="Long" value="0"
	 *	description="Specifies the break time in ms before executing the next iteration."
	 */
	public static final String ITER_BREAK_TIME = "iterationBreakTime";
	
	/**
	 * Service property
	 * 
	 * @scr.property type="Long" value="100"
	 *	description="Specifies after how many iterations the Garbage Collection is run."
	 */
	public static final String GC_FREQUENCY = "gcFrequency";
	private long gcFrequency = 100;
	private long iterationCounter = 0;
	
	/**
	 * @scr.reference cardinality=0..1
	 */
	AbnormalTeminationNotificator notificator;
	final Logger logger = LoggerFactory.getLogger(WebIntegrationTestFramework.class);

	synchronized protected void incrementIterationCounter() {
		if(++iterationCounter == gcFrequency) {
			iterationCounter = 0;
			System.gc();
		}
	}
	
	protected void bindWebTestCase(ServiceReference serviceReference) {
		logger.info("Binding {}", serviceReference.toString());
		boundTestCases.add(serviceReference);
	}

	protected void unbindWebTestCase(ServiceReference serviceReference) {
		logger.info("Unbinding {}", serviceReference.toString());
		boundTestCases.remove(serviceReference);
	}

	protected void activate(ComponentContext componentContext) {
		logger.info("Activating WebIntegrationTestFramework");
		final String resultsDirectory = (String) componentContext.getProperties().get(TEST_RESULTS_DIR);
		resultsLogger = new ResultsLogger(resultsDirectory, notificator);
		final String subjectUnderTest = (String) componentContext.getProperties().get(SUBJECT_UNDER_TEST);

		final long iterationBreakTime = (Long) componentContext.getProperties().get(ITER_BREAK_TIME);
		
		gcFrequency = (Long) componentContext.getProperties().get(GC_FREQUENCY);
		
		for (ServiceReference serviceRef : boundTestCases) {
			WebTestCase wtc = (WebTestCase) componentContext.locateService("WebTestCase", serviceRef);
			wtc.init(subjectUnderTest);

			String testCaseName = wtc.getClass().getSimpleName();
			int threadCount = 1;
			if (wtc.multiThreadingCapable()) {
				threadCount = (Integer) serviceRef.getProperty("threadCount");
			}
			logger.info("WebIntegrationTestFramework: {} threads to execute {}", threadCount, testCaseName);
			for (int i = 0; i < threadCount; i++) {
				TestThread testCaseThread =
						new TestThread(testCaseName + "-" + i, wtc, this, resultsLogger,
						iterationBreakTime);
				allTestThreads.add(testCaseThread);
			}
		}
		for (TestThread th : allTestThreads) {
			th.start();
			runningTestThreads.add(th);
		}
	}

	protected void deactivate(ComponentContext componentContext) {
		logger.info("Deactivating WebIntegrationTestFramework");
		requestThreadsStop();
		for (TestThread th : allTestThreads) {
			try {
				th.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	void notifyThreadFinishing(TestThread thread) {
		runningTestThreads.remove(thread);
		if (runningTestThreads.size() == 0) {
			resultsLogger.close();
		}
	}

	void requestThreadsStop() {
		for (TestThread th : allTestThreads) {
			th.requestStop();
		}
	}
}
