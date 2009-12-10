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
package org.apache.clerezza.platform.dashboard.ontology;

import org.apache.clerezza.rdf.core.UriRef;

public class DASHBOARD {
	//Classes

	/**
	 * A html containing the dashboard.
	 */
	public static final UriRef DashBoard = new UriRef("http://clerezza.org/2009/06/dashboard#DashBoard");
	
	/**
	 * A html include containing the dashboardMenu.
	 */
	public static final UriRef DashBoardMenu = new UriRef("http://clerezza.org/2009/06/dashboard#DashBoardMenu");
	
	//Properties

	/**
	 * Points to an RdfList containing the list of dashboard entries.
	 */
	public static final UriRef includeDashBoardMenu = new UriRef("http://clerezza.org/2009/06/dashboard#includeDashBoardMenu");

	/**
	 * Points to a label of a dashboard entry 
	 */
	public static final UriRef hasLabel = new UriRef("http://clerezza.org/2009/06/dashboard#hasLabel");
	
	/**
	 * Points to a group of modules 
	 */
	public static final UriRef hasDashBoardGroup = new UriRef("http://clerezza.org/2009/06/dashboard#hasDashBoardGroup");
	
	/**
	 * Points to a group label of a dashboard entry 
	 */
	public static final UriRef hasDashBoardGroupLabel = new UriRef("http://clerezza.org/2009/06/dashboard#hasDashBoardGroupLabel");
	
	/**
	 * Points to a url of a dashboard entry 
	 */
	public static final UriRef hasRelativeUrl = new UriRef("http://clerezza.org/2009/06/dashboard#hasRelativeUrl");
	
	/**
	 * Points to a the name of the logged in user 
	 */
	public static final UriRef hasUser = new UriRef("http://clerezza.org/2009/06/dashboard#hasUser");
	
}
