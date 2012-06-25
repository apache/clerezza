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
package org.apache.clerezza.platform.documentation;

import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;

/**
 * Registers the documentation graph as addition to the content graph
 * 
 * @scr.component
 * 
 * @author rbn, hasan
 */
public class DocumentationInContentGraph {


	/**
	 * we have this dependency to make sure we get started after the documentatinProvider
	 *
	 * @scr.reference
	 */
	private DocumentationProvider documentationProvider;

	/**
	 * @scr.reference
	 */
	private ContentGraphProvider cgProvider;

	protected void activate(final ComponentContext componentContext) {
		cgProvider.addTemporaryAdditionGraph(DocumentationProvider.DOCUMENTATION_GRAPH_URI);
	}

	protected void deactivate(final ComponentContext componentContext) {
		cgProvider.removeTemporaryAdditionGraph(DocumentationProvider.DOCUMENTATION_GRAPH_URI);
	}
	
}
