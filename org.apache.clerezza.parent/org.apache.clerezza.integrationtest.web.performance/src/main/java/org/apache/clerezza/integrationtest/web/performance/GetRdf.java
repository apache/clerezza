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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.integrationtest.web.framework.WebTestCase;

/**
 * @scr.component
 * @scr.service 
 *              interface="org.apache.clerezza.integrationtest.web.framework.WebTestCase"
 * 
 * @author hasan, daniel
 * 
 * @since version 0.1
 */
public class GetRdf implements WebTestCase {

	/**
	 * Service property
	 * 
	 * @scr.property type="Integer" value="3" description=
	 *               "Specifies the number of threads to execute the run method."
	 */
	public static final String THREAD_COUNT = "threadCount";

	/**
	 * The URI of the resource after POSTed to the subject under test
	 */
	private String resourceUri;

	/**
	 * The RDF resource as a String
	 */
	private String rdfResource;

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
	
	private String authString;

	final Logger logger = LoggerFactory.getLogger(GetRdf.class);


	protected void activate(ComponentContext componentContext) throws UnsupportedEncodingException {
		username = (String) componentContext.getProperties().get(USER_NAME);
		password = (String) componentContext.getProperties().get(USER_PASSWORD);
		authString = new String(Base64.encodeBase64((username + ":" + password).
				getBytes()), "UTF-8");
	}
	
	@Override
	public void init(String testSubjectUriPrefix) {

		logger.info("Init GetRdf");

		resourceUri = testSubjectUriPrefix + "/getrdftestcase";

		String postRequestUri = testSubjectUriPrefix
				+ "/tools/editor/post?resource=" + resourceUri;

		post(postRequestUri);
	}

	private void post(String postRequestUri) {

		HttpURLConnection con = null;

		try {
			rdfResource =
"<rdf:RDF\n" +
"    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
"    xmlns:j.0=\"http://discobits.org/ontology#\" > \n" +
"  <rdf:Description rdf:about=\"" + resourceUri + "\">\n" +
"    <rdf:type rdf:resource=\"http://discobits.org/ontology#XHTMLInfoDiscoBit\"/>\n" +
"    <j.0:infoBit rdf:parseType=\"Literal\">some <em xmlns=\"http://www.w3.org/1999/xhtml\">xhtml</em> content</j.0:infoBit>\n" +
"  </rdf:Description>\n" +
"</rdf:RDF>\n";
			
			URL url = new URL(postRequestUri);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "*/*");
			con.setRequestProperty("Content-Type", "application/rdf+xml");
			con.setRequestProperty("Authorization", "Basic " + authString);
			con.setInstanceFollowRedirects(false);
			con.setDoOutput(true);

			OutputStream outputStream = con.getOutputStream();
			outputStream.write(rdfResource.getBytes("UTF-8"));
			outputStream.close();

			int responseCode = con.getResponseCode();
			if (responseCode >= 400) {
				throw new RuntimeException("GetRdf: (POST) unexpected "
						+ "response code: " + responseCode);
			}
		} catch (MalformedURLException me) {
			throw new RuntimeException(me);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		} finally {
			con.disconnect();
		}
	}

	@Override
	public void run() {

		try {
			URL url = new URL(resourceUri);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			try {
				con.setRequestMethod("GET");
				con.setRequestProperty("Accept", "application/rdf+xml");
				con.setRequestProperty("Authorization", "Basic " + authString);
				con.setDoInput(true);

				int responseCode = con.getResponseCode();
				if (responseCode != 200) {
					throw new RuntimeException("GetRdf: (GET) unexpected "
							+ "response code: " + responseCode);
				}

				String contentType = con.getContentType();
				if (contentType == null) {
					throw new RuntimeException(
							"GetRdf: Couldn't determine content type.");
				}

				int length = con.getContentLength();
				byte[] content = new byte[length];
				InputStream inputStream = con.getInputStream();
				try {

					int i = 0;
					while (i < length
							&& ((content[i++] = (byte) inputStream.read()) != -1)) {
					}
					if (i != length) {
						throw new RuntimeException(
								"GetRdf: Couldn't read all data.");
					}
					String contentStr = new String(content,
							getCharSet(contentType));

					if (!contentStr.contains(getInfoBitString(rdfResource))) {
						throw new RuntimeException(
								"GetRdf: Content does not contain expected information.");
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

	private String getCharSet(String contentType) {
		int idx = contentType.toLowerCase().indexOf("charset=");
		if (idx == -1) {
			return "UTF-8";
		}
		String[] charSet = contentType.substring(idx).split("=");
		if (charSet.length != 2) {
			throw new RuntimeException("GetRdf: Invalid Content-Type");
		}
		return charSet[1];
	}

	@Override
	public boolean multiThreadingCapable() {
		return true;
	}
	
	private String getInfoBitString(String rdfResourceString) {
		String dataStr = rdfResourceString;
		int fromIdx = dataStr.indexOf("<infoBit");
		if (fromIdx == -1) {
			return dataStr;
		}
		int infoBitStart = 1 + dataStr.indexOf('>', fromIdx);
		int infoBitEnd = dataStr.indexOf("</infoBit>");
		if (infoBitEnd == -1) {
			return dataStr;
		}
		return dataStr.substring(infoBitStart, infoBitEnd);
	}
}
