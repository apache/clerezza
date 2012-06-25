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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.net.URLEncoder;
import org.apache.commons.codec.binary.Base64;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.integrationtest.web.framework.WebTestCase;

/**
 *
 * A <code>WebTestCase</code> for testing the SPAQRL endpoint. The test creates
 * a SPARQL CONSTRUCT query and send a GET-request. The request accepts
 * "application/rdf+xml" 
 *
 * @scr.component
 * @scr.service 
 *              interface="org.apache.clerezza.integrationtest.web.framework.WebTestCase"
 * 
 * @author tio
 * 
 */
public class GetSparqlQuery implements WebTestCase {

	/**
	 * Service property
	 * 
	 * @scr.property type="Integer" value="10" description=
	 *               "Specifies the number of threads to execute the run method."
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
	private static final String UTF8 = "UTF-8";
	private String requestUri;
	private String query;
	final Logger logger = LoggerFactory.getLogger(GetSparqlQuery.class);

	protected void activate(ComponentContext componentContext) {
		username = (String) componentContext.getProperties().get(USER_NAME);
		password = (String) componentContext.getProperties().get(USER_PASSWORD);
	}

	@Override
	public void init(String testSubjectUriPrefix) {

		logger.info("Init sparql query get request");

		query = "CONSTRUCT { <http://example.org> " +
				"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x . }" +
				"WHERE { ?y <http://clerezza.org/2009/04/typerendering#mediaType> ?x . }";
		requestUri = testSubjectUriPrefix + "/sparql";

	}

	@Override
	public void run() {
		try {
			String queryParams = URLEncoder.encode("query", UTF8) + "=" + URLEncoder.encode(query, UTF8);
			queryParams += "&";
			queryParams += URLEncoder.encode("default-graph-uri", UTF8) + "=" +
					URLEncoder.encode("urn:x-localinstance:/content.graph", UTF8);
			URL url = new URL(requestUri + "?" + queryParams);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			try {
				con.setRequestMethod("GET");
				con.setRequestProperty("Accept", "application/rdf+xml");
				con.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64((username + ":" + password).getBytes())));
				con.setDoOutput(true);

				int responseCode = con.getResponseCode();
				if (responseCode != 200) {
					throw new RuntimeException("GetSparqlQuery: unexpected " + "response code: " + responseCode);
				}
				String contentType = con.getContentType();
				if (contentType == null) {
					throw new RuntimeException(
							"GetSparqlQuery: Couldn't determine content type.");
				}
				int length = con.getContentLength();
				byte[] content = new byte[length];
				InputStream inputStream = con.getInputStream();
				try {
					int i = 0;
					while (i < length && ((content[i++] = (byte) inputStream.read()) != -1)) {
					}
					if (i != length) {
						throw new RuntimeException(
								"GetSparqlQuery: Couldn't read all data.");
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
	public boolean multiThreadingCapable() {
		return true;
	}
}
