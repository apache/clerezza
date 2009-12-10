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
package org.apache.clerezza.templating.seedsnipe.simpleparser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.templating.RenderingFunctions;
import org.apache.clerezza.templating.TemplatingEngine;
import org.apache.clerezza.templating.seedsnipe.graphnodeadapter.GraphNodeDataFieldResolver;

/**
 * The default parser for RDF content. Uses {@link DefaultParser}.
 * 
 * @author daniel
 * 
 * @scr.component
 * @scr.service interface="org.apache.clerezza.templating.TemplatingEngine"
 */
public class SeedsnipeTemplatingEngine implements TemplatingEngine {

	@Override
	public void process(GraphNode res, GraphNode user,
			RenderingFunctions renderingFunctions, URL template,
			OutputStream os) throws IOException {

		try {
			InputStreamReader in = new InputStreamReader(template.openStream(), "UTF-8");
			OutputStreamWriter out = new OutputStreamWriter(os, "UTF-8");

			new DefaultParser(in, out).perform(new GraphNodeDataFieldResolver(res,
					renderingFunctions));

			in.close();
			out.flush();
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}
}
