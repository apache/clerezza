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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.integrationtest.web.framework.WebTestCase;

/**
 * This test uses the HTTP PUT method in each iteration to generate a resource on the tested
 * instance and uses the HTTP GET method to retrieve and check that resource.
 * 
 * @scr.component
 * @scr.service 
 *              interface="org.apache.clerezza.integrationtest.web.framework.WebTestCase"
 * 
 * @author daniel
 * 
 * @since version 0.1
 */
public class PutAndGetBinaryContent implements WebTestCase {


	/**
	 * Service property
	 * 
	 * @scr.property type="Integer" value="10" description=
	 * 				"Specifies the number of threads to execute the run method. 
	 * 				NOTE: This test creates two HTTP connections per iteration)"
	 */
	public static final String THREAD_COUNT = "threadCount";

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

	private static final int CONTENT_LENGTH = 1024;
	private static final byte[] content = new byte[CONTENT_LENGTH];
	private static final String CONTENT_TYPE = "application/x-clerezza-test";
	
	private String requestUri;
	private static final String RESOURCE_PATH = "/putandgetbinarycontenttest-";

	final Logger logger = LoggerFactory.getLogger(PutAndGetBinaryContent.class);

	protected void activate(ComponentContext componentContext) {
		username = (String) componentContext.getProperties().get(USER_NAME);
		password = (String) componentContext.getProperties().get(USER_PASSWORD);
	}

	@Override
	public void init(String testSubjectUriPrefix) {
		logger.info("Init " + getClass().getName());

		for (int i = 0; i < CONTENT_LENGTH; ++i) {
			content[i] = 1;
		}

		requestUri = testSubjectUriPrefix;

	}

	private void put() {
		try {
			URL url = new URL(requestUri + RESOURCE_PATH
					+ Thread.currentThread().getName());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			try {
				con.setRequestMethod("PUT");
				con.setRequestProperty("Accept", "*/*");
				con.setRequestProperty("Content-Type", CONTENT_TYPE);
				con.setRequestProperty("Authorization", "Basic "
						+ new String(Base64
								.encodeBase64((username + ":" + password)
										.getBytes())));
				con.setDoOutput(true);
				OutputStream os = con.getOutputStream();
				try {
					os.write(content);
				} finally {
					os.close();
				}

				int responseCode = con.getResponseCode();
				if (responseCode != 201 && responseCode != 202) {
					throw new RuntimeException(getClass().getSimpleName()
							+ ": unexpected " + "response code: "
							+ responseCode);
				}
			} finally {
				con.disconnect();
			}
		} catch (MalformedURLException me) {
			throw new RuntimeException(me);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private void get() {
		try {
			URL url = new URL(requestUri + RESOURCE_PATH
					+ Thread.currentThread().getName());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			try {
				con.setRequestMethod("GET");
				con.setRequestProperty("Accept", "*/*");
				con.setRequestProperty("Content-Type", CONTENT_TYPE);
				con.setRequestProperty("Authorization", "Basic "
						+ new String(Base64
								.encodeBase64((username + ":" + password)
										.getBytes())));
				con.setDoInput(true);

				int responseCode = con.getResponseCode();
				if (responseCode != 200) {
					throw new RuntimeException(getClass().getSimpleName()
							+ ": unexpected " + "response code: "
							+ responseCode);
				}

				InputStream inputStream = con.getInputStream();
				try {
					int byteData;
					int ctr = 0;
					while ((byteData = inputStream.read()) != -1) {
						if (byteData != 1) {
							throw new RuntimeException(getClass()
									.getSimpleName()
									+ ": Read data does not match input data.");
						}
						++ctr;
					}
					if (ctr != CONTENT_LENGTH) {
						throw new RuntimeException(getClass().getSimpleName()
								+ ": Content length does not match.");
					}
				} finally {
					inputStream.close();
				}
			} finally {
				con.disconnect();
			}
		} catch (MalformedURLException me) {
			me.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public void run() {
		put();
		get();
	}

	@Override
	public boolean multiThreadingCapable() {
		return true;
	}
}
