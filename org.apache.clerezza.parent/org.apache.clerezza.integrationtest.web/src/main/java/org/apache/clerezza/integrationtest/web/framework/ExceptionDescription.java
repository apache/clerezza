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
 * Holds Exception occurred in {@link TestThread} instances along with
 * additional information.
 * 
 * @author daniel
 * 
 */
public final class ExceptionDescription {

	private final long timeInNanos;
	private final TestThread testThread;
	private final RuntimeException exception;

	/**
	 * Constructor.
	 * 
	 * @param timeInNanos
	 *            The time the test iteration was running until the
	 *            <code>RuntimeException</code> occurred.
	 * @param testThread
	 *            The TestThread that the exception occurred in.
	 * @param exception
	 *            The exception that occurred.
	 */
	public ExceptionDescription(long timeInNanos, TestThread testThread,
			RuntimeException exception) {

		this.timeInNanos = timeInNanos;
		this.testThread = testThread;
		this.exception = exception;
	}

	/**
	 * Returns the time the test iteration was running until the exception
	 * occurred.
	 * 
	 * @return the time in nano-seconds.
	 */
	public long getTimeInNanos() {
		return timeInNanos;
	}

	/**
	 * Returns the thread in which the exception occurred.
	 * 
	 * @return the thread instance.
	 */
	public TestThread getTestThread() {
		return testThread;
	}

	/**
	 * Returns the <code>RuntimeException</code> that occurred.
	 * 
	 * @return the exception instance.
	 */
	public RuntimeException getException() {
		return exception;
	}
}