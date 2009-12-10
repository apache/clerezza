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

import java.util.List;

/**
 * Provides a method to send notifications (for example via E-Mail) in case the
 * {@link WebIntegrationTestFramework} terminates because of
 * <code>RuntimeExceptions</code> in running tests.
 * 
 * @author daniel
 * 
 */
public interface AbnormalTeminationNotificator {

	/**
	 * Send a notification about an abnormal termination.
	 * 
	 * @param exeptionDescriptionList
	 *            Holds all the <code>RuntimeExceptions</code>, 
	 *            the threads in which they occurred 
	 *            and the time after which they occurred. 
	 *            The first element is the exception that occurred first 
	 *            and caused the shutdown of the test framework.
	 */
	public void notifyAbnormalTermination(
			List<ExceptionDescription> exeptionDescriptionList);

}
