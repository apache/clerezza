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
import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.platform.graphprovider.content.GraphNameTransitioner;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
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
	public static final String CONFIG_FILE = "default-system-graph.rdf";
		
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 *
	 * @deprecated use org.apache.clerezza.platform.Contants instead
	 */
	@Deprecated
	public static final UriRef SYSTEM_GRAPH_URI = Constants.SYSTEM_GRAPH_URI;

	/**
	 * A filter that can be used to get the system graph as OSGi service,
	 * that is provided by <code>org.apache.clerezza.rdf.core.access.TcManager</code>.
	 */
	public static final String SYSTEM_GRAPH_FILTER =
			"(name="+ Constants.SYSTEM_GRAPH_URI_STRING +")";
	public static final String PARSER_FILTER =
			"(supportedFormat=" + SupportedFormat.RDF_XML +")";

	@Reference
	private TcManager tcManager;
	
	@Reference(target=PARSER_FILTER)
	private ParsingProvider parser;

	protected void activate(ComponentContext componentContext) {
		GraphNameTransitioner.renameGraphsWithOldNames(tcManager);
		try {
			tcManager.getMGraph(Constants.SYSTEM_GRAPH_URI);
		} catch (NoSuchEntityException nsee) {
			MGraph systemGraph = tcManager.createMGraph(Constants.SYSTEM_GRAPH_URI);
			logger.info("Add initial configuration to system graph");
			readConfigGraphFile(systemGraph);
			
		}
	}

	private void readConfigGraphFile(MGraph mGraph) {
		URL config = getClass().getResource(CONFIG_FILE);
		if (config == null) {
			throw new RuntimeException("no config file found");
		}
		try {
			parser.parse(mGraph, config.openStream(),
					SupportedFormat.RDF_XML, null);
		} catch (IOException ex) {
			logger.warn("Cannot parse coniguration at URL: {}", config);
			throw new RuntimeException(ex);
		}
	}

}
