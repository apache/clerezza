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
package org.apache.clerezza.platform.scripting.scriptmanager.ontology;

import org.apache.clerezza.rdf.core.UriRef;

/**
 * ScriptManager ontology.
 *
 * @author daniel, marc
 */
public class SCRIPTMANAGER {
	// Classes

	/**
	 * A web page containing scripts.
	 */
	public static final UriRef ScriptManagerOverviewPage = new UriRef("http://clerezza.org/2009/07/scriptmanager#ScriptManagerOverviewPage");
	
	/**
	 * A web page to install a script.
	 */
	public static final UriRef ScriptManagerInstallPage = new UriRef("http://clerezza.org/2009/07/scriptmanager#ScriptManagerInstallPage");
	
	/**
	 * A web page to manage execution URIs of scripts.
	 */
	public static final UriRef ExecutionUriOverviewPage = new UriRef("http://clerezza.org/2009/07/scriptmanager#ExecutionUriOverviewPage");
	
	/**
	 * A web page snippet representing a list of Execution URIs for a specified script.
	 */
	public static final UriRef ExecutionUriList = new UriRef("http://clerezza.org/2009/07/scriptmanager#ExecutionUriList");
	
	/**
	 * The script that is currently selected.
	 */
	public static final UriRef SelectedScript = new UriRef("http://clerezza.org/2009/07/scriptmanager#SelectedScript");
	
	/**
	 * A rendered xhtml snippet containing the script list of installed scripts.
	 */
	public static final UriRef ScriptList = new UriRef("http://clerezza.org/2009/07/scriptmanager#ScriptList");


	//Properties
	/**
	 * Points to a description of a script language in terms of language name and version.
	 */
	public static final UriRef scriptLanguageDescription = new UriRef("http://clerezza.org/2009/07/scriptmanager#scriptLanguageDescription");

	/**
	 * Points to a Script.
	 */
	public static final UriRef script = new UriRef("http://clerezza.org/2009/07/scriptmanager#script");
	
	/**
	 * A script source code of a specific script
	 */
	public static final UriRef code = new UriRef("http://clerezza.org/2009/07/scriptmanager#code");
	
	/**
	 * Points to a list of all available scripts.
	 */
	public static final UriRef scriptList = new UriRef("http://clerezza.org/2009/07/scriptmanager#scriptList");

	/**
	 * A list of all available execution URIs for a specific script
	 */
	public static final UriRef executionUri = new UriRef("http://clerezza.org/2009/07/scriptmanager#executionUri");
}
