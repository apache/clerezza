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

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hasan, reto
 *
 * @since version 0.1
 */
public class ResultsLogger {

	AbnormalTeminationNotificator notificator;
	
	private List<ExceptionDescription> exeptionDescriptionList = new ArrayList<ExceptionDescription>(); 

	private PrintWriter printWriter;
	/**
	 * the prefix of report files' name
	 */
	private static final String REPORT_FILEPRFX = "testresults-";
	/**
	 * the suffix of report files' name
	 */
	private static final String REPORT_FILESUFX = ".txt";
	/**
	 * maps a TestThread to its statistics data
	 */
	private Map<TestThread, TestStatistics> testThreadStatistics = 
			new HashMap<TestThread, TestStatistics>();

	final Logger logger = LoggerFactory.getLogger(ResultsLogger.class);

	ResultsLogger(String resultsDirectory, AbnormalTeminationNotificator notificator) {
		this.notificator = notificator;
		printWriter = null;

		try {
			String index = getNextIndex(resultsDirectory);
			String fname = resultsDirectory + "/" + REPORT_FILEPRFX + index + ".txt";
			logger.info("ResultsLogger: test results stored into {}", fname);
			File f = new File(fname);
			if (!f.exists()) {
				f.createNewFile();
			}
			printWriter = new PrintWriter(new FileWriter(f, true));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * gets the next index to be used as part of the filename for storing 
	 * test results
	 * 
	 * @param resultsDirectory the directory for storing test results
	 * @return index for the new file to store test results
	 */
	private String getNextIndex(String resultsDirectory) {
		File dir = new File(resultsDirectory);

		FilenameFilter filter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(REPORT_FILEPRFX) &&
						name.endsWith(REPORT_FILESUFX);
			}
		};

		String[] children = dir.list(filter);

		if (children == null) { // Either dir does not exist or is not a directory or is not readable
			if (dir.isFile()) {
				throw new RuntimeException(resultsDirectory +
						" already exists as a file");
			}
			if (dir.exists()) {
				if (!dir.canRead()) {
					throw new RuntimeException("Cannot read directory " +
							resultsDirectory);
				}
			} else {
				if (!dir.mkdir()) {
					throw new RuntimeException("Cannot create directory " +
							resultsDirectory);
				}
			}
			return "1";
		} else {
			int max = 0;
			for (int i = 0; i < children.length; i++) {
				// Get filename of file
				String filename = children[i];
				String strIndex = filename.substring(REPORT_FILEPRFX.length(),
						filename.length() - REPORT_FILESUFX.length());
				int idx = Integer.parseInt(strIndex);
				if (max < idx) {
					max = idx;
				}
			}
			return String.valueOf(++max);
		}
	}

	/**
	 * logs results of a TestThread
	 * updates TestStatistics of this TestThread
	 * 
	 * @param timeInNanos elapsed time measured by the TestThread
	 * @param test the TestThread
	 */
	synchronized void logResult(long timeInNanos, TestThread test) {
		TestStatistics testStatistics = getTestStatistics(test);
		testStatistics.newTestValue(timeInNanos);

		printWriter.print(test.getName());
		printWriter.print("\t");
		printWriter.print(timeInNanos);
		printWriter.println("\tns");
		printWriter.flush();
	}
	
	synchronized void logException(long timeInNanos, TestThread testThread, RuntimeException exception) {
		exeptionDescriptionList.add(new ExceptionDescription(timeInNanos, testThread, exception));
	}

	/**
	 * returns an instance of a TestStatistics for the given TestThread
	 * creates a new instance if none has been assigned to this TestThread
	 * 
	 * @param test TestThread whose information is to be logged
	 * @return the TestStatistics assigned to this TestThread
	 */
	private TestStatistics getTestStatistics(TestThread test) {
		TestStatistics testStatistics = testThreadStatistics.get(test);
		if (testStatistics == null) {
			testStatistics = new TestStatistics();
			testThreadStatistics.put(test, testStatistics);
		}
		return testStatistics;
	}

	/**
	 * this method prints all TestStatistics and then close the writer
	 */
	void close() {
		Set<TestThread> testThreads = testThreadStatistics.keySet();
		TestStatistics testStatistics;
		printWriter.println("# Test Statistics:");
		for (TestThread tt : testThreads) {
			testStatistics = testThreadStatistics.get(tt);
			printWriter.print(tt.getName());
			printWriter.print("\t");
			printWriter.print(testStatistics.getMax());
			printWriter.print("\tns\t");
			printWriter.print(testStatistics.getMin());
			printWriter.print("\tns\t");
			printWriter.print(testStatistics.getSum() / testStatistics.getSize());
			printWriter.println("\tns");
		}
		if (exeptionDescriptionList.size() == 0) {
			printWriter.print("# Test regularly terminated at: ");
		} else {
			printWriter.print("# Test terminated after exception at: ");
			notifyAbnormalTemination();
		}
		printWriter.println(new Date());
		printExceptions();
		printWriter.close();
	}

	private void notifyAbnormalTemination() {
		if (notificator != null) {
			printWriter.println("Sending notification...");
			try {
				notificator.notifyAbnormalTermination(exeptionDescriptionList);
			} catch (RuntimeException ex) {
				printWriter.println();
				printWriter.println("###ERROR### Couldn't send notification:");
				ex.printStackTrace(printWriter);
				printWriter.println("###");
			}
		}
		
	}

	private void printExceptions() {
		for (ExceptionDescription exceptionDescription : exeptionDescriptionList) {
			printExceptionDescription(exceptionDescription);
		}
		
	}

	private void printExceptionDescription(
			ExceptionDescription exceptionDescription) {
		printWriter.print("The test-thread ");
		printWriter.print(exceptionDescription.getTestThread().getName());
		printWriter.print(" got a ");
		printWriter.print(exceptionDescription.getException().getClass().getName());
		printWriter.print(" after ");
		printWriter.print(exceptionDescription.getTimeInNanos());
		printWriter.println(" ns");
		exceptionDescription.getException().printStackTrace(printWriter);
		
	}
}
