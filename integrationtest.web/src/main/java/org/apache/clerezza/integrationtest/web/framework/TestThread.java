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

/**
 *
 * @author hasan, reto
 * 
 * @since version 0.1
 */
class TestThread extends Thread {

	/**
	 * the WebTestCase whose run method is to be executed by this thread
	 */
	private WebTestCase wtc;
	/**
	 * the time required (in nanoseconds) to execute the run method of 
	 * a WebTestCase
	 */
	private long elapsedTime;
	private ResultsLogger resultsLogger;
	private boolean stopRequested = false;
	private long loopSleepTime; // ms
	private WebIntegrationTestFramework webIntegrationTestFramework;
	
	TestThread(String threadName, WebTestCase wtc, WebIntegrationTestFramework webIntegrationTestFramework, ResultsLogger resultsLogger,
			long loopSleepTime) {
		super(threadName);
		this.wtc = wtc;
		this.webIntegrationTestFramework = webIntegrationTestFramework;
		this.resultsLogger = resultsLogger;
		this.loopSleepTime = loopSleepTime;
	}

	/**
	 * this method repeatedly executes the run method of the registered 
	 * WebTestCase and measures the elapsed time.
	 * repetition is done until stop is requested.
	 * 
	 * @see WebTestCase#run()
	 * @see #requestStop()
	 */
	@Override
	public void run() {
        try {
            while (!stopRequested) {
                long startTime = System.nanoTime();
                try {
                    wtc.run();
                    webIntegrationTestFramework.incrementIterationCounter();
                } catch (RuntimeException ex) {
                    elapsedTime = System.nanoTime() - startTime;
                    resultsLogger.logException(elapsedTime, this, ex);
                    webIntegrationTestFramework.requestThreadsStop();
                    break;
                }
                elapsedTime = System.nanoTime() - startTime;
                resultsLogger.logResult(elapsedTime, this);

                try {
                    sleep(loopSleepTime);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            webIntegrationTestFramework.notifyThreadFinishing(this);
        }
	}

	/**
	 * this method is used to stop this thread
	 * 
	 * @see #run() 
	 */
	void requestStop() {
		stopRequested = true;
	}
}
