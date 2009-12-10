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
package org.apache.clerezza.integrationtest.web.performance;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.integrationtest.web.framework.WebTestCase;

/**
 * @scr.component
 * @scr.service 
 *              interface="org.apache.clerezza.integrationtest.web.framework.WebTestCase"
 * 
 * @author daniel
 * 
 * @since version 0.1
 */
public class Get404 implements WebTestCase {

	/**
	 * Service property
	 * 
	 * @scr.property type="Integer" value="5" description=
	 *               "Specifies the number of threads to execute the run method."
	 */
	public static final String THREAD_COUNT = "threadCount";
	private String testSubjectUriPrefix;
	
	/**
	 * Service property
	 * 
	 * @scr.property value="admin" description=
	 *               "Specifies the user name used in the authorization header."
	 */
	public static final String USER_NAME = "user";
	private String username;

	/**
	 * Service property
	 * 
	 * @scr.property value="admin" description=
	 *               "Specifies the user password used in the authorization header."
	 */
	public static final String USER_PASSWORD = "password";
	private String password;

	final Logger logger = LoggerFactory.getLogger(Get404.class);

	protected void activate(ComponentContext componentContext) {
		username = (String) componentContext.getProperties().get(USER_NAME);
		password = (String) componentContext.getProperties().get(USER_PASSWORD);
	}
	
	@Override
	public void init(String testSubjectUriPrefix) {
		logger.info("Init Get404");
		this.testSubjectUriPrefix = testSubjectUriPrefix;
	}

	@Override
	public void run() {

		try {
			URL serverURL = new URL(testSubjectUriPrefix + "/foobar");
			HttpClient client = new HttpClient();

			client.getState().setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(username, password));
			HttpMethod method = new GetMethod(serverURL.toString());
			method.setRequestHeader("Accept", "*/*");
			method.setDoAuthentication(true);

			try {
				int responseCode = client.executeMethod(method);

				if (responseCode != HttpStatus.SC_NOT_FOUND) {
					throw new RuntimeException("Get404: unexpected "
							+ "response code: " + responseCode);
				}
			} finally {
				method.releaseConnection();
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean multiThreadingCapable() {
		return true;
	}
}
