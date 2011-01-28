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
package org.apache.clerezza.platform.concepts.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;

/**
 * This implementation of {@link ConceptProvider} provides a functionality
 * to query a SPARQL End Point for a given search term. The query is to be
 * generated from a template by replacing the place holder for the search term
 * with the specified search term.
 *
 * @author tio, hasan
 */
public class RemoteConceptProvider implements ConceptProvider {

	private UriRef sparqlEndPoint = null;
	private UriRef defaultGraph = null;
	private String queryTemplate = null;
	private ConceptCache conceptCache = null;
	private RemoteConceptsDescriptionManager remoteConceptsDescriptionManager = null;

	/**
	 * Constructs a {@link RemoteConceptProvider} with the specified parameters.
	 * 
	 * @param sparqlEndPoint
	 *		the SPARQL End Point to connect to
	 * @param defaultGraph
	 *		the Graph to query for concepts
	 * @param queryTemplate
	 *		the template for query containing place holders for the search term.
	 */
	public RemoteConceptProvider(
			UriRef sparqlEndPoint, UriRef defaultGraph, String queryTemplate) {
		this.sparqlEndPoint = sparqlEndPoint;
		this.defaultGraph = defaultGraph;
		this.queryTemplate = queryTemplate;
		this.conceptCache = new ConceptCache(sparqlEndPoint, defaultGraph);
		this.remoteConceptsDescriptionManager = new RemoteConceptsDescriptionManager();
	}

	@Override
	public Graph retrieveConcepts(String searchTerm) {

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -30);
		Date acceptableOldestCachingDate = calendar.getTime();
		MGraph cachedConcepts = conceptCache.retrieve(searchTerm.toLowerCase(),
				acceptableOldestCachingDate);
		if (cachedConcepts != null) {
			return cachedConcepts.getGraph();
		} else {
			final int connectionTimeout = 4000;
			String query = queryTemplate.replace("${searchTerm}", searchTerm);
			try {
				String param = "query=" + URLEncoder.encode(query, "UTF-8");
				if (defaultGraph != null) {
					param += "&default-graph-uri=" + defaultGraph.getUnicodeString();
				}
				final URL url = new URL(sparqlEndPoint.getUnicodeString());
				final HttpURLConnection con =
						(HttpURLConnection) url.openConnection();
				con.setRequestProperty("Accept", "application/rdf+xml");
				con.setRequestMethod("POST");
				con.setDoOutput(true);
				con.setDoInput(true);
				con.setUseCaches(false);
				con.setConnectTimeout(connectionTimeout);

				final OutputStream os = con.getOutputStream();
				os.write(param.getBytes());
				os.close();

				if (con.getResponseCode() < 400) {
					final InputStream is = con.getInputStream();
					Graph parsedGraph = Parser.getInstance().parse(is,
							SupportedFormat.RDF_XML);
					is.close();
					conceptCache.cache(searchTerm.toLowerCase(), parsedGraph);
					remoteConceptsDescriptionManager.storeConceptsDescription(parsedGraph);
					return parsedGraph;
				} else {
					final InputStream es = con.getErrorStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					for (int ch = es.read(); ch != -1; ch = es.read()) {
						baos.write(ch);
					}
					es.close();
					throw new RuntimeException(baos.toString());
				}
			} catch (ProtocolException ex) {
				throw new RuntimeException(ex);
			} catch (MalformedURLException ex) {
				throw new RuntimeException(ex);
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
