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
package org.apache.clerezza.templating;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * A templating engine parses and expands a template 
 * with data from a GraphNode resource.
 * 
 * @author daniel
 */
public interface TemplatingEngine {
	
	/**
	 * Parses and expands the <code>template</code> 
	 * with data from <code>res</code>.
	 * 
	 * @param res  RDF resource to be rendered with the template.
	 * @param user  RDF resource describing the current user.
	 * @param RenderingFunctions functions that can be applied on the valued from the <code>GraphNode</code>s
	 * @param template  the template URL. 
	 * @param os  where the output will be written to.
	 */
	public void process(GraphNode res, 
			GraphNode user,
			RenderingFunctions renderingFunctions,
			URL template,
			OutputStream os) throws IOException;
}
