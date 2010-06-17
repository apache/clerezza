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
package org.apache.clerezza.platform.config;


import java.io.IOException;
import java.net.URL;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
	
/**
 * When the <code>SystemConfig</code> component is activated it checks if the
 * system graph exists, in case it does not exist then it creates the
 * system graph and writes the default platform configuration into it.

 * @author mir
 */
@Component
public class SystemConfig {
	public static final String CONFIG_FILE = "/META-INF/config.rdf";
		
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String SYSTEM_GRAPH_STRING =
			"http://tpf.localhost/system.graph";
	
	public static final UriRef SYSTEM_GRAPH_URI =
			new UriRef(SYSTEM_GRAPH_STRING);

	/**
	 * A filter that can be used to get the system graph as OSGi service,
	 * that is provided by <code>org.apache.clerezza.rdf.core.access.TcManager</code>.
	 */
	public static final String SYSTEM_GRAPH_FILTER =
			"(name="+ SYSTEM_GRAPH_STRING +")";
	public static final String PARSER_FILTER =
			"(supportedFormat=" + SupportedFormat.RDF_XML +")";

	@Reference
	private TcManager tcManager;
	
	@Reference(target=PARSER_FILTER)
	private ParsingProvider parser;

	protected void activate(ComponentContext componentContext) {
		try {
			tcManager.getMGraph(SYSTEM_GRAPH_URI);
		} catch (NoSuchEntityException nsee) {
			MGraph systemGraph = tcManager.createMGraph(SYSTEM_GRAPH_URI);
			Graph configGraph = readConfigGraphFile();
			logger.info("Add initial configuration to system graph");
			systemGraph.addAll(configGraph);
		}
	}

	private Graph readConfigGraphFile() {
		URL config = getClass().getResource(CONFIG_FILE);
		if (config == null) {
			throw new RuntimeException("no config file found");
		}
		try {
			return parser.parse(config.openStream(),
					SupportedFormat.RDF_XML, null);
		} catch (IOException ex) {
			logger.warn("Cannot parse coniguration at URL: {}", config);
			throw new RuntimeException(ex);
		}
	}

}
